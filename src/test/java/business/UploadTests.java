/**
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

import business.services.TestService;
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
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.multipart.MultipartFile;

import business.controllers.RequestController;
import business.models.User;
import business.representation.FileRepresentation;
import business.representation.RequestRepresentation;
import business.representation.RequestStatus;
import business.security.UserAuthenticationToken;
import business.services.UserService;


@Profile("dev")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@ContextConfiguration
public class UploadTests {

    private final Logger log = LoggerFactory.getLogger(UploadTests.class);

    @Autowired
    private UserService userService;

    @Autowired
    private TestService testService;

    @Autowired
    private RequestController requestController;

    @Autowired
    private AuthenticationProvider authenticationProvider;

    private String processInstanceId;

    @Before
    public void setup() {
        log.info("Clearing database before test ...");
        testService.clearDatabase();
    }

    private UserAuthenticationToken getRequester() {
        User user = userService.findByUsername("test+requester@dntp.thehyve.nl");
        Authentication authentication = new UsernamePasswordAuthenticationToken(user, "requester");
        return (UserAuthenticationToken)authenticationProvider.authenticate(authentication);
    }

    private void createRequest() {
        UserAuthenticationToken requester = getRequester();
        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(requester);

        RequestRepresentation representation = requestController.start(requester);
        log.info("Started request " + representation.getProcessInstanceId());
        log.info("Status: " + representation.getStatus());
        log.info("Assignee: " + representation.getAssignee());
        assertEquals(RequestStatus.OPEN, representation.getStatus());
        processInstanceId = representation.getProcessInstanceId();

        SecurityContextHolder.clearContext();
    }

    private void printFile(FileRepresentation f) {
        log.info(String.format("id: %d, mimetype: [%s], type: [%s], name: %s",
                f.getId(), f.getMimeType(), f.getType(), f.getName()));
    }

    private void printFiles(List<FileRepresentation> files) {
        files.forEach(this::printFile);
    }

    public void uploadFileNoMimetype() throws IOException {
        createRequest();

        UserAuthenticationToken requester = getRequester();
        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(requester);

        RequestRepresentation representation =
                requestController.getRequestById(requester, processInstanceId);
        log.info("Status: " + representation.getStatus());
        int attachmentCount = representation.getAttachments().size();

        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource("test/Utrecht_Oude_Gracht_Hamburgerbrug_(LOC).jpg");
        InputStream input = resource.openStream();
        MultipartFile file = new MockMultipartFile(resource.getFile(), input);
        //MultipartFile file = new MockMultipartFile(resource.getFile(), resource.getFile().toString(), "undefined", input);

        Integer flowTotalChunks = 1;
        Integer flowChunkNumber = 1;
        String flowIdentifier = "flow";

        representation = requestController.uploadRequestAttachment(
                requester,
                processInstanceId,
                resource.getFile(),
                flowTotalChunks,
                flowChunkNumber,
                flowIdentifier,
                file);

        assertEquals(attachmentCount + 1, representation.getAttachments().size());
        printFiles(representation.getAttachments());

        SecurityContextHolder.clearContext();
    }

    public void uploadFileInvalidMimetype() throws IOException {
        createRequest();

        UserAuthenticationToken requester = getRequester();
        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(requester);

        RequestRepresentation representation =
                requestController.getRequestById(requester, processInstanceId);
        log.info("Status: " + representation.getStatus());
        int attachmentCount = representation.getAttachments().size();

        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource("test/Utrecht_Oude_Gracht_Hamburgerbrug_(LOC).jpg");
        InputStream input = resource.openStream();
        MultipartFile file = new MockMultipartFile(resource.getFile(), resource.getFile().toString(), "undefined", input);

        Integer flowTotalChunks = 1;
        Integer flowChunkNumber = 1;
        String flowIdentifier = "flow";

        representation = requestController.uploadRequestAttachment(
                requester,
                processInstanceId,
                resource.getFile(),
                flowTotalChunks,
                flowChunkNumber,
                flowIdentifier,
                file);

        assertEquals(attachmentCount + 1, representation.getAttachments().size());
        printFiles(representation.getAttachments());

        SecurityContextHolder.clearContext();
    }

    @Test
    public void uploadFileSuccess() throws IOException {
        createRequest();

        UserAuthenticationToken requester = getRequester();
        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(requester);

        RequestRepresentation representation =
                requestController.getRequestById(requester, processInstanceId);
        log.info("Status: " + representation.getStatus());
        int attachmentCount = representation.getAttachments().size();

        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource("test/Utrecht_Oude_Gracht_Hamburgerbrug_(LOC).jpg");
        InputStream input = resource.openStream();
        MultipartFile file = new MockMultipartFile(resource.getFile(), resource.getFile().toString(), "image/jpeg", input);

        Integer flowTotalChunks = 1;
        Integer flowChunkNumber = 1;
        String flowIdentifier = "flow";

        representation = requestController.uploadRequestAttachment(
                requester,
                processInstanceId,
                resource.getFile(),
                flowTotalChunks,
                flowChunkNumber,
                flowIdentifier,
                file);

        assertEquals(attachmentCount + 1, representation.getAttachments().size());
        printFiles(representation.getAttachments());

        SecurityContextHolder.clearContext();
    }

    @Test
    public void downloadFiles() throws IOException {
        createRequest();

        UserAuthenticationToken requester = getRequester();
        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(requester);

        RequestRepresentation representation =
                requestController.getRequestById(requester, processInstanceId);
        log.info("Status: " + representation.getStatus());
        log.info("Downloading...");
        representation.getAttachments().forEach(f -> {
            printFile(f);
            HttpEntity<InputStreamResource> response = requestController.getFile(
                    requester, representation.getProcessInstanceId(), f.getId());
            log.info(String.format("Response: contenttype: [%s]",
                    response.getHeaders().getContentType() == null ?
                    null :
                    response.getHeaders().getContentType().toString()));
        });

        printFiles(representation.getAttachments());

        SecurityContextHolder.clearContext();
    }

}
