/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.security.web.header.writers.StaticHeadersWriter;

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
                        "/js/**",
                        "/messages/**",
                        "/css/**",
                        "/*.ico",
                        "/images/**"
                ).permitAll()
                .antMatchers(HttpMethod.POST, "/register/users").permitAll()
                .antMatchers(HttpMethod.POST, "/register/users/**").permitAll()
                .antMatchers(HttpMethod.GET, "/register/users/activate/**").permitAll()
                .antMatchers(HttpMethod.GET, "/status").permitAll()
                .antMatchers(HttpMethod.GET, "/ping").permitAll()
            .anyRequest()
                .authenticated()
                .and()
                .addFilterAfter(new CsrfHeaderFilter(), CsrfFilter.class)
                .csrf().csrfTokenRepository(csrfTokenRepository())
        .and()
            .headers()
            .addHeaderWriter(new StaticHeadersWriter("Content-Security-Policy-Report-Only", "default-src 'self' 'unsafe-inline'; img-src 'self' data:; font-src 'self' data:"))
        ;
    }

    private CsrfTokenRepository csrfTokenRepository() {
        HttpSessionCsrfTokenRepository repository = new HttpSessionCsrfTokenRepository();
        repository.setHeaderName("X-XSRF-TOKEN");
        return repository;
    }

}
