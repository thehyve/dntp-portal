package business.services;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import business.controllers.UserController.EmailAddressNotAvailableException;
import business.models.User;
import business.models.UserRepository;

@Service
@Transactional
public class UserService {

    @Autowired
    UserRepository userRepository;
    
    public class EmailAddressNotUnique extends RuntimeException {
        private static final long serialVersionUID = 6789077965053928047L;
        public EmailAddressNotUnique(String message) {
            super(message);
        }
        public EmailAddressNotUnique() {
            super("Email address not available.");
        }
    }
    
    public User save(User user) throws EmailAddressNotAvailableException {
        User result = userRepository.save(user);
        long count = userRepository.countByUsernameAndDeletedFalse(user.getUsername());
        if (count <= 1) {
            return result;
        }
        throw new EmailAddressNotUnique();
    }
    
}
