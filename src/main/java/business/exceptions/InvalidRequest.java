package business.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.validation.ConstraintViolation;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class InvalidRequest extends RuntimeException {

    private static final Logger log = LoggerFactory.getLogger(InvalidRequest.class);

    private Set<ConstraintViolation> constraintViolations;

    public Set<ConstraintViolation> getConstraintViolations() {
        return constraintViolations;
    }

    public <T> InvalidRequest(String message, Set<ConstraintViolation<T>> constraintViolations) {
        super(message);
        this.constraintViolations = new LinkedHashSet<>(constraintViolations);
        for(ConstraintViolation<T> violation: constraintViolations) {
            log.debug("Constraint violation: {}", violation);
        }
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
