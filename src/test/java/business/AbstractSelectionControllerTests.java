/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.mail.internet.MimeMessage;

import business.controllers.*;
import business.exceptions.RequestNotAdmissible;
import business.models.ContactData;
import business.models.LabRequest;
import business.representation.*;
import business.services.*;
import com.opencsv.*;
import com.opencsv.exceptions.CsvValidationException;
import org.activiti.engine.TaskService;
import org.activiti.engine.task.Task;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import business.models.PathologyItemRepository;
import business.models.User;
import business.security.MockConfiguration.MockMailSender;
import business.security.UserAuthenticationToken;
import org.springframework.web.multipart.MultipartFile;


public abstract class AbstractSelectionControllerTests {

    final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    UserService userService;

    @Autowired
    TestService testService;

    @Autowired
    SelectionController selectionController;

    @Autowired
    RequestController requestController;

    @Autowired
    RequestService requestService;

    @Autowired
    RequestFileController requestFileController;

    @Autowired
    RequestExportController requestExportController;

    @Autowired
    LabRequestExportController labRequestExportController;

    @Autowired
    TaskService taskService;

    @Autowired
    LabRequestService labRequestService;

    @Autowired
    LabRequestQueryService labRequestQueryService;

    @Autowired
    PathologyItemRepository pathologyItemRepository;

    @Autowired
    JavaMailSender mailSender;

    @Autowired
    AuthenticationProvider authenticationProvider;

    String processInstanceId;

    UserAuthenticationToken getRequester() {
        User user = userService.findByUsername("test+requester@dntp.thehyve.nl");
        Authentication authentication = new UsernamePasswordAuthenticationToken(user, "requester");
        return (UserAuthenticationToken)authenticationProvider.authenticate(authentication);
    }

    UserAuthenticationToken getPalga() {
        User user = userService.findByUsername("test+palga@dntp.thehyve.nl");
        Authentication authentication = new UsernamePasswordAuthenticationToken(user, "palga"); // because of password tests
        return (UserAuthenticationToken)authenticationProvider.authenticate(authentication);
    }

    @Before
    public void setup() {
        ((MockMailSender)this.mailSender).clear();
        log.info("TEST  Test: " + this.getClass().toString());
    }

    void createRequest() {
        UserAuthenticationToken requester = getRequester();
        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(requester);

        RequestRepresentation representation = requestController.start(requester);
        log.info("Started request " + representation.getProcessInstanceId());
        log.info("Status: " + representation.getStatus());
        log.info("Assignee: " + representation.getAssignee());
        assertEquals(RequestStatus.OPEN, representation.getStatus());
        processInstanceId = representation.getProcessInstanceId();

        setTestData(representation);
        requestController.update(requester, representation.getProcessInstanceId(), representation);

        SecurityContextHolder.clearContext();
    }

    void submitRequest() {
        UserAuthenticationToken requester = getRequester();
        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(requester);

        RequestRepresentation representation =
                requestController.getRequestById(requester, processInstanceId);
        log.info("Status: " + representation.getStatus());
        representation.setTitle("Test request");
        representation.setPathologistEmail("test+pathologist@dntp.thehyve.nl");
        representation = requestController.submit(requester, processInstanceId, representation);
        log.info("Status: " + representation.getStatus());
        assertEquals(RequestStatus.REVIEW, representation.getStatus());

        SecurityContextHolder.clearContext();
    }

    void submitRequestForApproval() throws InterruptedException {
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
        representation.setBlockMaterialsRequest(true);
        representation.setPaReportRequest(true);
        // required checks
        representation.setRequestType("National request (LZV)");
        representation.setRequesterValid(true);
        representation.setRequesterAllowed(true);
        representation.setContactPersonAllowed(true);
        representation.setRequesterLabValid(true);
        representation.setAgreementReached(true);

        representation = requestController.submitReview(palga, processInstanceId, representation);
        log.info("Status: " + representation.getStatus());
        assertEquals(RequestStatus.APPROVAL, representation.getStatus());

        // Mail sending is asynchronous. Sleep for 1 second.
        Thread.sleep(1000);

        assertEquals(mailSender.getClass(), MockMailSender.class);
        List<MimeMessage> emails = ((MockMailSender)mailSender).getMessages();
        assertEquals(1, emails.size());

        SecurityContextHolder.clearContext();
    }

