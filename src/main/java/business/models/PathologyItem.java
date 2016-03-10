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
    
    String paNumber;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(indexes = @Index(columnList="pathology_item_id"))
    @Fetch(FetchMode.JOIN)
    @BatchSize(size = 100)
    @OrderColumn
    List<String> samples = new ArrayList<String>();
    
    public PathologyItem() {
        
    }
    
    public PathologyItem(@NotNull Long labRequestId, @NotNull String paNumber) {
        this.labRequestId = labRequestId;
        this.paNumber = paNumber;
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

    public List<String> getSamples() {
        return samples;
    }

    public void setSamples(List<String> samples) {
        this.samples = samples;
    }
    
}
