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
import org.springframework.web.bind.annotation.RestController;

import business.models.ContactData;
import business.models.Institute;
import business.models.InstituteRepository;
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

    @Autowired
    InstituteRepository instituteRepository;

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
   
    public ResponseEntity<Object> transferUserData(ProfileRepresentation body, User user) {
        user.setFirstName(body.getFirstName());
        user.setLastName(body.getLastName());
        user.setPathologist(body.isPathologist());
        Institute institute = null;
        if (user.isRequester()) {
            institute = instituteRepository.findOne(body.getInstituteId());
            if (institute == null) {
                return new ResponseEntity<Object>("No institute selected.", HttpStatus.BAD_REQUEST);
            }
        }
        user.setInstitute(institute);
        Lab lab = null;
        if (user.isLabUser()) {
            lab = labRepository.findOne(body.getLabId());
            if (lab == null) {
                return new ResponseEntity<Object>("No lab selected.", HttpStatus.BAD_REQUEST);
            }
        }
        user.setLab(lab);
        
        if (body.getContactData() == null) {
            return new ResponseEntity<Object>("No contact data entered.", HttpStatus.BAD_REQUEST);
        }
        if (user.getContactData() == null) {
            user.setContactData(new ContactData());
        }
        user.getContactData().copy(body.getContactData());

        // copy email address
        String email = body.getContactData().getEmail();
        if (email == null) {
            return new ResponseEntity<Object>("No email address entered.", HttpStatus.BAD_REQUEST);
        }
        if (user.getUsername() == null || !user.getUsername().equals(email)) {
            // check for uniqueness (also enforced by database):
            User u = userRepository.findByUsernameAndDeletedFalse(email);
            if (u == null) {
                user.setUsername(email);
            } else {
                return new ResponseEntity<Object>("Email address not available.", HttpStatus.NOT_MODIFIED);
            }
        }
        return null;
    }
    
    @RequestMapping(value = "/admin/users", method = RequestMethod.POST)
    public ResponseEntity<Object> create(Principal principal, @RequestBody ProfileRepresentation body) {
        LogFactory.getLog(getClass()).info("POST /admin/users (for user: " + principal.getName() + ")");
        if (body.getPassword1() != null && body.getPassword1().equals(body.getPassword2()))
        {
            Role role = roleRepository.findByName(body.getCurrentRole());
            Set<Role> roles;
            if (role == null) {
                return new ResponseEntity<Object>("No role selected.", HttpStatus.BAD_REQUEST);
            } else {
                roles = Collections.singleton(role);
            }
            User user = new User(body.getUsername(), body.getPassword1(), true, roles);
            
            ResponseEntity<Object> result = transferUserData(body, user);
            if (result != null) {
                return result;
            }
            return new ResponseEntity<Object>(new ProfileRepresentation(userRepository.save(user)), HttpStatus.OK);
        }
        else
        {
            return new ResponseEntity<Object>("Passwords do not match.", HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/admin/users/{id}", method = RequestMethod.PUT,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> update(Principal principal, @PathVariable Long id, @RequestBody ProfileRepresentation body) {
        LogFactory.getLog(getClass()).info("PUT /admin/users/" + id);
        User user = userRepository.findOne(id);
        if (user != null) {
            ResponseEntity<Object> result = transferUserData(body, user);
            if (result != null) {
                return result;
            }
            return new ResponseEntity<Object>(new ProfileRepresentation(userRepository.save(user)), HttpStatus.OK);
        }
        return new ResponseEntity<Object>("User not found.", HttpStatus.NOT_FOUND);
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
    public User register(@RequestBody User user) {
        LogFactory.getLog(getClass()).info("POST /register (for user: " + user.getEmail() + ")");
        return userRepository.save(user);
    }


}
