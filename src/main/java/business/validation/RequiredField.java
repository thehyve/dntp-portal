package business.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = RequiredFieldValidator.class)
public @interface RequiredField {
    String message() default "Field required";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
