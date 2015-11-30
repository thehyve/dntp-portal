package business.controllers;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import business.models.LabRequestRepository;
import business.models.PathologyItemRepository;
import business.models.RequestPropertiesRepository;
import business.services.FileService;
import business.services.MailService;

@RestController
public class StatusController {

    Log log = LogFactory.getLog(getClass());

    @Autowired RequestPropertiesRepository requestPropertiesRepository;

    @Autowired PathologyItemRepository pathologyItemRepository;

    @Autowired LabRequestRepository labRequestRepository;

    @Autowired TaskService taskService;

    @Autowired RuntimeService runtimeService;
    
    @Autowired FileService fileService;
    
    @Autowired MailService mailService;

    @RequestMapping(value = "/status", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> getStatus() {
        log.info("GET /status");
        
        boolean ok = true;
        
        Map<String, Object> status = new HashMap<String, Object>();
        try {
            status.put("requests", requestPropertiesRepository.count());
        } catch (Exception e) {
            status.put("requests", e.getMessage());
            ok = false;
        }
        try {
            status.put("labrequests", labRequestRepository.count());
        } catch (Exception e) {
            status.put("labrequests", e.getMessage());
            ok = false;
        }
        try {
            status.put("pathologyitems", pathologyItemRepository.count());
        } catch (Exception e) {
            status.put("pathologyitems", e.getMessage());
            ok = false;
        }
        try {
            status.put("active_processes", runtimeService.createProcessInstanceQuery().active().count());
        } catch (Exception e) {
            status.put("active_processes", e.getMessage());
            ok = false;
        }
        try {
            status.put("active_tasks", taskService.createTaskQuery().active().count());
        } catch (Exception e) {
            status.put("active_tasks", e.getMessage());
            ok = false;
        }
        boolean fileServiceStatus = fileService.checkUploadPath();
        ok = ok && fileServiceStatus;
        status.put("fileservice", fileServiceStatus);
        boolean mailServiceStatus = mailService.checkMailSender();
        ok = ok && mailServiceStatus;
        status.put("mailservice", mailServiceStatus);

        HttpStatus code = (ok) ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR;
        return new ResponseEntity<Map<String, Object>>(status, code);
    }

}
