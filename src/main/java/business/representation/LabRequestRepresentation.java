package business.representation;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import business.models.Lab;

public class LabRequestRepresentation {

    /** 
     * Task id from Activiti. This is a unique identifier for lab requests.
     */
    private String taskId;
    
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

    private String excerptListRemark;

    public LabRequestRepresentation() {

    }
    
    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
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
}
