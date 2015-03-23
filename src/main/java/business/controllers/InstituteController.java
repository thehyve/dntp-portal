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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import business.models.ContactData;
import business.models.Institute;
import business.models.InstituteRepository;
import business.models.Lab;

@RestController
public class InstituteController {

    @Autowired
    InstituteRepository instituteRepository;
    
    @RequestMapping(value = "/admin/institutes/{id}", method = RequestMethod.GET)
    public Institute get(@RequestParam Long id) {
        return instituteRepository.findOne(id);
    }

    @RequestMapping(value = "/admin/institutes", method = RequestMethod.GET)
    public List<Institute> getAll(Principal principal) {
        LogFactory.getLog(getClass()).info("GET /admin/institutes (for user: " + principal.getName() + ")");
        return instituteRepository.findAll();
    }
   
    public ResponseEntity<Object> transferInstituteData(Institute body, Institute institute) {
        institute.setName(body.getName());
        if (institute.getContactData() == null) {
            institute.setContactData(new ContactData());
        }
        institute.getContactData().copy(body.getContactData());
        return null;
    }
    
    @RequestMapping(value = "/admin/institutes", method = RequestMethod.POST)
    public ResponseEntity<Object> create(Principal principal, @RequestBody Institute body) {
        LogFactory.getLog(getClass()).info("POST /admin/institutes (for user: " + principal.getName() + ")");
        Institute institute = new Institute();
        ResponseEntity<Object> result = transferInstituteData(body, institute);
        if (result == null) {
            return new ResponseEntity<Object>(instituteRepository.save(institute), HttpStatus.OK);
        }
        return result;
    }

    @RequestMapping(value = "/admin/institutes/{id}", method = RequestMethod.PUT,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> update(Principal principal, @PathVariable Long id, @RequestBody Institute body) {
        LogFactory.getLog(getClass()).info("PUT /admin/institutes/" + id);
        Institute institute = instituteRepository.getOne(id);
        if (institute == null) {
            return new ResponseEntity<Object>("Institute cannot be found.", HttpStatus.NOT_FOUND);
        }
        // Copy values.
        ResponseEntity<Object> result = transferInstituteData(body, institute);
        if (result == null) {
            return new ResponseEntity<Object>(instituteRepository.save(institute), HttpStatus.OK);
        }
        return result;
    }
    
}
