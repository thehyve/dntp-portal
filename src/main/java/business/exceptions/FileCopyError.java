/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value=HttpStatus.INTERNAL_SERVER_ERROR, reason="Error copying file.")
public class FileCopyError extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public FileCopyError() {
        super("Error copying file.");
    }

    public FileCopyError(String message) {
        super("Error copying file: " + message);
    }
}
