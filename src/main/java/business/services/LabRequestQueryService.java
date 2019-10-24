/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business.services;

import business.controllers.LabRequestComparator;
import business.exceptions.*;
import business.models.*;
import business.representation.*;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class LabRequestQueryService {

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
    private RuntimeService runtimeService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private LabRequestRepository labRequestRepository;

    @Autowired
    private PathologyItemService pathologyItemService;

    @Autowired
    private ExcerptListService excerptListService;

    @Autowired
    private LabRequestComparator labRequestComparator;

    @Autowired
    private LabRequestCacheService labRequestCacheService;

    @Autowired
    private RequestQueryService requestQueryService;


    public LabRequest findOne(Long id) {
        return this.labRequestRepository.findOne(id);
    }

    public LabRequestRepresentation get(Long id) {
        LabRequest labRequest = findOne(id);
        LabRequestRepresentation representation = new LabRequestRepresentation(labRequest);
        transferLabRequestData(representation, false);
        transferLabRequestDetails(representation, true);
        transferExcerptListData(representation);
        return representation;
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
     * @throws TaskNotFound
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
            String assigneeId = task.getAssignee();
            labRequestRepresentation.setAssignee(assigneeId);
            labRequestRepresentation.setAssigneeName(userService.getFullNameByUserId(assigneeId, cached));
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

    private static Sort sortByIdDesc() {
        return new Sort(Sort.Direction.DESC, "id");
    }

    private List<LabRequestRepresentation> convertLabRequestsToRepresentations(List<LabRequest> labRequests,
                                                                               boolean fetchDetails) {
        List<LabRequestRepresentation> representations = new ArrayList<>();
        for (LabRequest labRequest : labRequests) {
            LabRequestRepresentation representation;
            if (fetchDetails) {
                representation = labRequestCacheService.getDetailedLabRequestCached(labRequest);
            } else {
                representation = labRequestCacheService.getLabRequestCached(labRequest);
            }
            representations.add(representation);
        }
        return representations;
    }

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

    public List<LabRequestRepresentation> findLabRequestsForUser(User user, boolean fetchDetails) {
        List<LabRequestRepresentation> representations;
        if (user.isLabUser() || user.isHubUser()) {
            representations = findLabRequestsForLabUserOrHubUser(user, fetchDetails);
        } else if (user.isPalga()) {
            // Palga
            List<LabRequest> labRequests = labRequestRepository.findAll(sortByIdDesc());
            representations = convertLabRequestsToRepresentations(labRequests, fetchDetails);
        } else {
            // fetch requests in status "LabRequest" for requester
            List<String> requestIds = requestQueryService.getRequestsForRequesterByStatus(user, RequestStatus.LAB_REQUEST);
            log.info("#instances: " + requestIds.size());
            representations = new ArrayList<>();
            // find associated lab requests
            for (String id: requestIds) {
                List<LabRequest> labRequests = labRequestRepository.findAllByProcessInstanceId(id, sortByIdDesc());
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
     * @throws TaskNotFound
     */
    public Task getTaskByTaskId(String taskId) {
        Task task = taskService.createTaskQuery().taskId(taskId).active()
                .singleResult();
        if (task == null) {
            throw new TaskNotFound();
        }
        return task;
    }

    public void transferLabRequestDetails(LabRequestRepresentation representation, boolean fetchSamples) {
        LabRequest labRequest = labRequestRepository.findOne(representation.getId());
        transferLabRequestDetails(representation, labRequest, fetchSamples);
    }
    
    void transferLabRequestDetails(LabRequestRepresentation representation, LabRequest labRequest, boolean fetchSamples) {
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
