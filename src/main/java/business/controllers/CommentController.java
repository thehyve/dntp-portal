package business.controllers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

import business.models.Comment;
import business.models.CommentRepository;
import business.models.RequestProperties;
import business.models.RequestPropertiesRepository;
import business.representation.CommentRepresentation;
import business.security.UserAuthenticationToken;

@RestController
public class CommentController {

    Log log = LogFactory.getLog(getClass());
    
    @Autowired
    private RequestPropertiesRepository requestPropertiesRepository;
    
    @Autowired
    private CommentRepository commentRepository;

    @RequestMapping(value = "/requests/{id}/comments", method = RequestMethod.GET)
    public List<CommentRepresentation> getComments(
            UserAuthenticationToken user,
            @PathVariable String id) {
        log.info("GET /requests/" + id + "/comments");
        RequestProperties properties = requestPropertiesRepository.findByProcessInstanceId(id);
        List<CommentRepresentation> comments = new ArrayList<CommentRepresentation>();
        for (Comment comment: properties.getComments()) {
            comments.add(new CommentRepresentation(comment));
        }
        return comments;
    }
    
    @RequestMapping(value = "/requests/{id}/approvalComments", method = RequestMethod.GET)
    public List<CommentRepresentation> getApprovalComments(
            UserAuthenticationToken user,
            @PathVariable String id) {
        log.info("GET /requests/" + id + "/comments");
        RequestProperties properties = requestPropertiesRepository.findByProcessInstanceId(id);
        List<CommentRepresentation> comments = new ArrayList<CommentRepresentation>();
        for (Comment comment: properties.getApprovalComments()) {
            comments.add(new CommentRepresentation(comment));
        }
        return comments;
    }
    
    @RequestMapping(value = "/requests/{id}/comments", method = RequestMethod.POST)
    public CommentRepresentation addComment(
            UserAuthenticationToken user,
            @PathVariable String id,
            @RequestBody CommentRepresentation body) {
        log.info("POST /requests/" + id + "/comments");
        RequestProperties properties = requestPropertiesRepository.findByProcessInstanceId(id);
        Comment comment = new Comment(id, user.getUser(), body.getContents());
        comment = commentRepository.save(comment);
        properties.addComment(comment);
        requestPropertiesRepository.save(properties);
        
        return new CommentRepresentation(comment);
    }

    @RequestMapping(value = "/requests/{id}/approvalComments", method = RequestMethod.POST)
    public CommentRepresentation addApprovalComment(
            UserAuthenticationToken user,
            @PathVariable String id,
            @RequestBody CommentRepresentation body) {
        log.info("POST /requests/" + id + "/comments");
        RequestProperties properties = requestPropertiesRepository.findByProcessInstanceId(id);
        Comment comment = new Comment(id, user.getUser(), body.getContents());
        comment = commentRepository.save(comment);
        properties.addApprovalComment(comment);
        requestPropertiesRepository.save(properties);
        
        return new CommentRepresentation(comment);
    }
    
    @ResponseStatus(value=HttpStatus.FORBIDDEN, reason="Update not allowed.") 
    public class UpdateNotAllowed extends RuntimeException {
        private static final long serialVersionUID = 4000154580392628894L;
        public UpdateNotAllowed() {
            super("Update not allowed. Not the owner.");
        }
    }
    
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
        RequestProperties properties = requestPropertiesRepository.findByProcessInstanceId(id);
        properties.getComments().remove(comment);
        requestPropertiesRepository.save(properties);
    }
    
}
