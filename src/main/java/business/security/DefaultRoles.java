/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business.security;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import business.models.Role;
import business.models.RoleRepository;

@Profile("prod")
@Service
public class DefaultRoles {

    Log log = LogFactory.getLog(getClass());
    
    @Autowired
    RoleRepository roleRepository;

    static final String[] defaultRoles = new String[]{"requester", "palga", "scientific_council", "lab_user", "hub_user"};

    /**
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
    }

}
