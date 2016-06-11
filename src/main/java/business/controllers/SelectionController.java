/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business.controllers;

import java.io.IOException;
import java.io.InputStream;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import business.exceptions.ExcerptListNotFound;
import business.exceptions.ExcerptListUploadError;
import business.exceptions.InvalidActionInStatus;
import business.exceptions.RequestNotFound;
import business.models.ExcerptEntry;
import business.models.ExcerptList;
import business.models.File;
import business.models.RequestProperties;
import business.models.User;
import business.representation.ExcerptEntryRepresentation;
import business.representation.ExcerptListRepresentation;
import business.representation.RequestRepresentation;
import business.representation.RequestStatus;
import business.security.UserAuthenticationToken;
import business.services.ExcerptListService;
import business.services.FileService;
import business.services.RequestFormService;
import business.services.RequestPropertiesService;
import business.services.RequestService;
import business.services.UserService;

@RestController
public class SelectionController {

    Log log = LogFactory.getLog(getClass());

    @Autowired
    private ExcerptListService excerptListService;

    @Autowired
    private RequestPropertiesService requestPropertiesService;

    @Autowired
    private RequestService requestService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private RequestFormService requestFormService;

    @Autowired
    private FileService fileService;

    @Autowired
    private UserService userService;

    @PreAuthorize("isAuthenticated() and "
            + "hasRole('requester') and "
            + "hasPermission(#id, 'isRequester')")
    @RequestMapping(value = "/requests/{id}/selection", method = RequestMethod.GET)
    public ExcerptListRepresentation getSelection(
            UserAuthenticationToken user,
            @PathVariable String id) {
        log.info("GET /requests/" + id + "/selection");
        ExcerptList excerptList = excerptListService.findByProcessInstanceId(id);
        if (excerptList == null) {
            throw new RequestNotFound();
        }
        return excerptListService.findRepresentationByProcessInstanceId(id);
    }

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
        return excerptListService.updateExcerptListSelection(id, body);
    }

    @PreAuthorize("isAuthenticated() and "
            + "hasRole('requester') and "
            + "hasPermission(#requestId, 'isRequester')")
    @RequestMapping(value = "/requests/{requestId}/excerpts/{id}/selection", method = RequestMethod.PUT)
    public ExcerptEntryRepresentation updateExcerptSelection(
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
        excerptList = excerptListService.save(excerptList);

        return new ExcerptEntryRepresentation(excerptEntry);
    }
    
    @PreAuthorize("isAuthenticated() and "
            + "hasRole('requester') and "
            + "hasPermission(#id, 'isRequester')")
    @RequestMapping(value = "/requests/{id}/selection/csv", method = RequestMethod.POST)
    public Integer uploadExcerptSelection(
            UserAuthenticationToken user, 
            @PathVariable String id,
            @RequestParam("flowFilename") String name,
            @RequestParam("flowTotalChunks") Integer chunks,
            @RequestParam("flowChunkNumber") Integer chunk,
            @RequestParam("flowIdentifier") String flowIdentifier,
            @RequestParam("file") MultipartFile file) {
        log.info("POST /requests/" + id + "/selection/csv: chunk " + chunk + " / " + chunks);

        Task task = requestService.getTaskByRequestId(id, "data_delivery");

        Integer selectedCount = 0;

        File attachment = fileService.uploadPart(user.getUser(), name, File.AttachmentType.EXCERPT_SELECTION, file, chunk, chunks, flowIdentifier);
        if (attachment != null) {
        
            // process list
            try {
                InputStream input = fileService.getInputStream(attachment);
                List<Integer> selected = excerptListService.processExcerptSelection(input);
                selectedCount = selected.size();
                try {
                    input.close();
                } catch (IOException e) {
                    log.error("Error while closing input stream: " + e.getMessage());
                }
                // if not exception thrown, save selection
                ExcerptList excerptList = excerptListService.findByProcessInstanceId(id);
                excerptList.deselectAll();
                log.info("Saving selection.");
                for(Integer number: selected) {
                    ExcerptEntry entry = excerptList.getEntries().get(number - 1);
                    if (entry == null) {
                        log.warn("Null entry in selection (for number '" + number + "').");
                    } else if (!number.equals(entry.getSequenceNumber())) {
                        log.error("Excerpt list " + excerptList.getId() + " is inconsistent: "
                                + "entry with sequence number " + entry.getSequenceNumber() 
                                + " at index " + number);
                        throw new ExcerptListUploadError("Excerpt list is inconsistent.");
                    } else { 
                        entry.setSelected(true);
                    }
                }
                excerptListService.save(excerptList);
                log.info("Done.");
            } catch (RuntimeException e) {
                // revert uploading
                fileService.removeAttachment(attachment);
                throw e;
            }
        }
        return selectedCount;
    }

    private static final Set<RequestStatus> excerptSelectionStatuses = new HashSet<>();
    {
        excerptSelectionStatuses.add(RequestStatus.DATA_DELIVERY);
        excerptSelectionStatuses.add(RequestStatus.SELECTION_REVIEW);
        excerptSelectionStatuses.add(RequestStatus.LAB_REQUEST);
    }

    @PreAuthorize("isAuthenticated() and "
            + "(hasPermission(#id, 'isPalgaUser') "
            + " or hasPermission(#id, 'isRequester') "
            + ")")
    @RequestMapping(value = "/requests/{id}/selection/csv", method = RequestMethod.GET)
    public HttpEntity<InputStreamResource> downloadExcerptSelection(UserAuthenticationToken user, @PathVariable String id) {
        log.info("GET /requests/" + id + "/selection/csv");
        HistoricProcessInstance instance = requestService.getProcessInstance(id);
        RequestRepresentation request = new RequestRepresentation();
        requestFormService.transferData(instance, request, user.getUser());
        if (!excerptSelectionStatuses.contains(request.getStatus())) {
            throw new InvalidActionInStatus();
        }
        ExcerptList excerptList = excerptListService.findByProcessInstanceId(id);
        if (excerptList == null) {
            throw new ExcerptListNotFound();
        }
        return excerptListService.writeExcerptList(excerptList, /* selectedOnly = */ true);
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
        
        // FIXME: check if request type is pa reports or materials
        
        ExcerptList excerptList = excerptListService.findByProcessInstanceId(id);
        if (excerptList == null) {
            throw new RequestNotFound();
        }
        excerptList.setRemark(body.getExcerptListRemark());
        excerptList = excerptListService.save(excerptList);
        
        Task task = requestService.getTaskByRequestId(id, "data_delivery");
        User palgaUser = null;
        try {
            Long palgaUserId = Long.valueOf(task.getAssignee());
            palgaUser = userService.findOne(palgaUserId);
        } catch (NumberFormatException e) {
            ///
        }
        
        if (task.getDelegationState()==DelegationState.PENDING) {
            taskService.resolveTask(task.getId());
        }
        taskService.complete(task.getId());

        // claim next task
        if (palgaUser != null) {
            requestService.claimCurrentPalgaTask(id, palgaUser);
        }
        
        HistoricProcessInstance instance = requestService.getProcessInstance(id);
        RequestRepresentation updatedRequest = new RequestRepresentation();
        requestFormService.transferData(instance, updatedRequest, user.getUser());
        return updatedRequest;
    }
    
    @PreAuthorize("isAuthenticated() and "
            + "hasRole('palga') and "
            + "hasPermission(#id, 'requestAssignedToUser')")
    @RequestMapping(value = "/requests/{id}/excerptSelectionApproval", method = RequestMethod.PUT)
    public RequestRepresentation setExcerptSelectionApproval(
            UserAuthenticationToken user,
            @PathVariable String id,
            @RequestBody RequestRepresentation body) {
        log.info("PUT /requests/" + id + "/excerptSelectionApproval");
        
        excerptListService.setExcerptSelectionApproval(id, body);

        HistoricProcessInstance instance = requestService.getProcessInstance(id);
        RequestRepresentation updatedRequest = new RequestRepresentation();
        requestFormService.transferData(instance, updatedRequest, user.getUser());
        return updatedRequest;
    }

    @PreAuthorize("isAuthenticated() and "
            + "hasRole('palga') and "
            + "hasPermission(#id, 'requestAssignedToUser')")
    @RequestMapping(value = "/requests/{id}/selectAll", method = RequestMethod.PUT)
    public RequestRepresentation selectAll(
            UserAuthenticationToken user,
            @PathVariable String id) {
        log.info("PUT /requests/" + id + "/selectAll");
        
        HistoricProcessInstance instance = requestService.getProcessInstance(id);
        RequestRepresentation request = new RequestRepresentation();
        requestFormService.transferData(instance, request, user.getUser());
        
        if (request.isPaReportRequest() || request.isMaterialsRequest()) {
        
            // set lab numbers for creating lab requests.
            ExcerptList excerptList = excerptListService.findByProcessInstanceId(id);
            if (excerptList == null) {
                throw new RequestNotFound();
            }
            excerptList.selectAll();
            excerptList = excerptListService.save(excerptList);
            
            Task task = requestService.getTaskByRequestId(id, "data_delivery");
            if (task.getDelegationState()==DelegationState.PENDING) {
                taskService.resolveTask(task.getId());
            }
            taskService.complete(task.getId());
            
            requestService.claimCurrentPalgaTask(id, user.getUser());
            
            instance = requestService.getProcessInstance(id);
            request = new RequestRepresentation();
            requestFormService.transferData(instance, request, user.getUser());
        }
        return request;
    }
    
}
