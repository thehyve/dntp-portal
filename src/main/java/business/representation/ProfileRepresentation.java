/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business.representation;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.validation.constraints.NotNull;

import business.models.ContactData;
import business.models.Role;
import business.models.User;


public class ProfileRepresentation {

    private String currentRole;
    private Set<RoleRepresentation> roles = new HashSet<RoleRepresentation>();
    
    private boolean active;
    
    private Long id;
    private String username;
    private String password1;
    private String password2;
    private String firstName;
    private String lastName;
    private boolean emailValidated;

    private ContactData contactData;
    
    private Long labId;
    
    private boolean isPathologist;
    private String institute;

    private String specialism;
    
    private Date created;
    private Long createdTime;

    public ProfileRepresentation() {
    }

    public ProfileRepresentation(@NotNull User user) {
        this.active = user.isActive();
        this.id = user.getId();
        this.username = user.getUsername();
        this.currentRole =  user.isPalga() ? "palga" :
                            user.isLabUser() ? "lab_user" :
                            user.isScientificCouncilMember() ? "scientific_council" :
                            user.isRequester() ? "requester" :
                            "";
        for (Role role: user.getRoles()) {
            this.roles.add(new RoleRepresentation(role));
        }
        this.emailValidated = user.isEmailValidated();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.setPathologist(user.isPathologist());
        this.contactData = user.getContactData() == null ? new ContactData() : user.getContactData();
        this.labId = user.getLab() == null ? null : user.getLab().getId();
        this.institute = user.getInstitute();
        this.specialism = user.getSpecialism();
        this.created = user.getCreated();
        this.createdTime = user.getCreated().getTime();
    }

    public boolean isActive() {
        return active;
    }

    public String getCurrentRole() {
        return currentRole;
    }
    
    public Set<RoleRepresentation> getRoles() {
        return roles;
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

    public void setPassword1(String password1) {
        this.password1 = password1;
    }

    public void setPassword2(String password2) {
        this.password2 = password2;
    }

    public boolean isEmailValidated() {
        return emailValidated;
    }

    public void setEmailValidated(boolean emailValidated) {
        this.emailValidated = emailValidated;
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

    public String getInstitute() {
        return institute;
    }

    public void setInstitute(String institute) {
        this.institute = institute;
    }

    public String getSpecialism() {
        return specialism;
    }
    public void setSpecialism(String specialism) {
        this.specialism = specialism;
    }


    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
        this.createdTime = created.getTime();
    }

    public Long getCreatedTime() {
        return createdTime;
    }

    public boolean isPathologist() {
        return isPathologist;
    }

    public void setPathologist(boolean isPathologist) {
        this.isPathologist = isPathologist;
    }
    
    
}
