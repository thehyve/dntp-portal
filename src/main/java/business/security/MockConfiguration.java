package business.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.MailException;
import org.springframework.mail.MailPreparationException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessagePreparator;

import javax.mail.internet.MimeMessage;
import java.util.Properties;

/**
 *
 */
@Configuration
@Profile("dev")
public class MockConfiguration {

    @Bean
    public JavaMailSender mailSender() {
        return new MockMailSender();
    }

}

class MockMailSender extends JavaMailSenderImpl {

    @Override
    public void send(final MimeMessagePreparator mimeMessagePreparator) throws MailException {
        final MimeMessage mimeMessage = createMimeMessage();
        try {
            mimeMessagePreparator.prepare(mimeMessage);
            final String content = (String) mimeMessage.getContent();
            final Properties javaMailProperties = getJavaMailProperties();
            javaMailProperties.setProperty("mailContent", content);
        } catch (final Exception e) {
            throw new MailPreparationException(e);
        }
    }
}