package business.security;

import java.security.Principal;
import java.util.List;

import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import business.models.Lab;
import business.models.LabRepository;
import business.models.User;

@RestController
public class LabController {

    @Autowired
    LabRepository labRepository;
    
    @RequestMapping(value = "/admin/labs/{id}", method = RequestMethod.GET)
    public Lab get(@RequestParam Long id) {
        return labRepository.findOne(id);
    }

    @RequestMapping(value = "/admin/labs", method = RequestMethod.GET)
    public List<Lab> getAll(Principal principal) {
        LogFactory.getLog(getClass()).info("GET /admin/labs (for user: " + principal.getName() + ")");
        return labRepository.findAll();
    }
   
    @RequestMapping(value = "/admin/labs", method = RequestMethod.POST)
    public Lab create(Principal principal, @RequestBody Lab lab) {
        LogFactory.getLog(getClass()).info("POST /admin/labs (for user: " + principal.getName() + ")");
        return labRepository.save(lab);
    }

    @RequestMapping(value = "/admin/labs", method = RequestMethod.PUT)
    public Lab update(Principal principal, @RequestBody Lab body) {
        LogFactory.getLog(getClass()).info("PUT /admin/labs/" + body.getNumber());
        Lab lab = labRepository.getOne(body.getId());
        // Copy values. The lab number cannot be changed.
        lab.setName(body.getName());
        lab.setContactData(body.getContactData());
        return labRepository.save(lab);
    }
    
}
