/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business.validation;

import static  org.junit.Assert.*;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import business.security.SecureTokenGenerator;


public class PasswordValidatorTests {

    @Autowired
    PasswordValidator passwordValidator;

    @Test
    public void testNullPasswordValidation() throws Exception {
        assertFalse(PasswordValidator.validate(null));
    }

    @Test
    public void testEmptyStringPasswordValidation() throws Exception {
        assertFalse(PasswordValidator.validate(""));
    }

    @Test
    public void testOneSpacePasswordValidation() throws Exception {
        assertFalse(PasswordValidator.validate(" "));
    }

    @Test
    public void testSpacePasswordValidation() throws Exception {
        assertFalse(PasswordValidator.validate("                  "));
    }

    @Test
    public void testWrongLengthValidation() throws Exception {
        assertFalse(PasswordValidator.validate("3@sdfk"));
    }

    @Test
    public void testAllCharsValidation() throws Exception {
        assertFalse(PasswordValidator.validate("asdfghjk"));
    }

    @Test
    public void testAllNumbersValidation() throws Exception {
        assertFalse(PasswordValidator.validate("99999999"));
    }

    @Test
    public void testNoSpecialCharsValidation() throws Exception {
        assertFalse(PasswordValidator.validate("9999abcd"));
    }

    @Test
    public void testNoCharsValidation() throws Exception {
        assertTrue(PasswordValidator.validate("9999!@#$%"));
    }

    @Test
    public void testNoNumbersValidation() throws Exception {
        assertTrue(PasswordValidator.validate("abcd!@#$%"));
    }

    @Test
    public void testValidPasswordValidation1() throws Exception {
        assertTrue(PasswordValidator.validate("213abc!@"));
    }

    @Test
    public void testValidPasswordValidation2() throws Exception {
        assertTrue(PasswordValidator.validate("abc!@213"));
    }

    @Test
    public void testValidPasswordValidation3() throws Exception {
        assertTrue(PasswordValidator.validate("!@213abc"));
    }

    @Test
    public void testValidPasswordValidation4() throws Exception {
        assertTrue(PasswordValidator.validate("abc123&*"));
    }

    @Test
    public void testValidPasswordWithSpaceValidation() throws Exception {
        assertTrue(PasswordValidator.validate("abc 123 !@#"));
    }
    @Test
    public void testValidAllSpecialCharsValidation() throws Exception {
        assertTrue(PasswordValidator.validate("?=!*'();:@&=+$,/?#a0"));
    }

    @Test
    public void testPasswordGenerator() {
        for(int i = 0; i < 20; i++) {
            String password = SecureTokenGenerator.generatePassword();
            assertTrue(PasswordValidator.validate(password));
        }
    }

}
