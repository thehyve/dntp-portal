package business.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import business.exceptions.TaskNotFound;

import org.activiti.engine.HistoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.task.Task;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import business.exceptions.RequestNotFound;
import business.exceptions.TaskNotFound;
import business.models.Comment;
import business.models.ExcerptEntry;
import business.models.ExcerptList;
import business.models.Lab;
import business.models.LabRepository;
import business.models.LabRequest;
import business.models.LabRequestRepository;
import business.models.PathologyItem;
import business.models.PathologyItemRepository;
import business.models.User;
import business.models.UserRepository;
import business.representation.CommentRepresentation;
import business.representation.ExcerptEntryRepresentation;
import business.representation.ExcerptListRepresentation;
import business.representation.LabRequestRepresentation;
import business.representation.PathologyRepresentation;
import business.representation.ProfileRepresentation;
import business.representation.RequestListRepresentation;

@Service
public class LabRequestService {

    Log log = LogFactory.getLog(getClass());

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RequestPropertiesService requestPropertiesService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private RequestFormService requestFormService;

    @Autowired
    private RequestService requestService;

    @Autowired
    private MailService mailService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private LabRepository labRepository;

    @Autowired
    private LabRequestRepository labRequestRepository;
    
    @Autowired
    private PathologyItemRepository pathologyItemRepository;

    @Autowired
    private ExcerptListService excerptListService;

    /**
     * Finds task.
     * @param taskId
     * @return the task if it exists.
     * @throws business.exceptions.TaskNotFound
     */
    public Task getTask(String taskId, String taskDefinition) {
        Task task = taskService.createTaskQuery().taskId(taskId)
                .active()
                .taskDefinitionKey(taskDefinition)
                .singleResult();
        if (task == null) {
            throw new TaskNotFound();
        }
        return task;
    }

    private void setRequestListData(
            LabRequestRepresentation labRequestRepresentation,
            HistoricProcessInstance instance
            ) {
        // set requester id
        Map<String, Object> variables = instance.getProcessVariables();
        labRequestRepresentation
                .setRequesterId(variables.get("requester_id") == null ? ""
                        : variables.get("requester_id").toString());
        Long userId = null;
        try {
            userId = Long.valueOf(labRequestRepresentation.getRequesterId());
        } catch (NumberFormatException e) {
            log.error(e.getMessage());
        }

        if (userId != null) {
            User user = userRepository.findOne(userId);
            if (user != null) {
                labRequestRepresentation.setRequesterId(user.getId().toString());
                labRequestRepresentation.setRequesterName(RequestFormService
                        .getName(user));
                labRequestRepresentation.setRequesterEmail(user.getContactData().getEmail());
                labRequestRepresentation.setRequester(new ProfileRepresentation(user));
                labRequestRepresentation.setRequesterLab(user.getLab());
            }
            // copy request list representation data
            RequestListRepresentation requestListRepresentation = new RequestListRepresentation();
            requestFormService.transferData(instance, requestListRepresentation, user);
            labRequestRepresentation.setRequest(requestListRepresentation);
        }
    }


    public void transferExcerptListData(
            ExcerptListRepresentation list,
            ExcerptList excerptList,
            Integer labNumber
            ) {

        List<String> columnNames = new ArrayList<String>();
        for (String name : excerptList.getLabRequestColumnNames()) {
            columnNames.add(name);
        }
        list.setColumnNames(columnNames);
        List<ExcerptEntryRepresentation> entries = new ArrayList<ExcerptEntryRepresentation>();
        for (ExcerptEntry entry : excerptList.getEntryValues()) {
            if (entry.isSelected() && entry.getLabNumber().equals(labNumber)) {
                ExcerptEntryRepresentation representation = new ExcerptEntryRepresentation(
                        entry);
                List<String> values = new ArrayList<String>();
                for (String value : entry.getLabRequestValues()) {
                    values.add(value);
                }
                representation.setValues(values);
                assert(representation.getPaNumber().equals(entry.getPaNumber()));
                entries.add(representation);
            }
        }
        list.setEntries(entries);
    }
    
    public void transferExcerptListData(@NotNull LabRequestRepresentation labRequestRepresentation) {
        // set excerpt list data
        ExcerptList excerptList = excerptListService.findByProcessInstanceId(labRequestRepresentation.getProcessInstanceId());
        if (excerptList == null) {
            throw new RequestNotFound();
        }
        labRequestRepresentation.setExcerptListRemark(excerptList.getRemark());
        ExcerptListRepresentation list = new ExcerptListRepresentation();
        transferExcerptListData(list, excerptList, labRequestRepresentation.getLab().getNumber());

        labRequestRepresentation.setExcerptList(list);
    }

    public void transferLabRequestData(@NotNull LabRequestRepresentation labRequestRepresentation) {
        Date start = new Date();

        // get task data
        HistoricTaskInstance task = requestService.getTask(labRequestRepresentation.getTaskId(), "lab_request");
        HistoricVariableInstance status_variable = historyService.createHistoricVariableInstanceQuery()
                .taskId(labRequestRepresentation.getTaskId())
                .variableName("labrequest_status")
                .singleResult();
        if (status_variable != null) {
            labRequestRepresentation.setStatus((String)status_variable.getValue());
        }

        labRequestRepresentation.setDateCreated(task.getCreateTime());
        labRequestRepresentation.setAssignee(task.getAssignee());

        // set pa number count
        labRequestRepresentation.setPathologyCount(pathologyItemRepository.countByLabRequestId(labRequestRepresentation.getId()));
        
        // set request data
        HistoricProcessInstance instance = requestService.getProcessInstance(labRequestRepresentation.getProcessInstanceId());
        setRequestListData(labRequestRepresentation, instance);

        Date end = new Date();
        if ((end.getTime() - start.getTime()) > 10) {
            log.warn("transferLabRequestData took " + (end.getTime() - start.getTime()) + " ms "
                    +" (task id: " + task.getId() + ").");
        }
    }


