/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business.controllers;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.task.DelegationState;
import org.activiti.engine.task.Task;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import business.exceptions.EmptyInput;
import business.exceptions.InvalidActionInStatus;
import business.exceptions.PaNumbersDownloadError;
import business.exceptions.PathologyNotFound;
import business.models.Comment;
import business.models.CommentRepository;
import business.models.LabRequest;
import business.models.PathologyItem;
import business.models.PathologyItemRepository;
import business.models.User;
import business.representation.LabRequestRepresentation;
import business.representation.PathologyRepresentation;
import business.representation.RequestRepresentation;
import business.security.UserAuthenticationToken;
import business.services.LabRequestService;
import business.services.PaNumberService;
import business.services.RequestFormService;
import business.services.RequestService;


@RestController
public class LabRequestController {

    Log log = LogFactory.getLog(getClass());

    @Autowired
    private LabRequestService labRequestService;

    @Autowired
    private PathologyItemRepository pathologyItemRepository;

    @Autowired
    private PaNumberService paNumberService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private CommentRepository commentRepository;
    
    @Autowired
    private RequestService requestService;

    @Autowired
    private RequestFormService requestFormService;

    @PreAuthorize("isAuthenticated() and (hasRole('requester') or hasRole('palga') or "
            + "hasRole('lab_user') or hasRole('hub_user') )")
    @RequestMapping(value = "/labrequests", method = RequestMethod.GET)
    public List<LabRequestRepresentation> getLabRequests(
            UserAuthenticationToken user) {
        log.info("GET /labrequests");
        return labRequestService.findLabRequestsForUser(user.getUser(), false);
    }

    @PreAuthorize("isAuthenticated() and (hasRole('requester') or hasRole('palga') or "
            + "hasRole('lab_user') or hasRole('hub_user') )")
    @RequestMapping(value = "/labrequests/detailed", method = RequestMethod.GET)
    public List<LabRequestRepresentation> getDetailedLabRequests(
            UserAuthenticationToken user) {
        log.info("GET /labrequests/detailed");
        return labRequestService.findLabRequestsForUser(user.getUser(), true);
    }

    @PreAuthorize("isAuthenticated() and "
            + "(hasRole('palga') "
            + " or hasPermission(#id, 'isLabRequestRequester') "
            + " or hasPermission(#id, 'isLabRequestLabuser') "
            + " or hasPermission(#id, 'isLabRequestHubuser') "
            + ")")
    @RequestMapping(value = "/labrequests/{id}", method = RequestMethod.GET)
    public LabRequestRepresentation getLabRequest(
            UserAuthenticationToken user,
            @PathVariable Long id) {
        log.info("GET /labrequests/" + id);
        LabRequest labRequest = labRequestService.findOne(id);
        LabRequestRepresentation representation = new LabRequestRepresentation(labRequest);
        labRequestService.transferLabRequestData(representation);
        labRequestService.transferLabRequestDetails(representation, true);
        labRequestService.transferExcerptListData(representation);
        return representation;
    }

