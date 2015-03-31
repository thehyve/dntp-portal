package business.controllers;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import business.models.User;
import business.models.UserRepository;
import business.representation.RequestRepresentation;
import business.security.UserAuthenticationToken;

@RestController
public class RequestController {

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
            Task task = taskService.createTaskQuery().processInstanceId(instance.getId())
                    .active()
                    //.taskId("request_form")
                    .singleResult();
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
            }
        }
        return variables;        
    }
    
    @RequestMapping(value = "/requests", method = RequestMethod.GET)
    public List<RequestRepresentation> get(UserAuthenticationToken user) {
        LogFactory.getLog(getClass()).info(
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
    
    @ResponseStatus(value=HttpStatus.UNAUTHORIZED, reason="Not logged in.")  // 404
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
            LogFactory.getLog(getClass()).info(
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
        LogFactory.getLog(getClass()).info("PUT /requests/" + id);
        ProcessInstance instance = runtimeService.createProcessInstanceQuery()
                .includeProcessVariables()
                .processInstanceId(id).singleResult();
        if (instance == null) {
            LogFactory.getLog(getClass()).error(
                    "Request with id '" + id + "' not found.");
            return null;
        }
        Map<String, Object> variables = transferFormData(request, instance, user.getUser().isPalga());
        runtimeService.setVariables(instance.getProcessInstanceId(), variables);
        for (Entry<String, Object> entry: variables.entrySet()) {
            LogFactory.getLog(getClass()).info("PUT /processes/" + id + " set " + entry.getKey() + " = " + entry.getValue());
        }
        instance = runtimeService.createProcessInstanceQuery()
                .includeProcessVariables()
                .processInstanceId(id).singleResult();
        RequestRepresentation updatedRequest = new RequestRepresentation();
        transferData(instance, updatedRequest, user.getUser().isPalga());
        return updatedRequest;
    }    

    @ResponseStatus(value=HttpStatus.NOT_FOUND, reason="Request not found.")  // 404
    public class RequestNotFoundException extends RuntimeException {
        private static final long serialVersionUID = -2361055636793206513L;
    }
    
    @ResponseStatus(value=HttpStatus.NOT_FOUND, reason="No task for request.")  // 404
    public class TaskNotFoundException extends RuntimeException {
        private static final long serialVersionUID = -2361055636793206513L;
    }
    
    @Secured("hasPermission(#param, 'requestAssignedToUser')")
    @RequestMapping(value = "/requests/{id}/submit", method = RequestMethod.PUT)
    public RequestRepresentation submit(
            UserAuthenticationToken user,
            @PathVariable String id,
            @RequestBody RequestRepresentation request) {
        LogFactory.getLog(getClass()).info("PUT /requests/" + id + "/submit");
        ProcessInstance instance = runtimeService.createProcessInstanceQuery()
                .includeProcessVariables()
                .processInstanceId(id).singleResult();
        if (instance == null) {
            throw new RequestNotFoundException();
        }
        Map<String, Object> variables = transferFormData(request, instance, user.getUser().isPalga());
        runtimeService.setVariables(instance.getProcessInstanceId(), variables);
        for (Entry<String, Object> entry: variables.entrySet()) {
            LogFactory.getLog(getClass()).info("PUT /requests/" + id + " set " + entry.getKey() + " = " + entry.getValue());
        }
        
        Task task = taskService.createTaskQuery().processInstanceId(id)
            .active()
            //.taskId("request_form")
            .singleResult();
        if (task == null) {
            throw new TaskNotFoundException();
        } else {
            taskService.complete(task.getId());
        }
        instance = runtimeService.createProcessInstanceQuery()
                .includeProcessVariables()
                .processInstanceId(id).singleResult();
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
        LogFactory.getLog(getClass()).info("PUT /requests/" + id + "/claim");
        ProcessInstance instance = runtimeService.createProcessInstanceQuery()
                .includeProcessVariables()
                .processInstanceId(id).singleResult();
        if (instance == null) {
            throw new RequestNotFoundException();
        }
        Task task = taskService.createTaskQuery().processInstanceId(id)
            .active()
            //.taskId("request_form")
            .singleResult();
        if (task == null) {
            throw new TaskNotFoundException();
        } else {
            taskService.claim(task.getId(), user.getId().toString());
        }
        instance = runtimeService.createProcessInstanceQuery()
                .includeProcessVariables()
                .processInstanceId(id).singleResult();
        RequestRepresentation updatedRequest = new RequestRepresentation();
        transferData(instance, updatedRequest, user.getUser().isPalga());
        return updatedRequest;
    }    
    
    @Secured("hasPermission(#param, 'requestAssignedToUser')")
    @RequestMapping(value = "/requests/{id}", method = RequestMethod.DELETE)
    public RequestRepresentation remove(
            UserAuthenticationToken user,
            @PathVariable String id) {
        LogFactory.getLog(getClass()).info("DELETE /requests/" + id);
        ProcessInstance instance = runtimeService.createProcessInstanceQuery()
                .includeProcessVariables()
                .processInstanceId(id).singleResult();
        if (instance == null) {
            LogFactory.getLog(getClass()).error(
                    "Request with id '" + id + "' not found.");
            return null;
        }
        RequestRepresentation updatedRequest = new RequestRepresentation();
        transferData(instance, updatedRequest, user.getUser().isPalga());
        return updatedRequest;
    }    

}
