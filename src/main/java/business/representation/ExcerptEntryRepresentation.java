package business.representation;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import business.models.ExcerptEntry;
import business.models.ExcerptList;

public class ExcerptEntryRepresentation {

    private Long id;
    private boolean selected;
    private Integer sequenceNumber;
    private List<String> values = new ArrayList<String>();
    
    public ExcerptEntryRepresentation() {
        
    }
    
    public ExcerptEntryRepresentation(ExcerptEntry entry) {
        this.id = entry.getId();
        this.selected = entry.isSelected();
        this.sequenceNumber = entry.getSequenceNumber();
        values = new ArrayList<String>();
        for (String value: entry.getCsvValues()) {
            values.add(value);
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    
    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
    
    public Integer getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(Integer sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }
    
}
