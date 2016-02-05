package business.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value=HttpStatus.BAD_REQUEST, reason="Password change failed.")
public class PasswordChangeFailed extends RuntimeException {
    private static final long serialVersionUID = 3396082870753983510L;

    public PasswordChangeFailed() {
        super("Password change failed.");
    }
    
    public PasswordChangeFailed(String message) {
        super(message);
    }
}