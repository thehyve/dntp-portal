package business.models;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"processInstanceId", "creator_id"}))
public class ApprovalVote implements Serializable {

    private static final long serialVersionUID = 8158201293631148757L;

    public enum Value {
        ACCEPTED,
        REJECTED,
        NONE
    }
    
    @Id
    @GeneratedValue
    private Long id;

    private String processInstanceId;

    @ManyToOne(optional = true, fetch = FetchType.EAGER)
    private User creator;
    
    @OrderColumn
    private Date timeCreated = new Date();
    
    private Date timeEdited = timeCreated;
    
    private Value value;

    public ApprovalVote() {
        
    }
    
    public ApprovalVote(String processInstanceId, User creator, Value value) {
        this.processInstanceId = processInstanceId;
        this.creator = creator;
        this.value = value;
    }
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public Date getTimeCreated() {
        return timeCreated;
    }

    public void setTimeCreated(Date timeCreated) {
        this.timeCreated = timeCreated;
    }
    
    public Date getTimeEdited() {
        return timeEdited;
    }

    public void setTimeEdited(Date timeEdited) {
        this.timeEdited = timeEdited;
    }

    public Value getValue() {
        return value;
    }

    public void setValue(Value value) {
        this.value = value;
    }
    
}
