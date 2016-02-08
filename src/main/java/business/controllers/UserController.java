package business.controllers;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import business.exceptions.EmailAddressNotAvailable;
import business.exceptions.EmailAddressNotUnique;
import business.exceptions.UserNotFound;
import business.models.ActivationLink;
import business.models.ActivationLinkRepository;
import business.models.User;
import business.representation.ProfileRepresentation;
import business.security.SecureTokenGenerator;
import business.security.UserAuthenticationToken;
import business.services.UserService;
import business.services.UserService.NewUserLinkType;

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
    ActivationLinkRepository activationLinkRepository;

    @RequestMapping("/user")
    public ProfileRepresentation user(UserAuthenticationToken user) {
        log.info("GET /user");
        return new ProfileRepresentation(user.getUser());
    }

    @RequestMapping(value = "/register/users/activate/{token}", method = RequestMethod.GET)
    public ResponseEntity<Object> activateUser(@PathVariable String token) {
        ActivationLink link = activationLinkRepository.findByToken(token);

        if (link == null) {
            log.warn("Activation link not found.");
            return new ResponseEntity<Object>(HttpStatus.BAD_REQUEST);
        }
        // Check that the link has been issued in the previous week
        log.info("Activation link: expiry hours = " + activationLinkExpiryHours);
        long linkAge = TimeUnit.MILLISECONDS.toHours(new Date().getTime() - link.getCreationDate().getTime()); // hours
        log.info("Activation link age in hours: " + linkAge);
        if (linkAge <= activationLinkExpiryHours) {
            User user = link.getUser();
            user.setEmailValidated(true);
            userService.save(user);
            activationLinkRepository.delete(link);
            log.info("User validated.");
            return new ResponseEntity<Object>(HttpStatus.OK);
        } else {
            // The activation link doesn't exist or is outdated!
            log.warn("Activation link expired.");
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

    @RequestMapping(value = "/admin/users", method = RequestMethod.POST)
    public ProfileRepresentation create(UserAuthenticationToken user, @RequestBody ProfileRepresentation body) {
        log.info("POST /admin/users (for user: " + user.getName() + ")");
        
        /*
         * Set strong random password when a user is created by an administrator.
         * The user can set a password using the 'Forgot password' button on the
         * login page. 
         */
        String password = SecureTokenGenerator.generatePassword();
        body.setPassword1(password);
        body.setPassword2(password);
        return userService.createNewUser(body, NewUserLinkType.PASSWORD_RESET_LINK);
    }

    @RequestMapping(value = "/admin/users/{id}", method = RequestMethod.PUT,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ProfileRepresentation update(Principal principal, @PathVariable Long id, @RequestBody ProfileRepresentation body) {
        log.info("PUT /admin/users/" + id);
        User user = userService.findOne(id);
        if (user != null) {
            userService.transferUserData(body, user);
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
        return userService.createNewUser(body, NewUserLinkType.ACTIVATION_LINK);
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
