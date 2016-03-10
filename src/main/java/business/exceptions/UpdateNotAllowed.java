/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value=HttpStatus.FORBIDDEN, reason="Update not allowed.")
public class UpdateNotAllowed extends RuntimeException {
    private static final long serialVersionUID = 4000154580392628894L;
    public UpdateNotAllowed() {
        super("Update not allowed in this status.");
    }
}