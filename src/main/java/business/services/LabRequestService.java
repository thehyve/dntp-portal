/*
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business.services;

import java.util.*;

import business.exceptions.*;
import business.representation.*;
import business.security.UserAuthenticationToken;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import business.models.ExcerptEntry;
import business.models.ExcerptList;
import business.models.Lab;
import business.models.LabRequest;
import business.models.LabRequest.Status;
import business.models.LabRequestRepository;
import business.models.PathologyItem;
import business.models.User;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class LabRequestService {

    Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private UserService userService;

    @Autowired
    private RequestFormService requestFormService;

    @Autowired
    private RequestService requestService;

    @Autowired
    private MailService mailService;

    @Autowired
    private LabService labService;

    @Autowired
    private LabRequestRepository labRequestRepository;

    @Autowired
    private ExcerptListService excerptListService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private LabRequestQueryService labRequestQueryService;

    @Autowired
    private LabRequestStatusService labRequestStatusService;


    @CacheEvict(value = {"labrequestdata", "detailedlabrequestdata"}, key = "#labRequest.id")
    public LabRequest save(LabRequest labRequest) {
        return this.labRequestRepository.save(labRequest);
    }

    @SuppressWarnings("unchecked")
    public void generateLabRequests(String processInstanceId) {
        HistoricProcessInstance instance = requestService.getProcessInstance(processInstanceId);
        Object var = instance.getProcessVariables().get(
                "lab_request_labs");
        log.info("instance: " + instance.getId());
        if (var != null && var instanceof Collection<?>) {
            List<LabRequest> labRequests = new ArrayList<>();
            SortedSet<Integer> labNumbers = new TreeSet<>((Collection<Integer>) var);
            Set<User> hubUsers = new HashSet<>();
            for (Integer labNumber : labNumbers) {
                Lab lab = labService.findByNumber(labNumber);
                HistoricTaskInstance task = labRequestQueryService.findLabRequestTaskForLab(labNumber, instance.getId());

                // create lab requests
                LabRequest labRequest = new LabRequest();
                labRequest.setTimeCreated(new Date());
                labRequest.setLab(lab);
                labRequest.setProcessInstanceId(processInstanceId);
                labRequest.setTaskId(task.getId());
                labRequest = labRequestRepository.save(labRequest);
                // set initial status
                labRequest = labRequestStatusService.updateStatus(labRequest, Status.WAITING_FOR_LAB_APPROVAL);
                labRequest.setHubAssistanceRequested(lab.isHubAssistanceEnabled());
                if (lab.isHubAssistanceEnabled()) {
                    hubUsers.addAll(userService.findHubUsersForLab(lab));
                }

                ExcerptList excerptList = excerptListService.findByProcessInstanceId(processInstanceId);
                List<PathologyItem> pathologyList = new ArrayList<>();
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
                labRequestQueryService.transferLabRequestData(representation, false);
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
                                (representation.getLab().getEmailAddresses() == null ?
                                        "" : String.join(", ", representation.getLab().getEmailAddresses())) +
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
                            (u.getContactData() == null ?
                                    "" : u.getContactData().getEmail()) +
                            "'.");
                    // FIXME: return error messages.
                }
            }
        }
    }

    @Scheduled(fixedRate=24*60*60*1000)
    private void sendReturnEmails(){
        Date now = new Date();
        List<LabRequest> labRequests = labRequestRepository.findAllUnsentByReturnDate(now);
        log.info("Sending reminder emails, labRequests found: " + labRequests.size());

        for(LabRequest request: labRequests){
            LabRequestRepresentation representation = new LabRequestRepresentation(request);
            labRequestQueryService.transferLabRequestData(representation, false);

            if(request.getStatus() != Status.COMPLETED){
                Collection<String> emails = request.getLab().getEmailAddresses();
                mailService.sendReturnDateEmails(emails,
                                                 representation.getLabRequestCode(),
                                                 representation.getRequester().getUsername(),
                                                 representation.getRequesterName(),
                                                 representation.getRequesterEmail());
                // Store the fact that we sent an email to these people.
                request.setSentReturnEmail(Boolean.TRUE);
                this.save(request);
            }
        }
    }

    public LabRequestRepresentation reject(Long id, LabRequestRepresentation body) {
        LabRequest labRequest = labRequestQueryService.findOne(id);

        if (labRequest.getStatus() != Status.WAITING_FOR_LAB_APPROVAL) {
            log.error("Action not allowed in status '" + labRequest.getStatus() + "'");
            throw new InvalidActionInStatus("Action not allowed in status '" + labRequest.getStatus() + "'");
        }

        labRequest.setRejectReason(body.getRejectReason());
        labRequest.setRejectDate(new Date());

        labRequest = labRequestStatusService.updateStatus(labRequest, Status.REJECTED);

        LabRequestRepresentation representation = new LabRequestRepresentation(
                labRequest);
        labRequestQueryService.transferLabRequestData(representation, false);
        return representation;
    }

    public LabRequestRepresentation undoReject(Long id, UserAuthenticationToken user) {
        LabRequest labRequest = labRequestQueryService.findOne(id);

        if (labRequest.getStatus() != Status.REJECTED) {
            log.error("Action not allowed in status '" + labRequest.getStatus() + "'");
            throw new InvalidActionInStatus("Action not allowed in status '" + labRequest.getStatus() + "'");
        }

        labRequest = labRequestStatusService.updateStatus(labRequest, Status.WAITING_FOR_LAB_APPROVAL);

        //Add comment explaining what happened.
        CommentRepresentation comment = new CommentRepresentation();
        comment.setContents("Undid rejection previously rejected lab request");
        commentService.addLabRequestComment(user.getUser(), id, comment);

        LabRequestRepresentation representation = new LabRequestRepresentation(
                labRequest);
        labRequestQueryService.transferLabRequestData(representation, false);
        return representation;
    }

    public LabRequestRepresentation approve(Long id) {
        LabRequest labRequest = labRequestQueryService.findOne(id);
        if (labRequest.getStatus() != Status.WAITING_FOR_LAB_APPROVAL) {
            log.error("Action not allowed in status '" + labRequest.getStatus() + "'");
            throw new InvalidActionInStatus("Action not allowed in status '" + labRequest.getStatus() + "'");
        }
        labRequest = labRequestStatusService.updateStatus(labRequest, Status.APPROVED);

        LabRequestRepresentation representation = new LabRequestRepresentation(
                labRequest);
        labRequestQueryService.transferLabRequestData(representation, false);
        return representation;
    }

    public LabRequestRepresentation undoApprove(Long id, UserAuthenticationToken user) {
        LabRequest labRequest = labRequestQueryService.findOne(id);
        Status status = labRequest.getStatus();
        if (!(status == Status.APPROVED || status == Status.SENDING)) {
            log.error("Action not allowed in status '" + labRequest.getStatus() + "'");
            throw new InvalidActionInStatus("Action not allowed in status '" + labRequest.getStatus() + "'");
        }
        // Reset Values
        labRequest = labRequestStatusService.updateStatus(labRequest, Status.WAITING_FOR_LAB_APPROVAL);
        labRequest.setPaReportsSent(Boolean.FALSE);
        labRequest.setClinicalDataSent(Boolean.FALSE);
        labRequest.setReturnDate(null);
        this.save(labRequest);

        //Add comment explaining what happened.
        CommentRepresentation comment = new CommentRepresentation();
        comment.setContents("Undid approval previously approved lab request");
        commentService.addLabRequestComment(user.getUser(), id, comment);

        labRequest = labRequestQueryService.findOne(id);
        LabRequestRepresentation representation = new LabRequestRepresentation(labRequest);
        labRequestQueryService.transferLabRequestData(representation, false);
        return representation;
    }

    private static Set<Status> paReportSendingStatuses = new HashSet<>(Arrays.asList(
            Status.APPROVED,
            Status.SENDING,
            Status.RECEIVED,
            Status.RETURNING));

    private static Set<Status> setHubAssistanceStatuses = new HashSet<>(Arrays.asList(
            Status.WAITING_FOR_LAB_APPROVAL,
            Status.APPROVED,
            Status.REJECTED,
            Status.SENDING,
            Status.RECEIVED,
            Status.RETURNING));

    LabRequest transferLabRequestFormData(LabRequestRepresentation body, LabRequest labRequest, User user) {
        RequestRepresentation request = new RequestRepresentation();
        HistoricProcessInstance instance = requestService.getProcessInstance(labRequest.getProcessInstanceId());
        requestFormService.transferData(instance, request, user);
        if (paReportSendingStatuses.contains(labRequest.getStatus())) {
            if (request.isPaReportRequest()) {
                labRequest.setPaReportsSent(body.isPaReportsSent());
                log.debug("Updating PA reports sent: " + labRequest.isPaReportsSent());
            }
            if (request.isClinicalDataRequest()) {
                labRequest.setClinicalDataSent(body.isClinicalDataSent());
                log.debug("Updating PA clinical data sent: " + labRequest.IsClinicalDataSent());
            }
            labRequest = save(labRequest);
        }
        if (setHubAssistanceStatuses.contains(labRequest.getStatus())) {
            labRequest.setHubAssistanceRequested(Boolean.TRUE.equals(body.isHubAssistanceRequested()));
            log.debug("Updating hub assistance: " + labRequest.isHubAssistanceRequested());
            labRequest = save(labRequest);
        }
        return labRequest;
    }

    @CacheEvict(value = {"labrequestdata", "detailedlabrequestdata"}, key = "#id")
    public LabRequestRepresentation update(Long id, LabRequestRepresentation body, UserAuthenticationToken user) {
        LabRequest labRequest = labRequestQueryService.findOne(id);

        labRequest = transferLabRequestFormData(body, labRequest, user.getUser());

        LabRequestRepresentation representation = new LabRequestRepresentation(labRequest);
        labRequestQueryService.transferLabRequestData(representation, false);
        return representation;
    }

}
