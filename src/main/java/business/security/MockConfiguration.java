package business.security;

import java.util.ArrayList;
import java.util.List;

import javax.mail.internet.MimeMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

/**
 *
 */
@Configuration
@Profile("dev")
public class MockConfiguration {

    Log log = LogFactory.getLog(this.getClass());
    
    @Bean
    public JavaMailSender mailSender() {
        log.info("Initialising mock mail sender.");
        return new MockMailSender();
    }

    public class MockMailSender extends JavaMailSenderImpl {

        List<MimeMessage> messages = new ArrayList<MimeMessage>();
        
        public List<MimeMessage> getMessages() {
            return messages;
        }
        
        public void clear() {
            messages = new ArrayList<MimeMessage>();
        }
        
        @Override
        public void send(MimeMessage mimeMessage) throws MailException {
            log.info("MockMailSender: send mime mail message.");
            messages.add(mimeMessage);
            log.info("MockMailSender: mail sent. #messages = " + messages.size());
        };
        
    }    
    
}

