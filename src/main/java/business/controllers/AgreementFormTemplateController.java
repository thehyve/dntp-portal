package business.controllers;

import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import business.models.AgreementFormTemplate;
import business.models.AgreementFormTemplateRepository;
import business.representation.AgreementFormTemplateRepresentation;
import business.security.UserAuthenticationToken;

@RestController
public class AgreementFormTemplateController {

    Log log = LogFactory.getLog(getClass());

    @Autowired
    private AgreementFormTemplateRepository agreementFormTemplateRepository;

    @RequestMapping(value = "/public/agreementFormTemplate", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public AgreementFormTemplateRepresentation getTemplate(UserAuthenticationToken user) {
        log.info("GET /public/agreementFormTemplate");

        List<AgreementFormTemplate> templates = agreementFormTemplateRepository.findAll();
        if (templates.size() == 0) {
            return null;
        } else {
            return new AgreementFormTemplateRepresentation(templates.get(0));
        }
    }

    @RequestMapping(value = "/admin/agreementFormTemplate", method = RequestMethod.PUT)
    public AgreementFormTemplateRepresentation saveTemplate(UserAuthenticationToken user, @RequestBody AgreementFormTemplateRepresentation body) {
        log.info("PUT /admin/agreementFormTemplate");

        AgreementFormTemplate template;
        List<AgreementFormTemplate> templates = agreementFormTemplateRepository.findAll();
        if (templates.size() == 0) {
            template = new AgreementFormTemplate();
        } else {
            template = templates.get(0);
        }
        template.setContents(body.getContents());
        template.setLastEditedBy(user.getUser());
        template.setTimeEdited(new Date());
        template = agreementFormTemplateRepository.save(template);
        return new AgreementFormTemplateRepresentation(template);
    }


}
