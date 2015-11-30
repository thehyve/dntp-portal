package business.services;

import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import business.models.AgreementFormTemplate;
import business.models.AgreementFormTemplateRepository;

@Service
public class AgreementFormTemplateService {

    Log log = LogFactory.getLog(getClass());

    @Autowired
    private AgreementFormTemplateRepository agreementFormTemplateRepository;

    public AgreementFormTemplate get() {
        List<AgreementFormTemplate> templates = agreementFormTemplateRepository.findAll();
        if (templates.size() == 0) {
            AgreementFormTemplate template = new AgreementFormTemplate();
            template.setContents("");
            template.setTimeEdited(new Date());
            template = agreementFormTemplateRepository.save(template);
            return template;
        } else {
            return templates.get(0);
        }
    }

    public AgreementFormTemplate update(AgreementFormTemplate template) {
        AgreementFormTemplate currentTemplate = get();
        if (currentTemplate.getId() == template.getId()) {
            return agreementFormTemplateRepository.save(template);
        } else {
            log.warn("Attempt at adding a new template failed. There should be only one template.");
            return currentTemplate;
        }
    }

    @PostConstruct
    public void init() {
        AgreementFormTemplate template = get();
        log.info("Agreement form template service: template last edited at " + template.getTimeEdited());
    }
}
