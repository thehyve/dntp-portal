package business.security;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ResponseStatus;

import business.models.Role;
import business.models.User;
import business.models.UserRepository;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    static final long MAX_FAILED_LOGIN_ATTEMPTS = 10;
    static final long ACCOUNT_BLOCKING_PERIOD = 3600; // 3600 seconds, 1h

    Log log = LogFactory.getLog(this.getClass());

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Override
    public boolean supports(Class<?> authentication) {
        if (authentication == UsernamePasswordAuthenticationToken.class) {
            return true;
        }
        return false;
    }

    public static List<GrantedAuthority> getAuthorityList(User user) {
        List<GrantedAuthority> authorityList = new ArrayList<GrantedAuthority>();
        for (Role r : user.getRoles()) {
            authorityList.add(AuthorityUtils.createAuthorityList(r.getName()).get(0));
        }
        return authorityList;
    }
    
    @Override
    public Authentication authenticate(Authentication authentication)
            throws AuthenticationException {
        log.info("username: " + authentication.getName());
        User user = userRepository.findByUsernameAndActiveTrueAndEmailValidatedTrueAndDeletedFalse(authentication.getName());
        if (user != null) {
            if (user.isAccountTemporarilyBlocked()) {
                Date now = new Date();
                long interval = now.getTime() - user.getAccountBlockStartTime().getTime();
                if (interval > ACCOUNT_BLOCKING_PERIOD*1000) {
                    // unblock account
                    log.info("Unblocking blocked account for user " + user.getUsername());
                    user.resetFailedLoginAttempts();
                    user.setAccountTemporarilyBlocked(false);
                    user = userRepository.save(user);
                } else {
                    // account is temporarily blocked, deny access.
                    log.info("Account still blocked for user " + user.getUsername() + ". Access denied.");
                    throw new UserAccountBlocked();
                }
            }
            if (passwordEncoder.matches(authentication.getCredentials().toString(), user.getPassword())) {
                log.info("AuthenticationProvider: OK");
                if (user.getFailedLoginAttempts() > 0) {
                    user.resetFailedLoginAttempts();
                    user = userRepository.save(user);
                }
                return new UserAuthenticationToken(user, getAuthorityList(user));
            }
            // failed login attempt
            user.incrementFailedLoginAttempts();
            log.info("Login failed for user " + user.getUsername() + ". Failed attempt number " + user.getFailedLoginAttempts() + ".");
            if (user.getFailedLoginAttempts() >= MAX_FAILED_LOGIN_ATTEMPTS) {
                // block account
                user.setAccountTemporarilyBlocked(true);
                user.setAccountBlockStartTime(new Date());
                userRepository.save(user);
                throw new UserAccountBlocked();
            }
            userRepository.save(user);
        }
        return null;
    }

    @ResponseStatus(value= HttpStatus.FORBIDDEN, reason="User account blocked.")
    public static class UserAccountBlocked extends LockedException {
        private static final long serialVersionUID = 6789077965053928047L;
        public UserAccountBlocked(String message) {
            super(message);
        }
        public UserAccountBlocked() {
            super("User account blocked. Please retry in an hour.");
        }
    }
}
