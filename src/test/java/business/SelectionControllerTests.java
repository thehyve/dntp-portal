/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@Profile("dev")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@ContextConfiguration
public class SelectionControllerTests extends AbstractSelectionControllerTests {

    @Test
    public void testCreateRequest() {
        createRequest();
    }

    @Test
    public void testSubmitRequest() {
        createRequest();
        submitRequest();
    }

    @Test
    public void testSubmitRequestForApproval() throws Exception {
        createRequest();
        submitRequest();
        submitRequestForApproval();
    }

    @Test
    public void testApproveRequest() throws Exception {
        createRequest();
        submitRequest();
        submitRequestForApproval();
        approveRequest();
    }

}
