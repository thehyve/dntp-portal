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

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;
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
import org.springframework.core.io.InputStreamResource;
import org.springframework.format.datetime.DateFormatter;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import com.opencsv.CSVWriter;

import business.exceptions.EmailError;
import business.exceptions.FileDownloadError;
import business.exceptions.InvalidActionInStatus;
import business.exceptions.RequestNotFound;
import business.exceptions.TaskNotFound;
import business.exceptions.UserUnauthorised;
import business.models.File;
import business.models.RequestProperties;
import business.models.User;
import business.representation.LabRequestRepresentation;
import business.representation.RequestListRepresentation;
import business.representation.RequestRepresentation;
import business.representation.RequestStatus;

@Service
public class RequestService {

    public static final String CSV_CHARACTER_ENCODING = "UTF-8";

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

    @Autowired
    private FileService fileService;

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
    public List<HistoricProcessInstance> getProcessInstancesForUser(
            User user) {
        List<HistoricProcessInstance> processInstances;

        if (user == null) {
            processInstances = new ArrayList<HistoricProcessInstance>();
        } else if (user.isPalga()) {
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
        } else if (user.isScientificCouncilMember()) {
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
        } else if (user.isLabUser() || user.isHubUser()) {
            List<LabRequestRepresentation> labRequests =
                    labRequestService.findLabRequestsForLabUserOrHubUser(user, false);
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
            String userEmail = user.getUsername();
            log.info("Fetching requester requests for user:" + userEmail);
            processInstances = new ArrayList<HistoricProcessInstance>();
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
            processInstances.addAll(historyService
                    .createHistoricProcessInstanceQuery()
                    .notDeleted()
                    .includeProcessVariables()
                    .variableValueEquals("pathologist_email", user.getUsername())
                    .list());
            processInstances.addAll(historyService
                    .createHistoricProcessInstanceQuery()
                    .notDeleted()
                    .includeProcessVariables()
                    .variableValueEquals("contact_person_email", user.getUsername())
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
                values.add(request.getLab().getNumber().toString());
                values.add(request.getRequester().getSpecialism());
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
            HttpEntity<InputStreamResource> response =  new HttpEntity<InputStreamResource>(resource, headers);
            log.info("Returning reponse.");
            return response;
        } catch (IOException e) {
            log.error(e.getStackTrace());
            log.error(e.getMessage());
            throw new FileDownloadError();
        }

    }

}
