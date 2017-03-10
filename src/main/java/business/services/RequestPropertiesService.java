/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business.services;

import java.util.Date;
import java.util.Set;

import javax.transaction.Transactional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;

import business.exceptions.AttachmentNotFound;
import business.models.File;
import business.models.RequestProperties;
import business.models.RequestProperties.ReviewStatus;
import business.representation.RequestListRepresentation;
import business.models.RequestPropertiesRepository;
import business.models.User;

@Service
public class RequestPropertiesService {

    Log log = LogFactory.getLog(getClass());

    @Autowired
    RequestPropertiesRepository requestPropertiesRepository;

    @Autowired
    RequestNumberService requestNumberService;

    @Autowired
    FileService fileService;


    @CacheEvict(value = "dataattachmentcount", key = "#properties.processInstanceId")
    @Transactional
    public RequestProperties save(RequestProperties properties) {
        return requestPropertiesRepository.save(properties);
    }

    @Transactional
    public RequestProperties findByProcessInstanceId(String processInstanceId) {
        Date start = new Date();
        RequestProperties properties = requestPropertiesRepository.findByProcessInstanceId(processInstanceId);
        if (properties == null) {
            properties = new RequestProperties(processInstanceId);
        }
        Date end = new Date();
        if ((end.getTime()-start.getTime()) > 10) {
            log.warn("RequestPropertiesService: query took " + (end.getTime() - start.getTime()) + " ms.");
        }
        return properties;
    }

    /**
     * Suspend the request if it is not suspended.
     *
     * This will not disable any functionality, but display it in a different
     * list in the user interface.
     *
     * @param processInstanceId the processInstanceId of the request.
     */
    @Transactional
    public void suspendRequest(String processInstanceId) {
        RequestProperties properties = findByProcessInstanceId(processInstanceId);
        if (properties.getReviewStatus() != ReviewStatus.SUSPENDED) {
            properties.setReviewStatus(ReviewStatus.SUSPENDED);
            save(properties);
        }
    }

    /**
     * Resume the request if it is suspended.
     *
     * @param processInstanceId the processInstanceId of the request.
     */
    @Transactional
    public void resumeRequest(String processInstanceId) {
        RequestProperties properties = findByProcessInstanceId(processInstanceId);
        if (properties.getReviewStatus() != ReviewStatus.ACTIVE) {
            properties.setReviewStatus(ReviewStatus.ACTIVE);
            save(properties);
        }
    }

    @Cacheable("dataattachmentcount")
    public Long getDataAttachmentCount(String processInstanceId) {
        return requestPropertiesRepository.countDataAttachmentsByProcessInstanceId(processInstanceId);
    }

    @Cacheable("requestnumber")
    public String getRequestNumber(String processInstanceId) {
        return requestPropertiesRepository.getRequestNumberByProcessInstanceId(processInstanceId);
    }

    @Cacheable("datesubmitted")
    public Date getDateSubmitted(String processInstanceId) {
        return requestPropertiesRepository.getDateSubmittedByProcessInstanceId(processInstanceId);
    }

    /**
     * Retrieves the request number of a request if it exists or generates a new one.
     * The request number cache is updated as a result of this action.
     * When a new request number is generated, also the <code>dateSubmitted</code> is set.
     *
     * @param properties the request properties object representing the request.
     * @return the request number.
     */
    @CachePut(value = "requestnumber", key = "#properties.processInstanceId")
    @CacheEvict(value = "datesubmitted", key = "#properties.processInstanceId")
    @Transactional
    public String getNewRequestNumber(RequestProperties properties) {
        if (properties.getRequestNumber() == null || properties.getRequestNumber().isEmpty()) {
            // The request is a new request and needs to have a new number assigned.
            String requestNumber = requestNumberService.getNewRequestNumber();
            properties.setRequestNumber(requestNumber);
            properties.setDateSubmitted(new Date());
            properties = save(properties);
        }
        return properties.getRequestNumber();
    }

