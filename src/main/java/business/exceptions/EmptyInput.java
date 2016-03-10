/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value=HttpStatus.BAD_REQUEST)  // 
public class EmptyInput extends RuntimeException {
    public EmptyInput() {
        super("Empty input field.");
    }
    
    public EmptyInput(String message) {
        super(message);
    }
}