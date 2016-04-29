/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import javax.transaction.Transactional;

import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import business.exceptions.UpdateNotAllowed;
import business.models.ApprovalVote;
import business.models.ApprovalVoteRepository;
import business.models.Comment;
import business.models.ContactData;
import business.models.ContactDataRepository;
import business.models.ExcerptEntry;
import business.models.ExcerptList;
import business.models.File;
import business.models.RequestProperties;
import business.models.User;
import business.representation.ApprovalVoteRepresentation;
import business.representation.CommentRepresentation;
import business.representation.ExcerptListRepresentation;
import business.representation.FileRepresentation;
import business.representation.ProfileRepresentation;
import business.representation.RequestListRepresentation;
import business.representation.RequestRepresentation;

@Service
public class RequestFormService {

    Log log = LogFactory.getLog(getClass());
    
    @Autowired
    private UserService userService;

    @Autowired
    private RequestPropertiesService requestPropertiesService;

    @Autowired
    private ExcerptListService excerptListService;
  
    @Autowired
    private ApprovalVoteRepository approvalVoteRepository;
    
    @Autowired
    private RequestService requestService;

    @Autowired
    private ContactDataRepository contactDataRepository;

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
        List<String> parts = new ArrayList<>(2);
        if (user.getFirstName() != null && !user.getFirstName().trim().isEmpty()) {
            parts.add(user.getFirstName().trim());
        }
        if (user.getLastName() != null && !user.getLastName().trim().isEmpty()) {
            parts.add(user.getLastName().trim());
        }
        return String.join(" ", parts);
    }

    @Cacheable("requestlistdata")
    public RequestListRepresentation getRequestListDataCached(String processInstanceId) {
        HistoricProcessInstance instance = requestService.getProcessInstance(processInstanceId);
        // copy request list representation data
        RequestListRepresentation request = new RequestListRepresentation();
        transferBasicData(instance, request);
        return request;
    }

    @Cacheable("requestlistattachmentsdata")
    public RequestListRepresentation getRequestListDataWithAttachmentsCached(String processInstanceId) {
        HistoricProcessInstance instance = requestService.getProcessInstance(processInstanceId);
        // copy request list representation data
        RequestListRepresentation request = new RequestListRepresentation();
        transferBasicData(instance, request);
        transferAttachmentData(instance, request);
        return request;
    }

    public void transferBasicData(HistoricProcessInstance instance, RequestListRepresentation request) {
        request.setProcessInstanceId(instance.getId());
        request.setProcessId(instance.getProcessDefinitionId());
        request.setRequestNumber(requestPropertiesService.getRequestNumber(instance.getId()));

        Map<String, Object> variables = instance.getProcessVariables();

        if (variables != null) {
            request.setTitle((String)variables.get("title"));
            request.setBackground((String)variables.get("background"));
            request.setResearchQuestion((String)variables.get("research_question"));
            request.setHypothesis((String) variables.get("hypothesis"));
            request.setMethods((String) variables.get("methods"));
            request.setPathologistName((String)variables.get("pathologist_name"));
            request.setPathologistEmail((String)variables.get("pathologist_email"));
            request.setStatus((String)variables.get("status"));
            request.setDateCreated((Date)variables.get("date_created"));
            String requesterId = variables.get("requester_id") == null ? "" : variables.get("requester_id").toString();
            Long userId = null;
            try { userId = Long.valueOf(requesterId); }
            catch(NumberFormatException e) {}
            if (userId != null) {
                User user = userService.findOneCached(userId);
                if (user != null) {
                    request.setRequesterId(userId);
                    request.setRequesterName(getName(user));
                }
            }
            request.setStatisticsRequest(fetchBooleanVariable("is_statistics_request", variables));
            request.setExcerptsRequest(fetchBooleanVariable("is_excerpts_request", variables));
            request.setPaReportRequest(fetchBooleanVariable("is_pa_report_request", variables));
            request.setMaterialsRequest(fetchBooleanVariable("is_materials_request", variables));
            request.setReopenRequest(fetchBooleanVariable("reopen_request", variables));

            request.setDateAssigned((Date)variables.get("assigned_date"));
        }
    }

    public void transferAttachmentData(HistoricProcessInstance instance, RequestListRepresentation request) {
        RequestProperties properties = requestPropertiesService.findByProcessInstanceId(
                instance.getId());
        List<FileRepresentation> requestAttachments = new ArrayList<FileRepresentation>();
        for(File file: properties.getRequestAttachments()) {
            requestAttachments.add(new FileRepresentation(file));
        }
        request.setAttachments(requestAttachments);

        List<FileRepresentation> medicalEthicalCommitteeApprovalAttachments = new ArrayList<FileRepresentation>();
        for (File file: properties.getMedicalEthicalCommiteeApprovalAttachments()) {
            medicalEthicalCommitteeApprovalAttachments.add(new FileRepresentation(file));
        }
        request.setMedicalEthicalCommitteeApprovalAttachments(medicalEthicalCommitteeApprovalAttachments);
    }

    public void transferData(HistoricProcessInstance instance, RequestListRepresentation request, User currentUser) {
        transferBasicData(instance, request);
        Task task = null;
        if (request.getStatus() != null) {
            switch(request.getStatus()) {
                case "Review":
                    task = requestService.findTaskByRequestId(instance.getId(), "palga_request_review");
                    break;
                case "Approval":
                    task = requestService.findTaskByRequestId(instance.getId(), "request_approval");

                    request.setNumberOfApprovalVotes(approvalVoteRepository.countByProcessInstanceId(instance.getId()));
                    break;
                case "DataDelivery":
                    task = requestService.findTaskByRequestId(instance.getId(), "data_delivery"); 
                    break;
                case "SelectionReview":
                    task = requestService.findTaskByRequestId(instance.getId(), "selection_review"); 
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
                    User assignee = userService.findOneCached(assigneeId);
                    if (assignee != null) {
                        request.setAssigneeName(getName(assignee));
                    }
                }
            }
        }

        if (currentUser.isPalga()) {
            request.setReviewStatus(requestPropertiesService.getRequestReviewStatus(instance.getId()));
        } else if (currentUser.isScientificCouncilMember()) {
            // fetch my vote
            RequestProperties properties = requestPropertiesService.findByProcessInstanceId(
                    instance.getId());
            Map<Long, ApprovalVote> votes = properties.getApprovalVotes();
            if (votes.containsKey(currentUser.getId())) {
                request.setApprovalVote(votes.get(currentUser.getId()).getValue().name());
            }
        }
    }
    
    private static Set<String> excerptListStatuses = new HashSet<String>();
    {
        excerptListStatuses.add("DataDelivery");
        excerptListStatuses.add("SelectionReview");
        excerptListStatuses.add("LabRequest");
    }

    /**
     * Populate the RequestRepresentation object with data from the Activiti process
     * instance and the RequestProperties entity.
     * Nothing is returned; instead the representation object is updated.
     *  
     * @param instance the Activiti process instance for the request.
     * @param request the representation object to be populated.
     * @param currentUser the current user.
     */
    @Transactional
    public void transferData(HistoricProcessInstance instance, RequestRepresentation request, User currentUser) {
        boolean is_palga = currentUser == null ? false : currentUser.isPalga();
        boolean is_scientific_council = currentUser == null ? false : currentUser.isScientificCouncilMember();

        request.setProcessInstanceId(instance.getId());
        request.setProcessId(instance.getProcessDefinitionId());
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
            
            request.setPathologistName((String)variables.get("pathologist_name"));
            request.setPathologistEmail((String)variables.get("pathologist_email"));
            request.setPreviousContact(fetchBooleanVariable("previous_contact", variables));
            request.setPreviousContactDescription((String)variables.get("previous_contact_description"));

            request.setStatisticsRequest(fetchBooleanVariable("is_statistics_request", variables));
            request.setExcerptsRequest(fetchBooleanVariable("is_excerpts_request", variables));
            request.setPaReportRequest(fetchBooleanVariable("is_pa_report_request", variables));
            request.setMaterialsRequest(fetchBooleanVariable("is_materials_request", variables));

            request.setReopenRequest(fetchBooleanVariable("reopen_request", variables));

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
                User user = userService.findOneCached(userId);
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
                case "SelectionReview":
                    task = requestService.findTaskByRequestId(instance.getId(), "selection_review"); 
                    break;
            }
            if (task != null) {
                request.setAssignee(task.getAssignee());
                if (task.getAssignee() != null && !task.getAssignee().isEmpty()) {
                    Long assigneeId = null;
                    try { assigneeId = Long.valueOf(task.getAssignee()); }
                    catch(NumberFormatException e) {}
                    if (assigneeId != null) {
                        User assignee = userService.findOne(assigneeId);
                        if (assignee != null) {
                            request.setAssigneeName(getName(assignee));
                        }
                    }
                }
            }
            RequestProperties properties = requestPropertiesService.findByProcessInstanceId(
                    instance.getId());
            request.setRequestNumber(properties.getRequestNumber());
            request.setReviewStatus(properties.getReviewStatus());
            request.setBillingAddress(properties.getBillingAddress());
            request.setChargeNumber(properties.getChargeNumber());
            request.setResearchNumber(properties.getReseachNumber());
            
            List<FileRepresentation> requestAttachments = new ArrayList<FileRepresentation>();
            for(File file: properties.getRequestAttachments()) {
                requestAttachments.add(new FileRepresentation(file));
            }
            request.setAttachments(requestAttachments);

            request.setSentToPrivacyCommittee(properties.isSentToPrivacyCommittee());
            request.setPrivacyCommitteeRationale(properties.getPrivacyCommitteeRationale());
            request.setPrivacyCommitteeOutcome(properties.getPrivacyCommitteeOutcome());
            request.setPrivacyCommitteeOutcomeRef(properties.getPrivacyCommitteeOutcomeRef());
            request.setPrivacyCommitteeEmails(properties.getPrivacyCommitteeEmails());

            if (is_palga) {
                List<FileRepresentation> agreementAttachments = new ArrayList<FileRepresentation>();
                for(File file: properties.getAgreementAttachments()) {
                    agreementAttachments.add(new FileRepresentation(file));
                }
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

                request.setRequestAdmissible(fetchBooleanVariable("request_is_admissible", variables));

                request.setScientificCouncilApproved(fetchBooleanVariable("scientific_council_approved", variables));
                request.setPrivacyCommitteeApproved(fetchBooleanVariable("privacy_committee_approved", variables));

                request.setRequestApproved(fetchBooleanVariable("request_approved", variables));
                request.setRejectReason((String)variables.get("reject_reason"));
                request.setRejectDate((Date)variables.get("reject_date"));
            }

            List<FileRepresentation> medicalEthicalCommitteeApprovalAttachments = new ArrayList<FileRepresentation>();
            for (File file: properties.getMedicalEthicalCommiteeApprovalAttachments()) {
                medicalEthicalCommitteeApprovalAttachments.add(new FileRepresentation(file));
            }
            request.setMedicalEthicalCommitteeApprovalAttachments(medicalEthicalCommitteeApprovalAttachments);

            if (!is_scientific_council) {
                List<FileRepresentation> dataAttachments = new ArrayList<FileRepresentation>();
                for(File file: properties.getDataAttachments()) {
                    dataAttachments.add(new FileRepresentation(file));
                }
                request.setDataAttachments(dataAttachments);
            
                Date start = new Date();
                if (excerptListStatuses.contains(request.getStatus())) {
                    ExcerptList excerptList = excerptListService.findByProcessInstanceId(instance.getId());
                    if (excerptList != null) {
                        log.info("Set excerpt list.");
                        ExcerptListRepresentation excerptListRepresentation
                            = new ExcerptListRepresentation(excerptList);
                        List<ExcerptEntry> list = excerptList.getEntries();
                        excerptListRepresentation.setEntryList(list);
                        request.setExcerptList(excerptListRepresentation);
                        @SuppressWarnings("unchecked")
                        Collection<Integer> selectedLabs = (Collection<Integer>)variables.get("lab_request_labs");
                        Set<Integer> selectedLabSet = new TreeSet<Integer>();
                        if (selectedLabs != null) {
                            for (Integer labNumber: selectedLabs) { selectedLabSet.add(labNumber); }
                        }
                        request.setSelectedLabs(selectedLabSet);
                        request.setExcerptListRemark(excerptList.getRemark());
                    }
                    Date end = new Date();
                    log.info("Fetching excerpt list took " + (end.getTime() - start.getTime()) + " ms.");
                }
            }
        }
    }

    /**
     * Copy form data from the RequestRepresentation object to the Activiti process
     * instance and the RequestProperties entity associated with the request.
     * Updates the Activiti process variables and the RequestProperties entity
     * and returns the updated variables.
     * 
     * @param request the form data.
     * @param instance the Activiti process instance.
     * @param user the current user.
     * @return the updated variable map of the Activiti process instance.
     */
    public Map<String, Object> transferFormData(RequestRepresentation request, HistoricProcessInstance instance, User user) {
        request.setProcessInstanceId(instance.getId());
        Map<String, Object> variables = instance.getProcessVariables();
        if (variables == null) {
            return variables;
        }

        if (user.isRequester()) {
            // for requesters, editing fields is only allowed in status 'Open',
            // which corresponds to the 'request_form' task.
            Task task = requestService.findTaskByRequestId(instance.getId(), "request_form");
            if (task == null) {
                throw new UpdateNotAllowed();
            }
        } else if (!user.isPalga()) {
            // other users than the requester and Palga users are not allowed to
            // edit request data.
            throw new UpdateNotAllowed();
        }

        variables.put("title", request.getTitle());
        variables.put("background", request.getBackground());
        variables.put("research_question", request.getResearchQuestion());
        variables.put("hypothesis", request.getHypothesis());
        variables.put("methods", request.getMethods());

        variables.put("is_statistics_request", (Boolean)request.isStatisticsRequest());
        variables.put("is_excerpts_request", (Boolean)request.isExcerptsRequest());
        variables.put("is_pa_report_request", (Boolean)request.isPaReportRequest());
        variables.put("is_materials_request", (Boolean)request.isMaterialsRequest());

        variables.put("pathologist_name", request.getPathologistName());
        variables.put("pathologist_email", request.getPathologistEmail());
        variables.put("previous_contact",(Boolean) request.isPreviousContact());
        variables.put("previous_contact_description", request.getPreviousContactDescription());

        variables.put("is_linkage_with_personal_data", (Boolean)request.isLinkageWithPersonalData());
        variables.put("linkage_with_personal_data_notes", request.getLinkageWithPersonalDataNotes());
        variables.put("is_informed_consent", (Boolean)request.isInformedConsent());
        variables.put("reason_using_personal_data", request.getReasonUsingPersonalData());

        variables.put("return_date", request.getReturnDate());
        variables.put("contact_person_name", request.getContactPersonName());

        RequestProperties properties = requestPropertiesService.findByProcessInstanceId(instance.getId());
        properties.setChargeNumber(request.getChargeNumber());
        properties.setReseachNumber(request.getResearchNumber());
        ContactData billingAddress;
        if (request.getBillingAddress() != null) {
            billingAddress = request.getBillingAddress();
        } else {
            billingAddress = new ContactData();
            // FIXME: should throw exception
        }
        billingAddress = contactDataRepository.save(billingAddress);
        properties.setBillingAddress(billingAddress);

        if (user.isPalga()) {
            variables.put("requester_is_valid", (Boolean)request.isRequesterValid());
            variables.put("requester_is_allowed", (Boolean)request.isRequesterAllowed());
            variables.put("contact_person_is_allowed", (Boolean)request.isContactPersonAllowed());
            variables.put("requester_lab_is_valid", (Boolean)request.isRequesterLabValid());
            variables.put("agreement_reached", (Boolean)request.isAgreementReached());

            variables.put("request_is_admissible", (Boolean)request.isRequestAdmissible());

            variables.put("reopen_request", (Boolean)request.isReopenRequest());

            variables.put("scientific_council_approved", (Boolean)request.isScientificCouncilApproved());
            variables.put("privacy_committee_approved", (Boolean)request.isPrivacyCommitteeApproved());

            variables.put("request_approved", (Boolean)request.isRequestApproved());
            variables.put("reject_reason", request.getRejectReason());
            variables.put("reject_date", request.getRejectDate());

            if (!(request.isRequesterValid()
                    && request.isRequesterAllowed()
                    && request.isContactPersonAllowed()
                    && request.isRequesterLabValid()
                    && request.isAgreementReached())) {
                log.info("Request not admissible");
                variables.put("request_is_admissible", Boolean.FALSE);
            }
            properties.setSentToPrivacyCommittee(request.isSentToPrivacyCommittee());
            properties.setPrivacyCommitteeRationale(request.getPrivacyCommitteeRationale());
            properties.setPrivacyCommitteeOutcome(request.getPrivacyCommitteeOutcome());
            properties.setPrivacyCommitteeOutcomeRef(request.getPrivacyCommitteeOutcomeRef());
            properties.setPrivacyCommitteeEmails(request.getPrivacyCommitteeEmails());
        }
        requestPropertiesService.save(properties);
        return variables;
    }

}
