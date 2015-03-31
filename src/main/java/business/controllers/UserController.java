package business.controllers;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import business.models.ContactData;
import business.models.Lab;
import business.models.LabRepository;
import business.models.Role;
import business.models.RoleRepository;
import business.models.User;
import business.models.UserRepository;
import business.representation.ProfileRepresentation;

@RestController
public class UserController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    LabRepository labRepository;

    @RequestMapping("/user")
    public Principal user(Principal user) {
        return user;
    }

    @RequestMapping(value = "/admin/user", method = RequestMethod.GET)
    public ProfileRepresentation get(@RequestParam String username) {
        return new ProfileRepresentation(userRepository.findByUsernameAndDeletedFalse(username));
    }

    @RequestMapping(value = "/admin/users", method = RequestMethod.GET)
    public List<ProfileRepresentation> getAll(Principal principal) {
        LogFactory.getLog(getClass()).info("GET /admin/users (for user: " + principal.getName() + ")");
        List<ProfileRepresentation> users = new ArrayList<ProfileRepresentation>();
        for(User user: userRepository.findByActiveTrueAndDeletedFalse()) {
            users.add(new ProfileRepresentation(user));
        }
        return users;
    }
    
    @ResponseStatus(value=HttpStatus.BAD_REQUEST)  // 
    public class InvalidUserDataException extends RuntimeException {
        private static final long serialVersionUID = -7706933733462824596L;
        public InvalidUserDataException(String message) {
            super(message);
        }
    }

    @ResponseStatus(value=HttpStatus.NOT_MODIFIED, reason="Email address not available.")
    public class EmailAddressNotAvailableException extends RuntimeException {
        private static final long serialVersionUID = -2294620434526249799L;
    }
   
    public void transferUserData(ProfileRepresentation body, User user) {
        user.setFirstName(body.getFirstName());
        user.setLastName(body.getLastName());
        user.setPathologist(body.isPathologist());
        user.setInstitute(body.getInstitute());
        user.setSpecialism(body.getSpecialism());
        Lab lab = null;
        if (user.isLabUser() || user.isPathologist()) {
            lab = labRepository.findOne(body.getLabId());
            if (lab == null) {
                throw new InvalidUserDataException("No lab selected.");
            }
        }
        user.setLab(lab);
        
        if (body.getContactData() == null) {
            throw new InvalidUserDataException("No contact data entered.");
        }
        if (user.getContactData() == null) {
            user.setContactData(new ContactData());
        }
        user.getContactData().copy(body.getContactData());

        // copy email address
        String email = body.getContactData().getEmail();
        if (email == null) {
            throw new InvalidUserDataException("No email address entered.");
        }
        if (user.getUsername() == null || !user.getUsername().equals(email)) {
            // check for uniqueness (also enforced by database):
            User u = userRepository.findByUsernameAndDeletedFalse(email);
            if (u == null) {
                user.setUsername(email);
            } else {
                throw new EmailAddressNotAvailableException();
            }
        }
    }

    private ProfileRepresentation createNewUser(ProfileRepresentation body) {
        if (body.getPassword1() != null && body.getPassword1().equals(body.getPassword2()))
        {
            if (userRepository.findByUsername(body.getUsername()) != null ) {
                throw new IllegalArgumentException("Credentials already exist in our system.");
            }

            Role role = roleRepository.findByName(body.getCurrentRole());
            Set<Role> roles;
            if (role == null) {
                throw new InvalidUserDataException("No role selected.");
            } else {
                roles = Collections.singleton(role);
            }

            User user = new User(body.getUsername(), body.getPassword1(), true, roles);

            transferUserData(body, user);
            return new ProfileRepresentation(userRepository.save(user));
        }
        else
        {
            throw new InvalidUserDataException("Passwords do not match.");
        }
    }

    @RequestMapping(value = "/admin/users", method = RequestMethod.POST)
    public ProfileRepresentation create(Principal principal, @RequestBody ProfileRepresentation body) {
        LogFactory.getLog(getClass()).info("POST /admin/users (for user: " + principal.getName() + ")");
        return createNewUser(body);
    }

    @ResponseStatus(value=HttpStatus.NOT_FOUND, reason="User not found.")  // 404
    public class UserNotFoundException extends RuntimeException {
        private static final long serialVersionUID = -7666653096938904964L;
    }
    
    @RequestMapping(value = "/admin/users/{id}", method = RequestMethod.PUT,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ProfileRepresentation update(Principal principal, @PathVariable Long id, @RequestBody ProfileRepresentation body) {
        LogFactory.getLog(getClass()).info("PUT /admin/users/" + id);
        User user = userRepository.findOne(id);
        if (user != null) {
            transferUserData(body, user);
            return new ProfileRepresentation(userRepository.save(user));
        }
        throw new UserNotFoundException();
    }    
    
    @RequestMapping(value = "/admin/users/{id}/activate", method = RequestMethod.PUT)
    public ProfileRepresentation activate(Principal principal, @PathVariable String id) {
        LogFactory.getLog(getClass()).info("PUT /admin/users/" + id + "/activate");
        Long userId = Long.valueOf(id);
        User user = userRepository.getOne(userId);
        user.activate();
        return new ProfileRepresentation(userRepository.save(user));
    }

    @RequestMapping(value = "/admin/users/{id}/deactivate", method = RequestMethod.PUT)
    public ProfileRepresentation deactivate(Principal principal, @PathVariable String id) {
        LogFactory.getLog(getClass()).info("PUT /admin/users/" + id + "/deactivate");
        Long userId = Long.valueOf(id);
        User user = userRepository.getOne(userId);
        user.deactivate();
        return new ProfileRepresentation(userRepository.save(user));
    }
    
    @RequestMapping(value = "/admin/users/{id}/delete", method = RequestMethod.PUT)
    public void delete(Principal principal, @PathVariable String id) {
        LogFactory.getLog(getClass()).info("PUT /admin/users/" + id + "/delete");
        Long userId = Long.valueOf(id);
        User user = userRepository.getOne(userId);
        user.markDeleted();
        userRepository.save(user);
    }

    @RequestMapping(value = "/register/users", method = RequestMethod.POST)
    public ProfileRepresentation register(@RequestBody ProfileRepresentation body) {
        LogFactory.getLog(getClass()).info("POST /register new user");
        return createNewUser(body);
    }

}
