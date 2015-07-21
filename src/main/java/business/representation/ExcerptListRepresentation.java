package business.representation;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import business.models.ExcerptEntry;
import business.models.ExcerptList;

public class ExcerptListRepresentation {
    
    Log log = LogFactory.getLog(getClass());

    private Long id;
    private List<String> columnNames = new ArrayList<String>();
    private List<ExcerptEntryRepresentation> entries = new ArrayList<ExcerptEntryRepresentation>();
    
    public ExcerptListRepresentation() {
        
    }
    
    public ExcerptListRepresentation(ExcerptList list) {
        this.id = list.getId();
        for (String name: list.getCsvColumnNames()) {
            this.columnNames.add(name);
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<String> getColumnNames() {
        return columnNames;
    }

    public void setColumnNames(List<String> columnNames) {
        this.columnNames = columnNames;
    }

    public List<ExcerptEntryRepresentation> getEntries() {
        return entries;
    }

    public void setEntries(List<ExcerptEntryRepresentation> entries) {
        this.entries = entries;
    }
    
    public void setEntryList(List<ExcerptEntry> list) {
        Date start = new Date();
        for (ExcerptEntry entry: list) {
            entries.add(new ExcerptEntryRepresentation(entry));
        }
        Date end = new Date();
        log.warn("ExcerptListRepresentation: setEntries took " + (end.getTime() - start.getTime()) + " ms.");
    }

}
