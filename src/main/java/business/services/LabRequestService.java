/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.transaction.Transactional;
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
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import business.exceptions.EmailError;
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
    private UserService userService;

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

    @Transactional
    public LabRequest save(LabRequest labRequest) {
        return this.labRequestRepository.save(labRequest);
    }

    @Transactional
    public LabRequest findOne(Long id) {
        return this.labRequestRepository.findOne(id);
    }
    
    
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
        // copy request list representation data
        RequestListRepresentation request = new RequestListRepresentation();
        requestFormService.transferBasicData(instance, request);
        labRequestRepresentation.setRequest(request);
        if (request.getRequesterId() != null) {
            labRequestRepresentation.setRequesterId(request.getRequesterId());
            labRequestRepresentation.setRequesterName(request.getRequesterName());
            User user = userService.findOne(request.getRequesterId());
            labRequestRepresentation.setRequesterEmail(user.getContactData().getEmail());
            labRequestRepresentation.setRequester(new ProfileRepresentation(user));
            labRequestRepresentation.setRequesterLab(user.getLab());
        }
    }


    @Transactional
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
        List<ExcerptEntryRepresentation> representations = new ArrayList<ExcerptEntryRepresentation>();
        List<ExcerptEntry> entries = excerptList.getEntries();
        for (ExcerptEntry entry : entries) {
            if (entry.isSelected() && entry.getLabNumber().equals(labNumber)) {
                ExcerptEntryRepresentation representation = new ExcerptEntryRepresentation(
                        entry);
                List<String> values = new ArrayList<String>();
                for (String value : entry.getLabRequestValues()) {
                    values.add(value);
                }
                representation.setValues(values);
                assert(representation.getPaNumber().equals(entry.getPaNumber()));
                representations.add(representation);
            }
        }
        list.setEntries(representations);
    }

    public void transferPathologyCount(@NotNull LabRequestRepresentation labRequestRepresentation) {
        labRequestRepresentation.setPathologyCount(pathologyItemRepository.countByLabRequestId(labRequestRepresentation.getId()));
    }

    @Transactional
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
        labRequestRepresentation.setDateCreated(task.getCreateTime());
        labRequestRepresentation.setEndDate(task.getEndTime());
        labRequestRepresentation.setAssignee(task.getAssignee());

        // set request data
        HistoricProcessInstance instance = requestService.getProcessInstance(labRequestRepresentation.getProcessInstanceId());
        setRequestListData(labRequestRepresentation, instance);

        labRequestRepresentation.setLabRequestCode();

        transferPathologyCount(labRequestRepresentation);

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

    @Transactional
    public LabRequest updateStatus(LabRequest labRequest, String status) {
        taskService.setVariableLocal(labRequest.getTaskId(), "labrequest_status", status);
        labRequest.setStatus(status);
        return labRequestRepository.save(labRequest);
    }
    
    @SuppressWarnings("unchecked")
    @Transactional
    public void generateLabRequests(String processInstanceId) {
        HistoricProcessInstance instance = requestService.getProcessInstance(processInstanceId);
        Object var = instance.getProcessVariables().get(
                "lab_request_labs");
        log.info("instance: " + instance.getId());
        if (var != null && var instanceof Collection<?>) {
            List<LabRequest> labRequests = new ArrayList<LabRequest>();
            SortedSet<Integer> labNumbers = new TreeSet<>((Collection<Integer>) var);
            Set<User> hubUsers = new HashSet<>();
            for (Integer labNumber : labNumbers) {
                Lab lab = labRepository.findByNumber(labNumber);
                HistoricTaskInstance task = findLabRequestTaskForLab(labNumber, instance.getId());

                // create lab requests
                LabRequest labRequest = new LabRequest();
                labRequest.setTimeCreated(new Date());
                labRequest.setLab(lab);
                labRequest.setProcessInstanceId(processInstanceId);
                labRequest.setTaskId(task.getId());
                labRequest = labRequestRepository.save(labRequest);
                // set initial status
                labRequest = updateStatus(labRequest, "Waiting for lab approval");
                labRequest.setHubAssistanceRequested(lab.isHubAssistanceEnabled());
                if (lab.isHubAssistanceEnabled()) {
                    hubUsers.addAll(userService.findHubUsersForLab(lab));
                }

                ExcerptList excerptList = excerptListService.findByProcessInstanceId(processInstanceId);
                ExcerptListRepresentation list = new ExcerptListRepresentation();
                transferExcerptListData(list, excerptList, labNumber);
                List<PathologyItem> pathologyList = new ArrayList<PathologyItem>();
                for(ExcerptEntryRepresentation entry: list.getEntries()) {
                    pathologyList.add(new PathologyItem(labRequest.getId(), entry.getPaNumber()));
                }
                labRequest.setPathologyList(pathologyList);
                labRequest = labRequestRepository.save(labRequest);
                log.info("Saved lab request " + labRequest.getId() + " for lab " + labNumber + 
                        " with " + pathologyList.size() + " pathology items.");
                labRequests.add(labRequest);
            }
            Map<Integer, LabRequestRepresentation> representationMap = new TreeMap<>();
            // notify labs by mail
            for (LabRequest labRequest: labRequests) {
                LabRequestRepresentation representation = new LabRequestRepresentation(labRequest);
                transferLabRequestData(representation);
                if (representation.getLab() == null) {
                    log.warn("No lab for lab request " + representation.getLabRequestCode() +
                            " while gerating lab requests.");
                } else {
                    representationMap.put(representation.getLab().getNumber(), representation);
                    try {
                        mailService.notifyLab(representation);
                    } catch (EmailError e) {
                        log.warn("No mail sent to lab " + representation.getLab().getNumber() +
                                " for lab request " + representation.getLabRequestCode() +
                                ". Email address: '" +
                                representation.getLab().getContactData() == null ?
                                        "" : representation.getLab().getContactData().getEmail() +
                                "'.");
                        // FIXME: return error messages.
                    }
                }
            }
            // notify hub users by mail
            for (User u: hubUsers) {
                // build list of lab request representations for the lab requests for labs
                // associated with the hub user.
                List<LabRequestRepresentation> representations = new ArrayList<>();
                List<String> labRequestCodes = new ArrayList<>();
                for (Lab l: u.getHubLabs()) {
                    if (l.isHubAssistanceEnabled()) {
                        LabRequestRepresentation representation = representationMap.get(l.getNumber());
                        if (representation != null) {
                            representations.add(representation);
                            labRequestCodes.add(representation.getLabRequestCode());
                        }
                    }
                }
                String labRequestCodesString = String.join(", ", labRequestCodes);
                // send mail to hub user
                try {
                    mailService.notifyHubuser(u, representations);
                } catch (EmailError e) {
                    log.warn("No mail sent to hub user " + u.getUsername() +
                            " for lab requests " + labRequestCodesString +
                            ". Email address: '" +
                            u.getContactData() == null ?
                                    "" : u.getContactData().getEmail() +
                            "'.");
                    // FIXME: return error messages.
                }
            }
        }
    }

    private Sort sortByIdDesc() {
        return new Sort(Sort.Direction.DESC, "id");
    }
    
    @Transactional
    public List<LabRequestRepresentation> findLabRequestsForUser(User user, boolean fetchDetails) {
        List<LabRequestRepresentation> representations = new ArrayList<LabRequestRepresentation>();
        if (user.isLabUser()) {
            // Lab user
            List<LabRequest> labRequests = labRequestRepository.findAllByLab(user.getLab(), sortByIdDesc());
            for (LabRequest labRequest : labRequests) {
                LabRequestRepresentation representation = new LabRequestRepresentation(labRequest);
                transferLabRequestData(representation);
                if (fetchDetails) {
                    transferLabRequestDetails(representation, labRequest, true);
                }
                representations.add(representation);
            }
        } else if (user.isHubUser()) {
            // Hub user
            Set<Lab> hubLabs = new HashSet<>();
            for (Lab lab: user.getHubLabs()) {
                if (lab.isHubAssistanceEnabled()) {
                    hubLabs.add(lab);
                }
            }
            List<LabRequest> labRequests = labRequestRepository.findAllByLabIn(hubLabs, sortByIdDesc());
            for (LabRequest labRequest : labRequests) {
                LabRequestRepresentation representation = new LabRequestRepresentation(labRequest);
                transferLabRequestData(representation);
                if (fetchDetails) {
                    transferLabRequestDetails(representation, labRequest, true);
                }
                representations.add(representation);
            }
        } else if (user.isPalga()) {
            // Palga
            List<LabRequest> labRequests = labRequestRepository.findAll(sortByIdDesc());
            for (LabRequest labRequest : labRequests) {
                LabRequestRepresentation representation = new LabRequestRepresentation(labRequest);
                transferLabRequestData(representation);
                if (fetchDetails) {
                    transferLabRequestDetails(representation, labRequest, true);
                }
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
                List<LabRequest> labRequests = labRequestRepository.findAllByProcessInstanceId(instance.getId(), sortByIdDesc());
                for (LabRequest labRequest : labRequests) {
                    LabRequestRepresentation representation = new LabRequestRepresentation(labRequest);
                    transferLabRequestData(representation);
                    if (fetchDetails) {
                        transferLabRequestDetails(representation, labRequest, true);
                    }
                    representations.add(representation);
                }
            }
        }
        return representations;
    }

    /**
     * Finds current task. Assumes that exactly one task is currently active.
     * 
     * @param taskId
     * @return the current task if it exists.
     * @throws business.exceptions.TaskNotFound
     */
    public Task getTaskByTaskId(String taskId) {
        Task task = taskService.createTaskQuery().taskId(taskId).active()
                .singleResult();
        if (task == null) {
            throw new TaskNotFound();
        }
        return task;
    }

    @Transactional
    public void transferLabRequestDetails(LabRequestRepresentation representation, boolean fetchSamples) {
        LabRequest labRequest = labRequestRepository.findOne(representation.getId());
        transferLabRequestDetails(representation, labRequest, fetchSamples);
    }
    
    private void transferLabRequestDetails(LabRequestRepresentation representation, LabRequest labRequest, boolean fetchSamples) {
        List<PathologyRepresentation> pathologyList = new ArrayList<PathologyRepresentation>();
        for (PathologyItem item : labRequest.getPathologyList()) {
            PathologyRepresentation pathology = new PathologyRepresentation(item);
            if (fetchSamples) {
                pathology.mapSamples(item);
            }
            pathologyList.add(pathology);
        }
        representation.setPathologyCount((long) pathologyList.size());
        representation.setPathologyList(pathologyList);
        List<CommentRepresentation> commentList = new ArrayList<CommentRepresentation>();
        for (Comment comment : labRequest.getComments()) {
            commentList.add(new CommentRepresentation(comment));
        }
        representation.setComments(commentList);
    }

}
