/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business.representation;

public class EmailRepresentation {
    private String email;

    public EmailRepresentation() {}

    public EmailRepresentation(String email) {
        this.email = email.toLowerCase();
    }

    public String getEmail() {
        return this.email;
    }
}
