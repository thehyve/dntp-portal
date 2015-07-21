package business.models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import business.exceptions.InvalidRow;

@Entity
@Table(indexes = @Index(columnList="propertiesId"))
public class ExcerptList {

    @Transient
    Log log = LogFactory.getLog(getClass());
    
    @Id
    @GeneratedValue
    private Long id;
    
    private Long propertiesId;
    
    @Column(unique = true)
    private String processInstanceId;

    @Column(columnDefinition="TEXT")
    //@Column(length = 32767)
    private String remark;
    
    @ElementCollection
    private List<String> columnNames = new ArrayList<String>();
    
    private int labNumberColumn = -1;
    
    private int paNumberColumn = -1;

    /**
     * Maps sequence numbers to entries.
     */
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy="excerptListId")
    @OrderBy("sequenceNumber ASC")
    private List<ExcerptEntry> entries = new ArrayList<ExcerptEntry>();

    public ExcerptList() {
        
    }
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getPropertiesId() {
        return propertiesId;
    }

    public void setPropertiesId(Long requestId) {
        this.propertiesId = requestId;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }
    
    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String[] getCsvColumnNames() {
        String[] result = new String[columnNames.size() + 1];
        result[0] = "Sequence number";
        int i = 1;
        for (String name: columnNames) {
            result[i] = name;
            i++;
        }
        return result;
    }

    public String[] getLabRequestColumnNames() {
        String[] result = new String[columnNames.size() + 3];
        result[0] = "Sequence number";
        result[1] = "Lab";
        result[2] = "PA number";
        int i = 3;
        for (String name: columnNames) {
            result[i] = name;
            i++;
        }
        return result;
    }
    
    public class InvalidHeader extends RuntimeException {
        private static final long serialVersionUID = 4962263427071738791L;
        
        public InvalidHeader(String message) {
            super(message);
        }
        
        public InvalidHeader() {
            super("Invalid header.");
        }
    }   
    
    public void setColumnNames(String[] columnNames) {
        this.columnNames = new ArrayList<String>();
        this.labNumberColumn = -1;
        this.paNumberColumn = -1;
        for (int i=0; i < columnNames.length; i++) {
            String name_ = columnNames[i].trim().toLowerCase();
            log.info("column name: " + name_);
            if (name_.equals("palab_nu")) {
                labNumberColumn = i;
            } else if (name_.equals("pa_nummer_nu")) {
                paNumberColumn = i;
            } else {
                this.columnNames .add(columnNames[i]);
            }
        }
        if (labNumberColumn == -1) {
            throw new InvalidHeader("No lab number column.");
        }
        if (paNumberColumn == -1) {
            throw new InvalidHeader("No PA number column.");
        }
    }

    /**
     * Maps sequence numbers to entries.
     */
    public List<ExcerptEntry> getEntries() {
        return entries;
    }
    
    public void setEntries(List<ExcerptEntry> entries) {
        this.entries = entries;
    }

    public void addEntry(ExcerptEntry entry) {
        synchronized (this.entries) {
            entry.setExcerptListId(this.id);
            entry.setSequenceNumber(this.entries.size() + 1);
            this.entries.add(entry);
        }
    }
    
    public ExcerptEntry addEntry(String[] data) {
        if (data.length != columnNames.size() + 2) {
            throw new InvalidRow("Row length does not match header length.");
        }
        ExcerptEntry entry = new ExcerptEntry();
        if (labNumberColumn == -1) {
            throw new InvalidRow("No lab number column.");
        }
        if (paNumberColumn == -1) {
            throw new InvalidRow("No PA number column.");
        }
        entry.setLabNumber(Integer.valueOf(data[labNumberColumn]));
        entry.setPaNumber(data[paNumberColumn]);
        for (int i=0; i < data.length; i++) {
            if (i != labNumberColumn && i != paNumberColumn) {
                entry.addValue(data[i]);
            }
        }
        addEntry(entry);
        return entry;
    }

    public void deselectAll() {
        for (ExcerptEntry entry: entries) {
            entry.setSelected(false);
        }
    }
    
    public void selectAll() {
        for (ExcerptEntry entry: entries) {
            entry.setSelected(true);
        }
    }

}
