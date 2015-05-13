package business.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value=HttpStatus.INTERNAL_SERVER_ERROR, reason="File upload error.")
public class FileUploadError extends RuntimeException {
    private static final long serialVersionUID = 51403280891772531L;
    public FileUploadError() {
        super("File upload error.");
    }
    
    public FileUploadError(String message) {
        super("File upload error: " + message);
    }
}