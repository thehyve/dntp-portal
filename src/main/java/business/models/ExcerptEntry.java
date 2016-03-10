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

@Entity
@Table(indexes = @Index(columnList="excerptListId"))
public class ExcerptEntry {

    @Id
    @GeneratedValue
    private Long id;
    
    private Long excerptListId;
    
    private Integer labNumber;
    
    private String paNumber;
    
    private Integer sequenceNumber;
    
    private Boolean selected;
    
    @ElementCollection
    //@Column(length = 32767)
    @Column(columnDefinition="TEXT")
    @CollectionTable(indexes = @Index(columnList="excerpt_entry_id"))
    @OrderColumn
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
        String[] result = new String[this.getValues().size() + 1];
        result[0] = this.sequenceNumber.toString();
        int i = 1;
        for (String value: this.getValues()) {
            result[i] = value;
            i++;
        }
        return result;
    }

    public String[] getLabRequestValues() {
        String[] result = new String[this.getValues().size() + 3];
        result[0] = this.sequenceNumber.toString();
        result[1] = this.labNumber.toString();
        result[2] = this.paNumber;
        int i = 3;
        for (String value: this.getValues()) {
            result[i] = value;
            i++;
        }
        return result;
    }
    
}
