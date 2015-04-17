package business.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Attachment;
import org.activiti.engine.task.Task;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import business.models.RequestProperties;
import business.models.RequestPropertiesRepository;
import business.models.User;
import business.models.UserRepository;
import business.representation.AttachmentRepresentation;
import business.representation.RequestRepresentation;
import business.security.UserAuthenticationToken;

@RestController
public class RequestController {

    Log log = LogFactory.getLog(getClass());
    
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
    private UserRepository userRepository;
    
    @Autowired
    private RequestPropertiesRepository requestPropertiesRepository;

    private boolean fetchBooleanVariable(String name, Map<String,Object> variables) {
        if (variables.get(name) != null) {
            return (boolean)variables.get(name);
        }
        return false;
    }
    
    private String getName(User user) {
        if (user == null) {
            return "";
        }
        return user.getFirstName() 
                + (user.getFirstName().isEmpty() || user.getLastName() == null
                || user.getLastName().isEmpty() ? "" :" ") 
                + (user.getLastName() == null ? "" : user.getLastName());
    }
    
    private void transferData(ProcessInstance instance, RequestRepresentation request, boolean is_palga ) {
        request.setProcessInstanceId(instance.getProcessInstanceId());
        Map<String, Object> variables = instance.getProcessVariables();
        if (variables != null) {
            request.setDateCreated((Date)variables.get("date_created"));
            request.setRequesterId(variables.get("requester_id") == null ? "" : variables.get("requester_id").toString());
            Long userId = null;
            try { userId = Long.valueOf(request.getRequesterId()); }
            catch(NumberFormatException e) {}
            if (userId != null) {
                User user = userRepository.findOne(userId);
                if (user != null) {
                    request.setRequesterName(getName(user));
                    request.setLab(user.getLab());
                }
            }
            Task task = findTaskByRequestId(instance.getId());
            if (task != null) {
                request.setAssignee(task.getAssignee());
                if (task.getAssignee() != null && !task.getAssignee().isEmpty()) {
                    Long assigneeId = null;
                    try { assigneeId = Long.valueOf(task.getAssignee()); }
                    catch(NumberFormatException e) {}
                    if (assigneeId != null) {
                        User assignee = userRepository.findOne(assigneeId);
                        if (assignee != null) {
                            request.setAssigneeName(getName(assignee));
                        } 
                    } 
                }
                List<Attachment> attachments = taskService.getTaskAttachments(task.getId()); 
                List<HistoricTaskInstance> historicTasks = getHistoricTasksByRequestId(instance.getProcessInstanceId());
                for (HistoricTaskInstance historicTask: historicTasks) {
                    List<Attachment> historicAttachments = taskService.getTaskAttachments(historicTask.getId());
                    attachments.addAll(historicAttachments);
                }
                List<AttachmentRepresentation> requesterAttachments = new ArrayList<AttachmentRepresentation>();
                List<AttachmentRepresentation> agreementAttachments = new ArrayList<AttachmentRepresentation>();
                RequestProperties properties = requestPropertiesRepository.findByProcessInstanceId(
                        instance.getProcessInstanceId());
                if (properties != null) {
                    Set<String> agreementAttachmentIds = properties.getAgreementAttachmentIds();
                    for (Attachment attachment: attachments) {
                        if (agreementAttachmentIds.contains(attachment.getId())) {
                            agreementAttachments.add(new AttachmentRepresentation(attachment));
                        } else {
                            requesterAttachments.add(new AttachmentRepresentation(attachment));
                        }
                    }
                } else {
                    for (Attachment attachment: attachments) {
                        requesterAttachments.add(new AttachmentRepresentation(attachment));
                    }
                }
                request.setAttachments(requesterAttachments);
                request.setAgreementAttachments(agreementAttachments);
            }
            request.setStatus((String)variables.get("status"));
            request.setTitle((String)variables.get("title"));
            request.setDescription((String)variables.get("description"));
            request.setMotivation((String)variables.get("motivation"));
            request.setStatisticsRequest(fetchBooleanVariable("is_statistics_request", variables));
            request.setPaReportRequest(fetchBooleanVariable("is_pa_report_request", variables));
            request.setMaterialsRequest(fetchBooleanVariable("is_materials_request", variables));
            request.setReturnDate((Date)variables.get("return_date"));
            request.setLimitedToCohort(fetchBooleanVariable("limited_to_cohort", variables));
            request.setContactPersonName((String)variables.get("contact_person_name"));
            
            if (is_palga) {
                request.setRequesterValid(fetchBooleanVariable("requester_is_valid", variables));
                request.setRequesterAllowed(fetchBooleanVariable("requester_is_allowed", variables));
                request.setContactPersonAllowed(fetchBooleanVariable("contact_person_is_allowed", variables));
                request.setRequesterLabValid(fetchBooleanVariable("requester_lab_is_valid", variables));
                request.setAgreementReached(fetchBooleanVariable("agreement_reached", variables));
            }
        }
    }

