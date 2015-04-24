package business.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;

@Entity
public class ExcerptValue implements Serializable {

    private static final long serialVersionUID = -8349346200142613552L;

    @Id
    @GeneratedValue
    private Long id;
    
    @Lob
    private String value = "";

    public ExcerptValue() {
        
    }
    
    public ExcerptValue(String value) {
        this.value = value;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    
}
