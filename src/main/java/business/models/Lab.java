/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business.models;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.OneToOne;
import javax.persistence.OrderColumn;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import business.services.LabRequestService;

@Entity
public class Lab {

    @Id
    @GeneratedValue
    private Long id;

    @Column(unique = true)
    private Integer number;

    private String name;

    @OneToOne(optional = true, cascade = CascadeType.ALL)
    private ContactData contactData;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(indexes = @Index(columnList="lab_id"))
    @Fetch(FetchMode.JOIN)
    @BatchSize(size = 100)
    @OrderColumn
    private List<String> emailAddresses;

    private boolean hubAssistanceEnabled = true;

    private boolean active = true;

    public Lab() {

    }

    public Lab(Long id, Integer number, String name, ContactData contactData) {
        this.id = id;
        this.number = number;
        this.name = name;
        this.contactData = contactData;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ContactData getContactData() {
        return contactData;
    }

    public void setContactData(ContactData contactData) {
        this.contactData = contactData;
    }

    /**
     * Email addresses used to notify the lab that lab requests have been
     * created.
     * See {@link LabRequestService#generateLabRequests(String)}.
     * @return The list of email addresses.
     */
    public List<String> getEmailAddresses() {
        return emailAddresses;
    }

    public void setEmailAddresses(List<String> emailAddresses) {
        this.emailAddresses = emailAddresses;
    }

    /**
     * Indicates if for this lab, assistance from a hub user is enabled.
     *
     * @return true if hub assistance is enabled; false otherwise (default: true).
     */
    public boolean isHubAssistanceEnabled() {
        return hubAssistanceEnabled;
    }

    public void setHubAssistanceEnabled(boolean hubAssistanceEnabled) {
        this.hubAssistanceEnabled = hubAssistanceEnabled;
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    public boolean isActive() {
        return this.active;
    }

    
}
