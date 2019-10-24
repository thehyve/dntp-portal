/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business.controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.DelegationState;
import org.activiti.engine.task.IdentityLinkType;
import org.activiti.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import business.exceptions.InvalidActionInStatus;
import business.exceptions.NotLoggedInException;
import business.exceptions.RequestNotAdmissible;
import business.exceptions.RequestNotApproved;
import business.models.RequestProperties;
import business.models.RequestProperties.ReviewStatus;
import business.representation.RequestListRepresentation;
import business.representation.RequestRepresentation;
import business.representation.RequestStatus;
import business.security.UserAuthenticationToken;
import business.services.MailService;
import business.services.RequestFormService;
import business.services.RequestNumberService;
import business.services.RequestPropertiesService;
import business.services.RequestService;

@RestController
public class RequestController {

    Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private RequestService requestService;

    @Autowired
    private RequestFormService requestFormService;
   
    @Autowired
    private MailService mailService;
    
    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private RequestPropertiesService requestPropertiesService;

    @Autowired
    private RequestNumberService requestNumberService;

    @Autowired
    private RequestListRepresentationComparator requestListRepresentationComparator;

    @Autowired
    private RequestListRepresentationStartTimeComparator requestListRepresentationStartTimeComparator;

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/requests", method = RequestMethod.GET)
    public List<RequestListRepresentation> getRequestList(UserAuthenticationToken user) {
        log.info(
                "GET /requests (for user: " + (user == null ? "null" : user.getId()) + ")");

        List<RequestListRepresentation> result = new ArrayList<RequestListRepresentation>();

        Date start = new Date();
        List<String> processInstanceIds = requestService.getProcessInstanceIdsForUser(user.getUser());
        for (String id : processInstanceIds) {
            RequestListRepresentation request = requestService.getRequestData(id, user.getUser());
            result.add(request);
        }
        Date endQ = new Date();
        log.info("GET: fetching took " + (endQ.getTime() - start.getTime()) + " ms.");
        if (user.getUser().isRequester()) {
            Collections.sort(result, Collections.reverseOrder(requestListRepresentationStartTimeComparator));
            Date endSort = new Date();
            log.info("GET: sorting took " + (endSort.getTime() - endQ.getTime()) + " ms.");
        } else if (user.getUser().isScientificCouncilMember()) {
            Collections.sort(result, Collections.reverseOrder(requestListRepresentationComparator));
            Date endSort = new Date();
            log.info("GET: sorting took " + (endSort.getTime() - endQ.getTime()) + " ms.");
        } else {
            Collections.sort(result, Collections.reverseOrder(requestListRepresentationComparator));
            Date endSort = new Date();
            log.info("GET: sorting took " + (endSort.getTime() - endQ.getTime()) + " ms.");
        }
        return result;
    }

    private void incrementCount(Map<String, Long> counts, String key) {
        Long count = counts.get(key);
        counts.put(key, ((count == null) ? 0 : count.longValue()) + 1);
    }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/requests/counts", method = RequestMethod.GET)
    public Map<String, Long> getRequestCounts(UserAuthenticationToken user) {
        log.info(
                "GET /requests/counts (for user: " + (user == null ? "null" : user.getId()) + ")");

        Date start = new Date();
        Map<String, Long> counts = new HashMap<>();
        List<String> processInstanceIds = requestService.getProcessInstanceIdsForUser(user.getUser());
        Date middle = new Date();
        log.info("Fetching process instances took " + (middle.getTime() - start.getTime()) + "ms.");
        Set<String> suspendedRequestIds = requestPropertiesService.getProcessInstanceIdsByReviewStatus(ReviewStatus.SUSPENDED);
        for (String id: processInstanceIds) {
            RequestListRepresentation request = requestFormService.getRequestListDataCached(id);
            if (user.getUser().isPalga() && suspendedRequestIds.contains(id)) {
                incrementCount(counts, "suspended");
            } else {
                incrementCount(counts, request.getStatus().toString());
            }
        }
        Date end = new Date();
        log.info("Counting took " + (end.getTime() - middle.getTime()) + " ms.");
        return counts;
    }

