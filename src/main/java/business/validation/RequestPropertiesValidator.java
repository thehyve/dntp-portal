package business.validation;

import business.models.RequestProperties;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Validates the fields of a request properties object that are not checked in the
 * request representation {@link business.representation.RequestRepresentation}.
 */
public class RequestPropertiesValidator implements Validator {
    @Override
    public boolean supports(Class<?> aClass) {
        return RequestProperties.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        RequestProperties properties = (RequestProperties) o;
        if (properties.getInformedConsentFormAttachments().isEmpty()) {
            errors.rejectValue("informedConsentFormAttachments", "field.required",
                    "The informed consent form is mandatory.");
        }
    }
}
