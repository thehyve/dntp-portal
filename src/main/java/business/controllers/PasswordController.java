package business.controllers;

import business.models.NewPasswordRequest;
import business.models.NewPasswordRequestRepository;
import business.models.User;
import business.models.UserRepository;
import business.representation.EmailRepresentation;
import business.representation.NewPasswordRepresentation;
import business.representation.PasswordChangeRepresentation;
import business.security.UserAuthenticationToken;
import business.validation.PasswordValidator;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@RestController
public class PasswordController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NewPasswordRequestRepository nprRepo;

    @Autowired
    JavaMailSender mailSender;

    @Value("${dntp.server-name}")
    String serverName;

    @Value("${dntp.server-port}")
    String serverPort;

    @RequestMapping(value = "/password/request-new", method = RequestMethod.PUT)
    public void requestNewPassword(@RequestBody EmailRepresentation form) {
        LogFactory.getLog(this.getClass()).info("PUT request-password/" + form.getEmail());

        // Authenticate user (maybe the email doesn't even exist!)
        User user = this.userRepository.findByUsername(form.getEmail());
        if (user != null) {
            // Create a NewPasswordRequest for him
            NewPasswordRequest npr = new NewPasswordRequest(user);
            this.nprRepo.save(npr);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getContactData().getEmail());
            message.setFrom("no-reply@dntp.thehyve.nl");
            message.setReplyTo("no-reply@dntp.thehyve.nl");
            message.setSubject("Password recovery");
            message.setText(String.format("Please follow this link to reset your password: http://%s:%s/#/login/reset-password/%s", serverName, serverPort, npr.getToken()));
            mailSender.send(message);
            LogFactory.getLog(this.getClass()).info("Recovery password token generated: " + npr.getToken());
        }

        // If the user doesn't exist we still return OK, since we don't want to let an attacker know if an email
        // exists in our database
    }

    @RequestMapping(value = "/password/reset", method = RequestMethod.POST)
    public ResponseEntity<Object> setPassword(@RequestBody NewPasswordRepresentation form) {
        LogFactory.getLog(this.getClass()).info("POST password/reset");

        // LATER: Check if the link was issued a couple of days ago
        // LATER: Check that the new password meets the criteria

        NewPasswordRequest npr = this.nprRepo.findByToken(form.getToken());
        if (npr != null && PasswordValidator.validate(form.getPassword())) {
            // Update the password of the user and delete the npr
            User user = npr.getUser();
            user.setPassword(form.getPassword());
            this.userRepository.save(user);
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

        // LATER: Validate data (password requirements)

        // Update profile
        User currentUser = this.userRepository.getOne(user.getId());

        if (currentUser.getPassword().equals(form.getOldPassword()) && PasswordValidator.validate(form.getNewPassword())) {
            currentUser.setPassword(form.getNewPassword());
            this.userRepository.save(currentUser);
            return new ResponseEntity<Object>(HttpStatus.OK);
        } else {
            // Old password is incorrect or new password is invalid
            // Validation should never fail since there is client-side validation
            return new ResponseEntity<Object>(HttpStatus.BAD_REQUEST);
        }
    }
}
