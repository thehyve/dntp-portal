/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value=HttpStatus.INTERNAL_SERVER_ERROR, reason="Error while downloading excerpt list.")
public class ExcerptListDownloadError extends RuntimeException {
    public ExcerptListDownloadError() {
        super("Error while downloading excerpt list.");
    }
}