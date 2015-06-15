package business.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.Table;


@Entity
@Table(indexes = @Index(columnList="processInstanceId"))
public class RequestProperties {

    @Id
    @GeneratedValue
    private Long id;

    @Column(unique = true)
    private String processInstanceId;

    @ElementCollection
    private Set<String> agreementAttachmentIds = new HashSet<String>();

    @ElementCollection
    private Set<String> dataAttachmentIds = new HashSet<String>();
    
    private String excerptListAttachmentId;
    
    @OrderBy("timeCreated")
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<Comment> comments = new ArrayList<Comment>();

    @OrderBy("timeCreated")
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<Comment> approvalComments = new ArrayList<Comment>();

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Map<Long, ApprovalVote> approvalVotes = new HashMap<Long, ApprovalVote>();

    @Column
    private boolean sentToPrivacyCommittee;

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

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
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

    public Set<String> getAgreementAttachmentIds() {
        return agreementAttachmentIds;
    }

    public void setAgreementAttachmentIds(Set<String> agreementAttachmentIds) {
        this.agreementAttachmentIds = agreementAttachmentIds;
    }

    public Set<String> getDataAttachmentIds() {
        return dataAttachmentIds;
    }

    public void setDataAttachmentIds(Set<String> dataAttachmentIds) {
        this.dataAttachmentIds = dataAttachmentIds;
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

    public String getExcerptListAttachmentId() {
        return excerptListAttachmentId;
    }

    public void setExcerptListAttachmentId(String excerptListAttachmentId) {
        this.excerptListAttachmentId = excerptListAttachmentId;
    }
    
}
