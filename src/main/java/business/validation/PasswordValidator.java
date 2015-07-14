package business.validation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PasswordValidator {

    private static int MIN_PASSWORD_LENGTH = 8;
    
    private static String numericalRegex = "(?=.*[0-9])"; // at least one numerical
    private static String alphabeticalRegex = "(?=.*[a-zA-Z])"; // at least one alphabet
    private static String specialCharsRegex = "(?=.*[^a-zA-Z0-9 ])"; // at least one special chars
    
    private static Pattern numericalPattern = Pattern.compile(numericalRegex);
    private static Pattern alphabeticalPattern = Pattern.compile(alphabeticalRegex);
    private static Pattern specialsCharsPattern = Pattern.compile(specialCharsRegex);
    
    /**
     * Password should contains following characters
     * - alphabets in lower or capital letters
     * - numerical chars
     * - special chars (not numerical or alphabetical)
     *
     * @param password
     * @return true if valid, false if not valid
     */
    public static boolean validate(String password) {

        boolean validPassword = false;

        if (password != null) {

            Matcher numericalMatcher = numericalPattern.matcher(password);
            Matcher alphabeticalMatcher = alphabeticalPattern.matcher(password);
            Matcher specialCharsMatcher = specialsCharsPattern.matcher(password);

            if (password.length() >= MIN_PASSWORD_LENGTH) {
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
