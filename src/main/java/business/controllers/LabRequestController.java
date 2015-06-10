package business.controllers;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import business.representation.LabRequestRepresentation;
import business.security.UserAuthenticationToken;
import business.services.LabRequestService;

@RestController
public class LabRequestController {

    Log log = LogFactory.getLog(getClass());

    @Autowired
    private LabRequestService labRequestService;

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
  public LabRequestRepresentation reject(UserAuthenticationToken user, @PathVariable String id) {
    log.info("PUT /labrequest/" + id + "/reject" );

    // TODO -- //
      LabRequestRepresentation dummy = new LabRequestRepresentation();
      dummy.setTaskId(id);
      dummy.setStatus("Rejected");
      return dummy;
    // TODO -- //
  }

  @RequestMapping(value = "/labrequest/{id}/accept", method = RequestMethod.PUT)
  public LabRequestRepresentation accept(UserAuthenticationToken user, @PathVariable String id) {
    log.info("PUT /labrequest/" + id + "/accept" );

    // TODO -- //
      LabRequestRepresentation dummy = new LabRequestRepresentation();
      dummy.setTaskId(id);
      dummy.setStatus("In Progress");
      return dummy;
    // TODO -- //
  }

}
