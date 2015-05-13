package business.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value=HttpStatus.BAD_REQUEST)  // 
public class InvalidLabNumber extends RuntimeException {
    public InvalidLabNumber() {
        super("Invalid lab number.");
    }
}