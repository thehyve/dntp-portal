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
import javax.persistence.OneToMany;

@Entity
public class RequestProperties {

    @Id
    @GeneratedValue
    private Long id;
    
    @Column(unique = true)
    private String processInstanceId;
    
    @ElementCollection
    private Set<String> agreementAttachmentIds = new HashSet<String>();
    
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<Comment> comments = new ArrayList<Comment>();
    
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<Comment> approvalComments = new ArrayList<Comment>();

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Map<User, ApprovalVote> approvalVotes = new HashMap<User, ApprovalVote>();
    
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

    public Map<User, ApprovalVote> getApprovalVotes() {
        return approvalVotes;
    }

    public void addApprovalVote(User user, ApprovalVote approvalVote) {
        this.approvalVotes.put(user, approvalVote);
    }
    
    public void setApprovalVotes(Map<User, ApprovalVote> approvalVotes) {
        this.approvalVotes = approvalVotes;
    }

    public Set<String> getAgreementAttachmentIds() {
        return agreementAttachmentIds;
    }

    public void setAgreementAttachmentIds(Set<String> agreementAttachmentIds) {
        this.agreementAttachmentIds = agreementAttachmentIds;
    }
    
}
