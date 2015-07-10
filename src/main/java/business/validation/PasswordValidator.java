package business.validation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PasswordValidator {

    public static boolean validate(String password) {

        // =====================================================================
        // REGEX (?=.*[!*'();:@&=+$,/?#[\]])+[a-zA-Z0-9!*'();:@&=+$,/?#[\]]{8,}
        // at least one or more special chars and alphanumeric min 8 chars length
        // =====================================================================

        String regex = "\\(?=.*\\[!*'\\(\\);:@&=+$,/?#\\[\\\\]\\]\\)+\\[a-zA-Z0-9!*'\\(\\);:@&=+$,/?#\\[\\\\]\\]\\{8,\\}";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(password);
        return matcher.find();
    }
}