    @PreAuthorize("isAuthenticated() and ("
            + "     hasRole('palga')"
            + "  or hasPermission(#id, 'isRequestPathologist') "
            + "  or hasPermission(#id, 'isRequestContactPerson') "
            + "  or hasPermission(#id, 'isRequester') "
            + "  or hasPermission(#id, 'isScientificCouncil')"
            + "  or hasPermission(#id, 'isLabuser')"
            + "  or hasPermission(#id, 'isHubuser')"
            + ")")
    @RequestMapping(value = "/requests/{id}", method = RequestMethod.GET)
    public RequestRepresentation getRequestById(UserAuthenticationToken user,
                                                @PathVariable String id) {
        log.info(
                "GET /requests/" + id + " (for user: " + (user == null ? "null" : user.getId()) + ")");
        RequestRepresentation request = new RequestRepresentation();
        if (user == null) {
            throw new NotLoggedInException();
        } else {
            HistoricProcessInstance instance = requestService.getProcessInstance(id);
            requestFormService.transferData(instance, request, user.getUser());
        }

        return request;
    }

    @PreAuthorize("isAuthenticated() and hasRole('requester')")
    @RequestMapping(value = "/requests", method = RequestMethod.POST)
    public RequestRepresentation start(UserAuthenticationToken user) {
        String userId = user.getId().toString();
        log.info(
                "POST /requests (initiator: " + userId + ")");
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("initiator", userId);
        values.put("jump_to_review", Boolean.FALSE);
        values.put("skip_status_approval", Boolean.FALSE);

        ProcessInstance newInstance = runtimeService.startProcessInstanceByKey(
                RequestService.CURRENT_PROCESS_VERSION, values);
        runtimeService.addUserIdentityLink(newInstance.getId(), userId, IdentityLinkType.STARTER);

        HistoricProcessInstance instance = requestService.getProcessInstance(newInstance.getId());
        RequestRepresentation request = new RequestRepresentation();
        requestFormService.transferData(instance, request, null);
        return request;
    }

    @PreAuthorize("isAuthenticated() and hasRole('palga')")
    @RequestMapping(value = "/requests/{id}/forks", method = RequestMethod.POST)
    public RequestRepresentation fork(
            UserAuthenticationToken user,
            @PathVariable String id) {
        log.info("POST /requests/" + id + "/forks");
        return requestService.forkRequest(user.getUser(), id);
    }

    @PreAuthorize("isAuthenticated() and hasPermission(#id, 'requestAssignedToUser')")
    @RequestMapping(value = "/requests/{id}", method = RequestMethod.PUT)
    public RequestRepresentation update(
            UserAuthenticationToken user,
            @PathVariable String id,
            @RequestBody RequestRepresentation request) {
        log.info("PUT /requests/" + id);
        HistoricProcessInstance instance = requestService.getProcessInstance(id);
        Map<String, Object> variables = requestFormService.transferFormData(request, instance, user.getUser());

        runtimeService.setVariables(instance.getId(), variables);
        for (Entry<String, Object> entry: variables.entrySet()) {
            log.debug("PUT /processes/" + id + " set " + entry.getKey() + " = " + entry.getValue());
        }
        instance = requestService.getProcessInstance(id);
        RequestRepresentation updatedRequest = new RequestRepresentation();
        requestFormService.transferData(instance, updatedRequest, user.getUser());
        return updatedRequest;
    }

    @PreAuthorize("isAuthenticated() and hasPermission(#id, 'requestAssignedToUser')")
    @RequestMapping(value = "/requests/{id}/suspend", method = RequestMethod.PUT)
    public RequestRepresentation suspend(
            UserAuthenticationToken user,
            @PathVariable String id) {
        log.info("PUT /requests/" + id + "/suspend");

        requestPropertiesService.suspendRequest(id);

        requestFormService.invalidateCacheEntry(id);
        HistoricProcessInstance instance = requestService.getProcessInstance(id);
        RequestRepresentation updatedRequest = new RequestRepresentation();
        requestFormService.transferData(instance, updatedRequest, user.getUser());
        return updatedRequest;
    }

