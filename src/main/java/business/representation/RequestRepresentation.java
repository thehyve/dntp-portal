package business.representation;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import business.models.ContactData;
import business.models.Lab;
import business.models.RequestProperties.ReviewStatus;

public class RequestRepresentation {

    private String processInstanceId;
    private String requestNumber;
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

    private ReviewStatus reviewStatus;

    private String title;
    private String background;
    private String researchQuestion;
    private String hypothesis;
    private String methods;

    private String pathologistName;
    private String pathologistEmail;
    private boolean previousContact;
    private String previousContactDescription;
    
    private ContactData billingAddress;
    private String chargeNumber;
    private String researchNumber;
    
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

    private List<FileRepresentation> attachments;
    private List<FileRepresentation> agreementAttachments;
    private List<FileRepresentation> dataAttachments;
    private List<FileRepresentation> medicalEthicalCommitteeApprovalAttachments;
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

    private boolean selectionApproved;
    
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

    public String getRequestNumber() {
        return requestNumber;
    }

    public void setRequestNumber(String requestNumber) {
        this.requestNumber = requestNumber;
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

    public ReviewStatus getReviewStatus() {
        return reviewStatus;
    }

    public void setReviewStatus(ReviewStatus reviewStatus) {
        this.reviewStatus = reviewStatus;
    }

    public List<FileRepresentation> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<FileRepresentation> attachments) {
        this.attachments = attachments;
    }

    public List<FileRepresentation> getAgreementAttachments() {
        return agreementAttachments;
    }

    public void setAgreementAttachments(
            List<FileRepresentation> agreementAttachments) {
        this.agreementAttachments = agreementAttachments;
    }

    public List<FileRepresentation> getDataAttachments() {
        return dataAttachments;
    }

    public void setDataAttachments(List<FileRepresentation> dataAttachments) {
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

    public String getPathologistName() {
        return pathologistName;
    }

    public void setPathologistName(String pathologistName) {
        this.pathologistName = pathologistName;
    }

    public String getPathologistEmail() {
        return pathologistEmail;
    }

    public void setPathologistEmail(String pathologistEmail) {
        this.pathologistEmail = pathologistEmail;
    }

    public boolean isPreviousContact() {
        return previousContact;
    }

    public void setPreviousContact(boolean previousContact) {
        this.previousContact = previousContact;
    }

    public String getPreviousContactDescription() {
        return previousContactDescription;
    }

    public void setPreviousContactDescription(String previousContactDescription) {
        this.previousContactDescription = previousContactDescription;
    }

    public boolean isSelectionApproved() {
        return selectionApproved;
    }

    public void setSelectionApproved(boolean selectionApproved) {
        this.selectionApproved = selectionApproved;
    }

    public List<FileRepresentation> getMedicalEthicalCommitteeApprovalAttachments() {
        return medicalEthicalCommitteeApprovalAttachments;
    }

    public void setMedicalEthicalCommitteeApprovalAttachments(
            List<FileRepresentation> medicalEthicalCommitteeApprovalAttachments) {
        this.medicalEthicalCommitteeApprovalAttachments = medicalEthicalCommitteeApprovalAttachments;
    }

    public ContactData getBillingAddress() {
        return billingAddress;
    }

    public void setBillingAddress(ContactData billingAddress) {
        this.billingAddress = billingAddress;
    }

    public String getChargeNumber() {
        return chargeNumber;
    }

    public void setChargeNumber(String chargeNumber) {
        this.chargeNumber = chargeNumber;
    }

    public String getResearchNumber() {
        return researchNumber;
    }

    public void setResearchNumber(String researchNumber) {
        this.researchNumber = researchNumber;
    } 
    
}
