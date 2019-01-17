/*
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import business.controllers.RequestFileController;
import business.models.File.AttachmentType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.multipart.MultipartFile;

import business.controllers.RequestController;
import business.representation.FileRepresentation;
import business.representation.RequestRepresentation;
import business.security.UserAuthenticationToken;


@Profile("dev")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@ContextConfiguration
public class RequestFileControllerTests extends AbstractSelectionControllerTests {

    private final Logger log = LoggerFactory.getLogger(RequestFileControllerTests.class);

    @Autowired
    private RequestController requestController;

    @Autowired
    private RequestFileController requestFileController;

    @Before
    public void setup() {
        log.info("Clearing database before test ...");
        testService.clearDatabase();
    }

    private void printFile(FileRepresentation f) {
        log.info(String.format("id: %d, mimetype: [%s], type: [%s], name: %s",
                f.getId(), f.getMimeType(), f.getType(), f.getName()));
    }

    private void printFiles(List<FileRepresentation> files) {
        files.forEach(this::printFile);
    }

    private RequestRepresentation uploadTestFile(UserAuthenticationToken user, AttachmentType type, String contentType) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource("test/Utrecht_Oude_Gracht_Hamburgerbrug_(LOC).jpg");
        InputStream input = resource.openStream();
        MultipartFile file;
        if (contentType == null) {
            file = new MockMultipartFile(resource.getFile(), input);
        } else {
            file = new MockMultipartFile(resource.getFile(), resource.getFile(), contentType, input);
        }
        Integer flowTotalChunks = 1;
        Integer flowChunkNumber = 1;
        String flowIdentifier = "flow";

        switch(type) {
            case REQUEST:
                return requestFileController.uploadRequestAttachment(
                        user,
                        processInstanceId,
                        resource.getFile(),
                        flowTotalChunks,
                        flowChunkNumber,
                        flowIdentifier,
                        file);
            case DATA:
                return requestFileController.uploadDataAttachment(
                        user,
                        processInstanceId,
                        resource.getFile(),
                        flowTotalChunks,
                        flowChunkNumber,
                        flowIdentifier,
                        file);
            case MEDICAL_ETHICAL_COMMITEE_APPROVAL:
                return requestFileController.uploadMECAttachment(
                        user,
                        processInstanceId,
                        resource.getFile(),
                        flowTotalChunks,
                        flowChunkNumber,
                        flowIdentifier,
                        file);
            case AGREEMENT:
                return requestFileController.uploadAgreementAttachment(
                        user,
                        processInstanceId,
                        resource.getFile(),
                        flowTotalChunks,
                        flowChunkNumber,
                        flowIdentifier,
                        file);
            case INFORMED_CONSENT_FORM:
                return requestFileController.uploadInformedConsentFormAttachment(
                        user,
                        processInstanceId,
                        resource.getFile(),
                        flowTotalChunks,
                        flowChunkNumber,
                        flowIdentifier,
                        file);
            default:
                throw new RuntimeException("Upload type not supported: " + type.toString());
        }
    }

    private RequestRepresentation deleteFile(UserAuthenticationToken user, AttachmentType type, Long id) {
        switch(type) {
            case REQUEST:
                return requestFileController.deleteRequestAttachment(user, processInstanceId, id);
            case DATA:
                return requestFileController.removeDataAttachment(user, processInstanceId, id);
            case MEDICAL_ETHICAL_COMMITEE_APPROVAL:
                return requestFileController.removeMECAttachment(user, processInstanceId, id);
            case AGREEMENT:
                return requestFileController.removeAgreementAttachment(user, processInstanceId, id);
            case INFORMED_CONSENT_FORM:
                return requestFileController.removeInformedConsentFormAttachment(user, processInstanceId, id);
            default:
                throw new RuntimeException("Upload type not supported: " + type.toString());
        }
    }

    private RequestRepresentation uploadTestFile(UserAuthenticationToken requester, AttachmentType type) throws IOException {
        return uploadTestFile(requester, type, "image/jpeg");
    }

    @Test
    public void uploadFileNoMimetype() throws IOException {
        createRequest();

        UserAuthenticationToken requester = getRequester();
        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(requester);

        RequestRepresentation representation = uploadTestFile(requester, AttachmentType.REQUEST, null);

        assertEquals(1, representation.getAttachments().size());
        printFiles(representation.getAttachments());

        SecurityContextHolder.clearContext();
    }

    @Test
    public void uploadFileInvalidMimetype() throws IOException {
        createRequest();

        UserAuthenticationToken requester = getRequester();
        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(requester);

        RequestRepresentation representation = uploadTestFile(requester, AttachmentType.REQUEST, "undefined");

        assertEquals(1, representation.getAttachments().size());
        printFiles(representation.getAttachments());

        SecurityContextHolder.clearContext();
    }

    @Test
    public void uploadFileSuccess() throws IOException {
        createRequest();

        UserAuthenticationToken requester = getRequester();
        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(requester);

        RequestRepresentation representation = uploadTestFile(requester, AttachmentType.REQUEST);

        assertEquals(1, representation.getAttachments().size());
        printFiles(representation.getAttachments());

        SecurityContextHolder.clearContext();
    }

    @Test
    public void uploadAgreement() throws IOException {
        createRequest();
        submitRequest();

        UserAuthenticationToken palga = getPalga();
        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(palga);
        RequestRepresentation representation =
                requestController.getRequestById(palga, processInstanceId);
        requestController.claim(palga, processInstanceId, representation);

        // Test upload of regular attachment and agreement file by the Palga user
        uploadTestFile(palga, AttachmentType.REQUEST);
        uploadTestFile(palga, AttachmentType.AGREEMENT);
        representation = uploadTestFile(palga, AttachmentType.AGREEMENT);

        assertEquals(1, representation.getAttachments().size());
        assertEquals(2, representation.getAgreementAttachments().size());

        // Test deletion of agreement file
        representation = deleteFile(palga,
                AttachmentType.AGREEMENT,
                representation.getAgreementAttachments().get(0).getId());

        assertEquals(1, representation.getAttachments().size());
        assertEquals(1, representation.getAgreementAttachments().size());

        SecurityContextHolder.clearContext();
    }

    @Test
    public void downloadFiles() throws IOException {
        createRequest();

        SecurityContext securityContext = SecurityContextHolder.getContext();
        UserAuthenticationToken requester = getRequester();
        securityContext.setAuthentication(requester);

        uploadTestFile(requester, AttachmentType.MEDICAL_ETHICAL_COMMITEE_APPROVAL);
        uploadTestFile(requester, AttachmentType.INFORMED_CONSENT_FORM);
        RequestRepresentation representation = uploadTestFile(requester, AttachmentType.REQUEST);

        assertEquals(1, representation.getAttachments().size());
        assertEquals(1, representation.getMedicalEthicalCommitteeApprovalAttachments().size());
        assertEquals(1, representation.getInformedConsentFormAttachments().size());
        log.info("Status: " + representation.getStatus());
        log.info("Downloading ...");
        representation.getAttachments().forEach(f -> {
            printFile(f);
            HttpEntity<InputStreamResource> response = requestFileController.getFile(
                    requester, processInstanceId, f.getId());
            log.info(String.format("Response: contenttype: [%s]",
                    response.getHeaders().getContentType() == null ?
                    null :
                    response.getHeaders().getContentType().toString()));
        });

        printFiles(representation.getAttachments());

        SecurityContextHolder.clearContext();
    }

    @Test
    public void uploadDataAttachment() throws IOException, InterruptedException {
        createRequest();

        SecurityContext securityContext = SecurityContextHolder.getContext();
        UserAuthenticationToken requester = getRequester();
        securityContext.setAuthentication(requester);
        uploadTestFile(requester, AttachmentType.REQUEST);

        submitRequest();
        submitRequestForApproval();
        approveRequest();

        // Test upload of data files
        securityContext = SecurityContextHolder.getContext();
        UserAuthenticationToken palga = getPalga();
        securityContext.setAuthentication(palga);

        RequestRepresentation representation = uploadTestFile(palga, AttachmentType.DATA);

        assertEquals(1, representation.getAttachments().size());
        assertEquals(1, representation.getDataAttachments().size());

        // Test deletion of data files
        representation = deleteFile(palga,
                AttachmentType.DATA,
                representation.getDataAttachments().get(0).getId());

        assertEquals(1, representation.getAttachments().size());
        assertEquals(0, representation.getDataAttachments().size());

        SecurityContextHolder.clearContext();
    }

    @Test
    public void deleteAttachments() throws IOException {
        createRequest();

        SecurityContext securityContext = SecurityContextHolder.getContext();
        UserAuthenticationToken requester = getRequester();
        securityContext.setAuthentication(requester);

        uploadTestFile(requester, AttachmentType.MEDICAL_ETHICAL_COMMITEE_APPROVAL);
        uploadTestFile(requester, AttachmentType.INFORMED_CONSENT_FORM);
        RequestRepresentation representation = uploadTestFile(requester, AttachmentType.REQUEST);

        assertEquals(1, representation.getAttachments().size());
        assertEquals(1, representation.getMedicalEthicalCommitteeApprovalAttachments().size());
        assertEquals(1, representation.getInformedConsentFormAttachments().size());

        representation = deleteFile(requester,
                AttachmentType.REQUEST,
                representation.getAttachments().get(0).getId());
        representation = deleteFile(requester,
                AttachmentType.INFORMED_CONSENT_FORM,
                representation.getInformedConsentFormAttachments().get(0).getId());
        representation = deleteFile(requester,
                AttachmentType.MEDICAL_ETHICAL_COMMITEE_APPROVAL,
                representation.getMedicalEthicalCommitteeApprovalAttachments().get(0).getId());

        assertEquals(0, representation.getAttachments().size());
        assertEquals(0, representation.getMedicalEthicalCommitteeApprovalAttachments().size());
        assertEquals(0, representation.getInformedConsentFormAttachments().size());
    }

}
