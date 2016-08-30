/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business.security;

import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class CacheControlConfiguration extends WebMvcConfigurerAdapter {

    static final CacheControl cacheControl = CacheControl.maxAge(3600, TimeUnit.SECONDS).mustRevalidate();

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Add caching for static resources (1 hour).
        registry.addResourceHandler("/**")
            .addResourceLocations("classpath:/static/")
            .setCachePeriod(3600)
            .setCacheControl(cacheControl);
    }

}
