package business.security;

import java.util.Collections;
import java.util.Set;

import javax.annotation.PostConstruct;

import business.models.*;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class DefaultUsers {

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    LabRepository labRepository;

    @Autowired
    PasswordEncoder passwordEncoder;
    
    @PostConstruct
    private void initDatabase() {
        LogFactory.getLog(getClass()).info("Create default users and roles.");

        String[] defaultRoles = new String[]{"requester", "palga", "scientific_council", "lab_user"};

        for (String r: defaultRoles) {
            Role role = roleRepository.findByName(r);
            if (role == null) {
                role = roleRepository.save(new Role(r));
            }
            if (userRepository.findByEmailAndDeletedFalse(r) == null) {
                Set<Role> roles = Collections.singleton(role);
                String password = r; //passwordEncoder.encode("admin");
                userRepository.save(new User(r + "@dntp.nl", password, true, roles));
            }
        }

        LogFactory.getLog(getClass()).info("Create default labs");

        String[] defaultLabs = new String[] {
                "AMC, afd. Pathologie",
                "Meander Medisch Centrum, afd. Klinische Pathologie",
                "Canisius-Wilhelmina Ziekenhuis, afd. Pathologie",
                "Laboratorium voor Pathologie (PAL), Dordrecht"
        };

        int labIdx = 99;

        for (String r: defaultLabs) {
            if (labRepository.findByName(r) == null) {
                Lab l = new Lab(new Long(labIdx++), labIdx++, r, null);
                labRepository.save(l);
            }
        }

    }

}
