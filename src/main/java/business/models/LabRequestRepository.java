/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business.models;

import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LabRequestRepository extends JpaRepository<LabRequest, Long> {

    List<LabRequest> findAllByProcessInstanceId(String processInstanceId);

    List<LabRequest> findAllByProcessInstanceId(String processInstanceId, Sort sort);

    List<LabRequest> findAllByLab(Lab lab);

    List<LabRequest> findAllByLab(Lab lab, Sort sort);

    List<LabRequest> findAllByLabIn(Collection<Lab> labs);

    List<LabRequest> findAllByLabIn(Collection<Lab> labs, Sort sort);

    Long countByProcessInstanceIdAndLab(String processInstanceId, Lab lab);

    Long countByProcessInstanceIdAndLabIn(String processInstanceId, Collection<Lab> labs);

    Long countByProcessInstanceIdAndHubAssistanceRequestedTrue(String processInstanceId);

}
