/*
 * Copyright (C) 2019  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business.util;

import java.util.Arrays;
import java.util.stream.Collectors;

public class ExportUtils {

    /**
     * Replaces spaces (' ') with underscores ('_') in an array of strings.
     *
     * @param values the array of strings
     * @return the array with spaces replaces with underscores.
     */
    public static String[] replaceSpacesWithUnderscores(String[] values) {
        return Arrays.stream(values)
                .map(name -> name.replace(' ', '_'))
                .collect(Collectors.toList()).toArray(new String[] {});
    }

}
