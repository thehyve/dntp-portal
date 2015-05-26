package business.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value=HttpStatus.FORBIDDEN, reason="Update not allowed.")
public class UpdateNotAllowed extends RuntimeException {
    private static final long serialVersionUID = 4000154580392628894L;
    public UpdateNotAllowed() {
        super("Update not allowed. Not the owner.");
    }
}