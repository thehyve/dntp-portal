package business.models;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class Institution {

    @Id
    @GeneratedValue
    private Long id;
    
    private String name;
    @ManyToOne
    private ContactData contactData;
    
    public Institution() {
        
    }

    public Institution(Long id, String name, ContactData contactData) {
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
