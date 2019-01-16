/*
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business.controllers;

import business.exceptions.ExcerptListNotFound;
import business.exceptions.FileUploadError;
import business.exceptions.InvalidActionInStatus;
import business.exceptions.UpdateNotAllowed;
import business.models.File;
import business.models.RequestProperties;
import business.representation.ExcerptListRepresentation;
import business.representation.RequestRepresentation;
import business.representation.RequestStatus;
import business.security.UserAuthenticationToken;
import business.services.*;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@RestController
public class RequestFileController {

    Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private RequestService requestService;

    @Autowired
    private RequestFormService requestFormService;

    @Autowired
    private RequestPropertiesService requestPropertiesService;

    @Autowired
    private RequestFileService requestFileService;

    @Autowired
    private ExcerptListService excerptListService;

    @Autowired
    private FileService fileService;

    /**
     * Check if there is an active task where modifying attachments is allowed, i.e.,
     * when the request is in status open, review or approval.
     *
     * @param user the user to check for.
     * @param requestId the id of the request to check for.
     */
    void checkAttachmentTaskExists(UserAuthenticationToken user, String requestId) {
        if (user.getUser().isRequester()) {
            requestService.getTaskByRequestId(requestId, "request_form");
        } else if (user.getUser().isPalga()) {
            Task task = requestService.findTaskByRequestId(requestId, "palga_request_review");
            if (task == null) {
                requestService.getTaskByRequestId(requestId, "request_approval");
            }
        }
    }

    @PreAuthorize("isAuthenticated() "
            + " and ("
            + "     hasPermission(#id, 'isRequester') "
            + "     or (hasRole('palga') and hasPermission(#id, 'requestAssignedToUser'))"
            + ")")
    @RequestMapping(value = "/requests/{id}/files", method = RequestMethod.POST)
    public RequestRepresentation uploadRequestAttachment(
            UserAuthenticationToken user,
            @PathVariable String id,
            @RequestParam("flowFilename") String name,
            @RequestParam("flowTotalChunks") Integer chunks,
            @RequestParam("flowChunkNumber") Integer chunk,
            @RequestParam("flowIdentifier") String flowIdentifier,
            @RequestParam("file") MultipartFile file) {
        log.info("POST /requests/" + id + "/files: chunk " + chunk + " / " + chunks);

        checkAttachmentTaskExists(user, id);

        File attachment = fileService.uploadPart(user.getUser(), name, File.AttachmentType.REQUEST, file, chunk, chunks, flowIdentifier);
        if (attachment != null) {
            requestFileService.addRequestAttachment(id, attachment);
        }

        HistoricProcessInstance instance = requestService.getProcessInstance(id);
        RequestRepresentation request = new RequestRepresentation();
        requestFormService.transferData(instance, request, user.getUser());
        return request;
    }

    @PreAuthorize("isAuthenticated() "
            + " and ("
            + "     hasPermission(#id, 'isRequester') "
            + "     or (hasRole('palga') and hasPermission(#id, 'requestAssignedToUser'))"
            + ")")
    @RequestMapping(value = "/requests/{id}/files/{attachmentId}", method = RequestMethod.DELETE)
    public RequestRepresentation deleteRequestAttachment(UserAuthenticationToken user, @PathVariable String id,
                                                         @PathVariable Long attachmentId) {
        log.info("DELETE /requests/" + id + "/files/" + attachmentId);

        checkAttachmentTaskExists(user, id);

        // remove existing request attachment.
        requestFileService.removeRequestAttachment(id, attachmentId);

        HistoricProcessInstance instance = requestService.getProcessInstance(id);
        RequestRepresentation request = new RequestRepresentation();
        requestFormService.transferData(instance, request, user.getUser());
        return request;
    }

    @PreAuthorize("isAuthenticated() "
            + " and ("
            + "     hasPermission(#id, 'isRequester') "
            + "     or (hasRole('palga') and hasPermission(#id, 'requestAssignedToUser'))"
            + ")")
    @RequestMapping(value = "/requests/{id}/informedConsentFormFiles", method = RequestMethod.POST)
    public RequestRepresentation uploadInformedConsentFormAttachment(
            UserAuthenticationToken user,
            @PathVariable String id,
            @RequestParam("flowFilename") String name,
            @RequestParam("flowTotalChunks") Integer chunks,
            @RequestParam("flowChunkNumber") Integer chunk,
            @RequestParam("flowIdentifier") String flowIdentifier,
            @RequestParam("file") MultipartFile file) {
        log.info("POST /requests/" + id + "/informedConsentFormFiles: chunk " + chunk + " / " + chunks);

        checkAttachmentTaskExists(user, id);

        File attachment = fileService.uploadPart(user.getUser(), name, File.AttachmentType.INFORMED_CONSENT_FORM, file, chunk, chunks, flowIdentifier);
        if (attachment != null) {
            // add attachment id to the set of ids of the informed consent form attachments.
            requestFileService.addInformedConsentFormAttachment(id, attachment);
        }

        HistoricProcessInstance instance = requestService.getProcessInstance(id);
        RequestRepresentation request = new RequestRepresentation();
        requestFormService.transferData(instance, request, user.getUser());
        return request;
    }

    @PreAuthorize("isAuthenticated() "
            + " and ("
            + "     hasPermission(#id, 'isRequester') "
            + "     or (hasRole('palga'))"
            + ")")

    @RequestMapping(value = "/requests/{id}/informedConsentFormFiles/{attachmentId}", method = RequestMethod.GET)
    public HttpEntity<InputStreamResource> getICFile(UserAuthenticationToken user, @PathVariable String id,
                                                     @PathVariable Long attachmentId) {
        log.info("GET /requests/" + id + "/informedConsentFormFiles/" + attachmentId);
        return requestFileService.getFile(user.getUser(), id, attachmentId);
    }


    @RequestMapping(value = "/requests/{id}/informedConsentFormFiles/{attachmentId}", method = RequestMethod.DELETE)
    public RequestRepresentation removeInformedConsentFormAttachment(UserAuthenticationToken user, @PathVariable String id,
                                                                     @PathVariable Long attachmentId) {
        log.info("DELETE /requests/" + id + "/informedConsentFormFiles/" + attachmentId);

        checkAttachmentTaskExists(user, id);

        // Check access
        requestService.getProcessInstance(id);

        // remove existing agreement attachment.
        requestFileService.removeInformedConsentFormAttachment(id, attachmentId);

        HistoricProcessInstance instance = requestService.getProcessInstance(id);
        RequestRepresentation request = new RequestRepresentation();
        requestFormService.transferData(instance, request, user.getUser());
        return request;
    }

    @PreAuthorize("isAuthenticated() and hasRole('palga') and hasPermission(#id, 'requestAssignedToUser')")
    @RequestMapping(value = "/requests/{id}/agreementFiles", method = RequestMethod.POST)
    public RequestRepresentation uploadAgreementAttachment(
            UserAuthenticationToken user,
            @PathVariable String id,
            @RequestParam("flowFilename") String name,
            @RequestParam("flowTotalChunks") Integer chunks,
            @RequestParam("flowChunkNumber") Integer chunk,
            @RequestParam("flowIdentifier") String flowIdentifier,
            @RequestParam("file") MultipartFile file) {
        log.info("POST /requests/" + id + "/agreementFiles: chunk " + chunk + " / " + chunks);
        // Check access
        requestService.getTaskByRequestId(id, "palga_request_review");

        File attachment = fileService.uploadPart(user.getUser(), name, File.AttachmentType.AGREEMENT, file, chunk, chunks, flowIdentifier);
        if (attachment != null) {
            // add attachment id to the set of ids of the agreement attachments.
            requestFileService.addAgreementAttachment(id, attachment);
        }

        HistoricProcessInstance instance = requestService.getProcessInstance(id);
        RequestRepresentation request = new RequestRepresentation();
        requestFormService.transferData(instance, request, user.getUser());
        return request;
    }

    @PreAuthorize("isAuthenticated() and hasRole('palga') and hasPermission(#id, 'requestAssignedToUser')")
    @RequestMapping(value = "/requests/{id}/agreementFiles/{attachmentId}", method = RequestMethod.DELETE)
    public RequestRepresentation removeAgreementAttachment(UserAuthenticationToken user, @PathVariable String id,
                                                           @PathVariable Long attachmentId) {
        log.info("DELETE /requests/" + id + "/agreementFiles/" + attachmentId);
        // Check access
        requestService.getTaskByRequestId(id, "palga_request_review");
        requestService.getProcessInstance(id);

        // remove existing agreement attachment.
        requestFileService.removeAgreementAttachment(id, attachmentId);

        HistoricProcessInstance instance = requestService.getProcessInstance(id);
        RequestRepresentation request = new RequestRepresentation();
        requestFormService.transferData(instance, request, user.getUser());
        return request;
    }

    // FIXME: refactor
    @PreAuthorize("isAuthenticated() "
            + " and ("
            + "     hasPermission(#id, 'isRequester') "
            + "     or (hasRole('palga') and hasPermission(#id, 'requestAssignedToUser'))"
            + ")")
    @RequestMapping(value = "/requests/{id}/mecFiles", method = RequestMethod.POST)
    public RequestRepresentation uploadMECAttachment(
            UserAuthenticationToken user,
            @PathVariable String id,
            @RequestParam("flowFilename") String name,
            @RequestParam("flowTotalChunks") Integer chunks,
            @RequestParam("flowChunkNumber") Integer chunk,
            @RequestParam("flowIdentifier") String flowIdentifier,
            @RequestParam("file") MultipartFile file) {
        log.info("POST /requests/" + id + "/mecFiles: chunk " + chunk + " / " + chunks);

        if (user.getUser().isRequester()) {
            Task task = requestService.getTaskByRequestId(id, "request_form");
            if (task == null) {
                throw new UpdateNotAllowed();
            }
        }

        File attachment = fileService.uploadPart(user.getUser(), name, File.AttachmentType.MEDICAL_ETHICAL_COMMITEE_APPROVAL, file, chunk, chunks, flowIdentifier);
        if (attachment != null) {
            // add attachment id to the set of ids of the agreement attachments.
            RequestProperties properties = requestPropertiesService.findByProcessInstanceId(id);
            properties.getMedicalEthicalCommiteeApprovalAttachments().add(attachment);
            requestPropertiesService.save(properties);
        }

        HistoricProcessInstance instance = requestService.getProcessInstance(id);
        RequestRepresentation request = new RequestRepresentation();
        requestFormService.transferData(instance, request, user.getUser());
        return request;
    }

    // FIXME: refactor
    @PreAuthorize("isAuthenticated() "
            + " and ("
            + "     hasPermission(#id, 'isRequester') "
            + "     or (hasRole('palga') and hasPermission(#id, 'requestAssignedToUser'))"
            + ")")
    @RequestMapping(value = "/requests/{id}/mecFiles/{attachmentId}", method = RequestMethod.DELETE)
    public RequestRepresentation removeMECAttachment(UserAuthenticationToken user, @PathVariable String id,
                                                     @PathVariable Long attachmentId) {
        log.info("DELETE /requests/" + id + "/mecFiles/" + attachmentId);

        if (user.getUser().isRequester()) {
            Task task = requestService.getTaskByRequestId(id, "request_form");
            if (task == null) {
                throw new UpdateNotAllowed();
            }
        }

        // remove existing agreement attachment.
        RequestProperties properties = requestPropertiesService.findByProcessInstanceId(id);
        File toBeRemoved = null;
        for (File file: properties.getMedicalEthicalCommiteeApprovalAttachments()) {
            if (file.getId().equals(attachmentId)) {
                toBeRemoved = file;
                break;
            }
        }
        if (toBeRemoved != null) {
            properties.getMedicalEthicalCommiteeApprovalAttachments().remove(toBeRemoved);
            requestPropertiesService.save(properties);
            fileService.removeAttachment(toBeRemoved);
        }

        HistoricProcessInstance instance = requestService.getProcessInstance(id);
        RequestRepresentation request = new RequestRepresentation();
        requestFormService.transferData(instance, request, user.getUser());
        return request;
    }

    // FIXME: refactor
    @PreAuthorize("isAuthenticated() and hasRole('palga') and hasPermission(#id, 'requestAssignedToUser')")
    @RequestMapping(value = "/requests/{id}/dataFiles", method = RequestMethod.POST)
    public RequestRepresentation uploadDataAttachment(
            UserAuthenticationToken user,
            @PathVariable String id,
            @RequestParam("flowFilename") String name,
            @RequestParam("flowTotalChunks") Integer chunks,
            @RequestParam("flowChunkNumber") Integer chunk,
            @RequestParam("flowIdentifier") String flowIdentifier,
            @RequestParam("file") MultipartFile file) {
        log.info("POST /requests/" + id + "/dataFiles: chunk " + chunk + " / " + chunks);

        // Check access
        requestService.getTaskByRequestId(id, "data_delivery");

        File attachment = fileService.uploadPart(user.getUser(), name, File.AttachmentType.DATA, file, chunk, chunks, flowIdentifier);
        if (attachment != null) {
            // add attachment id to the set of ids of the agreement attachments.
            RequestProperties properties = requestPropertiesService.findByProcessInstanceId(id);
            properties.getDataAttachments().add(attachment);
            requestPropertiesService.save(properties);
        }

        requestFormService.invalidateCacheEntry(id);
        HistoricProcessInstance instance = requestService.getProcessInstance(id);
        RequestRepresentation request = new RequestRepresentation();
        requestFormService.transferData(instance, request, user.getUser());
        return request;
    }

    // FIXME: refactor
    @PreAuthorize("isAuthenticated() and hasRole('palga') and hasPermission(#id, 'requestAssignedToUser')")
    @RequestMapping(value = "/requests/{id}/dataFiles/{attachmentId}", method = RequestMethod.DELETE)
    public RequestRepresentation removeDataAttachment(UserAuthenticationToken user, @PathVariable String id,
                                                      @PathVariable Long attachmentId) {
        log.info("DELETE /requests/" + id + "/dataFiles/" + attachmentId);

        // Check access
        requestService.getProcessInstance(id);

        // remove existing data attachment.
        RequestProperties properties = requestPropertiesService.findByProcessInstanceId(id);
        File toBeRemoved = null;
        for (File file: properties.getDataAttachments()) {
            if (file.getId().equals(attachmentId)) {
                toBeRemoved = file;
                break;
            }
        }
        if (toBeRemoved != null) {
            properties.getDataAttachments().remove(toBeRemoved);
            requestPropertiesService.save(properties);
            fileService.removeAttachment(toBeRemoved);
        }

        requestFormService.invalidateCacheEntry(id);
        HistoricProcessInstance instance = requestService.getProcessInstance(id);
        RequestRepresentation request = new RequestRepresentation();
        requestFormService.transferData(instance, request, user.getUser());
        return request;
    }

    @Profile("dev")
    @PreAuthorize("isAuthenticated() and hasRole('palga') and hasPermission(#id, 'requestAssignedToUser')")
    @RequestMapping(value = "/requests/{id}/excerptList/useExample", method = RequestMethod.POST)
    public Integer useExampleExcerptList (
            UserAuthenticationToken user,
            @PathVariable String id
    ) {
        ClassLoader classLoader = getClass().getClassLoader();
        String filename = "Example excerptlist groot bestand.csv";
        URL resource = classLoader.getResource("test/" + filename);
        if (resource == null) {
            throw new FileUploadError("Empty test resource");
        }
        try {
            InputStream input = resource.openStream();
            MultipartFile file = new MockMultipartFile(resource.getFile(), input);

            Integer flowTotalChunks = 1;
            Integer flowChunkNumber = 1;
            String flowIdentifier = "flow_" + UUID.randomUUID().toString();

            return this.uploadExcerptList(
                    user,
                    id,
                    filename,
                    flowTotalChunks,
                    flowChunkNumber,
                    flowIdentifier,
                    file);
        } catch (IOException e) {
            log.error("Error while uploading example file: " + e.getMessage());
            throw new FileUploadError(e.getMessage());
        }
    }

    @PreAuthorize("isAuthenticated() and hasRole('palga') and hasPermission(#id, 'requestAssignedToUser')")
    @RequestMapping(value = "/requests/{id}/excerptList", method = RequestMethod.POST)
    public Integer uploadExcerptList(
            UserAuthenticationToken user,
            @PathVariable String id,
            @RequestParam("flowFilename") String name,
            @RequestParam("flowTotalChunks") Integer chunks,
            @RequestParam("flowChunkNumber") Integer chunk,
            @RequestParam("flowIdentifier") String flowIdentifier,
            @RequestParam("file") MultipartFile file) {
        log.info("POST /requests/" + id + "/excerptList: chunk " + chunk + " / " + chunks);

        Task task = requestService.getTaskByRequestId(id, "data_delivery");

        Integer excerptCount = 0;
        File attachment = fileService.uploadPart(user.getUser(), name, File.AttachmentType.EXCERPT_LIST, file, chunk, chunks, flowIdentifier);
        if (attachment != null) {
            Long excerptListId = excerptListService.replaceExcerptList(id, attachment);
            excerptCount = excerptListService.countEntriesByExcerptListId(excerptListId);
        }
        requestFormService.invalidateCacheEntry(id);

        return excerptCount;
    }

    @PreAuthorize("isAuthenticated() and (hasRole('palga') or hasPermission(#id, 'isRequester'))")
    @RequestMapping(value = "/requests/{id}/excerptList", method = RequestMethod.GET)
    public ExcerptListRepresentation getExcerptList(UserAuthenticationToken user, @PathVariable String id) {
        log.info("GET /requests/" + id + "/excerptList");
        // Check access
        requestService.getTaskByRequestId(id, "data_delivery");
        HistoricProcessInstance instance = requestService.getProcessInstance(id);
        RequestRepresentation request = new RequestRepresentation();
        requestFormService.transferData(instance, request, user.getUser());

        ExcerptListRepresentation excerptList = excerptListService.findRepresentationByProcessInstanceId(id);
        if (excerptList == null) {
            throw new ExcerptListNotFound();
        }
        log.info("entries: " + excerptList.getEntryCount());
        return excerptList;
    }

    private static final Set<RequestStatus> excerptListStatuses = new HashSet<>();
    {
        excerptListStatuses.add(RequestStatus.DATA_DELIVERY);
        excerptListStatuses.add(RequestStatus.SELECTION_REVIEW);
        excerptListStatuses.add(RequestStatus.LAB_REQUEST);
        excerptListStatuses.add(RequestStatus.CLOSED);
    }

    @PreAuthorize("isAuthenticated() and "
            + "(hasRole('palga') "
            + " or hasPermission(#id, 'isRequester') "
            + ")")
    @RequestMapping(value = "/requests/{id}/excerptList/csv", method = RequestMethod.GET)
    public HttpEntity<InputStreamResource> downloadExcerptList(UserAuthenticationToken user, @PathVariable String id) {
        log.info("GET /requests/" + id + "/excerptList/csv");
        HistoricProcessInstance instance = requestService.getProcessInstance(id);
        RequestRepresentation request = new RequestRepresentation();
        requestFormService.transferData(instance, request, user.getUser());
        if (!excerptListStatuses.contains(request.getStatus())) {
            throw new InvalidActionInStatus();
        }
        return excerptListService.writeExcerptList(id, false );
    }

    @PreAuthorize("isAuthenticated() and "
            + "(hasRole('palga') "
            + " or hasPermission(#id, 'isRequester') "
            + " or hasPermission(#id, 'isScientificCouncil') "
            + " or hasPermission(#id, 'isLabuser') "
            + " or hasPermission(#id, 'isHubuser') "
            + ")")
    @RequestMapping(value = "/requests/{id}/files/{attachmentId}", method = RequestMethod.GET)
    public HttpEntity<InputStreamResource> getFile(UserAuthenticationToken user, @PathVariable String id,
                                                   @PathVariable Long attachmentId) {
        log.info("GET /requests/" + id + "/files/" + attachmentId);
        return requestFileService.getFile(user.getUser(), id, attachmentId);
    }

}
