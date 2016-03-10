/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * From http://www.jayway.com/2012/09/16/improve-your-spring-rest-api-part-i/
 */
@ControllerAdvice
public class CustomResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers,
            HttpStatus status, WebRequest request) {
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
        List<ObjectError> globalErrors = ex.getBindingResult() .getGlobalErrors();
        List<String> errors = new ArrayList<>(fieldErrors.size() + globalErrors.size());
        for (FieldError fieldError : fieldErrors) {
            errors.add(fieldError.getField() + ", " + fieldError.getDefaultMessage());
        }
        for (ObjectError objectError : globalErrors) {
            errors.add(objectError.getObjectName() + ", " + objectError.getDefaultMessage());
        }
        ErrorMessage errorMessage = new ErrorMessage(errors);
        return new ResponseEntity<Object>(errorMessage, headers, status);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex, HttpHeaders headers,
            HttpStatus status, WebRequest request) {
        String unsupported = "Unsupported content type: " + ex.getContentType();
        String supported = "Supported content types: " + MediaType.toString(ex.getSupportedMediaTypes());
        ErrorMessage errorMessage = new ErrorMessage(unsupported, supported);
        return new ResponseEntity<Object>(errorMessage, headers, status);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpHeaders headers,
            HttpStatus status, WebRequest request) {
        Throwable mostSpecificCause = ex.getMostSpecificCause();
        ErrorMessage errorMessage;
        if (mostSpecificCause != null) {
            String exceptionName = mostSpecificCause.getClass().getName();
            String message = mostSpecificCause.getMessage();
            errorMessage = new ErrorMessage(exceptionName, message);
        } else {
            errorMessage = new ErrorMessage(ex.getMessage());
        }
        return new ResponseEntity<Object>(errorMessage, headers, status);
    }
}