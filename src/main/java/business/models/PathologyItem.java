package business.models;

import java.util.List;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(indexes = @Index(columnList="labRequestId"))
public class PathologyItem {

    @Id
    @GeneratedValue
    private Long id;
    
    private Long labRequestId;
    
    String paNumber;
    
    @ElementCollection
    List<String> samples;
    
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
