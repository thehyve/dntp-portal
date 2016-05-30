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
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;

import business.exceptions.AttachmentNotFound;
import business.models.File;
import business.models.RequestProperties;
import business.models.RequestProperties.ReviewStatus;
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

    @Cacheable("requestnumber")
    public String getRequestNumber(String processInstanceId) {
        return requestPropertiesRepository.getRequestNumberByProcessInstanceId(processInstanceId);
    }

    @CachePut(value = "requestnumber", key = "#properties.processInstanceId")
    @Transactional
    public String getNewRequestNumber(RequestProperties properties) {
        if (properties.getRequestNumber() == null || properties.getRequestNumber().isEmpty()) {
            // The request is a new request and needs to have a new number assigned.
            String requestNumber = requestNumberService.getNewRequestNumber();
            properties.setRequestNumber(requestNumber);
            properties =  save(properties);
        }
        return properties.getRequestNumber();
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
