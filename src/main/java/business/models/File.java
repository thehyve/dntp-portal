/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business.models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class File {
    @Id
    @GeneratedValue
    private Long id;

    public enum AttachmentType {
        REQUEST,
        AGREEMENT,
        EXCERPT_LIST,
        EXCERPT_SELECTION,
        MEDICAL_ETHICAL_COMMITEE_APPROVAL,
        DATA,
        INFORMED_CONSENT_FORM
    }

    private String name;
    private String description;
    private String filename;
    private AttachmentType type;
    @ManyToOne
    private User uploader;
    private Date date;
    private String mimeType;

    public File() {

    }

    public File(Long id, String name, String description, String filename,
                AttachmentType type, User uploader, Date date, String mimeType
    ) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.description = description;
        this.filename = filename;
        this.uploader = uploader;
        this.date = date;
        this.mimeType = mimeType;
    }

    public File clone() {
        File result = new File();
        result.name = this.name;
        result.description = this.description;
        result.filename = this.filename;
        result.type = this.type;
        result.uploader = this.uploader;
        result.date = this.date;
        result.mimeType = this.mimeType;
        return result;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public AttachmentType getType() { return type; }

    public void setType(AttachmentType type) { this.type = type; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    public String getFilename() { return filename; }

    public void setFilename(String filename) { this.filename = filename; }

    public User getUploader() { return uploader; }

    public void setUploader(User uploader) { this.uploader = uploader; }

    public Date getDate() { return date; }

    public void setDate(Date date) { this.date = date; }

    public String getMimeType() { return mimeType; }

    public void setMimeType(String mimeType) { this.mimeType = mimeType; }
}

