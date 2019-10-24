/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business.controllers;

import java.util.List;

import business.services.CommentService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import business.representation.CommentRepresentation;
import business.security.UserAuthenticationToken;

@RestController
public class CommentController {

    Log log = LogFactory.getLog(getClass());

    @Autowired
    private CommentService commentService;

    @PreAuthorize("isAuthenticated() and hasRole('palga')")
    @RequestMapping(value = "/api/requests/{id}/comments", method = RequestMethod.GET)
    public List<CommentRepresentation> getComments(
            UserAuthenticationToken user,
            @PathVariable String id) {
        log.info("GET /api/requests/" + id + "/comments");
        return commentService.getRequestComments(id);
    }

    @PreAuthorize("isAuthenticated() and "
            + "(hasRole('palga') or hasPermission(#id, 'isScientificCouncil'))")
    @RequestMapping(value = "/api/requests/{id}/approvalComments", method = RequestMethod.GET)
    public List<CommentRepresentation> getApprovalComments(
            UserAuthenticationToken user,
            @PathVariable String id) {
        log.info("GET /api/requests/" + id + "/comments");
        return commentService.getApprovalComments(id);
    }

    @PreAuthorize("isAuthenticated() and hasRole('palga')")
    @RequestMapping(value = "/api/requests/{id}/comments", method = RequestMethod.POST)
    public CommentRepresentation addComment(
            UserAuthenticationToken user,
            @PathVariable String id,
            @RequestBody CommentRepresentation body) {
        log.info("POST /api/requests/" + id + "/comments");
        return commentService.addRequestComment(user.getUser(), id, body);
    }

    @PreAuthorize("isAuthenticated() and "
            + "(hasRole('palga') or hasPermission(#id, 'isScientificCouncil'))")
    @RequestMapping(value = "/api/requests/{id}/approvalComments", method = RequestMethod.POST)
    public CommentRepresentation addApprovalComment(
            UserAuthenticationToken user,
            @PathVariable String id,
            @RequestBody CommentRepresentation body) {
        log.info("POST /api/requests/" + id + "/comments");
        return commentService.addApprovalComment(user.getUser(), id, body);
    }

    @PreAuthorize("isAuthenticated() and "
            + "(hasRole('palga') or hasPermission(#id, 'isScientificCouncil'))")
    @RequestMapping(value = "/api/requests/{id}/comments/{commentId}", method = RequestMethod.PUT)
    public CommentRepresentation updateComment(
            UserAuthenticationToken user,
            @PathVariable String id,
            @PathVariable Long commentId,
            @RequestBody CommentRepresentation body) {
        log.info("PUT /api/requests/" + id + "/comments/" + commentId);
        return commentService.updateComment(user.getUser(), commentId, body);
    }

    @PreAuthorize("isAuthenticated() and hasRole('palga')")
    @RequestMapping(value = "/api/requests/{id}/comments/{commentId}", method = RequestMethod.DELETE)
    public void removeComment(
            UserAuthenticationToken user,
            @PathVariable String id,
            @PathVariable Long commentId) {
        log.info("DELETE /api/requests/" + id + "/comments");
        commentService.removeRequestComment(user.getUser(), id, commentId);
    }

    @PreAuthorize("isAuthenticated() and "
            + "(hasRole('palga') or hasPermission(#id, 'isScientificCouncil'))")
    @RequestMapping(value = "/api/requests/{id}/approvalComments/{commentId}", method = RequestMethod.DELETE)
    public void removeApprovalComment(
            UserAuthenticationToken user,
            @PathVariable String id,
            @PathVariable Long commentId) {
        log.info("PUT /api/requests/" + id + "/comments");
        commentService.removeApprovalComment(user.getUser(), id, commentId);
    }

    @PreAuthorize("isAuthenticated() and "
            + "(hasPermission(#id, 'isLabRequestRequester') "
            + " or hasPermission(#id, 'isLabRequestLabuser') "
            + " or hasPermission(#id, 'isLabRequestHubuser') "
            + " or hasRole('palga')"
            + ")")
    @RequestMapping(value = "/api/labrequests/{id}/comments", method = RequestMethod.POST)
    public CommentRepresentation addLabRequestComment(
            UserAuthenticationToken user,
            @PathVariable Long id,
            @RequestBody CommentRepresentation body) {
        log.info("POST /api/labrequests/" + id + "/comments");
        return commentService.addLabRequestComment(user.getUser(), id, body);
    }

    @PreAuthorize("isAuthenticated() and "
            + "(hasRole('palga')"
            + " or hasPermission(#id, 'isLabRequestRequester') "
            + " or hasPermission(#id, 'isLabRequestLabuser') "
            + " or hasPermission(#id, 'isLabRequestHubuser') "
            + ")")
    @RequestMapping(value = "/api/labrequests/{id}/comments/{commentId}", method = RequestMethod.PUT)
    public CommentRepresentation updateLabRequestComment(
            UserAuthenticationToken user,
            @PathVariable Long id,
            @PathVariable Long commentId,
            @RequestBody CommentRepresentation body) {
        log.info("PUT /api/labrequests/" + id + "/comments/" + commentId);
        // check if the comment is associated with the lab request.
        commentService.findLabRequestComment(id, commentId);
        // update the comment.
        return commentService.updateComment(user.getUser(), commentId, body);
    }

    @PreAuthorize("isAuthenticated() and "
            + "(hasRole('palga')"
            + " or hasPermission(#id, 'isLabRequestRequester') "
            + " or hasPermission(#id, 'isLabRequestHubuser') "
            + " or hasPermission(#id, 'isLabRequestLabuser') "
            + ")")
    @RequestMapping(value = "/api/labrequests/{id}/comments/{commentId}", method = RequestMethod.DELETE)
    public void removeLabRequestComment(
            UserAuthenticationToken user,
            @PathVariable Long id,
            @PathVariable Long commentId) {
        log.info("DELETE /api/labrequests/" + id + "/comments/" + commentId);
        commentService.removeLabRequestComment(user, id, commentId);
    }

}
