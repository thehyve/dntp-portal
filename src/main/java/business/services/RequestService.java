/*
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import business.exceptions.*;
import business.models.*;
import business.security.UserAuthenticationToken;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.DelegationState;
import org.activiti.engine.task.IdentityLinkType;
import org.activiti.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import business.representation.LabRequestRepresentation;
import business.representation.RequestListRepresentation;
import business.representation.RequestRepresentation;
import business.representation.RequestStatus;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RequestService {

    public static final String CURRENT_PROCESS_VERSION = "dntp_request_005";

    Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private TaskService taskService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private LabRequestQueryService labRequestQueryService;

    @Autowired
    private RequestFormService requestFormService;

    @Autowired
    private RequestPropertiesService requestPropertiesService;

    @Autowired
    private MailService mailService;

    @Autowired
    private FileService fileService;

    @Autowired
    private UserService userService;

    @Autowired
    private ApprovalVoteRepository approvalVoteRepository;

    @Autowired
    private RequestQueryService requestQueryService;

    @PersistenceContext
    private EntityManager em;

    /**
     * Finds task. 
     * @param taskId
     * @return the task if it exists.
     * @throws business.exceptions.TaskNotFound
     */
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
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
     * Fetches the process instance ids of the requests that the user is allowed to see.
     * The query is based on the role of the user:
     * - palga: fetch all requests that are passed status 'Open' and reopened requests;
     * - scientific_council: fetch all requests that have council approval data associated with them
     *   (and hence, are currently in approval status or have been in such status);
     * - lab user, hub user: fetch all requests that have associated lab requests for labs that the user
     *   is associated with;
     * - requester: fetch all requests for which the user is the requester or is linked as pathologist
     *   or principal investigator.
     *
     * @param user the current user.
     * @return the list of process instance ids.
     */
    @Transactional(readOnly = true)
    public List<String> getProcessInstanceIdsForUser(User user) {
        List<String> processInstanceIds;

        if (user == null) {
            processInstanceIds = new ArrayList<>();
        } else if (user.isPalga()) {
            processInstanceIds = requestQueryService.getPalgaRequests();
        } else if (user.isScientificCouncilMember()) {
            Date start = new Date();
            List<HistoricTaskInstance> approvalTasks = historyService
                    .createHistoricTaskInstanceQuery()
                    .taskDefinitionKey("scientific_council_approval")
                    .list();
            processInstanceIds = new ArrayList<>();
            for (HistoricTaskInstance task: approvalTasks) {
                processInstanceIds.add(task.getProcessInstanceId());
            }
            Date end = new Date();
            log.info("Query for council member took {} ms.", (end.getTime() - start.getTime()));
        } else if (user.isLabUser() || user.isHubUser()) {
            Date start = new Date();
            List<LabRequestRepresentation> labRequests =
                    labRequestQueryService.findLabRequestsForLabUserOrHubUser(user, false);
            processInstanceIds = new ArrayList<>();
            for (LabRequestRepresentation labRequest: labRequests) {
                processInstanceIds.add(labRequest.getProcessInstanceId());
            }
            Date end = new Date();
            log.info("Query for lab or hub user took {} ms.", (end.getTime() - start.getTime()));
        } else {
            processInstanceIds = requestQueryService.getRequestsForRequesterByStatus(user, null);
        }
        return processInstanceIds;
    }

    /**
     * Completes <code>request_form</code> task, generates a
     * request number, and sends an email to the requester
     * for the request with processInstanceId <code>id</code>.
     * @param id the processInstanceId of the request.
     */
    public RequestProperties submitRequest(User requester, String id) {
        RequestProperties properties = requestPropertiesService.findByProcessInstanceId(id);

        Task task = getTaskByRequestId(id, "request_form");

        // validate form data
        {
            HistoricProcessInstance instance = getProcessInstance(id);
            RequestRepresentation request = new RequestRepresentation();
            requestFormService.transferData(instance, request, requester);

            ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
            Validator validator = factory.getValidator();

            Set<ConstraintViolation<RequestRepresentation>> requestConstraintViolations = validator.validate(request);
            if (!requestConstraintViolations.isEmpty()) {
                throw new InvalidRequest("Invalid request", requestConstraintViolations);
            }

            Set<ConstraintViolation<RequestProperties>> propertiesConstraintViolations = validator.validate(properties);
            if (!propertiesConstraintViolations.isEmpty()) {
                throw new InvalidRequest("Invalid request", propertiesConstraintViolations);
            }
        }

        if (task.getDelegationState()==DelegationState.PENDING) {
            taskService.resolveTask(task.getId());
        }
        taskService.complete(task.getId());

        properties.setRequestNumber(requestPropertiesService.getNewRequestNumber(properties));

        try {
            log.info("Sending agreement form link to requester: " + requester.getUsername());
            mailService.sendAgreementFormLink(requester.getUsername(), properties);
        } catch (EmailError e) {
            log.error("Email error while sending mail to requester: " + e.getMessage());
        }
        try {
            HistoricProcessInstance instance = getProcessInstance(id);
            RequestListRepresentation request = new RequestListRepresentation();
            requestFormService.transferBasicData(instance, request);
            log.info("Sending agreement form link to pathologist: " + request.getPathologistEmail());
            mailService.sendAgreementFormLink(request.getPathologistEmail(), properties);
        } catch (EmailError e) {
            log.error("Email error while sending mail to pathologist: " + e.getMessage());
        }

        return properties;
    }

    /**
     * Creates a clone of a request: a new request with a new process, based on the request properties
     * of the parent request. Also the attached files will be copied.
     * The action is only enabled in statuses {@link RequestStatus#LAB_REQUEST} and {@link RequestStatus#CLOSED}.
     * The cloned request will fast forward to status {@link RequestStatus#APPROVAL} with the approval variables
     * unchecked.
     *
     * The cloned request will be added to the list of 'additional requests' of the parent request.
     * The request number of the cloned request will be the request number of the parent with a suffix '-A<var>n</var>'
     * appended, where <var>n</var> is the index of the clone in the list of additional requests of the parent.
     *
     * @param user The current user that requests the clone (should be a Palga user).
     * @param parentId the process instance id of the parent request to clone.
     * @return a representation of the resulting clone request.
     * @throws InvalidActionInStatus iff the parent request is not in status {@link RequestStatus#LAB_REQUEST} or
     * {@link RequestStatus#CLOSED}.
     */
    public RequestRepresentation forkRequest(User user, String parentId) throws InvalidActionInStatus {
        HistoricProcessInstance parentInstance = getProcessInstance(parentId);
        RequestRepresentation parentRequest = new RequestRepresentation();
        requestFormService.transferData(parentInstance, parentRequest, user);
        if (parentRequest.getStatus() != RequestStatus.LAB_REQUEST &&
                parentRequest.getStatus() != RequestStatus.CLOSED) {
            throw new InvalidActionInStatus("Forking of requests not allowed in status " +
                    parentRequest.getStatus() + ".");
        }
        log.info("Forking request " + parentRequest.getRequestNumber() +
                " (requester: " + parentRequest.getRequesterId() + ", " +
                parentRequest.getRequesterEmail() + ")");

        // start new process instance
        Map<String, Object> values = new HashMap<>();
        values.put("initiator", parentRequest.getRequesterId());
        values.put("jump_to_review", Boolean.TRUE);


        ProcessInstance newInstance = runtimeService.startProcessInstanceByKey(
                CURRENT_PROCESS_VERSION, values);
        String childId = newInstance.getProcessInstanceId();
        log.info("New forked process instance started: " + childId);
        runtimeService.addUserIdentityLink(childId, parentRequest.getRequesterId(), IdentityLinkType.STARTER);

        HistoricProcessInstance childInstance = getProcessInstance(childId);
        // copy all request properties to the new instance.
        Map<String, Object> variables = requestFormService.transferFormData(
                parentRequest, childInstance, user);

        // Set certain variables to False, so the new request doesn't start off pre-approved and palga users can edit the values.
        variables.put("request_approved", Boolean.FALSE);
        variables.put("scientific_council_approved", Boolean.FALSE);

        // Requester and Lab are still approved though.
        variables.put("requester_lab_is_valid", Boolean.TRUE);
        variables.put("requester_is_valid", Boolean.TRUE);
        variables.put("requester_allowed", Boolean.TRUE);
        variables.put("contact_person_is_allowed", Boolean.TRUE);

        variables.put("skip_status_approval", Boolean.FALSE);
        runtimeService.setVariables(childId, variables);

        RequestProperties childProperties = requestPropertiesService.findByProcessInstanceId(childId);

        RequestProperties parentProperties = requestPropertiesService.findByProcessInstanceId(parentId);
        synchronized (parentProperties) {
            em.refresh(parentProperties, LockModeType.PESSIMISTIC_WRITE);
            // generate new request number
            String childRequestNumber = String.format("%s-A%d",
                    parentProperties.getRequestNumber(),
                    parentProperties.getChildren().size() + 1
            );
            log.info("Create child request with number: " + childRequestNumber);
            childProperties.setRequestNumber(childRequestNumber);
            // set link between parent and child request
            childProperties.setParent(parentProperties);
            parentProperties.getChildren().add(childProperties);
            em.persist(parentProperties);
            em.flush();
        }

        // set submit date to now
        childProperties.setDateSubmitted(new Date());
        // copy attachments
        for (File file: parentProperties.getRequestAttachments()) {
            File clone = fileService.clone(file);
            childProperties.getRequestAttachments().add(clone);
        }
        for (File file: parentProperties.getAgreementAttachments()) {
            File clone = fileService.clone(file);
            childProperties.getAgreementAttachments().add(clone);
        }
        for (File file: parentProperties.getMedicalEthicalCommiteeApprovalAttachments()) {
            File clone = fileService.clone(file);
            childProperties.getMedicalEthicalCommiteeApprovalAttachments().add(clone);
        }
        requestPropertiesService.save(childProperties);

        childInstance = getProcessInstance(childId);
        RequestRepresentation childRequest = new RequestRepresentation();
        requestFormService.transferData(childInstance, childRequest, null);
        return childRequest;
    }


    @Transactional(readOnly = true)
    public RequestListRepresentation getRequestData(String processInstanceId, User currentUser) {
        RequestListRepresentation request = requestFormService.getRequestListDataCached(processInstanceId);
        Task task = null;
        switch(request.getStatus()) {
            case REVIEW:
                task = findTaskByRequestId(processInstanceId, "palga_request_review");
                break;
            case APPROVAL:
                task = findTaskByRequestId(processInstanceId, "request_approval");
                request.setNumberOfApprovalVotes(approvalVoteRepository.countByProcessInstanceId(processInstanceId));
                break;
            case DATA_DELIVERY:
                task = findTaskByRequestId(processInstanceId, "data_delivery");
                break;
            case SELECTION_REVIEW:
                task = findTaskByRequestId(processInstanceId, "selection_review");
                break;
            default:
                break;
        }
        if (task != null) {
            request.setAssignee(task.getAssignee());
            if (task.getAssignee() != null && !task.getAssignee().isEmpty()) {
                Long assigneeId = null;
                try {
                    assigneeId = Long.valueOf(task.getAssignee());
                } catch (NumberFormatException e) {
                }
                if (assigneeId != null) {
                    User assignee = userService.findOneCached(assigneeId);
                    if (assignee != null) {
                        request.setAssigneeName(RequestFormService.getName(assignee));
                    }
                }
            }
        }

        if (currentUser.isPalga()) {
            request.setReviewStatus(requestPropertiesService.getRequestReviewStatus(processInstanceId));
        } else if (currentUser.isScientificCouncilMember()) {
            // fetch my vote
            RequestProperties properties = requestPropertiesService.findByProcessInstanceId(
                    processInstanceId);
            Map<Long, ApprovalVote> votes = properties.getApprovalVotes();
            if (votes.containsKey(currentUser.getId())) {
                request.setApprovalVote(votes.get(currentUser.getId()).getValue().name());
            }
        }
        return request;
    }

    public void delete(String username, String id) {
        log.info("Deleting process instance " + id);
        requestFormService.invalidateCacheEntry(id);
        if (runtimeService.createProcessInstanceQuery().processInstanceId(id).active().count() == 0) {
            log.warn("No process instance with id {}", id);
        } else {
            runtimeService.deleteProcessInstance(id, "Removed by user: " + username);
        }
        requestPropertiesService.delete(id);
    }

    public void deleteRequest(UserAuthenticationToken user, String id) {
        HistoricProcessInstance instance = this.getProcessInstance(id);
        RequestRepresentation request = new RequestRepresentation();
        requestFormService.transferData(instance, request, user.getUser());
        if (!request.getRequesterId().equals(user.getUser().getId().toString())) {
            throw new RequestNotFound();
        }
        if (request.getStatus() != RequestStatus.OPEN){
            throw new InvalidActionInStatus();
        }
        if (request.isReopenRequest()) {
            throw new InvalidActionInStatus("Removing not allowed for reopened requests.");
        }
        delete(user.getName(), id);
    }

}
