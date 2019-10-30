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

import org.activiti.engine.HistoryService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import business.exceptions.UpdateNotAllowed;
import business.models.ApprovalVote;
import business.models.Comment;
import business.models.ContactData;
import business.models.ContactDataRepository;
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
import business.representation.RequestStatus;

@Service
public class RequestFormService {

    Logger log = LoggerFactory.getLogger(getClass());
    
    @Autowired
    private UserService userService;

    @Autowired
    private RequestPropertiesService requestPropertiesService;

    @Autowired
    private ExcerptListService excerptListService;
  
    @Autowired
    public RequestService requestService;

    @Autowired
    private ContactDataRepository contactDataRepository;

    @Autowired
    private HistoryService historyService;

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

    public RequestListRepresentation getRequestListData(String processInstanceId) {
        log.info("Getting request list data for {}", processInstanceId);
        HistoricProcessInstance instance = requestService.getProcessInstance(processInstanceId);
        // copy request list representation data
        RequestListRepresentation request = new RequestListRepresentation();
        transferBasicData(instance, request);
        return request;
    }

    @Cacheable("requestlistdata")
    public RequestListRepresentation getRequestListDataCached(String processInstanceId) {
        return getRequestListData(processInstanceId);
    }

    public void transferBasicData(HistoricProcessInstance instance, RequestListRepresentation request) {
        request.setProcessInstanceId(instance.getId());
        request.setProcessId(instance.getProcessDefinitionId());
        request.setRequestNumber(requestPropertiesService.getRequestNumber(instance.getId()));
        request.setDateSubmitted(requestPropertiesService.getDateSubmitted(instance.getId()));
        request.setExcerptListUploaded(excerptListService.hasExcerptList(instance.getId()));
        request.setDataAttachmentCount(requestPropertiesService.getDataAttachmentCount(instance.getId()));
        {
            RequestListRepresentation parent = requestPropertiesService.getParentListRepresentation(instance.getId());
            if (parent != null) {
                request.setParent(parent);
            }
        }

        String lastAssignee = requestPropertiesService.getLastAssignee(instance.getId());
        String lastAssigneeName = userService.getFullNameByUserId(lastAssignee, false);
        request.setLastAssignee(lastAssigneeName);

        // Set biobank request number, germline mutation and billing address from properties
        RequestProperties properties = requestPropertiesService.findByProcessInstanceId(instance.getId());
        request.setBiobankRequestNumber(properties.getBiobankRequestNumber());
        request.setGermlineMutation(properties.getGermlineMutation());

        Map<String, Object> variables = instance.getProcessVariables();
        if (variables != null) {
            request.setTitle((String)variables.get("title"));
            request.setBackground((String)variables.get("background"));
            request.setResearchQuestion((String)variables.get("research_question"));
            request.setHypothesis((String) variables.get("hypothesis"));
            request.setMethods((String) variables.get("methods"));

            request.setPathologistName((String)variables.get("pathologist_name"));
            String pathologistEmail = (String)variables.get("pathologist_email");

            if (pathologistEmail != null) {
                request.setPathologistEmail(pathologistEmail.trim().toLowerCase());
            }

            request.setContactPersonName((String)variables.get("contact_person_name"));
            String contactPersonEmail = (String)variables.get("contact_person_email");

            if(contactPersonEmail != null) {
                request.setContactPersonEmail(contactPersonEmail.trim().toLowerCase());
            }

            request.setStatus(RequestStatus.forDescription((String)variables.get("status")));
            request.setDateCreated((Date)variables.get("date_created"));
            String requesterId = variables.get("requester_id") == null ? "" : variables.get("requester_id").toString();
            Long userId = null;
            try {
                userId = Long.valueOf(requesterId);
            } catch(NumberFormatException e) {
                // ignore
            }
            if (userId != null) {
                User user = userService.findOneCached(userId);
                if (user != null) {
                    request.setRequesterId(userId);
                    request.setRequesterName(user.getFullName());
                }
            }
            request.setStatisticsRequest(fetchBooleanVariable("is_statistics_request", variables));
            request.setExcerptsRequest(fetchBooleanVariable("is_excerpts_request", variables));
            request.setPaReportRequest(fetchBooleanVariable("is_pa_report_request", variables));
            request.setMaterialsRequest(fetchBooleanVariable("is_materials_request", variables));
            request.setClinicalDataRequest(fetchBooleanVariable("is_clinical_data_request", variables));
            request.setReopenRequest(fetchBooleanVariable("reopen_request", variables));
            request.setLinkageWithPersonalData(fetchBooleanVariable("is_linkage_with_personal_data", variables));
            request.setInformedConsent(fetchBooleanVariable("is_informed_consent", variables));

            request.setReopenRequest(fetchBooleanVariable("reopen_request", variables));
            request.setSkipStatusApproval(fetchBooleanVariable("skip_status_approval", variables));
            request.setDateAssigned((Date)variables.get("assigned_date"));
        }
    }

