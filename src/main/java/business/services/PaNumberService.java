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
import java.io.PrintWriter;
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
import com.opencsv.CSVWriter;


@Service
public class PaNumberService {

  Log log = LogFactory.getLog(getClass());

  String[] FILE_HEADER = "LAB_NO;PA_NUMBER;SAMPLES;NOTES".split(";");

  public HttpEntity<InputStreamResource> writePaNumbers(List<PathologyItem> items, String labNo) throws Exception {

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    Writer writer = new PrintWriter(out);
    CSVWriter csvwriter = new CSVWriter(writer, ';', '"');


    csvwriter.writeNext(FILE_HEADER);

    for (PathologyItem item : items) {
        log.info(item.getPaNumber());
        String[] toppings = {labNo, item.getPaNumber(), "", ""};
        csvwriter.writeNext(toppings);
    }

    String filename = "panumber_" + labNo + ".csv";

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
          "attachment; filename=\"" + filename + "\"");
        HttpEntity<InputStreamResource> response = new HttpEntity<InputStreamResource>(resource, headers);
        return response;
    } catch (IOException e) {
        throw new Exception(e);
    }
  }
}
