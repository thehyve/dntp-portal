package business.security;

import java.util.Date;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

@Component
public class CustomLoggingInterceptor extends HandlerInterceptorAdapter {

    Log log = LogFactory.getLog(getClass());
    
    @Autowired
    private ServletContext context;
    
    public CustomLoggingInterceptor() {
        log.info("Logging component started");
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
            HttpServletResponse response, Object handler) throws Exception {
        log.trace(String.format("%s\t%s\t%s\t%s\t%s", 
                new Date(), 
                request.getRemoteAddr(), 
                request.getUserPrincipal() == null ? " - " : request.getUserPrincipal().getName(),
                request.getMethod(),
                request.getRequestURI())
                );
        return super.preHandle(request, response, handler);
    }
    
    @Override
    public void postHandle(HttpServletRequest request,
            HttpServletResponse response, Object handler, ModelAndView model) throws Exception {
        log.trace(String.format("%s\t%s\t%s\t%s\t%s\t%d", 
                new Date(), 
                request.getRemoteAddr(), 
                request.getUserPrincipal() == null ? " - " : request.getUserPrincipal().getName(),
                request.getMethod(),
                request.getRequestURI(),
                response.getStatus())
                );
        super.postHandle(request, response, handler, model);
    }
}
