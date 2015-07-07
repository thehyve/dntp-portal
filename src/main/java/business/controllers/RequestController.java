package business.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Attachment;
import org.activiti.engine.task.DelegationState;
import org.activiti.engine.task.Task;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import business.exceptions.AttachmentNotFound;
import business.exceptions.ExcerptListNotFound;
import business.exceptions.FileUploadError;
import business.exceptions.InvalidActionInStatus;
import business.exceptions.NotLoggedInException;
import business.exceptions.RequestNotAdmissible;
import business.exceptions.RequestNotFound;
import business.exceptions.TaskNotFound;
import business.models.CommentRepository;
import business.models.ExcerptList;
import business.models.ExcerptListRepository;
import business.models.LabRequest;
import business.models.RequestProperties;
import business.models.RoleRepository;
import business.models.UserRepository;
import business.representation.LabRequestRepresentation;
import business.representation.RequestListRepresentation;
import business.representation.RequestRepresentation;
import business.security.UserAuthenticationToken;
import business.services.ExcerptListService;
import business.services.LabRequestService;
import business.services.MailService;
import business.services.RequestFormService;
import business.services.RequestPropertiesService;
import business.services.RequestService;

@RestController
public class RequestController {

    Log log = LogFactory.getLog(getClass());

    @Autowired
    private RequestService requestService;

    @Autowired
    private RequestFormService requestFormService;
   
    @Autowired
    private ExcerptListService excerptListService;

    @Autowired
    private MailService mailService;
    
    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private IdentityService identityService;

    @Autowired
    private FormService formService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RequestPropertiesService requestPropertiesService;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private RequestComparator requestComparator;

    @Autowired
    private ExcerptListRepository excerptListRepository;
    
