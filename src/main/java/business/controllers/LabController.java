/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business.controllers;

import java.security.Principal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.mail.internet.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import business.exceptions.EmailAddressInvalid;
import business.exceptions.InvalidLabNumber;
import business.exceptions.LabNotFound;
import business.exceptions.LabuserWithoutLab;
import business.exceptions.UserUnauthorised;
import business.models.ContactData;
import business.models.Lab;
import business.models.User;
import business.representation.ProfileRepresentation;
import business.security.UserAuthenticationToken;
import business.services.LabService;
import business.services.UserService;

@RestController
public class LabController {

    Log log = LogFactory.getLog(getClass());

    @Autowired
    LabService labService;

    @Autowired
    UserService userService;

    @RequestMapping(value = "/admin/labs/{id}", method = RequestMethod.GET)
    public Lab get(@PathVariable Long id) {
        return labService.findOne(id);
    }

    @RequestMapping(value = "/admin/labs", method = RequestMethod.GET)
    public List<Lab> getAll(Principal principal) {
        log.info("GET /admin/labs (for user: " + principal.getName() + ")");
        return labService.findAll();
    }

    public void transferLabData(Lab body, Lab lab) {
        lab.setName(body.getName());
        if (lab.getContactData() == null) {
            lab.setContactData(new ContactData());
        }
        lab.getContactData().copy(body.getContactData());
        Set<String> emailAddresses = new LinkedHashSet<>();
        for (String email: body.getEmailAddresses()) {
            try {
                InternetAddress address = new InternetAddress(email);
                address.validate();
                emailAddresses.add(email.trim().toLowerCase());
            } catch (AddressException e) {
                throw new EmailAddressInvalid(email);
            }
        }
        lab.setEmailAddresses(new ArrayList<>(emailAddresses));
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
        return labService.save(lab);
    }

    @RequestMapping(value = "/admin/labs/{id}", method = RequestMethod.PUT,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public Lab update(Principal principal, @PathVariable Long id, @RequestBody Lab body) {
        log.info("PUT /admin/labs/" + body.getNumber());
        Lab lab = labService.findOne(id);
        if (lab == null) {
            throw new LabNotFound();
        }
        // Copy values. The lab number cannot be changed.
        transferLabData(body, lab);
        return  labService.save(lab);
    }

    @RequestMapping(value = "/admin/labs/{id}/activate", method = RequestMethod.PUT,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public Lab activate(Principal principal, @PathVariable Long id, @RequestBody Lab body) {
        log.info("PUT /admin/labs/" + body.getNumber());
        Lab lab = labService.findOne(id);
        if (lab == null) {
            throw new LabNotFound();
        }
        lab.activate();
        return  labService.save(lab);
    }

    @RequestMapping(value = "/admin/labs/{id}/deactivate", method = RequestMethod.PUT,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public Lab deactivate(Principal principal, @PathVariable Long id, @RequestBody Lab body) {
        log.info("PUT /admin/labs/" + body.getNumber());
        Lab lab = labService.findOne(id);
        if (lab == null) {
            throw new LabNotFound();
        }
        lab.deactivate();
        return  labService.save(lab);
    }

    @RequestMapping(value = "/lab", method = RequestMethod.GET)
    public Lab getLab(UserAuthenticationToken token) {
        log.info("GET /lab");
        User user = token.getUser();
        if (user == null || !user.isLabUser()) {
            throw new UserUnauthorised("User not authorised to fetch lab information.");
        }
        if (user.getLab() == null) {
            throw new LabuserWithoutLab("No lab associated with lab user.");
        }
        return labService.findOne(user.getLab().getId());
    }

    @RequestMapping(value = "/lab/hubusers", method = RequestMethod.GET)
    public List<ProfileRepresentation> getLabHubUsers(UserAuthenticationToken token) {
        log.info("GET /lab/hubusers");
        User user = token.getUser();
        if (user == null || !user.isLabUser()) {
            throw new UserUnauthorised("User not authorised to fetch lab information.");
        }
        if (user.getLab() == null) {
            throw new LabuserWithoutLab("No lab associated with lab user.");
        }
        Lab lab = labService.findOne(user.getLab().getId());
        List<ProfileRepresentation> hubUsers = new ArrayList<>();
        for(User u: userService.findHubUsersForLab(lab)) {
            hubUsers.add(new ProfileRepresentation(u));
        }
        return hubUsers;
    }

    @PreAuthorize("isAuthenticated() and hasRole('lab_user')")
    @RequestMapping(value = "/lab", method = RequestMethod.PUT)
    public Lab updateLab(UserAuthenticationToken token, @RequestBody Lab body) {
        log.info("PUT /lab");
        User user = token.getUser();
        if (user != null && user.isLabUser()) {
            if (user.getLab() == null) {
                throw new LabuserWithoutLab("No lab associated with lab user.");
            }
        }
        Lab lab = labService.findOne(user.getLab().getId());
        if (lab == null) {
            throw new LabNotFound();
        }
        // Copy values. The lab number cannot be changed.
        transferLabData(body, lab);
        // Set hub assistance property (only when edited by a lab user).
        lab.setHubAssistanceEnabled(body.isHubAssistanceEnabled());
        // TODO: if !isHubAssistanceEnabled(), perhaps set 'hubAssistanceRequested' to false
        // for all associated lab requests.
        return labService.save(lab);
    }

    @PreAuthorize("isAuthenticated() and hasRole('palga')")
    @RequestMapping(value = "/lab/fixLabEmailAddresses", method = RequestMethod.PUT)
    public void fixLabEmailAddresses() {
        log.info("PUT /lab/fixLabEmailAddresses");
        labService.fixLabEmailAddresses();
    }

}
