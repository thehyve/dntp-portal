/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business.controllers;

import java.util.*;

import javax.validation.Valid;

import business.representation.*;
import business.services.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import business.security.UserAuthenticationToken;
import business.services.LabRequestService;


@RestController
public class LabRequestController {

    Log log = LogFactory.getLog(getClass());

    @Autowired
    private LabRequestService labRequestService;

    @Autowired
    private LabRequestQueryService labRequestQueryService;

    @Autowired
    private LabRequestStatusService labRequestStatusService;

    @Autowired
    private LabRequestDeliveryService labRequestDeliveryService;


    @PreAuthorize("isAuthenticated() and (hasRole('requester') or hasRole('palga') or "
            + "hasRole('lab_user') or hasRole('hub_user') )")
    @RequestMapping(value = "/api/labrequests", method = RequestMethod.GET)
    public List<LabRequestRepresentation> getLabRequests(
            UserAuthenticationToken user) {
        log.info("GET /api/labrequests");
        return labRequestQueryService.findLabRequestsForUser(user.getUser(), false);
    }

    @PreAuthorize("isAuthenticated() and (hasRole('requester') or hasRole('palga') or "
            + "hasRole('lab_user') or hasRole('hub_user') )")
    @RequestMapping(value = "/api/labrequests/detailed", method = RequestMethod.GET)
    public List<LabRequestRepresentation> getDetailedLabRequests(
            UserAuthenticationToken user) {
        log.info("GET /api/labrequests/detailed");
        return labRequestQueryService.findLabRequestsForUser(user.getUser(), true);
    }

    @PreAuthorize("isAuthenticated() and "
            + "(hasRole('palga') "
            + " or hasPermission(#id, 'isLabRequestRequester') "
            + " or hasPermission(#id, 'isLabRequestPathologistOrContactPerson') "
            + " or hasPermission(#id, 'isLabRequestLabuser') "
            + " or hasPermission(#id, 'isLabRequestHubuser') "
            + ")")
    @RequestMapping(value = "/api/labrequests/{id}", method = RequestMethod.GET)
    public LabRequestRepresentation getLabRequest(
            UserAuthenticationToken user,
            @PathVariable Long id) {
        log.info("GET /api/labrequests/" + id);
        return labRequestQueryService.get(id);
    }

    /**
     * Reject a lab request.
     * Action only allowed for lab users.
     *
     * @param user the authorised user.
     * @param id the lab request id.
     * @return a representation of the rejected lab request.
     */
    @PreAuthorize("isAuthenticated() and hasPermission(#id, 'labRequestAssignedToUser') and "
            + "hasPermission(#id, 'isLabRequestLabuser')")
    @RequestMapping(value = "/api/labrequests/{id}/reject", method = RequestMethod.PUT)
    public LabRequestRepresentation reject(UserAuthenticationToken user,
            @PathVariable Long id, @Valid @RequestBody LabRequestRepresentation body) {
        log.info("PUT /api/labrequests/" + id + "/reject");

        return labRequestService.reject(id, body);
    }

    /**
     * Return a lab request to status 'Waiting for lab approval'.
     *
     * @param user the authorised user.
     * @param id the lab request id.
     * @return a representation of the rejected lab request.
     */
    @PreAuthorize("isAuthenticated() and hasPermission(#id, 'labRequestAssignedToUser')")
    @RequestMapping(value = "/api/labrequests/{id}/undoreject", method = RequestMethod.PUT)
    public LabRequestRepresentation undoReject(UserAuthenticationToken user,
            @PathVariable Long id, @RequestBody LabRequestRepresentation body) {
        log.info("PUT /api/labrequests/" + id + "/undoreject");

        return labRequestService.undoReject(id, user);
    }

    /**
     * Approve a lab request. Action only allowed for lab users.
     *
     * @param user the authorised user.
     * @param id the lab request id.
     * @return a representation of the approved lab request.
     */
    @PreAuthorize("isAuthenticated() and hasPermission(#id, 'labRequestAssignedToUser') and "
            + "hasPermission(#id, 'isLabRequestLabuser')")
    @RequestMapping(value = "/api/labrequests/{id}/approve", method = RequestMethod.PUT)
    public LabRequestRepresentation approve(UserAuthenticationToken user,
            @PathVariable Long id,
            @RequestBody LabRequestRepresentation body) {
        log.info("PUT /api/labrequests/" + id + "/approve");

        return labRequestService.approve(id);
    }

    /**
     * Undo approval for a previously approved lab request. Action only allowed for lab users.
     *
     * @param user the authorised user.
     * @param id the lab request id.
     * @return a representation of the approved lab request.
     */
    @PreAuthorize("isAuthenticated() and hasPermission(#id, 'labRequestAssignedToUser') and "
            + "hasPermission(#id, 'isLabRequestLabuser')")
    @RequestMapping(value = "/api/labrequests/{id}/undoapprove", method = RequestMethod.PUT)
    public LabRequestRepresentation undoApprove(UserAuthenticationToken user,
                                                @PathVariable Long id,
                                                @RequestBody LabRequestRepresentation body) {
        log.info("PUT /api/labrequests/" + id + "/undoapprove");

        return labRequestService.undoApprove(id, user);
    }

