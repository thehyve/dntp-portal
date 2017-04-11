/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business.representation;

import java.util.Date;

import business.models.Comment;

public class CommentRepresentation {

    
    private Long id;
    
    private String processInstanceId;

    private ProfileRepresentation creator;
    
    private Date timeCreated = new Date();
    
    private Date timeEdited = timeCreated;
    
    private String contents;

    private Boolean internalNote = Boolean.FALSE;

    public CommentRepresentation() {
        
    }
    
    public CommentRepresentation(Comment comment) {
        this.id = comment.getId();
        this.creator = new ProfileRepresentation(comment.getCreator());
        this.processInstanceId = comment.getProcessInstanceId();
        this.timeCreated = comment.getTimeCreated();
        this.timeEdited = comment.getTimeEdited();
        this.contents = comment.getContents();
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

    public ProfileRepresentation getCreator() {
        return creator;
    }

    public void setCreator(ProfileRepresentation creator) {
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
