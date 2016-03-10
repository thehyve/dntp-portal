/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value=HttpStatus.BAD_REQUEST, reason="Email address not available.")
public class EmailAddressNotAvailable extends RuntimeException {
    private static final long serialVersionUID = -2294620434526249799L;
    public EmailAddressNotAvailable(String message) {
        super(message);
    }
    public EmailAddressNotAvailable() {
        super("Email address not available.");
    }
}