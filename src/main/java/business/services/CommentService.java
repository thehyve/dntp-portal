package business.services;

import business.exceptions.CommentNotFound;
import business.exceptions.UpdateNotAllowed;
import business.models.*;
import business.representation.CommentRepresentation;
import business.representation.LabRequestRepresentation;
import business.security.UserAuthenticationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class CommentService {

    @Autowired
    private RequestPropertiesService requestPropertiesService;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private LabRequestService labRequestService;

    @Autowired
    private UserService userService;

    @Autowired
    private MailService mailService;

    /**
     * Gets all request comments for a request.
     * @param processInstanceId the process instance id of the request.
     * @return the list of comment representations.
     */
    @Transactional(readOnly = true)
    public List<CommentRepresentation> getRequestComments(String processInstanceId) {
        RequestProperties properties = requestPropertiesService.findByProcessInstanceId(processInstanceId);
        List<CommentRepresentation> comments = new ArrayList<>();
        for (Comment comment: properties.getComments()) {
            comments.add(new CommentRepresentation(comment));
        }
        return comments;
    }

    /**
     * Gets all approval comments for a request.
     * @param processInstanceId the process instance id of the request.
     * @return the list of comment representations.
     */
    @Transactional(readOnly = true)
    public List<CommentRepresentation> getApprovalComments(String processInstanceId) {
        RequestProperties properties = requestPropertiesService.findByProcessInstanceId(processInstanceId);
        List<CommentRepresentation> comments = new ArrayList<>();
        for (Comment comment: properties.getApprovalComments()) {
            comments.add(new CommentRepresentation(comment));
        }
        return comments;
    }

    /**
     * Gets lab request comment for a lab request id and comment id if
     * the comment with the id exists and is attached to the lab request.
     * Throws {@link CommentNotFound} if the lab request or the comment do not exist
     * or if the comment is not associated with the lab request.
     *
     * @param labRequestId the id of the lab request.
     * @param commentId the id of the comment.
     * @return the comment representation.
     */
    @Transactional(readOnly = true)
    public CommentRepresentation findLabRequestComment(Long labRequestId, Long commentId) {
        LabRequest labRequest = labRequestService.findOne(labRequestId);
        Comment comment = commentRepository.findOne(commentId);
        if (labRequest == null || comment == null) {
            throw new CommentNotFound();
        }
        if (!labRequest.getComments().contains(comment)) {
            throw new CommentNotFound();
        }
        return new CommentRepresentation(comment);
    }

    /**
     * Adds a new comment to a request.
     * @param user the creator of the comment.
     * @param processInstanceId the process instance id of the request.
     * @param body the contents of the comment.
     * @return the result comment representation.
     */
    public CommentRepresentation addRequestComment(User user, String processInstanceId, CommentRepresentation body) {
        RequestProperties properties = requestPropertiesService.findByProcessInstanceId(processInstanceId);
        Comment comment = new Comment(processInstanceId, user, body.getContents());
        comment = commentRepository.save(comment);
        properties.addComment(comment);
        requestPropertiesService.save(properties);

        return new CommentRepresentation(comment);
    }

    /**
     * Adds a new approval comment to a request.
     * @param user the creator of the comment.
     * @param processInstanceId the process instance id of the request.
     * @param body the contents of the comment.
     * @return the result comment representation.
     */
    public CommentRepresentation addApprovalComment(User user, String processInstanceId, CommentRepresentation body) {
        RequestProperties properties = requestPropertiesService.findByProcessInstanceId(processInstanceId);
        Comment comment = new Comment(processInstanceId, user, body.getContents());
        comment = commentRepository.save(comment);
        properties.addApprovalComment(comment);
        requestPropertiesService.save(properties);

        return new CommentRepresentation(comment);
    }

    public CommentRepresentation updateComment(User user, Long commentId, CommentRepresentation body) {
        Comment comment = commentRepository.findOne(commentId);
        if (!comment.getCreator().getId().equals(user.getId())) {
            throw new UpdateNotAllowed();
        }
        comment.setContents(body.getContents());
        comment.setTimeEdited(new Date());
        comment = commentRepository.save(comment);

        return new CommentRepresentation(comment);
    }

    public void removeRequestComment(User user, String processInstanceId, Long commentId) {
        Comment comment = commentRepository.findOne(commentId);
        if (!comment.getCreator().getId().equals(user.getId())) {
            throw new UpdateNotAllowed();
        }
        RequestProperties properties = requestPropertiesService.findByProcessInstanceId(processInstanceId);
        properties.getComments().remove(comment);
        requestPropertiesService.save(properties);
    }

    public void removeApprovalComment(User user, String id, Long commentId) {
        Comment comment = commentRepository.findOne(commentId);
        if (!comment.getCreator().getId().equals(user.getId())) {
            throw new UpdateNotAllowed();
        }
        RequestProperties properties = requestPropertiesService.findByProcessInstanceId(id);
        properties.getApprovalComments().remove(comment);
        requestPropertiesService.save(properties);
    }

    public CommentRepresentation addLabRequestComment(User user, Long id, CommentRepresentation body) {
        LabRequest labRequest = labRequestService.findOne(id);
        Comment comment = new Comment(labRequest.getProcessInstanceId(), user, body.getContents());
        comment = commentRepository.save(comment);
        labRequest.addComment(comment);
        labRequestService.save(labRequest);

        LabRequestRepresentation labRequestRepresentation = new LabRequestRepresentation(labRequest);
        labRequestService.transferLabRequestData(labRequestRepresentation, false);
        Lab lab = labRequest.getLab();
        List<String> hubUserEmailAddresses = new ArrayList<>();
        if (lab.isHubAssistanceEnabled() && labRequest.isHubAssistanceRequested()) {
            for (User hubUser: userService.findHubUsersForLab(lab)) {
                hubUserEmailAddresses.add(hubUser.getContactData().getEmail());
            }
        }
        if (body.isNotificationRequested()){
            if (user.isLabUser()) {
                // if lab user: to requester, cc hub users
                String requesterEmail = labRequestRepresentation.getRequesterEmail();
                mailService.sendNewLabRequestCommentNotification(labRequestRepresentation, new CommentRepresentation(comment),
                        Collections.singleton(requesterEmail), hubUserEmailAddresses);
                comment.setNotificationSent(true);
                comment = commentRepository.save(comment);
            } else if (user.isHubUser()) {
                // if hub user: to requester only
                String requesterEmail = labRequestRepresentation.getRequesterEmail();
                mailService.sendNewLabRequestCommentNotification(labRequestRepresentation, new CommentRepresentation(comment),
                        Collections.singleton(requesterEmail), Collections.emptyList());
                comment.setNotificationSent(true);
                comment = commentRepository.save(comment);
            } else if (user.isRequester()) {
                // if requester: to lab, cc hub users
                Collection<String> labEmailAddresses = labRequestRepresentation.getLab().getEmailAddresses();
                mailService.sendNewLabRequestCommentNotification(labRequestRepresentation, new CommentRepresentation(comment),
                        labEmailAddresses, hubUserEmailAddresses);
                comment.setNotificationSent(true);
                comment = commentRepository.save(comment);
            }
        }
        return new CommentRepresentation(comment);

    }

    public void removeLabRequestComment(UserAuthenticationToken user, Long id, Long commentId) {
        LabRequest labRequest = labRequestService.findOne(id);
        Comment comment = commentRepository.findOne(commentId);
        if (labRequest != null && comment != null) {
            if (!comment.getCreator().getId().equals(user.getUser().getId())) {
                throw new UpdateNotAllowed();
            }
            labRequest.getComments().remove(comment);
            labRequestService.save(labRequest);
        }
    }

}
