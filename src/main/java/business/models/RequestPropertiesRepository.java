/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business.models;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import business.models.RequestProperties.ReviewStatus;

public interface RequestPropertiesRepository extends JpaRepository<RequestProperties, Long> {

    RequestProperties findByProcessInstanceId(String processInstanceId);

    List<RequestProperties> findByRequestNumberNull();

    @Query("SELECT p.reviewStatus FROM RequestProperties p WHERE processInstanceId = :processInstanceId")
    ReviewStatus getRequestReviewStatusByProcessInstanceId(@Param("processInstanceId") String processInstanceId);

    @Query("SELECT p.processInstanceId FROM RequestProperties p WHERE reviewStatus = :reviewStatus")
    Set<String> getProcessInstanceIdsByReviewStatus(@Param("reviewStatus") ReviewStatus reviewStatus);

    @Query("SELECT p.requestNumber FROM RequestProperties p WHERE processInstanceId = :processInstanceId")
    String getRequestNumberByProcessInstanceId(@Param("processInstanceId") String processInstanceId);

    @Query("SELECT COUNT(da.id) FROM RequestProperties p JOIN p.dataAttachments da WHERE p.processInstanceId = :processInstanceId")
    long countDataAttachmentsByProcessInstanceId(@Param("processInstanceId") String processInstanceId);

    @Query("SELECT p FROM RequestProperties r JOIN r.parent p WHERE r.processInstanceId = :processInstanceId")
    RequestProperties getParentByProcessInstanceId(@Param("processInstanceId") String processInstanceId);

    @Query("SELECT p.dateSubmitted FROM RequestProperties p WHERE processInstanceId = :processInstanceId")
    Date getDateSubmittedByProcessInstanceId(@Param("processInstanceId") String processInstanceId);

    @Query("SELECT p.lastAssignee FROM RequestProperties p WHERE processInstanceId = :processInstanceId")
    String getLastAssigneeByProcessInstanceId(@Param("processInstanceId") String processInstanceId);

}
