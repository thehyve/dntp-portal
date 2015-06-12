package business.security;

import java.io.Serializable;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.PermissionEvaluator;
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
    private RequestService requestService;

    @Autowired
    private RequestFormService requestFormService;
    
    @Autowired
    private LabRequestRepository labRequestRepository;
    
    @Autowired 
    private TaskService taskService;
    
    Log log = LogFactory.getLog(getClass());
    
    private void checkTargetDomainObjectNotNull(Object targetDomainObject) {
        if (targetDomainObject == null) {
            throw new NullIdentifier();
        }
    }
    
    /**
     * FIXME: documentation:
     * - isAssignedToTask
     * - requestAssignedToUser: hasPermission(#id, 'requestAssignedToUser')
     * - isPalgaUser
     * - isRequester
     * - isScientificCouncil
     * - isLabuser
     */
    @Override
    public boolean hasPermission(Authentication authentication,
            Object targetDomainObject, Object permission) {

        if (!authentication.isAuthenticated()) {
            log.info("CustomPermissionEvaluator: User not authenticated.");
            return false;
        }
        User user = (User)authentication.getPrincipal();
        log.info("CustomPermissionEvaluator: user = " + user.getId()
                + ", targetDomainObject = " + 
                    ((targetDomainObject==null) ? "" : targetDomainObject.toString())
                + ", permission = " + permission.toString());
        if ("isAssignedToTask".equals(permission)) 
        {
            checkTargetDomainObjectNotNull(targetDomainObject);
            String taskId = (String)targetDomainObject;
            Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
            log.info("isAssignedToTask: " + authentication.getName()
                    + ", " + task.getAssignee());
            return authentication.getName().equals(task.getAssignee());
        } 
        else if ("requestAssignedToUser".equals(permission)) 
        {
            checkTargetDomainObjectNotNull(targetDomainObject);
            String requestId = (String)targetDomainObject;
            log.info("requestAssignedToUser: user = " + user.getId()
                    + ", requestId = " + requestId);
            long count = taskService.createTaskQuery().processInstanceId(requestId)
                    .active()
                    .taskAssignee(user.getId().toString())
                    .count();
            return (count > 0);
        } 
        else if ("labRequestAssignedToUser".equals(permission)) 
        {
            checkTargetDomainObjectNotNull(targetDomainObject);
            Long labRequestId = (Long)targetDomainObject;
            log.info("labRequestAssignedToUser: user = " + user.getId()
                    + ", labRequestId = " + labRequestId);
            LabRequest labRequest = labRequestRepository.findOne(labRequestId); 
            long count = taskService.createTaskQuery().taskId(labRequest.getTaskId())
                    .active()
                    .taskAssignee(user.getId().toString())
                    .count();
            return (count > 0);
        } 
        else if ("isPalgaUser".equals(permission)) 
        {
            String requestId = (String)targetDomainObject;
            log.info("isPalgaUser: user = " + user.getId()
                    + ", requestId = " + requestId);
            return user.isPalga();
        }
        else if ("isRequester".equals(permission)) 
        {
            checkTargetDomainObjectNotNull(targetDomainObject);
            String requestId = (String)targetDomainObject;
            if (requestId == null)
            log.info("isRequester: user = " + user.getId()
                    + ", requestId = " + requestId);
            HistoricProcessInstance instance = requestService.findProcessInstance(requestId);
            RequestRepresentation request = new RequestRepresentation();
            requestFormService.transferData(instance, request, user);
            return request.getRequesterId().equals(user.getId().toString());
        } 
        else if ("isScientificCouncil".equals(permission)) 
        {
            checkTargetDomainObjectNotNull(targetDomainObject);
            String requestId = (String)targetDomainObject;
            log.info("isScientificCouncil: user = " + user.getId()
                    + ", requestId = " + requestId);
            if (!user.isScientificCouncilMember()) {
                return false;
            }
            Task task = requestService.findTaskByRequestId(requestId, "scientific_council_approval");
            log.info("Task: " + task);
            return (task != null);
        } 
        else if ("isLabuser".equals(permission)) 
        {
            checkTargetDomainObjectNotNull(targetDomainObject);
            String requestId = (String)targetDomainObject;
            log.info("isLabuser: user = " + user.getId());
            if (!user.isLabUser()) {
                return false;
            }
            long count = taskService
                    .createTaskQuery()
                    .processInstanceId(requestId)
                    .processVariableValueEquals("lab", user.getLab().getNumber())
                    .count();
            return (count > 0);
        } 
        else if ("isLabRequestLabuser".equals(permission)) 
        {
            checkTargetDomainObjectNotNull(targetDomainObject);
            Long labRequestId = (Long)targetDomainObject;
            log.info("isLabRequestLabuser: user = " + user.getId()
                    + ", labRequestId = " + labRequestId);
            if (!user.isLabUser()) {
                return false;
            }
            LabRequest labRequest = labRequestRepository.findOne(labRequestId);
            if (!labRequest.getLab().getNumber().equals(user.getLab().getNumber())) {
                return false;
            }
            return true;
        } 
        else if ("isLabRequestRequester".equals(permission)) 
        {
            checkTargetDomainObjectNotNull(targetDomainObject);
            Long labRequestId = (Long)targetDomainObject;
            log.info("isLabRequestRequester: user = " + user.getId()
                    + ", labRequestId = " + labRequestId);
            if (!user.isRequester()) {
                return false;
            }
            LabRequest labRequest = labRequestRepository.findOne(labRequestId);
            HistoricProcessInstance instance = requestService.findProcessInstance(labRequest.getProcessInstanceId());
            RequestRepresentation request = new RequestRepresentation();
            requestFormService.transferData(instance, request, user);
            return request.getRequesterId().equals(user.getId().toString());
        } 
        else 
        {
            throw new InvalidPermissionExpression();
        }
    }

    @Override
    public boolean hasPermission(Authentication authentication,
            Serializable targetId, String targetType, Object permission) {
        log.info("hasPermission[2]: user = " + authentication.getName()
                + ", targetId = " + targetId.toString() 
                + ", targetType = " + targetType 
                + ", permission = " + permission.toString());
        return false;
    }
    
}