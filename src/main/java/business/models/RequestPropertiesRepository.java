package business.models;

import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import business.models.RequestProperties.ReviewStatus;

public interface RequestPropertiesRepository extends JpaRepository<RequestProperties, Long> {

    RequestProperties findByProcessInstanceId(String processInstanceId);

    @Query("SELECT p.reviewStatus FROM RequestProperties p WHERE processInstanceId = :processInstanceId)")
    ReviewStatus getRequestReviewStatusByProcessInstanceId(@Param("processInstanceId") String processInstanceId);

    @Query("SELECT p.processInstanceId FROM RequestProperties p WHERE reviewStatus = :reviewStatus)")
    Set<String> getProcessInstanceIdsByReviewStatus(@Param("reviewStatus") ReviewStatus reviewStatus);

}
