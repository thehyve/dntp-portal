package business.representation;

import business.models.Role;

public class RoleRepresentation {
    
    private Long id;
    private String name;
    
    public RoleRepresentation() {
        
    }
    
    public RoleRepresentation(Role role) {
        this.id = role.getId();
        this.name = role.getName();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
}
