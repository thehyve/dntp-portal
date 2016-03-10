/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business.representation;

import java.util.Date;

import business.models.ApprovalVote;

public class ApprovalVoteRepresentation {


    private Long id;

    private String processInstanceId;

    private ProfileRepresentation creator;

    private Date timeCreated = new Date();

    private Date timeEdited = timeCreated;

    private String value;

    public ApprovalVoteRepresentation() {

    }

    public ApprovalVoteRepresentation(ApprovalVote vote) {
        this.id = vote.getId();
        this.creator = new ProfileRepresentation(vote.getCreator());
        this.processInstanceId = vote.getProcessInstanceId();
        this.timeCreated = vote.getTimeCreated();
        this.timeEdited = vote.getTimeEdited();
        this.value = vote.getValue().toString();
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

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
