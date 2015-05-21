package business.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.BasicErrorController;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
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

    @RequestMapping(value = PATH)
    public ResponseEntity<Map<String, Object>> error(HttpServletRequest request) {
        BasicErrorController errorController = new BasicErrorController(errorAttributes);
        return errorController.error(request);
    }

    @Override
    public String getErrorPath() {
        return PATH;
    }
}