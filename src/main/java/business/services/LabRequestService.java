package business.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.activiti.engine.HistoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.task.Task;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import business.exceptions.RequestNotFound;
import business.exceptions.TaskNotFound;
import business.models.ExcerptEntry;
import business.models.ExcerptList;
import business.models.Lab;
import business.models.LabRepository;
import business.models.LabRequest;
import business.models.LabRequestRepository;
import business.models.User;
import business.models.UserRepository;
import business.representation.ExcerptEntryRepresentation;
import business.representation.ExcerptListRepresentation;
import business.representation.LabRequestRepresentation;
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
    private RuntimeService runtimeService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private LabRepository labRepository;

    @Autowired
    private LabRequestRepository labRequestRepository;
    
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
            }
            // copy request list representation data
            RequestListRepresentation requestListRepresentation = new RequestListRepresentation();
            requestFormService.transferData(instance, requestListRepresentation, user);
            labRequestRepresentation.setRequestListRepresentation(requestListRepresentation);
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
    
    public void transferLabRequestData(
            @NotNull LabRequestRepresentation labRequestRepresentation,
            @NotNull LabRequest labRequest 
            ) {
        Date start = new Date();
        labRequestRepresentation.setId(labRequest.getId());
        labRequestRepresentation.setTaskId(labRequest.getTaskId());
        labRequestRepresentation.setProcessInstanceId(labRequest
                .getProcessInstanceId());

        // get task data
        Task task = requestService.getTask(labRequest.getTaskId(), "lab_request"); 
        String status = taskService.getVariableLocal(labRequest.getTaskId(), "labrequest_status", String.class);
        labRequestRepresentation.setStatus(status);
        labRequestRepresentation.setDateCreated(task.getCreateTime());

        // set request data
        HistoricProcessInstance instance = requestService.getProcessInstance(labRequest.getProcessInstanceId());
        setRequestListData(labRequestRepresentation, instance);
        
        // set lab data
        labRequestRepresentation.setLab(labRequest.getLab());

        // set excerpt list data
        ExcerptList excerptList = excerptListService.findByProcessInstanceId(task.getProcessInstanceId());
        if (excerptList == null) {
            throw new RequestNotFound();
        }
        labRequestRepresentation.setExcerptListRemark(excerptList.getRemark());
        ExcerptListRepresentation list = new ExcerptListRepresentation();
        transferExcerptListData(list, excerptList, labRequest.getLab().getNumber());
        
        labRequestRepresentation.setExcerptList(list);
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

    @SuppressWarnings("unchecked")
    public void generateLabRequests(String processInstanceId) {
        HistoricProcessInstance instance = requestService.getProcessInstance(processInstanceId);
        Object var = instance.getProcessVariables().get(
                "lab_request_labs");
        log.info("instance: " + instance.getId());
        if (var != null && var instanceof Collection<?>) {
            Collection<Integer> labNumbers = (Collection<Integer>) var;
            for (Integer labNumber : labNumbers) {
                Lab lab = labRepository.findByNumber(labNumber);
                HistoricTaskInstance task = findLabRequestTaskForLab(labNumber, instance.getId());
                // create lab requests
                LabRequest labRequest = new LabRequest();
                labRequest.setLab(lab);
                labRequest.setProcessInstanceId(processInstanceId);
                labRequest.setTaskId(task.getId());
                
                ExcerptList excerptList = excerptListService.findByProcessInstanceId(processInstanceId);
                ExcerptListRepresentation list = new ExcerptListRepresentation();
                transferExcerptListData(list, excerptList, labNumber);
                List<String> paNumbers = new ArrayList<String>();
                for(ExcerptEntryRepresentation entry: list.getEntries()) {
                    paNumbers.add(entry.getPaNumber());
                }
                labRequest.setPaNumbers(paNumbers);
                labRequestRepository.save(labRequest);
            }
        }
    }
    
    public List<LabRequestRepresentation> findLabRequestsForUser(User user) {
        List<LabRequestRepresentation> representations = new ArrayList<LabRequestRepresentation>();
        if (user.isLabUser()) {
            // Lab user
            List<LabRequest> labRequests = labRequestRepository.findAllByLab(user.getLab());
            for (LabRequest labRequest : labRequests) {
                LabRequestRepresentation representation = new LabRequestRepresentation();
                transferLabRequestData(representation, labRequest);
                representations.add(representation);
            }
        } else if (user.isPalga()) {
            // Palga
            List<LabRequest> labRequests = labRequestRepository.findAll();
            for (LabRequest labRequest : labRequests) {
                LabRequestRepresentation representation = new LabRequestRepresentation();
                transferLabRequestData(representation, labRequest);
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
                    LabRequestRepresentation representation = new LabRequestRepresentation();
                    transferLabRequestData(representation, labRequest);
                    representations.add(representation);
                }
            }
        }
        return representations;
    }

}
