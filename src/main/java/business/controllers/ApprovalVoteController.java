package business.controllers;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import business.models.ApprovalVote;
import business.models.ApprovalVote.Value;
import business.models.ApprovalVoteRepository;
import business.models.RequestProperties;
import business.models.User;
import business.representation.ApprovalVoteRepresentation;
import business.security.UserAuthenticationToken;
import business.services.RequestPropertiesService;

@RestController
public class ApprovalVoteController {

    Log log = LogFactory.getLog(getClass());

    @Autowired
    private RequestPropertiesService requestPropertiesService;

    @Autowired
    private ApprovalVoteRepository approvalVoteRepository;

    @RequestMapping(value = "/requests/{id}/approvalVotes", method = RequestMethod.GET)
    public Map<Long, ApprovalVoteRepresentation> getVotes(
            UserAuthenticationToken user,
            @PathVariable String id) {
        log.info("GET /requests/" + id + "/approvalVotes");
        RequestProperties properties = requestPropertiesService.findByProcessInstanceId(id);
        Map<Long, ApprovalVoteRepresentation> votes = new HashMap<Long, ApprovalVoteRepresentation>();
        for (Entry<Long, ApprovalVote> entry: properties.getApprovalVotes().entrySet()) {
            votes.put(entry.getKey(), new ApprovalVoteRepresentation(entry.getValue()));
        }
        return votes;
    }

    @RequestMapping(value = "/requests/{id}/approvalVotes", method = RequestMethod.POST)
    public ApprovalVoteRepresentation addVote(
            UserAuthenticationToken user,
            @PathVariable String id,
            @RequestBody ApprovalVoteRepresentation body) {
        log.info("POST /requests/" + id + "/approvalVotes");
        RequestProperties properties = requestPropertiesService.findByProcessInstanceId(id);
        ApprovalVote vote = approvalVoteRepository.findOneByProcessInstanceIdAndCreator(id, user.getUser());
        if (vote != null) {
            vote.setValue(Value.valueOf(body.getValue()));
        } else {
            vote = new ApprovalVote(id, user.getUser(), Value.valueOf(body.getValue()));
        }
        vote = approvalVoteRepository.save(vote);
        log.info("properties: " + properties);
        log.info("user: " + user);
        properties.addApprovalVote(user.getUser(), vote);
        requestPropertiesService.save(properties);

        return new ApprovalVoteRepresentation(vote);
    }

    @ResponseStatus(value=HttpStatus.FORBIDDEN, reason="Update not allowed.")
    public class UpdateNotAllowed extends RuntimeException {
        private static final long serialVersionUID = 4000154580392628894L;
        public UpdateNotAllowed() {
            super("Update not allowed. Not the owner.");
        }
    }

    @RequestMapping(value = "/requests/{id}/approvalVotes/{voteId}", method = RequestMethod.PUT)
    public ApprovalVoteRepresentation updateVote(
            UserAuthenticationToken user,
            @PathVariable String id,
            @PathVariable Long voteId,
            @RequestBody ApprovalVoteRepresentation body) {
        log.info("PUT /requests/" + id + "/approvalVotes/" + voteId);
        ApprovalVote vote = approvalVoteRepository.findOne(voteId);
        if (!vote.getCreator().getId().equals(user.getUser().getId())) {
            throw new UpdateNotAllowed();
        }
        vote.setValue(Value.valueOf(body.getValue()));
        vote.setTimeEdited(new Date());
        vote = approvalVoteRepository.save(vote);

        return new ApprovalVoteRepresentation(vote);
    }

}
