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
import java.io.PrintWriter;
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
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import business.exceptions.ExcerptListDownloadError;
import business.exceptions.ExcerptListNotFound;
import business.exceptions.ExcerptListUploadError;
import business.exceptions.ExcerptSelectionUploadError;
import business.exceptions.FileUploadError;
import business.exceptions.RequestNotFound;
import business.models.ExcerptEntry;
import business.models.ExcerptList;
import business.models.ExcerptListRepository;
import business.models.Lab;
import business.models.LabRepository;
import business.representation.ExcerptEntryRepresentation;
import business.representation.ExcerptListRepresentation;
import business.representation.RequestRepresentation;

@Service
public class ExcerptListService {

    Log log = LogFactory.getLog(getClass());
    
    @Autowired LabRepository labRepository;
    
    @Autowired ExcerptListRepository excerptListRepository;

    @Autowired RuntimeService runtimeService;

    @Autowired RequestService requestService;

    @Autowired TaskService taskService;

    @Autowired LabRequestService labRequestService;


    @Transactional
    public ExcerptList findByProcessInstanceId(String processInstanceId) {
        ExcerptList excerptList = excerptListRepository.findByProcessInstanceId(processInstanceId);
        return excerptList;
    }
    
    @Transactional
    public ExcerptListRepresentation findRepresentationByProcessInstanceId(String processInstanceId) {
        ExcerptList excerptList = excerptListRepository.findByProcessInstanceId(processInstanceId);
        ExcerptListRepresentation result = new ExcerptListRepresentation(excerptList);
        List<ExcerptEntry> list = excerptList.getEntries();
        result.setEntryList(list);
        return result;
    }
    
    @Transactional
    public void deleteByProcessInstanceId(String processInstanceId) {
        excerptListRepository.deleteByProcessInstanceId(processInstanceId);
    }
    
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

    public ExcerptList processExcerptList(ExcerptList list, InputStream input) {
        Set<Integer> validLabNumbers = new TreeSet<Integer>();
        log.info("Processing excerpt list");
        try {
            CSVReader reader = new CSVReader(new InputStreamReader(input), ';', '"');
            String [] nextLine;
            log.info("Column names.");
            if ((nextLine = reader.readNext()) != null) {
                try {
                    list.setColumnNames(nextLine);
                } catch (RuntimeException e) {
                    reader.close();
                    throw new ExcerptListUploadError(e.getMessage());
                } 
            }
            int line = 2;
            while ((nextLine = reader.readNext()) != null) {
                log.info("Line " + line);
                try {
                    ExcerptEntry entry = list.addEntry(nextLine);
                    // check lab number
                    if (!validLabNumbers.contains(entry.getLabNumber())) {
                        Lab lab = labRepository.findByNumber(entry.getLabNumber());
                        if (lab == null) {
                            throw new ExcerptListUploadError(
                                    "Lab not found: " + entry.getLabNumber());
                        } else {
                            validLabNumbers.add(entry.getLabNumber());
                        }
                    }
                    
                } catch (RuntimeException e) {
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
    
    public List<Integer> processExcerptSelection(InputStream input) {
        List<Integer> result = new ArrayList<Integer>();
        log.info("Processing excerpt selection");
        try {
            CSVReader reader = new CSVReader(new InputStreamReader(input), ';', '"');
            String [] nextLine;
            log.info("Column names.");
            nextLine = reader.readNext();
            if (nextLine == null || nextLine.length == 0 || !nextLine[0].equals("Sequence number")) {
                reader.close();
                throw new ExcerptSelectionUploadError("Invalid header");
            }
            int line = 2;
            while ((nextLine = reader.readNext()) != null) {
                log.info("Line " + line);
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
        ByteArrayOutputStream out = new ByteArrayOutputStream(); 
        Writer writer = new PrintWriter(out);
        CSVWriter csvwriter = new CSVWriter(writer, ';', '"');
        csvwriter.writeNext(list.getCsvColumnNames());
        for (ExcerptEntry entry: list.getEntries()) {
            if (!selectedOnly || entry.isSelected()) {
                csvwriter.writeNext(entry.getCsvValues());
            }
        }
        try {
            csvwriter.flush();
            csvwriter.close();
            writer.flush();
            writer.close();
            out.flush();
            InputStream in = new ByteArrayInputStream(out.toByteArray());
            out.close();
            InputStreamResource resource = new InputStreamResource(in);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.valueOf("text/csv"));
            String filename = (selectedOnly ? "selection" : "excerpts")
                                + "_" + list.getProcessInstanceId() + ".csv";
            headers.set("Content-Disposition",
                       "attachment; filename=" + filename);
            HttpEntity<InputStreamResource> response =  new HttpEntity<InputStreamResource>(resource, headers);
            log.info("Returning reponse.");
            return response;
        } catch (IOException e) {
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

}
