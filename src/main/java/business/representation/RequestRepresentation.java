package business.representation;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import business.models.Lab;

public class RequestRepresentation {

    private String processInstanceId;
    private String activityId;
    private String requesterId;
    private String requesterName;
    private String requesterEmail;
    private ProfileRepresentation requester;
    private String status;
    private Date dateCreated;
    private Lab lab;
    private String assignee;
    private String assigneeName;
    private Date dateAssigned;

    private String title;
    private String background;
    private String researchQuestion;
    private String hypothesis;
    private String methods;

    private boolean statisticsRequest;
    private boolean excerptsRequest;
    private boolean paReportRequest;
    private boolean materialsRequest;

    private boolean linkageWithPersonalData;
    private String linkageWithPersonalDataNotes;
    private boolean informedConsent;
    private String reasonUsingPersonalData;

    private Date returnDate;
    private String contactPersonName;

    private List<AttachmentRepresentation> attachments;
    private List<AttachmentRepresentation> agreementAttachments;
    private List<AttachmentRepresentation> dataAttachments;
    private List<CommentRepresentation> comments;

    private List<CommentRepresentation> approvalComments;
    private Map<Long, ApprovalVoteRepresentation> approvalVotes;
    
    private ExcerptListRepresentation excerptList;
    
    private Set<Integer> selectedLabs;
    
    // Palga specific
    private boolean requesterValid;
    private boolean requesterAllowed;
    private boolean contactPersonAllowed;
    private boolean requesterLabValid;
    private boolean agreementReached;
    private boolean requestAdmissible;
    
    private boolean scientificCouncilApproved;
    private boolean privacyCommitteeApproved;
    
    private boolean requestApproved;
    private String rejectReason;
    private Date rejectDate;

    // Privacy Committee
    private boolean sentToPrivacyCommittee;
    private String privacyCommitteeOutcome;
    private String privacyCommitteeOutcomeRef;
    private String privacyCommitteeEmails;

    private String excerptListRemark;

    public RequestRepresentation() {

    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public String getActivityId() {
        return activityId;
    }

    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }

    public String getRequesterId() {
        return requesterId;
    }

    public void setRequesterId(String requesterId) {
        this.requesterId = requesterId;
    }

    public String getRequesterName() {
        return requesterName;
    }

