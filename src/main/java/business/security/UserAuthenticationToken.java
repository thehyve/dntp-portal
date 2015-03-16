package business.security;

import java.util.Collection;

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

    @Override
    public Object getCredentials() {
        return user.getPassword();
    }

    @Override
    public Object getPrincipal() {
        return user;
    }
    
    public String getName() {
        return user.getUsername();
    }

}
