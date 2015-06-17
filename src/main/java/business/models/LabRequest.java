package business.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.*;

@Entity
public class LabRequest {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    private Lab lab;

    private String processInstanceId;

    private String taskId;

    @Column
    private boolean isPaReportsSent;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    List<PathologyItem> pathologyList = new ArrayList<PathologyItem>();

    private String rejectReason;

    private Date rejectDate;

    @OrderBy("timeCreated")
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
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

    public boolean isPaReportsSent() {
    return isPaReportsSent;
  }

    public void setPaReportsSent(boolean isPaReportsSent) {
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

}
