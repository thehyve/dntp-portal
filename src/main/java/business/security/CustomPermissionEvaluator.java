package business.security;

import java.io.Serializable;
import java.util.Date;

import org.activiti.engine.HistoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import business.exceptions.InvalidPermissionExpression;
import business.exceptions.NullIdentifier;
import business.models.LabRequest;
import business.models.LabRequestRepository;
import business.models.User;
import business.representation.RequestRepresentation;
import business.services.RequestFormService;
import business.services.RequestService;

@Component
public class CustomPermissionEvaluator implements PermissionEvaluator {

    @Autowired
    private RuntimeService runtimeService;

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
    
    

    private void logDecision(String permission, User user, String id, String decision) {
        log.trace(String.format("%s\t%s\t%d\t%s\t%s",
                new Date(),
                permission,
                user == null ? "" : user.getId(),
                id,
                decision));
    }

    /**
     * Use the annotation {@link PreAuthorize} with the permission rules below
     * for data access control to secure controller functions.<br>
     * Example: {@code @PreAuthorize("isAuthenticated() and hasPermission(#id, 'requestAssignedToUser')")}<br>
     * Usage of id {@code id} in the documentation actually refers to the
     * {@code processInstanceId} of the request.
     * <ul>
     * <li><strong>isAssignedToTask</strong>:
     *      Usage: {@code hasPermission(#taskId, 'isAssignedToTask')}<br>
     *      Checks if the user is assigned to the (single) task with the id
     *      {@code taskId}.
     * </li>     
     * <li><strong>requestAssignedToUser</strong>: 
     *      Usage: {@code hasPermission(#id, 'requestAssignedToUser')}<br>
     *      Checks if there exists a running task that is associated with the request
     *      with id {@code id} and is assigned to the user.
     * </li>
     * <li><strong>labRequestAssignedToUser</strong>:
     *      Usage: {@code hasPermission(#labRequestId, 'requestAssignedToUser')}<br>
     *      Checks if there exists a running task that is associated with the lab request
     *      with id {@code labRequestId} and is assigned to the user.
     * </li>
     * <li><strong>isPalgaUser</strong>:
     *      Usage: {@code hasPermission(#id, 'isPalgaUser')}<br>
     *      Checks if the user is a palga user.
     *      Equivalent to {@code hasRole('palga')}.
     * </li>
     * <li><strong>isRequester</strong>:
     *      Usage: {@code hasPermission(#id, 'isRequester')}<br>
     *      Checks if the user is the requester of the request with id {@code id}.
     * </li>
     * <li><strong>isScientificCouncil</strong>:
     *      Usage: {@code hasPermission(#id, 'isScientificCouncil')}<br>
     *      Checks if the request with id {@code id} is in status 'Approval'
     *      (actually, if an approval task is associated with the request)
     *      or if the request has already past the approval phase (i.e., 
     *      an approval decision is associated with the request).
     * </li>
     * <li><strong>isLabuser</strong>:
     *      Usage: {@code hasPermission(#id, 'isLabuser')}<br>
     *      Checks if the user is a lab user 
     *      and if there is a task that is both associated with 
     *          the request with id {@code id}
     *          and with the lab of the user.
     * </li>
     * <li><strong>isLabRequestLabuser</strong>:
     *      Usage: {@code hasPermission(#labRequestId, 'isLabRequestLabuser')}<br>
     *      Checks if the user is a lab user and if the lab request with id {@code labRequestId}
     *      is associated with the lab of the user. 
     * </li>
     * <li><strong>isLabRequestRequester</strong>:
     *      Usage: {@code hasPermission(#labRequestId, 'isLabRequestRequester')}<br>
     *      Checks if the user is a requester and if the user is the requester of
     *      the main request to which the lab request with id {@code labRequestId}
     *      belongs.
     * </li>
     * </ul>
     * 
     * @param targetDomainObject - the request id ({@code processInstanceId}) or
     *        the lab request id, depending on the value of {@code permission}
     * @param permission - the permission rule name, see above.
     */
    @Override
    public boolean hasPermission(Authentication authentication,
            Object targetDomainObject, Object permission) {

        if (!authentication.isAuthenticated()) {
            logDecision(permission.toString(), null, "", "DENIED (user not authenticated).");
            return false;
        }
        User user = (User)authentication.getPrincipal();
        logDecision(permission.toString(), user, ((targetDomainObject==null) ? "" : targetDomainObject.toString()), "");
        if ("isAssignedToTask".equals(permission)) 
        {
            checkTargetDomainObjectNotNull(targetDomainObject);
            String taskId = (String)targetDomainObject;
            return checkIsAssignedToTask(user, taskId);
        } 
        else if ("requestAssignedToUser".equals(permission)) 
        {
            checkTargetDomainObjectNotNull(targetDomainObject);
            String requestId = (String)targetDomainObject;
            return checkRequestAssignedToUser(user, requestId);
        } 
        else if ("labRequestAssignedToUser".equals(permission)) 
        {
            checkTargetDomainObjectNotNull(targetDomainObject);
            Long labRequestId = (Long)targetDomainObject;
            return checkLabRequestAssignedToUser(user, labRequestId);
        } 
        else if ("isPalgaUser".equals(permission)) 
        {
            String requestId = (String)targetDomainObject;
            if (user.isPalga()) {
                logDecision("isPalgaUser", user, requestId, "OK.");
                return true;
            } else {
                logDecision("isPalgaUser", user, requestId, "DENIED (not a Palga user).");
                return false;
            }
        }
        else if ("isRequester".equals(permission)) 
        {
            checkTargetDomainObjectNotNull(targetDomainObject);
            String requestId = (String)targetDomainObject;
            return checkIsRequester(user, requestId);
        } 
        else if ("isScientificCouncil".equals(permission)) 
        {
            checkTargetDomainObjectNotNull(targetDomainObject);
            String requestId = (String)targetDomainObject;
            return checkIsScientificCouncil(user, requestId);
        } 
        else if ("isLabuser".equals(permission)) 
        {
            checkTargetDomainObjectNotNull(targetDomainObject);
            String requestId = (String)targetDomainObject;
            return checkIsLabuser(user, requestId);
        } 
        else if ("isLabRequestLabuser".equals(permission)) 
        {
            checkTargetDomainObjectNotNull(targetDomainObject);
            Long labRequestId = (Long)targetDomainObject;
            return checkIsLabRequestLabuser(user, labRequestId);
        } 
        else if ("isLabRequestRequester".equals(permission)) 
        {
            checkTargetDomainObjectNotNull(targetDomainObject);
            Long labRequestId = (Long)targetDomainObject;
            return checkIsLabRequestRequester(user, labRequestId);
        } 
        else 
        {
            throw new InvalidPermissionExpression();
        }
    }
    
