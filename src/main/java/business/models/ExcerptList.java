/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business.models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.OrderColumn;
import javax.persistence.Table;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import business.exceptions.InvalidRow;

@Entity
@Table(indexes = @Index(columnList="propertiesId"))
public class ExcerptList {

    static final Log log = LogFactory.getLog(ExcerptList.class);

    @Id
    @GeneratedValue
    private Long id;

    private Long propertiesId;

    @Column(unique = true)
    private String processInstanceId;

    @Column(columnDefinition="TEXT")
    //@Column(length = 32767)
    private String remark;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(indexes = @Index(columnList="excerpt_list_id"))
    @Fetch(FetchMode.JOIN)
    @BatchSize(size = 10000)
    @OrderColumn
    private List<String> columnNames = new ArrayList<String>();

    private int palgaPatientNrColumn = -1;

    private int palgaExcerptNrColumn = -1;

    private int palgaExcerptIdColumn = -1;

    private int labNumberColumn = -1;

    private int paNumberColumn = -1;

    /**
     * Maps sequence numbers to entries.
     */
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy="excerptListId")
    @OrderBy("sequenceNumber ASC")
    @BatchSize(size = 10000)
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
        String[] result = new String[columnNames.size() + 4];
        result[0] = "Sequence number";
        result[1] = "PALGAPatiëntnr";
        result[2] = "PALGAExcerptnr";
        result[3] = "PALGAExcerptid";
        int i = 4;
        for (String name: columnNames) {
            result[i] = name;
            i++;
        }
        return result;
    }

    public String[] getLabRequestColumnNames() {
        String[] result = new String[columnNames.size() + 6];
        result[0] = "Sequence number";
        result[1] = "PALGAPatiëntnr";
        result[2] = "PALGAExcerptnr";
        result[3] = "PALGAExcerptid";
        result[4] = "Lab";
        result[5] = "PA number";
        int i = 6;
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
        this.palgaPatientNrColumn = -1;
        this.palgaExcerptNrColumn = -1;
        this.palgaExcerptIdColumn =-1;
        this.labNumberColumn = -1;
        this.paNumberColumn = -1;
        for (int i=0; i < columnNames.length; i++) {
            String name_ = columnNames[i].trim().toLowerCase();
            log.debug("column name: " + name_);
            if (name_.equals("palgapatiëntnr")) {
                palgaPatientNrColumn = i;
            } else if (name_.equals("palgaexcerptnr")) {
                palgaExcerptNrColumn = i;
            } else if (name_.equals("palgaexcerptid")) {
                palgaExcerptIdColumn = i;
            } else if (name_.equals("palab_nu")) {
                labNumberColumn = i;
            } else if (name_.equals("pa_nummer_nu")) {
                paNumberColumn = i;
            } else {
                this.columnNames .add(columnNames[i]);
            }
        }
        if (palgaPatientNrColumn == -1) {
            throw new InvalidHeader("No patient number column (PALGAPatiëntnr).");
        }
        if (palgaExcerptNrColumn == -1) {
            throw new InvalidHeader("No excerpt number column (PALGAexcerptnr).");
        }
        if (palgaExcerptIdColumn == -1) {
            throw new InvalidHeader("No excerpt id column (PALGAexcerptid).");
        }
        if (labNumberColumn == -1) {
            throw new InvalidHeader("No lab number column (PAlab_Nu).");
        }
        if (paNumberColumn == -1) {
            throw new InvalidHeader("No PA number column (PA_Nummer_Nu).");
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

    static final boolean validIndex(int index, String[] data) {
        return index >= 0 && index < data.length;
    }

    public ExcerptEntry addEntry(String[] data) {
        if (data.length != columnNames.size() + 5) {
            throw new InvalidRow("Row length does not match header length.");
        }
        ExcerptEntry entry = new ExcerptEntry();
        if (validIndex(palgaPatientNrColumn, data)) {
            entry.setPalgaPatientNr(data[palgaPatientNrColumn]);
        }
        if (validIndex(palgaExcerptNrColumn, data)) {
            entry.setPalgaExcerptNr(data[palgaExcerptNrColumn]);
        }
        if (validIndex(palgaExcerptIdColumn, data)) {
            entry.setPalgaExcerptId(data[palgaExcerptIdColumn]);
        }
        entry.setLabNumber(Integer.valueOf(data[labNumberColumn]));
        entry.setPaNumber(data[paNumberColumn]);
        for (int i=0; i < data.length; i++) {
            if (    i == palgaPatientNrColumn ||
                    i == palgaExcerptNrColumn ||
                    i == palgaExcerptIdColumn ||
                    i == labNumberColumn ||
                    i == paNumberColumn) {
                continue;
            }
            entry.addValue(data[i]);
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
