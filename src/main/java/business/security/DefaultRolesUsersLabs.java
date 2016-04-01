/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business.security;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import business.models.ContactData;
import business.models.Lab;
import business.models.LabRepository;
import business.models.Role;
import business.models.RoleRepository;
import business.models.User;
import business.models.UserRepository;
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
    
    @Autowired
    private Environment env;

    static final String[] defaultRoles = new String[]{"requester", "palga", "scientific_council", "lab_user", "hub_user"};

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

        if (env.acceptsProfiles("prod")) {
            return;
        }

        log.info("Creating default labs...");
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
                cd.setEmail("lab_" + l.getNumber() + "@labs.dntp.thehyve.nl");
                l.setContactData(cd);
                labRepository.save(l);
            }
        }

        log.info("Creating default users...");
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
                if (role.isHubUser()) {
                    Set<Lab> hubLabs = new HashSet<>();
                    hubLabs.add(defaultLab);
                    hubLabs.add(labRepository.findByName(defaultLabs[2]));
                    user.setHubLabs(hubLabs);
                } else {
                    user.setLab(defaultLab);
                }
                userService.save(user);
                userService.save(user);
            }
        }
        // Create default lab users for each lab (if they don't exist)
        for (Lab lab: labRepository.findAll()) {
            String labNumber = lab.getNumber().toString();
            String username = "lab_user" + labNumber + "@dntp.thehyve.nl";

            if (userRepository.findByUsernameAndDeletedFalse(username) == null) {
                Role role = roleRepository.findByName("lab_user");
                User user = createUser("lab_user" + labNumber, role);
                user.setLab(lab);
                userRepository.save(user);
            }
        }

        log.info("Created default users and roles.");
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
