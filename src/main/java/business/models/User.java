package business.models;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

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
    @Column
    private boolean enabled;
    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Role> roles = new HashSet<Role>();
    
    public User() {
        
    }
    
    public User(String username, String password, boolean enabled,
            Set<Role> roles) {
        super();
        this.username = username;
        this.password = password;
        this.enabled = enabled;
        this.roles = roles;
    }
    
    public Long getId() {
        return id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public Set<Role> getRoles() {
        return roles;
    }

    public void clearPassword() {
        this.password = "";
        
    }
 
    public String toString() {
        return username;
    }
    
}
