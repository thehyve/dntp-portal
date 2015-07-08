package business.controllers;

import business.models.File;
import business.models.FileRepository;
import business.representation.FileRepresentation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import business.security.UserAuthenticationToken;
import business.services.FileService;

import java.util.ArrayList;
import java.util.List;

/**
 * File Routes
 */
@RestController
public class FileController {

    Log log = LogFactory.getLog(getClass());
    
    @Autowired
    FileService fileService;

    @Autowired
    FileRepository fileRepository;

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/files", method = RequestMethod.GET)
    public List<FileRepresentation> getFiles(
            UserAuthenticationToken user) {
        log.info("GET /files/");
        List<FileRepresentation> availableFiles = new ArrayList<>();
        List<File> files = fileRepository.findAll();

        for (File file : files) {
            FileRepresentation fileRepresentation = new FileRepresentation(file);
            availableFiles.add(fileRepresentation);
        }

        return availableFiles;
    }

    /*
     * File download
     */
    // FIXME
    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/files/{id}/download", method = RequestMethod.GET)
    public HttpEntity<InputStreamResource> downloadFile(
            UserAuthenticationToken user, 
            @PathVariable Long id) {
        log.info("GET /files/" + id);
        return fileService.download(id);
    }

    /*
     * Get files by type
     */
    // FIXME: security
    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/files/{type}", method = RequestMethod.GET)
    public List<FileRepresentation> getFilesByType(
            UserAuthenticationToken user,
            @PathVariable File.AttachmentType type) {
        log.info("GET /files/"+type);
        List<FileRepresentation> availableFiles = new ArrayList<>();
        List<File> files = fileRepository.findByType(type);

        for (File file : files) {
            FileRepresentation fileRepresentation = new FileRepresentation(file);
            availableFiles.add(fileRepresentation);
        }

        return availableFiles;
    }

}
