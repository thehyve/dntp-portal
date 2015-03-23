package business.controllers;

import java.security.Principal;
import java.util.List;

import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import business.models.ContactData;
import business.models.Lab;
import business.models.LabRepository;

@RestController
public class LabController {

    @Autowired
    LabRepository labRepository;
    
    @RequestMapping(value = "/admin/labs/{id}", method = RequestMethod.GET)
    public Lab get(@PathVariable Long id) {
        return labRepository.findOne(id);
    }

    @RequestMapping(value = "/admin/labs", method = RequestMethod.GET)
    public List<Lab> getAll(Principal principal) {
        LogFactory.getLog(getClass()).info("GET /admin/labs (for user: " + principal.getName() + ")");
        return labRepository.findAll();
    }
    
    public ResponseEntity<Object> transferLabData(Lab body, Lab lab) {
        lab.setName(body.getName());
        if (lab.getContactData() == null) {
            lab.setContactData(new ContactData());
        }
        lab.getContactData().copy(body.getContactData());
        return null;
    }
   
    @RequestMapping(value = "/admin/labs", method = RequestMethod.POST)
    public ResponseEntity<Object> create(Principal principal, @RequestBody Lab body) {
        LogFactory.getLog(getClass()).info("POST /admin/labs (for user: " + principal.getName() + ")");
        Lab lab = new Lab();
        if (body.getNumber() == null || body.getNumber().intValue() <= 0) {
            return new ResponseEntity<Object>("Invalid lab number", HttpStatus.BAD_REQUEST);
        }
        lab.setNumber(body.getNumber());
        ResponseEntity<Object> result = transferLabData(body, lab);
        if (result == null) {
            return new ResponseEntity<Object>(labRepository.save(lab), HttpStatus.OK);
        }
        return result;
    }
    
    @RequestMapping(value = "/admin/labs/{id}", method = RequestMethod.PUT,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> update(Principal principal, @PathVariable Long id, @RequestBody Lab body) {
        LogFactory.getLog(getClass()).info("PUT /admin/labs/" + body.getNumber());
        Lab lab = labRepository.getOne(id);
        if (lab == null) {
            return new ResponseEntity<Object>("Lab cannot be found.", HttpStatus.NOT_FOUND);
        }
        // Copy values. The lab number cannot be changed.
        ResponseEntity<Object> result = transferLabData(body, lab);
        if (result == null) {
            return new ResponseEntity<Object>(labRepository.save(lab), HttpStatus.OK);
        }
        return result;
    }
    
}
