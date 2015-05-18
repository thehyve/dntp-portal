package business.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value=HttpStatus.BAD_REQUEST, reason="Identifier is null.")
public class NullIdentifier extends RuntimeException {

    private static final long serialVersionUID = 830272783214319532L;

}