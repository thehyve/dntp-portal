/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business.models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

@Entity
@Table(indexes = @Index(columnList="excerptListId"))
public class ExcerptEntry {

    @Id
    @GeneratedValue
    private Long id;

    private Long excerptListId;

    private Integer labNumber;

    private String paNumber;

    private String palgaPatientNr;

    private String palgaExcerptNr;

    private String palgaExcerptId;

    private Integer sequenceNumber;

    private Boolean selected;

    @ElementCollection
    @Cascade(value={CascadeType.ALL})
    //@Column(length = 32767)
    @Column(columnDefinition="TEXT")
    @CollectionTable(indexes = @Index(columnList="excerpt_entry_id"))
    @OrderColumn
    @BatchSize(size = 10000)
    private List<String> values = new ArrayList<String>();

    public ExcerptEntry() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getExcerptListId() {
        return excerptListId;
    }

    public void setExcerptListId(Long excerptListId) {
        this.excerptListId = excerptListId;
    }

    public Integer getLabNumber() {
        return labNumber;
    }

    public void setLabNumber(Integer labNumber) {
        this.labNumber = labNumber;
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
    
    public boolean isSelected() {
        return (selected != null && selected.booleanValue());
    }

    public void setSelected(@NotNull Boolean selected) {
        this.selected = selected;
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }

    public void addValue(String value) {
        this.values.add(value);
    }

    public String[] getCsvValues() {
        String[] result = new String[this.getValues().size() + 4];
        result[0] = this.sequenceNumber.toString();
        result[1] = this.palgaPatientNr;
        result[2] = this.palgaExcerptNr;
        result[3] = this.palgaExcerptId;
        int i = 4;
        for (String value: this.getValues()) {
            result[i] = value;
            i++;
        }
        return result;
    }

    public String[] getLabRequestValues() {
        String[] result = new String[this.getValues().size() + 6];
        result[0] = this.sequenceNumber.toString();
        result[1] = this.palgaPatientNr;
        result[2] = this.palgaExcerptNr;
        result[3] = this.palgaExcerptId;
        result[4] = this.labNumber.toString();
        result[5] = this.paNumber;
        int i = 6;
        for (String value: this.getValues()) {
            result[i] = value;
            i++;
        }
        return result;
    }

}
