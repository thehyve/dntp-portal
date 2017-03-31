/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
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
import business.models.LabRequest;
import business.models.LabRequestRepository;
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
    private LabRequestRepository labRequestRepository;

    @Autowired
    private CommentRepository commentRepository;

    @PreAuthorize("isAuthenticated() and hasRole('palga')")
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
            + "(hasRole('palga') or hasPermission(#id, 'isScientificCouncil'))")
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

    @PreAuthorize("isAuthenticated() and hasRole('palga')")
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
            + "(hasRole('palga') or hasPermission(#id, 'isScientificCouncil'))")
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

    @PreAuthorize("isAuthenticated() and "
            + "(hasRole('palga') or hasPermission(#id, 'isScientificCouncil'))")
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

    @PreAuthorize("isAuthenticated() and hasRole('palga')")
    @RequestMapping(value = "/requests/{id}/comments/{commentId}", method = RequestMethod.DELETE)
    public void removeComment(
            UserAuthenticationToken user,
            @PathVariable String id,
            @PathVariable Long commentId) {
        log.info("DELETE /requests/" + id + "/comments");
        Comment comment = commentRepository.findOne(commentId);
        if (!comment.getCreator().getId().equals(user.getUser().getId())) {
            throw new UpdateNotAllowed();
        }
        RequestProperties properties = requestPropertiesService.findByProcessInstanceId(id);
        properties.getComments().remove(comment);
        requestPropertiesService.save(properties);
    }

    @PreAuthorize("isAuthenticated() and "
            + "(hasRole('palga') or hasPermission(#id, 'isScientificCouncil'))")
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
    
    @PreAuthorize("isAuthenticated() and "
            + "(hasPermission(#id, 'isLabRequestRequester') "
            + " or hasPermission(#id, 'isLabRequestLabuser') "
            + " or hasPermission(#id, 'isLabRequestHubuser') "
            + " or hasRole('palga')"
            + ")")
    @RequestMapping(value = "/labrequests/{id}/comments", method = RequestMethod.POST)
    public CommentRepresentation addLabRequestComment(
            UserAuthenticationToken user,
            @PathVariable Long id,
            @RequestBody CommentRepresentation body) {
        log.info("POST /lab-requests/" + id + "/comments");
        LabRequest labRequest = labRequestRepository.findOne(id);
        Comment comment = new Comment(labRequest.getProcessInstanceId(), user.getUser(), body.getContents());
        comment = commentRepository.save(comment);
        labRequest.addComment(comment);
        labRequestRepository.save(labRequest);

        return new CommentRepresentation(comment);
    }
    
    @PreAuthorize("isAuthenticated() and "
            + "(hasRole('palga')"
            + " or hasPermission(#id, 'isLabRequestRequester') "
            + " or hasPermission(#id, 'isLabRequestLabuser') "
            + " or hasPermission(#id, 'isLabRequestHubuser') "
            + ")")
    @RequestMapping(value = "/labrequests/{id}/comments/{commentId}", method = RequestMethod.PUT)
    public CommentRepresentation updateLabRequestComment(
            UserAuthenticationToken user,
            @PathVariable Long id,
            @PathVariable Long commentId,
            @RequestBody CommentRepresentation body) {
        log.info("PUT /lab-requests/" + id + "/comments/" + commentId);
        LabRequest labRequest = labRequestRepository.findOne(id);
        Comment comment = commentRepository.findOne(commentId);
        if (labRequest != null && comment != null) {
            if (!comment.getCreator().getId().equals(user.getUser().getId())) {
                throw new UpdateNotAllowed();
            }
            if (!labRequest.getComments().contains(comment)) {
                throw new UpdateNotAllowed();
            }
            comment.setContents(body.getContents());
            comment.setTimeEdited(new Date());
            comment = commentRepository.save(comment);
        }
        return new CommentRepresentation(comment);
    }
    
    @PreAuthorize("isAuthenticated() and "
            + "(hasRole('palga')"
            + " or hasPermission(#id, 'isLabRequestRequester') "
            + " or hasPermission(#id, 'isLabRequestHubuser') "
            + " or hasPermission(#id, 'isLabRequestLabuser') "
            + ")")
    @RequestMapping(value = "/labrequests/{id}/comments/{commentId}", method = RequestMethod.DELETE)
    public void removeLabRequestComment(
            UserAuthenticationToken user,
            @PathVariable Long id,
            @PathVariable Long commentId) {
        log.info("DELETE /lab-requests/" + id + "/comments/" + commentId);
        LabRequest labRequest = labRequestRepository.findOne(id);
        Comment comment = commentRepository.findOne(commentId);
        if (labRequest != null && comment != null) {
            if (!comment.getCreator().getId().equals(user.getUser().getId())) {
                throw new UpdateNotAllowed();
            }
            labRequest.getComments().remove(comment);
            labRequestRepository.save(labRequest);
        }
    }

}
