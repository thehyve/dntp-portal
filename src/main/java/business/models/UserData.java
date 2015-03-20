package business.models;

import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class UserData {
    
    @Id
    @GeneratedValue
    private Long id;
    
    private String firstName;
    private String lastName;
    private String telephone;
    private String institution;
    
    

}
