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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

@Entity
public class LabRequest {


    public enum Status {
        WAITING_FOR_LAB_APPROVAL ("Waiting for lab approval"),
        APPROVED ("Approved"),
        REJECTED ("Rejected"),
        COMPLETED ("Completed"),
        SENDING ("Sending"),
        RECEIVED ("Received"),
        RETURNING ("Returning"),
        @Deprecated RETURNED ("Returned");

        private final String description;

        Status(final String description) {
            this.description = description;
        }

        @JsonValue
        public final String toString() {
            return description;
        }

        private static final Map<String,Status> mapping = new HashMap<>();
        static {
            for (Status status: Status.values()) {
                mapping.put(status.toString(), status);
            }
        }

        @JsonCreator
        public static Status forDescription(String description) {
            return mapping.get(description);
        }

    }

    public enum Result {
        REJECTED ("Rejected"),
        REPORTS_ONLY ("PA reports only"),
        RETURNED ("Materials returned"),
        NONE ("None");

        private final String description;

        Result(final String description) {
            this.description = description;
        }

        public final String toString() {
            return description;
        }
    }

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @Fetch(FetchMode.JOIN)
    private Lab lab;

    private String processInstanceId;

    private String taskId;

    private Status status = Status.WAITING_FOR_LAB_APPROVAL;

    @Enumerated(EnumType.STRING)
    private Result result = Result.NONE;

    private Date timeCreated;

    private Boolean hubAssistanceRequested;

    @Column
    private Boolean isPaReportsSent;

    @Column
    private Boolean isClinicalDataSent;

    @OrderBy("sequenceNumber ASC")
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @BatchSize(size = 10000)
    //@OrderColumn // FIXME
    List<PathologyItem> pathologyList = new ArrayList<PathologyItem>();

    private String rejectReason;

    private Date rejectDate;
    
    private Date sendDate;

    @Column
    private Date returnDate;
    @Column
    private Boolean sentReturnEmail;

    @OrderBy("timeCreated DESC")
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Fetch(FetchMode.JOIN)
    @BatchSize(size = 1000)
    @OrderColumn
    private List<Comment> comments = new ArrayList<Comment>();

    public LabRequest() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Lab getLab() {
        return lab;
    }

    public void setLab(Lab lab) {
        this.lab = lab;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public Boolean isHubAssistanceRequested() {
        return hubAssistanceRequested;
    }

    public void setHubAssistanceRequested(Boolean hubAssistanceRequested) {
        this.hubAssistanceRequested = hubAssistanceRequested;
    }

    public Boolean isPaReportsSent() {
        return isPaReportsSent;
    }

    public void setPaReportsSent(Boolean isPaReportsSent) {
        this.isPaReportsSent = isPaReportsSent;
    }

    public Boolean IsClinicalDataSent() {
        return isClinicalDataSent;
    }

    public void setClinicalDataSent(Boolean isClinicalDataSent) {
        this.isClinicalDataSent = isClinicalDataSent;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public List<PathologyItem> getPathologyList() {
        return pathologyList;
    }

    public void setPathologyList(List<PathologyItem> pathologyList) {
        this.pathologyList = pathologyList;
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

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public void addComment(Comment comment) {
        this.comments.add(comment);
    }

    public Date getTimeCreated() {
        return timeCreated;
    }

    public void setTimeCreated(Date timeCreated) {
        this.timeCreated = timeCreated;
    }

    public Date getSendDate() {
        return sendDate;
    }

    public void setSendDate(Date sendDate) {
        this.sendDate = sendDate;
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

    public Date getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(Date returnDate) {
        this.returnDate = returnDate;
    }

    public Boolean getSentReturnEmail() {
        return sentReturnEmail;
    }

    public void setSentReturnEmail(Boolean sentReturnEmail) {
        this.sentReturnEmail = sentReturnEmail;
    }
}
