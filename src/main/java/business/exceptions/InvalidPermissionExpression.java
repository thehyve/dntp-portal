package business.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value=HttpStatus.FORBIDDEN, reason="Invalid permission expression.")
public class InvalidPermissionExpression extends RuntimeException {

    private static final long serialVersionUID = -6899697912895646944L;
    
}