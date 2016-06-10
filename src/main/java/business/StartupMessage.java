package business;

import java.nio.charset.Charset;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import business.services.ExcerptListService;
import business.services.RequestService;

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
        log.info("Character set " + ExcerptListService.EXCERPT_LIST_CHARACTER_ENCODING + " supported: " +
                Boolean.toString(Charset.isSupported(ExcerptListService.EXCERPT_LIST_CHARACTER_ENCODING)));
        if (!RequestService.CSV_CHARACTER_ENCODING.equals(ExcerptListService.EXCERPT_LIST_CHARACTER_ENCODING)) {
            log.info("Character set " + RequestService.CSV_CHARACTER_ENCODING + " supported: " +
                    Boolean.toString(Charset.isSupported(RequestService.CSV_CHARACTER_ENCODING)));
        }
    }
}
