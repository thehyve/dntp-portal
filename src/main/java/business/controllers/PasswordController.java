package business.controllers;

import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import business.models.NewPasswordRequest;
import business.models.NewPasswordRequestRepository;
import business.models.User;
import business.representation.EmailRepresentation;
import business.representation.NewPasswordRepresentation;
import business.representation.PasswordChangeRepresentation;
import business.security.UserAuthenticationToken;
import business.services.MailService;
import business.services.UserService;
import business.validation.PasswordValidator;

@RestController
public class PasswordController {

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    UserService userService;
    
    @Autowired
    MailService mailService;
    
    @Autowired
    private NewPasswordRequestRepository nprRepo;

    @RequestMapping(value = "/password/request-new", method = RequestMethod.PUT)
    public void requestNewPassword(@RequestBody EmailRepresentation form) {
        LogFactory.getLog(this.getClass()).info("PUT request-password/" + form.getEmail());

        // Authenticate user (maybe the email doesn't even exist!)
        User user = this.userService.findByUsername(form.getEmail());
        if (user != null) {
            // Create a NewPasswordRequest for him
            NewPasswordRequest npr = new NewPasswordRequest(user);
            this.nprRepo.save(npr);

            mailService.sendPasswordRecoveryToken(npr);
            
            LogFactory.getLog(this.getClass()).info("Recovery password token generated: " + npr.getToken());
        }

        // If the user doesn't exist we still return OK, since we don't want to let an attacker know if an email
        // exists in our database
    }

    @RequestMapping(value = "/password/reset", method = RequestMethod.POST)
    public ResponseEntity<Object> setPassword(@RequestBody NewPasswordRepresentation form) {
        LogFactory.getLog(this.getClass()).info("POST password/reset");

        // LATER: Check if the link was issued a couple of days ago

        NewPasswordRequest npr = this.nprRepo.findByToken(form.getToken());
        if (npr != null && PasswordValidator.validate(form.getPassword())) {
            // Update the password of the user and delete the npr
            User user = npr.getUser();
            user.setPassword(passwordEncoder.encode(form.getPassword()));
            this.userService.save(user);
            this.nprRepo.delete(npr);
            return new ResponseEntity<Object>(HttpStatus.OK);
        } else {
            // The token doesn't exist or the password is invalid!
            // Validation should never fail since there is client-side validation
            return new ResponseEntity<Object>(HttpStatus.BAD_REQUEST);
        }
    }


    @RequestMapping(value = "/password/change", method = RequestMethod.POST)
    public ResponseEntity<Object> changePassword(UserAuthenticationToken user, @RequestBody PasswordChangeRepresentation form) {
        LogFactory.getLog(this.getClass()).info("POST /password/change");

        // Update profile
        User currentUser = this.userService.getOne(user.getId());

        if (passwordEncoder.matches(form.getOldPassword(), currentUser.getPassword()) && PasswordValidator.validate(form.getNewPassword())) {
            currentUser.setPassword(passwordEncoder.encode(form.getNewPassword()));
            this.userService.save(currentUser);
            return new ResponseEntity<Object>(HttpStatus.OK);
        } else {
            // Old password is incorrect or new password is invalid
            // Validation should never fail since there is client-side validation
            return new ResponseEntity<Object>(HttpStatus.BAD_REQUEST);
        }
    }
}
