package business.security;

import java.security.Principal;
import java.util.List;

import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import business.models.Institution;
import business.models.InstitutionRepository;

@RestController
public class InstitutionController {

    @Autowired
    InstitutionRepository institutionRepository;
    
    @RequestMapping(value = "/admin/institutions/{id}", method = RequestMethod.GET)
    public Institution get(@RequestParam Long id) {
        return institutionRepository.findOne(id);
    }

    @RequestMapping(value = "/admin/institutions", method = RequestMethod.GET)
    public List<Institution> getAll(Principal principal) {
        LogFactory.getLog(getClass()).info("GET /admin/institutions (for user: " + principal.getName() + ")");
        return institutionRepository.findAll();
    }
   
    @RequestMapping(value = "/admin/institutions", method = RequestMethod.POST)
    public Institution create(Principal principal, @RequestBody Institution institution) {
        LogFactory.getLog(getClass()).info("POST /admin/institutions (for user: " + principal.getName() + ")");
        return institutionRepository.save(institution);
    }

    @RequestMapping(value = "/admin/institutions", method = RequestMethod.PUT)
    public Institution update(Principal principal, @RequestBody Institution body) {
        LogFactory.getLog(getClass()).info("PUT /admin/institutions/" + body.getId());
        Institution institution = institutionRepository.getOne(body.getId());
        // Copy values (PUT should not create new
        institution.setName(body.getName());
        institution.setContactData(body.getContactData());
        return institutionRepository.save(institution);
    }
    
}
