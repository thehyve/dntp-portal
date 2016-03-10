/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business.models;

import business.security.SecureTokenGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
public class ActivationLink {
    @Id
    @GeneratedValue
    private Long requestId;

    @ManyToOne(optional = false)
    private User user;

    private Date creationDate;
    private String token;

    public ActivationLink() {}

    public ActivationLink(@NotNull User user) {
        this.user = user;
        this.creationDate = new Date();
        this.token = SecureTokenGenerator.generateToken();
    }

    public Long getRequestId() {
        return requestId;
    }

    public User getUser() {
        return this.user;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public String getToken() {
        return this.token;
    }
}
