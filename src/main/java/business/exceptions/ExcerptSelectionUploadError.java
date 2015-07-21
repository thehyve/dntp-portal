package business.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value=HttpStatus.BAD_REQUEST, reason="Excerpt selection upload error.")
public class ExcerptSelectionUploadError extends RuntimeException {
    
    public ExcerptSelectionUploadError() {
        super("Excerpt selection upload error.");
    }

    public ExcerptSelectionUploadError(String message) {
        super(message);
    }

}