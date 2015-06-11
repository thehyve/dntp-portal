package business.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@Entity
public class ExcerptList implements Serializable {

    private static final long serialVersionUID = -3801412654264700878L;

    @Transient
    Log log = LogFactory.getLog(getClass());
    
    @Id
    @GeneratedValue
    private Long id;
    
    @Column(unique = true)
    private String processInstanceId;

    @Column(columnDefinition="TEXT")
    private String remark;
    
    @ElementCollection
    private List<String> columnNames = new ArrayList<String>();
    
    private int labNumberColumn = -1;
    
    private int paNumberColumn = -1;

    /**
     * Maps sequence numbers to entries.
     */
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Map<Integer, ExcerptEntry> entries = new TreeMap<Integer, ExcerptEntry>();

    public ExcerptList() {
        
    }
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
    public Map<Integer, ExcerptEntry> getEntries() {
        return entries;
    }
    
    public Collection<ExcerptEntry> getEntryValues() {
        return entries.values();
    }

    public void setEntries(Map<Integer, ExcerptEntry> entries) {
        this.entries = entries;
    }

    public class InvalidRow extends RuntimeException {
        private static final long serialVersionUID = 4962263427071738791L;
        
        public InvalidRow(String message) {
            super(message);
        }
        
        public InvalidRow() {
            super("Invalid row.");
        }
    }   
    
    public void addEntry(ExcerptEntry entry) {
        this.entries.put(this.entries.size(), entry);
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
        // not thread-safe, but that is not required here anyway.
        entry.setSequenceNumber(this.entries.size() + 1);
        this.entries.put(this.entries.size(), entry);
        return entry;
    }
}
