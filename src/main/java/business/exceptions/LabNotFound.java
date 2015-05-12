package business.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value=HttpStatus.NOT_FOUND)  // 
public class LabNotFound extends RuntimeException {
    public LabNotFound() {
        super("Lab cannot be found.");
    }
}