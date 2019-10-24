/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business.controllers;

import business.representation.LabRepresentation;
import business.services.LabService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class LabProfileController {

    Log log = LogFactory.getLog(getClass());

    @Autowired
    LabService labService;

    @RequestMapping(value = "/api/public/labs", method = RequestMethod.GET)
    public List<LabRepresentation> get() {
        log.info("GET /api/public/labs/ for anonymous user");
        return labService.findAllActive();
    }

    @RequestMapping(value = "/api/public/labs/{id}", method = RequestMethod.GET)
    public LabRepresentation get(@PathVariable("id") long id) {
        log.info("GET /api/public/labs/" + id + " for anonymous user");
        return labService.findOneActive(id);
    }

}