    @Cacheable("parentlistrepresentation")
    public RequestListRepresentation getParentListRepresentation(String processInstanceId) {
        RequestProperties parentProperties = requestPropertiesRepository.getParentByProcessInstanceId(processInstanceId);
        if (parentProperties == null) {
            return null;
        }
        RequestListRepresentation parent = new RequestListRepresentation();
        parent.setRequestNumber(parentProperties.getRequestNumber());
        parent.setProcessInstanceId(parentProperties.getProcessInstanceId());
        return parent;
    }

    public ReviewStatus getRequestReviewStatus(String processInstanceId) {
        return requestPropertiesRepository.getRequestReviewStatusByProcessInstanceId(processInstanceId);
    }

    public Set<String> getProcessInstanceIdsByReviewStatus(ReviewStatus reviewStatus) {
        return requestPropertiesRepository.getProcessInstanceIdsByReviewStatus(reviewStatus);
    }

    @Transactional
    public void addRequestAttachment(String processInstanceId, File attachment) {
        RequestProperties properties = findByProcessInstanceId(processInstanceId);
        properties.getRequestAttachments().add(attachment);
        properties = save(properties);
    }

    @Transactional
    public void removeRequestAttachment(String processInstanceId, Long attachmentId) {
        RequestProperties properties = findByProcessInstanceId(processInstanceId);
        File toBeRemoved = null;
        for (File file: properties.getRequestAttachments()) {
            if (file.getId().equals(attachmentId)) {
                toBeRemoved = file;
                break;
            }
        }
        if (toBeRemoved == null) {
            throw new AttachmentNotFound();
        }
        properties.getRequestAttachments().remove(toBeRemoved);
        save(properties);
        fileService.removeAttachment(toBeRemoved);
    }

    @Transactional
    public void addInformedConsentFormAttachment(String processInstanceId, File attachment) {
        RequestProperties properties = findByProcessInstanceId(processInstanceId);
        properties.getInformedConsentFormAttachments().add(attachment);
        save(properties);
    }

    @Transactional
    public void removeInformedConsentFormAttachment(String processInstanceId, Long attachmentId) {
        RequestProperties properties = findByProcessInstanceId(processInstanceId);
        File toBeRemoved = null;
        for (File file: properties.getInformedConsentFormAttachments()) {
            if (file.getId().equals(attachmentId)) {
                toBeRemoved = file;
                break;
            }
        }
        if (toBeRemoved == null) {
            throw new AttachmentNotFound();
        }
        properties.getInformedConsentFormAttachments().remove(toBeRemoved);
        save(properties);
        fileService.removeAttachment(toBeRemoved);
    }

    @Transactional
    public void addAgreementAttachment(String processInstanceId, File attachment) {
        RequestProperties properties = findByProcessInstanceId(processInstanceId);
        properties.getAgreementAttachments().add(attachment);
        properties = save(properties);
    }

    @Transactional
    public void removeAgreementAttachment(String processInstanceId, Long attachmentId) {
        RequestProperties properties = findByProcessInstanceId(processInstanceId);
        File toBeRemoved = null;
        for (File file: properties.getAgreementAttachments()) {
            if (file.getId().equals(attachmentId)) {
                toBeRemoved = file;
                break;
            }
        }
        if (toBeRemoved == null) {
            throw new AttachmentNotFound();
        }
        properties.getAgreementAttachments().remove(toBeRemoved);
        save(properties);
        fileService.removeAttachment(toBeRemoved);
    }

    @Transactional
    public HttpEntity<InputStreamResource> getFile(User user, String id, Long attachmentId) {
        RequestProperties properties = findByProcessInstanceId(id);
        for (File file: properties.getRequestAttachments()) {
            if (file.getId().equals(attachmentId)) {
                return fileService.download(file.getId());
            }
        }
        for (File file: properties.getAgreementAttachments()) {
            if (file.getId().equals(attachmentId)) {
                return fileService.download(file.getId());
            }
        }
        if (!user.isScientificCouncilMember()) {
            for (File file: properties.getDataAttachments()) {
                if (file.getId().equals(attachmentId)) {
                    return fileService.download(file.getId());
                }
            }
        }
        for (File file: properties.getMedicalEthicalCommiteeApprovalAttachments()) {
            if (file.getId().equals(attachmentId)) {
                return fileService.download(file.getId());
            }
        }
        throw new AttachmentNotFound();
    }

}
