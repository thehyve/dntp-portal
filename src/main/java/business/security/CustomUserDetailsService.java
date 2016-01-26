package business.security;

import business.models.Role;
import business.models.User;
import business.models.UserRepository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    UserRepository userRepository;

    Log log = LogFactory.getLog(getClass());

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("loadUserByUsername: " + username);
        User user = userRepository.findByUsernameAndActiveTrueAndEmailValidatedTrueAndDeletedFalse(username);
        if (user != null) {
        List<GrantedAuthority> authorityList = new ArrayList<GrantedAuthority>();
        for (Role r : user.getRoles()) {
            authorityList.add(AuthorityUtils.createAuthorityList("ROLE_"+r.getName()).get(0));
        }
        return new org.springframework.security.core.userdetails.User(
        user.getUsername(), user.getPassword(), true, true,
        true, true, authorityList);
        } else {
        throw new UsernameNotFoundException(
        "Could not find the user '" + username + "'.");
        }
    }
}