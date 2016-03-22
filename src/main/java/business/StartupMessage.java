package business;

import java.nio.charset.Charset;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class StartupMessage implements ApplicationListener<ContextRefreshedEvent> {

    Log log = LogFactory.getLog(getClass());

    @Value("${info.build.name}")
    String applicationTitle;

    @Value("${info.build.version}")
    String applicationVersion;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent arg0) {
        log.info(String.format("Starting %s (%s).", applicationTitle, applicationVersion));
        log.info("Default character set: " + Charset.defaultCharset());
    }
}
