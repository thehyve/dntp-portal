/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business.models;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ApprovalVoteRepository extends JpaRepository<ApprovalVote, Long> {

    ApprovalVote findOneByProcessInstanceIdAndCreator(String processInstanceId, User creator);

    Long countByProcessInstanceId(String processInstanceId);
}
