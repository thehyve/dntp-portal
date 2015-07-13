package business.security;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;

import business.models.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import business.services.UserService;

@Profile({"default", "dev", "test"})
@Service
public class DefaultRolesUsersLabs {

    Log log = LogFactory.getLog(getClass());
    
    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    LabRepository labRepository;

    @Autowired
    PasswordEncoder passwordEncoder;
    
    static final String[] defaultRoles = new String[]{"requester", "palga", "scientific_council", "lab_user"};
    
    /**
     * Generates default users and labs in the 'dev' and 'test' profiles.
     * Always generates default roles (if not already present).
     */
    @PostConstruct
    private void initDatabase() {
        log.info("Creating default roles...");
        for (String r: defaultRoles) {
            Role role = roleRepository.findByName(r);
            if (role == null) {
                role = roleRepository.save(new Role(r));
            }
        }
        
        log.warn("Creating default labs...");
        String[] defaultLabs = new String[] {
                "AMC, afd. Pathologie",
                "Meander Medisch Centrum, afd. Klinische Pathologie",
                "Canisius-Wilhelmina Ziekenhuis, afd. Pathologie",
                "Laboratorium voor Pathologie (PAL), Dordrecht"
        };
        int labIdx = 99;
        // Create default labs
        for (String r: defaultLabs) {
            if (labRepository.findByName(r) == null) {
                Lab l = new Lab(new Long(labIdx++), labIdx++, r, null);
                ContactData cd = new ContactData();
                cd.setEmail(l.getName() + "@labs.dntp.thehyve.nl");
                l.setContactData(cd);
                labRepository.save(l);
            }
        }

        log.warn("Creating default users...");
        Lab defaultLab = labRepository.findByName(defaultLabs[0]);
        // Create default roles and users for each role
        for (String r: defaultRoles) {
            // Save the role if it doesn't exist yet
            Role role = roleRepository.findByName(r);

            // Create a user for the role if it doesn't exist yet
            String username = r + "@dntp.thehyve.nl";
            User user = userService.findByUsername(username);
            if (user == null) {
                user = createUser(r, role);
                user.setLab(defaultLab);
                userService.save(user);
                userService.save(user);
            }
        }
        // Create default lab users for each lab (if they don't exist)
        for (Lab lab: labRepository.findAll()) {
            String labNumber = lab.getNumber().toString();
            String username = "lab_user" + labNumber + "@dntp.thehyve.nl";

            if (userRepository.findByUsernameAndDeletedFalse(username) == null) {
                Set<Role> roles = Collections.singleton(roleRepository.findByName("lab_user"));
                User user = createUser("lab_user" + labNumber, roles.stream().findFirst().get());
                user.setLab(lab);
                userRepository.save(user);
            }
        }

        LogFactory.getLog(getClass()).info("Created default users and roles");
    }

    private User createUser(String username, Role role) {
        Set<Role> roles = new HashSet<Role>();
        roles.add(role);
        String password = passwordEncoder.encode(role.getName());
        User user = new User(username + "@dntp.thehyve.nl", password, true, roles);
        user.setFirstName(username);
        ContactData contactData = new ContactData();
        contactData.setEmail(user.getUsername());
        user.setContactData(contactData);
        user.setEmailValidated(true);
        user.activate();
        return user;
    }

}
