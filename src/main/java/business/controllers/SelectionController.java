package business.controllers;

import java.util.Set;
import java.util.TreeSet;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.task.DelegationState;
import org.activiti.engine.task.Task;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import business.exceptions.RequestNotFound;
import business.models.ExcerptEntry;
import business.models.ExcerptList;
import business.models.ExcerptListRepository;
import business.models.RequestProperties;
import business.representation.ExcerptEntryRepresentation;
import business.representation.ExcerptListRepresentation;
import business.representation.RequestRepresentation;
import business.security.UserAuthenticationToken;
import business.services.ExcerptListService;
import business.services.LabRequestService;
import business.services.RequestFormService;
import business.services.RequestPropertiesService;
import business.services.RequestService;

@RestController
public class SelectionController {

    Log log = LogFactory.getLog(getClass());

    @Autowired
    private ExcerptListService excerptListService;
    
    @Autowired 
    ExcerptListRepository excerptListRepository;

    @Autowired
    private LabRequestService labRequestService;
    
    @Autowired
    private RequestPropertiesService requestPropertiesService;
    
    @Autowired
    private RequestService requestService;

    @Autowired
    private TaskService taskService;
    
    @Autowired
    private RuntimeService runtimeService;
    
    @Autowired
    private RequestFormService requestFormService;
    
    @PreAuthorize("isAuthenticated() and "
            + "hasRole('requester') and "
            + "hasPermission(#id, 'isRequester')")
    @RequestMapping(value = "/requests/{id}/selection", method = RequestMethod.GET)
    public ExcerptListRepresentation getSelection(
            UserAuthenticationToken user,
            @PathVariable String id) {
        log.info("GET /requests/" + id + "/selection");
        ExcerptList excerptList = excerptListRepository.findByProcessInstanceId(id);
        if (excerptList == null) {
            throw new RequestNotFound();
        }
        return new ExcerptListRepresentation(excerptList);
    }

    // not tested yet
    @PreAuthorize("isAuthenticated() and "
            + "hasRole('requester') and "
            + "hasPermission(#id, 'isRequester')")
    @RequestMapping(value = "/requests/{id}/selection", method = RequestMethod.PUT)
    public ExcerptListRepresentation updateExcerptListSelection(
            UserAuthenticationToken user,
            @PathVariable String id,
            @RequestBody ExcerptListRepresentation body) {
        log.info("PUT /requests/" + id + "/selection");
        RequestProperties properties = requestPropertiesService.findByProcessInstanceId(id);
        ExcerptList excerptList = excerptListRepository.findByProcessInstanceId(id);
        if (excerptList == null) {
            throw new RequestNotFound();
        }
        //list.getEntries().get(body.get)
        for (ExcerptEntryRepresentation entry: body.getEntries()) {
            ExcerptEntry excerptEntry = excerptList.getEntries().get(entry.getSequenceNumber());
            if (entry.getId().equals(excerptEntry.getId())) {
                // indeed the same entry
                excerptEntry.setSelected(entry.isSelected());
            }
        }
        properties = requestPropertiesService.save(properties);

        return new ExcerptListRepresentation(excerptList);
    }

    @PreAuthorize("isAuthenticated() and "
            + "hasRole('requester') and "
            + "hasPermission(#requestId, 'isRequester')")
    @RequestMapping(value = "/requests/{requestId}/excerpts/{id}/selection", method = RequestMethod.PUT)
    public ExcerptListRepresentation updateExcerptSelection(
            UserAuthenticationToken user,
            @PathVariable String requestId,
            @PathVariable String id,
            @RequestBody ExcerptEntryRepresentation body) {
        log.info("PUT /requests/" + requestId + "/excerpts/" + id + "/selection");
        ExcerptList excerptList = excerptListService.findByProcessInstanceId(requestId);
        if (excerptList == null) {
            throw new RequestNotFound();
        }
        ExcerptEntry excerptEntry = excerptList.getEntries().get(body.getSequenceNumber()-1);
        if (body.getId().equals(excerptEntry.getId())) {
            // indeed the same entry
            excerptEntry.setSelected(body.isSelected());
            log.info("set value to " + excerptEntry.isSelected());
        } else {
            log.info("Sequence number not properly set: body.id = " + body.getId() + ", excerpt.id = " + excerptEntry.getId());
            log.info("body.seqNr = " + body.getSequenceNumber() + ", excerpt.seqNr = " + excerptEntry.getSequenceNumber());
        }
        excerptList = excerptListRepository.save(excerptList);

        return new ExcerptListRepresentation(excerptList);
    }
    
    @PreAuthorize("isAuthenticated() and "
            + "hasRole('requester') and "
            + "hasPermission(#id, 'isRequester')")
    @RequestMapping(value = "/requests/{id}/submitExcerptSelection", method = RequestMethod.PUT)
    public RequestRepresentation submitExcerptSelection(
            UserAuthenticationToken user,
            @PathVariable String id,
            @RequestBody RequestRepresentation body) {
        log.info("PUT /requests/" + id + "/submitExcerptSelection");
        
        // FIXME: claim task for requester
        
        ExcerptList excerptList = excerptListRepository.findByProcessInstanceId(id);
        if (excerptList == null) {
            throw new RequestNotFound();
        }
        
        excerptList.setRemark(body.getExcerptListRemark());
        excerptList = excerptListRepository.save(excerptList);
        
        // TODO: set lab numbers for creating lab requests.
        Set<Integer> selectedLabNumbers = new TreeSet<Integer>();
        for(ExcerptEntry entry: excerptList.getEntryValues()) {
            selectedLabNumbers.add(entry.getLabNumber());
        }
        runtimeService.setVariable(id, "lab_request_labs", selectedLabNumbers);
        
        Task task = requestService.getTaskByRequestId(id, "data_delivery");
        if (task.getDelegationState()==DelegationState.PENDING) {
            taskService.resolveTask(task.getId());
        }

        taskService.complete(task.getId());
        
        // generate lab requests
        labRequestService.generateLabRequests(id);

        HistoricProcessInstance instance = requestService.getProcessInstance(id);
        RequestRepresentation updatedRequest = new RequestRepresentation();
        requestFormService.transferData(instance, updatedRequest, user.getUser());
        return updatedRequest;
    }
    
}
