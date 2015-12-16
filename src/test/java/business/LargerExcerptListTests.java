package business;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import org.activiti.engine.task.Task;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;
import org.testng.annotations.Test;

import business.models.LabRequest;
import business.representation.ExcerptEntryRepresentation;
import business.representation.ExcerptListRepresentation;
import business.representation.LabRequestRepresentation;
import business.representation.RequestRepresentation;
import business.security.MockConfiguration.MockMailSender;
import business.security.UserAuthenticationToken;

public class LargerExcerptListTests extends SelectionControllerTests {

    @Test(groups="request", dependsOnMethods="approveRequest")
    public void uploadExcerptList() throws IOException {
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
        
        representation = requestController.claim(palga, processInstanceId, representation);
        
        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource("test/Example excerptlist 20150521 v2.csv");
        InputStream input = resource.openStream();
        MultipartFile file = new MockMultipartFile(resource.getFile(), input);
        
        Integer flowTotalChunks = 1;
        Integer flowChunkNumber = 1;
        String flowIdentifier = "flow";
        
        representation = requestController.uploadExcerptList(
                palga, 
                processInstanceId, 
                resource.getFile(),
                flowTotalChunks,
                flowChunkNumber, 
                flowIdentifier,
                file);
        
        assertEquals(6, representation.getExcerptList().getEntries().size());
        
        SecurityContextHolder.clearContext();
    }
    
    @Test(groups="request", dependsOnMethods="uploadExcerptList")
    public void selectExcerpts() {
        UserAuthenticationToken requester = getRequester();
        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(requester);

        RequestRepresentation representation = 
                requestController.getRequestById(requester, processInstanceId);
        
        log.info("Status: " + representation.getStatus());
        
        ExcerptListRepresentation excerptList = representation.getExcerptList();
        for(ExcerptEntryRepresentation entry: excerptList.getEntries()) {
            entry.setSelected(true);
        }
        excerptList = selectionController.updateExcerptListSelection(requester, processInstanceId, excerptList);
        representation.setExcerptList(excerptList);
        representation = selectionController.submitExcerptSelection(requester, processInstanceId, representation);

        log.info("Status: " + representation.getStatus());
        
        assertEquals("SelectionReview", representation.getStatus());
        
        SecurityContextHolder.clearContext();
    }
    
    @Test(groups="request", dependsOnMethods="selectExcerpts")
    public void approveSelection() {
        UserAuthenticationToken requester = getRequester();
        UserAuthenticationToken palga = getPalga();
        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(requester);
     
        ((MockMailSender)mailSender).clear();
        
        RequestRepresentation representation = 
                requestController.getRequestById(requester, processInstanceId);
        
        log.info("Status: " + representation.getStatus());

        securityContext.setAuthentication(palga);

        representation.setSelectionApproved(true);
        representation = selectionController.setExcerptSelectionApproval(palga, processInstanceId, representation);
        
        assertEquals("LabRequest", representation.getStatus());
        
        assertEquals(2, labRequestRepository.count());
        List<LabRequest> labRequests = labRequestRepository.findAllByProcessInstanceId(processInstanceId);
        assertEquals(2, labRequests.size());
        
        int pathologyCount = 0;
        for (LabRequest labRequest: labRequests) {
            LabRequestRepresentation labRequestRepresentation = 
                    new LabRequestRepresentation(labRequest);
            labRequestService.transferLabRequestData(labRequestRepresentation);
            labRequestService.transferExcerptListData(labRequestRepresentation);
            labRequestService.transferLabRequestDetails(labRequestRepresentation, false);
            pathologyCount += labRequestRepresentation.getPathologyCount();
        }
        long pathologyCount2 = pathologyItemRepository.count();
        assertEquals(6, pathologyCount);
        assertEquals(6, pathologyCount2);
        
        // fails because contact data for labs is not set
        //assertEquals(2, ((MockMailSender)mailSender).getMessages().size());
        
        SecurityContextHolder.clearContext();
    }    
    
}
