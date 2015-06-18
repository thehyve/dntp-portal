package business.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value=HttpStatus.BAD_REQUEST)  // 
public class EmptyInput extends RuntimeException {
    public EmptyInput() {
        super("Empty input field.");
    }
    
    public EmptyInput(String message) {
        super(message);
    }
}