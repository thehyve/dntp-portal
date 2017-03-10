package business.validation;

import business.models.ContactData;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ValidAddressValidator implements ConstraintValidator<ValidAddress, ContactData>  {

    @Override
    public void initialize(ValidAddress validAddress) {

    }

    private static boolean isEmpty(String text) {
        return text == null || text.isEmpty();
    }

    @Override
    public boolean isValid(ContactData contactData, ConstraintValidatorContext constraintValidatorContext) {
        if (contactData != null) return false;
        if (isEmpty(contactData.getAddress1())) {
            return false;
        }
        if (isEmpty(contactData.getPostalCode())) {
            return false;
        }
        if (isEmpty(contactData.getCity())) {
            return false;
        }
        return true;
    }

}
