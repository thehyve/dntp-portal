package business.models;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.annotations.CollectionType;

@Entity
public class RequestProperties {

    @Id
    @GeneratedValue
    private Long id;
    
    @Column(unique = true)
    private String processInstanceId;
    
    @ElementCollection
    private Set<String> agreementAttachmentIds = new HashSet<String>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public Set<String> getAgreementAttachmentIds() {
        return agreementAttachmentIds;
    }

    public void setAgreementAttachmentIds(Set<String> agreementAttachmentIds) {
        this.agreementAttachmentIds = agreementAttachmentIds;
    }
    
}
