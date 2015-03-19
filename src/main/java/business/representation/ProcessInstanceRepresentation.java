package business.representation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.activiti.engine.history.HistoricIdentityLink;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Event;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.Task;

public class ProcessInstanceRepresentation {
    
    private String id;
    private String activityId;
    private String deploymentId;
    private String name;
    private String parentId;
    private String businessKey;
    private String processDefinitionId;
    private String processInstanceId;
    private String tenantId;
    private Map<String, Object> processVariables;
    private List<Event> events;
    private List<IdentityLinkRepresentation> identityLinks;
    private List<TaskRepresentation> tasks;
    private boolean suspended;
    private boolean ended;
    
    private byte[] diagramData;
    private boolean diagramAvailable;
    private String diagramError;

    public ProcessInstanceRepresentation(ProcessInstance instance) {
        this.id = instance.getId();
        this.activityId = instance.getActivityId();
        this.deploymentId = instance.getDeploymentId();
        this.name = instance.getName();
        this.parentId = instance.getParentId();
        this.businessKey = instance.getBusinessKey();
        this.processDefinitionId = instance.getProcessDefinitionId();
        this.processInstanceId = instance.getProcessInstanceId();
        this.tenantId = instance.getTenantId();
        this.processVariables = instance.getProcessVariables();
        this.suspended = instance.isSuspended();
        this.ended = instance.isEnded();
    }

    public ProcessInstanceRepresentation(HistoricProcessInstance instance) {
        this.id = instance.getId();
        this.name = instance.getName();
        this.businessKey = instance.getBusinessKey();
        this.processDefinitionId = instance.getProcessDefinitionId();
        this.tenantId = instance.getTenantId();
        this.processVariables = instance.getProcessVariables();
        this.ended = true;
    }
    
    public ProcessInstanceRepresentation() {
        
    }

    public String getId() {
        return id;
    }
    
    public String getActivityId() {
        return activityId;
    }

    public String getDeploymentId() {
        return deploymentId;
    }

    public String getName() {
        return name;
    }

    public String getParentId() {
        return parentId;
    }

    public String getBusinessKey() {
        return businessKey;
    }

    public String getProcessDefinitionId() {
        return processDefinitionId;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public Map<String, Object> getProcessVariables() {
        return processVariables;
    }

    public void setProcessVariables(Map<String, Object> processVariables) {
        this.processVariables = processVariables;
    }

    public List<Event> getEvents() {
        return events;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }

    public List<IdentityLinkRepresentation> getIdentityLinks() {
        return identityLinks;
    }
    
    public void setIdentityLinks(List<IdentityLinkRepresentation> identityLinks) {
        this.identityLinks = identityLinks;
    }

    public void setIdentityLinksFromActiviti(List<IdentityLink> identityLinks) {
        this.identityLinks = new ArrayList<IdentityLinkRepresentation>();
        for (IdentityLink link: identityLinks) {
            this.identityLinks.add(new IdentityLinkRepresentation(link));
        }
    }

    public void setIdentityLinksFromHistory(
            List<HistoricIdentityLink> identityLinks) {
        this.identityLinks = new ArrayList<IdentityLinkRepresentation>();
        for (HistoricIdentityLink link: identityLinks) {
            this.identityLinks.add(new IdentityLinkRepresentation(link));
        }
    }
    
    public List<TaskRepresentation> getTasks() {
        return tasks;
    }
    
    public void setTasks(List<TaskRepresentation> tasks) {
        this.tasks = tasks;
    }

    public void setTasksFromActiviti(List<Task> tasks) {
        this.tasks = new ArrayList<TaskRepresentation>();
        for (Task task: tasks) {
            this.tasks.add(new TaskRepresentation(task));
        }
    }        
    
    public boolean isSuspended() {
        return suspended;
    }

    public boolean isEnded() {
        return ended;
    }

    public byte[] getDiagramData() {
        return diagramData;
    }

    public void setDiagramData(byte[] diagramData) {
        this.diagramData = diagramData;
    }

    public boolean isDiagramAvailable() {
        return diagramAvailable;
    }

    public void setDiagramAvailable(boolean diagramAvailable) {
        this.diagramAvailable = diagramAvailable;
    }

    public String getDiagramError() {
        return diagramError;
    }

    public void setDiagramError(String diagramError) {
        this.diagramError = diagramError;
    }
    
}