package business.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class ExcerptValue {

    @Id
    @GeneratedValue
    private Long id;
    
    @Column(columnDefinition="TEXT")
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