    /**
     * Reject a lab request and complete the associated task.
     * Action only allowed for lab users.
     *
     * @param user the authorised user.
     * @param id the lab request id.
     * @return a representation of the rejected lab request.
     */
    @PreAuthorize("isAuthenticated() and hasPermission(#id, 'labRequestAssignedToUser') and "
            + "hasPermission(#id, 'isLabRequestLabuser')")
    @RequestMapping(value = "/labrequests/{id}/reject", method = RequestMethod.PUT)
    public LabRequestRepresentation reject(UserAuthenticationToken user,
            @PathVariable Long id, @RequestBody LabRequestRepresentation body) {
        log.info("PUT /labrequests/" + id + "/reject");

        LabRequest labRequest = labRequestService.findOne(id);
        labRequest.setRejectReason(body.getRejectReason());
        labRequest.setRejectDate(new Date());

        labRequest = labRequestService.updateStatus(labRequest, "Rejected");

        Task task = labRequestService.getTask(labRequest.getTaskId(),
                "lab_request");
        // complete task
        if (task.getDelegationState() == DelegationState.PENDING) {
            taskService.resolveTask(task.getId());
        }
        taskService.complete(task.getId());

        LabRequestRepresentation representation = new LabRequestRepresentation(
                labRequest);
        labRequestService.transferLabRequestData(representation);
        return representation;

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
    @RequestMapping(value = "/labrequests/{id}/accept", method = RequestMethod.PUT)
    public LabRequestRepresentation accept(UserAuthenticationToken user,
            @PathVariable Long id,
            @RequestBody LabRequestRepresentation body) {
        log.info("PUT /labrequests/" + id + "/accept");

        LabRequest labRequest = labRequestService.findOne(id);
        labRequest = labRequestService.updateStatus(labRequest, "Approved");

        LabRequestRepresentation representation = new LabRequestRepresentation(
                labRequest);
        labRequestService.transferLabRequestData(representation);
        return representation;
    }

    @PreAuthorize("isAuthenticated() and hasPermission(#id, 'labRequestAssignedToUser')")
    @RequestMapping(value = "/labrequests/{id}/sending", method = RequestMethod.PUT)
    public LabRequestRepresentation sending(UserAuthenticationToken user,
            @PathVariable Long id,
            @RequestBody LabRequestRepresentation body) {
        log.info("PUT /labrequests/" + id + "/sending");

        LabRequest labRequest = labRequestService.findOne(id);

        if (!labRequest.getStatus().equals("Approved")) {
            log.error("Action not allowed in status '" + labRequest.getStatus() + "'");
            throw new InvalidActionInStatus("Action not allowed in status '" + labRequest.getStatus() + "'");
        }

        RequestRepresentation request = new RequestRepresentation();
        HistoricProcessInstance instance = requestService.getProcessInstance(labRequest.getProcessInstanceId());
        requestFormService.transferData(instance, request, user.getUser());
        if (!request.isMaterialsRequest()) {
            log.error("Action not allowed in status '" + labRequest.getStatus() + "'. Not a materials request.");
            throw new InvalidActionInStatus("Action not allowed in status '" + labRequest.getStatus() + "'");
        }
        
        labRequestService.updateStatus(labRequest, "Sending");

        labRequest.setSendDate(new Date());
        labRequest = labRequestService.save(labRequest);

        LabRequestRepresentation representation = new LabRequestRepresentation(labRequest);
        labRequestService.transferLabRequestData(representation);
        return representation;
    }

    @PreAuthorize("isAuthenticated() and hasPermission(#id, 'isLabRequestRequester')")
    @RequestMapping(value = "/labrequests/{id}/received", method = RequestMethod.PUT)
    public LabRequestRepresentation received(UserAuthenticationToken user,
            @PathVariable Long id,
            @RequestBody LabRequestRepresentation body
            ) {
        log.info("PUT /labrequests/" + id + "/received");

        LabRequest labRequest = labRequestService.findOne(id);
        if (!labRequest.getStatus().equals("Sending")) {
            log.error("Action not allowed in status '" + labRequest.getStatus() + "'");
            throw new InvalidActionInStatus("Action not allowed in status '" + labRequest.getStatus() + "'");
        }
        
        RequestRepresentation request = new RequestRepresentation();
        HistoricProcessInstance instance = requestService.getProcessInstance(labRequest.getProcessInstanceId());
        requestFormService.transferData(instance, request, user.getUser());
        if (!request.isMaterialsRequest()) {
            log.error("Action not allowed in status '" + labRequest.getStatus() + "'. Not a materials request.");
            throw new InvalidActionInStatus("Action not allowed in status '" + labRequest.getStatus() + "'");
        }
        
        if (body.isSamplesMissing() != null && body.isSamplesMissing()) {
            if (body.getMissingSamples() == null || body.getMissingSamples().getContents().trim().isEmpty()) {
                throw new EmptyInput("Empty field 'missing samples'");
            }
            Comment comment = new Comment(labRequest.getProcessInstanceId(), user.getUser(), body.getMissingSamples().getContents());
            comment = commentRepository.save(comment);
            labRequest.addComment(comment);
            labRequest = labRequestService.save(labRequest);
        }

        labRequest = labRequestService.updateStatus(labRequest, "Received");

        LabRequestRepresentation representation = new LabRequestRepresentation(labRequest);
        labRequestService.transferLabRequestData(representation);
        return representation;
    }
    
    @PreAuthorize("isAuthenticated() and hasPermission(#id, 'isLabRequestRequester')")
    @RequestMapping(value = "/labrequests/{id}/returning", method = RequestMethod.PUT)
    public LabRequestRepresentation returning(UserAuthenticationToken user,
            @PathVariable Long id) {
        log.info("PUT /labrequests/" + id + "/returning");

        LabRequest labRequest = labRequestService.findOne(id);
        if (!labRequest.getStatus().equals("Received")) {
            log.error("Action not allowed in status '" + labRequest.getStatus() + "'");
            throw new InvalidActionInStatus("Action not allowed in status '" + labRequest.getStatus() + "'");
        }

        labRequest = labRequestService.updateStatus(labRequest, "Returning");

        LabRequestRepresentation representation = new LabRequestRepresentation(labRequest);
        labRequestService.transferLabRequestData(representation);
        return representation;
    }

    private static final Set<String> labRequestReturnedEnabledStatuses = new HashSet<String>();
    {
        labRequestReturnedEnabledStatuses.add("Sending");
        labRequestReturnedEnabledStatuses.add("Received");
        labRequestReturnedEnabledStatuses.add("Returning");
    }

    /**
     * Updates the status to 'Returned' (if the current status is in
     * {@link #labRequestReturnedEnabledStatuses}) and completes the task associated with
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
    @RequestMapping(value = "/labrequests/{id}/returned", method = RequestMethod.PUT)
    public LabRequestRepresentation returned(UserAuthenticationToken user,
            @PathVariable Long id,
            @RequestBody LabRequestRepresentation body
            ) {
        log.info("PUT /labrequests/" + id + "/returned");

        LabRequest labRequest = labRequestService.findOne(id);
        if (!labRequestReturnedEnabledStatuses.contains(labRequest.getStatus())) {
            log.error("Action not allowed in status '" + labRequest.getStatus() + "'");
            throw new InvalidActionInStatus("Action not allowed in status '" + labRequest.getStatus() + "'");
        }
        
        if (body.isSamplesMissing() != null && body.isSamplesMissing()) {
            if (body.getMissingSamples() == null || body.getMissingSamples().getContents().trim().isEmpty()) {
                log.error("Empty field 'missing samples'");
                throw new EmptyInput("Empty field 'missing samples'");
            }
            Comment comment = new Comment(labRequest.getProcessInstanceId(), user.getUser(), body.getMissingSamples().getContents());
            comment = commentRepository.save(comment);
            labRequest.addComment(comment);
            labRequest = labRequestService.save(labRequest);
        }

        labRequest = labRequestService.updateStatus(labRequest, "Returned");

        Task task = labRequestService.getTask(labRequest.getTaskId(),
                "lab_request");
        // complete task
        if (task.getDelegationState() == DelegationState.PENDING) {
            taskService.resolveTask(task.getId());
        }
        taskService.complete(task.getId());
        
        LabRequestRepresentation representation = new LabRequestRepresentation(labRequest);
        labRequestService.transferLabRequestData(representation);
        return representation;
    }

    @PreAuthorize("isAuthenticated() and ( hasPermission(#id, 'isLabRequestLabuser') or "
            + "hasPermission(#id, 'isLabRequestHubuser') )")
    @RequestMapping(value = "/labrequests/{id}/complete", method = RequestMethod.PUT)
    public LabRequestRepresentation complete(UserAuthenticationToken user,
            @PathVariable Long id,
            @RequestBody LabRequestRepresentation body
            ) {
        log.info("PUT /labrequests/" + id + "/complete");

        LabRequest labRequest = labRequestService.findOne(id);
        if (!labRequest.getStatus().equals("Approved")) {
            log.error("Action not allowed in status '" + labRequest.getStatus() + "'");
            throw new InvalidActionInStatus("Action not allowed in status '" + labRequest.getStatus() + "'");
        }

        RequestRepresentation request = new RequestRepresentation();
        HistoricProcessInstance instance = requestService.getProcessInstance(labRequest.getProcessInstanceId());
        requestFormService.transferData(instance, request, user.getUser());
        if (request.isMaterialsRequest()) {
            log.error("Action not allowed in status '" + labRequest.getStatus() + "'. Not a materials request.");
            throw new InvalidActionInStatus("Action not allowed in status '" + labRequest.getStatus() + "'");
        }

        labRequest = transferLabRequestFormData(body, labRequest, user.getUser());
        
        labRequest = labRequestService.updateStatus(labRequest, "Completed");
        
        Task task = labRequestService.getTask(labRequest.getTaskId(),
                "lab_request");

        // complete task
        if (task.getDelegationState() == DelegationState.PENDING) {
            taskService.resolveTask(task.getId());
        }
        taskService.complete(task.getId());
        
        LabRequestRepresentation representation = new LabRequestRepresentation(labRequest);
        labRequestService.transferLabRequestData(representation);
        return representation;
    }

    @PreAuthorize("isAuthenticated() and ( hasPermission(#id, 'isLabRequestLabuser') or "
            + "hasPermission(#id, 'isLabRequestHubuser') )")
    @RequestMapping(value = "/labrequests/{id}/claim", method = RequestMethod.PUT)
    public LabRequestRepresentation claim(UserAuthenticationToken user,
            @PathVariable Long id) {
        log.info("PUT /labrequests/" + id + "/claim");

        LabRequest labRequest = labRequestService.findOne(id);
        Task task = labRequestService.getTask(labRequest.getTaskId(),
                "lab_request");

        if (task.getAssignee() == null || task.getAssignee().isEmpty()) {
            taskService.claim(task.getId(), user.getId().toString());
        } else {
            taskService.delegateTask(task.getId(), user.getId().toString());
        }

        LabRequestRepresentation representation = new LabRequestRepresentation(
                labRequest);
        labRequestService.transferLabRequestData(representation);
        return representation;
    }

    @PreAuthorize("isAuthenticated() and ( hasPermission(#id, 'isLabRequestLabuser') or "
            + "hasPermission(#id, 'isLabRequestHubuser') )")
    @RequestMapping(value = "/labrequests/{id}/unclaim", method = RequestMethod.PUT)
    public LabRequestRepresentation unclaim(UserAuthenticationToken user,
            @PathVariable Long id) {
      log.info("PUT /labrequests/" + id + "/unclaim for userId " + user.getId());

      LabRequest labRequest = labRequestService.findOne(id);
      Task task = labRequestService.getTask(labRequest.getTaskId(),
        "lab_request");

      taskService.unclaim(task.getId());

      LabRequestRepresentation representation = new LabRequestRepresentation(
        labRequest);
      labRequestService.transferLabRequestData(representation);
      return representation;
    }

    @PreAuthorize("isAuthenticated() and "
      + "(hasRole('palga') "
      + " or hasPermission(#id, 'isLabRequestRequester') "
      + " or hasPermission(#id, 'isLabRequestLabuser') "
      + " or hasPermission(#id, 'isLabRequestHubuser') "
      + ")")
    @RequestMapping(value = "/labrequests/{id}/panumbers/csv", method = RequestMethod.GET)
    public HttpEntity<InputStreamResource> downloadPANumber(UserAuthenticationToken user, @PathVariable Long id) {
        log.info("GET /labrequests/" + id + "/panumbers/csv for userId " + user.getId());

        LabRequest labRequest = labRequestService.findOne(id);
        if (labRequest.getStatus().equals("Waiting for lab approval")
            || labRequest.getStatus().equals("Rejected")) {
            log.error("Download not allowed in status '" + labRequest.getStatus() + "'");
            throw new InvalidActionInStatus("Download not allowed in status '" + labRequest.getStatus() + "'");
        }
        HttpEntity<InputStreamResource> file = null;

        try {
          file =  paNumberService.writePaNumbers(labRequest.getPathologyList(), labRequest.getLab().getName());
        } catch (Exception e) {
          log.error(e.getMessage());
          throw new PaNumbersDownloadError();
        }
        return file;
    }

    static Set<String> paReportSendingStatuses;
    {
        paReportSendingStatuses = new HashSet<String>(Arrays.asList("Approved", "Sending", "Received", "Returning"));
    }

    static Set<String> setHubAssistanceStatuses;
    {
        setHubAssistanceStatuses = new HashSet<String>(Arrays.asList("Waiting for lab approval", "Approved", "Sending", "Received", "Returning"));
    }

    private LabRequest transferLabRequestFormData(LabRequestRepresentation body, LabRequest labRequest, User user) {
        RequestRepresentation request = new RequestRepresentation();
        HistoricProcessInstance instance = requestService.getProcessInstance(labRequest.getProcessInstanceId());
        requestFormService.transferData(instance, request, user);
        if (request.isPaReportRequest()) {
            if (paReportSendingStatuses.contains(labRequest.getStatus())) {
                labRequest.setPaReportsSent(body.isPaReportsSent());
                log.debug("Updating PA reports sent: " + Boolean.toString(labRequest.isPaReportsSent()));
                labRequest = labRequestService.save(labRequest);
            }
        }
        if (setHubAssistanceStatuses.contains(labRequest.getStatus())) {
            labRequest.setHubAssistanceRequested(Boolean.TRUE.equals(body.isHubAssistanceRequested()));
            log.debug("Updating hub assistance: " + Boolean.toString(labRequest.isHubAssistanceRequested()));
            labRequest = labRequestService.save(labRequest);
        }
        return labRequest;
    }

    @PreAuthorize("isAuthenticated() and hasPermission(#id, 'labRequestAssignedToUser')")
    @RequestMapping (value = "/labrequests/{id}", method = RequestMethod.PUT)
    public LabRequestRepresentation update (UserAuthenticationToken user,
            @PathVariable Long id,
            @RequestBody LabRequestRepresentation body) {
        log.info("PUT /labrequests/" + id + " for userId " + user.getId());
        LabRequest labRequest = labRequestService.findOne(id);
        
        transferLabRequestFormData(body, labRequest, user.getUser());
        
        LabRequestRepresentation representation = new LabRequestRepresentation(labRequest);
        labRequestService.transferLabRequestData(representation);
        return representation;
    }

    @PreAuthorize("isAuthenticated() and hasPermission(#id, 'labRequestAssignedToUser')")
    @RequestMapping (value = "/labrequests/{id}/pathology", method = RequestMethod.POST)
    public PathologyRepresentation addPathology (UserAuthenticationToken user,
            @PathVariable Long id,
            @RequestBody PathologyRepresentation body) {
        log.info("POST /labrequests/" + id + "/pathology for userId " + user.getId());
        LabRequest labRequest = labRequestService.findOne(id);
        
        PathologyItem pathology = new PathologyItem();
        pathology.setLabRequestId(id);
        pathology.setPaNumber(body.getPaNumber());
        pathology.setSamples(body.getSamples());
        pathology = pathologyItemRepository.save(pathology);
        
        labRequest.getPathologyList().add(pathology);
        labRequest = labRequestService.save(labRequest);
        
        PathologyRepresentation result = new PathologyRepresentation(pathology);
        result.mapSamples(pathology);
        return result;
    }

    @PreAuthorize("isAuthenticated() and hasPermission(#id, 'labRequestAssignedToUser')")
    @RequestMapping (value = "/labrequests/{id}/pathology/{pathologyId}", method = RequestMethod.DELETE)
    public void removePathology (UserAuthenticationToken user,
            @PathVariable Long id,
            @PathVariable Long pathologyId) {
        log.info("PUT /labrequests/" + id + "/pathology/ " + pathologyId + " for userId " + user.getId());
        LabRequest labRequest = labRequestService.findOne(id);

        PathologyItem pathology = pathologyItemRepository.findOne(pathologyId);
        if (pathology == null) {
            throw new PathologyNotFound();
        }
        if (!labRequest.getPathologyList().remove(pathology)) {
            throw new PathologyNotFound();
        }
        labRequest = labRequestService.save(labRequest);

        pathologyItemRepository.delete(pathologyId);
    }

    @PreAuthorize("isAuthenticated() and hasPermission(#id, 'labRequestAssignedToUser')")
    @RequestMapping (value = "/labrequests/{id}/pathology/{pathologyId}", method = RequestMethod.PUT)
    public PathologyRepresentation updatePathology (UserAuthenticationToken user,
            @PathVariable Long id,
            @PathVariable Long pathologyId,
            @RequestBody PathologyRepresentation body) {
        log.info("PUT /labrequests/" + id + "/pathology/ " + pathologyId + " for userId " + user.getId());
        LabRequest labRequest = labRequestService.findOne(id);
        
        PathologyItem pathology = pathologyItemRepository.findOne(pathologyId);
        if (pathology == null) {
            throw new PathologyNotFound();
        }
        if (!labRequest.getPathologyList().contains(pathology)) {
            throw new PathologyNotFound();
        }
        
        pathology.setSamples(body.getSamples());
        pathology = pathologyItemRepository.save(pathology);
        
        PathologyRepresentation result = new PathologyRepresentation(pathology);
        result.mapSamples(pathology);
        return result;
    }

}
