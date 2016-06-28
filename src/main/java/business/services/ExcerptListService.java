/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business.services;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.transaction.Transactional;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.task.DelegationState;
import org.activiti.engine.task.Task;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import business.exceptions.ExcerptListDownloadError;
import business.exceptions.ExcerptListNotFound;
import business.exceptions.ExcerptListUploadError;
import business.exceptions.ExcerptSelectionUploadError;
import business.exceptions.FileDeleteError;
import business.exceptions.FileUploadError;
import business.exceptions.RequestNotFound;
import business.models.ExcerptEntry;
import business.models.ExcerptEntryRepository;
import business.models.ExcerptList;
import business.models.ExcerptListRepository;
import business.models.File;
import business.models.Lab;
import business.models.RequestProperties;
import business.representation.ExcerptEntryRepresentation;
import business.representation.ExcerptListRepresentation;
import business.representation.RequestRepresentation;

@Service
public class ExcerptListService {

    public static final String EXCERPT_LIST_CHARACTER_ENCODING = "ISO-8859-1";

    Log log = LogFactory.getLog(getClass());

    @Autowired LabService labService;

    @Autowired ExcerptListRepository excerptListRepository;

    @Autowired ExcerptEntryRepository excerptEntryRepository;

    @Autowired RuntimeService runtimeService;

    @Autowired RequestService requestService;

    @Autowired RequestPropertiesService requestPropertiesService;

    @Autowired TaskService taskService;

    @Autowired LabRequestService labRequestService;

    @Autowired FileService fileService;

    @Transactional
    public ExcerptList findByProcessInstanceId(String processInstanceId) {
        ExcerptList excerptList = excerptListRepository.findByProcessInstanceId(processInstanceId);
        return excerptList;
    }

    @Transactional
    public ExcerptListRepresentation findRepresentationByProcessInstanceId(String processInstanceId) {
        ExcerptList excerptList = excerptListRepository.findByProcessInstanceId(processInstanceId);
        if (excerptList == null) {
            return null;
        }
        ExcerptListRepresentation result = new ExcerptListRepresentation(excerptList);
        List<ExcerptEntry> list = excerptList.getEntries();
        result.setEntryList(list);
        return result;
    }

    @Cacheable("excerptlistexists")
    public Boolean hasExcerptList(String processInstanceId) {
        return excerptListRepository.findByProcessInstanceId(processInstanceId) != null;
    }

    public Integer countEntriesByExcerptListId(Long excerptListId) {
        return excerptEntryRepository.countByExcerptListId(excerptListId);
    }

    public Integer countSelectedEntriesByExcerptListId(Long excerptListId) {
        return excerptEntryRepository.countBySelectedTrueAndExcerptListId(excerptListId);
    }

    @Transactional
    public void deleteByProcessInstanceId(String processInstanceId) {
        ExcerptList excerptList = findByProcessInstanceId(processInstanceId);
        if (excerptList != null) {
            Long excerptListId = excerptList.getId();
            log.info("Deleting excerpt entries...");
            excerptEntryRepository.deleteAllByExcerptListId(excerptListId);
            log.info("Deleting excerpt list...");
            excerptListRepository.delete(excerptListId);
            log.info("Done deleting.");
        }
    }

    @CacheEvict(value = "excerptlistexists", key = "#list.processInstanceId")
    @Transactional
    public ExcerptList save(ExcerptList list) {
        return excerptListRepository.save(list);
    }

    @Transactional
    public ExcerptListRepresentation updateExcerptListSelection(String processInstanceId, ExcerptListRepresentation body) {
        ExcerptList excerptList = findByProcessInstanceId(processInstanceId);
        if (excerptList == null) {
            throw new RequestNotFound();
        }
        for (ExcerptEntryRepresentation entry: body.getEntries()) {
            ExcerptEntry excerptEntry = excerptList.getEntries().get(entry.getSequenceNumber()-1);
            if (entry.getId().equals(excerptEntry.getId())) {
                // indeed the same entry
                excerptEntry.setSelected(entry.isSelected());
            }
        }
        excerptList = save(excerptList);
        ExcerptListRepresentation result = new ExcerptListRepresentation(excerptList);
        List<ExcerptEntry> list = excerptList.getEntries();
        result.setEntryList(list);
        return result;
    }

