package business.models;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Role implements Serializable {
    
    private static final long serialVersionUID = 9017390179681480450L;

    @Id
    @GeneratedValue
    private Long id;
    @Column(unique = true)
    private String name;

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

}
