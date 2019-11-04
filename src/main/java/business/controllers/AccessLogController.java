/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business.controllers;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import business.services.FileService;

@RestController
public class AccessLogController {

    Log log = LogFactory.getLog(getClass());

    @Autowired FileService fileService;

    @RequestMapping(value = "/api/admin/accesslogs", method = RequestMethod.GET)
    public List<String> getAccessLogs() {
        log.info("GET /api/admin/accesslogs");

        return fileService.getAccessLogFilenames();
    }

    @RequestMapping(value = "/api/admin/accesslogs/{name:.+}", method = RequestMethod.GET)
    public HttpEntity<InputStreamResource> getAccessLog(@PathVariable String name) {
        log.info("GET /api/admin/accesslogs/" + name);

        return fileService.downloadAccessLog(name, false);
    }

    @RequestMapping(value = "/api/admin/accesslogs/{name:.+}/download", method = RequestMethod.GET)
    public HttpEntity<InputStreamResource> downloadAccessLog(@PathVariable String name) {
        log.info("GET /api/admin/accesslogs/" + name + "/download");

        return fileService.downloadAccessLog(name, true);
    }

}
