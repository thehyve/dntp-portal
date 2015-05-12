package business.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value=HttpStatus.NOT_FOUND, reason="Attachment not found for request.")  // 404
public class AttachmentNotFound extends RuntimeException {

    private static final long serialVersionUID = -3306430106255243401L;
    
}