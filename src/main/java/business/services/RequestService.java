/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.transaction.Transactional;

import org.activiti.engine.HistoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.DelegationState;
import org.activiti.engine.task.IdentityLinkType;
import org.activiti.engine.task.Task;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import business.exceptions.InvalidActionInStatus;
import business.exceptions.RequestNotFound;
import business.exceptions.TaskNotFound;
import business.exceptions.UserUnauthorised;
import business.models.RequestProperties;
import business.models.User;
import business.representation.LabRequestRepresentation;
import business.representation.RequestRepresentation;
import business.representation.RequestStatus;
import business.security.UserAuthenticationToken;

@Service
public class RequestService {

    Log log = LogFactory.getLog(getClass());

    @Autowired
    private TaskService taskService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private LabRequestService labRequestService;

    @Autowired
    private RequestFormService requestFormService;

    @Autowired
    private RequestPropertiesService requestPropertiesService;

    @Autowired
    private MailService mailService;

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
     * @param user
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
            processInstances = new ArrayList<HistoricProcessInstance>();
            processInstances.addAll(
                    historyService
                    .createHistoricProcessInstanceQuery()
                    .notDeleted()
                    .includeProcessVariables()
                    .variableValueEquals("status", "Open")
                    .variableValueEquals("reopen_request", Boolean.TRUE)
                    .list());
            processInstances.addAll(
                    historyService
                    .createHistoricProcessInstanceQuery()
                    .notDeleted()
                    .includeProcessVariables()
                    .variableValueNotEquals("status", "Open")
                    .list());
        } else if (user.getUser().isScientificCouncilMember()) {
            Date start = new Date();
            List<HistoricTaskInstance> approvalTasks = historyService
                    .createHistoricTaskInstanceQuery()
                    .taskDefinitionKey("scientific_council_approval")
                    .list();
            Set<String> processInstanceIds = new HashSet<>();
            for (HistoricTaskInstance task: approvalTasks) {
                processInstanceIds.add(task.getProcessInstanceId());
            }
            processInstances = historyService
                    .createHistoricProcessInstanceQuery()
                    .notDeleted()
                    .includeProcessVariables()
                    .processInstanceIds(processInstanceIds)
                    .list();
            Date end = new Date();
            log.info("GET: query took " + (end.getTime() - start.getTime()) + " ms.");
        } else if (user.getUser().isLabUser() || user.getUser().isHubUser()) {
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
            processInstances = new ArrayList<HistoricProcessInstance>();
            processInstances.addAll(historyService
                    .createHistoricProcessInstanceQuery()
                    .notDeleted()
                    .includeProcessVariables()
                    .involvedUser(user.getId().toString())
                    .variableValueNotEquals("pathologist_email", user.getUser().getContactData().getEmail())
                    .list());
            processInstances.addAll(historyService
                    .createHistoricProcessInstanceQuery()
                    .notDeleted()
                    .includeProcessVariables()
                    .variableValueEquals("pathologist_email", user.getUser().getContactData().getEmail())
                    .list());

        }
        return processInstances;
    }

    /**
     * Completes <code>request_form</code> task, generates a
     * request number, and sends an email to the requester
     * for the request with processInstanceId <code>id</code>.
     * @param id the processInstanceId of the request.
     */
    @Transactional
    public RequestProperties submitRequest(User requester, String id) {
        RequestProperties properties = requestPropertiesService.findByProcessInstanceId(id);

        Task task = getTaskByRequestId(id, "request_form");
        if (task.getDelegationState()==DelegationState.PENDING) {
            taskService.resolveTask(task.getId());
        }
        taskService.complete(task.getId());

        properties.setRequestNumber(requestPropertiesService.getNewRequestNumber(properties));

        mailService.sendAgreementFormLink(requester, properties);

        return properties;
    }

    /**
     * 
     */
    @Transactional
    public RequestRepresentation forkRequest(User user, String parentId) {
        HistoricProcessInstance parentInstance = getProcessInstance(parentId);
        RequestRepresentation parentRequest = new RequestRepresentation();
        requestFormService.transferData(parentInstance, parentRequest, user);
        if (parentRequest.getStatus() != RequestStatus.LAB_REQUEST) {
            throw new InvalidActionInStatus("Forking of requests not allowed in status " +
                    parentRequest.getStatus() + ".");
        }
        log.info("Forking request " + parentRequest.getRequestNumber() +
                " (requester: " + parentRequest.getRequesterId() + ", " +
                parentRequest.getRequesterEmail() + ")");

        // start new process instance
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("initiator", parentRequest.getRequesterId());
        values.put("jump_to_data_delivery", Boolean.TRUE);
        ProcessInstance newInstance = runtimeService.startProcessInstanceByKey(
                "dntp_request_003", values);
        String childId = newInstance.getProcessInstanceId();
        log.info("New forked process instance started: " + childId);
        runtimeService.addUserIdentityLink(childId, parentRequest.getRequesterId(), IdentityLinkType.STARTER);

        HistoricProcessInstance childInstance = getProcessInstance(childId);
        // copy all request properties to the new instance.
        Map<String, Object> variables = requestFormService.transferFormData(
                parentRequest, childInstance, user);
        runtimeService.setVariables(childId, variables);

        RequestProperties childProperties = requestPropertiesService.findByProcessInstanceId(childId);
        // generate new request number
        childProperties.setRequestNumber(requestPropertiesService.getNewRequestNumber(childProperties));
        // set link between parent and child request
        RequestProperties parentProperties = requestPropertiesService.findByProcessInstanceId(parentId);
        childProperties.setParent(parentProperties);
        parentProperties.getChildren().add(childProperties);
        requestPropertiesService.save(parentProperties);

        childInstance = getProcessInstance(childId);
        RequestRepresentation childRequest = new RequestRepresentation();
        requestFormService.transferData(childInstance, childRequest, null);
        return childRequest;
    }

}
