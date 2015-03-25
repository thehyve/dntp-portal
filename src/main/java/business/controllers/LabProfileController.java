package business.controllers;

import business.models.Lab;
import business.models.LabRepository;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class LabProfileController {

    @Autowired
    LabRepository labRepository;

    @RequestMapping(value = "/public/labs", method = RequestMethod.GET)
    public List<Lab> get() {
        LogFactory.getLog(getClass()).info("GET /labs/ for anonymous user");
        return labRepository.findAll();
    }

    @RequestMapping(value = "/public/labs/{id}", method = RequestMethod.GET)
    public Lab get(@PathVariable("id") long id) {
        LogFactory.getLog(getClass()).info("GET /labs/" + id + " for anonymous user");
        return labRepository.findOne(id);
    }
}
