package business.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value=HttpStatus.UNAUTHORIZED, reason="Not logged in.")
public class NotLoggedInException extends RuntimeException {
    private static final long serialVersionUID = -2361055636793206513L;
}