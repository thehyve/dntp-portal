package business.representation;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.form.FormProperty;
import org.activiti.engine.form.TaskFormData;
import org.activiti.engine.history.HistoricData;
import org.activiti.engine.history.HistoricFormProperty;
import org.activiti.engine.history.HistoricVariableInstance;

public class TaskFormDataRepresentation {
    
    private String deploymentId;
    private String formKey;
    private List<FormPropertyRepresentation> formProperties;
    private String taskId;
    
    public TaskFormDataRepresentation(TaskFormData data) {
        this.deploymentId = data.getDeploymentId();
        this.formKey = data.getFormKey();
        this.setFormPropertiesFromActiviti(data.getFormProperties());
        this.taskId = data.getTask().getId();
    }

    public TaskFormDataRepresentation(String formKey,
            List<HistoricData> variables, String taskId) {
        this.formKey = formKey;
        this.setFormPropertiesFromHistory(variables);
        this.taskId = taskId;
    }
    
    public TaskFormDataRepresentation() {
        
    }

    public List<FormPropertyRepresentation> getFormProperties() {
        return formProperties;
    }

    public void setFormProperties(List<FormPropertyRepresentation> formProperties) {
        this.formProperties = formProperties;
    }

    public void setFormPropertiesFromActiviti(List<FormProperty> formProperties) {
        this.formProperties = new ArrayList<FormPropertyRepresentation>();
        for (FormProperty property: formProperties) {
            this.formProperties.add(new FormPropertyRepresentation(property));
        }
    }
    
    public void setFormPropertiesFromHistory(List<HistoricData> history) {
        this.formProperties = new ArrayList<FormPropertyRepresentation>();
        for (HistoricData data: history) {
            if (data instanceof HistoricVariableInstance) {
                HistoricVariableInstance variable = (HistoricVariableInstance)data;
                FormPropertyRepresentation property = new FormPropertyRepresentation();
                property.setId(variable.getId());
                property.setName(variable.getVariableName());
                property.setType(variable.getVariableTypeName());
                if (variable.getValue() != null) {
                    property.setValue(variable.getValue().toString());
                }
                this.formProperties.add(property);
            } else if (data instanceof HistoricFormProperty) {
                HistoricFormProperty property = (HistoricFormProperty)data;
                FormPropertyRepresentation representation = new
                        FormPropertyRepresentation(property);
                this.formProperties.add(representation);
            }
        }
    }
    
    public String getDeploymentId() {
        return deploymentId;
    }

    public String getFormKey() {
        return formKey;
    }

    public String getTaskId() {
        return taskId;
    }
    
}