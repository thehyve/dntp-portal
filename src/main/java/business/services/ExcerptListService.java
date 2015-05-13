package business.services;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Writer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

@Service
public class ExcerptListService {

    Log log = LogFactory.getLog(getClass());
    
    public ExcerptList processExcerptList(MultipartFile file) {
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
                    list.addEntry(nextLine);
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
    
    public HttpEntity<InputStreamResource> writeExcerptList(ExcerptList list, String id) {
        ByteArrayOutputStream out = new ByteArrayOutputStream(); 
        Writer writer = new PrintWriter(out);
        CSVWriter csvwriter = new CSVWriter(writer, ';', '"');
        csvwriter.writeNext(list.getCsvColumnNames());
        for (ExcerptEntry entry: list.getEntries()) {
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
                       "attachment; filename=excerpts_" + id + ".csv");
            HttpEntity<InputStreamResource> response =  new HttpEntity<InputStreamResource>(resource, headers);
            log.info("Returning reponse.");
            return response;
        } catch (IOException e) {
            throw new ExcerptListDownloadError();
        }

    }

}
