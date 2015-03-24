package business.models;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.*;

@Entity
@Table(name = "appUser")
public class User implements Serializable {

    private static final long serialVersionUID = -1083781091067573685L;

    @Id
    @GeneratedValue
    private Long id;    
    @Column(unique = true)
    private String username;
    private String password;
    private boolean active = true;
    private boolean deleted = false;

    private String firstName = "";
    private String lastName = "";
    boolean isPathologist = false;
    private String institute;
    
    @ManyToOne(optional = true)
    private Lab lab;
   
    @OneToOne(optional = true, cascade = CascadeType.ALL)
    private ContactData contactData;
    
    @ManyToMany(fetch = FetchType.EAGER, targetEntity=Role.class)
    private Set<Role> roles = new HashSet<Role>();
    
    private Date created = new Date();
    
    public User() {
        
    }
    
    public User(String username, String password, boolean active,
            Set<Role> roles) {
        super();
        this.username = username;
        this.password = password;
        this.active = active;
        this.roles = roles;
    }
    
    public boolean isRequester() {
        for (Role role: roles) {
            if (role.isRequester()) return true;
        }
        return false;
    }

    public boolean isPalga() {
        for (Role role: roles) {
            if (role.isPalga()) return true;
        }
        return false;
    }

    public boolean isLabUser() {
        for (Role role: roles) {
            if (role.isLabUser()) return true;
        }
        return false;
    }

    public boolean isScientificCouncilMember() {
        for (Role role: roles) {
            if (role.isScientificCouncilMember()) return true;
        }
        return false;
    }

    
    public Long getId() {
        return id;
    }
    

    public String getUsername() {
        return username;
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

    public boolean isPathologist() {
        return isPathologist;
    }

    public void setPathologist(boolean isPathologist) {
        this.isPathologist = isPathologist;
    }

    public String getPassword() {
        return password;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void activate() {
        this.active = true; 
    }
    
    public void deactivate() {
        this.active = false;
    }
    
    public boolean isDeleted() {
        return deleted;
    }
    
    public void markDeleted() {
        this.deleted = true;
    }

    public Lab getLab() {
        return lab;
    }

    public void setLab(Lab lab) {
        this.lab = lab;
    }

    public String getInstitute() {
        return institute;
    }

    public void setInstitute(String institute) {
        this.institute = institute;
    }

    public ContactData getContactData() {
        return contactData;
    }

    public void setContactData(ContactData contactData) {
        this.contactData = contactData;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void clearPassword() {
        this.password = ""; 
    }
 
    public String toString() {
        return this.username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUsername(String username) {
        this.username = username;
        
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

}
