/*
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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import business.exceptions.InvalidActionInStatus;
import business.exceptions.PaNumbersDownloadError;
import business.models.LabRequest;
import business.models.User;
import business.representation.RequestListRepresentation;
import business.security.UserAuthenticationToken;
import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import business.models.PathologyItem;
import business.representation.LabRequestRepresentation;
import business.representation.PathologyRepresentation;
import org.springframework.transaction.annotation.Transactional;

import static business.services.ExportFormatHelper.booleanToString;
import static business.services.ExportFormatHelper.dateToString;
import static business.util.ExportUtils.replaceSpacesWithUnderscores;


@Service
@Transactional(readOnly = true)
public class LabRequestExportService {

    private static final String PA_NUMBERS_DOWNLOAD_CHARACTER_ENCODING = "UTF-8";


    private Log log = LogFactory.getLog(LabRequestExportService.class);

    @Autowired
    private LabRequestQueryService labRequestQueryService;

    @Autowired
    private RequestService requestService;


    private static final String[] FILE_HEADER = {
            "LAB_NO",
            "PA_NUMBER",
            "PALGA_PATIENT_NR",
            "EXTRA",
            "SAMPLES",
            "NOTES"
    };

    private InputStreamResource writePaNumbers(List<PathologyItem> items, Integer labNumber) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Writer writer = new OutputStreamWriter(out, PA_NUMBERS_DOWNLOAD_CHARACTER_ENCODING);
        ICSVWriter csvwriter = new CSVWriterBuilder(writer)
                .withSeparator(';')
                .withEscapeChar('"')
                .build();

        csvwriter.writeNext(FILE_HEADER);

        for (PathologyItem item : items) {
            log.info(item.getPaNumber());
            String[] toppings = {
                    labNumber.toString(),
                    item.getPaNumber(),
                    item.getPalgaPatientNr(),
                    item.getRemark(),
                    "",
                    ""
            };
            csvwriter.writeNext(toppings);
        }

        try {
            csvwriter.flush();
            writer.flush();
            out.flush();
            InputStream in = new ByteArrayInputStream(out.toByteArray());
            csvwriter.close();
            writer.close();
            out.close();
            return new InputStreamResource(in);
        } catch (IOException e) {
            throw new Exception(e);
        }
    }

    private static Set<LabRequest.Status> paNumberDownloadStatuses = new HashSet<>(Arrays.asList(
            LabRequest.Status.WAITING_FOR_LAB_APPROVAL,
            LabRequest.Status.APPROVED,
            LabRequest.Status.COMPLETED,
            LabRequest.Status.SENDING,
            LabRequest.Status.RECEIVED,
            LabRequest.Status.RETURNING));

    /**
     * Exports PA numbers for a lab request.
     *
     * @param id the id of the lab request.
     * @param user the user to export the data for.
     * @return the HTTP entity to return with the export contents as body.
     */
    public HttpEntity<InputStreamResource> downloadPANumbers(Long id, UserAuthenticationToken user) {
        LabRequest labRequest = labRequestQueryService.findOne(id);
        if (!paNumberDownloadStatuses.contains(labRequest.getStatus()) ||
                (user.getUser().isRequester() && labRequest.getStatus() == LabRequest.Status.WAITING_FOR_LAB_APPROVAL)) {
            log.error("Download not allowed in status '" + labRequest.getStatus() + "'");
            throw new InvalidActionInStatus("Download not allowed in status '" + labRequest.getStatus() + "'");
        }
        LabRequestRepresentation representation = new LabRequestRepresentation(labRequest);
        labRequestQueryService.transferLabRequestData(representation, false);

        try {
            String filename = "panumbers_" + representation.getLabRequestCode() + ".csv";
            InputStreamResource resource = writePaNumbers(
                    labRequest.getPathologyList(),
                    representation.getLab().getNumber());
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.valueOf("text/csv;charset=" + PA_NUMBERS_DOWNLOAD_CHARACTER_ENCODING));
            headers.set("Content-Disposition", "attachment; filename=\"" + filename + "\"");
            return new HttpEntity<>(resource, headers);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new PaNumbersDownloadError();
        }
    }

    private static final String[] PA_NUMBERS_HEADER = new String[]{
            "Request number",
            "Date created",
            "Date sent",
            "Status",
            "PA reports",
            "PA material Block",
            "PA material HE slice",
            "PA material other",
            "Clinical data",
            "PA number count",
            "PA number",
            "PALGA patient nr",
            "PALGA excerpt ID",
            "PALGA excerpt nr",
            "Laboratory",
            "Requester name",
            "Requester email",
            "Requester telephone number",
            "Extra"
    };

    private InputStreamResource writeAllPaNumbers(
            List<LabRequestRepresentation> labRequests,
            User user) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Writer writer = new OutputStreamWriter(out, PA_NUMBERS_DOWNLOAD_CHARACTER_ENCODING);
        ICSVWriter csvwriter = new CSVWriterBuilder(writer)
                .withSeparator(';')
                .withQuoteChar('"')
                .build();
        csvwriter.writeNext(replaceSpacesWithUnderscores(PA_NUMBERS_HEADER));
        for (LabRequestRepresentation labRequest: labRequests) {
            RequestListRepresentation request = requestService.getRequestData(labRequest.getProcessInstanceId(), user);
            String labRequestCode = labRequest.getLabRequestCode();
            String dateCreated = dateToString(labRequest.getDateCreated());
            String dateSent = dateToString(labRequest.getSendDate());
            String status = labRequest.getStatus().toString();
            String paReports = booleanToString(request.isPaReportRequest());
            String blockPaMaterial = booleanToString(request.isBlockMaterialsRequest());
            String heSlicePaMaterial = booleanToString(request.isHeSliceMaterialsRequest());
            String otherPaMaterial = request.getOtherMaterialsRequest();
            String clinicalData = booleanToString(request.isClinicalDataRequest());
            String numberOfPaNumbers = labRequest.getPathologyCount().toString();
            String labName = labRequest.getLab().getName();
            String requesterName = labRequest.getRequesterName();
            String requesterEmail = labRequest.getRequesterEmail();
            String requesterTelephone = labRequest.getRequesterTelephone();

            for (PathologyRepresentation item: labRequest.getPathologyList()) {
                csvwriter.writeNext(new String[] {
                        labRequestCode,
                        dateCreated,
                        dateSent,
                        status,
                        paReports,
                        blockPaMaterial,
                        heSlicePaMaterial,
                        otherPaMaterial,
                        clinicalData,
                        numberOfPaNumbers,
                        item.getPaNumber(),
                        item.getPalgaPatientNr(),
                        item.getPalgaExcerptId(),
                        item.getPalgaExcerptNr(),
                        labName,
                        requesterName,
                        requesterEmail,
                        requesterTelephone,
                        item.getRemark()
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
        return new InputStreamResource(in);
    }

    /**
     * Exports all PA numbers for a user.
     *
     * @param user the user to export the data for.
     * @return the HTTP entity to return with the export contents as body.
     */
    public HttpEntity<InputStreamResource> downloadAllPANumbers(UserAuthenticationToken user) {
        List<LabRequestRepresentation> labRequests = labRequestQueryService.findLabRequestsForUser(user.getUser(), true);

        try {
            InputStreamResource resource = writeAllPaNumbers(labRequests, user.getUser());
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.valueOf("text/csv;charset=" + PA_NUMBERS_DOWNLOAD_CHARACTER_ENCODING));
            String filename = "pa_numbers.csv";
            headers.set("Content-Disposition",
                    "attachment; filename=" + filename);
            return new HttpEntity<>(resource, headers);
        } catch (IOException e) {
            e.printStackTrace();
            log.error(e.getMessage());
            throw new PaNumbersDownloadError();
        }
    }

}
