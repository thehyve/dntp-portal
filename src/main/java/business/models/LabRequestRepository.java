/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business.models;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LabRequestRepository extends JpaRepository<LabRequest, Long> {

    List<LabRequest> findAllByProcessInstanceId(String processInstanceId);

    List<LabRequest> findAllByProcessInstanceId(String processInstanceId, Sort sort);

    List<LabRequest> findAllByLab(Lab lab);

    List<LabRequest> findAllByLab(Lab lab, Sort sort);

    List<LabRequest> findAllByLabIn(Collection<Lab> labs);

    List<LabRequest> findAllByLabIn(Collection<Lab> labs, Sort sort);

    /* Since sent_return_email was added later, not all labrequests have it set. Using COALESE means all items that are
     * null will get evaluated as being FALSE */
    @Query(value  = "SELECT * FROM lab_request lr WHERE lr.return_date < to_timestamp(:now, 'YYYY-MM-DD HH24:MI:SS.US') "+
                    "AND COALESCE(lr.sent_return_email, FALSE) = FALSE;", nativeQuery = true)
    List<LabRequest> findAllUnsentByReturnDate(@Param("now") Date now);

    Long countByProcessInstanceIdAndLab(String processInstanceId, Lab lab);

    Long countByProcessInstanceIdAndLabIn(String processInstanceId, Collection<Lab> labs);

    @Query("SELECT COUNT(lr) FROM LabRequest lr,"
            + " RequestProperties c"
            + " JOIN c.parent AS p"
            + " WHERE p.processInstanceId = :processInstanceId"
            + " AND lr.processInstanceId = c.processInstanceId "
            + " AND lr.lab IN :labs")
    Long countByChildProcessInstanceIdAndLabIn(
            @Param("processInstanceId") String processInstanceId,
            @Param("labs")Collection<Lab> labs);

    Long countByProcessInstanceIdAndHubAssistanceRequestedTrue(String processInstanceId);

}
