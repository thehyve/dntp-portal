/*
 * Copyright (C) 2016-2019  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business.services;

import org.springframework.format.datetime.DateFormatter;

import java.util.Date;
import java.util.Locale;

public class ExportFormatHelper {

    final static Locale LOCALE = Locale.getDefault();

    final static DateFormatter DATE_FORMATTER = new DateFormatter("yyyy-MM-dd");

    public static String dateToString(Date value) {
        if (value == null) {
            return "";
        }
        return DATE_FORMATTER.print(value, LOCALE);
    }

    public static String booleanToString(Boolean value) {
        if (value == null) {
            return "";
        } else if (value) {
            return "Yes";
        } else {
            return "No";
        }
    }

}