    /**
     * Transfer fields stored in request properties.
     */
    public void transferPropertiesData(String processInstanceId, RequestListRepresentation request) {
        RequestProperties properties = requestPropertiesService.findByProcessInstanceId(
                processInstanceId);
        request.setSearchCriteria(properties.getSearchCriteria());
        request.setStudyPeriod(properties.getStudyPeriod());
        request.setLaboratoryTechniques(properties.getLaboratoryTechniques());
    }

    private static Set<RequestStatus> excerptListStatuses = new HashSet<>();
    static {
        excerptListStatuses.add(RequestStatus.DATA_DELIVERY);
        excerptListStatuses.add(RequestStatus.SELECTION_REVIEW);
        excerptListStatuses.add(RequestStatus.LAB_REQUEST);
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
        boolean isPalga = currentUser == null ? false : currentUser.isPalga();
        boolean isScientificCouncil = currentUser == null ? false : currentUser.isScientificCouncilMember();
        request.setProcessInstanceId(instance.getId());
        request.setProcessId(instance.getProcessDefinitionId());
        //request.setActivityId(instance.getActivityId()); // fetch from runtimeService?

        Map<String, Object> variables = instance.getProcessVariables();
        if (variables != null) {
            request.setStatus(RequestStatus.forDescription((String)variables.get("status")));
            request.setDateCreated((Date)variables.get("date_created"));
            request.setDateAssigned((Date)variables.get("assigned_date"));

            request.setContactPersonName((String)variables.get("contact_person_name"));
            String contactPersonEmail = (String)variables.get("contact_person_email");

            if(contactPersonEmail != null) {
                request.setContactPersonEmail(contactPersonEmail.trim().toLowerCase());
            }

            request.setTitle((String)variables.get("title"));
            request.setBackground((String)variables.get("background"));
            request.setResearchQuestion((String)variables.get("research_question"));
            request.setHypothesis((String) variables.get("hypothesis"));
            request.setMethods((String) variables.get("methods"));
            
            request.setPathologistName((String)variables.get("pathologist_name"));
            String pathologistEmail = (String)variables.get("pathologist_email");

            if (pathologistEmail != null) {
                request.setPathologistEmail(pathologistEmail.trim().toLowerCase());
            }

            request.setPreviousContact(fetchBooleanVariable("previous_contact", variables));
            request.setPreviousContactDescription((String)variables.get("previous_contact_description"));

            request.setStatisticsRequest(fetchBooleanVariable("is_statistics_request", variables));
            request.setExcerptsRequest(fetchBooleanVariable("is_excerpts_request", variables));
            request.setPaReportRequest(fetchBooleanVariable("is_pa_report_request", variables));
            request.setMaterialsRequest(fetchBooleanVariable("is_materials_request", variables));
            request.setClinicalDataRequest(fetchBooleanVariable("is_clinical_data_request", variables));

            request.setReopenRequest(fetchBooleanVariable("reopen_request", variables));

            request.setSkipStatusApproval(fetchBooleanVariable("skip_status_approval", variables));

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
                    request.setRequesterName(user.getFullName());
                    if (user.getContactData() != null) {
                        request.setRequesterEmail(user.getContactData().getEmail());
                    }
                    request.setRequester(new ProfileRepresentation(user));
                    request.setLab(user.getLab());
                }
            }
            Task task = null;
            switch(request.getStatus()) {
                case REVIEW:
                    task = requestService.findTaskByRequestId(instance.getId(), "palga_request_review");
                    break;
                case APPROVAL:
                    task = requestService.findTaskByRequestId(instance.getId(), "request_approval");
                    break;
                case DATA_DELIVERY:
                    task = requestService.findTaskByRequestId(instance.getId(), "data_delivery"); 
                    break;
                case SELECTION_REVIEW:
                    task = requestService.findTaskByRequestId(instance.getId(), "selection_review"); 
                    break;
                default:
                    break;
            }
            if (task != null) {
                String assigneeId = task.getAssignee();
                request.setAssignee(assigneeId);
                request.setAssigneeName(userService.getFullNameByUserId(assigneeId, false));
            }
            request.setExcerptListUploaded(excerptListService.hasExcerptList(instance.getId()));
            request.setDataAttachmentCount(requestPropertiesService.getDataAttachmentCount(instance.getId()));
            RequestProperties properties = requestPropertiesService.findByProcessInstanceId(instance.getId());
            request.setRequestNumber(properties.getRequestNumber());
            request.setDateSubmitted(properties.getDateSubmitted());
            request.setReviewStatus(properties.getReviewStatus());

