/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value=HttpStatus.INTERNAL_SERVER_ERROR, reason="File delete error.")
public class FileDeleteError extends RuntimeException {
    private static final long serialVersionUID = -7305899469837272938L;

    public FileDeleteError() {
        super("File delete error.");
    }
    
    public FileDeleteError(String message) {
        super("File delete error: " + message);
    }
}