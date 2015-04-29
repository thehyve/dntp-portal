package business.controllers;

import business.Application;
import business.models.NewPasswordRequest;
import business.models.NewPasswordRequestRepository;
import business.models.User;
import business.models.UserRepository;
import business.representation.EmailRepresentation;
import business.representation.NewPasswordRepresentation;
import business.util.DefaultUsers;
import business.util.Util;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.EmbeddedWebApplicationContext;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.mail.internet.MimeMessage;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {Application.class})
@WebIntegrationTest("server.port = 8093")
public class PasswordResetTests {
    @Autowired
    private EmbeddedWebApplicationContext webApplicationContext;

    @Autowired
    private NewPasswordRequestRepository passwordRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private business.security.DefaultUsers defaultUsers;

    private MockMvc mockMvc;
    public GreenMail greenMail;

    @Before
    public void setUp() {
        // Reset database
        //defaultUsers.resetDatabase();

        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .build();

        this.greenMail = new GreenMail(ServerSetupTest.SMTP);
    }

    @Test
    public void requestNewPasswordSendsCorrectEmail() throws Exception {
        EmailRepresentation emailForm = new EmailRepresentation(DefaultUsers.Admin.getEmail());
        greenMail.start();

        // Perform the request and ensure we get a correct result status
        Util.performHttpRequest(mockMvc, HttpMethod.PUT, "/password/request-new", emailForm);

        // Check that the link has been sent to the right user, only once
        MimeMessage[] emails = greenMail.getReceivedMessages();
        Assert.assertEquals(1, emails.length);
        Assert.assertEquals("Password recovery", emails[0].getSubject());
        Assert.assertEquals(emailForm.getEmail(), emails[0].getAllRecipients()[0].toString());

        // Check that the link exists in the database and that it is associated to the same username
        String emailBody = emails[0].getContent().toString();
        Pattern r = Pattern.compile("login/reset-password/(.*)");
        Matcher m = r.matcher(emailBody);
        m.find();

        LogFactory.getLog(getClass()).info("Email body: `" + emailBody + "`");

        String link = m.group(1);
        NewPasswordRequest passwordRequest = passwordRequestRepository.findByToken(link);
        Assert.assertNotNull(passwordRequest);
        Assert.assertEquals(passwordRequest.getUser().getUsername(), emailForm.getEmail());

        greenMail.stop();
    }

    @Test
    public void requestNewPasswordDoesNothingForUnknownUser() throws Exception {
        EmailRepresentation emailForm = new EmailRepresentation("somebody@nowhere.com");
        greenMail.start();

        // Perform the request and ensure we get a correct result status
        Util.performHttpRequest(mockMvc, HttpMethod.PUT, "/password/request-new", emailForm);

        // Check that no email has been sent
        MimeMessage[] emails = greenMail.getReceivedMessages();
        Assert.assertEquals(0, emails.length);

        greenMail.stop();
    }

    @Test
    public void resetPasswordWorks() throws Exception {
        // Insert a password reset request in the database
        User user = userRepository.findByUsername(DefaultUsers.Admin.getEmail());
        NewPasswordRequest npr = new NewPasswordRequest(user);
        passwordRequestRepository.saveAndFlush(npr);

        // Call password reset with the token
        NewPasswordRepresentation npRepr = new NewPasswordRepresentation("12345678", npr.getToken());
        Util.performHttpRequest(mockMvc, HttpMethod.POST, "/password/reset", npRepr);

        // Check that the password has been changed
        user = userRepository.findOne(user.getId());
        Assert.assertEquals("12345678", user.getPassword());

        // Check that the reset link doesn't exist anymore
        Assert.assertNull(passwordRequestRepository.findByToken(npr.getToken()));

        // Restore original password
        user.setPassword(DefaultUsers.Admin.getPassword());
        userRepository.save(user);
    }

    @Test
    public void resetPasswordRejectsInvalidPassword() throws Exception {
        // Insert a password reset request in the database
        User user = userRepository.findByUsername(DefaultUsers.Admin.getEmail());
        NewPasswordRequest npr = new NewPasswordRequest(user);
        passwordRequestRepository.saveAndFlush(npr);

        // Call password reset with the token
        // The new password is invalid, since it has only 4 characters
        NewPasswordRepresentation npRepr = new NewPasswordRepresentation("1234", npr.getToken());
        Util.performHttpRequest(mockMvc, HttpMethod.POST, "/password/reset", npRepr, status().is4xxClientError());

        // Check that the password has not been changed
        user = userRepository.findOne(user.getId());
        Assert.assertEquals(DefaultUsers.Admin.getPassword(), user.getPassword());

        // Check that the reset link still exists
        Assert.assertNotNull(passwordRequestRepository.findByToken(npr.getToken()));
    }
}
