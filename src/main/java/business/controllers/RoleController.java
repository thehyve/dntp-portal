package business.controllers;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import business.models.Role;
import business.models.RoleRepository;
import business.models.User;
import business.models.UserRepository;
import business.representation.RoleRepresentation;

@RestController
public class RoleController {

    @Autowired
    UserRepository userRepository;
    
    @Autowired
    RoleRepository roleRepository;
    
    @RequestMapping(value = "/admin/roles", method = RequestMethod.GET)
    public List<RoleRepresentation> getAll(Principal principal) {
        LogFactory.getLog(getClass()).info("GET /admin/roles (for user: " + principal.getName() + ")");
        List<RoleRepresentation> roles = new ArrayList<RoleRepresentation>();
        for (Role role: roleRepository.findAll()) {
            roles.add(new RoleRepresentation(role));
        }
        return roles;
    }

    @RequestMapping(value = "/admin/roles", method = RequestMethod.POST)
    public RoleRepresentation create(Principal principal, @RequestBody RoleRepresentation role) {
        LogFactory.getLog(getClass()).info("POST /admin/roles");
        Role result = new Role(role.getName());
        return new RoleRepresentation(roleRepository.save(result));
    }
    
/*    @RequestMapping(value = "/admin/roles/{userid}", method = RequestMethod.GET)
    public List<Role> getUserRoles(Principal principal) {
        LogFactory.getLog(getClass()).info("GET /admin/roles (for user: " + principal.getName() + ")");
        List<Role> roles = roleRepository.findAll();
        return roles;
    }*/
    
    @RequestMapping(value = "/admin/roles/{userid}/{roleid}", method = RequestMethod.PUT)
    public void set(Principal principal, @PathVariable String userid, @PathVariable String roleid) {
        LogFactory.getLog(getClass()).info("PUT /admin/roles/" + userid + "/" + roleid);
        User user = userRepository.getOne(Long.valueOf(userid));
        Role role = roleRepository.getOne(Long.valueOf(roleid));
        user.getRoles().add(role);
        userRepository.save(user);
    }

    @RequestMapping(value = "/admin/roles/{userid}/{roleid}", method = RequestMethod.DELETE)
    public ResponseEntity<Object> remove(Principal principal, @PathVariable String userid, @PathVariable String roleid) {
        LogFactory.getLog(getClass()).info("DELETE /admin/roles/" + userid + "/" + roleid);
        User user = userRepository.getOne(Long.valueOf(userid));
        Role role = roleRepository.getOne(Long.valueOf(roleid));
        if (user.getUsername().equals("admin@dntp.nl") && role.getName().equals("admin"))
        {
            //throw new RuntimeException("Invalid: cannot remove admin rights from user admin.");
            return new ResponseEntity<Object>(HttpStatus.FORBIDDEN);
        } else {
            user.getRoles().remove(role);
            userRepository.save(user);
            return new ResponseEntity<Object>(HttpStatus.OK);
        }
    }
    
}
