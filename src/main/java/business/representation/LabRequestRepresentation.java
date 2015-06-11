package business.representation;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import business.models.Lab;
import business.models.LabRequest;

public class LabRequestRepresentation {

    private Long id;
    
    /** 
     * Task id from Activiti. This is a unique identifier for lab requests.
     */
    private String taskId;

    private String assignee;
    
    private String processInstanceId;

    private RequestListRepresentation requestListRepresentation;

    private String requesterId;
    private String requesterName;
    private String requesterEmail;
    private ProfileRepresentation requester;

    private String status;

    private Date dateCreated;

    private Lab lab;

    private ExcerptListRepresentation excerptList;
    
    private List<String> paNumbers;

    private String excerptListRemark;
    
    private String rejectReason;
    
    private Date rejectDate;

    public LabRequestRepresentation() {

    }

    public LabRequestRepresentation(LabRequest labRequest) {
        this.setId(labRequest.getId());
        this.setProcessInstanceId(labRequest.getProcessInstanceId());
        this.setTaskId(labRequest.getTaskId());
        this.setLab(labRequest.getLab());
        this.setRejectDate(labRequest.getRejectDate());
        this.setRejectReason(labRequest.getRejectReason());
        this.setPaNumbers(labRequest.getPaNumbers());
    }
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }
    
    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
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

    public Lab getLab() {
        return lab;
    }

    public void setLab(Lab lab) {
        this.lab = lab;
    }

    public ExcerptListRepresentation getExcerptList() {
        return excerptList;
    }

    public void setExcerptList(ExcerptListRepresentation excerptList) {
        this.excerptList = excerptList;
    }

    public String getExcerptListRemark() {
        return excerptListRemark;
    }

    public void setExcerptListRemark(String excerptListRemark) {
        this.excerptListRemark = excerptListRemark;
    }

    public RequestListRepresentation getRequestListRepresentation() {
      return requestListRepresentation;
    }

    public void setRequestListRepresentation(RequestListRepresentation requestListRepresentation) {
      this.requestListRepresentation = requestListRepresentation;
    }
    
    public List<String> getPaNumbers() {
        return paNumbers;
    }

    public void setPaNumbers(List<String> paNumbers) {
        this.paNumbers = paNumbers;
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
    
}
