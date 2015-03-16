package business;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;


/**
 * From http://www.jayway.com/2012/09/16/improve-your-spring-rest-api-part-i/
 */
@XmlRootElement
public class ErrorMessage {

    private List<String> errors;

    public ErrorMessage() {
    }

    public ErrorMessage(List<String> errors) {
        this.errors = errors;
    }

    public ErrorMessage(String error) {
        this(Collections.singletonList(error));
    }

    public ErrorMessage(String... errors) {
        this(Arrays.asList(errors));
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }
}