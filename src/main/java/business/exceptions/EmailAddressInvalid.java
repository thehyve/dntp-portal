/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value=HttpStatus.BAD_REQUEST, reason="Email address invalid.")
public class EmailAddressInvalid extends RuntimeException {
    private static final long serialVersionUID = -2877015062907280457L;
    public EmailAddressInvalid(String email) {
        super("Email address invalid: " + email);
    }
    public EmailAddressInvalid() {
        super("Email address invalid.");
    }
}

