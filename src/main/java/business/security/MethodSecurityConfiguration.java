package business.security;

import java.io.Serializable;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.task.Task;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;
import org.springframework.security.core.Authentication;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class MethodSecurityConfiguration extends GlobalMethodSecurityConfiguration {                   

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;
    
    private PermissionEvaluator permissionEvaluator = new PermissionEvaluator() {

        Log log = LogFactory.getLog(getClass());
        
        @Override
        public boolean hasPermission(Authentication authentication,
                Object targetDomainObject, Object permission) {
            log.info("hasPermission[1]: user = " + authentication.getName()
                    + ", targetDomainObject = " + 
                        ((targetDomainObject==null) ? "" : targetDomainObject.toString())
                    + ", permission = " + permission.toString());
            if ("isAssignedToTask".equals(permission)) {
                String taskId = (String)targetDomainObject;
                Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
                log.info("isAssignedToTask: " + authentication.getName()
                        + ", " + task.getAssignee());
                return authentication.getName().equals(task.getAssignee());
            } else if ("requestAssignedToUser".equals(permission)) {
                String requestId = (String)targetDomainObject;
                //Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
                log.info("requestAssignedToUser: username = " + authentication.getName()
                        + ", requestId = " + requestId);
            } else if ("isPalgaUser".equals(permission)) {
                String requestId = (String)targetDomainObject;
                //Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
                log.info("isPalgaUser: username = " + authentication.getName()
                        + ", requestId = " + requestId);
            }
            return false;
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
        
    };
    
    @Override
    protected MethodSecurityExpressionHandler createExpressionHandler() {
        DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
        expressionHandler.setPermissionEvaluator(permissionEvaluator);
        return expressionHandler;
    }
    
}