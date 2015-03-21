package business.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class Lab {

    @Id
    @GeneratedValue
    private Long id;
    
    @Column(unique = true)
    private Integer number;
    
    private String name;
    
    @ManyToOne
    private ContactData contactData;
    
    public Lab() {
        
    }

    public Lab(Long id, Integer number, String name, ContactData contactData) {
        this.id = id;
        this.number = number;
        this.name = name;
        this.contactData = contactData;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
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