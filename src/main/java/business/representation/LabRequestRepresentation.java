/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business.representation;

import java.util.Date;
import java.util.List;

import javax.validation.constraints.Size;

import business.models.Lab;
import business.models.LabRequest;
import business.models.LabRequest.Result;
import business.models.LabRequest.Status;

public class LabRequestRepresentation {

    private Long id;

    /**
     * Task id from Activiti. This is a unique identifier for lab requests.
     */
    private String taskId;

    private String assignee;

    private String assigneeName;

    private String processInstanceId;

    private String labRequestCode;

    private RequestListRepresentation request;

    private Long requesterId;
    private String requesterName;
    private String requesterEmail;
    private String requesterTelephone;
    private ProfileRepresentation requester;
    private Lab requesterLab;

    private Status status;

    private Result result;

    private Date dateCreated;
    
    private Date endDate;
    
    private Lab lab;

    private Boolean hubAssistanceRequested;

    private List<PathologyRepresentation> pathologyList;
    private Long pathologyCount;

    private String excerptListRemark;

    @Size(max=2000)
    private String rejectReason;

    private Date rejectDate;
    
    private Date sendDate;

    private Date returnDate;

    private Boolean isPaReportsSent;

    private Boolean isClinicalDataSent;

    private List<CommentRepresentation> comments;
    
    private Boolean samplesMissing;
    
    private CommentRepresentation missingSamples;
    
    public LabRequestRepresentation() {

    }

    public LabRequestRepresentation(LabRequest labRequest) {
        this.setId(labRequest.getId());
        this.setProcessInstanceId(labRequest.getProcessInstanceId());
        this.setTaskId(labRequest.getTaskId());
        this.setStatus(labRequest.getStatus());
        this.setResult(labRequest.getResult());
        this.setLab(labRequest.getLab());
        //this.pathologyCount = (long)labRequest.getPathologyList().size();
        this.setRejectDate(labRequest.getRejectDate());
        this.setSendDate(labRequest.getSendDate());
        this.setRejectReason(labRequest.getRejectReason());
        this.setHubAssistanceRequested(labRequest.isHubAssistanceRequested());
        this.setPaReportsSent(labRequest.isPaReportsSent());
        this.setClinicalDataSent(labRequest.IsClinicalDataSent());
        this.setReturnDate(labRequest.getReturnDate());
    }

    public void setLabRequestCode() {
        this.setLabRequestCode(this.getRequest().getRequestNumber(), this.getLab().getNumber().toString());
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

    public String getAssigneeName() {
        return assigneeName;
    }

    public void setAssigneeName(String assigneeName) {
        this.assigneeName = assigneeName;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
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

    public String getRequesterEmail() {
        return requesterEmail;
    }

    public void setRequesterEmail(String requesterEmail) {
        this.requesterEmail = requesterEmail;
    }

    public String getRequesterTelephone() {
        return requesterTelephone;
    }

    public void setRequesterTelephone(String requesterTelephone) {
        this.requesterTelephone = requesterTelephone;
    }

    public ProfileRepresentation getRequester() {
        return requester;
    }

    public void setRequester(ProfileRepresentation requester) {
        this.requester = requester;
    }

    public Lab getRequesterLab() {
        return requesterLab;
    }

    public void setRequesterLab(Lab requesterLab) {
        this.requesterLab = requesterLab;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
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

    public Boolean isHubAssistanceRequested() {
        return hubAssistanceRequested;
    }

    public void setHubAssistanceRequested(Boolean hubAssistanceRequested) {
        this.hubAssistanceRequested = hubAssistanceRequested;
    }

    public String getExcerptListRemark() {
        return excerptListRemark;
    }

    public void setExcerptListRemark(String excerptListRemark) {
        this.excerptListRemark = excerptListRemark;
    }

    public RequestListRepresentation getRequest() {
      return request;
    }

    public void setRequest(RequestListRepresentation request) {
      this.request = request;
    }

    public List<PathologyRepresentation> getPathologyList() {
        return pathologyList;
    }

    public void setPathologyList(List<PathologyRepresentation> pathologyList) {
        this.pathologyList = pathologyList;
    }

    public Long getPathologyCount() {
        return pathologyCount;
    }

    public void setPathologyCount(Long pathologyCount) {
        this.pathologyCount = pathologyCount;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }

    public Boolean isPaReportsSent() {
      return isPaReportsSent;
    }

    public void setPaReportsSent(Boolean isPaReportsSent) {
        this.isPaReportsSent = isPaReportsSent;
    }

    public Boolean isClinicalDataSent() {
        return isClinicalDataSent;
    }

    public void setClinicalDataSent(Boolean isClinicalDataSent) {
        this.isClinicalDataSent = isClinicalDataSent;
    }

    public Date getRejectDate() {
        return rejectDate;
    }

    public void setRejectDate(Date rejectDate) {
        this.rejectDate = rejectDate;
    }

    public List<CommentRepresentation> getComments() {
        return comments;
    }

    public void setComments(List<CommentRepresentation> comments) {
        this.comments = comments;
    }

    public Boolean isSamplesMissing() {
        return samplesMissing;
    }

    public void setSamplesMissing(Boolean samplesMissing) {
        this.samplesMissing = samplesMissing;
    }

    public CommentRepresentation getMissingSamples() {
        return missingSamples;
    }

    public void setMissingSamples(CommentRepresentation missingSamples) {
        this.missingSamples = missingSamples;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getLabRequestCode() {
        return labRequestCode;
    }

    public void setLabRequestCode(String requestNumber, String labNumber) {
        String labRequestCode = "";
        if (!requestNumber.isEmpty() && !labNumber.isEmpty()) {
            labRequestCode = requestNumber
                    .concat("-")
                    .concat(labNumber);
        }
        this.labRequestCode = labRequestCode;
    }

    public Date getSendDate() {
        return sendDate;
    }

    public void setSendDate(Date sendDate) {
        this.sendDate = sendDate;
    }

    public Date getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(Date returnDate) {
        this.returnDate = returnDate;
    }
}