    @PreAuthorize("isAuthenticated() and hasPermission(#id, 'labRequestAssignedToUser')")
    @RequestMapping(value = "/api/labrequests/{id}/sending", method = RequestMethod.PUT, consumes = {"application/json"})
    public LabRequestRepresentation sending(UserAuthenticationToken user,
            @PathVariable Long id,
            @RequestBody ReturnDateRepresentation body) {
        log.info("PUT /api/labrequests/" + id + "/sending");

        return labRequestDeliveryService.sending(id, body, user);
    }

    @PreAuthorize("isAuthenticated() and hasPermission(#id, 'isLabRequestRequester')")
    @RequestMapping(value = "/api/labrequests/{id}/received", method = RequestMethod.PUT)
    public LabRequestRepresentation received(UserAuthenticationToken user,
            @PathVariable Long id,
            @RequestBody LabRequestRepresentation body
            ) {
        log.info("PUT /api/labrequests/" + id + "/received");

        return labRequestDeliveryService.received(id, body, user);
    }

    @PreAuthorize("isAuthenticated() and hasPermission(#id, 'isLabRequestRequester')")
    @RequestMapping(value = "/api/labrequests/{id}/returning", method = RequestMethod.PUT)
    public LabRequestRepresentation returning(UserAuthenticationToken user,
            @PathVariable Long id) {
        log.info("PUT /api/labrequests/" + id + "/returning");

        return labRequestDeliveryService.returning(id);
    }

    /**
     * Updates the status to 'Returned' (if the current status is in
     * {@link LabRequestDeliveryService#labRequestReturnedEnabledStatuses}) and completes the task associated with
     * the lab request.
     *
     * From the <var>body</var>, only the <var>samplesMissing</var> and <var>missingSamples</var>
     * are processed. When <var>samplesMissing</var> is true, the contents of <var>missingSamples</var>
     * is added as a comment to the lab request.
     *
     * @param user the currently authenticated user.
     * @param id the id of the lab request.
     * @param body the LabRequestRepresentation with <var>samplesMissing</var> and <var>missingSamples</var>
     *        fields.
     * @return the updated (completed) lab request.
     */
    @PreAuthorize("isAuthenticated() and ( hasPermission(#id, 'isLabRequestLabuser') or "
            + "hasPermission(#id, 'isLabRequestHubuser') )")
    @RequestMapping(value = "/api/labrequests/{id}/completereturned", method = RequestMethod.PUT)
    public LabRequestRepresentation completeReturned(UserAuthenticationToken user,
            @PathVariable Long id,
            @RequestBody LabRequestRepresentation body
            ) {
        log.info("PUT /api/labrequests/" + id + "/completereturned");

        return labRequestDeliveryService.completeReturned(id, body, user);
    }

    @PreAuthorize("isAuthenticated() and hasRole('palga')")
    @RequestMapping(value = "/api/labrequests/{id}/completerejected", method = RequestMethod.PUT)
    public LabRequestRepresentation completeRejected(UserAuthenticationToken user,
            @PathVariable Long id,
            @RequestBody LabRequestRepresentation body
            ) {
        log.info("PUT /api/labrequests/" + id + "/completerejected");

        return labRequestDeliveryService.completeRejected(id);
    }

    @PreAuthorize("isAuthenticated() and ( hasPermission(#id, 'isLabRequestLabuser') or "
            + "hasPermission(#id, 'isLabRequestHubuser') )")
    @RequestMapping(value = "/api/labrequests/{id}/completereportsonly", method = RequestMethod.PUT)
    public LabRequestRepresentation completeReportsOnly(UserAuthenticationToken user,
            @PathVariable Long id,
            @RequestBody LabRequestRepresentation body
            ) {
        log.info("PUT /api/labrequests/" + id + "/completereportsonly");

        return labRequestDeliveryService.completeReportsOnly(id, body, user);
    }

    @PreAuthorize("isAuthenticated() and ( hasPermission(#id, 'isLabRequestLabuser') or "
            + "hasPermission(#id, 'isLabRequestHubuser') )")
    @RequestMapping(value = "/api/labrequests/{id}/claim", method = RequestMethod.PUT)
    public LabRequestRepresentation claim(UserAuthenticationToken user,
            @PathVariable Long id) {
        log.info("PUT /api/labrequests/" + id + "/claim");

        return labRequestStatusService.claim(id, user);
    }

    @PreAuthorize("isAuthenticated() and ( hasPermission(#id, 'isLabRequestLabuser') or "
            + "hasPermission(#id, 'isLabRequestHubuser') )")
    @RequestMapping(value = "/api/labrequests/{id}/unclaim", method = RequestMethod.PUT)
    public LabRequestRepresentation unclaim(UserAuthenticationToken user,
            @PathVariable Long id) {
        log.info("PUT /api/labrequests/" + id + "/unclaim for userId " + user.getId());

        return labRequestStatusService.unclaim(id, user);
    }

    @PreAuthorize("isAuthenticated() and hasPermission(#id, 'labRequestAssignedToUser')")
    @RequestMapping (value = "/api/labrequests/{id}", method = RequestMethod.PUT)
    public LabRequestRepresentation update(UserAuthenticationToken user,
            @PathVariable Long id,
            @RequestBody LabRequestRepresentation body) {
        log.info("PUT /api/labrequests/" + id + " for userId " + user.getId());
        return labRequestService.update(id, body, user);
    }

}
