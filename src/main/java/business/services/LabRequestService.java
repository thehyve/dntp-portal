package business.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.activiti.engine.HistoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.runtime.Execution;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.bind.PropertiesConfigurationFactory;
import org.springframework.stereotype.Service;

import business.models.ExcerptEntry;
import business.models.ExcerptList;
import business.models.Lab;
import business.models.LabRepository;
import business.models.RequestProperties;
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
    private RuntimeService runtimeService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private LabRepository labRepository;

    private void setRequestListData(
            LabRequestRepresentation labRequestRepresentation,
            HistoricProcessInstance instance,
            Map<String, RequestListRepresentation> requestListCache
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
            RequestListRepresentation requestListRepresentation = requestListCache.get(instance.getId());
            if (requestListRepresentation == null) {
                requestListRepresentation = new RequestListRepresentation();
                requestFormService.transferData(instance,
                    requestListRepresentation, user);
                requestListCache.put(instance.getId(), requestListRepresentation);
            }
            labRequestRepresentation.setRequestListRepresentation(requestListRepresentation);
        } 
    }
    
    public void transferLabRequestData(
            @NotNull LabRequestRepresentation labRequestRepresentation,
            @NotNull HistoricTaskInstance task, 
            @NotNull Lab lab,
            Map<String, RequestListRepresentation> requestListCache,
            Map<String, HistoricProcessInstance> instanceCache,
            Map<String, RequestProperties> requestPropertiesCache
            ) {
        Date start = new Date();
        labRequestRepresentation.setTaskId(task.getId());
        labRequestRepresentation.setProcessInstanceId(task
                .getProcessInstanceId());

        // get task data
        Map<String, Object> taskVariables = task.getTaskLocalVariables();
        labRequestRepresentation.setStatus((String) taskVariables
                .get("labrequest_status"));
        labRequestRepresentation.setDateCreated(task.getCreateTime());

        // set main request data
        HistoricProcessInstance instance = instanceCache.get(task.getProcessInstanceId());
        if (instance == null) {
            instance = historyService
                    .createHistoricProcessInstanceQuery()
                    .processInstanceId(task.getProcessInstanceId())
                    .includeProcessVariables().singleResult();
            instanceCache.put(task.getProcessInstanceId(), instance);
        }
        setRequestListData(labRequestRepresentation, instance, requestListCache);
        
        // set lab data
        labRequestRepresentation.setLab(lab);

        // set excerpt list data
        RequestProperties properties = requestPropertiesCache.get(task.getProcessInstanceId());
        if (properties == null) {
            // FIXME: performance is slow here when the excerpt list is fetched as well
            properties =
                requestPropertiesService
                .findByProcessInstanceId(task.getProcessInstanceId());
        }
        labRequestRepresentation.setExcerptListRemark(properties
                .getExcerptListRemark());
        
        ExcerptList list = properties.getExcerptList();
        ExcerptListRepresentation excerptList = new ExcerptListRepresentation();
        List<String> columnNames = new ArrayList<String>();
        for (String name : list.getLabRequestColumnNames()) {
            columnNames.add(name);
        }
        excerptList.setColumnNames(columnNames);
        List<ExcerptEntryRepresentation> entries = new ArrayList<ExcerptEntryRepresentation>();
        for (ExcerptEntry entry : properties.getExcerptList().getEntryValues()) {
            if (entry.isSelected() && entry.getLabNumber().equals(lab.getNumber())) {
                ExcerptEntryRepresentation representation = new ExcerptEntryRepresentation(
                        entry);
                List<String> values = new ArrayList<String>();
                for (String value : entry.getLabRequestValues()) {
                    values.add(value);
                }
                representation.setValues(values);
                entries.add(representation);
            }
        }
        excerptList.setEntries(entries);
        labRequestRepresentation.setExcerptList(excerptList);
        Date end = new Date();
        if ((end.getTime() - start.getTime()) > 10) {
            log.warn("transferLabRequestData took " + (end.getTime() - start.getTime()) + " ms "
                    +" (task id: " + task.getId() + ").");
        }
    }

    public List<HistoricTaskInstance> findLabRequestTasksForRequester(User user) {
        return historyService
                .createHistoricTaskInstanceQuery()
                .taskDefinitionKey("lab_request")
                .processVariableValueEquals("requester_id",
                        user.getId().toString()).orderByTaskCreateTime().desc()
                .list();
    }

    public List<HistoricTaskInstance> findLabRequestTasksForLab(
            Integer labNumber, String processInstanceId) {
        log.info("findLabRequestTasksForLab: lab " + labNumber + ", request "
                + processInstanceId);
        List<HistoricTaskInstance> tasks = new ArrayList<HistoricTaskInstance>();
        List<Execution> executions;
        if (processInstanceId == null) {
            executions = runtimeService.createExecutionQuery()
                    .variableValueEquals("lab", labNumber).list();
        } else {
            executions = runtimeService.createExecutionQuery()
                    .variableValueEquals("lab", labNumber)
                    .processInstanceId(processInstanceId).list();
        }
        for (Execution execution : executions) {
            HistoricTaskInstance task = historyService
                    .createHistoricTaskInstanceQuery()
                    .executionId(execution.getId())
                    .taskDefinitionKey("lab_request").singleResult();
            if (task != null) {
                tasks.add(task);
            } else {
                log.info("(task is null)");
            }

        }
        return tasks;
    }

    public List<HistoricTaskInstance> findAllLabRequestTasksForLab(
            Integer labNumber) {
        return findLabRequestTasksForLab(labNumber, null);
    }
    
    
    public List<HistoricTaskInstance> findLabRequestTasksForPalga() {
        return historyService.createHistoricTaskInstanceQuery()
                .taskDefinitionKey("lab_request").orderByTaskCreateTime()
                .desc().list();
    }

    @SuppressWarnings("unchecked")
    public List<LabRequestRepresentation> findLabRequestsForUser(User user) {
        List<LabRequestRepresentation> labrequests = new ArrayList<LabRequestRepresentation>();
        Map<String, RequestListRepresentation> requestListCache = new HashMap<String, RequestListRepresentation>();
        Map<String, HistoricProcessInstance> instanceCache = new HashMap<String, HistoricProcessInstance>();
        Map<String, RequestProperties> requestPropertiesCache = new HashMap<String, RequestProperties>();
        if (user.isLabUser()) {
            Lab lab = user.getLab();
            // fetch tasks for lab user
            List<HistoricTaskInstance> tasks = findLabRequestTasksForLab(user
                    .getLab().getNumber(), null);
            for (HistoricTaskInstance task : tasks) {
                LabRequestRepresentation labrequest = new LabRequestRepresentation();
                transferLabRequestData(labrequest, task, 
                        lab,
                        requestListCache,
                        instanceCache,
                        requestPropertiesCache);
                labrequests.add(labrequest);
            }
        } else if (user.isPalga()) {
            // fetch lab request tasks for requester
            Date start = new Date();
            List<Lab> labs = labRepository.findAll();
            for(Lab lab: labs) {
                List<HistoricTaskInstance> tasks = findAllLabRequestTasksForLab(lab.getNumber());
                for (HistoricTaskInstance task : tasks) {
                    LabRequestRepresentation labrequest = new LabRequestRepresentation();
                    transferLabRequestData(labrequest, task, lab, 
                            requestListCache, instanceCache, requestPropertiesCache);
                    labrequests.add(labrequest);
                }
            }
            Date end = new Date();
            if ((end.getTime() - start.getTime()) > 10) {
                log.warn("LabRequestService took " + (end.getTime() - start.getTime()) + " ms.");
            }
        } else {
            // fetch lab request tasks for requester
            List<HistoricProcessInstance> historicInstances = historyService
                    .createHistoricProcessInstanceQuery()
                    .includeProcessVariables()
                    .involvedUser(user.getId().toString())
                    .variableValueEquals("status", "LabRequest")
                    .orderByProcessInstanceStartTime().desc().list();
            log.info("#instances: " + historicInstances.size());
            // find associated lab requests
            for (HistoricProcessInstance instance : historicInstances) {
                Object var = instance.getProcessVariables().get(
                        "lab_request_labs");
                log.info("instance: " + instance.getId());
                if (var != null && var instanceof Collection<?>) {
                    Collection<Integer> labNumbers = (Collection<Integer>) var;
                    for (Integer labNumber : labNumbers) {
                        Lab lab = labRepository.findByNumber(labNumber);
                        List<HistoricTaskInstance> tasks = findLabRequestTasksForLab(
                                labNumber, instance.getId());
                        for (HistoricTaskInstance task : tasks) {
                            LabRequestRepresentation labrequest = new LabRequestRepresentation();
                            transferLabRequestData(labrequest, task, lab, 
                                    requestListCache, instanceCache, requestPropertiesCache);
                            labrequests.add(labrequest);
                        }
                    }
                }
            }
        }
        return labrequests;
    }

}
