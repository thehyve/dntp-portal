package business.security;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import business.models.User;
import business.models.UserRepository;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @Autowired
    UserRepository userRepository;
    
    @RequestMapping("/user")
    public Principal user(Principal user) {
      return user;
    }

    @RequestMapping(value = "/admin/user", method = RequestMethod.GET)
    public User get(@RequestParam String username) {
        return userRepository.findByEmailAndDeletedFalse(username);
    }

    @RequestMapping(value = "/admin/users", method = RequestMethod.GET)
    public List<User> getAll(Principal principal) {
        LogFactory.getLog(getClass()).info("GET /admin/users (for user: " + principal.getName() + ")");
        List<User> users = new ArrayList<User>();
        for(User user: userRepository.findByActiveTrueAndDeletedFalse()) {
            user.clearPassword();
            users.add(user);
        }
        return users;
    }
   
    @RequestMapping(value = "/admin/users", method = RequestMethod.POST)
    public User create(Principal principal, @RequestBody User user) {
        LogFactory.getLog(getClass()).info("POST /admin/users (for user: " + principal.getName() + ")");
        return userRepository.save(user);
    }

    @RequestMapping(value = "/admin/users/{id}/activate", method = RequestMethod.PUT)
    public User activate(Principal principal, @PathVariable String id) {
        LogFactory.getLog(getClass()).info("PUT /admin/users/" + id + "/activate");
        Long userId = Long.valueOf(id);
        User user = userRepository.getOne(userId);
        user.activate();
        return userRepository.save(user);
    }

    @RequestMapping(value = "/admin/users/{id}/deactivate", method = RequestMethod.PUT)
    public User deactivate(Principal principal, @PathVariable String id) {
        LogFactory.getLog(getClass()).info("PUT /admin/users/" + id + "/deactivate");
        Long userId = Long.valueOf(id);
        User user = userRepository.getOne(userId);
        user.deactivate();
        return userRepository.save(user);
    }
    
    @RequestMapping(value = "/admin/users/{id}/delete", method = RequestMethod.PUT)
    public void delete(Principal principal, @PathVariable String id) {
        LogFactory.getLog(getClass()).info("PUT /admin/users/" + id + "/delete");
        Long userId = Long.valueOf(id);
        User user = userRepository.getOne(userId);
        user.markDeleted();
        userRepository.save(user);
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public User register(@RequestBody User user) {
        LogFactory.getLog(getClass()).info("POST /register (for user: " + user.getEmail() + ")");
        return userRepository.save(user);
    }


}
