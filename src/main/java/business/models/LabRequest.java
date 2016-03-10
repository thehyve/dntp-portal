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
public class LabRequest {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @Fetch(FetchMode.JOIN)
    private Lab lab;

    private String processInstanceId;

    private String taskId;

    private String status;
    
    private Date timeCreated;
    
    @Column
    private Boolean isPaReportsSent;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @BatchSize(size = 1000)
    List<PathologyItem> pathologyList = new ArrayList<PathologyItem>();

    private String rejectReason;

    private Date rejectDate;
    
    private Date sendDate;

    @OrderBy("timeCreated DESC")
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Fetch(FetchMode.JOIN)
    @BatchSize(size = 1000)
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

    public Boolean isPaReportsSent() {
        return isPaReportsSent;
    }

    public void setPaReportsSent(Boolean isPaReportsSent) {
        this.isPaReportsSent = isPaReportsSent;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    
}
