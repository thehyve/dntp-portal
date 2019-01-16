package business.controllers;

import business.representation.RequestRepresentation;
import business.security.UserAuthenticationToken;
import business.services.RequestExportService;
import business.services.RequestFormService;
import business.services.RequestService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class RequestExportController {

    Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private RequestService requestService;

    @Autowired
    private RequestFormService requestFormService;

    @Autowired
    private RequestExportService requestExportService;

    @Autowired
    private RequestListRepresentationComparator requestListRepresentationComparator;

    @PreAuthorize("isAuthenticated() and hasRole('palga')")
    @RequestMapping(value = "/requests/csv", method = RequestMethod.GET)
    public HttpEntity<InputStreamResource> downloadRequestList(UserAuthenticationToken user) {
        log.info("GET /requests/requests/csv");
        List<RequestRepresentation> result = new ArrayList<>();
        List<String> processInstanceIds = requestService.getProcessInstanceIdsForUser(user.getUser());
        for (String id: processInstanceIds) {
            HistoricProcessInstance instance = requestService.getProcessInstance(id);
            RequestRepresentation request = new RequestRepresentation();
            requestFormService.transferData(instance, request, user.getUser());
            result.add(request);
        }
        result.sort(requestListRepresentationComparator);
        return requestExportService.writeRequestListCsv(result);
    }

}
