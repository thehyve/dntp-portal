package business.models;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LabRequestRepository extends JpaRepository<LabRequest, Long> {

    List<LabRequest> findAllByProcessInstanceId(String processInstanceId);
    
    List<LabRequest> findAllByProcessInstanceId(String processInstanceId, Sort sort);

    List<LabRequest> findAllByLab(Lab lab);
    
    List<LabRequest> findAllByLab(Lab lab, Sort sort);

}
