package business.security;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
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
import business.services.UserService;

@Profile({"default", "dev", "test"})
@Service
public class DefaultRolesUsersLabs {

    Log log = LogFactory.getLog(getClass());
    
    @Autowired
    UserService userService;

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
        for (String r: defaultLabs) {
            if (labRepository.findByName(r) == null) {
                Lab l = new Lab(new Long(labIdx++), labIdx++, r, null);
                labRepository.save(l);
            }
        }
        
        log.warn("Creating default users...");
        for (String r: defaultRoles) {
            Role role = roleRepository.findByName(r);
            String username = r + "@dntp.thehyve.nl";
            User user = userService.findByUsername(username);
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
                user.activate();
                if (r.equals("lab_user")) {
                    Lab lab = labRepository.findByName(defaultLabs[0]);
                    user.setLab(lab);
                }
                userService.save(user);
            } else if (!user.getPassword().startsWith("$")) {
                // Detect and encrypt old plain-text passwords
                user.setPassword(passwordEncoder.encode(user.getPassword()));
                userService.save(user);
            }
        }
        // Create default lab users for each lab (if they don't exist)
        for (Lab lab: labRepository.findAll()) {
            String labNumber = lab.getNumber().toString();
            String username = "lab_user" + labNumber + "@dntp.thehyve.nl";

            if (userRepository.findByUsernameAndDeletedFalse(username) == null) {
                Set<Role> roles = Collections.singleton(roleRepository.findByName("lab_user"));
                String password = passwordEncoder.encode("lab_user");
                User user = new User(username, password, true, roles);
                user.setFirstName("lab_user" + labNumber);
                ContactData contactData = new ContactData();
                contactData.setEmail(username);
                user.setContactData(contactData);
                user.setLab(lab);
                userRepository.save(user);
            }
        }
    }

}