    public void setRequesterName(String requesterName) {
        this.requesterName = requesterName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBackground() {
        return background;
    }

    public void setBackground(String background) {
        this.background = background;
    }

    public String getResearchQuestion() {
        return researchQuestion;
    }

    public void setResearchQuestion(String researchQuestion) {
        this.researchQuestion = researchQuestion;
    }

    public String getHypothesis() {
        return hypothesis;
    }

    public void setHypothesis(String hypothesis) {
        this.hypothesis = hypothesis;
    }

    public String getMethods() {
        return methods;
    }

    public void setMethods(String methods) {
        this.methods = methods;
    }

    public boolean isStatisticsRequest() {
        return statisticsRequest;
    }

    public void setStatisticsRequest(boolean statisticsRequest) {
        this.statisticsRequest = statisticsRequest;
    }

    public boolean isExcerptsRequest() {
        return excerptsRequest;
    }

    public void setExcerptsRequest(boolean excerptsRequest) {
        this.excerptsRequest = excerptsRequest;
    }

    public boolean isPaReportRequest() {
        return paReportRequest;
    }

    public void setPaReportRequest(boolean paReportRequest) {
        this.paReportRequest = paReportRequest;
    }

    public boolean isMaterialsRequest() {
        return materialsRequest;
    }

    public void setMaterialsRequest(boolean materialsRequest) {
        this.materialsRequest = materialsRequest;
    }

    public boolean isLinkageWithPersonalData() {
        return linkageWithPersonalData;
    }

    public void setLinkageWithPersonalData(boolean linkageWithPersonalData) {
        this.linkageWithPersonalData = linkageWithPersonalData;
    }

    public String getLinkageWithPersonalDataNotes() {
        return linkageWithPersonalDataNotes;
    }

    public void setLinkageWithPersonalDataNotes(String linkageWithPersonalDataNotes) {
        this.linkageWithPersonalDataNotes = linkageWithPersonalDataNotes;
    }

    public boolean isInformedConsent() {
        return informedConsent;
    }

    public void setInformedConsent(boolean informedConsent) {
        this.informedConsent = informedConsent;
    }

    public String getReasonUsingPersonalData() {
        return reasonUsingPersonalData;
    }

    public void setReasonUsingPersonalData(String reasonUsingPersonalData) {
        this.reasonUsingPersonalData = reasonUsingPersonalData;
    }

    public Date getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(Date returnDate) {
        this.returnDate = returnDate;
    }

    public String getContactPersonName() {
        return contactPersonName;
    }

    public void setContactPersonName(String contactPersonName) {
        this.contactPersonName = contactPersonName;
    }

    public boolean isRequesterValid() {
        return requesterValid;
    }

    public void setRequesterValid(boolean requesterValid) {
        this.requesterValid = requesterValid;
    }

    public boolean isRequesterAllowed() {
        return requesterAllowed;
    }

    public void setRequesterAllowed(boolean requesterAllowed) {
        this.requesterAllowed = requesterAllowed;
    }

    public boolean isContactPersonAllowed() {
        return contactPersonAllowed;
    }

    public void setContactPersonAllowed(boolean contactPersonAllowed) {
        this.contactPersonAllowed = contactPersonAllowed;
    }

    public boolean isRequesterLabValid() {
        return requesterLabValid;
    }

    public void setRequesterLabValid(boolean requesterLabValid) {
        this.requesterLabValid = requesterLabValid;
    }

    public Lab getLab() {
        return lab;
    }

    public void setLab(Lab lab) {
        this.lab = lab;
    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    public String getAssigneeName() {
        return assigneeName;
    }

    public Date getDateAssigned() {
        return dateAssigned;
    }

    public void setDateAssigned(Date dateAssigned) {
        this.dateAssigned = dateAssigned;
    }

    public void setAssigneeName(String assigneeName) {
        this.assigneeName = assigneeName;
    }

    public List<AttachmentRepresentation> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<AttachmentRepresentation> attachments) {
        this.attachments = attachments;
    }

    public List<AttachmentRepresentation> getAgreementAttachments() {
        return agreementAttachments;
    }

    public void setAgreementAttachments(List<AttachmentRepresentation> agreementAttachments) {
        this.agreementAttachments = agreementAttachments;
    }

    public List<AttachmentRepresentation> getDataAttachments() {
        return dataAttachments;
    }

    public void setDataAttachments(List<AttachmentRepresentation> dataAttachments) {
        this.dataAttachments = dataAttachments;
    }

    public boolean isAgreementReached() {
        return agreementReached;
    }

    public void setAgreementReached(boolean agreementReached) {
        this.agreementReached = agreementReached;
    }
    
    public boolean isRequestAdmissible() {
        return requestAdmissible;
    }

    public void setRequestAdmissible(boolean requestAdmissible) {
        this.requestAdmissible = requestAdmissible;
    }

    public List<CommentRepresentation> getComments() {
        return comments;
    }

    public void setComments(List<CommentRepresentation> comments) {
        this.comments = comments;
    }

    public List<CommentRepresentation> getApprovalComments() {
        return approvalComments;
    }

    public void setApprovalComments(List<CommentRepresentation> approvalComments) {
        this.approvalComments = approvalComments;
    }

    public Map<Long, ApprovalVoteRepresentation> getApprovalVotes() {
        return approvalVotes;
    }

    public void setApprovalVotes(Map<Long, ApprovalVoteRepresentation> approvalVotes) {
        this.approvalVotes = approvalVotes;
    }

    public String getPrivacyCommitteeOutcomeRef() {
        return privacyCommitteeOutcomeRef;
    }

    public void setPrivacyCommitteeOutcomeRef(String privacyCommitteeOutcomeRef) {
        this.privacyCommitteeOutcomeRef = privacyCommitteeOutcomeRef;
    }

    public String getPrivacyCommitteeEmails() {
        return privacyCommitteeEmails;
    }

    public void setPrivacyCommitteeEmails(String privacyCommitteeEmails) {
        this.privacyCommitteeEmails = privacyCommitteeEmails;
    }

    public boolean isSentToPrivacyCommittee() {
        return sentToPrivacyCommittee;
    }

    public void setSentToPrivacyCommittee(boolean sentToPrivacyCommittee) {
        this.sentToPrivacyCommittee = sentToPrivacyCommittee;
    }

    public String getPrivacyCommitteeOutcome() {
        return privacyCommitteeOutcome;
    }

    public void setPrivacyCommitteeOutcome(String privacyCommitteeOutcome) {
        this.privacyCommitteeOutcome = privacyCommitteeOutcome;
    }

    public ExcerptListRepresentation getExcerptList() {
        return excerptList;
    }

    public void setExcerptList(ExcerptListRepresentation excerptList) {
        this.excerptList = excerptList;
    }
    
    public Set<Integer> getSelectedLabs() {
        return selectedLabs;
    }

    public void setSelectedLabs(Set<Integer> selectedLabs) {
        this.selectedLabs = selectedLabs;
    }

    public boolean isScientificCouncilApproved() {
        return scientificCouncilApproved;
    }

    public void setScientificCouncilApproved(boolean scientificCouncilApproved) {
        this.scientificCouncilApproved = scientificCouncilApproved;
    }

    public boolean isPrivacyCommitteeApproved() {
        return privacyCommitteeApproved;
    }

    public void setPrivacyCommitteeApproved(boolean privacyCommitteeApproved) {
        this.privacyCommitteeApproved = privacyCommitteeApproved;
    }

    public boolean isRequestApproved() {
        return requestApproved;
    }

    public void setRequestApproved(boolean requestApproved) {
        this.requestApproved = requestApproved;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }

    public Date getRejectDate() {
        return rejectDate;
    }

    public void setRejectDate(Date rejectDate) {
        this.rejectDate = rejectDate;
    }

    public String getRequesterEmail() {
        return requesterEmail;
    }

    public void setRequesterEmail(String requesterEmail) {
        this.requesterEmail = requesterEmail;
    }

    public ProfileRepresentation getRequester() {
        return requester;
    }

    public void setRequester(ProfileRepresentation requester) {
        this.requester = requester;
    }

    public String getExcerptListRemark() {
        return excerptListRemark;
    }

    public void setExcerptListRemark(String excerptListRemark) {
        this.excerptListRemark = excerptListRemark;
    } 
    
}
