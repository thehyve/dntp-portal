package business.representation;

import business.models.ContactData;
import javax.validation.constraints.NotNull;


public class ProfileRepresentation {

    private String currentRole;
    private boolean active;
    
    private Long id;
    private String username;
    private String password1;
    private String password2;
    private String firstName;
    private String lastName;

    private ContactData contactData;
    
    private Long labId;
    private Long institutionId;
    
    private boolean isPathologist;

    public ProfileRepresentation() {
        this.labId = -1L;
        this.institutionId = -1L;
    }
    
    public ProfileRepresentation(@NotNull User user) {
        this.active = user.isActive();
        this.id = user.getId();
        this.username = user.getUsername();
        this.currentRole =  user.isPalga() ? "palga" :
                            user.isLabUser() ? "labuser" :
                            user.isScientificCouncilMember() ? "scientific_council" :
                            user.isRequester() ? "requester" :
                            "";
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.isPathologist = user.isPathologist();
        this.contactData = user.getContactData() == null ? new ContactData() : user.getContactData();
        this.labId = user.getLab() == null ? null : user.getLab().getId();
    }

    public boolean isActive() {
        return active;
    }

    public String getCurrentRole() {
        return currentRole;
    }
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword1() {
        return password1;
    }

    public String getPassword2() {
        return password2;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public ContactData getContactData() {
        return contactData;
    }

    public void setContactData(ContactData contactData) {
        this.contactData = contactData;
    }

    public Long getLabId() {
        return labId;
    }

    public void setLabId(Long labId) {
        this.labId = labId;
    }

    public Long getInstitutionId() {
        return institutionId;
    }

    public void setInstitutionId(Long institutionId) {
        this.institutionId = institutionId;
    }

    public String getInstitution() {
        return institution;
    }

    public String getSpecialism() {
        return specialism;
    }
}
