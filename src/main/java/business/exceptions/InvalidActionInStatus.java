package business.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value=HttpStatus.METHOD_NOT_ALLOWED, reason="Action not allowed in current status.")
public class InvalidActionInStatus extends RuntimeException {
    private static final long serialVersionUID = 607177856129334391L;
}