package business.models;

import business.models.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    List<User> findByActiveTrueAndDeletedFalse();
    
    List<User> findByActiveFalseAndDeletedFalse();
    
    List<User> findByDeletedFalse();
    
    User findByEmailAndDeletedFalse(String email);
    
    User findByEmailAndActiveTrueAndDeletedFalse(String email);

    User findByEmail(String email);

}
