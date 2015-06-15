package business.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value=HttpStatus.NOT_FOUND, reason="User not found.")  // 404
public class UserNotFound extends RuntimeException {
    private static final long serialVersionUID = -7666653096938904964L;
}