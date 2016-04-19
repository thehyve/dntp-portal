/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business.representation;

import java.util.Date;

import business.models.RequestProperties.ReviewStatus;

public class RequestListRepresentation {

    private String processInstanceId;
    private String processId;
    private String requestNumber;
    private Long requesterId;
    private String requesterName;
    private String status;
    private Date dateCreated;

    private ReviewStatus reviewStatus;

    private String assignee;
    private String assigneeName;
    private Date dateAssigned;

    private String title;
    private String background;
    private String researchQuestion;
    private String hypothesis;
    private String methods;
    private String pathologistName;
    private String pathologistEmail;

    private boolean statisticsRequest;
    private boolean excerptsRequest;
    private boolean paReportRequest;
    private boolean materialsRequest;
    
    private String approvalVote;
    private Long numberOfApprovalVotes;
    
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
    
}