            request.setSearchCriteria(properties.getSearchCriteria());
            request.setStudyPeriod(properties.getStudyPeriod());
            request.setLaboratoryTechniques(properties.getLaboratoryTechniques());

            request.setBillingAddress(properties.getBillingAddress());
            request.setChargeNumber(properties.getChargeNumber());
            request.setGrantProvider(properties.getGrantProvider());
            request.setBiobankRequestNumber(properties.getBiobankRequestNumber());
            request.setGermlineMutation(properties.getGermlineMutation());
            request.setResearchNumber(properties.getReseachNumber());

            {
                RequestProperties parentProperties = properties.getParent();
                if (parentProperties != null) {
                    RequestRepresentation parent = new RequestRepresentation();
                    parent.setRequestNumber(parentProperties.getRequestNumber());
                    parent.setProcessInstanceId(parentProperties.getProcessInstanceId());
                    parent.setStatus(RequestStatus.forDescription((String)
                            historyService.createHistoricVariableInstanceQuery()
                                .processInstanceId(parentProperties.getProcessInstanceId())
                                .variableName("status")
                                .singleResult()
                                .getValue()));
                    request.setParent(parent);
                }
            }
            if (properties.getChildren() != null) {
                List<RequestRepresentation> children = new ArrayList<>();
                for (RequestProperties childProperties: properties.getChildren()) {
                    RequestRepresentation child = new RequestRepresentation();
                    child.setRequestNumber(childProperties.getRequestNumber());
                    child.setProcessInstanceId(childProperties.getProcessInstanceId());
                    child.setStatus(RequestStatus.forDescription((String)
                            historyService.createHistoricVariableInstanceQuery()
                            .processInstanceId(childProperties.getProcessInstanceId())
                            .variableName("status")
                            .singleResult()
                            .getValue()));
                    children.add(child);
                }
                request.setChildren(children);
            }

            List<FileRepresentation> requestAttachments = new ArrayList<>();
            for(File file: properties.getRequestAttachments()) {
                requestAttachments.add(new FileRepresentation(file));
            }
            request.setAttachments(requestAttachments);

            List<FileRepresentation> informedConsentFormAttachments = new ArrayList<>();
            for(File file: properties.getInformedConsentFormAttachments()) {
                informedConsentFormAttachments.add(new FileRepresentation(file));
            }
            request.setInformedConsentFormAttachments(informedConsentFormAttachments);

            request.setPrivacyCommitteeRationale(properties.getPrivacyCommitteeRationale());
            request.setPrivacyCommitteeOutcome(properties.getPrivacyCommitteeOutcome());
            request.setPrivacyCommitteeOutcomeRef(properties.getPrivacyCommitteeOutcomeRef());
            request.setPrivacyCommitteeEmails(properties.getPrivacyCommitteeEmails());

            if (isPalga) {
                List<FileRepresentation> agreementAttachments = new ArrayList<>();
                for(File file: properties.getAgreementAttachments()) {
                    agreementAttachments.add(new FileRepresentation(file));
                }
                request.setAgreementAttachments(agreementAttachments);
            }

