/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business.models;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class Comment implements Serializable {

    private static final long serialVersionUID = -2971541749268025366L;

    @Id
    @GeneratedValue
    private Long id;
    
    private String processInstanceId;

    @ManyToOne(optional = true)
    private User creator;
    
    private Date timeCreated = new Date();
    
    private Date timeEdited = timeCreated;

    private Boolean internalNote;

    @Column(columnDefinition="TEXT")
    private String contents;

    public Comment() {
        
    }
    
    public Comment(String processInstanceId, User creator, String contents, Boolean internalNote) {
        this.processInstanceId = processInstanceId;
        this.creator = creator;
        this.contents = contents;
        this.internalNote = internalNote;
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

    public Boolean getInternalNote() {
        return internalNote;
    }

    public void setInternalNote(Boolean internalNote) {
        this.internalNote = internalNote;
    }
}
