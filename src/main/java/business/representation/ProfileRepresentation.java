package business.representation;



public class ProfileRepresentation {

    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String telephone;
    private String institution;
    private String specialism;
    private boolean isPathologist;

    public ProfileRepresentation() {
        this.id = "1";
        this.firstName = "John";
        this.lastName = "Doe";
        this.email = "john@doe.com";
        this.telephone = "+31611100222";
        this.institution = "UMC Utrecht";
        this.specialism = "None";
        this.isPathologist = true;
    }

    public String getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getTelephone() {
        return telephone;
    }

    public String getInstitution() {
        return institution;
    }

    public String getSpecialism() {
        return specialism;
    }

    public boolean isPathologist() {
        return isPathologist;
    }
}
