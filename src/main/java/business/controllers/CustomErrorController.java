/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.BasicErrorController;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
public class CustomErrorController implements ErrorController {

    private static final String PATH = "/error";

    @Autowired
    ErrorAttributes errorAttributes;

    ErrorProperties errorProperties;

    CustomErrorController() {
        this.errorProperties = new ErrorProperties();
        this.errorProperties.setPath(PATH);
    }

    @RequestMapping(value = PATH)
    public ResponseEntity<Map<String, Object>> error(HttpServletRequest request) {
        BasicErrorController errorController = new BasicErrorController(errorAttributes, errorProperties);
        return errorController.error(request);
    }

    @Override
    public String getErrorPath() {
        return PATH;
    }

}
