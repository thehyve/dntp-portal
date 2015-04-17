package business.representation;

import java.util.Date;
import java.util.List;

import org.activiti.engine.task.Attachment;

import business.models.Lab;

public class AttachmentRepresentation {

    private String id;
    private String name;
    private String description;
    private String type;
    
    public AttachmentRepresentation() {
        
    }
    
    public AttachmentRepresentation(Attachment attachment) {
        this.id = attachment.getId();
        this.name = attachment.getName();
        this.type = attachment.getTaskId();
        this.description = attachment.getDescription();
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    
    
}
