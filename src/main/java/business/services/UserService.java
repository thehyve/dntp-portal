package business.services;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import business.exceptions.EmailAddressNotAvailable;
import business.exceptions.EmailAddressNotUnique;
import business.models.User;
import business.models.UserRepository;

@Service
@Transactional
public class UserService {

    @Autowired
    UserRepository userRepository;
    
    public User save(User user) throws EmailAddressNotAvailable {
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
    
}
