package business.representation;

import java.util.Date;

public class RequestListRepresentation {

    private String processInstanceId;
    private String requesterId;
    private String requesterName;
    private String status;
    private Date dateCreated;

    private String assignee;
    private String assigneeName;
    private Date dateAssigned;

    private String title;

    private String approvalVote;
    private Integer numberOfApprovalVotes;
    
    public RequestListRepresentation() {

    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
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

    public String getApprovalVote() {
        return approvalVote;
    }

    public void setApprovalVote(String approvalVote) {
        this.approvalVote = approvalVote;
    }

    public Integer getNumberOfApprovalVotes() {
        return numberOfApprovalVotes;
    }

    public void setNumberOfApprovalVotes(Integer numberOfApprovalVotes) {
        this.numberOfApprovalVotes = numberOfApprovalVotes;
    }

}
