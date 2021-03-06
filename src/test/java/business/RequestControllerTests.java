/**
 * Copyright (C) 2017  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business;

import business.exceptions.InvalidRequest;
import business.representation.RequestListRepresentation;
import business.representation.RequestRepresentation;
import business.representation.RequestStatus;
import business.security.MockConfiguration.MockMailSender;
import business.security.UserAuthenticationToken;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;


@Profile("dev")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@ContextConfiguration
public class RequestControllerTests extends AbstractSelectionControllerTests {

    @Before
    public void setup() {
        ((MockMailSender) this.mailSender).clear();
        log.info("TEST  Test: " + this.getClass().toString());
    }

    @Test(expected = InvalidRequest.class)
    public void testRequestFieldMissing() {
        createRequestResearchQuestionMissing();
        submitRequest();
    }

    @Test(expected = InvalidRequest.class)
    public void testRequestRejectedICFormMissing() {
        createRequestWithInformedConsent();
        submitRequest();
    }

    @Test
    public void testRequestWithInformedConsent() throws IOException {
        createRequestWithInformedConsent();
        uploadInformedConsentForm();
        submitRequest();
    }

    @Test
    public void testApproveRequest() throws Exception {
        createRequest();
        submitRequest();
        submitRequestForApproval();
        approveRequest();

        UserAuthenticationToken palga = getPalga();
        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(palga);

        RequestRepresentation representation =
                requestController.getRequestById(palga, processInstanceId);
        log.info("Status: " + representation.getStatus());
        assertEquals(RequestStatus.DATA_DELIVERY, representation.getStatus());

        SecurityContextHolder.clearContext();
    }

    @Test
    public void testApproveRequestWithNonApplicableAgreement() throws Exception {
        createRequest();
        submitRequest();
        submitRequestForApprovalWithNonApplicableAgreement();

        UserAuthenticationToken palga = getPalga();
        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(palga);

        RequestRepresentation representation =
                requestController.getRequestById(palga, processInstanceId);
        log.info("Status: " + representation.getStatus());
        assertEquals(RequestStatus.REVIEW, representation.getStatus());
        assertFalse(representation.isRequestAdmissible());

        SecurityContextHolder.clearContext();
    }

    @Test
    public void testSkipApproval() throws InterruptedException {
        createRequest();
        submitRequest();
        skipApproval();

        UserAuthenticationToken palga = getPalga();
        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(palga);

        RequestRepresentation representation =
                requestController.getRequestById(palga, processInstanceId);
        log.info("Status: " + representation.getStatus());
        assertEquals(RequestStatus.DATA_DELIVERY, representation.getStatus());

        SecurityContextHolder.clearContext();
    }

    @Test
    public void testSkipApprovalWithNonApplicableAgreement() throws InterruptedException {
        createRequest();
        submitRequest();
        skipApprovalWithNonApplicableAgreement();

        UserAuthenticationToken palga = getPalga();
        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(palga);

        RequestRepresentation representation =
                requestController.getRequestById(palga, processInstanceId);
        log.info("Status: " + representation.getStatus());
        assertEquals(RequestStatus.DATA_DELIVERY, representation.getStatus());

        SecurityContextHolder.clearContext();
    }

    @Test
    public void testLastAssignee() {
        createRequest();
        submitRequest();

        UserAuthenticationToken palga = getPalga();
        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(palga);

        RequestRepresentation representation =
                requestController.getRequestById(palga, processInstanceId);
        RequestRepresentation claimedRepresentation = requestController.claim(palga, processInstanceId, representation);
        RequestListRepresentation requestListRepresentation = requestService.getRequestData(processInstanceId,
                palga.getUser());
        assertEquals("palga", requestListRepresentation.getAssigneeName());
        assertEquals("palga", requestListRepresentation.getLastAssignee());

        RequestRepresentation unclaimedRepresentation = requestController.unclaim(palga, processInstanceId,
                claimedRepresentation);
        requestListRepresentation =  requestService.getRequestData(processInstanceId, palga.getUser());
        assertNotNull(requestListRepresentation);
        assertNull(requestListRepresentation.getAssignee());
        assertNull(requestListRepresentation.getAssigneeName());
        assertEquals("palga", requestListRepresentation.getLastAssignee());

        SecurityContextHolder.clearContext();
    }

}