    @CacheEvict(value = "excerptlistexists", key = "#list.processInstanceId")
    @Transactional
    public ExcerptList processExcerptList(ExcerptList list, InputStream input) {
        Set<Integer> validLabNumbers = new TreeSet<Integer>();
        log.info("Processing excerpt list");
        try {
            CSVReader reader = new CSVReader(new InputStreamReader(input, EXCERPT_LIST_CHARACTER_ENCODING), ';', '"');
            String [] nextLine;
            log.debug("Column names.");
            if ((nextLine = reader.readNext()) != null) {
                try {
                    list.setColumnNames(nextLine);
                } catch (RuntimeException e) {
                    log.error("Error while setting column names: " + e.getMessage());
                    reader.close();
                    throw new ExcerptListUploadError(e.getMessage());
                }
            }
            int line = 2;
            while ((nextLine = reader.readNext()) != null) {
                log.debug("Line " + line);
                try {
                    ExcerptEntry entry = list.addEntry(nextLine);
                    // check lab number
                    if (!validLabNumbers.contains(entry.getLabNumber())) {
                        Lab lab = labService.findByNumber(entry.getLabNumber());
                        if (lab == null) {
                            throw new ExcerptListUploadError(
                                    "Lab not found: " + entry.getLabNumber());
                        } else {
                            validLabNumbers.add(entry.getLabNumber());
                        }
                    }
                    
                } catch (RuntimeException e) {
                    log.error("Error while processing line " + line + ": " + e.getMessage());
                    reader.close();
                    throw new ExcerptListUploadError("Line " + line + ": " + e.getMessage());
                }
                line++;
            }
            reader.close();
            log.info("Added " + list.getEntries().size() + " entries.");
            list = excerptListRepository.save(list);
            return list;
        } catch(IOException e) {
            throw new FileUploadError(e.getMessage());
        }
    }

    @Transactional
    public List<Integer> processExcerptSelection(InputStream input) {
        List<Integer> result = new ArrayList<Integer>();
        log.info("Processing excerpt selection");
        try {
            CSVReader reader = new CSVReader(new InputStreamReader(input), ';', '"');
            String [] nextLine;
            log.debug("Column names.");
            nextLine = reader.readNext();
            if (nextLine == null || nextLine.length == 0 || !nextLine[0].equals("Sequence number")) {
                reader.close();
                throw new ExcerptSelectionUploadError("Invalid header");
            }
            int line = 2;
            while ((nextLine = reader.readNext()) != null) {
                log.debug("Line " + line);
                if (nextLine != null || nextLine.length > 0) {
                    try {
                        Integer selected = Integer.valueOf(nextLine[0]);
                        if (result.contains(selected)) {
                            log.warn("Number already selected before: " + selected + " (line " + line + ")");
                        }
                        if (selected == null) {
                            log.warn("Number null (line " + line + ")");
                        } else {
                            result.add(selected);
                        }
                    } catch (NumberFormatException e) {
                        log.warn("Invalid number at line " + line);
                    }
                }
                line++;
            }
            reader.close();
            log.info("Selected " + result.size() + " entries.");
            return result;
        } catch(IOException e) {
            throw new FileUploadError(e.getMessage());
        }
    }
    
