package business.controllers;

import business.security.UserAuthenticationToken;
import business.services.LabRequestExportService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LabRequestExportController {

    Log log = LogFactory.getLog(getClass());

    @Autowired
    LabRequestExportService labRequestExportService;

    @PreAuthorize("isAuthenticated() and "
            + "(hasRole('palga') "
            + " or hasPermission(#id, 'isLabRequestRequester') "
            + " or hasPermission(#id, 'isLabRequestLabuser') "
            + " or hasPermission(#id, 'isLabRequestHubuser') "
            + ")")
    @RequestMapping(value = "/labrequests/{id}/panumbers/csv", method = RequestMethod.GET)
    public HttpEntity<InputStreamResource> downloadPANumbers(UserAuthenticationToken user, @PathVariable Long id) {
        log.info("GET /labrequests/" + id + "/panumbers/csv for userId " + user.getId());

        return labRequestExportService.downloadPANumbers(id, user);
    }

    @PreAuthorize("isAuthenticated() and " +
            "(hasRole('palga') or hasRole('lab_user') or hasRole('hub_user'))")
    @RequestMapping(value = "/labrequests/panumbers/csv", method = RequestMethod.GET)
    public HttpEntity<InputStreamResource> downloadAllPANumbers(UserAuthenticationToken user) {
        log.info("GET /labrequests/panumbers/csv for userId " + user.getId());

        return labRequestExportService.downloadAllPANumbers(user);
    }

}
