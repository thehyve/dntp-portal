package business.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.transaction.Transactional;

import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.task.DelegationState;
import org.activiti.engine.task.Task;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import business.controllers.RequestComparator;
import business.exceptions.RequestNotFound;
import business.exceptions.TaskNotFound;
import business.exceptions.UserUnauthorised;
import business.models.RequestProperties;
import business.models.User;
import business.representation.LabRequestRepresentation;
import business.security.UserAuthenticationToken;

@Service
public class RequestService {

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
    private RequestComparator requestComparator;

    @Autowired
    private LabRequestService labRequestService;

    @Autowired
    private RequestNumberService requestNumberService;

    @Autowired
    private RequestPropertiesService requestPropertiesService;

    /**
     * Finds task. 
     * @param taskId
     * @return the task if it exists.
     * @throws business.exceptions.TaskNotFound
     */
    public HistoricTaskInstance getTask(String taskId, String taskDefinition) {
        HistoricTaskInstance task = historyService.createHistoricTaskInstanceQuery()
                .taskId(taskId)
                //.active()
                .taskDefinitionKey(taskDefinition)
                .singleResult();
        if (task == null) {
            throw new TaskNotFound();
        }
        return task;
    }

    /**
     * Finds historic task. Assumes that exactly one task is currently active.
     * @param requestId
     * @return the task if it exists; null otherwise.
     */
    public HistoricTaskInstance findHistoricTaskByRequestId(String requestId, String taskDefinition) {
        HistoricTaskInstance task = historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(requestId)
                .taskDefinitionKey(taskDefinition)
                .singleResult();
        return task;
    }

    /**
     * Finds current task. Assumes that exactly one task is currently active.
     * @param requestId
     * @return the current task if it exists.
     * @throws business.exceptions.TaskNotFound
     */
    public Task getTaskByRequestId(String requestId, String taskDefinition) {
        Task task = taskService.createTaskQuery().processInstanceId(requestId)
                .active()
                .taskDefinitionKey(taskDefinition)
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
    public Task findTaskByRequestId(String requestId, String taskDefinition) {
        Task task = taskService.createTaskQuery().processInstanceId(requestId)
                .active()
                .taskDefinitionKey(taskDefinition)
                .singleResult();
        return task;
    }

    /**
     * Finds current task. Assumes that exactly one task is currently active.
     * @param requestId
     * @return the current task if it exists.
     * @throws business.exceptions.TaskNotFound
     */
    public Task getCurrentPalgaTaskByRequestId(String requestId) {
        Task task = findCurrentPalgaTaskByRequestId(requestId);
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
    public Task findCurrentPalgaTaskByRequestId(String requestId) {
        Task task = findTaskByRequestId(requestId, "palga_request_review");
        if (task == null) {
            task = findTaskByRequestId(requestId, "request_approval");
        }
        if (task == null) {
            task = findTaskByRequestId(requestId, "data_delivery");
        }
        if (task == null) {
            task = findTaskByRequestId(requestId, "selection_review");
        }
        return task;
    }
    
    /**
     * Claims current Palga task.
     * @param requestId
     * @param the Palga user token.
     */
    public void claimCurrentPalgaTask(String requestId, User user) {
        if (user.isPalga()) {
            Task task = this.getCurrentPalgaTaskByRequestId(requestId);
            if (task.getAssignee() == null || task.getAssignee().isEmpty()) {
                taskService.claim(task.getId(), user.getId().toString());
            } else {
                taskService.delegateTask(task.getId(), user.getId().toString());
            }
        } else {
            throw new UserUnauthorised("User not allowed to claim task.");
        }
    }
    
    /**
     * Finds request.
     * @param processInstanceId
     * @return the current request if it exists; null otherwise.
     */
    public HistoricProcessInstance findProcessInstance(String processInstanceId) {
        HistoricProcessInstance instance = historyService
                .createHistoricProcessInstanceQuery()
                .includeProcessVariables()
                .processInstanceId(processInstanceId)
                .singleResult();
        return instance;
    }

    /**
     * Finds request.
     * @param processInstanceId
     * @return the current request if it exists.
     * @throws
     */
    public HistoricProcessInstance getProcessInstance(String processInstanceId) {
        HistoricProcessInstance instance = historyService
                .createHistoricProcessInstanceQuery()
                .includeProcessVariables()
                .processInstanceId(processInstanceId)
                .singleResult();
        if (instance == null) {
            throw new RequestNotFound();
        }
        return instance;
    }

    /**
     * 
     * @param user
     * @return
     */
    public List<HistoricProcessInstance> getProcessInstancesForUser(
            UserAuthenticationToken user) {
        List<HistoricProcessInstance> processInstances;

        if (user == null) {
            processInstances = new ArrayList<HistoricProcessInstance>();
        } else if (user.getUser().isPalga()) {
            processInstances = historyService
                    .createHistoricProcessInstanceQuery()
                    .notDeleted()
                    .includeProcessVariables()
                    .variableValueNotEquals("status", "Open")
                    .list();
        } else if (user.getUser().isScientificCouncilMember()) {
            Date start = new Date();
            processInstances = new ArrayList<HistoricProcessInstance>();
            processInstances.addAll(historyService
                    .createHistoricProcessInstanceQuery()
                    .notDeleted()
                    .includeProcessVariables()
                    .variableValueEquals("status", "Approval")
                    .list());
            Date endQ1 = new Date();
            List<HistoricProcessInstance> list = historyService
                    .createHistoricProcessInstanceQuery()
                    .notDeleted()
                    .includeProcessVariables()
                    .variableValueNotEquals("status", "Approval")
                    .variableValueNotEquals("scientific_council_approved", null)
                    .list();
            Date endQ2 = new Date();
            log.info("GET: query 1 took " + (endQ1.getTime() - start.getTime()) + " ms.");
            log.info("GET: query 2 took " + (endQ2.getTime() - endQ1.getTime()) + " ms.");
            if (list != null) {
                processInstances.addAll(list);
            }
        } else if (user.getUser().isLabUser()) {
            List<LabRequestRepresentation> labRequests = labRequestService.findLabRequestsForUser(user.getUser(), false);
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
        return processInstances;
    }

    /**
     * Completes <code>request_form</code> task and generates a
     * request number for the request with processInstanceId <code>id</code>.
     * @param id the processInstanceId of the request.
     */
    @Transactional
    public RequestProperties submitRequest(String id) {
        RequestProperties properties = requestPropertiesService.findByProcessInstanceId(id);

        Task task = getTaskByRequestId(id, "request_form");
        if (task.getDelegationState()==DelegationState.PENDING) {
            taskService.resolveTask(task.getId());
        }
        taskService.complete(task.getId());

        String requestNumber = requestNumberService.getNewRequestNumber();
        properties.setRequestNumber(requestNumber);
        return requestPropertiesService.save(properties);
    }

}
