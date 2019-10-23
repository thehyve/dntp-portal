/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business.security;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import business.models.ContactData;
import business.models.Lab;
import business.models.Role;
import business.models.RoleRepository;
import business.models.User;
import business.models.UserRepository;
import business.services.LabService;
import business.services.UserService;

@Profile({"dev", "test"})
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
    LabService labService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Value("${dntp.test-account}")
    String testAccount;

    @Value("${dntp.test-domain}")
    String testDomain;

    @Autowired
    private Environment env;

    static final String[] defaultRoles = new String[]{"requester", "palga", "scientific_council", "lab_user", "hub_user"};

    static final String[] specialRequesters = new String[]{"pathologist", "contactperson"};

    private String getEmailAddress(String accountName) {
        return testAccount + '+' + accountName + '@' + testDomain;
    }

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
        // First, generate default testing labs 100, 102, 104 and 106.
        String[] defaultLabs = new String[] {
                "AMC, afd. Pathologie",
                "Meander Medisch Centrum, afd. Klinische Pathologie",
                "Canisius-Wilhelmina Ziekenhuis, afd. Pathologie",
                "Laboratorium voor Pathologie (PAL), Dordrecht"
        };
        Map<Integer, String> cities = new HashMap<>();
        cities.put(100, "Amsterdam");
        cities.put(102, "Amersfoort");
        cities.put(104, "Nijmegen");
        cities.put(106, "Dordrecht");
        int labIdx = 99;
        // Create default labs
        for (String r: defaultLabs) {
            Lab l = labService.findByName(r);
            if (l == null) {
                l = new Lab(new Long(labIdx++), labIdx++, r, null);
                ContactData cd = new ContactData();
                cd.setCity(cities.get(l.getNumber()));
                l.setContactData(cd);
                l = labService.save(l);
            }
            List<String> emailAddresses = new ArrayList<>(Arrays.asList(new String[]{
                getEmailAddress("lab_" + l.getNumber()),
                getEmailAddress("lab_" + l.getNumber() + "_test")
            }));
            if (!emailAddresses.equals(l.getEmailAddresses())) {
                log.debug("Updating email addresses for lab " + l.getNumber() + ".");
                l.setEmailAddresses(emailAddresses);
                l = labService.save(l);
            }
        }
        // Second, generate labs in the range 1-99 for testing with large excerpt lists.
        for(int i = 1; i < 100; i++) {
            Lab l = labService.findByNumber(i);
            if (l == null) {
                l = new Lab();
                l.setName("Lab " + i);
                l.setNumber(i);
                ContactData cd = new ContactData();
                cd.setCity("Utrecht");
                l.setContactData(cd);
                l = labService.save(l);
            }
            List<String> emailAddresses = new ArrayList<>(Arrays.asList(new String[]{
                getEmailAddress("lab_" + l.getNumber()),
            }));
            if (!emailAddresses.equals(l.getEmailAddresses())) {
                log.debug("Updating email addresses for lab " + l.getNumber() + ".");
                l.setEmailAddresses(emailAddresses);
                l = labService.save(l);
            }
        }

        log.info("Creating default users...");
        Lab defaultLab = labService.findByName(defaultLabs[0]);
        // Create default roles and users for each role
        for (String r: defaultRoles) {
            Role role = roleRepository.findByName(r);
            // Create a user for the role if it doesn't exist yet
            String username = getEmailAddress(r);
            User user = userService.findByUsername(username);
            if (user == null) {
                user = createUser(r, role);
                if (role.isHubUser()) {
                    Set<Lab> hubLabs = new HashSet<>();
                    hubLabs.add(defaultLab);
                    hubLabs.add(labService.findByName(defaultLabs[2]));
                    user.setHubLabs(hubLabs);
                } else {
                    user.setLab(defaultLab);
                }
                user = userService.save(user);
            }
        }
        // Create special requester users
        for (String name: specialRequesters) {
            // Save the role if it doesn't exist yet
            Role role = roleRepository.findByName("requester");
            // Create a user if it doesn't exist yet
            String username = getEmailAddress(name);
            User user = userService.findByUsername(username);
            if (user == null) {
                user = createUser(name, role);
                user.setLab(defaultLab);
                user = userService.save(user);
            }
        }
        // Create special palga2 user
        String palga2Username = "palga2";
        User palga2User = userService.findByUsername(getEmailAddress(palga2Username));
        if (palga2User == null) {
            palga2User = createUser(palga2Username, roleRepository.findByName("palga"));
            palga2User.setLab(defaultLab);
            palga2User = userService.save(palga2User);
        }

        // Create default lab users for each lab (if they don't exist)
        for (Lab lab: labService.findAll()) {
            String labNumber = lab.getNumber().toString();
            String username = getEmailAddress("lab_user" + labNumber);

            if (userRepository.findByUsernameAndDeletedFalse(username) == null) {
                Role role = roleRepository.findByName("lab_user");
                User user = createUser("lab_user" + labNumber, role);
                user.setLab(lab);
                user = userRepository.save(user);
            }
        }

        log.info("Created default users and roles.");
    }

    private User createUser(String username, Role role) {
        Set<Role> roles = new HashSet<Role>();
        roles.add(role);
        String password = passwordEncoder.encode(role.getName());
        User user = new User(getEmailAddress(username), password, true, roles);
        user.setFirstName(username);
        ContactData contactData = new ContactData();
        contactData.setEmail(user.getUsername());
        user.setContactData(contactData);
        user.setEmailValidated(true);
        user.activate();
        return user;
    }

}
