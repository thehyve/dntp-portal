package business.validation;

import org.junit.Assert;
import org.junit.Test;

public class PasswordValidatorTests {
    @Test
    public void rejectEmptyPassword() {
        Assert.assertFalse(PasswordValidator.validate(""));
    }

    @Test
    public void rejectShortPassword() {
        Assert.assertFalse(PasswordValidator.validate("1234"));
    }

    @Test
    public void acceptPasswords() {
        String[] acceptedPasswords = new String[] {
                "12345678",
                "hello_world",
                "you_shall_not_pass"
        };

        for (String password : acceptedPasswords) {
            Assert.assertTrue(PasswordValidator.validate(password));
        }
    }
}
