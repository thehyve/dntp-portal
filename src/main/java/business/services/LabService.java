package business.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import business.models.Lab;
import business.models.LabRepository;

@Service
public class LabService {

    Log log = LogFactory.getLog(getClass());

    @Autowired
    LabRepository labRepository;
    
    @Transactional
    public Lab save(Lab lab) {
        return labRepository.save(lab);
    }

    @Transactional(readOnly = true)
    public List<Lab> findAll() {
        return labRepository.findAllByOrderByNumberAsc();
    }

    @Transactional(readOnly = true)
    public List<Lab> findAllActive() {
        return labRepository.findAllByActiveTrueOrderByNumberAsc();
    }

    @Transactional(readOnly = true)
    public List<Lab> findAll(Set<Long> labIds) {
        return labRepository.findAllByOrderByNumberAsc(labIds);
    }

    public Lab findOne(Long id) {
        return labRepository.findOne(id);
    }

    public Lab findOneActive(long id) {
        return labRepository.findOneByActiveTrue(id);
    }

    public Lab findByNumber(Integer labNumber) {
        return labRepository.findByNumber(labNumber);
    }

    public Lab findByName(String name) {
        return labRepository.findByName(name);
    }

    @Transactional
    public void fixLabEmailAddresses() {
        for (Lab lab: findAll()) {
            List<String> emailAddresses = lab.getEmailAddresses();
            if (emailAddresses == null) {
                emailAddresses = new ArrayList<>();
            }
            if (emailAddresses.isEmpty()) {
                if (lab.getContactData() != null) {
                    String email = lab.getContactData().getEmail();
                    if (email != null) {
                        email = email.trim().toLowerCase();
                        if (!email.isEmpty()) {
                            emailAddresses.add(email);
                        }
                    }
                    lab.getContactData().setEmail(null);
                }
            }
            lab.setEmailAddresses(emailAddresses);
            save(lab);
        }
    }

}
