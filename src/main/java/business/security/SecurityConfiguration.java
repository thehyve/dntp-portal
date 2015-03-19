package business.security;

import java.util.ArrayList;
import java.util.List;

import business.models.Role;
import business.models.User;
import business.models.UserRepository;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
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

@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends
        GlobalAuthenticationConfigurerAdapter {

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
                LogFactory.getLog(getClass()).info("AuthenticationProvider: "+
                    "username: " + authentication.getName() + ", " +
                    "password: " + authentication.getCredentials().toString()
                    );
                User user = userRepository.findByUsername(authentication.getName());
                if (user != null 
                        && user.getPassword().equals(
                                authentication.getCredentials().toString())) {
                    LogFactory.getLog(getClass()).info("AuthenticationProvider: OK");
                    List<GrantedAuthority> authorityList = new ArrayList<GrantedAuthority>();
                    for (Role r : user.getRoles()) {
                        authorityList.add(AuthorityUtils.createAuthorityList(
                                r.getName()).get(0));
                    }
                    return new UserAuthenticationToken(user, authorityList);
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
                LogFactory.getLog(getClass()).info(
                        "loadUserByUsername: " + username);
                User user = userRepository.findByUsername(username);
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
