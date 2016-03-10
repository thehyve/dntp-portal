/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value=HttpStatus.INTERNAL_SERVER_ERROR, reason="File download error.")
public class FileDownloadError extends RuntimeException {

    private static final long serialVersionUID = -5240643155513205816L;

    public FileDownloadError() {
        super("File download error.");
    }
    
    public FileDownloadError(String message) {
        super("File download error: " + message);
    }
}