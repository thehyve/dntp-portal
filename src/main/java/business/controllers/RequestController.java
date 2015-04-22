package business.controllers;

import java.io.IOException;
import java.io.InputStream;
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

import business.models.ApprovalVote;
import business.models.Comment;
import business.models.CommentRepository;
import business.models.RequestProperties;
import business.models.RequestPropertiesRepository;
import business.models.Role;
import business.models.RoleRepository;
import business.models.User;
import business.models.UserRepository;
import business.representation.ApprovalVoteRepresentation;
import business.representation.AttachmentRepresentation;
import business.representation.CommentRepresentation;
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
    private RequestPropertiesRepository requestPropertiesRepository;

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
            request.setDescription((String)variables.get("description"));
            request.setMotivation((String)variables.get("motivation"));
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
            request.setTitle((String)variables.get("title"));
            request.setDescription((String)variables.get("description"));
            request.setMotivation((String)variables.get("motivation"));
            request.setStatisticsRequest(fetchBooleanVariable("is_statistics_request", variables));
            request.setPaReportRequest(fetchBooleanVariable("is_pa_report_request", variables));
            request.setMaterialsRequest(fetchBooleanVariable("is_materials_request", variables));
            request.setReturnDate((Date)variables.get("return_date"));
            request.setLimitedToCohort(fetchBooleanVariable("limited_to_cohort", variables));
            request.setContactPersonName((String)variables.get("contact_person_name"));
            request.setRequesterId(variables.get("requester_id") == null ? "" : variables.get("requester_id").toString());
            Long userId = null;
            try { userId = Long.valueOf(request.getRequesterId()); }
            catch(NumberFormatException e) {}
            if (userId != null) {
                User user = userRepository.findOne(userId);
                if (user != null) {
                    request.setRequesterName(getName(user));
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
            RequestProperties properties = requestPropertiesRepository.findByProcessInstanceId(
                    instance.getProcessInstanceId());
            if (properties != null) {
                Set<String> agreementAttachmentIds = properties.getAgreementAttachmentIds();
                for (Attachment attachment: attachments) {
                    if (agreementAttachmentIds.contains(attachment.getId())) {
                        agreementAttachments.add(new AttachmentRepresentation(attachment));
                    } else {
                        requesterAttachments.add(new AttachmentRepresentation(attachment));
                    }
                }
                request.setSentToPrivacyCommittee(properties.isSentToPrivacyCommittee());
                request.setPrivacyCommitteeOutcome(properties.getPrivacyCommitteeOutcome());
                request.setPrivacyCommitteeOutcomeRef(properties.getPrivacyCommitteeOutcomeRef());
                request.setPrivacyCommitteeEmails(properties.getPrivacyCommitteeEmails());
            } else {
                properties = new RequestProperties();
                for (Attachment attachment: attachments) {
                    requesterAttachments.add(new AttachmentRepresentation(attachment));
                }
            }
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
            }
        }
    }

    private Map<String, Object> transferFormData(RequestRepresentation request, ProcessInstance instance, User user) {
        boolean is_palga = user.isPalga();
        request.setProcessInstanceId(instance.getProcessInstanceId());
        Map<String, Object> variables = instance.getProcessVariables();
        if (variables != null) {
            variables.put("title", request.getTitle());
            variables.put("description", request.getDescription());
            variables.put("motivation", request.getMotivation());
            variables.put("is_statistics_request", (Boolean)request.isStatisticsRequest());
            variables.put("is_pa_report_request", (Boolean)request.isPaReportRequest());
            variables.put("is_materials_request", (Boolean)request.isMaterialsRequest());
            variables.put("return_date", request.getReturnDate());
            variables.put("limited_to_cohort", (Boolean)request.isLimitedToCohort());
            variables.put("contact_person_name", request.getContactPersonName());

            if (is_palga) {
                variables.put("requester_is_valid", (Boolean)request.isRequesterValid());
                variables.put("requester_is_allowed", (Boolean)request.isRequesterAllowed());
                variables.put("contact_person_is_allowed", (Boolean)request.isContactPersonAllowed());
                variables.put("requester_lab_is_valid", (Boolean)request.isRequesterLabValid());
                variables.put("agreement_reached", (Boolean)request.isAgreementReached());
                if (request.isRequesterValid()
                        && request.isRequesterAllowed()
                        && request.isContactPersonAllowed()
                        && request.isRequesterLabValid()
                        && request.isAgreementReached()) {
                    variables.put("request_is_admissible", Boolean.TRUE);
                } else {
                    variables.put("request_is_admissible", Boolean.FALSE);
                }
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

        RequestProperties properties = requestPropertiesRepository.findByProcessInstanceId(id);
        if (properties == null) {
            properties = new RequestProperties();
        }
        properties.setProcessInstanceId(id);
        properties.setSentToPrivacyCommittee(request.isSentToPrivacyCommittee());
        properties.setPrivacyCommitteeOutcome(request.getPrivacyCommitteeOutcome());
        properties.setPrivacyCommitteeOutcomeRef(request.getPrivacyCommitteeOutcomeRef());
        properties.setPrivacyCommitteeEmails(request.getPrivacyCommitteeEmails());
        requestPropertiesRepository.save(properties);

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
     * @throws TaskNotFound.
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
     * @throws RequestNotFound.
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
                    "Request : %s\n"
                +   "Title   : %s\n";
            String body = String.format(template,
                    request.getProcessInstanceId(),
                    request.getTitle());
            message.setText(String.format(
                    "(We're testing a prototype system. If you receive this email, please contact gijs@thehyve.nl.)\n"
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
        RequestRepresentation updatedRequest = new RequestRepresentation();
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
        Task task = getTaskByRequestId(id, "palga_request_review");
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
        Task task = getTaskByRequestId(id, "palga_request_review");
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
        RequestProperties properties = requestPropertiesRepository.findByProcessInstanceId(id);
        if (properties == null) {
            properties = new RequestProperties();
            properties.setProcessInstanceId(id);
        }
        properties.getAgreementAttachmentIds().add(attachmentId);
        requestPropertiesRepository.save(properties);

        Map<String, Object> variables = transferFormData(request, instance, user.getUser());
        runtimeService.setVariables(instance.getProcessInstanceId(), variables);
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
        RequestProperties properties = requestPropertiesRepository.findByProcessInstanceId(id);
        if (properties != null && properties.getAgreementAttachmentIds().contains(attachmentId)) {
            taskService.deleteAttachment(attachmentId);
            properties.getAgreementAttachmentIds().remove(attachmentId);
            requestPropertiesRepository.save(properties);
        }

        instance = getProcessInstance(id);
        RequestRepresentation request = new RequestRepresentation();
        transferData(instance, request, user.getUser());
        return request;
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
