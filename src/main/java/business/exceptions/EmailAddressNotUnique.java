/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business.exceptions;

public class EmailAddressNotUnique extends RuntimeException {
    private static final long serialVersionUID = 6789077965053928047L;
    public EmailAddressNotUnique(String message) {
        super(message);
    }
    public EmailAddressNotUnique() {
        super("Email address not available.");
    }
}