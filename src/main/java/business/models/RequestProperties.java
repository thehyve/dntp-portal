/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.*;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;


@Entity
@Table(indexes = @Index(columnList="processInstanceId"))
public class RequestProperties {

    public enum ReviewStatus {
        ACTIVE,
        SUSPENDED,
    }

    @Id
    @GeneratedValue
    private Long id;

    @Fetch(FetchMode.JOIN)
    @ManyToOne(fetch = FetchType.EAGER)
    private RequestProperties parent;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "parent")
    @BatchSize(size = 1000)
    @OrderColumn
    private List<RequestProperties> children = new ArrayList<>();

    @Column(unique = true)
    private String processInstanceId;

    @Column(unique = true)
    private String requestNumber;

    private ReviewStatus reviewStatus;

    @Column(length = 10000)
    private String searchCriteria;

    private String studyPeriod;

    @Column(length = 10000)
    private String laboratoryTechniques;

    @OneToOne
    private ContactData billingAddress;

    private String chargeNumber;

    private String grantProvider;
    private String biobankRequestNumber;
    private String reseachNumber;

    private String lastAssignee;

    private String requestType;

    private Boolean blockMaterialsRequest;
    private Boolean heSliceMaterialsRequest;
    private String otherMaterialsRequest;

    @Column
    private Boolean germlineMutation;
    /**
     * The datetime on which the request number is generated.
     */
    private Date dateSubmitted;

    @OneToMany
    private List<File> requestAttachments = new ArrayList<>();

    @OneToMany
    private List<File> informedConsentFormAttachments = new ArrayList<>();

    @OneToMany
    private List<File> agreementAttachments = new ArrayList<>();

    @OneToMany
    private List<File> dataAttachments = new ArrayList<>();

    @OneToMany
    @JoinTable(name = "request_properties_medical_ethical_commitee_approval_attachment",
            joinColumns = @JoinColumn(name="request_properties_id", referencedColumnName="id"),
            inverseJoinColumns = @JoinColumn(name="medical_ethical_commitee_approval_attachments_id", referencedColumnName="id"))
    private List<File> medicalEthicalCommiteeApprovalAttachments = new ArrayList<>();

    @OneToOne
    private File excerptListAttachment;

    @OrderBy("timeCreated")
    @OneToMany(cascade = CascadeType.ALL)
    @OrderColumn
    @BatchSize(size = 1000)
    private List<Comment> comments = new ArrayList<>();

    @OrderBy("timeCreated")
    @OneToMany(cascade = CascadeType.ALL)
    @OrderColumn
    @BatchSize(size = 1000)
    private List<Comment> approvalComments = new ArrayList<>();

    @Fetch(FetchMode.JOIN)
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @BatchSize(size = 1000)
    private Map<Long, ApprovalVote> approvalVotes = new HashMap<>();

    @Deprecated
    @Column
    private boolean sentToPrivacyCommittee;

    @Column(length = 10000)
    private String privacyCommitteeRationale;

    @Column(length = 10000)
    private String privacyCommitteeOutcome;

    @Column(length = 10000)
    private String privacyCommitteeOutcomeRef;

    @Column(columnDefinition="TEXT")
    private String privacyCommitteeEmails;

    public RequestProperties() {
        
    }
    
    public RequestProperties(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RequestProperties getParent() {
        return parent;
    }

    public void setParent(RequestProperties parent) {
        this.parent = parent;
    }

    public List<RequestProperties> getChildren() {
        return children;
    }

    public void setChildren(List<RequestProperties> children) {
        this.children = children;
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

    public List<Comment> getComments() {
        return comments;
    }

    public void addComment(Comment comment) {
        this.comments.add(comment);
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public List<Comment> getApprovalComments() {
        return approvalComments;
    }

    public void addApprovalComment(Comment comment) {
        this.approvalComments.add(comment);
    }

    public void setApprovalComments(List<Comment> approvalComments) {
        this.approvalComments = approvalComments;
    }

    public Map<Long, ApprovalVote> getApprovalVotes() {
        return approvalVotes;
    }

    public void addApprovalVote(User user, ApprovalVote approvalVote) {
        this.approvalVotes.put(user.getId(), approvalVote);
    }

    public void setApprovalVotes(Map<Long, ApprovalVote> approvalVotes) {
        this.approvalVotes = approvalVotes;
    }

    public List<File> getRequestAttachments() {
        return requestAttachments;
    }

    public void setRequestAttachments(List<File> requestAttachments) {
        this.requestAttachments = requestAttachments;
    }

    public List<File> getInformedConsentFormAttachments() {
        return informedConsentFormAttachments;
    }

    public void setInformedConsentFormAttachments(List<File> informedConsentFormAttachments) {
        this.informedConsentFormAttachments = informedConsentFormAttachments;
    }

    public List<File> getAgreementAttachments() {
        return agreementAttachments;
    }

    public void setAgreementAttachments(List<File> agreementAttachments) {
        this.agreementAttachments = agreementAttachments;
    }

    public List<File> getDataAttachments() {
        return dataAttachments;
    }

    public void setDataAttachments(List<File> dataAttachments) {
        this.dataAttachments = dataAttachments;
    }

    public File getExcerptListAttachment() {
        return excerptListAttachment;
    }

    public void setExcerptListAttachment(File excerptListAttachment) {
        this.excerptListAttachment = excerptListAttachment;
    }

    @Deprecated
    public boolean isSentToPrivacyCommittee() {
        return sentToPrivacyCommittee;
    }

    @Deprecated
    public void setSentToPrivacyCommittee(boolean sentToPrivacyCommittee) {
        this.sentToPrivacyCommittee = sentToPrivacyCommittee;
    }

    public String getPrivacyCommitteeRationale() {
        return privacyCommitteeRationale;
    }

    public void setPrivacyCommitteeRationale(String privacyCommitteeRationale) {
        this.privacyCommitteeRationale = privacyCommitteeRationale;
    }

    public String getPrivacyCommitteeOutcome() {
        return privacyCommitteeOutcome;
    }

    public void setPrivacyCommitteeOutcome(String privacyCommitteeOutcome) {
        this.privacyCommitteeOutcome = privacyCommitteeOutcome;
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

    public List<File> getMedicalEthicalCommiteeApprovalAttachments() {
        return medicalEthicalCommiteeApprovalAttachments;
    }

    public void setMedicalEthicalCommiteeApprovalAttachments(
            List<File> medicalEthicalCommiteeApprovalAttachments) {
        this.medicalEthicalCommiteeApprovalAttachments = medicalEthicalCommiteeApprovalAttachments;
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

    public String getGrantProvider() {
        return grantProvider;
    }

    public void setGrantProvider(String grantProvider) {
        this.grantProvider = grantProvider;
    }

    public String getBiobankRequestNumber() {
        return biobankRequestNumber;
    }

    public void setBiobankRequestNumber(String biobankRequestNumber) {
        this.biobankRequestNumber = biobankRequestNumber;
    }

    public String getReseachNumber() {
        return reseachNumber;
    }

    public void setReseachNumber(String reseachNumber) {
        this.reseachNumber = reseachNumber;
    }

    public String getLastAssignee() {
        return lastAssignee;
    }

    public void setLastAssignee(String lastAssignee) {
        this.lastAssignee = lastAssignee;
    }

    public String getRequestType() { return requestType; }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public boolean getBlockMaterialsRequest() {
        if (blockMaterialsRequest == null){
            blockMaterialsRequest = false;
        }
        return blockMaterialsRequest;
    }

    public void setBlockMaterialsRequest(Boolean blockMaterialsRequest) {
        if (blockMaterialsRequest == null){
            blockMaterialsRequest = false;
        }
        this.blockMaterialsRequest = blockMaterialsRequest;
    }

    public boolean getHeSliceMaterialsRequest() {
        if (heSliceMaterialsRequest == null){
            heSliceMaterialsRequest = false;
        }
        return heSliceMaterialsRequest;
    }

    public void setHeSliceMaterialsRequest(Boolean heSliceMaterialsRequest) {
        if (heSliceMaterialsRequest == null){
            heSliceMaterialsRequest = false;
        }
        this.heSliceMaterialsRequest = heSliceMaterialsRequest;
    }

    public String getOtherMaterialsRequest() { return otherMaterialsRequest; }

    public void setOtherMaterialsRequest(String otherMaterialsRequest) {
        this.otherMaterialsRequest = otherMaterialsRequest;
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
