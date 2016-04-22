/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.List;

import javax.mail.internet.MimeMessage;

import org.activiti.engine.TaskService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import business.controllers.RequestController;
import business.controllers.SelectionController;
import business.models.PathologyItemRepository;
import business.models.User;
import business.representation.RequestRepresentation;
import business.security.MockConfiguration.MockMailSender;
import business.security.UserAuthenticationToken;
import business.services.LabRequestService;
import business.services.UserService;

@Profile("dev")
@SpringApplicationConfiguration(classes = {Application.class})
@WebIntegrationTest("server.port = 8093")
public abstract class SelectionControllerTests extends AbstractTestNGSpringContextTests {

    Log log = LogFactory.getLog(this.getClass());
    
    FileSystem fileSystem = FileSystems.getDefault();
    
    @Autowired UserService userService;

    @Autowired SelectionController selectionController;

    @Autowired RequestController requestController;

    @Autowired TaskService taskService;

    @Autowired LabRequestService labRequestService;

    @Autowired PathologyItemRepository pathologyItemRepository;

    @Autowired
    JavaMailSender mailSender;

    @Autowired
    AuthenticationProvider authenticationProvider;

    protected String processInstanceId;

    protected UserAuthenticationToken getRequester() {
        User user = userService.findByUsername("requester@dntp.thehyve.nl");
        Authentication authentication = new UsernamePasswordAuthenticationToken(user, "requester");
        return (UserAuthenticationToken)authenticationProvider.authenticate(authentication);
    }

    protected UserAuthenticationToken getPalga() {
        User user = userService.findByUsername("palga@dntp.thehyve.nl");
        Authentication authentication = new UsernamePasswordAuthenticationToken(user, "palga"); // because of password tests
        return (UserAuthenticationToken)authenticationProvider.authenticate(authentication);
    }
    
    @BeforeClass
    public void setUp() throws Exception {
        ((MockMailSender)this.mailSender).clear();
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
        
        //testController.clearAll();
        //List<RequestListRepresentation> requestList = requestController.getRequestList(requester);
        //assertEquals(0, requestList.size());
        
        SecurityContextHolder.clearContext();
    }
    
    @Test(groups = "request", dependsOnMethods = "createRequest")
    public void submitRequest() {
        UserAuthenticationToken requester = getRequester();
        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(requester);
        
        RequestRepresentation representation = 
                requestController.getRequestById(requester, processInstanceId);
        log.info("Status: " + representation.getStatus());
        representation = requestController.submit(requester, processInstanceId, representation);
        log.info("Status: " + representation.getStatus());
        assertEquals("Review", representation.getStatus());
        
        SecurityContextHolder.clearContext();
    }
    

    @Test(groups = "request", dependsOnMethods = "submitRequest")
    public void submitRequestForApproval() {
        UserAuthenticationToken palga = getPalga();
        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(palga);
        
        RequestRepresentation representation = 
                requestController.getRequestById(palga, processInstanceId);
        log.info("Status: " + representation.getStatus());
        
        representation = requestController.claim(palga, processInstanceId, representation);
        
        ((MockMailSender)mailSender).clear();
        
        // only enforced in front end, not in back end
        representation.setBackground("Background is testing.");
        representation.setHypothesis("Tests will pass");
        representation.setMethods("JUnit");
        // request type
        representation.setMaterialsRequest(true);
        representation.setPaReportRequest(true);
        // required checks
        representation.setRequesterValid(true);
        representation.setRequesterAllowed(true);
        representation.setContactPersonAllowed(true);
        representation.setRequesterLabValid(true);
        representation.setAgreementReached(true);
        
        representation = requestController.submitForApproval(palga, processInstanceId, representation);
        log.info("Status: " + representation.getStatus());
        assertEquals("Approval", representation.getStatus());
        
        assertEquals(mailSender.getClass(), MockMailSender.class);
        List<MimeMessage> emails = ((MockMailSender)mailSender).getMessages();
        assertEquals(1, emails.size());
        
        SecurityContextHolder.clearContext();
    }

    @Test(groups = "request", dependsOnMethods = "submitRequestForApproval")
    public void approveRequest() {
        UserAuthenticationToken palga = getPalga();
        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(palga);

        RequestRepresentation representation =
                requestController.getRequestById(palga, processInstanceId);
        log.info("Status: " + representation.getStatus());

        representation = requestController.claim(palga, processInstanceId, representation);

        representation.setPrivacyCommitteeRationale("ppc_approved_written_procedure");
        representation.setScientificCouncilApproved(true);
        representation.setPrivacyCommitteeApproved(true);

        representation = requestController.finalise(palga, processInstanceId, representation);
        log.info("Status: " + representation.getStatus());
        assertEquals("DataDelivery", representation.getStatus());

        SecurityContextHolder.clearContext();
    }

    public abstract void uploadExcerptList() throws IOException;

    public abstract void selectExcerpts();

}
