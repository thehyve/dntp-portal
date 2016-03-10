/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business.models;

import business.security.SecureTokenGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
public class NewPasswordRequest {
    @Id
    @GeneratedValue
    private Long requestId;

    @ManyToOne(optional = false)
    private User user;

    private Date creationDate;

    @Column(unique = true)
    private String token;

    public NewPasswordRequest() {}

    public NewPasswordRequest(@NotNull User user) {
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
