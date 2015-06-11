package business.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value=HttpStatus.INTERNAL_SERVER_ERROR, reason="Error while downloading excerpt list.")
public class ExcerptListDownloadError extends RuntimeException {
    public ExcerptListDownloadError() {
        super("Error while downloading excerpt list.");
    }
}