package business.security;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import business.models.ContactData;
import business.models.Lab;
import business.models.LabRepository;
import business.models.Role;
import business.models.RoleRepository;
import business.models.User;
import business.models.UserRepository;

@Profile("dev")
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

        LogFactory.getLog(getClass()).info("Creating default labs...");

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
        
        
        LogFactory.getLog(getClass()).info("Creating default users and roles...");

        String[] defaultRoles = new String[]{"requester", "palga", "scientific_council", "lab_user"};

        for (String r: defaultRoles) {
            Role role = roleRepository.findByName(r);
            if (role == null) {
                role = roleRepository.save(new Role(r));
            }
            String username = r + "@dntp.thehyve.nl";
            User user = userRepository.findByUsernameAndDeletedFalse(username);
            if (user == null) {
                Set<Role> roles = new HashSet<Role>();
                roles.add(role);
                String password = passwordEncoder.encode(r);
                user = new User(username, password, true, roles);
                user.setFirstName(r);
                ContactData contactData = new ContactData();
                contactData.setEmail(username);
                user.setContactData(contactData);
                user.setEmailValidated(true);
                if (r.equals("lab_user")) {
                    Lab lab = labRepository.findByName(defaultLabs[0]);
                    user.setLab(lab);
                }
                userRepository.save(user);
            } else if (!user.getPassword().startsWith("$")) {
                // Detect and encrypt old plain-text passwords
                user.setPassword(passwordEncoder.encode(user.getPassword()));
                userRepository.save(user);
            }
        }

    }

}
