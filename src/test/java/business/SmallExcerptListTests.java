/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import business.security.MockConfiguration.MockMailSender;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@Profile("dev")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@ContextConfiguration
public class SmallExcerptListTests extends AbstractSelectionControllerTests {

    @Test
    public void testUploadExcerptList() throws Exception {
        createRequest();
        submitRequest();
        submitRequestForApproval();
        approveRequest();

        long entryCount = uploadExcerptList("Example excerptlist.csv");
        assertEquals(3, entryCount);
    }

    @Test
    public void testSelectExcerpts() throws Exception {
        createRequest();
        submitRequest();
        submitRequestForApproval();
        approveRequest();
        uploadExcerptList("Example excerptlist.csv");
        selectExcerpts();
    }
    
    @Test
    public void testApproveSelection() throws Exception {
        createRequest();
        submitRequest();
        submitRequestForApproval();
        approveRequest();
        uploadExcerptList("Example excerptlist.csv");
        selectExcerpts();

        ((MockMailSender)mailSender).clear();

        int pathologyCount = approveSelection();

        assertEquals(3, pathologyCount);
        assertEquals(3, ((MockMailSender)mailSender).getMessages().size());
    }

}
