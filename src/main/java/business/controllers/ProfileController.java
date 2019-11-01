/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business.controllers;

import business.models.Lab;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import business.models.ContactData;
import business.models.User;
import business.representation.ProfileRepresentation;
import business.security.UserAuthenticationToken;
import business.services.LabService;
import business.services.UserService;

import java.util.List;

@RestController
public class ProfileController {

    Log log = LogFactory.getLog(getClass());

    @Autowired
    UserService userService;

    @Autowired
    LabService labService;

    @RequestMapping(value = "/api/profile", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ProfileRepresentation getOwnProfile(UserAuthenticationToken user) {
        // Query user's profile
        log.info("GET profile for user with id " + user.getId());

        // Return the representation
        return new ProfileRepresentation(userService.findOne(user.getId()));
    }

    @RequestMapping(value = "/api/hublabs", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Lab> getProfileHubLabs(UserAuthenticationToken user) {
        // Query user's profile
        log.info("GET labs profile for user with id " + user.getId());

        ProfileRepresentation profileRepresentation = new ProfileRepresentation(userService.findOne(user.getId()));

        // Return the representation
        return labService.findAll(profileRepresentation.getHubLabIds());
    }

    @RequestMapping(value = "/api/profile", method = RequestMethod.PUT)
    public void putOwnProfile(UserAuthenticationToken user, @RequestBody ProfileRepresentation form) {
        log.info("PUT profile for user with id " + user.getId());

        if (form == null) {
            log.warn("form is null!");
            return;
        }

        // Get user
        User currentUser = userService.getOne(user.getId());

        // Update
        ContactData cData = currentUser.getContactData();

        if (cData == null) {
            cData = new ContactData();
            currentUser.setContactData(cData);
        }

        // Only update the telephone number
        ContactData modifiedData = form.getContactData();
        if (modifiedData == null) {
            log.warn("new contact data is null!");
        } else {
            cData.setTelephone(modifiedData.getTelephone());
        }

        if (currentUser.isRequester()) {
            String specialism = currentUser.getSpecialism();
            // update specialism for any changes
            if (form.getSpecialism() != null && !form.getSpecialism().equals(specialism)) {
                currentUser.setSpecialism(form.getSpecialism());
            }
        }

        // Save
        userService.save(currentUser);
    }
}
