/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value=HttpStatus.INTERNAL_SERVER_ERROR, reason = "Email error.")
public class EmailError extends RuntimeException {

    private static final long serialVersionUID = -6519420956992848556L;

    public EmailError(String message, Throwable cause) {
        super(message, cause);
    }
    public EmailError(String message) {
        super(message);
    }

}
