/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business.representation;

import java.util.Date;
import java.util.List;

import business.models.ContactData;
import business.models.RequestProperties.ReviewStatus;

public class RequestListRepresentation implements RequestListElement {

    private String processInstanceId;
    private String processId;
    private String requestNumber;
    private Long requesterId;
    private String requesterName;
    private RequestStatus status = RequestStatus.NONE;
    private Date dateCreated;
    private Date dateSubmitted;

    private ReviewStatus reviewStatus;

    private String assignee;
    private String assigneeName;
    private Date dateAssigned;

    private RequestListRepresentation parent;

    private String title;
    private String background;
    private String researchQuestion;
    private String hypothesis;
    private String methods;

    private String searchCriteria;
    private String studyPeriod;
    private String laboratoryTechniques;

    private String pathologistName;
    private String pathologistEmail;
    private String contactPersonName;
    private String contactPersonEmail;

    private boolean statisticsRequest;
    private boolean excerptsRequest;
    private boolean paReportRequest;
    private boolean materialsRequest;
    private boolean clinicalDataRequest;

    private boolean linkageWithPersonalData;
    private boolean informedConsent;

    private boolean reopenRequest;
    private boolean skipStatusApproval;

    private String approvalVote;
    private Long numberOfApprovalVotes;

    private List<FileRepresentation> attachments;
    private List<FileRepresentation> agreementAttachments;
    private List<FileRepresentation> dataAttachments;
    private List<FileRepresentation> medicalEthicalCommitteeApprovalAttachments;

    private boolean excerptListUploaded;

    private String biobankRequestNumber;

    private Boolean germlineMutation;

    private Long dataAttachmentCount;

    public RequestListRepresentation() {

    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    /**
     * The version of the process.
     * @return the process id.
     */
    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public String getRequestNumber() {
        return requestNumber;
    }

    public void setRequestNumber(String requestNumber) {
        this.requestNumber = requestNumber;
    }

    public Long getRequesterId() {
        return requesterId;
    }

    public void setRequesterId(Long requesterId) {
        this.requesterId = requesterId;
    }

    public String getRequesterName() {
        return requesterName;
    }

    public void setRequesterName(String requesterName) {
        this.requesterName = requesterName;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Date getDateSubmitted() {
        return dateSubmitted;
    }

    public void setDateSubmitted(Date dateSubmitted) {
        this.dateSubmitted = dateSubmitted;
    }

    public ReviewStatus getReviewStatus() {
        return reviewStatus;
    }

    public void setReviewStatus(ReviewStatus reviewStatus) {
        this.reviewStatus = reviewStatus;
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

    public String getSearchCriteria() {
        return searchCriteria;
    }

    public void setSearchCriteria(String searchCriteria) {
        this.searchCriteria = searchCriteria;
    }

    public String getStudyPeriod() {
        return studyPeriod;
    }

    public void setStudyPeriod(String studyPeriod) {
        this.studyPeriod = studyPeriod;
    }

    public String getLaboratoryTechniques() {
        return laboratoryTechniques;
    }

    public void setLaboratoryTechniques(String laboratoryTechniques) {
        this.laboratoryTechniques = laboratoryTechniques;
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

    public String getContactPersonName() {
        return contactPersonName;
    }

    public void setContactPersonName(String contactPersonName) {
        this.contactPersonName = contactPersonName;
    }

    public String getContactPersonEmail() {
        return contactPersonEmail;
    }

    public void setContactPersonEmail(String contactPersonEmail) {
        this.contactPersonEmail = contactPersonEmail;
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

    public RequestListRepresentation getParent() {
        return parent;
    }

    public void setParent(RequestListRepresentation parent) {
        this.parent = parent;
    }

    public void setAssigneeName(String assigneeName) {
        this.assigneeName = assigneeName;
    }

    public String getApprovalVote() {
        return approvalVote;
    }

    public void setApprovalVote(String approvalVote) {
        this.approvalVote = approvalVote;
    }

    public Long getNumberOfApprovalVotes() {
        return numberOfApprovalVotes;
    }

    public void setNumberOfApprovalVotes(Long numberOfApprovalVotes) {
        this.numberOfApprovalVotes = numberOfApprovalVotes;
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

    public boolean isClinicalDataRequest() {
        return clinicalDataRequest;
    }

    public void setClinicalDataRequest(boolean clinicalDataRequest) {
        this.clinicalDataRequest = clinicalDataRequest;
    }

    public boolean isLinkageWithPersonalData() {
        return linkageWithPersonalData;
    }

    public void setLinkageWithPersonalData(boolean linkageWithPersonalData) {
        this.linkageWithPersonalData = linkageWithPersonalData;
    }

    public boolean isInformedConsent() {
        return informedConsent;
    }

    public void setInformedConsent(boolean informedConsent) {
        this.informedConsent = informedConsent;
    }

    public boolean isReopenRequest() {
        return reopenRequest;
    }

    public void setReopenRequest(boolean reopenRequest) {
        this.reopenRequest = reopenRequest;
    }

    public boolean isSkipStatusApproval() {
        return skipStatusApproval;
    }

    public void setSkipStatusApproval(boolean skipStatusApproval) {
        this.skipStatusApproval = skipStatusApproval;
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

    public void setAgreementAttachments(List<FileRepresentation> agreementAttachments) {
        this.agreementAttachments = agreementAttachments;
    }

    public List<FileRepresentation> getDataAttachments() {
        return dataAttachments;
    }

    public void setDataAttachments(List<FileRepresentation> dataAttachments) {
        this.dataAttachments = dataAttachments;
    }

    public List<FileRepresentation> getMedicalEthicalCommitteeApprovalAttachments() {
        return medicalEthicalCommitteeApprovalAttachments;
    }

    public void setMedicalEthicalCommitteeApprovalAttachments(
            List<FileRepresentation> medicalEthicalCommitteeApprovalAttachments) {
        this.medicalEthicalCommitteeApprovalAttachments = medicalEthicalCommitteeApprovalAttachments;
    }

    public boolean isExcerptListUploaded() {
        return excerptListUploaded;
    }

    public void setExcerptListUploaded(boolean excerptListUploaded) {
        this.excerptListUploaded = excerptListUploaded;
    }

    public Long getDataAttachmentCount() {
        return dataAttachmentCount;
    }

    public void setDataAttachmentCount(Long dataAttachmentCount) {
        this.dataAttachmentCount = dataAttachmentCount;
    }

    public String getBiobankRequestNumber(){
        return this.biobankRequestNumber;
    }

    public void setBiobankRequestNumber(String number){
        this.biobankRequestNumber = number;
    }

    public boolean getGermlineMutation() {
        if (germlineMutation == null){
            germlineMutation = false;
        }
        return germlineMutation;
    }

    public void setGermlineMutation(Boolean germlineMutation) {
        if (germlineMutation == null){
            germlineMutation = false;
        }
        this.germlineMutation = germlineMutation;
    }
}
