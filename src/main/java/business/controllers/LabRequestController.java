package business.controllers;

import java.util.List;

import org.activiti.engine.TaskService;
import org.activiti.engine.task.Task;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
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

  @RequestMapping(value = "/labrequest/{id}/reject", method = RequestMethod.PUT)
  public LabRequestRepresentation reject(UserAuthenticationToken user, @PathVariable Long id) {
    log.info("PUT /labrequest/" + id + "/reject" );

    LabRequest labRequest = labRequestRepository.findOne(id);
    Task task = labRequestService.getTask(labRequest.getTaskId(), "lab_request");
    taskService.setVariableLocal(labRequest.getTaskId(), "labrequest_status", "Rejected");
    
    LabRequestRepresentation representation = new LabRequestRepresentation();
    labRequestService.transferLabRequestData(representation, labRequest);
    return representation;
  }

  @RequestMapping(value = "/labrequest/{id}/accept", method = RequestMethod.PUT)
  public LabRequestRepresentation accept(UserAuthenticationToken user, @PathVariable Long id) {
    log.info("PUT /labrequest/" + id + "/accept" );

    LabRequest labRequest = labRequestRepository.findOne(id);
    Task task = labRequestService.getTask(labRequest.getTaskId(), "lab_request");
    taskService.setVariableLocal(labRequest.getTaskId(), "labrequest_status", "In progress");
    
    LabRequestRepresentation representation = new LabRequestRepresentation();
    labRequestService.transferLabRequestData(representation, labRequest);
    return representation;
  }

}