    void submitRequestForApprovalWithNonApplicableAgreement() throws InterruptedException {
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
        representation.setOtherMaterialsRequest("Some test material");
        // required checks
        representation.setRequestType("National request (LZV)");
        representation.setRequesterValid(true);
        representation.setRequesterAllowed(true);
        representation.setContactPersonAllowed(true);
        representation.setRequesterLabValid(true);
        representation.setAgreementReached(false);
        representation.setAgreementNotApplicable(true);

        try {
            requestController.submitReview(palga, processInstanceId, representation);
        } catch (RequestNotAdmissible e) {
            log.info(e.getMessage());
        }

        SecurityContextHolder.clearContext();
    }

    static void setTestData(RequestRepresentation representation) {
        representation.setTitle("Test title");
        representation.setBackground("Test background.");
        representation.setResearchQuestion("Q");
        representation.setHypothesis("H");
        representation.setMethods("Trial and error");
        representation.setSearchCriteria("q");
        representation.setStudyPeriod("2017");
        representation.setLaboratoryTechniques("Pipeteren");
        representation.setPathologistName("P.A. Thologist");
        representation.setPathologistEmail("pathologist@local");
        ContactData billingAddress = new ContactData();
        billingAddress.setAddress1("Test street 123");
        billingAddress.setPostalCode("1234 AB");
        billingAddress.setCity("Nowhere");
        representation.setBillingAddress(billingAddress);
        representation.setChargeNumber("1234567");
        representation.setGrantProvider("Test foundation");
        representation.setContactPersonName("Principal Investigator");
        representation.setContactPersonEmail("pi@local");
    }

    void createRequestResearchQuestionMissing() {
        UserAuthenticationToken requester = getRequester();
        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(requester);

        RequestRepresentation representation = requestController.start(requester);
        log.info("Started request " + representation.getProcessInstanceId());
        log.info("Status: " + representation.getStatus());
        log.info("Assignee: " + representation.getAssignee());
        assertEquals(RequestStatus.OPEN, representation.getStatus());
        processInstanceId = representation.getProcessInstanceId();

        setTestData(representation);
        representation.setResearchQuestion(null); // Validation should reject
        requestController.update(requester, representation.getProcessInstanceId(), representation);

        SecurityContextHolder.clearContext();
    }

    void createRequestWithInformedConsent() {
        UserAuthenticationToken requester = getRequester();
        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(requester);

        RequestRepresentation representation = requestController.start(requester);
        log.info("Started request " + representation.getProcessInstanceId());
        log.info("Status: " + representation.getStatus());
        log.info("Assignee: " + representation.getAssignee());
        assertEquals(RequestStatus.OPEN, representation.getStatus());
        processInstanceId = representation.getProcessInstanceId();

        // Validation should fail as IC form upload is missing.
        setTestData(representation);
        representation.setLinkageWithPersonalData(true);
        representation.setInformedConsent(true);
        requestController.update(requester, representation.getProcessInstanceId(), representation);

        SecurityContextHolder.clearContext();
    }

    void approveRequest() {
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
        assertEquals(RequestStatus.DATA_DELIVERY, representation.getStatus());

        SecurityContextHolder.clearContext();
    }

    void uploadInformedConsentForm() throws IOException {
        UserAuthenticationToken requester = getRequester();
        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(requester);

        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource("test/Utrecht_Oude_Gracht_Hamburgerbrug_(LOC).jpg");
        InputStream input = resource.openStream();
        MultipartFile file = new MockMultipartFile(resource.getFile(), resource.getFile().toString(), "image/jpeg", input);

        Integer flowTotalChunks = 1;
        Integer flowChunkNumber = 1;
        String flowIdentifier = "flow";

        requestFileController.uploadInformedConsentFormAttachment(
                requester,
                processInstanceId,
                resource.getFile(),
                flowTotalChunks,
                flowChunkNumber,
                flowIdentifier,
                file);

        SecurityContextHolder.clearContext();
    }

