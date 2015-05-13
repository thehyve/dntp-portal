package business.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value=HttpStatus.BAD_REQUEST)  // 
public class LabuserWithoutLab extends RuntimeException {
    public LabuserWithoutLab(String message) {
        super(message);
    }
}