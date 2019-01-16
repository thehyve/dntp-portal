/*
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business.services;

import business.exceptions.AttachmentNotFound;
import business.models.File;
import business.models.RequestProperties;
import business.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RequestFileService {

    @Autowired
    private RequestPropertiesService requestPropertiesService;

    @Autowired
    private FileService fileService;


    public void addRequestAttachment(String processInstanceId, File attachment) {
        RequestProperties properties = requestPropertiesService.findByProcessInstanceId(processInstanceId);
        properties.getRequestAttachments().add(attachment);
        requestPropertiesService.save(properties);
    }

    public void removeRequestAttachment(String processInstanceId, Long attachmentId) {
        RequestProperties properties = requestPropertiesService.findByProcessInstanceId(processInstanceId);
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
        requestPropertiesService.save(properties);
        fileService.removeAttachment(toBeRemoved);
    }

    public void addInformedConsentFormAttachment(String processInstanceId, File attachment) {
        RequestProperties properties = requestPropertiesService.findByProcessInstanceId(processInstanceId);
        properties.getInformedConsentFormAttachments().add(attachment);
        requestPropertiesService.save(properties);
    }

    public void removeInformedConsentFormAttachment(String processInstanceId, Long attachmentId) {
        RequestProperties properties = requestPropertiesService.findByProcessInstanceId(processInstanceId);
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
        requestPropertiesService.save(properties);
        fileService.removeAttachment(toBeRemoved);
    }

    public void addAgreementAttachment(String processInstanceId, File attachment) {
        RequestProperties properties = requestPropertiesService.findByProcessInstanceId(processInstanceId);
        properties.getAgreementAttachments().add(attachment);
        requestPropertiesService.save(properties);
    }

    public void removeAgreementAttachment(String processInstanceId, Long attachmentId) {
        RequestProperties properties = requestPropertiesService.findByProcessInstanceId(processInstanceId);
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
        requestPropertiesService.save(properties);
        fileService.removeAttachment(toBeRemoved);
    }

    @Transactional(readOnly = true)
    public HttpEntity<InputStreamResource> getFile(User user, String id, Long attachmentId) {
        RequestProperties properties = requestPropertiesService.findByProcessInstanceId(id);
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
        for (File file: properties.getInformedConsentFormAttachments()) {
            if (file.getId().equals(attachmentId)) {
                return fileService.download(file.getId());
            }
        }
        throw new AttachmentNotFound();
    }

}