    /**
     * Write the excerpt list as a file.
     * @param list - the list
     * @param selectedOnly - writes only selected excerpts if true; all excerpts otherwise.
     * @return the resource holding selected excerpts or all (depends on the value of {@value selected}
     * in CSV format.
     */
    public HttpEntity<InputStreamResource> writeExcerptList(ExcerptList list, boolean selectedOnly) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Writer writer = new OutputStreamWriter(out, EXCERPT_LIST_CHARACTER_ENCODING);
            CSVWriter csvwriter = new CSVWriter(writer, ';', '"');
            csvwriter.writeNext(list.getCsvColumnNames());
            for (ExcerptEntry entry: list.getEntries()) {
                if (!selectedOnly || entry.isSelected()) {
                    csvwriter.writeNext(entry.getCsvValues());
                }
            }
            csvwriter.flush();
            writer.flush();
            out.flush();
            InputStream in = new ByteArrayInputStream(out.toByteArray());
            csvwriter.close();
            writer.close();
            out.close();
            InputStreamResource resource = new InputStreamResource(in);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.valueOf("text/csv;charset=" + EXCERPT_LIST_CHARACTER_ENCODING));
            String filename = (selectedOnly ? "selection" : "excerpts")
                                + "_" + list.getProcessInstanceId() + ".csv";
            headers.set("Content-Disposition",
                       "attachment; filename=" + filename);
            HttpEntity<InputStreamResource> response =  new HttpEntity<InputStreamResource>(resource, headers);
            log.info("Returning reponse.");
            return response;
        } catch (IOException e) {
            log.error(e.getStackTrace());
            log.error(e.getMessage());
            throw new ExcerptListDownloadError();
        }
    }

    @Transactional
    public HttpEntity<InputStreamResource> writeExcerptList(String id,
            boolean selectedOnly) {
        ExcerptList excerptList = excerptListRepository.findByProcessInstanceId(id);
        if (excerptList == null) {
            throw new ExcerptListNotFound();
        }
        return writeExcerptList(excerptList, selectedOnly);
    }

    @Transactional
    public void setExcerptSelectionApproval(String id,
            RequestRepresentation body) {
        // Set approval
        runtimeService.setVariable(id, "selection_approved", body.isSelectionApproved());

        if (body.isSelectionApproved()) {
            // set lab numbers for creating lab requests.
            ExcerptList excerptList = findByProcessInstanceId(id);
            if (excerptList == null) {
                throw new RequestNotFound();
            }
            Set<Integer> selectedLabNumbers = new TreeSet<Integer>();
            for(ExcerptEntry entry: excerptList.getEntries()) {
                if (entry.isSelected()) {
                    selectedLabNumbers.add(entry.getLabNumber());
                }
            }
            runtimeService.setVariable(id, "lab_request_labs", selectedLabNumbers);
        }

        Task task = requestService.getTaskByRequestId(id, "selection_review");
        if (task.getDelegationState()==DelegationState.PENDING) {
            taskService.resolveTask(task.getId());
        }
        taskService.complete(task.getId());

        // generate lab requests
        if (body.isSelectionApproved()) {
            labRequestService.generateLabRequests(id);
        }
    }

    @CacheEvict(value = "excerptlistexists", key = "#processInstanceId")
    @Transactional
    public Long replaceExcerptList(String processInstanceId, File attachment) {
        RequestProperties properties = requestPropertiesService.findByProcessInstanceId(processInstanceId);

        // store existing excerpt list attachment, to be removed later
        File toBeRemoved = properties.getExcerptListAttachment();
        // delete existing excerpt list
        deleteByProcessInstanceId(properties.getProcessInstanceId());
        {
            ExcerptList list = findByProcessInstanceId(processInstanceId);
            assert(list == null);
        }

        // process new list
        try {
            InputStream input = fileService.getInputStream(attachment);
            ExcerptList list = new ExcerptList();
            list.setProcessInstanceId(properties.getProcessInstanceId());
            list.setPropertiesId(properties.getId());
            list = save(list);
            list = processExcerptList(list, input);
            try {
                input.close();
            } catch (IOException e) {
                log.error("Error while closing input stream: " + e.getMessage());
            }
            log.info("Saving excerpt list.");
            list = save(list);
            // remove existing excerpt list attachment
            try {
                if (toBeRemoved != null) {
                    log.info("Removing previous excerpt list attachment.");
                    fileService.removeAttachment(toBeRemoved);
                }
            } catch (FileDeleteError e) {
                log.error("Error deleting file: " + e.getMessage());
            }
            log.info("Saving excerpt list attachment.");
            properties.setExcerptListAttachment(attachment);
            properties = requestPropertiesService.save(properties);
            log.info("Done.");
            return list.getId();
        } catch (RuntimeException e) {
            // revert uploading
            fileService.removeAttachment(attachment);
            throw e;
        }
    }

}
