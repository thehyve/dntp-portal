package business.services;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import business.exceptions.PasswordChangeFailed;
import business.models.NewPasswordRequest;
import business.models.NewPasswordRequestRepository;
import business.models.User;
import business.representation.NewPasswordRepresentation;
import business.representation.PasswordChangeRepresentation;
import business.validation.PasswordValidator;

@Service
public class PasswordService {

    Log log = LogFactory.getLog(getClass());

    @Autowired
    UserService userService;

    @Autowired
    MailService mailService;

    @Autowired
    NewPasswordRequestRepository newPasswordRequestRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Value("${dntp.password-link.expiry-hours}")
    @NotNull
    Integer passwordLinkExpiryHours;

    @Transactional
    public void requestNewPassword(String email) {
        // Authenticate user (maybe the email doesn't even exist!)
        User user = this.userService.findByUsername(email);
        if (user == null) {
            log.warn("New password requested for unknown username: " + email);
            // If the user doesn't exist we still return OK, since we don't want to let an attacker know if an email
            // exists in our database
        } else {
            // Create a NewPasswordRequest for the user
            NewPasswordRequest npr = new NewPasswordRequest(user);
            newPasswordRequestRepository.save(npr);
            mailService.sendPasswordRecoveryToken(npr);
        }
    }

    @Transactional
    public void resetPassword(@NotNull NewPasswordRepresentation body) {
        NewPasswordRequest npr = newPasswordRequestRepository.findByToken(body.getToken());
        if (npr == null) {
            // The token doesn't exist.
            log.warn("Token not found.");
            throw new PasswordChangeFailed();
        }
        // Check if the link is not older than <var>passwordLinkExpiryHours</var> hours.
        log.info("password link: expiry hours = " + passwordLinkExpiryHours);
        long linkAge = TimeUnit.MILLISECONDS.toHours(new Date().getTime() - npr.getCreationDate().getTime()); // hours
        log.info("password link age in hours: " + linkAge);

        if (linkAge > passwordLinkExpiryHours) {
            // The token is expired.
            log.warn("Token expired.");
            throw new PasswordChangeFailed();
        }
        if (!PasswordValidator.validate(body.getPassword())) {
            // The password is invalid!
            // Validation should never fail since there is client-side validation.
            log.warn("Invalid password.");
            throw new PasswordChangeFailed();
        }
        // Update the password of the user and delete the npr
        User user = npr.getUser();
        user.setPassword(passwordEncoder.encode(body.getPassword()));
        if (!user.isEmailValidated()) {
            user.setEmailValidated(true);
        }
        this.userService.save(user);
        newPasswordRequestRepository.delete(npr);
    }

    @Transactional
    public void updatePassword(User user, PasswordChangeRepresentation body) {
        // Update profile
        User currentUser = this.userService.getOne(user.getId());

        if (passwordEncoder.matches(body.getOldPassword(), currentUser.getPassword()) && PasswordValidator.validate(body.getNewPassword())) {
            currentUser.setPassword(passwordEncoder.encode(body.getNewPassword()));
            this.userService.save(currentUser);
        } else {
            // Old password is incorrect or new password is invalid.
            // Validation should never fail since there is client-side validation.
            log.warn("Password change failed.");
            throw new PasswordChangeFailed();
        }
    }

    public PasswordEncoder getEncoder() {
        return passwordEncoder;
    }
}
