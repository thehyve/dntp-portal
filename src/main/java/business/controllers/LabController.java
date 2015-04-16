package business.controllers;

import java.security.Principal;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import business.models.ContactData;
import business.models.Lab;
import business.models.LabRepository;
import business.models.User;
import business.security.UserAuthenticationToken;

@RestController
public class LabController {

    Log log = LogFactory.getLog(getClass());
    
    @Autowired
    LabRepository labRepository;
    
    @RequestMapping(value = "/admin/labs/{id}", method = RequestMethod.GET)
    public Lab get(@PathVariable Long id) {
        return labRepository.findOne(id);
    }

    @RequestMapping(value = "/admin/labs", method = RequestMethod.GET)
    public List<Lab> getAll(Principal principal) {
        log.info("GET /admin/labs (for user: " + principal.getName() + ")");
        return labRepository.findAll();
    }
    
    public void transferLabData(Lab body, Lab lab) {
        lab.setName(body.getName());
        if (lab.getContactData() == null) {
            lab.setContactData(new ContactData());
        }
        lab.getContactData().copy(body.getContactData());
    }
   
    @ResponseStatus(value=HttpStatus.BAD_REQUEST)  // 
    public class InvalidLabNumber extends RuntimeException {
        public InvalidLabNumber() {
            super("Invalid lab number.");
        }
    }
    
    @RequestMapping(value = "/admin/labs", method = RequestMethod.POST)
    public Lab create(Principal principal, @RequestBody Lab body) {
        log.info("POST /admin/labs (for user: " + principal.getName() + ")");
        Lab lab = new Lab();
        if (body.getNumber() == null || body.getNumber().intValue() <= 0) {
            throw new InvalidLabNumber();
        }
        lab.setNumber(body.getNumber());
        transferLabData(body, lab);
        return labRepository.save(lab);
    }
    
    @ResponseStatus(value=HttpStatus.NOT_FOUND)  // 
    public class LabNotFound extends RuntimeException {
        public LabNotFound() {
            super("Lab cannot be found.");
        }
    }

    
    @RequestMapping(value = "/admin/labs/{id}", method = RequestMethod.PUT,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public Lab update(Principal principal, @PathVariable Long id, @RequestBody Lab body) {
        log.info("PUT /admin/labs/" + body.getNumber());
        Lab lab = labRepository.getOne(id);
        if (lab == null) {
            throw new LabNotFound();
        }
        // Copy values. The lab number cannot be changed.
        transferLabData(body, lab);
        return  labRepository.save(lab);
    }

    @RequestMapping(value = "/admin/labs/{id}/activate", method = RequestMethod.PUT,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public Lab activate(Principal principal, @PathVariable Long id, @RequestBody Lab body) {
        log.info("PUT /admin/labs/" + body.getNumber());
        Lab lab = labRepository.getOne(id);
        if (lab == null) {
            throw new LabNotFound();
        }
        lab.activate();
        return  labRepository.save(lab);
    }
    
    @RequestMapping(value = "/admin/labs/{id}/deactivate", method = RequestMethod.PUT,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public Lab deactivate(Principal principal, @PathVariable Long id, @RequestBody Lab body) {
        log.info("PUT /admin/labs/" + body.getNumber());
        Lab lab = labRepository.getOne(id);
        if (lab == null) {
            throw new LabNotFound();
        }
        lab.deactivate();
        return  labRepository.save(lab);
    }
    
    @ResponseStatus(value=HttpStatus.UNAUTHORIZED)  // 
    public class UserUnauthorised extends RuntimeException {
        public UserUnauthorised(String message) {
            super(message);
        }
    }
    
    @ResponseStatus(value=HttpStatus.BAD_REQUEST)  // 
    public class LabuserWithoutLab extends RuntimeException {
        public LabuserWithoutLab(String message) {
            super(message);
        }
    }
    
    @RequestMapping(value = "/lab", method = RequestMethod.GET)
    public Lab getLab(UserAuthenticationToken token) {
        User user = token.getUser();
        if (user != null && user.isLabUser()) {
            if (user.getLab() == null) {
                throw new LabuserWithoutLab("No lab associated with lab user.");
            }
            return labRepository.findOne(user.getLab().getId());
        }
        throw new UserUnauthorised("User not authorised to fetch lab information.");
    }
    
    @RequestMapping(value = "/lab", method = RequestMethod.PUT,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public Lab updateLab(UserAuthenticationToken token, @RequestBody Lab body) {
        log.info("PUT /lab " + body.getNumber());
        User user = token.getUser();
        if (user != null && user.isLabUser()) {
            if (user.getLab() == null) {
                throw new LabuserWithoutLab("No lab associated with lab user.");
            }
        }
        Lab lab = labRepository.findOne(user.getLab().getId());
        if (lab == null) {
            throw new LabNotFound();
        }
        // Copy values. The lab number cannot be changed.
        transferLabData(body, lab);
        return labRepository.save(lab);
    }
    
}
