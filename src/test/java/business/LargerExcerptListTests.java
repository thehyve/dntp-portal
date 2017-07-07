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

import org.activiti.engine.task.Task;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.multipart.MultipartFile;

import business.models.LabRequest;
import business.representation.ExcerptEntryRepresentation;
import business.representation.ExcerptListRepresentation;
import business.representation.LabRequestRepresentation;
import business.representation.RequestRepresentation;
import business.representation.RequestStatus;
import business.security.MockConfiguration.MockMailSender;
import business.security.UserAuthenticationToken;

@Profile("dev")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@ContextConfiguration
public class LargerExcerptListTests extends AbstractSelectionControllerTests {

    @Test
    public void testUploadExcerptList() throws Exception {
        createRequest();
        submitRequest();
        submitRequestForApproval();
        approveRequest();

        long entryCount = uploadExcerptList("Example excerptlist 20150521 v2.csv");
        assertEquals(6, entryCount);
    }

    @Test
    public void testSelectExcerpts() throws Exception {
        createRequest();
        submitRequest();
        submitRequestForApproval();
        approveRequest();
        uploadExcerptList("Example excerptlist 20150521 v2.csv");
        selectExcerpts();
    }

    @Test
    public void testApproveSelection() throws Exception {
        createRequest();
        submitRequest();
        submitRequestForApproval();
        approveRequest();
        uploadExcerptList("Example excerptlist 20150521 v2.csv");
        selectExcerpts();

        ((MockMailSender)mailSender).clear();

        int pathologyCount = approveSelection();

        assertEquals(6, pathologyCount);
        assertEquals(3, ((MockMailSender)mailSender).getMessages().size());
    }
    
}
