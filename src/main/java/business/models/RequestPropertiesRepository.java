package business.models;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RequestPropertiesRepository extends JpaRepository<RequestProperties, Long> {

    RequestProperties findByProcessInstanceId(String processInstanceId);
    
}
