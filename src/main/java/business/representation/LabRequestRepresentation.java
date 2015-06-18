package business.representation;

import java.util.Date;
import java.util.List;

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

    private RequestListRepresentation request;

    private String requesterId;
    private String requesterName;
    private String requesterEmail;
    private ProfileRepresentation requester;
    private Lab requesterLab;

    private String status;

    private Date dateCreated;
    
    private Date endDate;
    
    private Lab lab;

    private ExcerptListRepresentation excerptList;

    private List<PathologyRepresentation> pathologyList;
    private Long pathologyCount;

    private String excerptListRemark;

    private String rejectReason;

    private Date rejectDate;

    private Boolean isPaReportsSent;

    private List<CommentRepresentation> comments;
    
    private Boolean samplesMissing;
    
    private CommentRepresentation missingSamples;
    
    public LabRequestRepresentation() {

    }

    public LabRequestRepresentation(LabRequest labRequest) {
        this.setId(labRequest.getId());
        this.setProcessInstanceId(labRequest.getProcessInstanceId());
        this.setTaskId(labRequest.getTaskId());
        this.setLab(labRequest.getLab());
        //this.pathologyCount = labRequest.getPathologyList().size();
        this.setRejectDate(labRequest.getRejectDate());
        this.setRejectReason(labRequest.getRejectReason());
        this.setPaReportsSent(labRequest.isPaReportsSent());
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

    public Lab getRequesterLab() {
        return requesterLab;
    }

    public void setRequesterLab(Lab requesterLab) {
        this.requesterLab = requesterLab;
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
    
}
