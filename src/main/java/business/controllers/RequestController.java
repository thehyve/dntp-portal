package business.controllers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Attachment;
import org.activiti.engine.task.DelegationState;
import org.activiti.engine.task.Task;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import business.models.ApprovalVote;
import business.models.Comment;
import business.models.CommentRepository;
import business.models.ExcerptEntry;
import business.models.ExcerptList;
import business.models.RequestProperties;
import business.models.RequestPropertiesService;
import business.models.Role;
import business.models.RoleRepository;
import business.models.User;
import business.models.UserRepository;
import business.representation.ApprovalVoteRepresentation;
import business.representation.AttachmentRepresentation;
import business.representation.CommentRepresentation;
import business.representation.ExcerptListRepresentation;
import business.representation.ProfileRepresentation;
import business.representation.RequestListRepresentation;
import business.representation.RequestRepresentation;
import business.security.UserAuthenticationToken;

@RestController
public class RequestController {

    Log log = LogFactory.getLog(getClass());

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private IdentityService identityService;

    @Autowired
    private FormService formService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RequestPropertiesService requestPropertiesService;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    JavaMailSender mailSender;

    @Value("${dntp.server-name}")
    String serverName;

    @Value("${dntp.server-port}")
    String serverPort;

    private boolean fetchBooleanVariable(String name, Map<String,Object> variables) {
        if (variables.get(name) != null) {
            return (boolean)variables.get(name);
        }
        return false;
    }

    private String getName(User user) {
        if (user == null) {
            return "";
        }
        return user.getFirstName()
                + (user.getFirstName() == null || user.getFirstName().isEmpty() || user.getLastName() == null
                || user.getLastName().isEmpty() ? "" :" ")
                + (user.getLastName() == null ? "" : user.getLastName());
    }

