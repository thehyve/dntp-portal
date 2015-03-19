package business.models;

import java.io.Serializable;
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
    private String email;
    private String password;
    private boolean active = true;
    private boolean deleted = false;
    
    @ManyToOne(optional = true)
    private Lab lab;
    @ManyToOne(optional = true)
    private Institution institution;
    @OneToOne(optional = true, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private ContactData contactData;
    
    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Role> roles = new HashSet<Role>();
    
    public User() {
        
    }
    
    public User(String email, String password, boolean active,
            Set<Role> roles) {
        super();
        this.email = email;
        this.password = password;
        this.active = active;
        this.roles = roles;
    }
    
    public Long getId() {
        return id;
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

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
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
        return this.email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
