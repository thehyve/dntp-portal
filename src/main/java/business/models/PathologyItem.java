/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business.models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity
@Table(indexes = @Index(columnList="labRequestId"))
public class PathologyItem {

    @Id
    @GeneratedValue
    private Long id;

    private Long labRequestId;

    private String paNumber;

    private String palgaPatientNr;

    private String palgaExcerptNr;

    private String palgaExcerptId;

    private Integer sequenceNumber;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(indexes = @Index(columnList="pathology_item_id"))
    @Fetch(FetchMode.JOIN)
    @BatchSize(size = 10000)
    @OrderColumn
    List<String> samples = new ArrayList<String>();

    private Boolean samplesAvailable;
    
    public PathologyItem() {
        
    }

    public PathologyItem(@NotNull Long labRequestId, @NotNull ExcerptEntry entry) {
        this.labRequestId = labRequestId;
        this.paNumber = entry.getPaNumber();
        this.palgaPatientNr = entry.getPalgaPatientNr();
        this.palgaExcerptNr = entry.getPalgaExcerptNr();
        this.palgaExcerptId = entry.getPalgaExcerptId();
        this.sequenceNumber = entry.getSequenceNumber();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getLabRequestId() {
        return labRequestId;
    }

    public void setLabRequestId(Long labRequestId) {
        this.labRequestId = labRequestId;
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

    public Boolean isSamplesAvailable() {
        return samplesAvailable;
    }

    public void setSamplesAvailable(Boolean samplesAvailable) {
        this.samplesAvailable = samplesAvailable;
    }

}
