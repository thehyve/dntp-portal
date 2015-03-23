package business.representation;

import business.models.ContactData;
import business.models.Lab;
import business.models.User;


public class LabRepresentation {

    private Long id;
    private Integer number;
    private String name;

    private ContactData contactData;
    
    public LabRepresentation() {
    }
    
    public LabRepresentation(Lab lab) {
        this.id = lab.getId();
        this.number = lab.getNumber();
        this.name = lab.getName();
        this.contactData = lab.getContactData();
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
