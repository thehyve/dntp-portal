package business.mocks;

import business.models.User;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.ArrayList;

public class MockAuthenticationToken extends AbstractAuthenticationToken {

    private User user;

    public MockAuthenticationToken(User user) {
        super(new ArrayList<GrantedAuthority>());
        this.user = user;
    }

    @Override
    public Object getCredentials() {
        return user.getPassword();
    }

    @Override
    public Object getPrincipal() {
        return user;
    }
}