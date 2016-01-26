package business.controllers;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import business.exceptions.EmailAddressNotAvailable;
import business.exceptions.EmailAddressNotUnique;
import business.exceptions.InvalidPassword;
import business.exceptions.InvalidUserData;
import business.exceptions.UserNotFound;
import business.models.ActivationLink;
import business.models.ActivationLinkRepository;
import business.models.ContactData;
import business.models.Lab;
import business.models.LabRepository;
import business.models.Role;
import business.models.RoleRepository;
import business.models.User;
import business.representation.ProfileRepresentation;
import business.security.UserAuthenticationToken;
import business.services.MailService;
import business.services.UserService;
import business.validation.PasswordValidator;

@RestController
public class UserController {

    Log log = LogFactory.getLog(getClass());

    @Value("${dntp.server-name}")
    String serverName;

    @Value("${dntp.server-port}")
    String serverPort;

    @Value("${dntp.activation-link.expiry-hours}")
    @NotNull
    Integer activationLinkExpiryHours;

    @Autowired
    UserService userService;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    LabRepository labRepository;

    @Autowired
    ActivationLinkRepository activationLinkRepository;

    @Autowired
    MailService mailService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @RequestMapping("/user")
    public ProfileRepresentation user(UserAuthenticationToken user) {
        log.info("GET /user");
        return new ProfileRepresentation(user.getUser());
    }

