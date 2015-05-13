package business.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value=HttpStatus.NOT_ACCEPTABLE, reason="Excerpt list upload error.")
public class ExcerptListUploadError extends RuntimeException {
    
    public ExcerptListUploadError() {
        super("Excerpt list upload error.");
    }

    public ExcerptListUploadError(String message) {
        super("Excerpt list upload error: " + message);
    }

}