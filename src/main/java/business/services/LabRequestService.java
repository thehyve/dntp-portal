/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.validation.constraints.NotNull;

import org.activiti.engine.HistoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import business.controllers.LabRequestComparator;
import business.exceptions.EmailError;
import business.exceptions.RequestNotFound;
import business.exceptions.TaskNotFound;
import business.models.Comment;
import business.models.ExcerptEntry;
import business.models.ExcerptList;
import business.models.Lab;
import business.models.LabRequest;
import business.models.LabRequest.Result;
import business.models.LabRequest.Status;
import business.models.LabRequestRepository;
import business.models.PathologyItem;
import business.models.User;
import business.representation.CommentRepresentation;
import business.representation.LabRequestRepresentation;
import business.representation.PathologyRepresentation;
import business.representation.ProfileRepresentation;
import business.representation.RequestListRepresentation;
import business.representation.RequestStatus;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LabRequestService {

    Logger log = LoggerFactory.getLogger(getClass());

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
    private LabService labService;

    @Autowired
    private LabRequestRepository labRequestRepository;

    @Autowired
    private PathologyItemService pathologyItemService;

    @Autowired
    private ExcerptListService excerptListService;

    @Autowired
    private LabRequestComparator labRequestComparator;

    @Autowired
    private LabRequestListService labRequestListService;

    @Transactional
    @CacheEvict(value = {"labrequestdata", "detailedlabrequestdata"}, key = "#labRequest.id")
    public LabRequest save(LabRequest labRequest) {
        return this.labRequestRepository.save(labRequest);
    }

    @Transactional(readOnly = true)
    public LabRequest findOne(Long id) {
        return this.labRequestRepository.findOne(id);
    }

    /**
     * Counts the number of lab requests.
     * @return the number of lab requests.
     */
    public long count() {
        return labRequestRepository.count();
    }

    public List<LabRequest> findAllByProcessInstanceId(String processInstanceId) {
        return labRequestRepository.findAllByProcessInstanceId(processInstanceId);
    }

    public Long countHubAssistanceLabRequestsForRequest(String processInstanceId) {
        return labRequestRepository.countByProcessInstanceIdAndHubAssistanceRequestedTrue(processInstanceId);
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

    private void setRequestListData(LabRequestRepresentation labRequestRepresentation, boolean cached) {
        RequestListRepresentation request;
        if (cached) {
            request = requestFormService.getRequestListDataCached(labRequestRepresentation.getProcessInstanceId());
        } else {
            request = requestFormService.getRequestListData(labRequestRepresentation.getProcessInstanceId());
            requestFormService.transferPropertiesData(labRequestRepresentation.getProcessInstanceId(), request);
        }
        labRequestRepresentation.setRequest(request);
        if (request.getRequesterId() != null) {
            labRequestRepresentation.setRequesterId(request.getRequesterId());
            labRequestRepresentation.setRequesterName(request.getRequesterName());
            User user;
            if (cached) {
                user = userService.findOneCached(request.getRequesterId());
            } else {
                user = userService.findOne(request.getRequesterId());
            }
            labRequestRepresentation.setRequesterEmail(user.getContactData().getEmail());
            labRequestRepresentation.setRequesterTelephone(user.getContactData().getTelephone());
            labRequestRepresentation.setRequester(new ProfileRepresentation(user));
            labRequestRepresentation.setRequesterLab(user.getLab());
        }
    }

    public void transferPathologyCount(@NotNull LabRequestRepresentation labRequestRepresentation) {
        labRequestRepresentation.setPathologyCount(
                pathologyItemService.getPathologyCountCached(labRequestRepresentation.getId()));
    }

    @Transactional(readOnly = true)
    public void transferExcerptListData(@NotNull LabRequestRepresentation labRequestRepresentation) {
        // set excerpt list data
        ExcerptList excerptList = excerptListService.findByProcessInstanceId(labRequestRepresentation.getProcessInstanceId());
        if (excerptList == null) {
            throw new RequestNotFound();
        }
        labRequestRepresentation.setExcerptListRemark(excerptList.getRemark());
    }

    public void transferLabRequestData(@NotNull LabRequestRepresentation labRequestRepresentation, boolean cached) {
        log.debug("Fetching data for lab request {}", labRequestRepresentation.getId());
        Date start = new Date();

        // get task data
        HistoricTaskInstance task = requestService.getTask(labRequestRepresentation.getTaskId(), "lab_request");
        labRequestRepresentation.setDateCreated(task.getCreateTime());
        labRequestRepresentation.setEndDate(task.getEndTime());

        if (task.getEndTime() == null && task.getAssignee() != null && !task.getAssignee().isEmpty()) {
            labRequestRepresentation.setAssignee(task.getAssignee());
            Long assigneeId = null;
            try {
                assigneeId = Long.valueOf(task.getAssignee());
            } catch (NumberFormatException e) {
            }
            if (assigneeId != null) {
                User assignee;
                if (cached) {
                    assignee = userService.findOneCached(assigneeId);
                } else {
                    assignee = userService.findOne(assigneeId);
                }
                if (assignee != null) {
                    labRequestRepresentation.setAssigneeName(RequestFormService.getName(assignee));
                }
            }
        }

        // set request data
        setRequestListData(labRequestRepresentation, cached);

        labRequestRepresentation.setLabRequestCode();

        transferPathologyCount(labRequestRepresentation);

        Date end = new Date();
        if ((end.getTime() - start.getTime()) > 10) {
            log.warn(String.format("transfer lab request data took: %6d ms (task id: %s).",
                    end.getTime() - start.getTime(),
                    task.getId()
                    ));
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
    @CacheEvict(value = {"labrequestdata", "detailedlabrequestdata"}, key = "#labRequest.id")
    public LabRequest updateStatus(LabRequest labRequest, Status status) {
        taskService.setVariableLocal(labRequest.getTaskId(), "labrequest_status", status);
        labRequest.setStatus(status);
        return labRequestRepository.save(labRequest);
    }

    @Transactional
    @CacheEvict(value = {"labrequestdata", "detailedlabrequestdata"}, key = "#labRequest.id")
    public LabRequest updateStatus(LabRequest labRequest, Status status, Result result) {
        taskService.setVariableLocal(labRequest.getTaskId(), "labrequest_status", status);
        labRequest.setStatus(status);
        labRequest.setResult(result);
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
                Lab lab = labService.findByNumber(labNumber);
                HistoricTaskInstance task = findLabRequestTaskForLab(labNumber, instance.getId());

                // create lab requests
                LabRequest labRequest = new LabRequest();
                labRequest.setTimeCreated(new Date());
                labRequest.setLab(lab);
                labRequest.setProcessInstanceId(processInstanceId);
                labRequest.setTaskId(task.getId());
                labRequest = labRequestRepository.save(labRequest);
                // set initial status
                labRequest = updateStatus(labRequest, Status.WAITING_FOR_LAB_APPROVAL);
                labRequest.setHubAssistanceRequested(lab.isHubAssistanceEnabled());
                if (lab.isHubAssistanceEnabled()) {
                    hubUsers.addAll(userService.findHubUsersForLab(lab));
                }

                ExcerptList excerptList = excerptListService.findByProcessInstanceId(processInstanceId);
                List<PathologyItem> pathologyList = new ArrayList<PathologyItem>();
                for(ExcerptEntry entry: excerptList.getEntries()) {
                    if (entry.isSelected() && labNumber.equals(entry.getLabNumber())) {
                        pathologyList.add(new PathologyItem(labRequest.getId(), entry));
                    }
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
                transferLabRequestData(representation, false);
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
                                ". Email addresses: '" +
                                        representation.getLab().getEmailAddresses() == null ?
                                        "" : String.join(", ", representation.getLab().getEmailAddresses()) +
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

    private static Sort sortByIdDesc() {
        return new Sort(Sort.Direction.DESC, "id");
    }

    private List<LabRequestRepresentation> convertLabRequestsToRepresentations(List<LabRequest> labRequests,
                                                                               boolean fetchDetails) {
        List<LabRequestRepresentation> representations = new ArrayList<>();
        for (LabRequest labRequest : labRequests) {
            LabRequestRepresentation representation;
            if (fetchDetails) {
                representation = labRequestListService.getDetailedLabRequestCached(labRequest);
            } else {
                representation = labRequestListService.getLabRequestCached(labRequest);
            }
            representations.add(representation);
        }
        return representations;
    }

    @Transactional(readOnly = true)
    public List<LabRequestRepresentation> findLabRequestsForLabUserOrHubUser(User user, boolean fetchDetails) {
        List<LabRequestRepresentation> representations = null;
        List<LabRequest> labRequests;

        if (user.isLabUser()) {
            // Lab user
            labRequests = labRequestRepository.findAllByLab(user.getLab(), sortByIdDesc());
            representations = convertLabRequestsToRepresentations(labRequests, fetchDetails);
        } else if (user.isHubUser()) {
            // Hub user
            Set<Lab> hubLabs = new HashSet<>();
            for (Lab lab: user.getHubLabs()) {
                if (lab.isHubAssistanceEnabled()) {
                    hubLabs.add(lab);
                }
            }
            labRequests = labRequestRepository.findAllByLabIn(hubLabs, sortByIdDesc());
            representations = convertLabRequestsToRepresentations(labRequests, fetchDetails);
        }
        return representations;
    }

    @Transactional(readOnly = true)
    public List<LabRequestRepresentation> findLabRequestsForUser(User user, boolean fetchDetails) {
        List<LabRequestRepresentation> representations = null;
        if (user.isLabUser() || user.isHubUser()) {
            representations = findLabRequestsForLabUserOrHubUser(user, fetchDetails);
        } else if (user.isPalga()) {
            // Palga
            List<LabRequest> labRequests = labRequestRepository.findAll(sortByIdDesc());
            representations = convertLabRequestsToRepresentations(labRequests, fetchDetails);
        } else {
            // fetch requests in status "LabRequest" for requester
            List<HistoricProcessInstance> historicInstances = new ArrayList<>();
            for (HistoricProcessInstance instance: requestService.getProcessInstancesForUser(user)) {
                String statusText = (String)instance.getProcessVariables().get("status");
                if (statusText != null) {
                    RequestStatus status = RequestStatus.forDescription(statusText);
                    if (status == RequestStatus.LAB_REQUEST) {
                        historicInstances.add(instance);
                    }
                }
            }
            log.info("#instances: " + historicInstances.size());
            representations = new ArrayList<>();
            // find associated lab requests
            for (HistoricProcessInstance instance : historicInstances) {
                List<LabRequest> labRequests = labRequestRepository.findAllByProcessInstanceId(instance.getId(), sortByIdDesc());
                representations.addAll(convertLabRequestsToRepresentations(labRequests, fetchDetails));
            }
        }
        Collections.sort(representations, Collections.reverseOrder(labRequestComparator));
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

    @Transactional(readOnly = true)
    public void transferLabRequestDetails(LabRequestRepresentation representation, boolean fetchSamples) {
        LabRequest labRequest = labRequestRepository.findOne(representation.getId());
        transferLabRequestDetails(representation, labRequest, fetchSamples);
    }
    
    protected void transferLabRequestDetails(LabRequestRepresentation representation, LabRequest labRequest, boolean fetchSamples) {
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