    @PreAuthorize("isAuthenticated() and hasPermission(#id, 'requestAssignedToUser')")
    @RequestMapping(value = "/requests/{id}/resume", method = RequestMethod.PUT)
    public RequestRepresentation resume(
            UserAuthenticationToken user,
            @PathVariable String id) {
        log.info("PUT /requests/" + id + "/resume");

        requestPropertiesService.resumeRequest(id);

        requestFormService.invalidateCacheEntry(id);
        HistoricProcessInstance instance = requestService.getProcessInstance(id);
        RequestRepresentation updatedRequest = new RequestRepresentation();
        requestFormService.transferData(instance, updatedRequest, user.getUser());
        return updatedRequest;
    }

    @PreAuthorize("isAuthenticated() and hasPermission(#id, 'requestAssignedToUser')")
    @RequestMapping(value = "/requests/{id}/submit", method = RequestMethod.PUT)
    public RequestRepresentation submit(
            UserAuthenticationToken user,
            @PathVariable String id,
            @RequestBody RequestRepresentation request) {
        log.info("PUT /requests/" + id + "/submit");
        HistoricProcessInstance instance = requestService.getProcessInstance(id);
        Map<String, Object> variables = requestFormService.transferFormData(request, instance, user.getUser());
        //FIXME: validation of the data
        runtimeService.setVariables(instance.getId(), variables);
        for (Entry<String, Object> entry: variables.entrySet()) {
            log.debug("PUT /requests/" + id + " set " + entry.getKey() + " = " + entry.getValue());
        }

        RequestProperties properties = requestService.submitRequest(user.getUser(), id);
        log.info("Request submitted. Request number: " + properties.getRequestNumber());

        instance = requestService.getProcessInstance(id);
        RequestRepresentation updatedRequest = new RequestRepresentation();
        requestFormService.transferData(instance, updatedRequest, user.getUser());
        return updatedRequest;
    }

    @PreAuthorize("isAuthenticated() and hasRole('palga')")
    @RequestMapping(value = "/requestNumbers/fix", method = RequestMethod.PUT)
    public void fixRequestNumbers() {
        log.info("PUT /requestNumbers/fix");
        requestNumberService.fixRequestNumbers();
    }

    @PreAuthorize("isAuthenticated() and hasRole('palga') and hasPermission(#id, 'requestAssignedToUser')")
    @RequestMapping(value = "/requests/{id}/submitReview", method = RequestMethod.PUT)
    public RequestRepresentation submitReview(
            UserAuthenticationToken user,
            @PathVariable String id,
            @RequestBody RequestRepresentation request) {
        log.info("PUT /requests/{}/submitReview", id);
        request.setRequestAdmissible(true);
        request.setReopenRequest(false);
        HistoricProcessInstance instance = requestService.getProcessInstance(id);
        Map<String, Object> variables = requestFormService.transferFormData(request, instance, user.getUser());
        runtimeService.setVariables(instance.getId(), variables);

        instance = requestService.getProcessInstance(id);
        RequestRepresentation updatedRequest = new RequestRepresentation();
        requestFormService.transferData(instance, updatedRequest, user.getUser());

        if (updatedRequest.isRequestAdmissible()) {
            Task task = requestService.getTaskByRequestId(id, "palga_request_review");
            if (task.getDelegationState()==DelegationState.PENDING) {
                taskService.resolveTask(task.getId());
            }
            taskService.complete(task.getId());
        } else {
            throw new RequestNotAdmissible();
        }

        requestService.claimCurrentPalgaTask(id, user.getUser());

        instance = requestService.getProcessInstance(id);
        updatedRequest = new RequestRepresentation();
        requestFormService.transferData(instance, updatedRequest, user.getUser());

        if (!updatedRequest.isSkipStatusApproval()) {
            mailService.notifyScientificCouncil(updatedRequest);
        } else {
            log.info("Skipping status 'Approval' for request {}", updatedRequest.getRequestNumber());
        }

        return updatedRequest;
    }