    @Autowired
    private LabRequestService labRequestService;

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "requests", method = RequestMethod.GET)
    public List<RequestListRepresentation> getRequestList(UserAuthenticationToken user) {
        log.info(
                "GET /requests/ (for user: " + (user == null ? "null" : user.getId()) + ")");

        List<HistoricProcessInstance> processInstances;

        if (user == null) {
            processInstances = new ArrayList<HistoricProcessInstance>();
        } else if (user.getUser().isPalga()) {
            processInstances = historyService
                    .createHistoricProcessInstanceQuery()
                    .notDeleted()
                    .includeProcessVariables()
                    .variableValueNotEquals("status", "Open")
                    .orderByProcessInstanceStartTime()
                    .desc()
                    .list();
        } else if (user.getUser().isScientificCouncilMember()) {
            Date start = new Date();
            processInstances = historyService
                    .createHistoricProcessInstanceQuery()
                    .notDeleted()
                    .includeProcessVariables()
                    .variableValueEquals("status", "Approval")
                    .orderByProcessInstanceStartTime()
                    .desc()
                    .list();
            Date endQ1 = new Date();
            List<HistoricProcessInstance> list = historyService
                    .createHistoricProcessInstanceQuery()
                    .notDeleted()
                    .includeProcessVariables()
                    .variableValueNotEquals("status", "Approval")
                    .variableValueNotEquals("scientific_council_approved", null)
                    .orderByProcessInstanceStartTime()
                    .desc()
                    .list();
            Date endQ2 = new Date();
            if (list != null) {
                Collections.sort(list, requestComparator);
            }
            Date endSort = new Date();
            log.info("GET: query 1 took " + (endQ1.getTime() - start.getTime()) + " ms.");
            log.info("GET: query 2 took " + (endQ2.getTime() - endQ1.getTime()) + " ms.");
            log.info("GET: sorting took " + (endSort.getTime() - endQ2.getTime()) + " ms.");
            if (list != null) {
                processInstances.addAll(list);
            }
        } else if (user.getUser().isLabUser()) {
            List<LabRequestRepresentation> labRequests = labRequestService.findLabRequestsForUser(user.getUser());
            Set<String> processInstanceIds = new HashSet<String>();
            for (LabRequestRepresentation labRequest: labRequests) {
                processInstanceIds.add(labRequest.getProcessInstanceId());
            }
            if (!processInstanceIds.isEmpty()) {
                processInstances = historyService
                        .createHistoricProcessInstanceQuery()
                        .notDeleted()
                        .includeProcessVariables()
                        .processInstanceIds(processInstanceIds)
                        .orderByProcessInstanceStartTime()
                        .desc()
                        .list();
            } else {
                processInstances = new ArrayList<HistoricProcessInstance>();
            }
        } else {
            processInstances = historyService
                    .createHistoricProcessInstanceQuery()
                    .notDeleted()
                    .includeProcessVariables()
                    .involvedUser(user.getId().toString())
                    .orderByProcessInstanceStartTime()
                    .desc()
                    .list();
        }

        List<RequestListRepresentation> result = new ArrayList<RequestListRepresentation>();

        for (HistoricProcessInstance instance : processInstances) {
            RequestListRepresentation request = new RequestListRepresentation();
            requestFormService.transferData(instance, request, user.getUser());
            result.add(request);
        }
        return result;
    }

    @PreAuthorize("isAuthenticated() and hasPermission(#id, 'requestAssignedToUser')")
    @RequestMapping(value = "/requests/{id}", method = RequestMethod.GET)
    public RequestRepresentation getRequestById(UserAuthenticationToken user,
                                                @PathVariable String id) {
        log.info(
                "GET /requests/{" + id + "} (for user: " + (user == null ? "null" : user.getId()) + ")");
        RequestRepresentation request = new RequestRepresentation();
        if (user == null) {
            throw new NotLoggedInException();
        } else {
            HistoricProcessInstance instance = requestService.getProcessInstance(id);
            requestFormService.transferData(instance, request, user.getUser());
        }

        return request;
    }

    @PreAuthorize("isAuthenticated() and hasRole('requester')")
    @RequestMapping(value = "/requests", method = RequestMethod.POST)
    public RequestRepresentation start(
            UserAuthenticationToken user,
            @RequestBody RequestRepresentation req) {
        if (user == null) {
            throw new NotLoggedInException();
        } else {
            String userId = user.getId().toString();
            log.info(
                    "POST /requests (initiator: " + userId + ")");
            Map<String, Object> values = new HashMap<String, Object>();
            values.put("initiator", userId);

            ProcessInstance newInstance = runtimeService.startProcessInstanceByKey(
                    "dntp_request_001", values);

            HistoricProcessInstance instance = requestService.getProcessInstance(newInstance.getId());
            RequestRepresentation request = new RequestRepresentation();
            requestFormService.transferData(instance, request, null);
            return request;
        }
    }

    @PreAuthorize("isAuthenticated() and hasPermission(#id, 'requestAssignedToUser')")
    @RequestMapping(value = "/requests/{id}", method = RequestMethod.PUT)
    public RequestRepresentation update(
            UserAuthenticationToken user,
            @PathVariable String id,
            @RequestBody RequestRepresentation request) {
        log.info("PUT /requests/" + id);
        HistoricProcessInstance instance = requestService.getProcessInstance(id);
        Map<String, Object> variables = requestFormService.transferFormData(request, instance, user.getUser());

        runtimeService.setVariables(instance.getId(), variables);
        for (Entry<String, Object> entry: variables.entrySet()) {
            log.info("PUT /processes/" + id + " set " + entry.getKey() + " = " + entry.getValue());
        }
        instance = requestService.getProcessInstance(id);
        RequestRepresentation updatedRequest = new RequestRepresentation();
        requestFormService.transferData(instance, updatedRequest, user.getUser());
        return updatedRequest;
    }

    @PreAuthorize("isAuthenticated() and hasPermission(#id, 'requestAssignedToUser')")
    @RequestMapping(value = "/requests/{id}/submit", method = RequestMethod.PUT)
    public RequestRepresentation submit(
            UserAuthenticationToken user,
            @PathVariable String id,
            @RequestBody RequestRepresentation request) {
        log.info("PUT /requests/" + id + "/submit");
        HistoricProcessInstance instance = requestService.getProcessInstance(id);
        Map<String, Object> variables = requestFormService.transferFormData(request, instance, user.getUser());
        runtimeService.setVariables(instance.getId(), variables);
        for (Entry<String, Object> entry: variables.entrySet()) {
            log.info("PUT /requests/" + id + " set " + entry.getKey() + " = " + entry.getValue());
        }

        Task task = requestService.getTaskByRequestId(id, "request_form");
        if (task.getDelegationState()==DelegationState.PENDING) {
            taskService.resolveTask(task.getId());
        }

        taskService.complete(task.getId());
        instance = requestService.getProcessInstance(id);
        RequestRepresentation updatedRequest = new RequestRepresentation();
        requestFormService.transferData(instance, updatedRequest, user.getUser());
        return updatedRequest;
    }

    @PreAuthorize("isAuthenticated() and hasPermission(#id, 'requestAssignedToUser')")
    @RequestMapping(value = "/requests/{id}/submitForApproval", method = RequestMethod.PUT)
    public RequestRepresentation submitForApproval(
            UserAuthenticationToken user,
            @PathVariable String id,
            @RequestBody RequestRepresentation request) {
        log.info("PUT /requests/" + id + "/submitForApproval");
        request.setRequestAdmissible(true);
        HistoricProcessInstance instance = requestService.getProcessInstance(id);
        Map<String, Object> variables = requestFormService.transferFormData(request, instance, user.getUser());
        runtimeService.setVariables(instance.getId(), variables);

        instance = requestService.getProcessInstance(id);
        RequestRepresentation updatedRequest = new RequestRepresentation();
        requestFormService.transferData(instance, updatedRequest, user.getUser());
        
        if (updatedRequest.isRequestAdmissible()) {
            Task task = requestService.getTaskByRequestId(id, "palga_request_review");
            if (task.getDelegationState()==DelegationState.PENDING) {
                taskService.resolveTask(task.getId());
            }
            taskService.complete(task.getId());
        } else {
            throw new RequestNotAdmissible();
        }
        
        instance = requestService.getProcessInstance(id);
        updatedRequest = new RequestRepresentation();
        requestFormService.transferData(instance, updatedRequest, user.getUser());

        mailService.notifyScientificCouncil(updatedRequest);

        return updatedRequest;
    }

    @PreAuthorize("isAuthenticated() and hasPermission(#id, 'requestAssignedToUser')")
    @RequestMapping(value = "/requests/{id}/finalise", method = RequestMethod.PUT)
    public RequestRepresentation finalise(
            UserAuthenticationToken user,
            @PathVariable String id,
            @RequestBody RequestRepresentation request) {
        log.info("PUT /requests/" + id + "/finalise");
        HistoricProcessInstance instance = requestService.getProcessInstance(id);
        Map<String, Object> variables = requestFormService.transferFormData(request, instance, user.getUser());
        runtimeService.setVariables(instance.getId(), variables);

        instance = requestService.getProcessInstance(id);
        RequestRepresentation updatedRequest = new RequestRepresentation();
        requestFormService.transferData(instance, updatedRequest, user.getUser());
        if (updatedRequest.isPrivacyCommitteeApproved() && 
                updatedRequest.isScientificCouncilApproved()) {
            // marking request as approved
            updatedRequest.setRequestApproved(true);
            variables = requestFormService.transferFormData(updatedRequest, instance, user.getUser());
            runtimeService.setVariables(instance.getId(), variables);
            
            Boolean requestApproved = runtimeService.getVariable(id, "request_approved", Boolean.class);
            log.info("Request approved: " + requestApproved);
        
            log.info("Fetching scientific_council_approval task");
            Task councilTask = requestService.getTaskByRequestId(id, "scientific_council_approval");
            if (councilTask.getDelegationState()==DelegationState.PENDING) {
                taskService.resolveTask(councilTask.getId());
            }
            taskService.complete(councilTask.getId());
    
            log.info("Fetching request_approval task");
            Task palgaTask = requestService.getTaskByRequestId(id, "request_approval");
            if (palgaTask.getDelegationState()==DelegationState.PENDING) {
                taskService.resolveTask(palgaTask.getId());
            }
            taskService.complete(palgaTask.getId());
        } else {
            log.warn("Finalisation failed because of lacking approval.");
        }

        instance = requestService.getProcessInstance(id);
        updatedRequest = new RequestRepresentation();
        requestFormService.transferData(instance, updatedRequest, user.getUser());

        return updatedRequest;
    }

    @PreAuthorize("isAuthenticated() and hasPermission(#id, 'requestAssignedToUser')")
    @RequestMapping(value = "/requests/{id}/reject", method = RequestMethod.PUT)
    public RequestRepresentation reject(
            UserAuthenticationToken user,
            @PathVariable String id,
            @RequestBody RequestRepresentation request) {
        log.info("PUT /requests/" + id + "/reject");
        HistoricProcessInstance instance = requestService.getProcessInstance(id);

        if (request.getStatus().equals("Review")) {
            request.setRequestAdmissible(false);
        } else if (request.getStatus().equals("Approval")) {
            request.setRequestApproved(false);
        }
        request.setRejectDate(new Date());
        Map<String, Object> variables = requestFormService.transferFormData(request, instance, user.getUser());
        runtimeService.setVariables(instance.getId(), variables);

        instance = requestService.getProcessInstance(id);
        RequestRepresentation updatedRequest = new RequestRepresentation();
        requestFormService.transferData(instance, updatedRequest, user.getUser());

        log.info("Reject request.");
        log.info("Reject reason: " + updatedRequest.getRejectReason());
    
        if (updatedRequest.getStatus().equals("Review")) {
            log.info("Fetching palga_request_review task");
            Task palgaTask = requestService.getTaskByRequestId(id, "palga_request_review");
            if (palgaTask.getDelegationState()==DelegationState.PENDING) {
                taskService.resolveTask(palgaTask.getId());
            }
            taskService.complete(palgaTask.getId());

        } else if (updatedRequest.getStatus().equals("Approval")) {
            log.info("Fetching scientific_council_approval task");
            Task councilTask = requestService.getTaskByRequestId(id, "scientific_council_approval");
            if (councilTask.getDelegationState()==DelegationState.PENDING) {
                taskService.resolveTask(councilTask.getId());
            }
            taskService.complete(councilTask.getId());
    
            log.info("Fetching request_approval task");
            Task palgaTask = requestService.getTaskByRequestId(id, "request_approval");
            if (palgaTask.getDelegationState()==DelegationState.PENDING) {
                taskService.resolveTask(palgaTask.getId());
            }
            taskService.complete(palgaTask.getId());
        }

        instance = requestService.getProcessInstance(id);
        updatedRequest = new RequestRepresentation();
        requestFormService.transferData(instance, updatedRequest, user.getUser());

        return updatedRequest;
    }
    
    @PreAuthorize("isAuthenticated() and hasPermission(#id, 'requestAssignedToUser')")
    @RequestMapping(value = "/requests/{id}/close", method = RequestMethod.PUT)
    public RequestRepresentation close(
            UserAuthenticationToken user,
            @PathVariable String id,
            @RequestBody RequestRepresentation request) {
        log.info("PUT /requests/" + id + "/close");
        HistoricProcessInstance instance = requestService.getProcessInstance(id);
        Map<String, Object> variables = requestFormService.transferFormData(request, instance, user.getUser());
        runtimeService.setVariables(instance.getId(), variables);

        Task task = requestService.getTaskByRequestId(id, "data_delivery");
        if (task.getDelegationState()==DelegationState.PENDING) {
            taskService.resolveTask(task.getId());
        }
        taskService.complete(task.getId());
        instance = requestService.getProcessInstance(id);
        RequestRepresentation updatedRequest = new RequestRepresentation();
        requestFormService.transferData(instance, updatedRequest, user.getUser());

        return updatedRequest;
    }
    
    @PreAuthorize("isAuthenticated() and hasPermission(#id, 'isPalgaUser')")
    @RequestMapping(value = "/requests/{id}/claim", method = RequestMethod.PUT)
    public RequestRepresentation claim(
            UserAuthenticationToken user,
            @PathVariable String id,
            @RequestBody RequestRepresentation request) {
        log.info("PUT /requests/" + id + "/claim");
        HistoricProcessInstance instance = requestService.getProcessInstance(id);
        Task task = requestService.getCurrentPalgaTaskByRequestId(id);
        if (task.getAssignee() == null || task.getAssignee().isEmpty()) {
            taskService.claim(task.getId(), user.getId().toString());
        } else {
            taskService.delegateTask(task.getId(), user.getId().toString());
        }
        Map<String, Object> variables = instance.getProcessVariables();
        if (variables != null) {
            variables.put("assigned_date", new Date());
        }
        runtimeService.setVariables(instance.getId(), variables);
        instance = requestService.getProcessInstance(id);
        HistoricProcessInstance singleResult = historyService.createHistoricProcessInstanceQuery().processInstanceId(id).singleResult();
        //singleResult.
        RequestRepresentation updatedRequest = new RequestRepresentation();
        requestFormService.transferData(instance, updatedRequest, user.getUser());
        return updatedRequest;
    }

    @PreAuthorize("isAuthenticated() and hasPermission(#id, 'isPalgaUser')")
    @RequestMapping(value = "/requests/{id}/unclaim", method = RequestMethod.PUT)
    public RequestRepresentation unclaim(
            UserAuthenticationToken user,
            @PathVariable String id,
            @RequestBody RequestRepresentation request) {
        log.info("PUT /requests/" + id + "/unclaim");
        HistoricProcessInstance instance = requestService.getProcessInstance(id);
        Task task = requestService.getCurrentPalgaTaskByRequestId(id);
        taskService.unclaim(task.getId());
        instance = requestService.getProcessInstance(id);
        RequestRepresentation updatedRequest = new RequestRepresentation();
        requestFormService.transferData(instance, updatedRequest, user.getUser());
        return updatedRequest;
    }

    @PreAuthorize("isAuthenticated() and hasPermission(#id, 'isRequester')")
    @RequestMapping(value = "/requests/{id}", method = RequestMethod.DELETE)
    public void remove(
            UserAuthenticationToken user,
            @PathVariable String id) {
        log.info("DELETE /requests/" + id);
        HistoricProcessInstance instance = requestService.getProcessInstance(id);
        RequestRepresentation request = new RequestRepresentation();
        requestFormService.transferData(instance, request, user.getUser());
        if (!request.getRequesterId().equals(user.getUser().getId().toString())) {
            throw new RequestNotFound();
        }
        if (!request.getStatus().equals("Open")) {
            throw new InvalidActionInStatus();
        }
        log.info("deleting process instance " + id);
        runtimeService.deleteProcessInstance(id, "Removed by user: " + user.getName());
        log.info("process instance " + id + " deleted.");
    }

    @PreAuthorize("isAuthenticated() and hasPermission(#id, 'isRequester')")
    @RequestMapping(value = "/requests/{id}/files", method = RequestMethod.POST)
    public RequestRepresentation uploadFile(UserAuthenticationToken user, @PathVariable String id,
            @RequestParam("flowFilename") String name,
            @RequestParam("file") MultipartFile file) {
        log.info("POST /requests/" + id + "/files");
        Task task = requestService.getTaskByRequestId(id, "request_form");
        try{
            Attachment result = taskService.createAttachment(
                    file.getContentType(),
                    task.getId(), task.getProcessInstanceId(),
                    name, name, file.getInputStream());
            log.info("Uploaded file: " + result.getId());
        } catch(IOException e) {
            throw new FileUploadError();
        }
        HistoricProcessInstance instance = requestService.getProcessInstance(id);
        RequestRepresentation request = new RequestRepresentation();
        requestFormService.transferData(instance, request, user.getUser());
        return request;
    }

    @PreAuthorize("isAuthenticated() and hasPermission(#id, 'isRequester')")
    @RequestMapping(value = "/requests/{id}/files/{attachmentId}", method = RequestMethod.DELETE)
    public void deleteFile(UserAuthenticationToken user, @PathVariable String id,
            @PathVariable String attachmentId) {
        log.info("DELETE /requests/" + id + "/files/" + attachmentId);
        Task task = requestService.getTaskByRequestId(id, "request_form");
        Attachment result = taskService.getAttachment(attachmentId);
        if (!result.getTaskId().equals(task.getId())) {
            // not associated with current task
            throw new TaskNotFound();
        }
        HistoricProcessInstance instance = requestService.getProcessInstance(id);
        RequestRepresentation request = new RequestRepresentation();
        requestFormService.transferData(instance, request, user.getUser());
        log.info("Status: " + request.getStatus());
        if (!request.getStatus().equals("Open")) {
            throw new InvalidActionInStatus();
        }
        taskService.deleteAttachment(attachmentId);
    }    
    
    @PreAuthorize("isAuthenticated() and hasRole('palga') and hasPermission(#id, 'requestAssignedToUser')")
    @RequestMapping(value = "/requests/{id}/agreementFiles", method = RequestMethod.POST)
    public RequestRepresentation uploadAgreementAttachment(UserAuthenticationToken user, @PathVariable String id,
            @RequestParam("flowFilename") String name,
            @RequestParam("file") MultipartFile file) {
        log.info("POST /requests/" + id + "/agreementFiles");
        Task task = requestService.getTaskByRequestId(id, "palga_request_review");
        String attachmentId;
        try{
            Attachment result = taskService.createAttachment(
                    file.getContentType(),
                    task.getId(), task.getProcessInstanceId(),
                    name, name, file.getInputStream());
            attachmentId = result.getId();
            log.info("Uploaded file: " + attachmentId);
        } catch(IOException e) {
            throw new FileUploadError();
        }
        HistoricProcessInstance instance = requestService.getProcessInstance(id);
        RequestRepresentation request = new RequestRepresentation();
        requestFormService.transferData(instance, request, user.getUser());

        // add attachment id to the set of ids of the agreement attachments.
        RequestProperties properties = requestPropertiesService.findByProcessInstanceId(id);
        if (properties == null) {
            properties = new RequestProperties();
            properties.setProcessInstanceId(id);
        }
        properties.getAgreementAttachmentIds().add(attachmentId);
        requestPropertiesService.save(properties);

        instance = requestService.getProcessInstance(id);
        request = new RequestRepresentation();
        requestFormService.transferData(instance, request, user.getUser());
        return request;
    }
    
    @PreAuthorize("isAuthenticated() and hasRole('palga') and hasPermission(#id, 'requestAssignedToUser')")
    @RequestMapping(value = "/requests/{id}/agreementFiles/{attachmentId}", method = RequestMethod.DELETE)
    public RequestRepresentation removeAgreementAttachment(UserAuthenticationToken user, @PathVariable String id,
            @PathVariable String attachmentId) {
        log.info("DELETE /requests/" + id + "/agreementFiles/" + attachmentId);

        HistoricProcessInstance instance = requestService.getProcessInstance(id);

        // remove existing agreement.
        RequestProperties properties = requestPropertiesService.findByProcessInstanceId(id);
        if (properties != null && properties.getAgreementAttachmentIds().contains(attachmentId)) {
            taskService.deleteAttachment(attachmentId);
            properties.getAgreementAttachmentIds().remove(attachmentId);
            requestPropertiesService.save(properties);
        }

        instance = requestService.getProcessInstance(id);
        RequestRepresentation request = new RequestRepresentation();
        requestFormService.transferData(instance, request, user.getUser());
        return request;
    }

    @PreAuthorize("isAuthenticated() and hasRole('palga') and hasPermission(#id, 'requestAssignedToUser')")
    @RequestMapping(value = "/requests/{id}/dataFiles", method = RequestMethod.POST)
    public RequestRepresentation uploadDataAttachment(UserAuthenticationToken user, @PathVariable String id,
            @RequestParam("flowFilename") String name,
            @RequestParam("file") MultipartFile file) {
        log.info("POST /requests/" + id + "/dataFiles");
        Task task = requestService.getTaskByRequestId(id, "data_delivery");
        String attachmentId;
        try{
            Attachment result = taskService.createAttachment(
                    file.getContentType(),
                    task.getId(), task.getProcessInstanceId(),
                    name, name, file.getInputStream());
            attachmentId = result.getId();
        } catch(IOException e) {
            throw new FileUploadError();
        }
        HistoricProcessInstance instance = requestService.getProcessInstance(id);
        RequestRepresentation request = new RequestRepresentation();
        requestFormService.transferData(instance, request, user.getUser());

        // add attachment id to the set of ids of the agreement attachments.
        RequestProperties properties = requestPropertiesService.findByProcessInstanceId(id);
        properties.getDataAttachmentIds().add(attachmentId);
        requestPropertiesService.save(properties);

        //Map<String, Object> variables = transferFormData(request, instance, user.getUser());
        //runtimeService.setVariables(instance.getProcessInstanceId(), variables);
        instance = requestService.getProcessInstance(id);
        request = new RequestRepresentation();
        requestFormService.transferData(instance, request, user.getUser());
        return request;
    }
    
    @PreAuthorize("isAuthenticated() and hasRole('palga') and hasPermission(#id, 'requestAssignedToUser')")
    @RequestMapping(value = "/requests/{id}/dataFiles/{attachmentId}", method = RequestMethod.DELETE)
    public RequestRepresentation removeDataAttachment(UserAuthenticationToken user, @PathVariable String id,
            @PathVariable String attachmentId) {
        log.info("DELETE /requests/" + id + "/dataFiles/" + attachmentId);

        HistoricProcessInstance instance = requestService.getProcessInstance(id);

        // remove existing agreement.
        RequestProperties properties = requestPropertiesService.findByProcessInstanceId(id);
        if (properties != null && properties.getDataAttachmentIds().contains(attachmentId)) {
            taskService.deleteAttachment(attachmentId);
            properties.getDataAttachmentIds().remove(attachmentId);
            requestPropertiesService.save(properties);
        }

        instance = requestService.getProcessInstance(id);
        RequestRepresentation request = new RequestRepresentation();
        requestFormService.transferData(instance, request, user.getUser());
        return request;
    }
    
    @PreAuthorize("isAuthenticated() and hasRole('palga') and hasPermission(#id, 'requestAssignedToUser')")
    @RequestMapping(value = "/requests/{id}/excerptList", method = RequestMethod.POST)
    public RequestRepresentation uploadExcerptList(UserAuthenticationToken user, @PathVariable String id,
            @RequestParam("flowFilename") String name,
            @RequestParam("file") MultipartFile file) {
        log.info("POST /requests/" + id + "/excerptList");
        Task task = requestService.getTaskByRequestId(id, "data_delivery");
        String attachmentId;
        try{
            Attachment result = taskService.createAttachment(
                    file.getContentType(),
                    task.getId(), task.getProcessInstanceId(),
                    name, name, file.getInputStream());
            attachmentId = result.getId();
        } catch(IOException e) {
            throw new FileUploadError();
        }
        HistoricProcessInstance instance = requestService.getProcessInstance(id);
        
        RequestProperties properties = requestPropertiesService.findByProcessInstanceId(id);
        if (properties == null) {
            properties = new RequestProperties();
            properties.setProcessInstanceId(id);
        }

        // process list
        try {
            excerptListService.deleteByProcessInstanceId(id);
            ExcerptList list = excerptListService.processExcerptList(file);
            // if not exception thrown, save list and attachment
            if (properties.getExcerptListAttachmentId() != null && !properties.getExcerptListAttachmentId().equals(attachmentId)) {
                log.info("Deleting attachment " + properties.getExcerptListAttachmentId());
                taskService.deleteAttachment(properties.getExcerptListAttachmentId());
            }
            
            properties.setExcerptListAttachmentId(attachmentId);
            list.setProcessInstanceId(id);
            log.info("Saving excerpt list.");
            list = excerptListService.save(list);
            log.info("Done.");
        } catch (RuntimeException e) {
            // revert uploading
            taskService.deleteAttachment(attachmentId);
            throw e;
        }
        
        instance = requestService.getProcessInstance(id);
        RequestRepresentation request = new RequestRepresentation();
        requestFormService.transferData(instance, request, user.getUser());
        return request;
    }

    @PreAuthorize("isAuthenticated() and (hasPermission(#id, 'isPalgaUser') or hasPermission(#id, 'isRequester'))")
    @RequestMapping(value = "/requests/{id}/excerptList", method = RequestMethod.GET)
    public ExcerptList getExcerptList(UserAuthenticationToken user, @PathVariable String id) {
        log.info("GET /requests/" + id + "/excerptList");
        Task task = requestService.getTaskByRequestId(id, "data_delivery");
        HistoricProcessInstance instance = requestService.getProcessInstance(id);
        RequestRepresentation request = new RequestRepresentation();
        requestFormService.transferData(instance, request, user.getUser());

        ExcerptList excerptList = excerptListRepository.findByProcessInstanceId(id);
        if (excerptList == null) {
            throw new ExcerptListNotFound();
        }
        log.info("entries: " + excerptList.getEntries().size());
        return excerptList;
    }

    private static final Set<String> excerptListStatuses = new HashSet<String>();
    {
        excerptListStatuses.add("DataDelivery");
        excerptListStatuses.add("LabRequest");
        excerptListStatuses.add("Closed");
    }
    
    @PreAuthorize("isAuthenticated() and "
            + "(hasPermission(#id, 'isPalgaUser') "
            + " or hasPermission(#id, 'isRequester') "
            //+ " or hasPermission(#id, 'isLabuser') "
            + ")")
    @RequestMapping(value = "/requests/{id}/excerptList/csv", method = RequestMethod.GET)
    public HttpEntity<InputStreamResource> downloadExcerptList(UserAuthenticationToken user, @PathVariable String id) {
        log.info("GET /requests/" + id + "/excerptList/csv");
        HistoricProcessInstance instance = requestService.getProcessInstance(id);
        RequestRepresentation request = new RequestRepresentation();
        requestFormService.transferData(instance, request, user.getUser());
        if (!excerptListStatuses.contains(request.getStatus())) {
            throw new InvalidActionInStatus();
        }
        ExcerptList excerptList = excerptListRepository.findByProcessInstanceId(id);
        if (excerptList == null) {
            throw new ExcerptListNotFound();
        }
        return excerptListService.writeExcerptList(excerptList);
    }
    
    @PreAuthorize("isAuthenticated() and "
            + "(hasPermission(#id, 'isPalgaUser') "
            + " or hasPermission(#id, 'isRequester') "
            + " or hasPermission(#id, 'isScientificCouncil') "
            + " or hasPermission(#id, 'isLabuser') "
            + ")")
    @RequestMapping(value = "/requests/{id}/files/{attachmentId}", method = RequestMethod.GET)
    public HttpEntity<InputStreamResource> getFile(UserAuthenticationToken user, @PathVariable String id,
            @PathVariable String attachmentId) {
        log.info("GET /requests/" + id + "/files/" + attachmentId);
        Attachment result = taskService.getAttachment(attachmentId);
        List<Attachment> attachments = taskService.getProcessInstanceAttachments(id);
        for (Attachment attachment: attachments) {
            if (attachment.getId().equals(result.getId())) {
                InputStream input = taskService.getAttachmentContent(attachmentId);
                InputStreamResource resource = new InputStreamResource(input);
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.valueOf(result.getType()));
                headers.set("Content-Disposition",
                        "attachment; filename=" + result.getName().replace(" ", "_"));
                HttpEntity<InputStreamResource> response =  new HttpEntity<InputStreamResource>(resource, headers);
                log.info("Returning reponse.");
                return response;
            }
        }
        throw new AttachmentNotFound();
    }

}
