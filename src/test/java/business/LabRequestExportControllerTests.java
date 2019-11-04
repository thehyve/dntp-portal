package business;

import business.security.MockConfiguration;
import business.services.RequestPropertiesService;
import business.services.TestService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

import static org.junit.Assert.assertEquals;

@Profile("dev")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@ContextConfiguration
public class LabRequestExportControllerTests extends AbstractSelectionControllerTests {

    @Autowired
    TestService testService;

    @Before
    public void setup() {
        ((MockConfiguration.MockMailSender) this.mailSender).clear();
        testService.clearDatabase();
        log.info("TEST  Test: " + this.getClass().toString());
    }

    @Test
    public void testExport() throws Exception {
        // Create request with data delivered
        createRequest();
        submitRequest();
        submitRequestForApproval();
        approveRequest();
        uploadExcerptList("Example excerptlist 20150521 v2.csv");
        selectExcerpts();
        approveSelection();

        // Download export data
        List<List<String>> exportData = downloadLabRequestsExport();
        // Expect a header row and 6 PA numbers (for 2 lab requests)
        assertEquals(7, exportData.size());
        List<String> exportDataHeader = exportData.get(0);

        assertEquals("Request_number", exportDataHeader.get(0));
        assertEquals("Date_created", exportDataHeader.get(1));
        assertEquals("Date_sent", exportDataHeader.get(2));
        assertEquals("Status", exportDataHeader.get(3));
        assertEquals("PA_reports", exportDataHeader.get(4));
        assertEquals("PA_material_Block", exportDataHeader.get(5));
        assertEquals("PA_material_HE_slice", exportDataHeader.get(6));
        assertEquals("PA_material_other", exportDataHeader.get(7));
        assertEquals("Clinical_data", exportDataHeader.get(8));
        assertEquals("PA_number_count", exportDataHeader.get(9));
    }

}
