package business.controllers;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import business.representation.ProcessInstanceRepresentation;
import business.security.UserAuthenticationToken;

import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.runtime.ProcessInstance;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProcessController {

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

    @RequestMapping(value = "/processes", method = RequestMethod.GET)
    public List<ProcessInstanceRepresentation> get(UserAuthenticationToken user) {
        LogFactory.getLog(getClass()).info(
                "GET /processes/ (for user: " + (user == null ? "null" : user.getId()) + ")");
        List<ProcessInstance> processInstances;
        if (user == null) {
            processInstances = new ArrayList<ProcessInstance>();
        } else if (user.getUser().isPalga()) {
            processInstances = runtimeService
                    .createProcessInstanceQuery()
                    .includeProcessVariables()
                    .list();
        } else {
            processInstances = runtimeService
                .createProcessInstanceQuery()
                .includeProcessVariables()
                .involvedUser(user.getId().toString())
                // .active()
                .list();
            //taskService.createTaskQuery()
            //    .taskCandidateGroup("palga");
        }
        List<ProcessInstanceRepresentation> result = new ArrayList<ProcessInstanceRepresentation>();
        for (ProcessInstance instance : processInstances) {
            result.add(buildRepresentation(instance));
        }
        return result;
    }

    @RequestMapping(value = "/processes/completed", method = RequestMethod.GET)
    public List<ProcessInstanceRepresentation> getCompleted(UserAuthenticationToken user) {
        LogFactory.getLog(getClass()).info(
                "GET /processes/completed (for user: " + user.getId() + ")");
        List<HistoricProcessInstance> processInstances = historyService
                .createHistoricProcessInstanceQuery()
                .includeProcessVariables()
                .list();
        List<ProcessInstanceRepresentation> result = new ArrayList<ProcessInstanceRepresentation>();
        for (HistoricProcessInstance instance : processInstances) {
            result.add(buildRepresentation(instance));
        }
        return result;
    }
    
    @RequestMapping(value = "/processes", method = RequestMethod.POST)
    public ProcessInstanceRepresentation start(
            UserAuthenticationToken user,
            @RequestBody ProcessInstanceRepresentation process) {
        String initiator = user.getId().toString(); // principal.getName();
        LogFactory.getLog(getClass()).info(
                "POST /processes (initiator: " + initiator + ")");
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("initiator", initiator);
        ProcessInstance instance = runtimeService.startProcessInstanceByKey(
                "dntp_request_001", values);
        return buildRepresentation(instance);
    }

    @Secured("hasPermission(#param, 'somePermissionName')")
    @RequestMapping(value = "/processes/{id}", method = RequestMethod.PUT)
    public ProcessInstanceRepresentation update(@PathVariable String id,
            @RequestBody ProcessInstanceRepresentation process) {
        LogFactory.getLog(getClass()).info("PUT /processes/" + id);
        ProcessInstance instance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(id).singleResult();
        if (instance == null) {
            LogFactory.getLog(getClass()).error(
                    "Process with id '" + id + "' not found.");
            return null;
        }
        if (process.isSuspended() && !instance.isSuspended()) {
            runtimeService.suspendProcessInstanceById(id);
        } else if (!process.isSuspended() && instance.isSuspended()) {
            runtimeService.activateProcessInstanceById(id);
        }
        instance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(id).singleResult();
        return buildRepresentation(instance);
    }

    @RequestMapping(value = "/processes/{id}", method = RequestMethod.DELETE)
    public void stop(@PathVariable String id) {
        LogFactory.getLog(getClass()).info("DELETE /processes/" + id);
        runtimeService.deleteProcessInstance(id, null);
    }

    ProcessInstanceRepresentation buildRepresentation(ProcessInstance instance) {
        String id = instance.getId();
        ProcessInstanceRepresentation representation = new ProcessInstanceRepresentation(
                instance);
        representation.setEvents(runtimeService.getProcessInstanceEvents(id));
        representation.setIdentityLinksFromActiviti(runtimeService
                .getIdentityLinksForProcessInstance(id));
        representation.setTasksFromActiviti(taskService.createTaskQuery()
                .processInstanceId(id).list());
        return representation;
    }

    private ProcessInstanceRepresentation buildRepresentation(
            HistoricProcessInstance instance) {
        String id = instance.getId();
        ProcessInstanceRepresentation representation = new ProcessInstanceRepresentation(
                instance);
        /*representation.setProcessVariablesFromHistory(historyService
                .createHistoricVariableInstanceQuery()
                .processInstanceId(id)
                .list());*/
        representation.setIdentityLinksFromHistory(historyService
                .getHistoricIdentityLinksForProcessInstance(id));
        return representation;
    }
    
    
}
