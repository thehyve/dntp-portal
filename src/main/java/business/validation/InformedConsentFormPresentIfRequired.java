package business.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Checks if the informed consent form has been uploaded if
 * the request has data linkage and requires informed consent.
 * See {@link InformedConsentFormPresentIfRequiredValidator}.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = InformedConsentFormPresentIfRequiredValidator.class)
public @interface InformedConsentFormPresentIfRequired {
    String message() default "No informed consent form has been uploaded.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
