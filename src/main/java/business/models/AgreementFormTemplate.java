/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business.models;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class AgreementFormTemplate {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(optional = true)
    private User lastEditedBy;

    private Date timeEdited = new Date();

    @Column(columnDefinition="TEXT")
    private String contents;

    public AgreementFormTemplate() { }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getLastEditedBy() {
        return lastEditedBy;
    }

    public void setLastEditedBy(User lastEditedBy) {
        this.lastEditedBy = lastEditedBy;
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
