package business.services;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Set;
import java.util.TreeSet;

import javax.transaction.Transactional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import business.exceptions.ExcerptListDownloadError;
import business.exceptions.ExcerptListUploadError;
import business.exceptions.FileUploadError;
import business.models.ExcerptEntry;
import business.models.ExcerptList;
import business.models.ExcerptListRepository;
import business.models.Lab;
import business.models.LabRepository;
import business.representation.ExcerptListRepresentation;

@Service
public class ExcerptListService {

    Log log = LogFactory.getLog(getClass());
    
    @Autowired LabRepository labRepository;
    
    @Autowired ExcerptListRepository excerptListRepository;
    
    @Transactional
    public ExcerptList findByProcessInstanceId(String processInstanceId) {
        ExcerptList excerptList = excerptListRepository.findByProcessInstanceId(processInstanceId);
        return excerptList;
    }
    
    @Transactional
    public ExcerptListRepresentation findRepresentationByProcessInstanceId(String processInstanceId) {
        ExcerptList excerptList = excerptListRepository.findByProcessInstanceId(processInstanceId);
        return new ExcerptListRepresentation(excerptList);
    }
    
    @Transactional
    public void deleteByProcessInstanceId(String processInstanceId) {
        excerptListRepository.deleteByProcessInstanceId(processInstanceId);
    }
    
    @Transactional
    public ExcerptList save(ExcerptList list) {
        return excerptListRepository.save(list);
    }
    
    public ExcerptList processExcerptList(MultipartFile file) {
        Set<Integer> validLabNumbers = new TreeSet<Integer>();
        log.info("Processing excerpt list");
        try {
            CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()), ';', '"');
            ExcerptList list = new ExcerptList();
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
            return list;
        } catch(IOException e) {
            throw new FileUploadError(e.getMessage());
        }
    }
    
    public HttpEntity<InputStreamResource> writeExcerptList(ExcerptList list) {
        ByteArrayOutputStream out = new ByteArrayOutputStream(); 
        Writer writer = new PrintWriter(out);
        CSVWriter csvwriter = new CSVWriter(writer, ';', '"');
        csvwriter.writeNext(list.getCsvColumnNames());
        for (ExcerptEntry entry: list.getEntryValues()) {
            csvwriter.writeNext(entry.getCsvValues());
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
            headers.set("Content-Disposition",
                       "attachment; filename=excerpts_" + list.getProcessInstanceId() + ".csv");
            HttpEntity<InputStreamResource> response =  new HttpEntity<InputStreamResource>(resource, headers);
            log.info("Returning reponse.");
            return response;
        } catch (IOException e) {
            throw new ExcerptListDownloadError();
        }

    }

}
