package business.validation;

import business.representation.RequestRepresentation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validates the fields of a request representation object that are not checked in the
 * request representation {@link business.representation.RequestRepresentation}.
 */
public class InformedConsentFormPresentIfRequiredValidator implements ConstraintValidator<InformedConsentFormPresentIfRequired, RequestRepresentation> {

    @Override
    public void initialize(InformedConsentFormPresentIfRequired constraintAnnotation) {

    }

    @Override
    public boolean isValid(RequestRepresentation value, ConstraintValidatorContext context) {
        if (value.isLinkageWithPersonalData() && value.isInformedConsent()) {
            if (value.getInformedConsentFormAttachments().isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
