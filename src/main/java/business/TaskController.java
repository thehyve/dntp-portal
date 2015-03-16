package business;

import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.history.HistoricData;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.task.Attachment;
import org.activiti.engine.task.Task;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class TaskController {

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

    TaskRepresentation buildRepresentation(Task task) {
        if (task == null) {
            return null;
        }
        TaskRepresentation representation = new TaskRepresentation(task);
        representation.setFormData(new TaskFormDataRepresentation(formService.getTaskFormData(task.getId())));
        representation.setAttachments(taskService.getTaskAttachments(task.getId()));
        return representation;
    }
    
    TaskRepresentation buildRepresentation(HistoricTaskInstance task) {
        if (task == null) {
            return null;
        }
        TaskRepresentation representation = new TaskRepresentation(task);
        List<HistoricData> history = new ArrayList<HistoricData>();
        for (HistoricData data: historyService.createHistoricVariableInstanceQuery()
                .processInstanceId(task.getProcessInstanceId())
                .list()) {
            history.add(data);
        }
        
        for (HistoricData data: historyService.createHistoricDetailQuery()
                .taskId(task.getId())
                .formProperties()
                .list()) {
            history.add(data);
        }
        representation.setFormData(new TaskFormDataRepresentation(
                task.getFormKey(),
                history,
                task.getId()));
        return representation;
    }
    
    @RequestMapping(value = "/tasks", method = RequestMethod.GET, 
            produces = MediaType.APPLICATION_JSON_VALUE)
    public List<TaskRepresentation> get(Principal principal) {
        String assignee = principal.getName();
        LogFactory.getLog(getClass()).info("GET /tasks/" + assignee);
        List<Task> tasks = taskService.createTaskQuery()
                //.taskAssignee(assignee)
                .list();
        List<TaskRepresentation> dtos = new ArrayList<TaskRepresentation>();
        for (Task task : tasks) {
            TaskRepresentation representation = buildRepresentation(task);
            dtos.add(representation);
        }
        return dtos;
    }
    
    @RequestMapping(value = "/tasks/completed", method = RequestMethod.GET, 
            produces = MediaType.APPLICATION_JSON_VALUE)
    public List<TaskRepresentation> getCompleted(Principal principal) {
        String assignee = principal.getName();
        LogFactory.getLog(getClass()).info("GET /tasks/completed/" + assignee);
        List<HistoricTaskInstance> tasks = historyService.createHistoricTaskInstanceQuery()
                .taskAssignee(assignee)
                .orderByHistoricTaskInstanceEndTime()
                .desc()
                .list();
        //List<Task> tasks = taskService.createNativeTaskQuery()
        //List<Task> tasks = taskService.createTaskQuery()
                //.taskInvolvedUser(assignee)
                //.taskAssignee(assignee)
                //.list();
        List<TaskRepresentation> dtos = new ArrayList<TaskRepresentation>();
        for (HistoricTaskInstance task : tasks) {
            TaskRepresentation representation = buildRepresentation(task);
            dtos.add(representation);
        }
        return dtos;
    }    
    
    @PreAuthorize("hasPermission(#id, 'isAssignedToTask')")
    @RequestMapping(value = "/tasks/{id}", method = RequestMethod.PUT)
    public TaskRepresentation complete(Principal principal, @RequestBody TaskRepresentation representation, 
            @PathVariable String id) {
        LogFactory.getLog(getClass()).info("PUT /tasks/" + id + ": " + representation.getId());
        Task task = taskService.createTaskQuery().taskId(id).singleResult();
        // Update form data from task representation
        Map<String, String> properties = new HashMap<String, String>();
        for (FormPropertyRepresentation property : 
            representation.getFormData().getFormProperties()) {
            properties.put(property.getId(), property.getValue());
        }
        formService.saveFormData(id, properties);
        taskService.complete(id);
        // Fetch updated task with associated form data.
        task = taskService.createTaskQuery().taskId(id).singleResult();
        return buildRepresentation(task);
    }

    @RequestMapping(value = "/tasks/{id}", method = RequestMethod.DELETE)
    public void delete(Principal principal, @PathVariable String id) {
        LogFactory.getLog(getClass()).info("DELETE /tasks/" + id);      
    }    
    
    @Secured("hasPermission(#property, 'isAssignedToPropertyTask')")
    @RequestMapping(value = "/formdata", method = RequestMethod.PUT)
    public void updateFormData(@RequestParam FormProperty property) {
        LogFactory.getLog(getClass()).info("PUT /formdata/" + property.getName());
    }
    
    @RequestMapping(value = "/tasks/{id}/files", method = RequestMethod.POST)
    public ResponseEntity<String> uploadFile(Principal principal, @PathVariable String id, 
            @RequestParam("flowFilename") String name,
            @RequestParam("file") MultipartFile file) {
        Task task = taskService.createTaskQuery().taskId(id).singleResult();
        LogFactory.getLog(getClass()).info("POST /tasks/" + id + "/files");
        String attachmentId;
        try{
            Attachment result = taskService.createAttachment(file.getContentType(), id, task.getProcessInstanceId(), 
                    name, name, file.getInputStream());
            attachmentId = result.getId();
        } catch(IOException e) {
            return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<String>(attachmentId, HttpStatus.OK);
    }
    
    @RequestMapping(value = "/tasks/{id}/files/{attachmentId}", method = RequestMethod.GET)
    public HttpEntity<InputStreamResource> getFile(Principal principal, @PathVariable String id, 
            @PathVariable String attachmentId) {
        LogFactory.getLog(getClass()).info("GET /tasks/" + id + "/files/" + attachmentId);
        Attachment result = taskService.getAttachment(attachmentId);
        if (!result.getTaskId().equals(id)) {
            // error
            LogFactory.getLog(getClass()).info("Error: not owner of task.");
            return new ResponseEntity<InputStreamResource>(HttpStatus.BAD_REQUEST);
        }
        InputStream input = taskService.getAttachmentContent(attachmentId);
        InputStreamResource resource = new InputStreamResource(input);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf(result.getType()));
        HttpEntity<InputStreamResource> response =  new HttpEntity<InputStreamResource>(resource, headers);
        LogFactory.getLog(getClass()).info("Returning reponse.");
        return response;
    }

    
}
