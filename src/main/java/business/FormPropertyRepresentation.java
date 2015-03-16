package business;

import org.activiti.engine.form.FormProperty;
import org.activiti.engine.history.HistoricFormProperty;


public class FormPropertyRepresentation {

    String id;
    
    String name;
    
    String type;

    String value;

    public FormPropertyRepresentation(String id, String name, String type,
            String value) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.value = value;
    }

    public FormPropertyRepresentation() {
    }

    public FormPropertyRepresentation(FormProperty property) {
        this.id = property.getId();
        this.name = property.getName();
        if (property.getType() != null) {
            this.type = property.getType().getName();
        }
        this.value = property.getValue();
    }

    public FormPropertyRepresentation(HistoricFormProperty property) {
        this.id = property.getId();
        this.value = property.getPropertyValue();
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
    
}
