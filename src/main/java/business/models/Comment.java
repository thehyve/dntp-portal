package business.models;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OrderColumn;

import org.springframework.data.rest.core.annotation.RestResource;

@Entity
public class Comment implements Serializable {

    private static final long serialVersionUID = -2971541749268025366L;

    @Id
    @GeneratedValue
    private Long id;
    
    private String processInstanceId;

    @ManyToOne(optional = true)
    //@RestResource(exported = false)
    private User creator;
    
    @OrderColumn
    private Date timeCreated = new Date();
    
    private Date timeEdited = timeCreated;
    
    private String contents;

    public Comment() {
        
    }
    
    public Comment(String processInstanceId, User creator, String contents) {
        this.processInstanceId = processInstanceId;
        this.creator = creator;
        this.contents = contents;
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

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }
    
}