    void skipApproval() throws InterruptedException {
        UserAuthenticationToken palga = getPalga();
        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(palga);

        RequestRepresentation representation =
                requestController.getRequestById(palga, processInstanceId);
        log.info("Status: " + representation.getStatus());

        representation = requestController.claim(palga, processInstanceId, representation);

        ((MockMailSender)mailSender).clear();

        // Set the variable to skip the approval status
        representation.setSkipStatusApproval(true);

        // only enforced in front end, not in back end
        representation.setBackground("Background is testing.");
        representation.setHypothesis("Tests will pass");
        representation.setMethods("JUnit");
        // request type
        representation.setHeSliceMaterialsRequest(true);
        // required checks
        representation.setRequestType("National request (LZV)");
        representation.setRequesterValid(true);
        representation.setRequesterAllowed(true);
        representation.setContactPersonAllowed(true);
        representation.setRequesterLabValid(true);
        representation.setAgreementReached(true);

        representation = requestController.submitReview(palga, processInstanceId, representation);
        log.info("Status: " + representation.getStatus());
        assertEquals(RequestStatus.DATA_DELIVERY, representation.getStatus());

        // Mail sending is asynchronous. Sleep for 1 second.
        Thread.sleep(1 * 1000);

        assertEquals(mailSender.getClass(), MockMailSender.class);
        List<MimeMessage> emails = ((MockMailSender)mailSender).getMessages();
        assertEquals(0, emails.size());

        SecurityContextHolder.clearContext();
    }

    void skipApprovalWithNonApplicableAgreement() throws InterruptedException {
        UserAuthenticationToken palga = getPalga();
        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(palga);

        RequestRepresentation representation =
                requestController.getRequestById(palga, processInstanceId);
        log.info("Status: " + representation.getStatus());

        representation = requestController.claim(palga, processInstanceId, representation);

        ((MockMailSender)mailSender).clear();

        // Set the variable to skip the approval status
        representation.setSkipStatusApproval(true);

        // only enforced in front end, not in back end
        representation.setBackground("Background is testing.");
        representation.setHypothesis("Tests will pass");
        representation.setMethods("JUnit");
        // request type
        representation.setBlockMaterialsRequest(true);
        representation.setHeSliceMaterialsRequest(true);
        representation.setPaReportRequest(true);
        // required checks
        representation.setRequestType("National request (LZV)");
        representation.setRequesterValid(true);
        representation.setRequesterAllowed(true);
        representation.setContactPersonAllowed(true);
        representation.setRequesterLabValid(true);
        representation.setAgreementReached(false);
        representation.setAgreementNotApplicable(true);

        representation = requestController.submitReview(palga, processInstanceId, representation);
        log.info("Status: " + representation.getStatus());
        assertEquals(RequestStatus.DATA_DELIVERY, representation.getStatus());

        // Mail sending is asynchronous. Sleep for 1 second.
        Thread.sleep(1 * 1000);

        assertEquals(mailSender.getClass(), MockMailSender.class);
        List<MimeMessage> emails = ((MockMailSender)mailSender).getMessages();
        assertEquals(0, emails.size());

        SecurityContextHolder.clearContext();
    }

