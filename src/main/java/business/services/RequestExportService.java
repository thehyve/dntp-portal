/*
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business.services;

import business.exceptions.FileDownloadError;
import business.representation.RequestRepresentation;
import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.format.datetime.DateFormatter;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class RequestExportService {

    private static final String CSV_CHARACTER_ENCODING = "UTF-8";

    private static final String[] CSV_COLUMN_NAMES = {
            "Request number",
            "Date created",
            "Date submitted",
            "Date delivered",
            "Title",
            "Status",
            "Linkage",
            "Linkage notes",
            "Numbers only",
            "Excerpts",
            "PA reports",
            "PA material",
            "Clinical data",
            "Requester name",
            "Requester lab",
            "Specialism",
            "Grant provider",
            "Review PPC",
            "Review result",
            "Explanation for PPC",
            "Summary review process",
            "# Hub assistance lab requests",
            "Pathologist"
    };

    private final static Locale LOCALE = Locale.getDefault();

    private final static DateFormatter DATE_FORMATTER = new DateFormatter("yyyy-MM-dd");

    static String booleanToString(Boolean value) {
        if (value == null) return "";
        if (value) {
            return "Yes";
        } else {
            return "No";
        }
    }

    Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private LabRequestQueryService labRequestQueryService;

    @Transactional(readOnly = true)
    public HttpEntity<InputStreamResource> writeRequestListCsv(List<RequestRepresentation> requests) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Writer writer = new OutputStreamWriter(out, CSV_CHARACTER_ENCODING);
            ICSVWriter csvwriter = new CSVWriterBuilder(writer)
                    .withSeparator(';')
                    .withQuoteChar('"')
                    .build();
            csvwriter.writeNext(CSV_COLUMN_NAMES);

            for (RequestRepresentation request: requests) {
                List<String> values = new ArrayList<>();
                values.add(request.getRequestNumber());
                values.add(DATE_FORMATTER.print(request.getDateCreated(), LOCALE));
                values.add(DATE_FORMATTER.print(request.getDateSubmitted(), LOCALE));
                values.add(request.getExcerptListAttachment() != null ?
                    DATE_FORMATTER.print(request.getExcerptListAttachment().getDate(), LOCALE) : "");
                values.add(request.getTitle());
                values.add(request.getStatus().toString());
                values.add(booleanToString(request.isLinkageWithPersonalData()));
                values.add(request.getLinkageWithPersonalDataNotes());
                values.add(booleanToString(request.isStatisticsRequest()));
                values.add(booleanToString(request.isExcerptsRequest()));
                values.add(booleanToString(request.isPaReportRequest()));
                values.add(booleanToString(request.isMaterialsRequest()));
                values.add(booleanToString(request.isClinicalDataRequest()));
                values.add(request.getRequesterName());
                values.add(request.getLab() != null ?
                        request.getLab().getNumber().toString() + ". " + request.getLab().getName() : "");
                values.add(request.getRequester() != null ? request.getRequester().getSpecialism() : "");
                values.add(request.getGrantProvider());
                values.add(request.getPrivacyCommitteeRationale());
                values.add(request.getPrivacyCommitteeOutcome());
                values.add(request.getPrivacyCommitteeOutcomeRef());
                values.add(request.getPrivacyCommitteeEmails());
                values.add(labRequestQueryService.countHubAssistanceLabRequestsForRequest(
                        request.getProcessInstanceId()).toString());
                values.add(request.getPathologistName());
                csvwriter.writeNext(values.toArray(new String[]{}));
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
            headers.setContentType(MediaType.valueOf("text/csv"));
            String filename = "requests_" +
                    DATE_FORMATTER.print(new Date(), LOCALE) +
                    ".csv";
            headers.set("Content-Disposition",
                    "attachment; filename=" + filename);
            HttpEntity<InputStreamResource> response =  new HttpEntity<>(resource, headers);
            log.info("Returning response.");
            return response;
        } catch (IOException e) {
            log.error("Error writing to CSV.", e);
            throw new FileDownloadError();
        }

    }

}
