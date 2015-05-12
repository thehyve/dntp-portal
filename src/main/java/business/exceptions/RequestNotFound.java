package business.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value=HttpStatus.NOT_FOUND, reason="Request not found.")  // 404
public class RequestNotFound extends RuntimeException {
    private static final long serialVersionUID = 607177856129334391L;
}