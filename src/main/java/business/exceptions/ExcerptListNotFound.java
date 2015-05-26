package business.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value=HttpStatus.NOT_FOUND, reason="Excerpt list not found.")
public class ExcerptListNotFound extends RuntimeException {
    public ExcerptListNotFound() {
        super("Excerpt list not found.");
    }
}