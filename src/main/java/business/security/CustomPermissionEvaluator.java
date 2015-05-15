package business.security;

import java.io.Serializable;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import business.exceptions.InvalidPermissionExpression;
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
    private TaskService taskService;
    
    Log log = LogFactory.getLog(getClass());
    
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
            String taskId = (String)targetDomainObject;
            Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
            log.info("isAssignedToTask: " + authentication.getName()
                    + ", " + task.getAssignee());
            return authentication.getName().equals(task.getAssignee());
        } 
        else if ("requestAssignedToUser".equals(permission)) 
        {
            String requestId = (String)targetDomainObject;
            log.info("requestAssignedToUser: user = " + user.getId()
                    + ", requestId = " + requestId);
            long count = taskService.createTaskQuery().processInstanceId(requestId)
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
            String requestId = (String)targetDomainObject;
            log.info("isRequester: user = " + user.getId()
                    + ", requestId = " + requestId);
            ProcessInstance instance = requestService.findProcessInstance(requestId);
            RequestRepresentation request = new RequestRepresentation();
            requestFormService.transferData(instance, request, user);
            return request.getRequesterId().equals(user.getId().toString());
        } 
        else if ("isScientificCouncil".equals(permission)) 
        {
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
            log.info("isLabuser: user = " + user.getId());
            return user.isLabUser();
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