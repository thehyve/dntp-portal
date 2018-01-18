/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business.services;

import business.models.*;
import business.models.LabRequest.Result;
import business.models.LabRequest.Status;
import business.representation.*;
import business.security.UserAuthenticationToken;
import org.activiti.engine.TaskService;
import org.activiti.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class LabRequestStatusService {

    Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private TaskService taskService;

    @Autowired
    private LabRequestService labRequestService;

    @Autowired
    private LabRequestQueryService labRequestQueryService;


    @CacheEvict(value = {"labrequestdata", "detailedlabrequestdata"}, key = "#labRequest.id")
    public LabRequest updateStatus(LabRequest labRequest, Status status) {
        taskService.setVariableLocal(labRequest.getTaskId(), "labrequest_status", status);
        labRequest.setStatus(status);
        return labRequestService.save(labRequest);
    }

    @CacheEvict(value = {"labrequestdata", "detailedlabrequestdata"}, key = "#labRequest.id")
    public LabRequest updateStatus(LabRequest labRequest, Status status, Result result) {
        taskService.setVariableLocal(labRequest.getTaskId(), "labrequest_status", status);
        labRequest.setStatus(status);
        labRequest.setResult(result);
        return labRequestService.save(labRequest);
    }

    @CacheEvict(value = {"labrequestdata", "detailedlabrequestdata"}, key = "#id")
    public LabRequestRepresentation claim(Long id, UserAuthenticationToken user) {
        LabRequest labRequest = labRequestQueryService.findOne(id);
        Task task = labRequestQueryService.getTask(labRequest.getTaskId(), "lab_request");

        if (task.getAssignee() == null || task.getAssignee().isEmpty()) {
            taskService.claim(task.getId(), user.getId().toString());
        } else {
            taskService.delegateTask(task.getId(), user.getId().toString());
        }

        LabRequestRepresentation representation = new LabRequestRepresentation(
                labRequest);
        labRequestQueryService.transferLabRequestData(representation, false);
        return representation;
    }

    @CacheEvict(value = {"labrequestdata", "detailedlabrequestdata"}, key = "#id")
    public LabRequestRepresentation unclaim(Long id, UserAuthenticationToken user) {
        LabRequest labRequest = labRequestQueryService.findOne(id);
        Task task = labRequestQueryService.getTask(labRequest.getTaskId(),"lab_request");

        taskService.unclaim(task.getId());

        LabRequestRepresentation representation = new LabRequestRepresentation(labRequest);
        labRequestQueryService.transferLabRequestData(representation, false);
        return representation;
    }

}
