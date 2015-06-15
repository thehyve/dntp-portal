package business.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value=HttpStatus.BAD_REQUEST)  // 
public class InvalidUserData extends RuntimeException {
    private static final long serialVersionUID = -7706933733462824596L;
    public InvalidUserData(String message) {
        super(message);
    }
}