    private void checkTargetDomainObjectNotNull(Object targetDomainObject) {
        if (targetDomainObject == null) {
            throw new NullIdentifier();
        }
    }
    
    /**
     * Usage: {@code hasPermission(#taskId, 'isAssignedToTask')}<br>
     * Checks if the user is assigned to the (single) task with the id
     * {@code taskId}.
     * @param user
     * @param taskId
     */
    private boolean checkIsAssignedToTask(User user, String taskId) {
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
    private boolean checkRequestAssignedToUser(User user, String requestId) {
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
    private boolean checkLabRequestAssignedToUser(User user, Long labRequestId) {
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
    private boolean checkIsRequester(User user, String requestId) {
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
    private boolean checkIsScientificCouncil(User user, String requestId) {
        if (!user.isScientificCouncilMember()) {
            logDecision("isScientificCouncil", user, requestId, "DENIED (user is not a member of the scientific council).");
            return false;
        }
        Task task = requestService.findTaskByRequestId(requestId, "scientific_council_approval");
        if (task != null) {
            logDecision("isScientificCouncil", user, requestId, "OK.");
            return true;
        } else {
            long count = historyService
                    .createHistoricProcessInstanceQuery()
                    .notDeleted()
                    .processInstanceId(requestId)
                    .includeProcessVariables()
                    .variableValueNotEquals("status", "Approval")
                    .variableValueNotEquals("scientific_council_approved", null)
                    .count();
            if (count > 0) {
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
    private boolean checkIsLabuser(User user, String requestId) {
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
     * Usage: {@code hasPermission(#labRequestId, 'isLabRequestLabuser')}<br>
     * Checks if the user is a lab user and if the lab request with id {@code labRequestId}
     * is associated with the lab of the user. 
     * @param user
     * @param labRequestId
     */
    private boolean checkIsLabRequestLabuser(User user, Long labRequestId) {
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
    private boolean checkIsLabRequestRequester(User user, Long labRequestId) {
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
        requestFormService.transferData(instance, request, user);
        if (request.getRequesterId().equals(user.getId().toString())) {
            logDecision("isLabRequestRequester", user, labRequestId.toString(), "OK.");
            return true;
        } else {
            logDecision("isLabRequestRequester", user, labRequestId.toString(), "DENIED (user is not the requester of the request).");
            return false;
        }
    }

    @Override
    public boolean hasPermission(Authentication authentication,
            Serializable targetId, String targetType, Object permission) {
        log.trace("hasPermission[2]: user = " + authentication.getName()
                + ", targetId = " + targetId.toString() 
                + ", targetType = " + targetType 
                + ", permission = " + permission.toString());
        return false;
    }
    
}