            if (isPalga || isScientificCouncil) {
                List<CommentRepresentation> comments = new ArrayList<>();
                for (Comment comment: properties.getComments()) {
                    comments.add(new CommentRepresentation(comment));
                }
                request.setComments(comments);

                List<CommentRepresentation> approvalComments = new ArrayList<>();
                for (Comment comment: properties.getApprovalComments()) {
                    approvalComments.add(new CommentRepresentation(comment));
                }
                request.setApprovalComments(approvalComments);

                Map<Long, ApprovalVoteRepresentation> approvalVotes = new HashMap<>();
                for (Entry<Long, ApprovalVote> entry: properties.getApprovalVotes().entrySet()) {
                    approvalVotes.put(entry.getKey(), new ApprovalVoteRepresentation(entry.getValue()));
                }
                request.setApprovalVotes(approvalVotes);
            }

            if (isPalga) {
                request.setRequesterValid(fetchBooleanVariable("requester_is_valid", variables));
                request.setRequesterAllowed(fetchBooleanVariable("requester_is_allowed", variables));
                request.setContactPersonAllowed(fetchBooleanVariable("contact_person_is_allowed", variables));
                request.setRequesterLabValid(fetchBooleanVariable("requester_lab_is_valid", variables));
                request.setAgreementReached(fetchBooleanVariable("agreement_reached", variables));
                request.setAgreementNotApplicable(fetchBooleanVariable("agreement_not_applicable", variables));

                request.setRequestAdmissible(fetchBooleanVariable("request_is_admissible", variables));

                request.setScientificCouncilApproved(fetchBooleanVariable("scientific_council_approved", variables));
                request.setPrivacyCommitteeApproved(fetchBooleanVariable("privacy_committee_approved", variables));

                request.setRequestApproved(fetchBooleanVariable("request_approved", variables));
                request.setRejectReason((String)variables.get("reject_reason"));
                request.setRejectDate((Date)variables.get("reject_date"));
            }

            List<FileRepresentation> medicalEthicalCommitteeApprovalAttachments = new ArrayList<>();
            for (File file: properties.getMedicalEthicalCommiteeApprovalAttachments()) {
                medicalEthicalCommitteeApprovalAttachments.add(new FileRepresentation(file));
            }
            request.setMedicalEthicalCommitteeApprovalAttachments(medicalEthicalCommitteeApprovalAttachments);

