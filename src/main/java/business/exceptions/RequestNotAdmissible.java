package business.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value=HttpStatus.BAD_REQUEST)  // 
public class RequestNotAdmissible extends RuntimeException {
    public RequestNotAdmissible() {
        super("Request is not admissible.");
    }
}