    @RequestMapping(value = "/register/users/activate/{token}", method = RequestMethod.GET)
    public ResponseEntity<Object> activateUser(@PathVariable String token) {
        log.info("activation link: expiry hours = " + activationLinkExpiryHours);
        ActivationLink link = activationLinkRepository.findByToken(token);

        if (link == null) {
            return new ResponseEntity<Object>(HttpStatus.BAD_REQUEST);
        }
        // Check that the link has been issued in the previous week
        long linkAge = TimeUnit.MILLISECONDS.toHours(new Date().getTime() - link.getCreationDate().getTime()); // hours
        log.info("activation link age in hours: " + linkAge);
        if (linkAge <= activationLinkExpiryHours) {
            User user = link.getUser();
            user.setEmailValidated(true);
            userService.save(user);
            activationLinkRepository.delete(link);
            return new ResponseEntity<Object>(HttpStatus.OK);
        } else {
            // The activation link doesn't exist or is outdated!
            return new ResponseEntity<Object>(HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/admin/user", method = RequestMethod.GET)
    public ProfileRepresentation get(@RequestParam String username) {
        return new ProfileRepresentation(userService.findByUsername(username));
    }

    @RequestMapping(value = "/admin/users", method = RequestMethod.GET)
    public List<ProfileRepresentation> getAll(Principal principal) {
        log.info("GET /admin/users (for user: " + principal.getName() + ")");
        List<ProfileRepresentation> users = new ArrayList<ProfileRepresentation>();
        for(User user: userService.findAll()) {
            users.add(new ProfileRepresentation(user));
        }
        return users;
    }

    @RequestMapping(value = "/admin/users/scientific_council", method = RequestMethod.GET)
    public List<ProfileRepresentation> getScientificCouncilMembers(Principal principal) {
        log.info("GET /admin/users/scientific_council (for user: " + principal.getName() + ")");
        List<ProfileRepresentation> users = new ArrayList<ProfileRepresentation>();
        for(User user: userService.findScientificCouncilMembers()) {
            users.add(new ProfileRepresentation(user));
        }
        return users;
    }
    
    public void transferUserData(ProfileRepresentation body, User user) {
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
        if (user.getUsername() == null || !user.getUsername().equals(email)) {
            // check for uniqueness (also enforced by user service):
            User u = userService.findByUsername(email);
            if (u == null) {
                user.setUsername(email);
            } else {
                throw new EmailAddressNotAvailable();
            }
        }

        // change role
        ProfileRepresentation representation = new ProfileRepresentation(user);
        if (!representation.getCurrentRole().equals(body.getCurrentRole())) {
            Role role = roleRepository.findByName(body.getCurrentRole());
            if (role == null) {
                throw new InvalidUserData("Unknown role selected.");
            }
            Set<Role> roles = new HashSet<Role>();
            roles.add(role);
            user.setRoles(roles);
        }
        
        if (user.isRequester() || user.isLabUser()) {
            if (body.getLabId() == null) {
                throw new InvalidUserData("No lab selected.");
            }
            Lab lab = labRepository.findOne(body.getLabId());
            if (lab == null) {
                throw new InvalidUserData("No lab selected.");
            }
            user.setLab(lab);
        }
        
        if (body.getContactData() == null) {
            throw new InvalidUserData("No contact data entered.");
        }
        if (user.getContactData() == null) {
            user.setContactData(new ContactData());
        }
        user.getContactData().copy(body.getContactData());

    }

    private ProfileRepresentation createNewUser(ProfileRepresentation body) {
        if (body.getPassword1() != null && body.getPassword1().equals(body.getPassword2()))
        {
            if (userService.findByUsername(body.getUsername()) != null ) {
                throw new EmailAddressNotAvailable();
            }

            Role role = roleRepository.findByName(body.getCurrentRole());
            Set<Role> roles = new HashSet<Role>();
            if (role == null) {
                throw new InvalidUserData("No role selected.");
            } else {
                roles.add(role);
            }

            if (!PasswordValidator.validate(body.getPassword1())) {
                throw new InvalidPassword();
            }
            
            User user = new User(body.getUsername(), passwordEncoder.encode(body.getPassword1()), true, roles);

            transferUserData(body, user);
            try {
                User result = userService.save(user);

                // Generate and save activation link
                ActivationLink link = new ActivationLink(user);
                this.activationLinkRepository.save(link);
                
                // The user has been successfully saved. Send activation email
                mailService.sendActivationEmail(link);

                return new ProfileRepresentation(result);
            } catch (EmailAddressNotUnique e) {
                throw new EmailAddressNotAvailable();
            }
        }
        else
        {
            throw new InvalidUserData("Passwords do not match.");
        }
    }

    @RequestMapping(value = "/admin/users", method = RequestMethod.POST)
    public ProfileRepresentation create(Principal principal, @RequestBody ProfileRepresentation body) {
        LogFactory.getLog(getClass()).info("POST /admin/users (for user: " + principal.getName() + ")");
        return createNewUser(body);
    }

    @RequestMapping(value = "/admin/users/{id}", method = RequestMethod.PUT,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ProfileRepresentation update(Principal principal, @PathVariable Long id, @RequestBody ProfileRepresentation body) {
        log.info("PUT /admin/users/" + id);
        User user = userService.findOne(id);
        if (user != null) {
            transferUserData(body, user);
            try {
                User result = userService.save(user);
                return new ProfileRepresentation(result);
            } catch(EmailAddressNotUnique e) {
                throw new EmailAddressNotAvailable();
            }
        }
        throw new UserNotFound();
    }    

    @RequestMapping(value = "/admin/users/{id}/activate", method = RequestMethod.PUT)
    public ProfileRepresentation activate(Principal principal, @PathVariable String id) {
        log.info("PUT /admin/users/" + id + "/activate");
        Long userId = Long.valueOf(id);
        User user = userService.getOne(userId);
        user.activate();
        return new ProfileRepresentation(userService.save(user));
    }

    @RequestMapping(value = "/admin/users/{id}/deactivate", method = RequestMethod.PUT)
    public ProfileRepresentation deactivate(Principal principal, @PathVariable String id) {
        log.info("PUT /admin/users/" + id + "/deactivate");
        Long userId = Long.valueOf(id);
        User user = userService.getOne(userId);
        user.deactivate();
        return new ProfileRepresentation(userService.save(user));
    }

    @RequestMapping(value = "/admin/users/{id}/delete", method = RequestMethod.PUT)
    public void delete(Principal principal, @PathVariable String id) {
        log.info("PUT /admin/users/" + id + "/delete");
        Long userId = Long.valueOf(id);
        User user = userService.getOne(userId);
        user.markDeleted();
        userService.save(user);
    }

    @RequestMapping(value = "/register/users", method = RequestMethod.POST)
    public ProfileRepresentation register(@RequestBody ProfileRepresentation body) {
        log.info("POST /register new user");
        return createNewUser(body);
    }

    /**
     * Convert all usernames to lowercase.
     */
    @RequestMapping(value = "/admin/userNames/fix", method = RequestMethod.PUT)
    public void fixUserNames() {
        log.info("PUT /admin/userNames/fix");
        userService.lowerCaseUsernames();
    }

}
