package business;

import business.controllers.UserController;
import business.models.ContactData;
import business.models.Lab;
import business.models.User;
import business.representation.ProfileRepresentation;
import business.security.MockConfiguration;
import business.security.UserAuthenticationToken;
import business.services.LabService;
import business.services.MailService;
import business.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

@Profile("dev")
@SpringApplicationConfiguration(classes = {Application.class})
@WebIntegrationTest("server.port = 8093")
public class UserControllerTests extends AbstractTestNGSpringContextTests {

    Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    UserService userService;

    @Autowired
    LabService labService;

    @Autowired
    UserController userController;

    @Autowired
    JavaMailSender mailSender;

    @Autowired
    AuthenticationProvider authenticationProvider;

    protected UserAuthenticationToken getRequester() {
        User user = userService.findByUsername("test+requester@dntp.thehyve.nl");
        Authentication authentication = new UsernamePasswordAuthenticationToken(user, "requester");
        return (UserAuthenticationToken)authenticationProvider.authenticate(authentication);
    }

    protected UserAuthenticationToken getPalga() {
        User user = userService.findByUsername("test+palga@dntp.thehyve.nl");
        Authentication authentication = new UsernamePasswordAuthenticationToken(user, "palga"); // because of password tests
        return (UserAuthenticationToken)authenticationProvider.authenticate(authentication);
    }

    @BeforeClass
    public void setUp() throws Exception {
        ((MockConfiguration.MockMailSender)this.mailSender).clear();
        log.info("TEST  Test: " + this.getClass().toString());
    }

    @Test
    public void testRegistration() throws InterruptedException, MessagingException {
        SecurityContextHolder.clearContext();
        ((MockConfiguration.MockMailSender)mailSender).clear();

        Lab lab = labService.findByNumber(1);

        ProfileRepresentation profile = new ProfileRepresentation();
        profile.setFirstName("Candidate");
        profile.setLastName("User");
        ContactData contactData = new ContactData();
        contactData.setEmail("test+user@dntp.thehyve.nl");
        profile.setContactData(contactData);
        profile.setLabId(lab.getId());
        profile.setPassword1("Password1!");
        profile.setPassword2("Password1!");
        userController.register(profile);

        User user = userService.findByUsername("test+user@dntp.thehyve.nl");
        assertNotNull(user);

        // Mail sending is asynchronous. Sleep for 1 second.
        Thread.sleep(1 * 1000);

        List<MimeMessage> emails = ((MockConfiguration.MockMailSender)mailSender).getMessages();
        assertEquals(1, emails.size());
        String subject = emails.get(0).getSubject();
        assertEquals(MailService.activationSubject, subject);
    }

    @Test
    public void testRegistrationUserAlreadyExists() throws InterruptedException, MessagingException {
        SecurityContextHolder.clearContext();
        ((MockConfiguration.MockMailSender)mailSender).clear();

        // check that the test user indeed already exists
        User user = userService.findByUsername("test+requester@dntp.thehyve.nl");
        assertNotNull(user);

        Lab lab = labService.findByNumber(1);

        ProfileRepresentation profile = new ProfileRepresentation();
        profile.setFirstName("Candidate");
        profile.setLastName("User");
        ContactData contactData = new ContactData();
        contactData.setEmail("test+requester@dntp.thehyve.nl");
        profile.setContactData(contactData);
        profile.setLabId(lab.getId());
        profile.setPassword1("Password1!");
        profile.setPassword2("Password1!");
        userController.register(profile);

        List<User> users = userService.findAll().stream().filter(u ->
                u.getUsername().equals("test+requester@dntp.thehyve.nl")
        ).collect(Collectors.toList());
        assertEquals(1, users.size());

        // Mail sending is asynchronous. Sleep for 1 second.
        Thread.sleep(1 * 1000);

        List<MimeMessage> emails = ((MockConfiguration.MockMailSender)mailSender).getMessages();
        assertEquals(1, emails.size());
        String subject = emails.get(0).getSubject();
        assertEquals(MailService.accountAlreadyExistsSubject, subject);
    }

}
