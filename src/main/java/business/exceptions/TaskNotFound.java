package business.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value=HttpStatus.NOT_FOUND, reason="No task for request.")  // 404
public class TaskNotFound extends RuntimeException {
    private static final long serialVersionUID = -2361055636793206513L;
}