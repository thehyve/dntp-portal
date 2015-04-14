package business.security;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.ResponseStatus;

import business.models.Role;
import business.models.User;
import business.models.UserRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends
        GlobalAuthenticationConfigurerAdapter {

    Log log = LogFactory.getLog(getClass());
    
    @Override
    public void init(AuthenticationManagerBuilder auth) throws Exception {
        LogFactory.getLog(getClass()).info("INIT AUTH!!!");
        //auth.inMemoryAuthentication().withUser("user").password("password")
        //        .roles("USER").and().withUser("admin").password("password")
        //        .roles("USER", "ADMIN");
        auth.userDetailsService(userDetailsService())
        .and()
        .authenticationProvider(authenticationProvider());
        LogFactory.getLog(getClass()).info("INIT AUTH DONE!!!");
    }
    
    static final long MAX_FAILED_LOGIN_ATTEMPTS = 10;
    static final long ACCOUNT_BLOCKING_PERIOD = 900; // 900 seconds, 15 minutes
    
    @ResponseStatus(value=HttpStatus.FORBIDDEN, reason="User account blocked.")
    public class UserAccountBlocked extends LockedException {
        private static final long serialVersionUID = 6789077965053928047L;
        public UserAccountBlocked(String message) {
            super(message);
        }
        public UserAccountBlocked() {
            super("User account blocked. Please retry in 15 minutes.");
        }
    }
    
    @Bean
    protected AuthenticationProvider authenticationProvider() {
        return new AuthenticationProvider() {
            
            @Autowired
            UserRepository userRepository;
            
            @Autowired
            AuthenticationManager authenticationManager;
            
            @Override
            public boolean supports(Class<?> authentication) {
                if (authentication == UsernamePasswordAuthenticationToken.class) {
                    return true;
                }
                return false;
            }
            
            @Override
            public Authentication authenticate(Authentication authentication)
                    throws AuthenticationException {
                log.info("AuthenticationProvider: "+
                    "username: " + authentication.getName() + ", " +
                    "password: " + authentication.getCredentials().toString()
                    );
                User user = userRepository.findByUsernameAndActiveTrueAndDeletedFalse(authentication.getName());
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
                    if (user.getPassword().equals(
                                authentication.getCredentials().toString())) {
                        log.info("AuthenticationProvider: OK");
                        if (user.getFailedLoginAttempts() > 0) {
                            user.resetFailedLoginAttempts();
                            user = userRepository.save(user);
                        }
                        List<GrantedAuthority> authorityList = new ArrayList<GrantedAuthority>();
                        for (Role r : user.getRoles()) {
                            authorityList.add(AuthorityUtils.createAuthorityList(
                                    r.getName()).get(0));
                        }
                        return new UserAuthenticationToken(user, authorityList);
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
        };
    }

    @Bean
    protected UserDetailsService userDetailsService() {
        return new UserDetailsService() {

            @Autowired
            UserRepository userRepository;

            @Override
            public UserDetails loadUserByUsername(String username)
                    throws UsernameNotFoundException {
                log.info(
                        "loadUserByUsername: " + username);
                User user = userRepository.findByUsernameAndActiveTrueAndDeletedFalse(username);
                if (user != null) {
                    List<GrantedAuthority> authorityList = new ArrayList<GrantedAuthority>();
                    for (Role r : user.getRoles()) {
                        authorityList.add(AuthorityUtils.createAuthorityList(
                                r.getName()).get(0));
                    }
                    return new org.springframework.security.core.userdetails.User(
                            user.getUsername(), user.getPassword(), true, true,
                            true, true, authorityList);
                } else {
                    throw new UsernameNotFoundException(
                            "could not find the user '" + username + "'");
                }
            }
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder;
    }
}
