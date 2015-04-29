package business.representation;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Transient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import business.models.ExcerptEntry;
import business.models.ExcerptList;

public class ExcerptListRepresentation {

    Log log = LogFactory.getLog(getClass());
    
    private Long id;
    private List<String> columnNames = new ArrayList<String>();
    private List<List<String>> values = new ArrayList<List<String>>();
    
    public ExcerptListRepresentation() {
        
    }
    
    public ExcerptListRepresentation(ExcerptList list) {
        this.id = list.getId();
        for (String name: list.getCsvColumnNames()) {
            this.columnNames.add(name);
        }
        for (ExcerptEntry entry: list.getEntries()) {
            List<String> row = new ArrayList<String>();
            for (String value: entry.getCsvValues()) {
                row.add(value);
            }
            this.values.add(row);
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

    public List<List<String>> getValues() {
        return values;
    }

    public void setValues(List<List<String>> values) {
        this.values = values;
    }
    
}
