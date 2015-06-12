package business.controllers;

import java.util.Date;
import java.util.List;

import org.activiti.engine.TaskService;
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

import business.models.LabRequest;
import business.models.LabRequestRepository;
import business.representation.LabRequestRepresentation;
import business.security.UserAuthenticationToken;
import business.services.LabRequestService;

@RestController
public class LabRequestController {

    Log log = LogFactory.getLog(getClass());

    @Autowired
    private LabRequestService labRequestService;

    @Autowired
    private LabRequestRepository labRequestRepository;

    @Autowired
    private TaskService taskService;

    @PreAuthorize("isAuthenticated() and ("
            + "hasRole('requester')"
            + " or "
            + "hasRole('palga')"
            + " or "
            + "hasRole('lab_user')"
            + ")")
    @RequestMapping(value = "/labrequests", method = RequestMethod.GET)
    public List<LabRequestRepresentation> getLabRequests(
            UserAuthenticationToken user) {
        log.info("GET /labrequests");
        return labRequestService.findLabRequestsForUser(user.getUser());
    }

  @PreAuthorize("isAuthenticated() and hasPermission(#id, 'labRequestAssignedToUser')")
  @RequestMapping(value = "/labrequests/{id}/reject", method = RequestMethod.PUT)
  public LabRequestRepresentation reject(
      UserAuthenticationToken user,
      @PathVariable Long id,
      @RequestBody LabRequestRepresentation body) {
    log.info("PUT /labrequests/" + id + "/reject");

    LabRequest labRequest = labRequestRepository.findOne(id);
    labRequest.setRejectReason(body.getRejectReason());
    labRequest.setRejectDate(new Date());
    taskService.setVariableLocal(labRequest.getTaskId(), "labrequest_status", "Rejected");
    Task task = labRequestService.getTask(labRequest.getTaskId(), "lab_request");
    if (task.getDelegationState() == DelegationState.PENDING) {
        taskService.resolveTask(task.getId());
    }
    taskService.complete(task.getId());

    LabRequestRepresentation representation = new LabRequestRepresentation(labRequest);
    labRequestService.transferLabRequestData(representation);
    return representation;
  }

  @PreAuthorize("isAuthenticated() and hasPermission(#id, 'labRequestAssignedToUser')")
  @RequestMapping(value = "/labrequests/{id}/accept", method = RequestMethod.PUT)
  public LabRequestRepresentation accept(UserAuthenticationToken user, @PathVariable Long id) {
    log.info("PUT /labrequests/" + id + "/accept");

    LabRequest labRequest = labRequestRepository.findOne(id);
    Task task = labRequestService.getTask(labRequest.getTaskId(), "lab_request");
    taskService.setVariableLocal(labRequest.getTaskId(), "labrequest_status", "Sending");
    
    LabRequestRepresentation representation = new LabRequestRepresentation(labRequest);
    labRequestService.transferLabRequestData(representation);
    return representation;
  }

  @PreAuthorize("isAuthenticated() and hasPermission(#id, 'isLabRequestLabuser')")
  @RequestMapping(value = "/labrequests/{id}/claim", method = RequestMethod.PUT)
  public LabRequestRepresentation claim(
      UserAuthenticationToken user,
      @PathVariable Long id) {
    log.info("PUT /labrequests/" + id + "/claim");

    LabRequest labRequest = labRequestRepository.findOne(id);
    Task task = labRequestService.getTask(labRequest.getTaskId(), "lab_request");

    if (task.getAssignee() == null || task.getAssignee().isEmpty()) {
      taskService.claim(task.getId(), user.getId().toString());
    } else {
      taskService.delegateTask(task.getId(), user.getId().toString());
    }

    LabRequestRepresentation representation = new LabRequestRepresentation(labRequest);
    labRequestService.transferLabRequestData(representation);
    return representation;
  }

  @PreAuthorize("isAuthenticated() and hasPermission(#id, 'isLabRequestLabuser')")
  @RequestMapping(value = "/labrequests/{id}/unclaim", method = RequestMethod.PUT)
  public LabRequestRepresentation unclaim(
      UserAuthenticationToken user,
      @PathVariable Long id) {
    log.info("PUT /labrequests/" + id + "/unclaim");

    LabRequest labRequest = labRequestRepository.findOne(id);
    Task task = labRequestService.getTask(labRequest.getTaskId(), "lab_request");

    taskService.unclaim(task.getId());

    LabRequestRepresentation representation = new LabRequestRepresentation(labRequest);
    labRequestService.transferLabRequestData(representation);
    return representation;
  }


  @PreAuthorize("isAuthenticated() and hasPermission(#id, 'isLabRequestLabuser')")
  @RequestMapping(value = "/labrequests/{id}/panumbers/csv", method = RequestMethod.GET)
  public HttpEntity<InputStreamResource> downloadPANumber(UserAuthenticationToken user, @PathVariable String id) {
    log.info("GET /labrequests/" + id + "/panumbers/csv");

    // TODO

    return null;
  }


}
