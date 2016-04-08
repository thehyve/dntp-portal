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
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.web.multipart.MultipartFile;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import business.controllers.RequestController;
import business.models.File;
import business.models.User;
import business.representation.FileRepresentation;
import business.representation.RequestRepresentation;
import business.security.UserAuthenticationToken;
import business.services.UserService;

@Profile("dev")
@SpringApplicationConfiguration(classes = {Application.class})
@WebIntegrationTest("server.port = 8093")
public class UploadTests extends AbstractTestNGSpringContextTests {

    Log log = LogFactory.getLog(this.getClass());

    FileSystem fileSystem = FileSystems.getDefault();

    @Autowired
    UserService userService;

    @Autowired
    RequestController requestController;

    @Autowired
    AuthenticationProvider authenticationProvider;

    protected String processInstanceId;

    protected UserAuthenticationToken getRequester() {
        User user = userService.findByUsername("requester@dntp.thehyve.nl");
        Authentication authentication = new UsernamePasswordAuthenticationToken(user, "requester");
        return (UserAuthenticationToken)authenticationProvider.authenticate(authentication);
    }

    @BeforeClass
    public void setUp() throws Exception {
        log.info("TEST  Test: " + this.getClass().toString());
    }

    @Test(groups = "request")
    public void createRequest() {
        UserAuthenticationToken requester = getRequester();
        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(requester);

        RequestRepresentation representation = new RequestRepresentation();
        representation.setTitle("Test request");
        representation = requestController.start(requester, representation);
        log.info("Started request " + representation.getProcessInstanceId());
        log.info("Status: " + representation.getStatus());
        log.info("Assignee: " + representation.getAssignee());
        assertEquals("Open", representation.getStatus());
        processInstanceId = representation.getProcessInstanceId();

        SecurityContextHolder.clearContext();
    }

    protected void printFile(FileRepresentation f) {
        log.info(String.format("id: %d, mimetype: [%s], type: [%s], name: %s",
                f.getId(), f.getMimeType(), f.getType(), f.getName()));
    }

    protected void printFiles(List<FileRepresentation> files) {
        files.forEach(f -> {
            printFile(f);
        });
    }

    @Test(groups = "upload", dependsOnMethods = "createRequest")
    public void uploadFileNoMimetype() throws IOException {
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

    @Test(groups = "upload", dependsOnMethods = "createRequest")
    public void uploadFileInvalidMimetype() throws IOException {
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

    @Test(groups = "upload", dependsOnMethods = "createRequest")
    public void uploadFileSuccess() throws IOException {
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

    @Test(groups = "download", dependsOnGroups = "upload")
    public void downloadFiles() throws IOException {
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
