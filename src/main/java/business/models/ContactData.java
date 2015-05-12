package business.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class ContactData {

    @Id
    @GeneratedValue
    private Long id;
    
    @Column(nullable = true)
    private String telephone;
    @Column(nullable = true)
    private String email;
    
    @Column(nullable = true)
    private String address1;
    @Column(nullable = true)
    private String address2;
    @Column(nullable = true)
    private String postalCode;
    @Column(nullable = true)
    private String city;
    @Column(nullable = true)
    private String stateProvince;
    @Column(nullable = true)
    private String country = "NL";
    
    public ContactData() {
        
    }

    public ContactData(Long id, String telephone, String email,
            String address1, String address2, String postalCode, String city,
            String stateProvince, String country) {
        this.id = id;
        this.telephone = telephone;
        this.email = email;
        this.address1 = address1;
        this.address2 = address2;
        this.postalCode = postalCode;
        this.city = city;
        this.stateProvince = stateProvince;
        this.country = country;
    }

    public void copy(ContactData other) {
        if (other != null) {
            this.telephone = other.telephone;
            this.email = other.email;
            this.address1 = other.address1;
            this.address2 = other.address2;
            this.postalCode = other.postalCode;
            this.city = other.city;
            this.stateProvince = other.stateProvince;
            this.country = other.country;
        }
    }
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress1() {
        return address1;
    }

    public void setAddress1(String address1) {
        this.address1 = address1;
    }

    public String getAddress2() {
        return address2;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStateProvince() {
        return stateProvince;
    }

    public void setStateProvince(String stateProvince) {
        this.stateProvince = stateProvince;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
    
    
    
}
