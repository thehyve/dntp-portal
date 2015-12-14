package business.services;

import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import business.exceptions.EmailAddressNotAvailable;
import business.exceptions.EmailAddressNotUnique;
import business.models.Role;
import business.models.RoleRepository;
import business.models.User;
import business.models.UserRepository;

@Service
@Transactional
public class UserService {

    @Autowired
    UserRepository userRepository;
    
    @Autowired
    RoleRepository roleRepository;
    
    public User save(User user) throws EmailAddressNotAvailable {
        assert(user.getRoles().size() == 1);
        User result = userRepository.save(user);
        long count = userRepository.countByUsernameAndDeletedFalse(user.getUsername());
        if (count <= 1) {
            return result;
        }
        throw new EmailAddressNotUnique();
    }
    
    public User findByUsername(String username) {
        return userRepository.findByUsernameAndDeletedFalse(username);
    }
    
    public User getOne(Long id) {
        return userRepository.getByIdAndDeletedFalse(id);
    }

    public User findOne(Long id) {
        return userRepository.findByIdAndDeletedFalse(id);
    }
    
    public List<User> findAll() {
        return userRepository.findByDeletedFalse();
    }

    public List<User> findScientificCouncilMembers() {
        Role role = roleRepository.findByName("scientific_council");
        List<User> members = userRepository.findAllByDeletedFalseAndActiveTrueAndHasRole(role.getId());
        return members;
    }

}
