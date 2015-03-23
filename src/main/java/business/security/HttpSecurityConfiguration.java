package business.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import business.models.Role;
import business.models.User;
import business.models.UserRepository;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;

@Configuration
@EnableWebSecurity
@Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
public class HttpSecurityConfiguration extends
        WebSecurityConfigurerAdapter {

    private AuthenticationFailureHandler authenticationFailureHandler = new AuthenticationFailureHandler() {
        @Override
        public void onAuthenticationFailure(HttpServletRequest arg0,
                HttpServletResponse arg1, AuthenticationException arg2)
                throws IOException, ServletException {
            StringBuilder sb = new StringBuilder();
            for (Entry<String, String[]> entry : arg0.getParameterMap()
                    .entrySet()) {
                sb.append(entry.getKey()).append(":");
                for (String s : entry.getValue()) {
                    sb.append(" '").append(s).append("'");
                }
                sb.append(", ");
            }
            LogFactory.getLog(getClass()).info(
                    "Authentication failure: " + sb.toString()
                            + arg2.getMessage());
        }
    };

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
        .userDetailsService(userDetailsService())
            .formLogin()
            .permitAll()
            .failureHandler(authenticationFailureHandler)
        .and()
            .logout()
            .permitAll()
            .logoutSuccessUrl("/#/login")
        .and()
            .authorizeRequests()
                .antMatchers(
                        "/public/labs/**",
                        "/public/institutions/**"
                ).permitAll()
        .and()
            .authorizeRequests()
                .antMatchers(
                        "/password/request-new",
                        "/password/reset"
                ).permitAll()
        .and()
                .authorizeRequests()
            .antMatchers("/admin/**").access("hasRole('palga')")
        .and()
                .authorizeRequests()
                .antMatchers(
                        "/workflow.html",
                        "/index.html",
                        "/login.html",
                        "/",
                        "/registration.html",
                        "/bower_components/**",
                        "/app/**",
                        "/messages/**",
                        "/images/**"
                ).permitAll()
            .anyRequest()
                .authenticated()
                .and()
                .addFilterAfter(new CsrfHeaderFilter(), CsrfFilter.class)
                .csrf().csrfTokenRepository(csrfTokenRepository())
        ;
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
                User user = userRepository.findByEmailAndActiveTrueAndDeletedFalse(username);
                if (user != null) {
                    List<GrantedAuthority> authorityList = new ArrayList<GrantedAuthority>();
                    for (Role r : user.getRoles()) {
                        authorityList.add(AuthorityUtils.createAuthorityList(
                                r.getName()).get(0));
                    }
                    return new org.springframework.security.core.userdetails.User(
                            user.getEmail(), user.getPassword(), true, true,
                            true, true, authorityList);
                } else {
                    throw new UsernameNotFoundException(
                            "could not find the user '" + username + "'");
                }
            }
        };
    }

    private CsrfTokenRepository csrfTokenRepository() {
        HttpSessionCsrfTokenRepository repository = new HttpSessionCsrfTokenRepository();
        repository.setHeaderName("X-XSRF-TOKEN");
        return repository;
    }

}
