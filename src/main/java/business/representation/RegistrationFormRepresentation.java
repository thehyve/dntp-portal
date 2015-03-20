package business.representation;

import business.models.User;

public class RegistrationFormRepresentation {

    public RegistrationFormRepresentation() {

    }

    public RegistrationFormRepresentation(User user) {

    }

    String firstname;

    String lastname;

    String email;

    String telephone;

    String institution;

    String specialism;

    String isLabMember;

    String labId;

    String password;

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getInstitution() {
        return institution;
    }

    public void setInstitution(String institution) {
        this.institution = institution;
    }

    public String getSpecialism() {
        return specialism;
    }

    public void setSpecialism(String specialism) {
        this.specialism = specialism;
    }

    public String getIsLabMember() {
        return isLabMember;
    }

    public void setIsLabMember(String isLabMember) {
        this.isLabMember = isLabMember;
    }

    public String getLabId() {
        return labId;
    }

    public void setLabId(String labId) {
        this.labId = labId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