    @PreAuthorize("isAuthenticated() and hasRole('palga') and hasPermission(#id, 'requestAssignedToUser')")
    @RequestMapping(value = "/requests/{id}/finalise", method = RequestMethod.PUT)
    public RequestRepresentation finalise(
            UserAuthenticationToken user,
            @PathVariable String id,
            @RequestBody RequestRepresentation request) {
        log.info("PUT /requests/" + id + "/finalise");
        HistoricProcessInstance instance = requestService.getProcessInstance(id);
        Map<String, Object> variables = requestFormService.transferFormData(request, instance, user.getUser());
        runtimeService.setVariables(instance.getId(), variables);

        instance = requestService.getProcessInstance(id);
        RequestRepresentation updatedRequest = new RequestRepresentation();
        requestFormService.transferData(instance, updatedRequest, user.getUser());
        if (    updatedRequest.getPrivacyCommitteeRationale() == null ||
                updatedRequest.getPrivacyCommitteeRationale().isEmpty() ||
                !updatedRequest.isPrivacyCommitteeApproved() ||
                !updatedRequest.isScientificCouncilApproved()) {
            log.warn("Finalisation failed because of lacking approval.");
            throw new RequestNotApproved();
        }
        // marking request as approved
        updatedRequest.setRequestApproved(true);
        updatedRequest.setReopenRequest(false);
        variables = requestFormService.transferFormData(updatedRequest, instance, user.getUser());
        runtimeService.setVariables(instance.getId(), variables);

        Boolean requestApproved = runtimeService.getVariable(id, "request_approved", Boolean.class);
        log.info("Request approved: " + requestApproved);

        log.info("Fetching scientific_council_approval task");
        Task councilTask = requestService.getTaskByRequestId(id, "scientific_council_approval");
        if (councilTask.getDelegationState()==DelegationState.PENDING) {
            taskService.resolveTask(councilTask.getId());
        }
        taskService.complete(councilTask.getId());

        log.info("Fetching request_approval task");
        Task palgaTask = requestService.getTaskByRequestId(id, "request_approval");
        if (palgaTask.getDelegationState()==DelegationState.PENDING) {
            taskService.resolveTask(palgaTask.getId());
        }
        taskService.complete(palgaTask.getId());

        requestService.claimCurrentPalgaTask(id, user.getUser());

        instance = requestService.getProcessInstance(id);
        updatedRequest = new RequestRepresentation();
        requestFormService.transferData(instance, updatedRequest, user.getUser());

        return updatedRequest;
    }

