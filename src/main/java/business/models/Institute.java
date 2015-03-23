package business.models;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import org.springframework.data.rest.core.annotation.RestResource;

@Entity
public class Institute {

    @Id
    @GeneratedValue
    private Long id;
    
    private String name;
    
    @OneToOne(optional = true, cascade = CascadeType.ALL)
    @RestResource(exported = false)
    private ContactData contactData;
    
    public Institute() {
        
    }

    public Institute(Long id, String name, ContactData contactData) {
        this.id = id;
        this.name = name;
        this.contactData = contactData;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ContactData getContactData() {
        return contactData;
    }

    public void setContactData(ContactData contactData) {
        this.contactData = contactData;
    }
    
    
}
