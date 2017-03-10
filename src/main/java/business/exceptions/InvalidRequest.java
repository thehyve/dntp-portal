package business.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.validation.ConstraintViolation;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class InvalidRequest extends RuntimeException {

    private Set<ConstraintViolation> constraintViolations;

    public Set<ConstraintViolation> getConstraintViolations() {
        return constraintViolations;
    }

    public <T> InvalidRequest(String message, Set<ConstraintViolation<T>> constraintViolations) {
        super(message);
        this.constraintViolations = new LinkedHashSet<>(constraintViolations);
    }

    public InvalidRequest(String message, Throwable cause) {
        super(message, cause);
        this.constraintViolations = Collections.emptySet();
    }

    public InvalidRequest(String message) {
        super(message);
        this.constraintViolations = Collections.emptySet();
    }

}
