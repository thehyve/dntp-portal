package business.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;

import business.models.Role;
import business.models.User;
import business.models.UserRepository;

@Configuration
@EnableWebSecurity
@Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
public class HttpSecurityConfiguration extends WebSecurityConfigurerAdapter {

    Log log = LogFactory.getLog(getClass());
    
    private AuthenticationFailureHandler authenticationFailureHandler = new AuthenticationFailureHandler() {
        @Override
        public void onAuthenticationFailure(HttpServletRequest request,
                HttpServletResponse response, AuthenticationException e)
                throws IOException, ServletException {
            log.error("Error: " + e.getMessage());
            response.setContentType(MediaType.TEXT_PLAIN_VALUE);
            response.sendError(HttpStatus.UNAUTHORIZED.value(), e.getMessage());
        }
    };

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
        .exceptionHandling().authenticationEntryPoint(new Http403ForbiddenEntryPoint())
        .and()
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
                .antMatchers("/admin/**").access("hasRole('palga')")
        .and()
                .authorizeRequests()
                .antMatchers(
                        "/",
                        "/robots.txt",
                        "/public/labs/**",
                        "/password/request-new",
                        "/password/reset",
                        "/index.html",
                        "/bower_components/**",
                        "/app/**",
                        "/messages/**",
                        "/css/**",
                        "/*.ico",
                        "/images/**"
                ).permitAll()
                .antMatchers(HttpMethod.POST, "/register/users").permitAll()
                .antMatchers(HttpMethod.POST, "/register/users/**").permitAll()
                .antMatchers(HttpMethod.GET, "/register/users/activate/**").permitAll()
                .antMatchers(HttpMethod.GET, "/status").permitAll()
            .anyRequest()
                .authenticated()
                .and()
                .addFilterAfter(new CsrfHeaderFilter(), CsrfFilter.class)
                .csrf().csrfTokenRepository(csrfTokenRepository())
        ;
    }

    private CsrfTokenRepository csrfTokenRepository() {
        HttpSessionCsrfTokenRepository repository = new HttpSessionCsrfTokenRepository();
        repository.setHeaderName("X-XSRF-TOKEN");
        return repository;
    }

}