    @PreAuthorize("isAuthenticated() and hasRole('palga') and hasPermission(#id, 'requestAssignedToUser')")
    @RequestMapping(value = "/requests/{id}/reopen", method = RequestMethod.PUT)
    public RequestRepresentation reopen(
            UserAuthenticationToken user,
            @PathVariable String id,
            @RequestBody RequestRepresentation request) {
        log.info("PUT /requests/" + id + "/reopen");

        requestPropertiesService.resumeRequest(id);

        request.setReopenRequest(true);
        HistoricProcessInstance instance = requestService.getProcessInstance(id);
        Map<String, Object> variables = requestFormService.transferFormData(request, instance, user.getUser());
        runtimeService.setVariables(instance.getId(), variables);

        instance = requestService.getProcessInstance(id);
        RequestRepresentation updatedRequest = new RequestRepresentation();
        requestFormService.transferData(instance, updatedRequest, user.getUser());

        if (updatedRequest.isReopenRequest()) {
            switch (updatedRequest.getStatus()) {
            case REVIEW:
                log.info("Completing review task...");
                Task task = requestService.getTaskByRequestId(id, "palga_request_review");
                if (task.getDelegationState()==DelegationState.PENDING) {
                    taskService.resolveTask(task.getId());
                }
                taskService.complete(task.getId());
                break;
            case APPROVAL:
                log.info("Completing scientific_council_approval task...");
                Task councilTask = requestService.getTaskByRequestId(id, "scientific_council_approval");
                if (councilTask.getDelegationState()==DelegationState.PENDING) {
                    taskService.resolveTask(councilTask.getId());
                }
                taskService.complete(councilTask.getId());

                log.info("Completing request_approval task...");
                Task palgaTask = requestService.getTaskByRequestId(id, "request_approval");
                if (palgaTask.getDelegationState()==DelegationState.PENDING) {
                    taskService.resolveTask(palgaTask.getId());
                }
                taskService.complete(palgaTask.getId());
                break;
            default:
                break;
            }
        } else {
            log.warn("'reopen_request' variable not saved correctly.");
        }

        instance = requestService.getProcessInstance(id);
        updatedRequest = new RequestRepresentation();
        requestFormService.transferData(instance, updatedRequest, user.getUser());

        return updatedRequest;
    }

    @PreAuthorize("isAuthenticated() and hasRole('palga') and hasPermission(#id, 'requestAssignedToUser')")
    @RequestMapping(value = "/requests/{id}/reject", method = RequestMethod.PUT)
    public RequestRepresentation reject(
            UserAuthenticationToken user,
            @PathVariable String id,
            @RequestBody RequestRepresentation body) {
        log.info("PUT /requests/" + id + "/reject");
        HistoricProcessInstance instance = requestService.getProcessInstance(id);

        requestPropertiesService.resumeRequest(id);

        RequestRepresentation request = new RequestRepresentation();
        requestFormService.transferData(instance, request, user.getUser());

        body.setReopenRequest(false);
        if (request.getStatus() == RequestStatus.REVIEW) {
            body.setRequestAdmissible(false);
        } else if (request.getStatus() == RequestStatus.APPROVAL) {
            body.setRequestApproved(false);
        }
        body.setRejectDate(new Date());
        Map<String, Object> variables = requestFormService.transferFormData(body, instance, user.getUser());
        runtimeService.setVariables(instance.getId(), variables);

        instance = requestService.getProcessInstance(id);
        RequestRepresentation updatedRequest = new RequestRepresentation();
        requestFormService.transferData(instance, updatedRequest, user.getUser());

        log.info("Reject request.");
        log.info("Reject reason: " + updatedRequest.getRejectReason());

        if (updatedRequest.getStatus() == RequestStatus.REVIEW) {
            log.info("Fetching palga_request_review task");
            Task palgaTask = requestService.getTaskByRequestId(id, "palga_request_review");
            if (palgaTask.getDelegationState()==DelegationState.PENDING) {
                taskService.resolveTask(palgaTask.getId());
            }
            taskService.complete(palgaTask.getId());

        } else if (updatedRequest.getStatus() == RequestStatus.APPROVAL) {
            log.info("Fetching scientific_council_approval task");
            Task councilTask = requestService.getTaskByRequestId(id, "scientific_council_approval");
            if (councilTask.getDelegationState()==DelegationState.PENDING) {
                taskService.resolveTask(councilTask.getId());
            }
            taskService.complete(councilTask.getId());

            log.info("Fetching request_approval task");
            Task palgaTask = requestService.getTaskByRequestId(id, "request_approval");
            if (palgaTask.getDelegationState()==DelegationState.PENDING) {
                taskService.resolveTask(palgaTask.getId());
            }
            taskService.complete(palgaTask.getId());
        }

        instance = requestService.getProcessInstance(id);
        updatedRequest = new RequestRepresentation();
        requestFormService.transferData(instance, updatedRequest, user.getUser());

        return updatedRequest;
    }