    private Map<String, Object> transferFormData(RequestRepresentation request, ProcessInstance instance, boolean is_palga ) {
        request.setProcessInstanceId(instance.getProcessInstanceId());
        Map<String, Object> variables = instance.getProcessVariables();
        if (variables != null) {
            variables.put("title", request.getTitle());
            variables.put("description", request.getDescription());
            variables.put("motivation", request.getMotivation());
            variables.put("is_statistics_request", (Boolean)request.isStatisticsRequest());
            variables.put("is_pa_report_request", (Boolean)request.isPaReportRequest());
            variables.put("is_materials_request", (Boolean)request.isMaterialsRequest());
            variables.put("return_date", request.getReturnDate());
            variables.put("limited_to_cohort", (Boolean)request.isLimitedToCohort());
            variables.put("contact_person_name", request.getContactPersonName());
            
            if (is_palga) {
                variables.put("requester_is_valid", (Boolean)request.isRequesterValid());
                variables.put("requester_is_allowed", (Boolean)request.isRequesterAllowed());
                variables.put("contact_person_is_allowed", (Boolean)request.isContactPersonAllowed());
                variables.put("requester_lab_is_valid", (Boolean)request.isRequesterLabValid());
                variables.put("agreement_reached", (Boolean)request.isAgreementReached());
            }
        }
        return variables;        
    }
    
    @RequestMapping(value = "/requests", method = RequestMethod.GET)
    public List<RequestRepresentation> get(UserAuthenticationToken user) {
        log.info(
                "GET /requests/ (for user: " + (user == null ? "null" : user.getId()) + ")");
        List<ProcessInstance> processInstances;
        if (user == null) {
            processInstances = new ArrayList<ProcessInstance>();
        } else if (user.getUser().isPalga()) {
            processInstances = runtimeService
                    .createProcessInstanceQuery()
                    .includeProcessVariables()
                    .variableValueEquals("status", "Review")
                    .list();
        } else {
            processInstances = runtimeService
                .createProcessInstanceQuery()
                .includeProcessVariables()
                .involvedUser(user.getId().toString())
                .list();
        }
        List<RequestRepresentation> result = new ArrayList<RequestRepresentation>();
        for (ProcessInstance instance : processInstances) {
            RequestRepresentation request = new RequestRepresentation();
            transferData(instance, request, user.getUser().isPalga());
            result.add(request);
        }
        return result;
    }
    
