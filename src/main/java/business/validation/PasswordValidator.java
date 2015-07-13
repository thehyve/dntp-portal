package business.validation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PasswordValidator {

    /**
     * Create Matcher
     *
     * @param regex
     * @param password
     * @return
     */
    private static Matcher createMatcher(String regex, String password) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(password);
        return matcher;
    }

    /**
     * Password should contains following characters
     * - alphabets in lower or capital letters
     * - numerical chars
     * - special chars ?=!*'();:@&=+$,/?#
     *
     * @param password
     * @return true if valid, false if not valid
     */
    public static boolean validate(String password) {

        boolean validPassword = false;

        if (password != null) {

            String numericalRegex = "(?=.*[0-9])";
            String alphabeticalRegex = "(?=.*[a-z])";
            String specialCharsRegex = "(?=.*[@#$%^&+=])";
            String atLeastEightCharsRegex = ".{8,}";

            Matcher numericalMatcher = createMatcher(numericalRegex, password);
            Matcher alphabeticalMatcher = createMatcher(alphabeticalRegex, password);
            Matcher specialCharsMatcher = createMatcher(specialCharsRegex, password);
            Matcher minLengthMatcher = createMatcher(atLeastEightCharsRegex, password);

            if (minLengthMatcher.find()) {
                if (specialCharsMatcher.find()) {
                    if (alphabeticalMatcher.find() || numericalMatcher.find()) {
                        validPassword = true;
                    }
                }
            }
        }
        return validPassword;
    }
}