    @PreAuthorize("isAuthenticated() and hasRole('palga') and hasPermission(#id, 'requestAssignedToUser')")
    @RequestMapping(value = "/requests/{id}/close", method = RequestMethod.PUT)
    public RequestRepresentation close(
            UserAuthenticationToken user,
            @PathVariable String id,
            @RequestBody RequestRepresentation body) {
        log.info("PUT /requests/" + id + "/close");
        HistoricProcessInstance instance = requestService.getProcessInstance(id);

        RequestRepresentation request = new RequestRepresentation();
        requestFormService.transferData(instance, request, user.getUser());
        if (request.getStatus() != RequestStatus.DATA_DELIVERY) {
            throw new InvalidActionInStatus("Cannot close requests in status " + request.getStatus());
        }
        if (request.isPaReportRequest() || request.isMaterialsRequest() || request.isClinicalDataRequest()) {
            throw new InvalidActionInStatus("Cannot close requests for reports, material or clinical data.");
        }

        requestPropertiesService.resumeRequest(id);

        Map<String, Object> variables = requestFormService.transferFormData(body, instance, user.getUser());
        runtimeService.setVariables(instance.getId(), variables);

        Task task = requestService.getTaskByRequestId(id, "data_delivery");
        if (task.getDelegationState()==DelegationState.PENDING) {
            taskService.resolveTask(task.getId());
        }
        taskService.complete(task.getId());
        instance = requestService.getProcessInstance(id);
        RequestRepresentation updatedRequest = new RequestRepresentation();
        requestFormService.transferData(instance, updatedRequest, user.getUser());

        return updatedRequest;
    }

    @PreAuthorize("isAuthenticated() and hasRole('palga')")
    @RequestMapping(value = "/requests/{id}/claim", method = RequestMethod.PUT)
    public RequestRepresentation claim(
            UserAuthenticationToken user,
            @PathVariable String id,
            @RequestBody RequestRepresentation request) {
        log.info("PUT /requests/" + id + "/claim");
        HistoricProcessInstance instance = requestService.getProcessInstance(id);
        requestService.claimCurrentPalgaTask(id, user.getUser());
        Map<String, Object> variables = instance.getProcessVariables();
        if (variables != null) {
            variables.put("assigned_date", new Date());
        }
        runtimeService.setVariables(instance.getId(), variables);
        requestFormService.invalidateCacheEntry(id);
        instance = requestService.getProcessInstance(id);
        RequestRepresentation updatedRequest = new RequestRepresentation();
        requestFormService.transferData(instance, updatedRequest, user.getUser());
        if (updatedRequest.getAssignee() != null) {
            requestFormService.updateLastAssignee(instance.getId(), updatedRequest.getAssignee());
        }
        return updatedRequest;
    }

    @PreAuthorize("isAuthenticated() and hasRole('palga')")
    @RequestMapping(value = "/requests/{id}/unclaim", method = RequestMethod.PUT)
    public RequestRepresentation unclaim(
            UserAuthenticationToken user,
            @PathVariable String id,
            @RequestBody RequestRepresentation request) {
        log.info("PUT /requests/" + id + "/unclaim");
        HistoricProcessInstance instance = requestService.getProcessInstance(id);
        Task task = requestService.getCurrentPalgaTaskByRequestId(id);
        taskService.unclaim(task.getId());
        requestFormService.invalidateCacheEntry(id);
        instance = requestService.getProcessInstance(id);
        RequestRepresentation updatedRequest = new RequestRepresentation();
        requestFormService.transferData(instance, updatedRequest, user.getUser());
        return updatedRequest;
    }

    @PreAuthorize("isAuthenticated() and hasPermission(#id, 'isRequester')")
    @RequestMapping(value = "/requests/{id}", method = RequestMethod.DELETE)
    public void remove(
            UserAuthenticationToken user,
            @PathVariable String id) {
        log.info("DELETE /requests/" + id);
        requestService.deleteRequest(user, id);
        log.info("process instance " + id + " deleted.");
    }

}
