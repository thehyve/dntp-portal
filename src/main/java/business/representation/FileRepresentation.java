/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business.representation;

import java.util.Date;

import business.models.File;

public class FileRepresentation {

    private Long id;
    private String name;
    private File.AttachmentType type;
    private String description;
    private String mimeType;
    private ProfileRepresentation uploader;
    private Date date;

    public FileRepresentation(){

    }

    public FileRepresentation(File file){
        this.id = file.getId();
        this.name = file.getName();
        this.type = file.getType();
        this.description = file.getDescription();
        this.mimeType = file.getMimeType();
        this.uploader = new ProfileRepresentation(file.getUploader());
        this.date = file.getDate();
    }

    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    public File.AttachmentType getType() { return type; }

    public void setType(File.AttachmentType type) { this.type = type; }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public ProfileRepresentation getUploader() {
        return uploader;
    }

    public void setUploader(ProfileRepresentation uploader) {
        this.uploader = uploader;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
    
}
