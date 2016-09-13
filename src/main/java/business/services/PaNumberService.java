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
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import business.models.PathologyItem;
import business.representation.LabRequestRepresentation;
import business.representation.PathologyRepresentation;

import com.opencsv.CSVWriter;

@Service
public class PaNumberService {

    public static final String PA_NUMBERS_DOWNLOAD_CHARACTER_ENCODING = "UTF-8";

    Log log = LogFactory.getLog(getClass());

    static final String[] FILE_HEADER = "LAB_NO;PA_NUMBER;SAMPLES;NOTES;EXTRA".split(";");

    public HttpEntity<InputStreamResource> writePaNumbers(List<PathologyItem> items, Integer labNumber,
            String labRequestCode) throws Exception {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Writer writer = new OutputStreamWriter(out, PA_NUMBERS_DOWNLOAD_CHARACTER_ENCODING);
        CSVWriter csvwriter = new CSVWriter(writer, ';', '"');

        csvwriter.writeNext(FILE_HEADER);

        for (PathologyItem item : items) {
            log.info(item.getPaNumber());
            String[] toppings = { labNumber.toString(), item.getPaNumber(), "", "", item.getRemark() };
            csvwriter.writeNext(toppings);
        }

        String filename = "panumbers_" + labRequestCode + ".csv";

        try {
            csvwriter.flush();
            writer.flush();
            out.flush();
            InputStream in = new ByteArrayInputStream(out.toByteArray());
            csvwriter.close();
            writer.close();
            out.close();
            InputStreamResource resource = new InputStreamResource(in);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.valueOf("text/csv;charset=" + PA_NUMBERS_DOWNLOAD_CHARACTER_ENCODING));
            headers.set("Content-Disposition", "attachment; filename=\"" + filename + "\"");
            HttpEntity<InputStreamResource> response = new HttpEntity<InputStreamResource>(resource, headers);
            return response;
        } catch (IOException e) {
            throw new Exception(e);
        }
    }

    static final String[] PA_NUMBERS_HEADER = new String[]{
       "Request number",
       "Status",
       "PA number",
       "Laboratory",
       "Requester name",
       "Requester email",
       "Requester telephone number",
       "Sent date"
    };

    public HttpEntity<InputStreamResource> writeAllPaNumbers(
            List<LabRequestRepresentation> labRequests) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Writer writer = new OutputStreamWriter(out, PA_NUMBERS_DOWNLOAD_CHARACTER_ENCODING);
        CSVWriter csvwriter = new CSVWriter(writer, ',', '"');
        csvwriter.writeNext(PA_NUMBERS_HEADER);
        for (LabRequestRepresentation labRequest: labRequests) {
            String labRequestCode = labRequest.getLabRequestCode();
            String status = labRequest.getStatus().toString();
            String labName = labRequest.getLab().getName();
            String requesterName = labRequest.getRequesterName();
            String requesterEmail = labRequest.getRequesterEmail();
            String requesterTelephone = labRequest.getRequesterTelephone();
            String labRequestSentDate = labRequest.getSendDate() == null ? "" : labRequest.getSendDate().toString();
            for (PathologyRepresentation item: labRequest.getPathologyList()) {
                csvwriter.writeNext(new String[] {
                    labRequestCode,
                    status,
                    item.getPaNumber(),
                    labName,
                    requesterName,
                    requesterEmail,
                    requesterTelephone,
                    labRequestSentDate
                });
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
        headers.setContentType(MediaType.valueOf("text/csv;charset=" + PA_NUMBERS_DOWNLOAD_CHARACTER_ENCODING));
        String filename = "pa_numbers.csv";
        headers.set("Content-Disposition",
                   "attachment; filename=" + filename);
        HttpEntity<InputStreamResource> response = new HttpEntity<InputStreamResource>(resource, headers);
        return response;
    }

}
