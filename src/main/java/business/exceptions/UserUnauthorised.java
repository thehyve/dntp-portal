package business.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value=HttpStatus.UNAUTHORIZED)  // 
public class UserUnauthorised extends RuntimeException {
    public UserUnauthorised(String message) {
        super(message);
    }
}