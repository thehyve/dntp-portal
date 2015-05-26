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

@Entity
public class Role implements Serializable {
    
    private static final long serialVersionUID = 9017390179681480450L;

    @Id
    @GeneratedValue
    private Long id;
    @Column(unique = true)
    private String name;

    @ManyToMany(fetch = FetchType.LAZY, mappedBy="roles", targetEntity = User.class)
    private Set<User> users = new HashSet<User>();
    
    public Role() {
    }
    
    public Role(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
    
    public String toString() {
        return name;
    }
    
    public boolean isRequester() {
        return name.equals("requester");
    }

    public boolean isPalga() {
        return name.equals("palga");
    }

    public boolean isScientificCouncilMember() {
        return name.equals("scientific_council");
    }

    public boolean isLabUser() {
        return name.equals("lab_user");
    }

    public Set<User> getUsers() {
        return users;
    }

}
