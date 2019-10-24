/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.internet.MimeMessage;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import business.models.NewPasswordRequest;
import business.models.NewPasswordRequestRepository;
import business.models.User;
import business.models.UserRepository;
import business.representation.EmailRepresentation;
import business.representation.NewPasswordRepresentation;
import business.security.MockConfiguration.MockMailSender;
import business.services.MailService;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.context.WebApplicationContext;

@Profile("dev")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@ContextConfiguration
public class PasswordControllerTests {

    private final Logger log = LoggerFactory.getLogger(PasswordControllerTests.class);

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private NewPasswordRequestRepository passwordRequestRepository;

    @Autowired
    private UserRepository userRepository;

    private MockMvc mockMvc;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .build();
    }

    static final String PASSWORD_TEST_ACCOUNT = "test+lab_user99@dntp.thehyve.nl";

    @Test
    public void requestNewPasswordSendsCorrectEmail() throws Exception {
        ((MockMailSender)this.mailSender).clear();

        EmailRepresentation emailForm = new EmailRepresentation(PASSWORD_TEST_ACCOUNT);

        // Perform the request and ensure we get a correct result status
        mockMvc.perform(MockMvcRequestBuilders.put("/api/password/request-new")
                .content(new ObjectMapper().writeValueAsString(emailForm))
                .contentType("application/json")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(result -> log.info("TEST: requestNewPassword()\n" +
                        result.getResponse().getStatus() +
                        "\n" +
                        result.getResponse().getContentAsString()))
                .andExpect(status().isOk());

        // Mail sending is asynchronous. Sleep for 1 second.
        Thread.sleep(1 * 1000);

        // Check that the link has been sent to the right user, only once
        Assert.assertEquals(mailSender.getClass(), MockMailSender.class);
        List<MimeMessage> emails = ((MockMailSender)mailSender).getMessages();
        Assert.assertEquals(1, emails.size());
        Assert.assertEquals(MailService.passwordRecoverySubject, emails.get(0).getSubject());
        Assert.assertEquals(emailForm.getEmail(), emails.get(0).getAllRecipients()[0].toString());

        // Check that the link exists in the database and that it is associated to the same username
        String emailBody = emails.get(0).getContent().toString();
        Pattern r = Pattern.compile("login/reset-password/([^\\s\\.]*)");
        Matcher m = r.matcher(emailBody);
        m.find();

        log.debug("Email body: `" + emailBody + "`");

        String link = m.group(1);
        log.debug("link: " + link);
        NewPasswordRequest passwordRequest = passwordRequestRepository.findByToken(link);
        Assert.assertNotNull(passwordRequest);
        Assert.assertEquals(passwordRequest.getUser().getUsername(), emailForm.getEmail());
    }

    @Test
    public void requestNewPasswordDoesNothingForUnknownUser() throws Exception {
        ((MockMailSender)this.mailSender).clear();

        EmailRepresentation emailForm = new EmailRepresentation("somebody@nowhere.com");

        // Perform the request and ensure we get a correct result status
        mockMvc.perform(MockMvcRequestBuilders.put("/api/password/request-new")
                .content(new ObjectMapper().writeValueAsString(emailForm))
                .contentType("application/json")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(result -> log.info("TEST: requestNewPassword()\n" +
                        result.getResponse().getStatus() +
                        "\n" +
                        result.getResponse().getContentAsString()))
                .andExpect(status().isOk());

        // Mail sending is asynchronous. Sleep for 1 second.
        Thread.sleep(1 * 1000);

        // Check that an email has been sent
        List<MimeMessage> emails = ((MockMailSender)mailSender).getMessages();
        Assert.assertEquals(1, emails.size());
        Assert.assertEquals(MailService.passwordRecoveryUserUnknownSubject, emails.get(0).getSubject());
        Assert.assertEquals(emailForm.getEmail(), emails.get(0).getAllRecipients()[0].toString());
    }

    @Test
    public void resetPassword() throws Exception {
        // Insert a password reset request in the database
        User user = userRepository.findByUsername(PASSWORD_TEST_ACCOUNT);
        NewPasswordRequest npr = new NewPasswordRequest(user);
        passwordRequestRepository.saveAndFlush(npr);

        String newPassword = "12345678%ABCdef";

        // Call password reset with the token
        NewPasswordRepresentation npRepr = new NewPasswordRepresentation(newPassword, npr.getToken());
        mockMvc.perform(MockMvcRequestBuilders.post("/api/password/reset")
                .content(new ObjectMapper().writeValueAsString(npRepr))
                .contentType("application/json")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(result -> log.info("TEST: setPassword()\n" +
                        result.getResponse().getStatus() +
                        "\n" +
                        result.getResponse().getContentAsString()))
                .andExpect(status().isOk());

        // Check that the password has been changed
        user = userRepository.findOne(user.getId());
        Assert.assertTrue(passwordEncoder.matches(newPassword, user.getPassword()));

        // Check that the reset link doesn't exist anymore
        Assert.assertNull(passwordRequestRepository.findByToken(npr.getToken()));
    }

}
