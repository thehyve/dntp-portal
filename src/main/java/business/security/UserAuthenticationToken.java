/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business.security;

import java.util.Collection;

import business.models.User;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

@SuppressWarnings("serial")
public class UserAuthenticationToken extends AbstractAuthenticationToken {

    private User user;
    
    public UserAuthenticationToken(
            Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
    }
    
    public UserAuthenticationToken(
            User user,
            Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.user = user;
        this.setAuthenticated(true);
    }
    
    public User getUser() {
        return user;
    }

    @Override
    public Object getCredentials() {
        return user.getPassword();
    }

    @Override
    public Object getPrincipal() {
        return user;
    }
    
    public Long getId() {
        return user.getId();
    }

}
