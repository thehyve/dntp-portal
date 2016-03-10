/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business.representation;

import org.activiti.engine.history.HistoricIdentityLink;
import org.activiti.engine.task.IdentityLink;

class IdentityLinkRepresentation {
    
    private String groupId;
    private String processInstanceId;
    private String taskId;
    private String type;
    private String userId;

    public IdentityLinkRepresentation(IdentityLink link) {
        this.groupId = link.getGroupId();
        this.processInstanceId = link.getProcessInstanceId();
        this.taskId = link.getTaskId();
        this.type = link.getType();
        this.userId = link.getUserId();
    }

    public IdentityLinkRepresentation(HistoricIdentityLink link) {
        this.groupId = link.getGroupId();
        this.processInstanceId = link.getProcessInstanceId();
        this.taskId = link.getTaskId();
        this.type = link.getType();
        this.userId = link.getUserId();
    }

    public IdentityLinkRepresentation() {
        
    }
    
    public String getGroupId() {
        return groupId;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public String getTaskId() {
        return taskId;
    }

    public String getType() {
        return type;
    }

    public String getUserId() {
        return userId;
    }
    
}