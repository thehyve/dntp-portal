package business.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

@Entity
public class ExcerptEntry implements Serializable {

    @Id
    @GeneratedValue
    private Long id;
    
    private Integer labNumber;
    
    private String paNumber;
    
    private Integer sequenceNumber;
    
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<ExcerptValue> values = new ArrayList<ExcerptValue>();

    public ExcerptEntry() {
        
    }
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public List<ExcerptValue> getValues() {
        return values;
    }

    public void setValues(List<ExcerptValue> values) {
        this.values = values;
    }
    
    public void addValue(String value) {
        this.values.add(new ExcerptValue(value));
    }
    
    public String[] getCsvValues() {
        String[] result = new String[this.getValues().size() + 1];
        result[0] = this.sequenceNumber.toString();
        int i = 1;
        for (ExcerptValue value: this.getValues()) {
            result[i] = value.getValue();
            i++;
        }
        return result;
    }
    
}