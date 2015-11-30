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
import business.representation.AgreementFormTemplateRepresentation;
import business.security.UserAuthenticationToken;
import business.services.AgreementFormTemplateService;

@RestController
public class AgreementFormTemplateController {

    Log log = LogFactory.getLog(getClass());

    @Autowired
    private AgreementFormTemplateService agreementFormTemplateService;

    @RequestMapping(value = "/public/agreementFormTemplate", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public AgreementFormTemplateRepresentation getTemplate(UserAuthenticationToken user) {
        log.info("GET /public/agreementFormTemplate");

        AgreementFormTemplate template = agreementFormTemplateService.get();
        return new AgreementFormTemplateRepresentation(template);
    }

    @RequestMapping(value = "/admin/agreementFormTemplate", method = RequestMethod.PUT)
    public AgreementFormTemplateRepresentation saveTemplate(UserAuthenticationToken user, @RequestBody AgreementFormTemplateRepresentation body) {
        log.info("PUT /admin/agreementFormTemplate");

        AgreementFormTemplate template = agreementFormTemplateService.get();
        template.setContents(body.getContents());
        template.setLastEditedBy(user.getUser());
        template.setTimeEdited(new Date());
        template = agreementFormTemplateService.update(template);
        return new AgreementFormTemplateRepresentation(template);
    }


}