            if (!isScientificCouncil) {
                List<FileRepresentation> dataAttachments = new ArrayList<>();
                for(File file: properties.getDataAttachments()) {
                    dataAttachments.add(new FileRepresentation(file));
                }
                request.setDataAttachments(dataAttachments);

                if (excerptListStatuses.contains(request.getStatus())) {
                    Date start = new Date();
                    ExcerptList excerptList = excerptListService.findByProcessInstanceId(instance.getId());
                    if (excerptList != null) {
                        log.info("Set excerpt list info.");
                        request.setExcerptListAttachment(new FileRepresentation(properties.getExcerptListAttachment()));
                        ExcerptListRepresentation excerptListRepresentation
                            = new ExcerptListRepresentation(excerptList);
                        Integer entryCount = excerptListService.countEntriesByExcerptListId(excerptList.getId());
                        excerptListRepresentation.setEntryCount(entryCount);
                        Integer selectedCount = excerptListService.countSelectedEntriesByExcerptListId(excerptList.getId());
                        excerptListRepresentation.setSelectedCount(selectedCount);
                        request.setExcerptList(excerptListRepresentation);
                        @SuppressWarnings("unchecked")
                        Collection<Integer> selectedLabs = (Collection<Integer>)variables.get("lab_request_labs");
                        Set<Integer> selectedLabSet = new TreeSet<>();
                        if (selectedLabs != null) {
                            selectedLabSet.addAll(selectedLabs);
                        }
                        request.setSelectedLabs(selectedLabSet);
                        request.setExcerptListRemark(excerptList.getRemark());
                    }
                    Date end = new Date();
                    log.info("Fetching excerpt list info took " + (end.getTime() - start.getTime()) + " ms.");
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
    @CacheEvict(value = "requestlistdata", key = "#instance.processInstanceId")
    public Map<String, Object> transferFormData(RequestRepresentation request, HistoricProcessInstance instance, User user) {
        request.setProcessInstanceId(instance.getId());
        Map<String, Object> variables = instance.getProcessVariables();
        if (variables == null) {
            return null;
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

        variables.put("is_statistics_request", request.isStatisticsRequest());
        variables.put("is_excerpts_request", request.isExcerptsRequest());
        variables.put("is_pa_report_request", request.isPaReportRequest());
        variables.put("is_materials_request", request.isMaterialsRequest());
        variables.put("is_clinical_data_request", request.isClinicalDataRequest());

        variables.put("pathologist_name", request.getPathologistName());
        variables.put("pathologist_email", request.getPathologistEmail());
        variables.put("previous_contact", request.isPreviousContact());
        variables.put("previous_contact_description", request.getPreviousContactDescription());

        variables.put("is_linkage_with_personal_data", request.isLinkageWithPersonalData());
        variables.put("linkage_with_personal_data_notes", request.getLinkageWithPersonalDataNotes());
        variables.put("is_informed_consent", request.isInformedConsent());
        variables.put("reason_using_personal_data", request.getReasonUsingPersonalData());

        variables.put("return_date", request.getReturnDate());
        variables.put("contact_person_name", request.getContactPersonName());
        variables.put("contact_person_email", request.getContactPersonEmail());
        RequestProperties properties = requestPropertiesService.findByProcessInstanceId(instance.getId());

        properties.setBiobankRequestNumber(request.getBiobankRequestNumber());
        properties.setGermlineMutation(request.isGermlineMutation());
        properties.setSearchCriteria(request.getSearchCriteria());
        properties.setStudyPeriod(request.getStudyPeriod());
        properties.setLaboratoryTechniques(request.getLaboratoryTechniques());

        properties.setChargeNumber(request.getChargeNumber());
        properties.setGrantProvider(request.getGrantProvider());
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
            variables.put("requester_is_valid", request.isRequesterValid());
            variables.put("requester_is_allowed", request.isRequesterAllowed());
            variables.put("contact_person_is_allowed", request.isContactPersonAllowed());
            variables.put("requester_lab_is_valid", request.isRequesterLabValid());
            variables.put("agreement_reached", request.isAgreementReached());
            variables.put("agreement_not_applicable", request.isAgreementNotApplicable());

            variables.put("request_is_admissible", request.isRequestAdmissible());

            variables.put("reopen_request", request.isReopenRequest());

            variables.put("skip_status_approval", request.isSkipStatusApproval());

            variables.put("scientific_council_approved", request.isScientificCouncilApproved());
            variables.put("privacy_committee_approved", request.isPrivacyCommitteeApproved());

            variables.put("request_approved", request.isRequestApproved());
            variables.put("reject_reason", request.getRejectReason());
            variables.put("reject_date", request.getRejectDate());

            if (!(request.isRequesterValid()
                    && request.isRequesterAllowed()
                    && request.isContactPersonAllowed()
                    && request.isRequesterLabValid()
                    && (request.isAgreementReached()
                        || (request.isAgreementNotApplicable() && request.isSkipStatusApproval())))) {
                log.info("Request not admissible");
                variables.put("request_is_admissible", Boolean.FALSE);
            }
            properties.setPrivacyCommitteeRationale(request.getPrivacyCommitteeRationale());
            properties.setPrivacyCommitteeOutcome(request.getPrivacyCommitteeOutcome());
            properties.setPrivacyCommitteeOutcomeRef(request.getPrivacyCommitteeOutcomeRef());
            properties.setPrivacyCommitteeEmails(request.getPrivacyCommitteeEmails());
        }
        requestPropertiesService.save(properties);
        return variables;
    }

    /**
     * Update the value of last request assignee
     * @param instanceId the Activiti process instance ID.
     * @param lastAssigneeId id of the last assignee that claimed the request
     */
    @Transactional
    @CacheEvict(value = "lastassignee", key = "#instanceId")
    public void updateLastAssignee(String instanceId, String lastAssigneeId) {
        RequestProperties properties = requestPropertiesService.findByProcessInstanceId(instanceId);
        properties.setLastAssignee(lastAssigneeId);
        requestPropertiesService.save(properties);
    }

    @CacheEvict(value = "requestlistdata", key = "#id")
    public void invalidateCacheEntry(String id) {
        log.info("Invalidating request list cache for request {}", id);
    }

}
