/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business.services;

import business.exceptions.EmptyInput;
import business.exceptions.InvalidActionInStatus;
import business.models.*;
import business.models.LabRequest.Result;
import business.models.LabRequest.Status;
import business.representation.CommentRepresentation;
import business.representation.LabRequestRepresentation;
import business.representation.RequestRepresentation;
import business.representation.ReturnDateRepresentation;
import business.security.UserAuthenticationToken;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.task.DelegationState;
import org.activiti.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class LabRequestDeliveryService {

    Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private LabRequestService labRequestService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private RequestFormService requestFormService;

    @Autowired
    private RequestService requestService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private LabRequestQueryService labRequestQueryService;

    @Autowired
    private LabRequestStatusService labRequestStatusService;


    public LabRequestRepresentation sending(Long id,
                                            ReturnDateRepresentation body,
                                            UserAuthenticationToken user) {
        LabRequest labRequest = labRequestQueryService.findOne(id);

        if (labRequest.getStatus() != Status.APPROVED) {
            log.error("Action not allowed in status '" + labRequest.getStatus() + "'");
            throw new InvalidActionInStatus("Action not allowed in status '" + labRequest.getStatus() + "'");
        }

        RequestRepresentation request = new RequestRepresentation();
        HistoricProcessInstance instance = requestService.getProcessInstance(labRequest.getProcessInstanceId());
        requestFormService.transferData(instance, request, user.getUser());
        if (!request.isMaterialsRequest()) {
            log.error("Action not allowed in status '" + labRequest.getStatus() + "'. Not a materials request.");
            throw new InvalidActionInStatus("Action not allowed in status '" + labRequest.getStatus() + "'");
        }

        labRequestStatusService.updateStatus(labRequest, Status.SENDING);

        labRequest.setSendDate(new Date());
        labRequest.setReturnDate(body.getReturnDate());
        labRequest = labRequestService.save(labRequest);

        LabRequestRepresentation representation = new LabRequestRepresentation(labRequest);
        labRequestQueryService.transferLabRequestData(representation, false);
        return representation;
    }

    public LabRequestRepresentation received(Long id,
                                            LabRequestRepresentation body,
                                            UserAuthenticationToken user) {
        LabRequest labRequest = labRequestQueryService.findOne(id);
        if (labRequest.getStatus() != Status.SENDING) {
            log.error("Action not allowed in status '" + labRequest.getStatus() + "'");
            throw new InvalidActionInStatus("Action not allowed in status '" + labRequest.getStatus() + "'");
        }

        RequestRepresentation request = new RequestRepresentation();
        HistoricProcessInstance instance = requestService.getProcessInstance(labRequest.getProcessInstanceId());
        requestFormService.transferData(instance, request, user.getUser());
        if (!request.isMaterialsRequest()) {
            log.error("Action not allowed in status '" + labRequest.getStatus() + "'. Not a materials request.");
            throw new InvalidActionInStatus("Action not allowed in status '" + labRequest.getStatus() + "'");
        }

        if (body.isSamplesMissing() != null && body.isSamplesMissing()) {
            if (body.getMissingSamples() == null || body.getMissingSamples().getContents().trim().isEmpty()) {
                throw new EmptyInput("Empty field 'missing samples'");
            }
            CommentRepresentation comment = new CommentRepresentation();
            comment.setContents(body.getMissingSamples().getContents());
            commentService.addLabRequestComment(user.getUser(), id, comment);
            labRequest = labRequestQueryService.findOne(id);
        }

        labRequest = labRequestStatusService.updateStatus(labRequest, Status.RECEIVED);

        LabRequestRepresentation representation = new LabRequestRepresentation(labRequest);
        labRequestQueryService.transferLabRequestData(representation, false);
        return representation;
    }

    public LabRequestRepresentation returning(Long id) {
        LabRequest labRequest = labRequestQueryService.findOne(id);
        if (labRequest.getStatus() != Status.RECEIVED) {
            log.error("Action not allowed in status '" + labRequest.getStatus() + "'");
            throw new InvalidActionInStatus("Action not allowed in status '" + labRequest.getStatus() + "'");
        }

        labRequest = labRequestStatusService.updateStatus(labRequest, Status.RETURNING);

        LabRequestRepresentation representation = new LabRequestRepresentation(labRequest);
        labRequestQueryService.transferLabRequestData(representation, false);
        return representation;
    }

    private static Set<Status> labRequestReturnedEnabledStatuses = new HashSet<>(Arrays.asList(
                Status.SENDING,
                Status.RECEIVED,
                Status.RETURNING
    ));

    public LabRequestRepresentation completeReturned(Long id,
                                                     LabRequestRepresentation body,
                                                     UserAuthenticationToken user){
        LabRequest labRequest = labRequestQueryService.findOne(id);
        if (!labRequestReturnedEnabledStatuses.contains(labRequest.getStatus())) {
            log.error("Action not allowed in status '" + labRequest.getStatus() + "'");
            throw new InvalidActionInStatus("Action not allowed in status '" + labRequest.getStatus() + "'");
        }

        if (body.isSamplesMissing() != null && body.isSamplesMissing()) {
            if (body.getMissingSamples() == null || body.getMissingSamples().getContents().trim().isEmpty()) {
                log.error("Empty field 'missing samples'");
                throw new EmptyInput("Empty field 'missing samples'");
            }
            CommentRepresentation comment = new CommentRepresentation();
            comment.setContents(body.getMissingSamples().getContents());
            commentService.addLabRequestComment(user.getUser(), labRequest.getId(), comment);
            labRequest = labRequestQueryService.findOne(labRequest.getId());
        }

        labRequest = labRequestStatusService.updateStatus(labRequest, Status.COMPLETED, Result.RETURNED);

        Task task = labRequestQueryService.getTask(labRequest.getTaskId(),"lab_request");
        // complete task
        if (task.getDelegationState() == DelegationState.PENDING) {
            taskService.resolveTask(task.getId());
        }
        taskService.complete(task.getId());

        LabRequestRepresentation representation = new LabRequestRepresentation(labRequest);
        labRequestQueryService.transferLabRequestData(representation, false);

        return representation;
    }


    public LabRequestRepresentation completeRejected(Long id) {
        LabRequest labRequest = labRequestQueryService.findOne(id);
        if (labRequest.getStatus() != Status.REJECTED) {
            log.error("Action not allowed in status '" + labRequest.getStatus() + "'");
            throw new InvalidActionInStatus("Action not allowed in status '" + labRequest.getStatus() + "'");
        }

        labRequest = labRequestStatusService.updateStatus(labRequest, Status.COMPLETED, Result.REJECTED);

        Task task = labRequestQueryService.getTask(labRequest.getTaskId(),
                "lab_request");
        // complete task
        if (task.getDelegationState() == DelegationState.PENDING) {
            taskService.resolveTask(task.getId());
        }
        taskService.complete(task.getId());

        LabRequestRepresentation representation = new LabRequestRepresentation(labRequest);
        labRequestQueryService.transferLabRequestData(representation, false);
        return representation;
    }

    public LabRequestRepresentation completeReportsOnly(Long id,
                                                     LabRequestRepresentation body,
                                                     UserAuthenticationToken user) {
        LabRequest labRequest = labRequestQueryService.findOne(id);
        if (labRequest.getStatus() != Status.APPROVED) {
            log.error("Action not allowed in status '" + labRequest.getStatus() + "'");
            throw new InvalidActionInStatus("Action not allowed in status '" + labRequest.getStatus() + "'");
        }

        RequestRepresentation request = new RequestRepresentation();
        HistoricProcessInstance instance = requestService.getProcessInstance(labRequest.getProcessInstanceId());
        requestFormService.transferData(instance, request, user.getUser());
        if (request.isMaterialsRequest()) {
            log.error("Action not allowed in status '" + labRequest.getStatus() + "'. Not a materials request.");
            throw new InvalidActionInStatus("Action not allowed in status '" + labRequest.getStatus() + "'");
        }

        labRequest = labRequestService.transferLabRequestFormData(body, labRequest, user.getUser());

        labRequest = labRequestStatusService.updateStatus(labRequest, Status.COMPLETED, Result.REPORTS_ONLY);

        Task task = labRequestQueryService.getTask(labRequest.getTaskId(),
                "lab_request");
        // complete task
        if (task.getDelegationState() == DelegationState.PENDING) {
            taskService.resolveTask(task.getId());
        }
        taskService.complete(task.getId());

        LabRequestRepresentation representation = new LabRequestRepresentation(labRequest);
        labRequestQueryService.transferLabRequestData(representation, false);
        return representation;
    }

}
