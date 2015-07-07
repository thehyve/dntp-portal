package business.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class LoggingConfiguration extends WebMvcConfigurerAdapter {

    @Autowired
    CustomLoggingInterceptor customLoggingInterceptor;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        super.addInterceptors(registry);
        registry.addInterceptor(customLoggingInterceptor);
    }

}
