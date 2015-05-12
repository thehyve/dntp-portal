package business.controllers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import business.exceptions.UpdateNotAllowed;
import business.models.Comment;
import business.models.CommentRepository;
import business.models.RequestProperties;
import business.representation.CommentRepresentation;
import business.security.UserAuthenticationToken;
import business.services.RequestPropertiesService;

@RestController
public class CommentController {

    Log log = LogFactory.getLog(getClass());

    @Autowired
    private RequestPropertiesService requestPropertiesService;

    @Autowired
    private CommentRepository commentRepository;

    @PreAuthorize("isAuthenticated() and hasPermission(#id, 'isPalgaUser')")
    @RequestMapping(value = "/requests/{id}/comments", method = RequestMethod.GET)
    public List<CommentRepresentation> getComments(
            UserAuthenticationToken user,
            @PathVariable String id) {
        log.info("GET /requests/" + id + "/comments");
        RequestProperties properties = requestPropertiesService.findByProcessInstanceId(id);
        List<CommentRepresentation> comments = new ArrayList<CommentRepresentation>();
        for (Comment comment: properties.getComments()) {
            comments.add(new CommentRepresentation(comment));
        }
        return comments;
    }

    @PreAuthorize("isAuthenticated() and "
            + "(hasPermission(#id, 'isPalgaUser') or hasPermission(#id, 'isScientificCouncil'))")
    @RequestMapping(value = "/requests/{id}/approvalComments", method = RequestMethod.GET)
    public List<CommentRepresentation> getApprovalComments(
            UserAuthenticationToken user,
            @PathVariable String id) {
        log.info("GET /requests/" + id + "/comments");
        RequestProperties properties = requestPropertiesService.findByProcessInstanceId(id);
        List<CommentRepresentation> comments = new ArrayList<CommentRepresentation>();
        for (Comment comment: properties.getApprovalComments()) {
            comments.add(new CommentRepresentation(comment));
        }
        return comments;
    }

    @PreAuthorize("isAuthenticated() and hasPermission(#id, 'isPalgaUser')")
    @RequestMapping(value = "/requests/{id}/comments", method = RequestMethod.POST)
    public CommentRepresentation addComment(
            UserAuthenticationToken user,
            @PathVariable String id,
            @RequestBody CommentRepresentation body) {
        log.info("POST /requests/" + id + "/comments");
        RequestProperties properties = requestPropertiesService.findByProcessInstanceId(id);
        Comment comment = new Comment(id, user.getUser(), body.getContents());
        comment = commentRepository.save(comment);
        properties.addComment(comment);
        requestPropertiesService.save(properties);

        return new CommentRepresentation(comment);
    }

    @PreAuthorize("isAuthenticated() and "
            + "(hasPermission(#id, 'isPalgaUser') or hasPermission(#id, 'isScientificCouncil'))")
    @RequestMapping(value = "/requests/{id}/approvalComments", method = RequestMethod.POST)
    public CommentRepresentation addApprovalComment(
            UserAuthenticationToken user,
            @PathVariable String id,
            @RequestBody CommentRepresentation body) {
        log.info("POST /requests/" + id + "/comments");
        RequestProperties properties = requestPropertiesService.findByProcessInstanceId(id);
        Comment comment = new Comment(id, user.getUser(), body.getContents());
        comment = commentRepository.save(comment);
        properties.addApprovalComment(comment);
        requestPropertiesService.save(properties);

        return new CommentRepresentation(comment);
    }

    @PreAuthorize("isAuthenticated() and hasPermission(#id, 'isPalgaUser')")
    @RequestMapping(value = "/requests/{id}/comments/{commentId}", method = RequestMethod.PUT)
    public CommentRepresentation updateComment(
            UserAuthenticationToken user,
            @PathVariable String id,
            @PathVariable Long commentId,
            @RequestBody CommentRepresentation body) {
        log.info("PUT /requests/" + id + "/comments/" + commentId);
        Comment comment = commentRepository.findOne(commentId);
        if (!comment.getCreator().getId().equals(user.getUser().getId())) {
            throw new UpdateNotAllowed();
        }
        comment.setContents(body.getContents());
        comment.setTimeEdited(new Date());
        comment = commentRepository.save(comment);

        return new CommentRepresentation(comment);
    }

    @RequestMapping(value = "/requests/{id}/comments/{commentId}", method = RequestMethod.DELETE)
    public void removeComment(
            UserAuthenticationToken user,
            @PathVariable String id,
            @PathVariable Long commentId) {
        log.info("PUT /requests/" + id + "/comments");
        Comment comment = commentRepository.findOne(commentId);
        if (!comment.getCreator().getId().equals(user.getUser().getId())) {
            throw new UpdateNotAllowed();
        }
        RequestProperties properties = requestPropertiesService.findByProcessInstanceId(id);
        properties.getComments().remove(comment);
        requestPropertiesService.save(properties);
    }

    @PreAuthorize("isAuthenticated() and "
            + "(hasPermission(#id, 'isPalgaUser') or hasPermission(#id, 'isScientificCouncil'))")
    @RequestMapping(value = "/requests/{id}/approvalComments/{commentId}", method = RequestMethod.DELETE)
    public void removeApprovalComment(
            UserAuthenticationToken user,
            @PathVariable String id,
            @PathVariable Long commentId) {
        log.info("PUT /requests/" + id + "/comments");
        Comment comment = commentRepository.findOne(commentId);
        if (!comment.getCreator().getId().equals(user.getUser().getId())) {
            throw new UpdateNotAllowed();
        }
        RequestProperties properties = requestPropertiesService.findByProcessInstanceId(id);
        properties.getApprovalComments().remove(comment);
        requestPropertiesService.save(properties);
    }
    
}
