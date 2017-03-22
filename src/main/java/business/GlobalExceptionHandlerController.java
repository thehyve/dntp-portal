/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business;

import business.exceptions.InvalidRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import business.exceptions.ExcerptListUploadError;
import business.exceptions.ExcerptSelectionUploadError;

import javax.validation.ConstraintViolation;
import java.util.ArrayList;
import java.util.List;

@ControllerAdvice
public class GlobalExceptionHandlerController {

    Log log = LogFactory.getLog(getClass());

    @ExceptionHandler(ExcerptListUploadError.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ResponseEntity<String> handleExcerptListUploadError(ExcerptListUploadError e) {
        log.error("ExcerptListUploadError: " + e.getMessage());
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ExcerptSelectionUploadError.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ResponseEntity<String> handleExcerptListUploadError(ExcerptSelectionUploadError e) {
        log.error("ExcerptSelectionUploadError: " + e.getMessage());
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidRequest.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ResponseEntity<Object> handleInvalidRequest(InvalidRequest e) {
        log.error("InvalidRequest: " + e.getMessage());
        List<String> errors = new ArrayList<>(e.getConstraintViolations().size());
        for (ConstraintViolation violation : e.getConstraintViolations()) {
            errors.add(violation.getMessage());
        }
        ErrorMessage errorMessage = new ErrorMessage(errors);
        return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

}