    private void transferData(ProcessInstance instance, RequestListRepresentation request, User currentUser) {

        request.setProcessInstanceId(instance.getProcessInstanceId());

        Map<String, Object> variables = instance.getProcessVariables();

        if (variables != null) {
            request.setTitle((String)variables.get("title"));
            request.setStatus((String)variables.get("status"));
            request.setDateCreated((Date)variables.get("date_created"));
            request.setRequesterId(variables.get("requester_id") == null ? "" : variables.get("requester_id").toString());
            Long userId = null;
            try { userId = Long.valueOf(request.getRequesterId()); }
            catch(NumberFormatException e) {}
            if (userId != null) {
                User user = userRepository.findOne(userId);
                if (user != null) {
                    request.setRequesterName(getName(user));
                }
            }

            Task task = null;
            switch(request.getStatus()) {
                case "Review":
                    task = findTaskByRequestId(instance.getId(), "palga_request_review");
                    break;
                case "Approval":
                    task = findTaskByRequestId(instance.getId(), "request_approval");
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
                        User assignee = userRepository.findOne(assigneeId);
                        if (assignee != null) {
                            request.setAssigneeName(getName(assignee));
                        }
                    }
                    request.setDateAssigned((Date)variables.get("assigned_date"));
                }
            }
        }
    }

    private void transferData(ProcessInstance instance, RequestRepresentation request, User currentUser) {
        boolean is_palga = currentUser == null ? false : currentUser.isPalga();
        boolean is_scientific_council = currentUser == null ? false : currentUser.isScientificCouncilMember();

        request.setProcessInstanceId(instance.getProcessInstanceId());
        request.setActivityId(instance.getActivityId());

        Map<String, Object> variables = instance.getProcessVariables();
        if (variables != null) {
            request.setStatus((String)variables.get("status"));
            request.setDateCreated((Date)variables.get("date_created"));
            request.setDateAssigned((Date)variables.get("assigned_date"));

            request.setContactPersonName((String)variables.get("contact_person_name"));
            request.setTitle((String)variables.get("title"));
            request.setResearchQuestion((String)variables.get("research_question"));
            request.setHypothesis((String) variables.get("hypothesis"));
            request.setMethods((String) variables.get("methods"));

            request.setStatisticsRequest(fetchBooleanVariable("is_statistics_request", variables));
            request.setExcerptsRequest(fetchBooleanVariable("is_excerpts_request", variables));
            request.setPaReportRequest(fetchBooleanVariable("is_pa_report_request", variables));
            request.setMaterialsRequest(fetchBooleanVariable("is_materials_request", variables));

            request.setLinkageWithPersonalData(fetchBooleanVariable("is_linkage_with_personal_data", variables));
            request.setLinkageWithPersonalDataNotes((String) variables.get("linkage_with_personal_data_notes"));
            request.setInformedConsent(fetchBooleanVariable("is_informed_consent", variables));
            request.setReasonUsingPersonalData((String) variables.get("reason_using_personal_data"));

            request.setReturnDate((Date)variables.get("return_date"));
            request.setRequesterId(variables.get("requester_id") == null ? "" : variables.get("requester_id").toString());
            Long userId = null;
            try { userId = Long.valueOf(request.getRequesterId()); }
            catch(NumberFormatException e) {}
            if (userId != null) {
                User user = userRepository.findOne(userId);
                if (user != null) {
                    request.setRequesterName(getName(user));
                    if (user.getContactData() != null) {
                        request.setRequesterEmail(user.getContactData().getEmail());
                    }
                    request.setRequester(new ProfileRepresentation(user));
                    request.setLab(user.getLab());
                }
            }
            Task task = null;
            switch(request.getStatus()) {
                case "Review":
                    task = findTaskByRequestId(instance.getId(), "palga_request_review");
                    break;
                case "Approval":
                    task = findTaskByRequestId(instance.getId(), "request_approval");
                    break;
                case "DataDelivery":
                    task = findTaskByRequestId(instance.getId(), "data_delivery"); 
                    break;
            }
            if (task != null) {
                request.setAssignee(task.getAssignee());
                if (task.getAssignee() != null && !task.getAssignee().isEmpty()) {
                    Long assigneeId = null;
                    try { assigneeId = Long.valueOf(task.getAssignee()); }
                    catch(NumberFormatException e) {}
                    if (assigneeId != null) {
                        User assignee = userRepository.findOne(assigneeId);
                        if (assignee != null) {
                            request.setAssigneeName(getName(assignee));
                        }
                    }
                }
            }
            List<Attachment> attachments = new ArrayList<Attachment>(); //taskService.getTaskAttachments(task.getId());
            List<HistoricTaskInstance> historicTasks = getHistoricTasksByRequestId(instance.getProcessInstanceId());
            for (HistoricTaskInstance historicTask: historicTasks) {
                List<Attachment> historicAttachments = taskService.getTaskAttachments(historicTask.getId());
                attachments.addAll(historicAttachments);
            }
            List<AttachmentRepresentation> requesterAttachments = new ArrayList<AttachmentRepresentation>();
            List<AttachmentRepresentation> agreementAttachments = new ArrayList<AttachmentRepresentation>();
            List<AttachmentRepresentation> dataAttachments = new ArrayList<AttachmentRepresentation>();
            RequestProperties properties = requestPropertiesService.findByProcessInstanceId(
                    instance.getProcessInstanceId());

            Set<String> agreementAttachmentIds = properties.getAgreementAttachmentIds();
            Set<String> dataAttachmentIds = properties.getDataAttachmentIds();
            for (Attachment attachment: attachments) {
                if (properties.getExcerptListAttachmentId() != null && 
                        properties.getExcerptListAttachmentId().equals(attachment.getId())) {
                    //
                } else if (agreementAttachmentIds.contains(attachment.getId())) {
                    agreementAttachments.add(new AttachmentRepresentation(attachment));
                } else if (dataAttachmentIds.contains(attachment.getId())) {
                    dataAttachments.add(new AttachmentRepresentation(attachment));
                } else {
                    requesterAttachments.add(new AttachmentRepresentation(attachment));
                }
            }
            request.setSentToPrivacyCommittee(properties.isSentToPrivacyCommittee());
            request.setPrivacyCommitteeOutcome(properties.getPrivacyCommitteeOutcome());
            request.setPrivacyCommitteeOutcomeRef(properties.getPrivacyCommitteeOutcomeRef());
            request.setPrivacyCommitteeEmails(properties.getPrivacyCommitteeEmails());
            
            request.setAttachments(requesterAttachments);
            if (is_palga) {
                request.setAgreementAttachments(agreementAttachments);
            }

            if (is_palga || is_scientific_council) {
                List<CommentRepresentation> comments = new ArrayList<CommentRepresentation>();
                for (Comment comment: properties.getComments()) {
                    comments.add(new CommentRepresentation(comment));
                }
                request.setComments(comments);

                List<CommentRepresentation> approvalComments = new ArrayList<CommentRepresentation>();
                for (Comment comment: properties.getApprovalComments()) {
                    approvalComments.add(new CommentRepresentation(comment));
                }
                request.setApprovalComments(approvalComments);

                Map<Long, ApprovalVoteRepresentation> approvalVotes = new HashMap<Long, ApprovalVoteRepresentation>();
                for (Entry<User, ApprovalVote> entry: properties.getApprovalVotes().entrySet()) {
                    approvalVotes.put(entry.getKey().getId(), new ApprovalVoteRepresentation(entry.getValue()));
                }
                request.setApprovalVotes(approvalVotes);
            }

            if (is_palga) {
                request.setRequesterValid(fetchBooleanVariable("requester_is_valid", variables));
                request.setRequesterAllowed(fetchBooleanVariable("requester_is_allowed", variables));
                request.setContactPersonAllowed(fetchBooleanVariable("contact_person_is_allowed", variables));
                request.setRequesterLabValid(fetchBooleanVariable("requester_lab_is_valid", variables));
                request.setAgreementReached(fetchBooleanVariable("agreement_reached", variables));
                
                request.setScientificCouncilApproved(fetchBooleanVariable("scientific_council_approved", variables));
                request.setPrivacyCommitteeApproved(fetchBooleanVariable("privacy_committee_approved", variables));
                
                request.setRequestApproved(fetchBooleanVariable("request_approved", variables));
                request.setRejectReason((String)variables.get("reject_reason"));
                request.setRejectDate((Date)variables.get("reject_date"));
            }

            request.setDataAttachments(dataAttachments);
            
            if (properties.getExcerptList() != null) {
                log.info("Set excerpt list.");
                request.setExcerptList(new ExcerptListRepresentation(properties.getExcerptList()));
            }
            log.info("Not setting excerpt list.");
        }
    }

    private Map<String, Object> transferFormData(RequestRepresentation request, ProcessInstance instance, User user) {
        boolean is_palga = user.isPalga();
        request.setProcessInstanceId(instance.getProcessInstanceId());
        Map<String, Object> variables = instance.getProcessVariables();
        if (variables != null) {
            variables.put("title", request.getTitle());
            variables.put("research_question", request.getResearchQuestion());
            variables.put("hypothesis", request.getHypothesis());
            variables.put("methods", request.getMethods());

            variables.put("is_statistics_request", (Boolean)request.isStatisticsRequest());
            variables.put("is_excerpts_request", (Boolean)request.isExcerptsRequest());
            variables.put("is_pa_report_request", (Boolean)request.isPaReportRequest());
            variables.put("is_materials_request", (Boolean)request.isMaterialsRequest());

            variables.put("is_linkage_with_personal_data", (Boolean)request.isLinkageWithPersonalData());
            variables.put("linkage_with_personal_data_notes", request.getLinkageWithPersonalDataNotes());
            variables.put("is_informed_consent", (Boolean)request.isInformedConsent());
            variables.put("reason_using_personal_data", request.getReasonUsingPersonalData());

            variables.put("return_date", request.getReturnDate());
            variables.put("contact_person_name", request.getContactPersonName());

            if (is_palga) {
                variables.put("requester_is_valid", (Boolean)request.isRequesterValid());
                variables.put("requester_is_allowed", (Boolean)request.isRequesterAllowed());
                variables.put("contact_person_is_allowed", (Boolean)request.isContactPersonAllowed());
                variables.put("requester_lab_is_valid", (Boolean)request.isRequesterLabValid());
                variables.put("agreement_reached", (Boolean)request.isAgreementReached());
                
                variables.put("scientific_council_approved", (Boolean)request.isScientificCouncilApproved());
                variables.put("privacy_committee_approved", (Boolean)request.isPrivacyCommitteeApproved());
                
                variables.put("request_approved", (Boolean)request.isRequestApproved());
                variables.put("reject_reason", request.getRejectReason());
                variables.put("reject_date", request.getRejectDate());
                
                if (request.isRequesterValid()
                        && request.isRequesterAllowed()
                        && request.isContactPersonAllowed()
                        && request.isRequesterLabValid()
                        && request.isAgreementReached()) {
                    variables.put("request_is_admissible", Boolean.TRUE);
                } else {
                    variables.put("request_is_admissible", Boolean.FALSE);
                }
                RequestProperties properties = requestPropertiesService.findByProcessInstanceId(instance.getProcessInstanceId());
                properties.setSentToPrivacyCommittee(request.isSentToPrivacyCommittee());
                properties.setPrivacyCommitteeOutcome(request.getPrivacyCommitteeOutcome());
                properties.setPrivacyCommitteeOutcomeRef(request.getPrivacyCommitteeOutcomeRef());
                properties.setPrivacyCommitteeEmails(request.getPrivacyCommitteeEmails());
                requestPropertiesService.save(properties);
            }
        }
        return variables;
    }

    @RequestMapping(value = "/completerequests", method = RequestMethod.GET)
    public List<RequestRepresentation> get(UserAuthenticationToken user) {
        Date start = new Date();
        log.info(
                "GET /completerequests/ (for user: " + (user == null ? "null" : user.getId()) + ")");
        List<ProcessInstance> processInstances;
        if (user == null) {
            processInstances = new ArrayList<ProcessInstance>();
        } else if (user.getUser().isPalga()) {
            processInstances = new ArrayList<ProcessInstance>();
            processInstances.addAll(runtimeService
                    .createProcessInstanceQuery()
                    .includeProcessVariables()
                    .variableValueEquals("status", "Review")
                    .list());
            processInstances.addAll(runtimeService
                    .createProcessInstanceQuery()
                    .includeProcessVariables()
                    .variableValueEquals("status", "Approval")
                    .list());
            processInstances.addAll(runtimeService
                    .createProcessInstanceQuery()
                    .includeProcessVariables()
                    .variableValueEquals("status", "DataDelivery")
                    .list());
            processInstances.addAll(runtimeService
                    .createProcessInstanceQuery()
                    .includeProcessVariables()
                    .variableValueEquals("status", "Rejected")
                    .list());
            processInstances.addAll(runtimeService
                    .createProcessInstanceQuery()
                    .includeProcessVariables()
                    .variableValueEquals("status", "Closed")
                    .list());
        } else if (user.getUser().isScientificCouncilMember()) {
            processInstances = runtimeService
                    .createProcessInstanceQuery()
                    .includeProcessVariables()
                    .variableValueEquals("status", "Approval")
                    .list();
        } else {
            processInstances = runtimeService
                .createProcessInstanceQuery()
                .includeProcessVariables()
                .involvedUser(user.getId().toString())
                .list();
        }
        Date queryEnd = new Date();

        List<RequestRepresentation> result = new ArrayList<RequestRepresentation>();
        for (ProcessInstance instance : processInstances) {
            RequestRepresentation request = new RequestRepresentation();
            transferData(instance, request, user.getUser());
            result.add(request);
        }
        Date representationEnd = new Date();
        log.info("GET: query took " + (queryEnd.getTime() - start.getTime()) + " ms.");
        log.info("GET: representations took " + (representationEnd.getTime() - queryEnd.getTime()) + " ms.");
        return result;
    }

    @RequestMapping(value = "requests", method = RequestMethod.GET)
    public List<RequestListRepresentation> getRequestList(UserAuthenticationToken user) {
        log.info(
                "GET /requests/ (for user: " + (user == null ? "null" : user.getId()) + ")");

        List<ProcessInstance> processInstances;

        if (user == null) {
            processInstances = new ArrayList<ProcessInstance>();
        } else if (user.getUser().isPalga()) {
            processInstances = new ArrayList<ProcessInstance>();
            processInstances.addAll(runtimeService
                    .createProcessInstanceQuery()
                    .includeProcessVariables()
                    .variableValueEquals("status", "Review")
                    .list());
            processInstances.addAll(runtimeService
                    .createProcessInstanceQuery()
                    .includeProcessVariables()
                    .variableValueEquals("status", "Approval")
                    .list());
            processInstances.addAll(runtimeService
                    .createProcessInstanceQuery()
                    .includeProcessVariables()
                    .variableValueEquals("status", "DataDelivery")
                    .list());
            processInstances.addAll(runtimeService
                    .createProcessInstanceQuery()
                    .includeProcessVariables()
                    .variableValueEquals("status", "Rejected")
                    .list());
            processInstances.addAll(runtimeService
                    .createProcessInstanceQuery()
                    .includeProcessVariables()
                    .variableValueEquals("status", "Closed")
                    .list());
        } else if (user.getUser().isScientificCouncilMember()) {
            processInstances = runtimeService
                    .createProcessInstanceQuery()
                    .includeProcessVariables()
                    .variableValueEquals("status", "Approval")
                    .list();
        } else {
            processInstances = runtimeService
                .createProcessInstanceQuery()
                .includeProcessVariables()
                .involvedUser(user.getId().toString())
                .list();
        }

        List<RequestListRepresentation> result = new ArrayList<RequestListRepresentation>();

        for (ProcessInstance instance : processInstances) {
            RequestListRepresentation request = new RequestListRepresentation();
            transferData(instance, request, user.getUser());
            result.add(request);
        }
        return result;
    }

    @RequestMapping(value = "/requests/{id}", method = RequestMethod.GET)
    public RequestRepresentation getRequestById(UserAuthenticationToken user,
                                                @PathVariable String id) {
        log.info(
                "GET /requests/{" + id + "} (for user: " + (user == null ? "null" : user.getId()) + ")");
        RequestRepresentation request = new RequestRepresentation();
        if (user == null) {
            throw new NotLoggedInException();
        } else {
            ProcessInstance instance = getProcessInstance(id);
            transferData(instance, request, user.getUser());
        }

        return request;
    }


    @ResponseStatus(value=HttpStatus.UNAUTHORIZED, reason="Not logged in.")
    public class NotLoggedInException extends RuntimeException {
        private static final long serialVersionUID = -2361055636793206513L;
    }


    @RequestMapping(value = "/requests", method = RequestMethod.POST)
    public RequestRepresentation start(
            UserAuthenticationToken user,
            @RequestBody RequestRepresentation req) {
        if (user == null) {
            throw new NotLoggedInException();
        } else {
            String userId = user.getId().toString();
            log.info(
                    "POST /requests (initiator: " + userId + ")");
            Map<String, Object> values = new HashMap<String, Object>();
            values.put("initiator", userId);

            ProcessInstance instance = runtimeService.startProcessInstanceByKey(
                    "dntp_request_001", values);

            instance = runtimeService.createProcessInstanceQuery()
                    .includeProcessVariables()
                    .processInstanceId(instance.getId()).singleResult();
            RequestRepresentation request = new RequestRepresentation();
            transferData(instance, request, null);
            return request;
        }
    }

    @Secured("hasPermission(#param, 'requestAssignedToUser')")
    @RequestMapping(value = "/requests/{id}", method = RequestMethod.PUT)
    public RequestRepresentation update(
            UserAuthenticationToken user,
            @PathVariable String id,
            @RequestBody RequestRepresentation request) {
        log.info("PUT /requests/" + id);
        ProcessInstance instance = getProcessInstance(id);
        Map<String, Object> variables = transferFormData(request, instance, user.getUser());

        runtimeService.setVariables(instance.getProcessInstanceId(), variables);
        for (Entry<String, Object> entry: variables.entrySet()) {
            log.info("PUT /processes/" + id + " set " + entry.getKey() + " = " + entry.getValue());
        }
        instance = getProcessInstance(id);
        RequestRepresentation updatedRequest = new RequestRepresentation();
        transferData(instance, updatedRequest, user.getUser());
        return updatedRequest;
    }

    @ResponseStatus(value=HttpStatus.NOT_FOUND, reason="Request not found.")  // 404
    public class RequestNotFound extends RuntimeException {
        private static final long serialVersionUID = 607177856129334391L;
    }

    @ResponseStatus(value=HttpStatus.NOT_FOUND, reason="No task for request.")  // 404
    public class TaskNotFound extends RuntimeException {
        private static final long serialVersionUID = -2361055636793206513L;
    }

    /**
     * Finds current task. Assumes that exactly one task is currently active.
     * @param requestId
     * @return the current task if it exists.
     */
    List<HistoricTaskInstance> getHistoricTasksByRequestId(String requestId) {
        return historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(requestId)
                .orderByHistoricTaskInstanceEndTime()
                .desc()
                .list();
    }

    /**
     * Finds current task. Assumes that exactly one task is currently active.
     * @param requestId
     * @return the current task if it exists.
     * @throws business.controllers.RequestController.TaskNotFound
     */
    Task getTaskByRequestId(String requestId, String taskDefinition) {
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
    Task findTaskByRequestId(String requestId, String taskDefinition) {
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
     * @throws business.controllers.RequestController.TaskNotFound
     */
    Task getCurrentPalgaTaskByRequestId(String requestId) {
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
    Task findCurrentPalgaTaskByRequestId(String requestId) {
        Task task = findTaskByRequestId(requestId, "palga_request_review");
        if (task == null) {
            task = findTaskByRequestId(requestId, "request_approval");
        }
        if (task == null) {
            task = findTaskByRequestId(requestId, "data_delivery");
        }
        return task;
    }
    
    /**
     * Finds request.
     * @param processInstanceId
     * @return the current request if it exists; null otherwise.
     */
    ProcessInstance findProcessInstance(String processInstanceId) {
        ProcessInstance instance = runtimeService.createProcessInstanceQuery()
                .includeProcessVariables()
                .processInstanceId(processInstanceId).singleResult();
        return instance;
    }

    /**
     * Finds request.
     * @param processInstanceId
     * @return the current request if it exists.
     * @throws
     */
    ProcessInstance getProcessInstance(String processInstanceId) {
        ProcessInstance instance = runtimeService.createProcessInstanceQuery()
                .includeProcessVariables()
                .processInstanceId(processInstanceId).singleResult();
        if (instance == null) {
            throw new RequestNotFound();
        }
        return instance;
    }

    @Secured("hasPermission(#param, 'requestAssignedToUser')")
    @RequestMapping(value = "/requests/{id}/submit", method = RequestMethod.PUT)
    public RequestRepresentation submit(
            UserAuthenticationToken user,
            @PathVariable String id,
            @RequestBody RequestRepresentation request) {
        log.info("PUT /requests/" + id + "/submit");
        ProcessInstance instance = getProcessInstance(id);
        Map<String, Object> variables = transferFormData(request, instance, user.getUser());
        runtimeService.setVariables(instance.getProcessInstanceId(), variables);
        for (Entry<String, Object> entry: variables.entrySet()) {
            log.info("PUT /requests/" + id + " set " + entry.getKey() + " = " + entry.getValue());
        }

        Task task = getTaskByRequestId(id, "request_form");
        if (task.getDelegationState()==DelegationState.PENDING) {
            taskService.resolveTask(task.getId());
        }

        taskService.complete(task.getId());
        instance = getProcessInstance(id);
        RequestRepresentation updatedRequest = new RequestRepresentation();
        transferData(instance, updatedRequest, user.getUser());
        return updatedRequest;
    }

    private void notifyScientificCouncil(RequestRepresentation request) {
        log.info("Notify scientic council for request " + request.getProcessInstanceId() + ".");

        Role role = roleRepository.findByName("scientific_council");
        Set<User> members = role.getUsers();
        for (User member: members) {
            log.info("Sending notification to user " + member.getUsername());
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(member.getContactData().getEmail());
            message.setFrom("no-reply@dntp.thehyve.nl");
            message.setReplyTo("no-reply@dntp.thehyve.nl");
            message.setSubject("[DNTP portal] New request open for approval.");
            String template =
                    "Request: %s\n"
                +   "Requester: %s\n"
                +   "Principal Investigator: %s\n"
                +   "Title: %s\n"
                +   "\nResearch Question:\n%s\n"
                +   "\nHypothesis:\n%s\n"
                +   "\nMethods:\n%s\n"
                ;
            String body = String.format(template,
                    request.getProcessInstanceId(),
                    request.getRequesterName(),
                    request.getContactPersonName(),
                    request.getTitle(),
                    request.getResearchQuestion(),
                    request.getHypothesis(),
                    request.getMethods()
                    );
            message.setText(String.format(
                    ""
                    + "Please follow this link to view the new request: http://%s:%s/#/request/view/%s.\n"
                    + "====\n"
                    + body,
                    serverName, serverPort, request.getProcessInstanceId()));
            log.info("Mail contents:\n" + message.getText());
            mailSender.send(message);
        }
    }


    @Secured("hasPermission(#param, 'isPalgaUser')")
    @RequestMapping(value = "/requests/{id}/submitForApproval", method = RequestMethod.PUT)
    public RequestRepresentation submitForApproval(
            UserAuthenticationToken user,
            @PathVariable String id,
            @RequestBody RequestRepresentation request) {
        log.info("PUT /requests/" + id + "/submitForApproval");
        ProcessInstance instance = getProcessInstance(id);
        Map<String, Object> variables = transferFormData(request, instance, user.getUser());
        runtimeService.setVariables(instance.getProcessInstanceId(), variables);

        Task task = getTaskByRequestId(id, "palga_request_review");
        if (task.getDelegationState()==DelegationState.PENDING) {
            taskService.resolveTask(task.getId());
        }
        taskService.complete(task.getId());
        instance = getProcessInstance(id);
        RequestRepresentation updatedRequest = new RequestRepresentation();
        transferData(instance, updatedRequest, user.getUser());

        notifyScientificCouncil(updatedRequest);

        return updatedRequest;
    }

    @Secured("hasPermission(#param, 'isPalgaUser')")
    @RequestMapping(value = "/requests/{id}/finalise", method = RequestMethod.PUT)
    public RequestRepresentation finalise(
            UserAuthenticationToken user,
            @PathVariable String id,
            @RequestBody RequestRepresentation request) {
        log.info("PUT /requests/" + id + "/finalise");
        ProcessInstance instance = getProcessInstance(id);
        
        Map<String, Object> variables = transferFormData(request, instance, user.getUser());
        runtimeService.setVariables(instance.getProcessInstanceId(), variables);

        instance = getProcessInstance(id);
        RequestRepresentation updatedRequest = new RequestRepresentation();
        transferData(instance, updatedRequest, user.getUser());
        if (updatedRequest.isPrivacyCommitteeApproved() && 
                updatedRequest.isScientificCouncilApproved()) {
            // marking request as approved
            updatedRequest.setRequestApproved(true);
            variables = transferFormData(updatedRequest, instance, user.getUser());
            runtimeService.setVariables(instance.getProcessInstanceId(), variables);
        
            log.info("Fetching scientific_council_approval task");
            Task councilTask = getTaskByRequestId(id, "scientific_council_approval");
            if (councilTask.getDelegationState()==DelegationState.PENDING) {
                taskService.resolveTask(councilTask.getId());
            }
            taskService.complete(councilTask.getId());
    
            log.info("Fetching request_approval task");
            Task palgaTask = getTaskByRequestId(id, "request_approval");
            if (palgaTask.getDelegationState()==DelegationState.PENDING) {
                taskService.resolveTask(palgaTask.getId());
            }
            taskService.complete(palgaTask.getId());
        } else {
            log.warn("Finalisation failed because of lacking approval.");
        }

        instance = getProcessInstance(id);
        updatedRequest = new RequestRepresentation();
        transferData(instance, updatedRequest, user.getUser());

        return updatedRequest;
    }

    @Secured("hasPermission(#param, 'isPalgaUser')")
    @RequestMapping(value = "/requests/{id}/reject", method = RequestMethod.PUT)
    public RequestRepresentation reject(
            UserAuthenticationToken user,
            @PathVariable String id,
            @RequestBody RequestRepresentation request) {
        log.info("PUT /requests/" + id + "/reject");
        ProcessInstance instance = getProcessInstance(id);

        request.setRequestApproved(false);
        request.setRejectDate(new Date());
        Map<String, Object> variables = transferFormData(request, instance, user.getUser());
        runtimeService.setVariables(instance.getProcessInstanceId(), variables);

        instance = getProcessInstance(id);
        RequestRepresentation updatedRequest = new RequestRepresentation();
        transferData(instance, updatedRequest, user.getUser());

        log.info("Reject request.");
        log.info("Reject reason: " + updatedRequest.getRejectReason());
    
        log.info("Fetching scientific_council_approval task");
        Task councilTask = getTaskByRequestId(id, "scientific_council_approval");
        if (councilTask.getDelegationState()==DelegationState.PENDING) {
            taskService.resolveTask(councilTask.getId());
        }
        taskService.complete(councilTask.getId());

        log.info("Fetching request_approval task");
        Task palgaTask = getTaskByRequestId(id, "request_approval");
        if (palgaTask.getDelegationState()==DelegationState.PENDING) {
            taskService.resolveTask(palgaTask.getId());
        }
        taskService.complete(palgaTask.getId());

        instance = getProcessInstance(id);
        updatedRequest = new RequestRepresentation();
        transferData(instance, updatedRequest, user.getUser());

        return updatedRequest;
    }
    
    @Secured("hasPermission(#param, 'isPalgaUser')")
    @RequestMapping(value = "/requests/{id}/claim", method = RequestMethod.PUT)
    public RequestRepresentation claim(
            UserAuthenticationToken user,
            @PathVariable String id,
            @RequestBody RequestRepresentation request) {
        log.info("PUT /requests/" + id + "/claim");
        ProcessInstance instance = getProcessInstance(id);
        Task task = getCurrentPalgaTaskByRequestId(id);
        if (task.getAssignee() == null || task.getAssignee().isEmpty()) {
            taskService.claim(task.getId(), user.getId().toString());
        } else {
            taskService.delegateTask(task.getId(), user.getId().toString());
        }
        Map<String, Object> variables = instance.getProcessVariables();
        if (variables != null) {
            variables.put("assigned_date", new Date());
        }
        runtimeService.setVariables(instance.getProcessInstanceId(), variables);
        instance = getProcessInstance(id);
        RequestRepresentation updatedRequest = new RequestRepresentation();
        transferData(instance, updatedRequest, user.getUser());
        return updatedRequest;
    }

    @Secured("hasPermission(#param, 'isPalgaUser')")
    @RequestMapping(value = "/requests/{id}/unclaim", method = RequestMethod.PUT)
    public RequestRepresentation unclaim(
            UserAuthenticationToken user,
            @PathVariable String id,
            @RequestBody RequestRepresentation request) {
        log.info("PUT /requests/" + id + "/unclaim");
        ProcessInstance instance = getProcessInstance(id);
        Task task = getCurrentPalgaTaskByRequestId(id);
        taskService.unclaim(task.getId());
        instance = getProcessInstance(id);
        RequestRepresentation updatedRequest = new RequestRepresentation();
        transferData(instance, updatedRequest, user.getUser());
        return updatedRequest;
    }

    @ResponseStatus(value=HttpStatus.METHOD_NOT_ALLOWED, reason="Action not allowed in current status.")
    public class InvalidActionInStatus extends RuntimeException {
        private static final long serialVersionUID = 607177856129334391L;
    }

    @Secured("hasPermission(#param, 'requestAssignedToUser')")
    @RequestMapping(value = "/requests/{id}", method = RequestMethod.DELETE)
    public void remove(
            UserAuthenticationToken user,
            @PathVariable String id) {
        log.info("DELETE /requests/" + id);
        ProcessInstance instance = getProcessInstance(id);
        RequestRepresentation request = new RequestRepresentation();
        transferData(instance, request, user.getUser());
        if (!request.getRequesterId().equals(user.getUser().getId().toString())) {
            throw new RequestNotFound();
        }
        if (!request.getStatus().equals("Open")) {
            throw new InvalidActionInStatus();
        }
        runtimeService.deleteProcessInstance(id, "Removed by user: " + user.getName());
    }

    @ResponseStatus(value=HttpStatus.INTERNAL_SERVER_ERROR, reason="File upload error.")
    public class FileUploadError extends RuntimeException {
        private static final long serialVersionUID = 51403280891772531L;
        public FileUploadError() {
            super("File upload error.");
        }
        
        public FileUploadError(String message) {
            super("File upload error: " + message);
        }
    }

    @RequestMapping(value = "/requests/{id}/files", method = RequestMethod.POST)
    public RequestRepresentation uploadFile(UserAuthenticationToken user, @PathVariable String id,
            @RequestParam("flowFilename") String name,
            @RequestParam("file") MultipartFile file) {
        log.info("POST /requests/" + id + "/files");
        Task task = getTaskByRequestId(id, "request_form");
        try{
            taskService.createAttachment(
                    file.getContentType(),
                    task.getId(), task.getProcessInstanceId(),
                    name, name, file.getInputStream());
        } catch(IOException e) {
            throw new FileUploadError();
        }
        ProcessInstance instance = getProcessInstance(id);
        RequestRepresentation request = new RequestRepresentation();
        transferData(instance, request, user.getUser());
        return request;
    }

    @Secured("hasPermission(#param, 'isPalgaUser')")
    @RequestMapping(value = "/requests/{id}/agreementFiles", method = RequestMethod.POST)
    public RequestRepresentation uploadAgreementAttachment(UserAuthenticationToken user, @PathVariable String id,
            @RequestParam("flowFilename") String name,
            @RequestParam("file") MultipartFile file) {
        log.info("POST /requests/" + id + "/agreementFiles");
        Task task = getTaskByRequestId(id, "palga_request_review");
        String attachmentId;
        try{
            Attachment result = taskService.createAttachment(
                    file.getContentType(),
                    task.getId(), task.getProcessInstanceId(),
                    name, name, file.getInputStream());
            attachmentId = result.getId();
        } catch(IOException e) {
            throw new FileUploadError();
        }
        ProcessInstance instance = getProcessInstance(id);
        RequestRepresentation request = new RequestRepresentation();
        transferData(instance, request, user.getUser());

        // add attachment id to the set of ids of the agreement attachments.
        RequestProperties properties = requestPropertiesService.findByProcessInstanceId(id);
        if (properties == null) {
            properties = new RequestProperties();
            properties.setProcessInstanceId(id);
        }
        properties.getAgreementAttachmentIds().add(attachmentId);
        requestPropertiesService.save(properties);

        //Map<String, Object> variables = transferFormData(request, instance, user.getUser());
        //runtimeService.setVariables(instance.getProcessInstanceId(), variables);
        instance = getProcessInstance(id);
        request = new RequestRepresentation();
        transferData(instance, request, user.getUser());
        return request;
    }
    
    @Secured("hasPermission(#param, 'isPalgaUser')")
    @RequestMapping(value = "/requests/{id}/agreementFiles/{attachmentId}", method = RequestMethod.DELETE)
    public RequestRepresentation removeAgreementAttachment(UserAuthenticationToken user, @PathVariable String id,
            @PathVariable String attachmentId) {
        log.info("DELETE /requests/" + id + "/agreementFiles/" + attachmentId);

        ProcessInstance instance = getProcessInstance(id);

        // remove existing agreement.
        RequestProperties properties = requestPropertiesService.findByProcessInstanceId(id);
        if (properties != null && properties.getAgreementAttachmentIds().contains(attachmentId)) {
            taskService.deleteAttachment(attachmentId);
            properties.getAgreementAttachmentIds().remove(attachmentId);
            requestPropertiesService.save(properties);
        }

        instance = getProcessInstance(id);
        RequestRepresentation request = new RequestRepresentation();
        transferData(instance, request, user.getUser());
        return request;
    }

    @Secured("hasPermission(#param, 'isPalgaUser')")
    @RequestMapping(value = "/requests/{id}/dataFiles", method = RequestMethod.POST)
    public RequestRepresentation uploadDataAttachment(UserAuthenticationToken user, @PathVariable String id,
            @RequestParam("flowFilename") String name,
            @RequestParam("file") MultipartFile file) {
        log.info("POST /requests/" + id + "/dataFiles");
        Task task = getTaskByRequestId(id, "data_delivery");
        String attachmentId;
        try{
            Attachment result = taskService.createAttachment(
                    file.getContentType(),
                    task.getId(), task.getProcessInstanceId(),
                    name, name, file.getInputStream());
            attachmentId = result.getId();
        } catch(IOException e) {
            throw new FileUploadError();
        }
        ProcessInstance instance = getProcessInstance(id);
        RequestRepresentation request = new RequestRepresentation();
        transferData(instance, request, user.getUser());

        // add attachment id to the set of ids of the agreement attachments.
        RequestProperties properties = requestPropertiesService.findByProcessInstanceId(id);
        properties.getDataAttachmentIds().add(attachmentId);
        requestPropertiesService.save(properties);

        //Map<String, Object> variables = transferFormData(request, instance, user.getUser());
        //runtimeService.setVariables(instance.getProcessInstanceId(), variables);
        instance = getProcessInstance(id);
        request = new RequestRepresentation();
        transferData(instance, request, user.getUser());
        return request;
    }
    
    @Secured("hasPermission(#param, 'isPalgaUser')")
    @RequestMapping(value = "/requests/{id}/dataFiles/{attachmentId}", method = RequestMethod.DELETE)
    public RequestRepresentation removeDataAttachment(UserAuthenticationToken user, @PathVariable String id,
            @PathVariable String attachmentId) {
        log.info("DELETE /requests/" + id + "/dataFiles/" + attachmentId);

        ProcessInstance instance = getProcessInstance(id);

        // remove existing agreement.
        RequestProperties properties = requestPropertiesService.findByProcessInstanceId(id);
        if (properties != null && properties.getDataAttachmentIds().contains(attachmentId)) {
            taskService.deleteAttachment(attachmentId);
            properties.getDataAttachmentIds().remove(attachmentId);
            requestPropertiesService.save(properties);
        }

        instance = getProcessInstance(id);
        RequestRepresentation request = new RequestRepresentation();
        transferData(instance, request, user.getUser());
        return request;
    }
    
    @ResponseStatus(value=HttpStatus.NOT_ACCEPTABLE, reason="Excerpt list upload error.")
    public class ExcerptListUploadError extends RuntimeException {
        
        public ExcerptListUploadError() {
            super("Excerpt list upload error.");
        }

        public ExcerptListUploadError(String message) {
            super("Excerpt list upload error: " + message);
        }
    
    }
    
    private ExcerptList processExcerptList(MultipartFile file) {
        log.info("Processing excerpt list");
        try {
            CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()), ';', '"');
            ExcerptList list = new ExcerptList();
            String [] nextLine;
            log.info("Column names.");
            if ((nextLine = reader.readNext()) != null) {
                try {
                    list.setColumnNames(nextLine);
                } catch (RuntimeException e) {
                    reader.close();
                    throw new ExcerptListUploadError(e.getMessage());
                } 
            }
            int line = 2;
            while ((nextLine = reader.readNext()) != null) {
                log.info("Line " + line);
                try {
                    list.addEntry(nextLine);
                } catch (RuntimeException e) {
                    reader.close();
                    throw new ExcerptListUploadError("Line " + line + ": " + e.getMessage());
                }
                line++;
            }
            reader.close();
            log.info("Added " + list.getEntries().size() + " entries.");
            return list;
        } catch(IOException e) {
            throw new FileUploadError(e.getMessage());
        }
    }
    
    @Secured("hasPermission(#param, 'isPalgaUser')")
    @RequestMapping(value = "/requests/{id}/excerptList", method = RequestMethod.POST)
    public RequestRepresentation uploadExcerptList(UserAuthenticationToken user, @PathVariable String id,
            @RequestParam("flowFilename") String name,
            @RequestParam("file") MultipartFile file) {
        log.info("POST /requests/" + id + "/excerptList");
        Task task = getTaskByRequestId(id, "data_delivery");
        String attachmentId;
        try{
            Attachment result = taskService.createAttachment(
                    file.getContentType(),
                    task.getId(), task.getProcessInstanceId(),
                    name, name, file.getInputStream());
            attachmentId = result.getId();
        } catch(IOException e) {
            throw new FileUploadError();
        }
        ProcessInstance instance = getProcessInstance(id);
        
        // add attachment id to the set of ids of the agreement attachments.
        RequestProperties properties = requestPropertiesService.findByProcessInstanceId(id);
        if (properties == null) {
            properties = new RequestProperties();
            properties.setProcessInstanceId(id);
        }
        if (properties.getExcerptListAttachmentId() != null && !properties.getExcerptListAttachmentId().equals(attachmentId)) {
            log.info("Deleting attachment " + properties.getExcerptListAttachmentId());
            taskService.deleteAttachment(properties.getExcerptListAttachmentId());
        }
        properties.setExcerptListAttachmentId(attachmentId);
        requestPropertiesService.save(properties);

        ExcerptList list = processExcerptList(file);
        properties.setExcerptList(list);
        log.info("Saving excerpt list.");
        requestPropertiesService.save(properties);
        log.info("Done.");
        
        instance = getProcessInstance(id);
        RequestRepresentation request = new RequestRepresentation();
        transferData(instance, request, user.getUser());
        return request;
    }

    @ResponseStatus(value=HttpStatus.NOT_FOUND, reason="Excerpt list not found.")
    public class ExcerptListNotFound extends RuntimeException {
        public ExcerptListNotFound() {
            super("Excerpt list not found.");
        }
    }
    
    @Secured("hasPermission(#param, 'isPalgaUser')")
    @RequestMapping(value = "/requests/{id}/excerptList", method = RequestMethod.GET)
    public ExcerptList getExcerptList(UserAuthenticationToken user, @PathVariable String id) {
        log.info("GET /requests/" + id + "/excerptList");
        Task task = getTaskByRequestId(id, "data_delivery");
        ProcessInstance instance = getProcessInstance(id);
        RequestRepresentation request = new RequestRepresentation();
        transferData(instance, request, user.getUser());

        // add attachment id to the set of ids of the agreement attachments.
        RequestProperties properties = requestPropertiesService.findByProcessInstanceId(id);
        if (properties == null || properties.getExcerptList() == null) {
            throw new ExcerptListNotFound();
        }
        ExcerptList list = properties.getExcerptList();
        log.info("entries: " + list.getEntries().size());
        return list;
    }

    @ResponseStatus(value=HttpStatus.INTERNAL_SERVER_ERROR, reason="Error while downloading excerpt list.")
    public class ExcerptListDownloadError extends RuntimeException {
        public ExcerptListDownloadError() {
            super("Error while downloading excerpt list.");
        }
    }
    
    @Secured("hasPermission(#param, 'isPalgaUser')")
    @RequestMapping(value = "/requests/{id}/excerptList/csv", method = RequestMethod.GET)
    public HttpEntity<InputStreamResource> downloadExcerptList(UserAuthenticationToken user, @PathVariable String id) {
        log.info("GET /requests/" + id + "/excerptList/csv");
        Task task = getTaskByRequestId(id, "data_delivery");
        ProcessInstance instance = getProcessInstance(id);
        RequestRepresentation request = new RequestRepresentation();
        transferData(instance, request, user.getUser());

        // add attachment id to the set of ids of the agreement attachments.
        RequestProperties properties = requestPropertiesService.findByProcessInstanceId(id);
        if (properties == null || properties.getExcerptList() == null) {
            throw new ExcerptListNotFound();
        }
        ExcerptList list = properties.getExcerptList();
        ByteArrayOutputStream out = new ByteArrayOutputStream(); 
        Writer writer = new PrintWriter(out);
        CSVWriter csvwriter = new CSVWriter(writer, ';', '"');
        csvwriter.writeNext(list.getCsvColumnNames());
        for (ExcerptEntry entry: list.getEntries()) {
            csvwriter.writeNext(entry.getCsvValues());
        }
        try {
            csvwriter.flush();
            csvwriter.close();
            writer.flush();
            writer.close();
            out.flush();
            InputStream in = new ByteArrayInputStream(out.toByteArray());
            out.close();
            InputStreamResource resource = new InputStreamResource(in);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.valueOf("text/csv"));
            headers.set("Content-Disposition",
                       "attachment; filename=excerpts_" + id + ".csv");
            HttpEntity<InputStreamResource> response =  new HttpEntity<InputStreamResource>(resource, headers);
            log.info("Returning reponse.");
            return response;
        } catch (IOException e) {
            throw new ExcerptListDownloadError();
        }
    }
    
    @RequestMapping(value = "/requests/{id}/files/{attachmentId}", method = RequestMethod.GET)
    public HttpEntity<InputStreamResource> getFile(UserAuthenticationToken user, @PathVariable String id,
            @PathVariable String attachmentId) {
        log.info("GET /requests/" + id + "/files/" + attachmentId);
        Attachment result = taskService.getAttachment(attachmentId);
        List<HistoricTaskInstance> historicTasks = getHistoricTasksByRequestId(id);
        boolean taskFound = false;
        for(HistoricTaskInstance historicTask: historicTasks) {
            if (result.getTaskId().equals(historicTask.getId())) {
                taskFound = true;
                break;
            }
        }
        if (!taskFound) {
            //log.info("Task not found: " + result.getTaskId());
            throw new TaskNotFound();
        }
        InputStream input = taskService.getAttachmentContent(attachmentId);
        InputStreamResource resource = new InputStreamResource(input);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf(result.getType()));
        headers.set("Content-Disposition",
                "attachment; filename=" + result.getName().replace(" ", "_"));
        HttpEntity<InputStreamResource> response =  new HttpEntity<InputStreamResource>(resource, headers);
        log.info("Returning reponse.");
        return response;
    }

    @RequestMapping(value = "/requests/{id}/files/{attachmentId}", method = RequestMethod.DELETE)
    public void deleteFile(UserAuthenticationToken user, @PathVariable String id,
            @PathVariable String attachmentId) {
        log.info("DELETE /requests/" + id + "/files/" + attachmentId);
        Task task = getTaskByRequestId(id, "request_form");
        Attachment result = taskService.getAttachment(attachmentId);
        if (!result.getTaskId().equals(task.getId())) {
            // not associated with current task
            throw new TaskNotFound();
        }
        ProcessInstance instance = getProcessInstance(id);
        RequestRepresentation request = new RequestRepresentation();
        transferData(instance, request, user.getUser());
        log.info("Status: " + request.getStatus());
        if (!request.getStatus().equals("Open")) {
            throw new InvalidActionInStatus();
        }
        taskService.deleteAttachment(attachmentId);
    }

}
