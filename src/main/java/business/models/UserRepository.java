/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business.models;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Query("select u from User u inner join u.roles ur "
            + "where u.deleted = false "
            + "and u.active = true "
            + "and ur.id = :roleId")
    List<User> findAllByDeletedFalseAndActiveTrueAndHasRole(@Param("roleId") Long roleId);

}