    long uploadExcerptList(String filename) throws IOException {
        UserAuthenticationToken palga = getPalga();
        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(palga);

        RequestRepresentation representation =
                requestController.getRequestById(palga, processInstanceId);
        log.info("Status: " + representation.getStatus());

        log.info("Activity: " + representation.getActivityId());
        if (representation.getActivityId() == null) {
            for (Task task: taskService.createTaskQuery().list()) {
                log.info("Task " + task.getId() + ", process instance: " + task.getProcessInstanceId()
                        + ", name: " + task.getName() + ", key: " + task.getTaskDefinitionKey());
            }
        }

        log.info("uploadExcerptList: processInstanceId = " + processInstanceId);

        requestController.claim(palga, processInstanceId, representation);

        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource("test/" + filename);
        InputStream input = resource.openStream();
        MultipartFile file = new MockMultipartFile(resource.getFile(), input);

        Integer flowTotalChunks = 1;
        Integer flowChunkNumber = 1;
        String flowIdentifier = "flow";

        int entryCount = requestFileController.uploadExcerptList(
                palga,
                processInstanceId,
                resource.getFile(),
                flowTotalChunks,
                flowChunkNumber,
                flowIdentifier,
                file);

        SecurityContextHolder.clearContext();
        return entryCount;
    }

    void selectExcerpts() {
        UserAuthenticationToken requester = getRequester();
        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(requester);

        RequestRepresentation representation =
                requestController.getRequestById(requester, processInstanceId);

        log.info("Status: " + representation.getStatus());

        ExcerptListRepresentation excerptList =
                requestFileController.getExcerptList(requester, processInstanceId);
        for(ExcerptEntryRepresentation entry: excerptList.getEntries()) {
            entry.setSelected(true);
        }
        excerptList = selectionController.updateExcerptListSelection(requester, processInstanceId, excerptList);
        representation.setExcerptList(excerptList);
        representation = selectionController.submitExcerptSelection(requester, processInstanceId, representation);

        log.info("Status: " + representation.getStatus());

        assertEquals(RequestStatus.SELECTION_REVIEW, representation.getStatus());

        SecurityContextHolder.clearContext();
    }

    int approveSelection() {
        UserAuthenticationToken requester = getRequester();
        UserAuthenticationToken palga = getPalga();
        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(requester);

        RequestRepresentation representation =
                requestController.getRequestById(requester, processInstanceId);

        log.info("Status: " + representation.getStatus());

        securityContext.setAuthentication(palga);

        representation.setSelectionApproved(true);
        representation = selectionController.setExcerptSelectionApproval(palga, processInstanceId, representation);

        assertEquals(RequestStatus.LAB_REQUEST, representation.getStatus());

        List<LabRequest> labRequests = labRequestQueryService.findAllByProcessInstanceId(processInstanceId);
        assertEquals(2, labRequests.size());

        int pathologyCount = 0;
        for (LabRequest labRequest: labRequests) {
            LabRequestRepresentation labRequestRepresentation =
                    new LabRequestRepresentation(labRequest);
            labRequestQueryService.transferLabRequestData(labRequestRepresentation, false);
            labRequestQueryService.transferExcerptListData(labRequestRepresentation);
            labRequestQueryService.transferLabRequestDetails(labRequestRepresentation, false);
            pathologyCount += labRequestRepresentation.getPathologyCount();
        }

        SecurityContextHolder.clearContext();

        return pathologyCount;
    }

    private List<List<String>> parseDelimiterSeparatedExport(char separator, InputStream inputStream) throws IOException, CsvValidationException {
        List<List<String>> result = new ArrayList<>();
        ICSVParser parser = new CSVParserBuilder().withSeparator(separator).withQuoteChar('"').build();
        CSVReader reader = new CSVReaderBuilder(new InputStreamReader(inputStream))
                .withCSVParser(parser)
                .build();
        String[] line;
        while ((line = reader.readNext()) != null) {
            result.add(Arrays.asList(line));
        }
        return result;
    }

    List<List<String>> downloadRequestsExport() throws IOException, CsvValidationException {
        UserAuthenticationToken palga = getPalga();
        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(palga);

        HttpEntity<InputStreamResource> response = requestExportController.downloadRequestList(palga);
        return parseDelimiterSeparatedExport(';', response.getBody().getInputStream());
    }

    List<List<String>> downloadLabRequestsExport() throws IOException, CsvValidationException {
        UserAuthenticationToken palga = getPalga();
        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(palga);

        HttpEntity<InputStreamResource> response = labRequestExportController.downloadAllPANumbers(palga);
        return parseDelimiterSeparatedExport(';', response.getBody().getInputStream());
    }

}
