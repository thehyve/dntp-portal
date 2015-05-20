package business.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Attachment;
import org.activiti.engine.task.Task;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import business.models.ApprovalVote;
import business.models.Comment;
import business.models.RequestProperties;
import business.models.User;
import business.models.UserRepository;
import business.representation.ApprovalVoteRepresentation;
import business.representation.AttachmentRepresentation;
import business.representation.CommentRepresentation;
import business.representation.ExcerptListRepresentation;
import business.representation.ProfileRepresentation;
import business.representation.RequestListRepresentation;
import business.representation.RequestRepresentation;

@Service
public class RequestFormService {

    Log log = LogFactory.getLog(getClass());
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RequestPropertiesService requestPropertiesService;
  
    @Autowired
    private RequestService requestService;

    @Autowired
    private TaskService taskService;

    /**
     * Casts variable 'name' to type Boolean if it exists as key in the variable map;
     * returns false otherwise.
     */
    public static boolean fetchBooleanVariable(String name, Map<String,Object> variables) {
        if (variables.get(name) != null) {
            return (boolean)variables.get(name);
        }
        return false;
    }
    
    /**
     * Concatenates first name and last name of the user if user is not null;
     * returns the empty string otherwise.
     */
    public static String getName(User user) {
        if (user == null) {
            return "";
        }
        return (user.getFirstName() == null ? "" : user.getFirstName())
                + (user.getFirstName() == null || user.getFirstName().isEmpty() || user.getLastName() == null
                || user.getLastName().isEmpty() ? "" :" ")
                + (user.getLastName() == null ? "" : user.getLastName());
    }
    
    public void transferData(HistoricProcessInstance instance, RequestListRepresentation request, User currentUser) {

        request.setProcessInstanceId(instance.getId());

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
            if (request.getStatus() != null) {
                switch(request.getStatus()) {
                    case "Review":
                        task = requestService.findTaskByRequestId(instance.getId(), "palga_request_review");
                        break;
                    case "Approval":
                        task = requestService.findTaskByRequestId(instance.getId(), "request_approval");
                                            
                        // fetch my vote, number of votes
                        RequestProperties properties = requestPropertiesService.findByProcessInstanceId(
                                instance.getId());
                        Map<Long, ApprovalVote> votes = properties.getApprovalVotes();
                        request.setNumberOfApprovalVotes(votes.size());
                        if (votes.containsKey(currentUser.getId())) {
                            request.setApprovalVote(votes.get(currentUser.getId()).getValue().name());
                        } 
                        break;
                }
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

    public void transferData(HistoricProcessInstance instance, RequestRepresentation request, User currentUser) {
        boolean is_palga = currentUser == null ? false : currentUser.isPalga();
        boolean is_scientific_council = currentUser == null ? false : currentUser.isScientificCouncilMember();

        request.setProcessInstanceId(instance.getId());
        
        //request.setActivityId(instance.getActivityId()); // fetch from runtimeService?

        Map<String, Object> variables = instance.getProcessVariables();
        if (variables != null) {
            request.setStatus((String)variables.get("status"));
            request.setDateCreated((Date)variables.get("date_created"));
            request.setDateAssigned((Date)variables.get("assigned_date"));

            request.setContactPersonName((String)variables.get("contact_person_name"));
            request.setTitle((String)variables.get("title"));
            request.setBackground((String)variables.get("background"));
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
                    task = requestService.findTaskByRequestId(instance.getId(), "palga_request_review");
                    break;
                case "Approval":
                    task = requestService.findTaskByRequestId(instance.getId(), "request_approval");
                    break;
                case "DataDelivery":
                    task = requestService.findTaskByRequestId(instance.getId(), "data_delivery"); 
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
            List<Attachment> attachments = taskService.getProcessInstanceAttachments(instance.getId());
            List<AttachmentRepresentation> requesterAttachments = new ArrayList<AttachmentRepresentation>();
            List<AttachmentRepresentation> agreementAttachments = new ArrayList<AttachmentRepresentation>();
            List<AttachmentRepresentation> dataAttachments = new ArrayList<AttachmentRepresentation>();
            RequestProperties properties = requestPropertiesService.findByProcessInstanceId(
                    instance.getId());

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
                for (Entry<Long, ApprovalVote> entry: properties.getApprovalVotes().entrySet()) {
                    approvalVotes.put(entry.getKey(), new ApprovalVoteRepresentation(entry.getValue()));
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
                Collection<Integer> selectedLabs = (Collection<Integer>)variables.get("lab_request_labs");
                Set<Integer> selectedLabSet = new TreeSet<Integer>();
                if (selectedLabs != null) {
                    for (Integer labNumber: selectedLabs) { selectedLabSet.add(labNumber); }
                }
                request.setSelectedLabs(selectedLabSet);
                request.setExcerptListRemark(properties.getExcerptListRemark());
            }
        }
    }

    public Map<String, Object> transferFormData(RequestRepresentation request, HistoricProcessInstance instance, User user) {
        boolean is_palga = user.isPalga();
        request.setProcessInstanceId(instance.getId());
        Map<String, Object> variables = instance.getProcessVariables();
        if (variables != null) {
            variables.put("title", request.getTitle());
            variables.put("background", request.getBackground());
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
                RequestProperties properties = requestPropertiesService.findByProcessInstanceId(instance.getId());
                properties.setSentToPrivacyCommittee(request.isSentToPrivacyCommittee());
                properties.setPrivacyCommitteeOutcome(request.getPrivacyCommitteeOutcome());
                properties.setPrivacyCommitteeOutcomeRef(request.getPrivacyCommitteeOutcomeRef());
                properties.setPrivacyCommitteeEmails(request.getPrivacyCommitteeEmails());
                requestPropertiesService.save(properties);
            }
        }
        return variables;
    }
    

}