    @ResponseStatus(value=HttpStatus.UNAUTHORIZED, reason="Not logged in.")
    public class NotLoggedInException extends RuntimeException {
        private static final long serialVersionUID = -2361055636793206513L;
    }

    
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
            ProcessInstance instance = runtimeService.startProcessInstanceByKey(
                    "dntp_request_001", values);
            instance = runtimeService.createProcessInstanceQuery()
                    .includeProcessVariables()
                    .processInstanceId(instance.getId()).singleResult();
            RequestRepresentation request = new RequestRepresentation();
            transferData(instance, request, false);
            return request;
        }
    }

    @Secured("hasPermission(#param, 'requestAssignedToUser')")
    @RequestMapping(value = "/requests/{id}", method = RequestMethod.PUT)
    public RequestRepresentation update(
            UserAuthenticationToken user,
            @PathVariable String id,
            @RequestBody RequestRepresentation request) {
        log.info("PUT /requests/" + id);
        ProcessInstance instance = getProcessInstance(id);
        Map<String, Object> variables = transferFormData(request, instance, user.getUser().isPalga());
        runtimeService.setVariables(instance.getProcessInstanceId(), variables);
        for (Entry<String, Object> entry: variables.entrySet()) {
            log.info("PUT /processes/" + id + " set " + entry.getKey() + " = " + entry.getValue());
        }
        instance = getProcessInstance(id);
        RequestRepresentation updatedRequest = new RequestRepresentation();
        transferData(instance, updatedRequest, user.getUser().isPalga());
        return updatedRequest;
    }    

    @ResponseStatus(value=HttpStatus.NOT_FOUND, reason="Request not found.")  // 404
    public class RequestNotFound extends RuntimeException {
        private static final long serialVersionUID = 607177856129334391L;
    }
    
    @ResponseStatus(value=HttpStatus.NOT_FOUND, reason="No task for request.")  // 404
    public class TaskNotFound extends RuntimeException {
        private static final long serialVersionUID = -2361055636793206513L;
    }

    /**
     * Finds current task. Assumes that exactly one task is currently active.
     * @param requestId
     * @return the current task if it exists.
     * @throws TaskNotFound.
     */
    List<HistoricTaskInstance> getHistoricTasksByRequestId(String requestId) {
        return historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(requestId)
                .orderByHistoricTaskInstanceEndTime()
                .desc()
                .list();
    }
    
    /**
     * Finds current task. Assumes that exactly one task is currently active.
     * @param requestId
     * @return the current task if it exists.
     * @throws TaskNotFound.
     */
    Task getTaskByRequestId(String requestId) {
        Task task = taskService.createTaskQuery().processInstanceId(requestId)
                .active()
                //.taskId("request_form")
                .singleResult();
        if (task == null) {
            throw new TaskNotFound();
        }
        return task;
    }

    /** 
     * Finds current task. Assumes that at most one task is currently active.
     * @param requestId
     * @return the current task if it exists, null otherwise.
     */
    Task findTaskByRequestId(String requestId) {
        Task task = taskService.createTaskQuery().processInstanceId(requestId)
                .active()
                //.taskId("request_form")
                .singleResult();
        return task;
    }
    
    /**
     * Finds request.
     * @param requestId
     * @return the current request if it exists; null otherwise.
     */
    ProcessInstance findProcessInstance(String processInstanceId) {
        ProcessInstance instance = runtimeService.createProcessInstanceQuery()
                .includeProcessVariables()
                .processInstanceId(processInstanceId).singleResult();
        return instance;
    }

    /**
     * Finds request.
     * @param requestId
     * @return the current request if it exists.
     * @throws RequestNotFound.
     */
    ProcessInstance getProcessInstance(String processInstanceId) {
        ProcessInstance instance = runtimeService.createProcessInstanceQuery()
                .includeProcessVariables()
                .processInstanceId(processInstanceId).singleResult();
        if (instance == null) {
            throw new RequestNotFound();
        }
        return instance;
    }
    
    @Secured("hasPermission(#param, 'requestAssignedToUser')")
    @RequestMapping(value = "/requests/{id}/submit", method = RequestMethod.PUT)
    public RequestRepresentation submit(
            UserAuthenticationToken user,
            @PathVariable String id,
            @RequestBody RequestRepresentation request) {
        log.info("PUT /requests/" + id + "/submit");
        ProcessInstance instance = getProcessInstance(id);
        Map<String, Object> variables = transferFormData(request, instance, user.getUser().isPalga());
        runtimeService.setVariables(instance.getProcessInstanceId(), variables);
        for (Entry<String, Object> entry: variables.entrySet()) {
            log.info("PUT /requests/" + id + " set " + entry.getKey() + " = " + entry.getValue());
        }
        
        Task task = getTaskByRequestId(id);
        taskService.complete(task.getId());
        instance = getProcessInstance(id);
        RequestRepresentation updatedRequest = new RequestRepresentation();
        transferData(instance, updatedRequest, user.getUser().isPalga());
        return updatedRequest;
    }    

    @Secured("hasPermission(#param, 'isPalgaUser')")
    @RequestMapping(value = "/requests/{id}/claim", method = RequestMethod.PUT)
    public RequestRepresentation claim(
            UserAuthenticationToken user,
            @PathVariable String id,
            @RequestBody RequestRepresentation request) {
        log.info("PUT /requests/" + id + "/claim");
        ProcessInstance instance = getProcessInstance(id);
        Task task = getTaskByRequestId(id);
        if (task.getAssignee() == null || task.getAssignee().isEmpty()) {
            taskService.claim(task.getId(), user.getId().toString());
        } else {
            taskService.delegateTask(task.getId(), user.getId().toString());
        }
        instance = getProcessInstance(id);
        RequestRepresentation updatedRequest = new RequestRepresentation();
        transferData(instance, updatedRequest, user.getUser().isPalga());
        return updatedRequest;
    }    
    
    @Secured("hasPermission(#param, 'isPalgaUser')")
    @RequestMapping(value = "/requests/{id}/unclaim", method = RequestMethod.PUT)
    public RequestRepresentation unclaim(
            UserAuthenticationToken user,
            @PathVariable String id,
            @RequestBody RequestRepresentation request) {
        log.info("PUT /requests/" + id + "/unclaim");
        ProcessInstance instance = getProcessInstance(id);
        Task task = getTaskByRequestId(id);
        taskService.unclaim(task.getId());
        instance = getProcessInstance(id);
        RequestRepresentation updatedRequest = new RequestRepresentation();
        transferData(instance, updatedRequest, user.getUser().isPalga());
        return updatedRequest;
    }    
    
    @ResponseStatus(value=HttpStatus.METHOD_NOT_ALLOWED, reason="Action not allowed in current status.")
    public class InvalidActionInStatus extends RuntimeException {
        private static final long serialVersionUID = 607177856129334391L;
    }
    
    @Secured("hasPermission(#param, 'requestAssignedToUser')")
    @RequestMapping(value = "/requests/{id}", method = RequestMethod.DELETE)
    public void remove(
            UserAuthenticationToken user,
            @PathVariable String id) {
        log.info("DELETE /requests/" + id);
        ProcessInstance instance = getProcessInstance(id);
        RequestRepresentation request = new RequestRepresentation();
        transferData(instance, request, user.getUser().isPalga());
        if (!request.getRequesterId().equals(user.getUser().getId().toString())) {
            throw new RequestNotFound();
        }
        if (!request.getStatus().equals("Open")) {
            throw new InvalidActionInStatus();
        }
        runtimeService.deleteProcessInstance(id, "Removed by user: " + user.getName());
    }    

    @ResponseStatus(value=HttpStatus.INTERNAL_SERVER_ERROR, reason="File upload error.") 
    public class FileUploadError extends RuntimeException {
        private static final long serialVersionUID = 51403280891772531L;
        public FileUploadError() {
            super("File upload error.");
        }
    }
    
    @RequestMapping(value = "/requests/{id}/files", method = RequestMethod.POST)
    public RequestRepresentation uploadFile(UserAuthenticationToken user, @PathVariable String id, 
            @RequestParam("flowFilename") String name,
            @RequestParam("file") MultipartFile file) {
        log.info("POST /requests/" + id + "/files");
        Task task = getTaskByRequestId(id);
        try{
            taskService.createAttachment(
                    file.getContentType(), 
                    task.getId(), task.getProcessInstanceId(), 
                    name, name, file.getInputStream());
        } catch(IOException e) {
            throw new FileUploadError();
        }
        ProcessInstance instance = getProcessInstance(id);
        RequestRepresentation request = new RequestRepresentation();
        transferData(instance, request, user.getUser().isPalga());
        return request;
    }
    
    @RequestMapping(value = "/requests/{id}/agreementFiles", method = RequestMethod.POST)
    public RequestRepresentation uploadAgreementAttachment(UserAuthenticationToken user, @PathVariable String id, 
            @RequestParam("flowFilename") String name,
            @RequestParam("file") MultipartFile file) {
        log.info("POST /requests/" + id + "/agreementFiles");
        Task task = getTaskByRequestId(id);
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
        ProcessInstance instance = getProcessInstance(id);
        RequestRepresentation request = new RequestRepresentation();
        transferData(instance, request, user.getUser().isPalga());
        
        // add attachment id to the set of ids of the agreement attachments.
        RequestProperties properties = requestPropertiesRepository.findByProcessInstanceId(id);
        if (properties == null) {
            properties = new RequestProperties();
            properties.setProcessInstanceId(id);
        }
        properties.getAgreementAttachmentIds().add(attachmentId);
        requestPropertiesRepository.save(properties);

        Map<String, Object> variables = transferFormData(request, instance, user.getUser().isPalga());
        runtimeService.setVariables(instance.getProcessInstanceId(), variables);
        instance = getProcessInstance(id);
        request = new RequestRepresentation();
        transferData(instance, request, user.getUser().isPalga());
        return request;
    }

    @RequestMapping(value = "/requests/{id}/agreementFiles/{attachmentId}", method = RequestMethod.DELETE)
    public RequestRepresentation removeAgreementAttachment(UserAuthenticationToken user, @PathVariable String id, 
            @PathVariable String attachmentId) {
        log.info("DELETE /requests/" + id + "/agreementFiles/" + attachmentId);

        ProcessInstance instance = getProcessInstance(id);
        
        // remove existing agreement.
        RequestProperties properties = requestPropertiesRepository.findByProcessInstanceId(id);
        if (properties != null && properties.getAgreementAttachmentIds().contains(attachmentId)) {
            taskService.deleteAttachment(attachmentId);
            properties.getAgreementAttachmentIds().remove(attachmentId);
            requestPropertiesRepository.save(properties);
        }
        
        instance = getProcessInstance(id);
        RequestRepresentation request = new RequestRepresentation();
        transferData(instance, request, user.getUser().isPalga());
        return request;
    }
    
    @RequestMapping(value = "/requests/{id}/files/{attachmentId}", method = RequestMethod.GET)
    public HttpEntity<InputStreamResource> getFile(UserAuthenticationToken user, @PathVariable String id, 
            @PathVariable String attachmentId) {
        log.info("GET /requests/" + id + "/files/" + attachmentId);
        Task task = getTaskByRequestId(id);
        Attachment result = taskService.getAttachment(attachmentId);
        if (!result.getTaskId().equals(task.getId())) {
         // not associated with current task
            List<HistoricTaskInstance> historicTasks = getHistoricTasksByRequestId(id);
            boolean taskFound = false;
            for(HistoricTaskInstance historicTask: historicTasks) {
                if (result.getTaskId().equals(historicTask.getId())) {
                    taskFound = true;
                    break;
                }
            }
            if (!taskFound) {
                //log.info("Task not found: " + result.getTaskId());
                throw new TaskNotFound();
            }
        }
        InputStream input = taskService.getAttachmentContent(attachmentId);
        InputStreamResource resource = new InputStreamResource(input);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf(result.getType()));
        HttpEntity<InputStreamResource> response =  new HttpEntity<InputStreamResource>(resource, headers);
        LogFactory.getLog(getClass()).info("Returning reponse.");
        return response;
    }    
 
    @RequestMapping(value = "/requests/{id}/files/{attachmentId}", method = RequestMethod.DELETE)
    public void deleteFile(UserAuthenticationToken user, @PathVariable String id, 
            @PathVariable String attachmentId) {
        log.info("DELETE /requests/" + id + "/files/" + attachmentId);
        Task task = getTaskByRequestId(id);
        Attachment result = taskService.getAttachment(attachmentId);
        if (!result.getTaskId().equals(task.getId())) {
            // not associated with current task
            throw new TaskNotFound();
        }
        ProcessInstance instance = getProcessInstance(id);
        RequestRepresentation request = new RequestRepresentation();
        transferData(instance, request, user.getUser().isPalga());
        log.info("Status: " + request.getStatus());
        if (!request.getStatus().equals("Open")) {
            throw new InvalidActionInStatus();
        }
        taskService.deleteAttachment(attachmentId);
    }      
    
}
