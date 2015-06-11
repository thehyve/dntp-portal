package business.models;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ExcerptListRepository extends JpaRepository<ExcerptList, Long> {

    ExcerptList findByProcessInstanceId(String processInstanceId);

    void deleteByProcessInstanceId(String processInstanceId);
        
}