    public HistoricTaskInstance findLabRequestTaskForLab(
            @NotNull Integer labNumber, @NotNull String processInstanceId) {
        log.info("findLabRequestTasksForLab: lab " + labNumber + ", request "
                + processInstanceId);
        List<HistoricTaskInstance> tasks = new ArrayList<HistoricTaskInstance>();
        Execution execution = runtimeService.createExecutionQuery()
                    .variableValueEquals("lab", labNumber)
                    .processInstanceId(processInstanceId)
                    .singleResult();
        HistoricTaskInstance task = historyService
                .createHistoricTaskInstanceQuery()
                .executionId(execution.getId())
                .taskDefinitionKey("lab_request").singleResult();
        return task;
    }

    // FIXME: add unit test
    @SuppressWarnings("unchecked")
    public void generateLabRequests(String processInstanceId) {
        HistoricProcessInstance instance = requestService.getProcessInstance(processInstanceId);
        Object var = instance.getProcessVariables().get(
                "lab_request_labs");
        log.info("instance: " + instance.getId());
        if (var != null && var instanceof Collection<?>) {
            List<LabRequest> labRequests = new ArrayList<LabRequest>();
            Collection<Integer> labNumbers = (Collection<Integer>) var;
            for (Integer labNumber : labNumbers) {
                Lab lab = labRepository.findByNumber(labNumber);
                HistoricTaskInstance task = findLabRequestTaskForLab(labNumber, instance.getId());

                // set initial status
                taskService.setVariableLocal(task.getId(), "labrequest_status", "Waiting for lab approval");

                // create lab requests
                LabRequest labRequest = new LabRequest();
                labRequest.setLab(lab);
                labRequest.setProcessInstanceId(processInstanceId);
                labRequest.setTaskId(task.getId());
                labRequest = labRequestRepository.save(labRequest);

                ExcerptList excerptList = excerptListService.findByProcessInstanceId(processInstanceId);
                ExcerptListRepresentation list = new ExcerptListRepresentation();
                transferExcerptListData(list, excerptList, labNumber);
                List<PathologyItem> pathologyList = new ArrayList<PathologyItem>();
                for(ExcerptEntryRepresentation entry: list.getEntries()) {
                    pathologyList.add(new PathologyItem(labRequest.getId(), entry.getPaNumber()));
                }
                labRequest.setPathologyList(pathologyList);
                labRequest = labRequestRepository.save(labRequest);
                labRequests.add(labRequest);
            }
            // notify labs by mail
            for (LabRequest labRequest: labRequests) {
                LabRequestRepresentation representation = new LabRequestRepresentation(labRequest);
                transferLabRequestData(representation);
                mailService.notifyLab(representation);
            }
        }
    }

    public List<LabRequestRepresentation> findLabRequestsForUser(User user) {
        List<LabRequestRepresentation> representations = new ArrayList<LabRequestRepresentation>();
        if (user.isLabUser()) {
            // Lab user
            List<LabRequest> labRequests = labRequestRepository.findAllByLab(user.getLab());
            for (LabRequest labRequest : labRequests) {
                LabRequestRepresentation representation = new LabRequestRepresentation(labRequest);
                transferLabRequestData(representation);
                representations.add(representation);
            }
        } else if (user.isPalga()) {
            // Palga
            List<LabRequest> labRequests = labRequestRepository.findAll();
            for (LabRequest labRequest : labRequests) {
                LabRequestRepresentation representation = new LabRequestRepresentation(labRequest);
                transferLabRequestData(representation);
                representations.add(representation);

            }
        } else {
            // fetch requests in status "LabRequest" for requester
            List<HistoricProcessInstance> historicInstances = historyService
                    .createHistoricProcessInstanceQuery()
                    .includeProcessVariables()
                    .involvedUser(user.getId().toString())
                    .variableValueEquals("status", "LabRequest")
                    .orderByProcessInstanceStartTime().desc().list();
            log.info("#instances: " + historicInstances.size());
            // find associated lab requests
            for (HistoricProcessInstance instance : historicInstances) {

                List<LabRequest> labRequests = labRequestRepository.findAllByProcessInstanceId(instance.getId());
                for (LabRequest labRequest : labRequests) {
                    LabRequestRepresentation representation = new LabRequestRepresentation(labRequest);
                    transferLabRequestData(representation);
                    representations.add(representation);

                }
            }
        }
        return representations;
    }

  /**
   * Finds current task. Assumes that exactly one task is currently active.
   * @param taskId
   * @return the current task if it exists.
   * @throws business.exceptions.TaskNotFound
   */
  public Task getTaskByTaskId(String taskId) {
    Task task = taskService.createTaskQuery().taskId(taskId)
      .active()
      .singleResult();
    if (task == null) {
      throw new TaskNotFound();
    }
    return task;
  }

public void transferLabRequestDetails(LabRequestRepresentation representation,
        LabRequest labRequest) {
    List<PathologyRepresentation> pathologyList = new ArrayList<PathologyRepresentation>();
    for(PathologyItem item: labRequest.getPathologyList()) {
        pathologyList.add(new PathologyRepresentation(item));
    }
    representation.setPathologyList(pathologyList);
    List<CommentRepresentation> commentList = new ArrayList<CommentRepresentation>();
    for(Comment comment: labRequest.getComments()) {
        commentList.add(new CommentRepresentation(comment));
    }
    representation.setComments(commentList);
}


}
