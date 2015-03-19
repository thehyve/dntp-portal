package business.representation;

import java.util.Date;
import java.util.List;

import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.task.Attachment;
import org.activiti.engine.task.Task;

import com.fasterxml.jackson.annotation.JsonFormat;

public class TaskRepresentation {

    private String id;
    private String name;
    private String deleteReason = "";
    private String description;
    private String assignee;
    @JsonFormat
    private Date createTime;
    @JsonFormat
    private Date endTime;
    @JsonFormat
    private Date dueDate;
    private String owner;
    private int priority;
    private String processInstanceId;
    private String parentTaskId;
    private TaskFormDataRepresentation formData;
    private List<Attachment> attachments;

    public TaskRepresentation(Task task) {
        this.id = task.getId();
        this.name = task.getName();
        this.description = task.getDescription();
        this.assignee = task.getAssignee();
        this.createTime = task.getCreateTime();
        this.dueDate = task.getDueDate();
        this.owner = task.getOwner();
        this.priority = task.getPriority();
        this.processInstanceId = task.getProcessInstanceId();
        this.parentTaskId = task.getParentTaskId();
    }

    public TaskRepresentation(HistoricTaskInstance task) {
        this.id = task.getId();
        this.name = task.getName();
        this.description = task.getDescription();
        this.deleteReason = task.getDeleteReason();
        this.assignee = task.getAssignee();
        this.createTime = task.getCreateTime();
        this.dueDate = task.getDueDate();
        this.owner = task.getOwner();
        this.priority = task.getPriority();
        this.processInstanceId = task.getProcessInstanceId();
        this.parentTaskId = task.getParentTaskId();
        this.endTime = task.getEndTime();
    }

    public TaskRepresentation() {
        
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getDeleteReason() {
        return deleteReason;
    }

    public String getAssignee() {
        return assignee;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public String getOwner() {
        return owner;
    }

    public int getPriority() {
        return priority;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public String getParentTaskId() {
        return parentTaskId;
    }

    public TaskFormDataRepresentation getFormData() {
        return formData;
    }

    public void setFormData(TaskFormDataRepresentation formData) {
        this.formData = formData;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
    }

}