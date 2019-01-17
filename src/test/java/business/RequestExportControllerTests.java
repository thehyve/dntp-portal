package business;

import business.representation.RequestStatus;
import business.services.RequestPropertiesService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

@Profile("dev")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@ContextConfiguration
public class RequestExportControllerTests extends AbstractSelectionControllerTests {

    @Autowired
    RequestPropertiesService requestPropertiesService;

    @Before
    public void setup() {
        testService.clearDatabase();
    }

    @Test
    public void testExport() throws Exception {
        List<String> expectedRequestIds = new ArrayList<>();
        // Create open request (should not be included in the export)
        createRequest();

        // Create submitted request
        createRequest();
        submitRequest();
        expectedRequestIds.add(processInstanceId);

        // Create approval request
        createRequest();
        submitRequest();
        submitRequestForApproval();
        expectedRequestIds.add(processInstanceId);

        // Create request with data delivered
        createRequest();
        submitRequest();
        submitRequestForApproval();
        approveRequest();
        uploadExcerptList("Example excerptlist 20150521 v2.csv");
        expectedRequestIds.add(processInstanceId);
        String dataDeliveredId = processInstanceId;

        // Download export data
        List<List<String>> exportData = downloadRequestsExport();
        // Expect a header row and three requests
        assertEquals(4, exportData.size());
        List<String> exportDataHeader = exportData.get(0);
        assertEquals("Request number", exportDataHeader.get(0));
        assertEquals("Date created", exportDataHeader.get(1));
        assertEquals("Date submitted", exportDataHeader.get(2));
        assertEquals("Date delivered", exportDataHeader.get(3));
        assertEquals("Status", exportDataHeader.get(5));

        // Expect all request, except the one with status Open, to be in the export
        Set<String> expectedRequestNumbers = expectedRequestIds.stream()
                .map(requestId -> requestPropertiesService.findByProcessInstanceId(requestId).getRequestNumber())
                .collect(Collectors.toSet());
        Set<String> actualRequestNumbers = exportData.stream().skip(1)
                .map(line -> line.get(0))
                .collect(Collectors.toSet());
        assertEquals(expectedRequestNumbers, actualRequestNumbers);

        // Expect matching statuses
        Set<String> expectedStatuses = new HashSet<>(Arrays.asList(
                RequestStatus.REVIEW.toString(),
                RequestStatus.APPROVAL.toString(),
                RequestStatus.DATA_DELIVERY.toString()
        ));
        Set<String> actualStatuses = exportData.stream().skip(1)
                .map(line -> line.get(5))
                .collect(Collectors.toSet());
        assertEquals(expectedStatuses, actualStatuses);

        // Expect date fields to be set
        for (List<String> line: exportData) {
            // Date created not empty
            assertNotEquals(0, line.get(1).length());
            // Date submitted not empty
            assertNotEquals(0, line.get(2).length());
        }
        String dataDeliveredRequestNumber = requestPropertiesService.findByProcessInstanceId(dataDeliveredId).getRequestNumber();
        List<String> dataDeliveredLine = exportData.stream().filter(line -> line.get(0).equals(dataDeliveredRequestNumber)).findFirst().get();
        // Expect Date delivered to be set
        assertNotEquals(0, dataDeliveredLine.get(3));
    }

}
