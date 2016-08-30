/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business.representation;

import java.util.ArrayList;
import java.util.List;

import business.models.PathologyItem;

public class PathologyRepresentation {

    private Long id;

    private String paNumber;

    private String palgaPatientNr;

    private String palgaExcerptNr;

    private String palgaExcerptId;

    private String remark;

    private Integer sequenceNumber;

    List<String> samples = new ArrayList<String>();

    Boolean samplesAvailable;

    public PathologyRepresentation() {
        
    }

    public PathologyRepresentation(PathologyItem item) {
        this.id = item.getId();
        this.paNumber = item.getPaNumber();
        this.palgaPatientNr = item.getPalgaPatientNr();
        this.palgaExcerptNr = item.getPalgaExcerptNr();
        this.palgaExcerptId = item.getPalgaExcerptId();
        this.remark = item.getRemark();
        this.sequenceNumber = item.getSequenceNumber();
        this.samplesAvailable = item.isSamplesAvailable();
    }
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPaNumber() {
        return paNumber;
    }

    public void setPaNumber(String paNumber) {
        this.paNumber = paNumber;
    }

    public String getPalgaPatientNr() {
        return palgaPatientNr;
    }

    public void setPalgaPatientNr(String palgaPatientNr) {
        this.palgaPatientNr = palgaPatientNr;
    }

    public String getPalgaExcerptNr() {
        return palgaExcerptNr;
    }

    public void setPalgaExcerptNr(String palgaExcerptNr) {
        this.palgaExcerptNr = palgaExcerptNr;
    }

    public String getPalgaExcerptId() {
        return palgaExcerptId;
    }

    public void setPalgaExcerptId(String palgaExcerptId) {
        this.palgaExcerptId = palgaExcerptId;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Integer getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(Integer sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public List<String> getSamples() {
        return samples;
    }

    public void setSamples(List<String> samples) {
        this.samples = samples;
    }

    public void mapSamples(PathologyItem item) {
        this.samples = item.getSamples();
    }

    public Boolean isSamplesAvailable() {
        return this.samplesAvailable;
    }

    public void setSamplesAvailable(Boolean samplesAvailable) {
        this.samplesAvailable = samplesAvailable;
    }

}
