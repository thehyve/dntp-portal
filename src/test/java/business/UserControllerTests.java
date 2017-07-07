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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

@Profile("dev")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@ContextConfiguration
public class UserControllerTests {

    private final Logger log = LoggerFactory.getLogger(UserControllerTests.class);

    @Autowired
    private UserService userService;

    @Autowired
    private LabService labService;

    @Autowired
    private UserController userController;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private AuthenticationProvider authenticationProvider;

    @Before
    public void setup() throws Exception {
        ((MockConfiguration.MockMailSender)this.mailSender).clear();
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
