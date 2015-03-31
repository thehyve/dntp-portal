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
            String username = r + "@dntp.thehyve.nl";
            if (userRepository.findByUsernameAndDeletedFalse(username) == null) {
                Set<Role> roles = Collections.singleton(role);
                String password = r; //passwordEncoder.encode("admin");
                User user = new User(username, password, true, roles);
                user.setFirstName(r);
                ContactData contactData = new ContactData();
                contactData.setEmail(username);
                user.setContactData(contactData);
                userRepository.save(user);
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

        LogFactory.getLog(getClass()).info("Create default institutes");

        String[] defaultInst = new String[] {
                "UMC",
                "AMC"
        };

    }

}
