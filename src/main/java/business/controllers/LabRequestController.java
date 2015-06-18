package business.controllers;

import java.util.Date;
import java.util.List;

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
import business.models.LabRequestRepository;
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
    private LabRequestRepository labRequestRepository;

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

    @PreAuthorize("isAuthenticated() and (" + "hasRole('requester')" + " or "
            + "hasRole('palga')" + " or " + "hasRole('lab_user')" + ")")
    @RequestMapping(value = "/labrequests", method = RequestMethod.GET)
    public List<LabRequestRepresentation> getLabRequests(
            UserAuthenticationToken user) {
        log.info("GET /labrequests");
        return labRequestService.findLabRequestsForUser(user.getUser());
    }

    @PreAuthorize("isAuthenticated() and "
            + "(hasRole('palga') "
            + " or hasPermission(#id, 'isLabRequestRequester') "
            + " or hasPermission(#id, 'isLabRequestLabuser') "
            + ")")
    @RequestMapping(value = "/labrequests/{id}", method = RequestMethod.GET)
    public LabRequestRepresentation getLabRequest(
            UserAuthenticationToken user,
            @PathVariable Long id) {
        log.info("GET /labrequests/" + id);
        LabRequest labRequest = labRequestRepository.findOne(id);
        LabRequestRepresentation representation = new LabRequestRepresentation(labRequest);
        labRequestService.transferLabRequestData(representation);
        labRequestService.transferLabRequestDetails(representation);
        labRequestService.transferExcerptListData(representation);
        return representation;
    }


    @PreAuthorize("isAuthenticated() and hasPermission(#id, 'labRequestAssignedToUser')")
    @RequestMapping(value = "/labrequests/{id}/reject", method = RequestMethod.PUT)
    public LabRequestRepresentation reject(UserAuthenticationToken user,
            @PathVariable Long id, @RequestBody LabRequestRepresentation body) {
        log.info("PUT /labrequests/" + id + "/reject");

        LabRequest labRequest = labRequestRepository.findOne(id);
        labRequest.setRejectReason(body.getRejectReason());
        labRequest.setRejectDate(new Date());
        taskService.setVariableLocal(labRequest.getTaskId(),
                "labrequest_status", "Rejected");
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

    @PreAuthorize("isAuthenticated() and hasPermission(#id, 'labRequestAssignedToUser')")
    @RequestMapping(value = "/labrequests/{id}/accept", method = RequestMethod.PUT)
    public LabRequestRepresentation accept(UserAuthenticationToken user,
            @PathVariable Long id) {
        log.info("PUT /labrequests/" + id + "/accept");

        LabRequest labRequest = labRequestRepository.findOne(id);
        Task task = labRequestService.getTask(labRequest.getTaskId(),
                "lab_request");
        taskService.setVariableLocal(labRequest.getTaskId(),
                "labrequest_status", "Approved");

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

        LabRequest labRequest = labRequestRepository.findOne(id);
        
        transferLabRequestFormData(body, labRequest, user.getUser());
        
        LabRequestRepresentation representation = new LabRequestRepresentation(labRequest);
        labRequestService.transferLabRequestData(representation);
        if (!representation.getStatus().equals("Approved")) {
            throw new InvalidActionInStatus("Action not allowed in status '" + representation.getStatus() + "'");
        }
        
        Task task = labRequestService.getTask(labRequest.getTaskId(),
                "lab_request");
        taskService.setVariableLocal(labRequest.getTaskId(),
                "labrequest_status", "Sending");

        representation = new LabRequestRepresentation(labRequest);
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

        LabRequest labRequest = labRequestRepository.findOne(id);
        LabRequestRepresentation representation = new LabRequestRepresentation(labRequest);
        labRequestService.transferLabRequestData(representation);
        if (!representation.getStatus().equals("Sending")) {
            throw new InvalidActionInStatus("Action not allowed in status '" + representation.getStatus() + "'");
        }
        
        RequestRepresentation request = new RequestRepresentation();
        HistoricProcessInstance instance = requestService.getProcessInstance(labRequest.getProcessInstanceId());
        requestFormService.transferData(instance, request, user.getUser());
        if (!request.isMaterialsRequest()) {
            throw new InvalidActionInStatus("Action not allowed in status '" + representation.getStatus() + "'");
        }
        
        if (body.isSamplesMissing()) {
            if (body.getMissingSamples() == null || body.getMissingSamples().getContents().trim().isEmpty()) {
                throw new EmptyInput("Empty field 'missing samples'");
            }
            Comment comment = new Comment(labRequest.getProcessInstanceId(), user.getUser(), body.getMissingSamples().getContents());
            comment = commentRepository.save(comment);
            labRequest.addComment(comment);
            labRequest = labRequestRepository.save(labRequest);
        }
        
        Task task = labRequestService.getTask(labRequest.getTaskId(),
                "lab_request");
        taskService.setVariableLocal(labRequest.getTaskId(),
                "labrequest_status", "Received");

        representation = new LabRequestRepresentation(labRequest);
        labRequestService.transferLabRequestData(representation);
        return representation;
    }
    
    @PreAuthorize("isAuthenticated() and hasPermission(#id, 'isLabRequestRequester')")
    @RequestMapping(value = "/labrequests/{id}/returning", method = RequestMethod.PUT)
    public LabRequestRepresentation returning(UserAuthenticationToken user,
            @PathVariable Long id) {
        log.info("PUT /labrequests/" + id + "/returning");

        LabRequest labRequest = labRequestRepository.findOne(id);
        LabRequestRepresentation representation = new LabRequestRepresentation(labRequest);
        labRequestService.transferLabRequestData(representation);
        if (!representation.getStatus().equals("Received")) {
            throw new InvalidActionInStatus("Action not allowed in status '" + representation.getStatus() + "'");
        }

        Task task = labRequestService.getTask(labRequest.getTaskId(),
                "lab_request");
        taskService.setVariableLocal(labRequest.getTaskId(),
                "labrequest_status", "Returning");

        representation = new LabRequestRepresentation(labRequest);
        labRequestService.transferLabRequestData(representation);
        return representation;
    }

    @PreAuthorize("isAuthenticated() and hasPermission(#id, 'isLabRequestLabuser')")
    @RequestMapping(value = "/labrequests/{id}/returned", method = RequestMethod.PUT)
    public LabRequestRepresentation returned(UserAuthenticationToken user,
            @PathVariable Long id,
            @RequestBody LabRequestRepresentation body
            ) {
        log.info("PUT /labrequests/" + id + "/returned");

        LabRequest labRequest = labRequestRepository.findOne(id);
        LabRequestRepresentation representation = new LabRequestRepresentation(labRequest);
        labRequestService.transferLabRequestData(representation);
        if (!representation.getStatus().equals("Returning")) {
            throw new InvalidActionInStatus("Action not allowed in status '" + representation.getStatus() + "'");
        }
        
        if (body.isSamplesMissing()) {
            if (body.getMissingSamples() == null || body.getMissingSamples().getContents().trim().isEmpty()) {
                throw new EmptyInput("Empty field 'missing samples'");
            }
            Comment comment = new Comment(labRequest.getProcessInstanceId(), user.getUser(), body.getMissingSamples().getContents());
            comment = commentRepository.save(comment);
            labRequest.addComment(comment);
            labRequest = labRequestRepository.save(labRequest);
        }
        
        Task task = labRequestService.getTask(labRequest.getTaskId(),
                "lab_request");
        taskService.setVariableLocal(labRequest.getTaskId(),
                "labrequest_status", "Returned");

        // complete task
        if (task.getDelegationState() == DelegationState.PENDING) {
            taskService.resolveTask(task.getId());
        }
        taskService.complete(task.getId());
        
        representation = new LabRequestRepresentation(labRequest);
        labRequestService.transferLabRequestData(representation);
        return representation;
    }
    
    @PreAuthorize("isAuthenticated() and hasPermission(#id, 'isLabRequestLabuser')")
    @RequestMapping(value = "/labrequests/{id}/claim", method = RequestMethod.PUT)
    public LabRequestRepresentation claim(UserAuthenticationToken user,
            @PathVariable Long id) {
        log.info("PUT /labrequests/" + id + "/claim");

        LabRequest labRequest = labRequestRepository.findOne(id);
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

    @PreAuthorize("isAuthenticated() and hasPermission(#id, 'isLabRequestLabuser')")
    @RequestMapping(value = "/labrequests/{id}/unclaim", method = RequestMethod.PUT)
    public LabRequestRepresentation unclaim(UserAuthenticationToken user,
            @PathVariable Long id) {
      log.info("PUT /labrequests/" + id + "/unclaim for userId " + user.getId());

      LabRequest labRequest = labRequestRepository.findOne(id);
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
      + ")")
    @RequestMapping(value = "/labrequests/{id}/panumbers/csv", method = RequestMethod.GET)
    public HttpEntity<InputStreamResource> downloadPANumber(UserAuthenticationToken user, @PathVariable Long id) {
        log.info("GET /labrequests/" + id + "/panumbers/csv for userId " + user.getId());

        LabRequest labRequest = labRequestRepository.findOne(id);
        LabRequestRepresentation representation = new LabRequestRepresentation(labRequest);
        labRequestService.transferLabRequestData(representation);

        if (representation.getStatus().equals("Waiting for lab approval")
            || representation.getStatus().equals("Rejected")) {
            throw new InvalidActionInStatus("Download not allowed in status '" + representation.getStatus() + "'");
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
    
    private void transferLabRequestFormData(LabRequestRepresentation body, LabRequest labRequest, User user) {
        RequestRepresentation request = new RequestRepresentation();
        HistoricProcessInstance instance = requestService.getProcessInstance(labRequest.getProcessInstanceId());
        requestFormService.transferData(instance, request, user);
        if (request.isPaReportRequest()) {
            labRequest.setPaReportsSent(body.isPaReportsSent());
            labRequest = labRequestRepository.save(labRequest);
        }
    }

    @PreAuthorize("isAuthenticated() and hasPermission(#id, 'labRequestAssignedToUser')")
    @RequestMapping (value = "/labrequests/{id}", method = RequestMethod.PUT)
    public LabRequestRepresentation update (UserAuthenticationToken user,
            @PathVariable Long id,
            @RequestBody LabRequestRepresentation body) {
        log.info("PUT /labrequests/" + id + " for userId " + user.getId());
        LabRequest labRequest = labRequestRepository.findOne(id);
        
        transferLabRequestFormData(body, labRequest, user.getUser());
        
        LabRequestRepresentation representation = new LabRequestRepresentation(labRequest);
        labRequestService.transferLabRequestData(representation);
        return representation;
    }
    
    @PreAuthorize("isAuthenticated() and hasPermission(#id, 'labRequestAssignedToUser')")
    @RequestMapping (value = "/labrequests/{id}/pathology/{pathologyId}", method = RequestMethod.PUT)
    public PathologyRepresentation updatePathology (UserAuthenticationToken user,
            @PathVariable Long id,
            @PathVariable Long pathologyId,
            @RequestBody PathologyRepresentation body) {
        log.info("PUT /labrequests/" + id + "/pathology/ " + pathologyId + " for userId " + user.getId());
        LabRequest labRequest = labRequestRepository.findOne(id);
        
        PathologyItem pathology = pathologyItemRepository.findOne(pathologyId);
        if (pathology == null) {
            throw new PathologyNotFound();
        }
        if (!labRequest.getPathologyList().contains(pathology)) {
            throw new PathologyNotFound();
        }
        
        pathology.setSamples(body.getSamples());
        pathology = pathologyItemRepository.save(pathology);
        
        return new PathologyRepresentation(pathology);
    }
    
}
