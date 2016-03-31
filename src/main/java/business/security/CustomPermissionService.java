/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business.security;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.activiti.engine.HistoryService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import business.models.Lab;
import business.models.LabRequest;
import business.models.LabRequestRepository;
import business.models.User;
import business.representation.RequestRepresentation;
import business.services.RequestFormService;
import business.services.RequestService;

@Transactional
@Service
public class CustomPermissionService {

    @Autowired
    private HistoryService historyService;

    @Autowired
    private RequestService requestService;

    @Autowired
    private RequestFormService requestFormService;

    @Autowired
    private LabRequestRepository labRequestRepository;

    @Autowired
    private TaskService taskService;

    Log log = LogFactory.getLog(getClass());

    public void logDecision(String permission, User user, String id, String decision) {
        log.trace(String.format("%s\t%s\t%d\t%s\t%s",
                new Date(),
                permission,
                user == null ? "" : user.getId(),
                id,
                decision));
    }

    /**
     * Usage: {@code hasPermission(#taskId, 'isAssignedToTask')}<br>
     * Checks if the user is assigned to the (single) task with the id
     * {@code taskId}.
     * @param user
     * @param taskId
     */
    public boolean checkIsAssignedToTask(User user, String taskId) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            logDecision("isAssignedToTask", user, taskId, "DENIED (task not found).");
            return false;
        }
        if (user.getId().toString().equals(task.getAssignee())) {
            logDecision("isAssignedToTask", user, taskId, "OK.");
            return true;
        } else {
            logDecision("isAssignedToTask", user, taskId, "DENIED (task not assigned to user).");
            return false;
        }
    }
    
    /**
     * Usage: {@code hasPermission(#id, 'requestAssignedToUser')}<br>
     * Checks if there exists a running task that is associated with the request
     * with id {@code requestId} and is assigned to the user.
     * @param user
     * @param requestId
     */
    public boolean checkRequestAssignedToUser(User user, String requestId) {
        long count = taskService.createTaskQuery()
                .processInstanceId(requestId)
                .active()
                .taskAssignee(user.getId().toString())
                .count();
        if (count > 0) {
            logDecision("requestAssignedToUser", user, requestId, "OK.");
            return true;
        } else {
            logDecision("requestAssignedToUser", user, requestId, "DENIED (no task found for request assigned to user).");
            return false;
        }
    }

    /**
     * Usage: {@code hasPermission(#labRequestId, 'requestAssignedToUser')}<br>
     * Checks if there exists a running task that is associated with the lab request
     * with id {@code labRequestId} and is assigned to the user.
     * @param user
     * @param labRequestId
     */
    public boolean checkLabRequestAssignedToUser(User user, Long labRequestId) {
        LabRequest labRequest = labRequestRepository.findOne(labRequestId);
        if (labRequest == null) {
            logDecision("labRequestAssignedToUser", user, labRequestId.toString(), "DENIED (lab request not found).");
            return false;
        }
        long count = taskService.createTaskQuery().taskId(labRequest.getTaskId())
                .active()
                .taskAssignee(user.getId().toString())
                .count();
        if (count > 0) {
            logDecision("labRequestAssignedToUser", user, labRequestId.toString(), "OK.");
            return true;
        } else {
            logDecision("labRequestAssignedToUser", user, labRequestId.toString(), "DENIED (no lab request task found assigned to user).");
            return false;
        }
    }

    /**
     * Usage: {@code hasPermission(#id, 'isRequester')}<br>
     * Checks if the user is the requester of the request with id {@code id}.
     * @param user
     * @param requestId
     */
    public boolean checkIsRequester(User user, String requestId) {
        if (!user.isRequester()) {
            logDecision("isRequester", user, requestId, "DENIED (not a requester).");
            return false;
        }
        if (requestId == null) {
            logDecision("isRequester", user, requestId, "DENIED (empty request id).");
            return false;
        }
        HistoricProcessInstance instance = requestService.findProcessInstance(requestId);
        if (instance == null) {
            logDecision("isRequester", user, requestId, "DENIED (request not found).");
            return false;
        }
        RequestRepresentation request = new RequestRepresentation();
        // FIXME: use more direct way to check if the requesterId of the request
        // matches the userId.
        requestFormService.transferData(instance, request, user);
        if (request.getRequesterId().equals(user.getId().toString())) {
            logDecision("isRequester", user, requestId, "OK.");
            return true;
        } else {
            logDecision("isRequester", user, requestId, "DENIED (user is not the requester of the request).");
            return false;
        }
    }

    /**
     * Usage: {@code hasPermission(#id, 'isScientificCouncil')}<br>
     * Checks if the request with id {@code id} is in status 'Approval'
     * (actually, if an approval task is associated with the request)
     * or if the request has already past the approval phase (i.e., 
     * an approval decision is associated with the request).
     * @param user
     * @param requestId
     */
    public boolean checkIsScientificCouncil(User user, String requestId) {
        if (!user.isScientificCouncilMember()) {
            logDecision("isScientificCouncil", user, requestId, "DENIED (user is not a member of the scientific council).");
            return false;
        }
        Task task = requestService.findTaskByRequestId(requestId, "scientific_council_approval");
        if (task != null) {
            logDecision("isScientificCouncil", user, requestId, "OK.");
            return true;
        } else {
            long requestCount = historyService
                    .createHistoricProcessInstanceQuery()
                    .notDeleted()
                    .processInstanceId(requestId)
                    .count();
            long approvalTaskCount = historyService
                    .createHistoricTaskInstanceQuery()
                    .processInstanceId(requestId)
                    .taskDefinitionKey("scientific_council_approval")
                    .count();
            if (requestCount > 0 && approvalTaskCount > 0) {
                logDecision("isScientificCouncil", user, requestId, "OK.");
                return true;
            } else {
                logDecision("isScientificCouncil", user, requestId, "DENIED (the request is not and has not been in Approval state).");
                return false;
            }
        }
    }

    /**
     * Usage: {@code hasPermission(#id, 'isLabuser')}<br>
     * Checks if the user is a lab user 
     * and if there is a task that is both associated with 
     * the request with id {@code id}
     * and with the lab of the user.
     * @param user
     * @param requestId
     */
    public boolean checkIsLabuser(User user, String requestId) {
        if (!user.isLabUser()) {
            logDecision("isLabuser", user, requestId, "DENIED (not a lab user).");
            return false;
        }
        long count = taskService
                .createTaskQuery()
                .processInstanceId(requestId)
                .processVariableValueEquals("lab", user.getLab().getNumber())
                .count();
        if (count > 0) {
            logDecision("isLabuser", user, requestId, "OK.");
            return true;
        } else {
            logDecision("isLabuser", user, requestId, "DENIED (no task found for user and request).");
            return false;
        }
    }

    /**
     * Usage: {@code hasPermission(#id, 'isHubuser')}<br>
     * Checks if the user is a hub user
     * and if there is a task that is both associated with
     * the request with id {@code id}
     * and with one of the hub labs of the user.
     * @param user
     * @param requestId
     */
    public boolean checkIsHubuser(User user, String requestId) {
        if (!user.isHubUser()) {
            logDecision("isHubuser", user, requestId, "DENIED (not a hub user).");
            return false;
        }
        if (user.getHubLabs() == null || user.getHubLabs().isEmpty()) {
            logDecision("isHubuser", user, requestId, "DENIED (no hub lab associated with the user).");
            return false;
        }
        Set<Lab> hubLabs = new HashSet<>();
        for(Lab lab: user.getHubLabs()) {
            if (lab.isHubAssistanceEnabled()) {
                hubLabs.add(lab);
            }
        }
        long count = labRequestRepository.countByProcessInstanceIdAndLabIn(requestId, hubLabs);
        if (count > 0) {
            logDecision("isHubuser", user, requestId, "OK.");
            return true;
        } else {
            logDecision("isHubuser", user, requestId, "DENIED (no task found for user and request).");
            return false;
        }
    }

    /**
     * Usage: {@code hasPermission(#labRequestId, 'isLabRequestLabuser')}<br>
     * Checks if the user is a lab user and if the lab request with id {@code labRequestId}
     * is associated with the lab of the user.
     * @param user
     * @param labRequestId
     */
    public boolean checkIsLabRequestLabuser(User user, Long labRequestId) {
        if (!user.isLabUser()) {
            logDecision("isLabRequestLabuser", user, labRequestId.toString(), "DENIED (not a lab user).");
            return false;
        }
        LabRequest labRequest = labRequestRepository.findOne(labRequestId);
        if (labRequest == null) {
            logDecision("isLabRequestLabuser", user, labRequestId.toString(), "DENIED (lab request not found).");
            return false;
        }
        if (!labRequest.getLab().getNumber().equals(user.getLab().getNumber())) {
            logDecision("isLabRequestLabuser", user, labRequestId.toString(), "DENIED (lab request not associated with user lab).");
            return false;
        }
        logDecision("isLabRequestLabuser", user, labRequestId.toString(), "OK.");
        return true;
    }

    /**
     * Usage: {@code hasPermission(#labRequestId, 'isLabRequestRequester')}<br>
     * Checks if the user is a requester and if the user is the requester of
     * the main request to which the lab request with id {@code labRequestId}
     * belongs.
     * @param user
     * @param labRequestId
     */
    public boolean checkIsLabRequestRequester(User user, Long labRequestId) {
        if (!user.isRequester()) {
            logDecision("isLabRequestRequester", user, labRequestId.toString(), "DENIED (not a requester).");
            return false;
        }
        LabRequest labRequest = labRequestRepository.findOne(labRequestId);
        HistoricProcessInstance instance = requestService.findProcessInstance(labRequest.getProcessInstanceId());
        if (instance == null) {
            logDecision("isLabRequestRequester", user, labRequestId.toString(), "DENIED (request not found).");
            return false;
        }
        RequestRepresentation request = new RequestRepresentation();
        // FIXME: use more direct way to check if the requesterId of the request
        // matches the userId.
        requestFormService.transferData(instance, request, user);
        if (request.getRequesterId().equals(user.getId().toString())) {
            logDecision("isLabRequestRequester", user, labRequestId.toString(), "OK.");
            return true;
        } else {
            logDecision("isLabRequestRequester", user, labRequestId.toString(), "DENIED (user is not the requester of the request).");
            return false;
        }
    }

    /**
     * Usage: {@code hasPermission(#labRequestId, 'isLabRequestHubuser')}<br>
     * Checks if the user is a hub user and if the lab request with id {@code labRequestId}
     * is associated with one of the hub labs of the user.
     * @param user
     * @param labRequestId
     */
    public boolean checkIsLabRequestHubuser(User user, Long labRequestId) {
        if (!user.isHubUser()) {
            logDecision("isLabRequestHubuser", user, labRequestId.toString(), "DENIED (not a hub user).");
            return false;
        }
        if (user.getHubLabs() == null || user.getHubLabs().isEmpty()) {
            logDecision("isLabRequestHubuser", user, labRequestId.toString(), "DENIED (no hub lab associated with the user).");
            return false;
        }
        LabRequest labRequest = labRequestRepository.findOne(labRequestId);
        if (labRequest == null) {
            logDecision("isLabRequestHubuser", user, labRequestId.toString(), "DENIED (lab request not found).");
            return false;
        }
        Set<Integer> hubLabNumbers = new HashSet<>();
        for(Lab lab: user.getHubLabs()) {
            if (lab.isHubAssistanceEnabled()) {
                hubLabNumbers.add(lab.getNumber());
            }
        }
        if (!hubLabNumbers.contains(labRequest.getLab().getNumber())) {
            logDecision("isLabRequestHubuser", user, labRequestId.toString(), "DENIED (lab request not associated with user lab).");
            return false;
        }
        logDecision("isLabRequestHubuser", user, labRequestId.toString(), "OK.");
        return true;
    }

}
