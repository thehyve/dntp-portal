package business.models;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LabRepository extends JpaRepository<Lab, Long> {

    Lab findByName(String lab);
    
    Lab findByNumber(Integer labNumber);
    
    List<Lab> findAllByActiveTrue();
    
    Lab findOneByActiveTrue(Long id);
}
