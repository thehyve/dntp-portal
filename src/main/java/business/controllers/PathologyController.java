/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business.controllers;

import business.exceptions.PathologyNotFound;
import business.models.LabRequest;
import business.models.PathologyItem;
import business.models.PathologyItemRepository;
import business.representation.*;
import business.security.UserAuthenticationToken;
import business.services.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
public class PathologyController {

    Log log = LogFactory.getLog(getClass());

    @Autowired
    private LabRequestService labRequestService;

    @Autowired
    private LabRequestQueryService labRequestQueryService;

    @Autowired
    private PathologyItemRepository pathologyItemRepository;

    @PreAuthorize("isAuthenticated() and hasPermission(#id, 'labRequestAssignedToUser')")
    @RequestMapping (value = "/api/labrequests/{id}/pathology", method = RequestMethod.POST)
    public PathologyRepresentation addPathology (UserAuthenticationToken user,
            @PathVariable Long id,
            @RequestBody PathologyRepresentation body) {
        log.info("POST /api/labrequests/" + id + "/pathology for userId " + user.getId());
        LabRequest labRequest = labRequestQueryService.findOne(id);

        PathologyItem pathology = new PathologyItem();
        pathology.setLabRequestId(id);
        pathology.setPaNumber(body.getPaNumber());
        pathology.setSamples(body.getSamples());
        pathology.setSamplesAvailable(body.isSamplesAvailable());
        pathology = pathologyItemRepository.save(pathology);

        labRequest.getPathologyList().add(pathology);
        labRequestService.save(labRequest);

        PathologyRepresentation result = new PathologyRepresentation(pathology);
        result.mapSamples(pathology);
        return result;
    }

    @PreAuthorize("isAuthenticated() and hasPermission(#id, 'labRequestAssignedToUser')")
    @RequestMapping (value = "/api/labrequests/{id}/pathology/{pathologyId}", method = RequestMethod.DELETE)
    public void removePathology (UserAuthenticationToken user,
            @PathVariable Long id,
            @PathVariable Long pathologyId) {
        log.info("PUT /api/labrequests/" + id + "/pathology/ " + pathologyId + " for userId " + user.getId());
        LabRequest labRequest = labRequestQueryService.findOne(id);

        PathologyItem pathology = pathologyItemRepository.findOne(pathologyId);
        if (pathology == null) {
            throw new PathologyNotFound();
        }
        if (!labRequest.getPathologyList().remove(pathology)) {
            throw new PathologyNotFound();
        }
        labRequestService.save(labRequest);

        pathologyItemRepository.delete(pathologyId);
    }

    @PreAuthorize("isAuthenticated() and hasPermission(#id, 'labRequestAssignedToUser')")
    @RequestMapping (value = "/api/labrequests/{id}/pathology/{pathologyId}", method = RequestMethod.PUT)
    public PathologyRepresentation updatePathology (UserAuthenticationToken user,
            @PathVariable Long id,
            @PathVariable Long pathologyId,
            @RequestBody PathologyRepresentation body) {
        log.info("PUT /api/labrequests/" + id + "/pathology/ " + pathologyId + " for userId " + user.getId());
        LabRequest labRequest = labRequestQueryService.findOne(id);

        PathologyItem pathology = pathologyItemRepository.findOne(pathologyId);
        if (pathology == null) {
            throw new PathologyNotFound();
        }
        if (!labRequest.getPathologyList().contains(pathology)) {
            throw new PathologyNotFound();
        }
        pathology.setSamplesAvailable(Boolean.TRUE.equals(body.isSamplesAvailable()));
        pathology.setSamples(body.getSamples());
        pathology = pathologyItemRepository.save(pathology);

        PathologyRepresentation result = new PathologyRepresentation(pathology);
        result.mapSamples(pathology);
        return result;
    }

}
