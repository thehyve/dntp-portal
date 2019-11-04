/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business.controllers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import business.representation.EmailRepresentation;
import business.representation.NewPasswordRepresentation;
import business.representation.PasswordChangeRepresentation;
import business.security.UserAuthenticationToken;
import business.services.MailService;
import business.services.PasswordService;
import business.services.UserService;

@RestController
public class PasswordController {

    Log log = LogFactory.getLog(getClass());

    @Autowired
    UserService userService;

    @Autowired
    MailService mailService;

    @Autowired
    PasswordService passwordService;

    @RequestMapping(value = "/api/password/request-new", method = RequestMethod.PUT)
    public void requestNewPassword(@RequestBody EmailRepresentation body) {
        String email = body.getEmail() == null ? "" : body.getEmail().trim().toLowerCase();
        log.info("PUT /api/password/request-new/" + email);

        passwordService.requestNewPassword(email);
    }

    @RequestMapping(value = "/api/password/reset", method = RequestMethod.POST)
    public void setPassword(@RequestBody NewPasswordRepresentation body) {
        log.info("POST /api/password/reset");

        passwordService.resetPassword(body);
    }

    @RequestMapping(value = "/api/password/change", method = RequestMethod.POST)
    public void changePassword(UserAuthenticationToken user, @RequestBody PasswordChangeRepresentation body) {
        log.info("POST /api/password/change");

        passwordService.updatePassword(user.getUser(), body);
    }
}
