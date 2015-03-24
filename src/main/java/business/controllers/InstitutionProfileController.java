package business.controllers;

import business.models.Institution;
import business.models.InstitutionRepository;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class InstitutionProfileController {

    @Autowired
    InstitutionRepository institutionRepository;

    @RequestMapping(value = "/public/institutions", method = RequestMethod.GET)
    public List<Institution> get() {
        LogFactory.getLog(getClass()).info("GET /institutions/ for anonymous user");
        return institutionRepository.findAll();
    }

    @RequestMapping(value = "/public/institutions/{id}", method = RequestMethod.GET)
    public Institution get(@PathVariable("id") long id) {
        LogFactory.getLog(getClass()).info("GET /institutions/" + id + " for anonymous user");
        return institutionRepository.findOne(id);
    }    

}
