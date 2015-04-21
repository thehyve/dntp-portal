package business.models;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ApprovalVoteRepository extends JpaRepository<ApprovalVote, Long> {

    ApprovalVote findOneByProcessInstanceIdAndCreator(String processInstanceId, User creator);

}
