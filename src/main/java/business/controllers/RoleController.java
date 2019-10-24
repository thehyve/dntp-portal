/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business.controllers;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import business.models.Role;
import business.models.RoleRepository;
import business.representation.RoleRepresentation;

@RestController
public class RoleController {

    @Autowired
    RoleRepository roleRepository;

    @RequestMapping(value = "/api/admin/roles", method = RequestMethod.GET)
    public List<RoleRepresentation> getAll(Principal principal) {
        LogFactory.getLog(getClass()).info("GET /api/admin/roles (for user: " + principal.getName() + ")");
        List<RoleRepresentation> roles = new ArrayList<RoleRepresentation>();
        for (Role role: roleRepository.findAll()) {
            roles.add(new RoleRepresentation(role));
        }
        return roles;
    }

}
