package business.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value= HttpStatus.INTERNAL_SERVER_ERROR, reason="Error while downloading PA numbers.")
public class PaNumbersDownloadError extends RuntimeException {
  public PaNumbersDownloadError() {
    super("\"Error while downloading PA numbers.");
  }
}
