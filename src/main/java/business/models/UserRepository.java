package business.models;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    List<User> findByActiveTrueAndDeletedFalse();
    
    List<User> findByActiveFalseAndDeletedFalse();
    
    List<User> findByDeletedFalse();
    
    List<User> findAllByUsernameAndDeletedFalse(String username);
    
    Long countByUsernameAndDeletedFalse(String username);
    
    User findByUsernameAndDeletedFalse(String username);
    
    User findByUsernameAndActiveTrueAndEmailValidatedTrueAndDeletedFalse(String username);

    User findByUsername(String username);
    
    User findByIdAndDeletedFalse(Long id);
    
    User getByIdAndDeletedFalse(Long id);
    
}
