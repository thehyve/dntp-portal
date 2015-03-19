package business.representation;

import business.models.ContactData;
import business.models.Institution;
import business.models.User;

import javax.validation.constraints.NotNull;

public class ProfileRepresentation {

    private Long id;
    private String firstName;
    private String lastName;
    private ContactData contactData;
    private String institution;
    private String specialism;
    private boolean isPathologist;

    public ProfileRepresentation() {}

    public ProfileRepresentation(@NotNull User user) {
        ContactData cData = user.getContactData();
        Institution institution = user.getInstitution();

        this.id = user.getId();
        this.firstName = "John";
        this.lastName = "Doe";

        if (cData != null) {
            this.contactData = cData;
        } else {
            this.contactData = new ContactData();
        }

        if (institution != null) {
            this.institution = institution.getName();
        } else {
            this.institution = "";
        }

        this.specialism = "None";
        this.isPathologist = true;
    }

    public boolean isPathologist() {
        return isPathologist;
    }

    public Long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public ContactData getContactData() {
        return contactData;
    }

    public String getInstitution() {
        return institution;
    }

    public String getSpecialism() {
        return specialism;
    }
}
