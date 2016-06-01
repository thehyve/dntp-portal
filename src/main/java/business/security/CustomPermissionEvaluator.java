/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business.security;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import business.exceptions.InvalidPermissionExpression;
import business.exceptions.NullIdentifier;
import business.models.User;

@Component
public class CustomPermissionEvaluator implements PermissionEvaluator {

    @Autowired
    CustomPermissionService permissionService;

    Log log = LogFactory.getLog(getClass());

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
     * <li><strong>isHubuser</strong>:
     *      Usage: {@code hasPermission(#id, 'isHubuser')}<br>
     *      Checks if the user is a hub user
     *      and if there is a task that is both associated with 
     *          the request with id {@code id}
     *          and with one of the hub labs of the user.
     * </li>
     * <li><strong>isLabRequestLabuser</strong>:
     *      Usage: {@code hasPermission(#labRequestId, 'isLabRequestLabuser')}<br>
     *      Checks if the user is a lab user and if the lab request with id {@code labRequestId}
     *      is associated with the lab of the user. 
     * </li>
     * <li><strong>isLabRequestHubuser</strong>:
     *      Usage: {@code hasPermission(#labRequestId, 'isLabRequestHubuser')}<br>
     *      Checks if the user is a hub user and if the lab request with id {@code labRequestId}
     *      is associated with one of the hub labs of the user. 
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
            permissionService.logDecision(permission.toString(), null, "", "DENIED (user not authenticated).");
            return false;
        }
        User user = (User)authentication.getPrincipal();
        permissionService.logDecision(permission.toString(), user, ((targetDomainObject==null) ? "" : targetDomainObject.toString()), "");
        if ("isAssignedToTask".equals(permission))
        {
            checkTargetDomainObjectNotNull(targetDomainObject);
            String taskId = (String)targetDomainObject;
            return permissionService.checkIsAssignedToTask(user, taskId);
        }
        else if ("requestAssignedToUser".equals(permission))
        {
            checkTargetDomainObjectNotNull(targetDomainObject);
            String requestId = (String)targetDomainObject;
            return permissionService.checkRequestAssignedToUser(user, requestId);
        }
        else if ("requestAssignedToUserAsPathologist".equals(permission))
        {
            checkTargetDomainObjectNotNull(targetDomainObject);
            String requestId = (String)targetDomainObject;
            return permissionService.checkRequestAssignedToUserAsPathologist(user, requestId);
        }
        else if ("labRequestAssignedToUser".equals(permission))
        {
            checkTargetDomainObjectNotNull(targetDomainObject);
            Long labRequestId = (Long)targetDomainObject;
            return permissionService.checkLabRequestAssignedToUser(user, labRequestId);
        }
        else if ("isPalgaUser".equals(permission))
        {
            String requestId = (String)targetDomainObject;
            if (user.isPalga()) {
                permissionService.logDecision("isPalgaUser", user, requestId, "OK.");
                return true;
            } else {
                permissionService.logDecision("isPalgaUser", user, requestId, "DENIED (not a Palga user).");
                return false;
            }
        }
        else if ("isRequester".equals(permission))
        {
            checkTargetDomainObjectNotNull(targetDomainObject);
            String requestId = (String)targetDomainObject;
            return permissionService.checkIsRequester(user, requestId);
        }
        else if ("isScientificCouncil".equals(permission))
        {
            checkTargetDomainObjectNotNull(targetDomainObject);
            String requestId = (String)targetDomainObject;
            return permissionService.checkIsScientificCouncil(user, requestId);
        }
        else if ("isLabuser".equals(permission)) 
        {
            checkTargetDomainObjectNotNull(targetDomainObject);
            String requestId = (String)targetDomainObject;
            return permissionService.checkIsLabuser(user, requestId);
        }
        else if ("isHubuser".equals(permission))
        {
            checkTargetDomainObjectNotNull(targetDomainObject);
            String requestId = (String)targetDomainObject;
            return permissionService.checkIsHubuser(user, requestId);
        }
        else if ("isLabRequestLabuser".equals(permission))
        {
            checkTargetDomainObjectNotNull(targetDomainObject);
            Long labRequestId = (Long)targetDomainObject;
            return permissionService.checkIsLabRequestLabuser(user, labRequestId);
        }
        else if ("isLabRequestHubuser".equals(permission))
        {
            checkTargetDomainObjectNotNull(targetDomainObject);
            Long labRequestId = (Long)targetDomainObject;
            return permissionService.checkIsLabRequestHubuser(user, labRequestId);
        }
        else if ("isLabRequestRequester".equals(permission))
        {
            checkTargetDomainObjectNotNull(targetDomainObject);
            Long labRequestId = (Long)targetDomainObject;
            return permissionService.checkIsLabRequestRequester(user, labRequestId);
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
