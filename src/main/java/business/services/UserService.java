/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business.services;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import business.exceptions.EmailAddressNotAvailable;
import business.exceptions.EmailAddressNotUnique;
import business.exceptions.InvalidPassword;
import business.exceptions.InvalidUserData;
import business.models.ActivationLink;
import business.models.ActivationLinkRepository;
import business.models.ContactData;
import business.models.Lab;
import business.models.Role;
import business.models.RoleRepository;
import business.models.User;
import business.models.UserRepository;
import business.representation.ProfileRepresentation;
import business.validation.PasswordValidator;

@Service
@Transactional
public class UserService {

    public enum NewUserLinkType {
        ACTIVATION_LINK,
        PASSWORD_RESET_LINK,
        NONE
    }

    Log log = LogFactory.getLog(getClass());

    private Object lock = new Object();

    @PersistenceContext
    private EntityManager em;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    LabService labService;

    @Autowired
    MailService mailService;

    @Autowired
    PasswordService passwordService;

    @Autowired
    ActivationLinkRepository activationLinkRepository;

    @Transactional
    public User save(User user) throws EmailAddressNotAvailable {
        assert(user.getRoles().size() == 1);
        synchronized (lock) {
            em.persist(user);
            em.flush();
            em.refresh(user, LockModeType.PESSIMISTIC_WRITE);
            em.flush();
            long count = userRepository.countByUsernameAndDeletedFalse(user.getUsername());
            if (count > 1) {
                throw new EmailAddressNotUnique();
            }
        }
        return user;
    }

    public User findByUsername(String username) {
        return userRepository.findByUsernameAndDeletedFalse(username);
    }

    public User getOne(Long id) {
        return userRepository.getByIdAndDeletedFalse(id);
    }

    public User findOne(Long id) {
        return userRepository.findByIdAndDeletedFalse(id);
    }

    public List<User> findAll() {
        return userRepository.findByDeletedFalseOrderByUsernameAsc();
    }

    public List<User> findScientificCouncilMembers() {
        Role role = roleRepository.findByName("scientific_council");
        List<User> members = userRepository.findAllByDeletedFalseAndActiveTrueAndHasRole(role.getId());
        return members;
    }

    public List<User> findHubUsersForLab(Lab lab) {
        Role role = roleRepository.findByName("hub_user");
        return userRepository.findAllByDeletedFalseAndActiveTrueAndHasRoleAndHubLab(role.getId(), lab.getId());
    }

    public void transferUserData(User currentUser, ProfileRepresentation body, User user) {
        user.setFirstName(body.getFirstName());
        user.setLastName(body.getLastName());
        user.setPathologist(body.isPathologist());
        user.setInstitute(body.getInstitute());
        user.setSpecialism(body.getSpecialism());

        // copy email address
        String email = body.getContactData().getEmail();
        if (email == null) {
            throw new InvalidUserData("No email address entered.");
        }
        email = email.trim().toLowerCase();
        if (user.getUsername() == null || !user.getUsername().equals(email)) {
            // check for uniqueness (also enforced by user service):
            User u = findByUsername(email);
            if (u == null) {
                user.setUsername(email);
            } else {
                throw new EmailAddressNotAvailable();
            }
        }

        // change role
        ProfileRepresentation representation = new ProfileRepresentation(user);
        // Update of user roles can only be done by Palga users.
        if (currentUser != null && currentUser.isPalga()) {
            if (!representation.getCurrentRole().equals(body.getCurrentRole())) {
                Role role = roleRepository.findByName(body.getCurrentRole());
                if (role == null) {
                    throw new InvalidUserData("Unknown role selected.");
                }
                Set<Role> roles = new HashSet<Role>();
                roles.add(role);
                user.setRoles(roles);
            }
        }

        if (user.isRequester() || user.isLabUser()) {
            if (body.getLabId() == null) {
                throw new InvalidUserData("No lab selected.");
            }
            Lab lab = labService.findOne(body.getLabId());
            if (lab == null) {
                throw new InvalidUserData("No lab selected.");
            }
            user.setLab(lab);
        }
        if (user.isHubUser()) {
            if (body.getHubLabIds() == null || body.getHubLabIds().isEmpty()) {
                throw new InvalidUserData("No hub labs selected.");
            }
            Set<Lab> labs = new HashSet<>();
            for (Long labId: body.getHubLabIds()) {
                Lab lab = labService.findOne(labId);
                if (lab == null) {
                    throw new InvalidUserData("Selected hub lab not found.");
                }
                labs.add(lab);
            }
            user.setHubLabs(labs);
        }

        if (body.getContactData() == null) {
            throw new InvalidUserData("No contact data entered.");
        }
        if (user.getContactData() == null) {
            user.setContactData(new ContactData());
        }
        user.getContactData().copy(body.getContactData());

    }

    @Transactional
    public ProfileRepresentation createNewUser(User currentUser, ProfileRepresentation body, NewUserLinkType linkType) {
        if (body == null || body.getContactData() == null || body.getContactData().getEmail() == null) {
            throw new InvalidUserData("Invalid user data.");
        }
        if (body.getPassword1() == null || !body.getPassword1().equals(body.getPassword2())) {
            throw new InvalidUserData("Passwords do not match.");
        }
        String email = body.getContactData().getEmail().trim().toLowerCase();
        if (findByUsername(email) != null ) {
            throw new EmailAddressNotAvailable();
        }
        Role role = null;
        if (currentUser != null && currentUser.isPalga()) {
            role = roleRepository.findByName(body.getCurrentRole());
        } else {
            // when a user is registered by anyone else than a Palga user,
            // then only the requester role can be selected.
            role = roleRepository.findByName("requester");
        }
        Set<Role> roles = new HashSet<Role>();
        if (role == null) {
            throw new InvalidUserData("No role selected.");
        } else {
            roles.add(role);
        }

        if (!PasswordValidator.validate(body.getPassword1())) {
            throw new InvalidPassword();
        }

        User user = new User(email, passwordService.getEncoder().encode(body.getPassword1()), true, roles);

        transferUserData(currentUser, body, user);
        try {
            User result = save(user);

            if (linkType == NewUserLinkType.ACTIVATION_LINK) {
                // Generate and save activation link
                ActivationLink link = new ActivationLink(user);
                activationLinkRepository.save(link);
                // The user has been successfully saved. Send activation email
                mailService.sendActivationEmail(link);
            } else if (linkType == NewUserLinkType.PASSWORD_RESET_LINK) {
                passwordService.requestNewPassword(email);
            }
            return new ProfileRepresentation(result);
        } catch (EmailAddressNotUnique e) {
            throw new EmailAddressNotAvailable();
        }
    }

    /**
     * Convert all usernames to lowercase.
     */
    public void lowerCaseUsernames() {
        List<User> users = userRepository.findByDeletedFalseOrderByUsernameAsc();
        for(User user: users) {
            if (!user.getUsername().equals(user.getUsername().toLowerCase())) {
                log.info("Changing username " + user.getUsername() + " to lowercase: " + user.getUsername().toLowerCase());
                user.setUsername(user.getUsername().toLowerCase());
                save(user);
            }
        }
    }

    @Cacheable("users")
    public User findOneCached(Long userId) {
        return findOne(userId);
    }

}
