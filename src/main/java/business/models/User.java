package business.models;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "appUser")
public class User implements Serializable {

    private static final long serialVersionUID = -1083781091067573685L;

    @Id
    @GeneratedValue
    private Long id;    
    private String username;
    private String password;
    private boolean active = true;
    private boolean deleted = false;

    private long failedLoginAttempts = 0;
    private boolean accountTemporarilyBlocked = false;
    private Date accountBlockStartTime;
    
    private String firstName = "";
    private String lastName = "";
    boolean isPathologist = false;
    private String institute;
    private String specialism;

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
    public String getSpecialism() {
        return specialism;
    }

    public void setSpecialism(String specialism) {
        this.specialism = specialism;
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
    
    public void setRoles(Set<Role> roles) {
        this.roles = roles;
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

    public Date getAccountBlockStartTime() {
        return accountBlockStartTime;
    }

    public void setAccountBlockStartTime(Date accountBlockStartTime) {
        this.accountBlockStartTime = accountBlockStartTime;
    }

    public long getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public void incrementFailedLoginAttempts() {
        if (this.failedLoginAttempts < Long.MAX_VALUE) {
            this.failedLoginAttempts++;
        }
    }
    
    public void resetFailedLoginAttempts() {
        this.failedLoginAttempts = 0;
    }

    public boolean isAccountTemporarilyBlocked() {
        return accountTemporarilyBlocked;
    }

    public void setAccountTemporarilyBlocked(boolean accountTemporarilyBlocked) {
        this.accountTemporarilyBlocked = accountTemporarilyBlocked;
    }
    
}
