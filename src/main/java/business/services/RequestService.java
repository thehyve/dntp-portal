/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business.services;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import business.exceptions.*;
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
import org.springframework.core.io.InputStreamResource;
import org.springframework.format.datetime.DateFormatter;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import com.opencsv.CSVWriter;

import business.models.File;
import business.models.RequestProperties;
import business.models.User;
import business.representation.LabRequestRepresentation;
import business.representation.RequestListRepresentation;
import business.representation.RequestRepresentation;
import business.representation.RequestStatus;
import org.springframework.transaction.annotation.Transactional;


@Service
public class RequestService {

    public static final String CURRENT_PROCESS_VERSION = "dntp_request_005";

    public static final String CSV_CHARACTER_ENCODING = "UTF-8";

    Logger log = LoggerFactory.getLogger(getClass());

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
                    labRequestService.findLabRequestsForLabUserOrHubUser(user, false);
            processInstanceIds = new ArrayList<>();
            for (LabRequestRepresentation labRequest: labRequests) {
                processInstanceIds.add(labRequest.getProcessInstanceId());
            }
            Date end = new Date();
            log.info("Query for lab or hub user took {} ms.", (end.getTime() - start.getTime()));
        } else {
            Date start = new Date();
            String userEmail = user.getUsername();
            log.info("Fetching requester requests for user:" + userEmail);
            List<HistoricProcessInstance> processInstances = new ArrayList<>();
            for (HistoricProcessInstance instance: historyService
                    .createHistoricProcessInstanceQuery()
                    .notDeleted()
                    .includeProcessVariables()
                    .involvedUser(user.getId().toString())
                    .list()) {
                Map<String, Object> variables = instance.getProcessVariables();
                String pathologistEmail = (String)variables.get("pathologist_email");
                String contactPersonEmail = (String)variables.get("contact_person_email");
                if ((pathologistEmail == null || !pathologistEmail.equals(userEmail)) &&
                        (contactPersonEmail == null || !contactPersonEmail.equals(userEmail))) {
                    processInstances.add(instance);
                }
            }
            final Set<String> idSet1 = processInstances.stream().map(i -> i.getId()).collect(Collectors.toSet());
            processInstances.addAll(historyService
                    .createHistoricProcessInstanceQuery()
                    .notDeleted()
                    .includeProcessVariables()
                    .variableValueEquals("pathologist_email", user.getUsername())
                    .list()
                    .stream()
                    .filter(i -> !idSet1.contains(i.getId()))
                    .collect(Collectors.toList())
                    );
            final Set<String> idSet2 = processInstances.stream().map(i -> i.getId()).collect(Collectors.toSet());
            processInstances.addAll(historyService
                    .createHistoricProcessInstanceQuery()
                    .notDeleted()
                    .includeProcessVariables()
                    .variableValueEquals("contact_person_email", user.getUsername())
                    .list()
                    .stream()
                    .filter(i -> !idSet2.contains(i.getId()))
                    .collect(Collectors.toList())
                    );
            Date end = new Date();
            log.info("Query for requester took {} ms.", (end.getTime() - start.getTime()));
            processInstanceIds = processInstances.stream()
                    .map(HistoricProcessInstance::getId)
                    .collect(Collectors.toList());
        }
        return processInstanceIds;
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
     * 
     */
    @Transactional
    public RequestRepresentation forkRequest(User user, String parentId) {
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
        Map<String, Object> values = new HashMap<String, Object>();
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

    final static String[] CSV_COLUMN_NAMES = {
            "Request number",
            "Date created",
            "Title",
            "Status",
            "Linkage",
            "Linkage notes",
            "Numbers only",
            "Excerpts",
            "PA reports",
            "PA material",
            "Clinical data",
            "Requester name",
            "Requester lab",
            "Specialism",
            "# Hub assistance lab requests",
            "Pathologist"
    };

    final static Locale LOCALE = Locale.getDefault();

    final static DateFormatter DATE_FORMATTER = new DateFormatter("yyyy-MM-dd");

    final static String booleanToString(Boolean value) {
        if (value == null) return "";
        if (value.booleanValue()) {
            return "Yes";
        } else {
            return "No";
        }
    }

    public HttpEntity<InputStreamResource> writeRequestListCsv(List<RequestRepresentation> requests) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Writer writer = new OutputStreamWriter(out, CSV_CHARACTER_ENCODING);
            CSVWriter csvwriter = new CSVWriter(writer, ';', '"');
            csvwriter.writeNext(CSV_COLUMN_NAMES);

            for (RequestRepresentation request: requests) {
                List<String> values = new ArrayList<>();
                values.add(request.getRequestNumber());
                values.add(DATE_FORMATTER.print(request.getDateCreated(), LOCALE));
                values.add(request.getTitle());
                values.add(request.getStatus().toString());
                values.add(booleanToString(request.isLinkageWithPersonalData()));
                values.add(request.getLinkageWithPersonalDataNotes());
                values.add(booleanToString(request.isStatisticsRequest()));
                values.add(booleanToString(request.isExcerptsRequest()));
                values.add(booleanToString(request.isPaReportRequest()));
                values.add(booleanToString(request.isMaterialsRequest()));
                values.add(booleanToString(request.isClinicalDataRequest()));
                values.add(request.getRequesterName());
                values.add(request.getLab() == null ?
                        "" :
                        request.getLab().getNumber().toString() + ". " + request.getLab().getName()
                        );
                values.add(request.getRequester() == null ? "" : request.getRequester().getSpecialism());
                values.add(labRequestService.countHubAssistanceLabRequestsForRequest(
                        request.getProcessInstanceId()).toString());
                values.add(request.getPathologistName());
                csvwriter.writeNext(values.toArray(new String[]{}));
            }
            csvwriter.flush();
            writer.flush();
            out.flush();
            InputStream in = new ByteArrayInputStream(out.toByteArray());
            csvwriter.close();
            writer.close();
            out.close();
            InputStreamResource resource = new InputStreamResource(in);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.valueOf("text/csv"));
            String filename = "requests_" +
                    DATE_FORMATTER.print(new Date(), LOCALE) +
                    ".csv";
            headers.set("Content-Disposition",
                       "attachment; filename=" + filename);
            HttpEntity<InputStreamResource> response =  new HttpEntity<>(resource, headers);
            log.info("Returning response.");
            return response;
        } catch (IOException e) {
            log.error("Error writing to CSV.", e);
            throw new FileDownloadError();
        }

    }

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

}
