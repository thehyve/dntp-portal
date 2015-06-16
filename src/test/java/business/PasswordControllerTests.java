package business;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.internet.MimeMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.EmbeddedWebApplicationContext;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultHandler;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import business.models.NewPasswordRequest;
import business.models.NewPasswordRequestRepository;
import business.models.User;
import business.models.UserRepository;
import business.representation.EmailRepresentation;
import business.representation.NewPasswordRepresentation;
import business.security.MockConfiguration.MockMailSender;

import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {Application.class})
@WebIntegrationTest("server.port = 8093")
public class PasswordControllerTests {
    
    Log log = LogFactory.getLog(this.getClass());
    
    @Autowired
    private EmbeddedWebApplicationContext webApplicationContext;

    @Autowired
    private NewPasswordRequestRepository passwordRequestRepository;

    @Autowired
    private UserRepository userRepository;

    private MockMvc mockMvc;
    //public GreenMail greenMail;
    
    @Autowired
    JavaMailSender mailSender;

    @Autowired
    PasswordEncoder passwordEncoder;
    
    @Before
    public void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        //this.greenMail = new GreenMail(ServerSetupTest.SMTP);
        ((MockMailSender)this.mailSender).clear();
    }

    @Test
    public void requestNewPasswordSendsCorrectEmail() throws Exception {
        EmailRepresentation emailForm = new EmailRepresentation("palga@dntp.thehyve.nl");
        //greenMail.start();

        // Perform the request and ensure we get a correct result status
        mockMvc.perform(MockMvcRequestBuilders.put("/password/request-new")
                .content(new ObjectMapper().writeValueAsString(emailForm))
                .contentType("application/json")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(new ResultHandler() {
                    @Override
                    public void handle(MvcResult result) throws Exception {
                        log.info("TEST: requestNewPassword()\n" +
                                result.getResponse().getStatus() +
                                "\n" +
                                result.getResponse().getContentAsString());
                    }
                })
                .andExpect(status().isOk());

        // Check that the link has been sent to the right user, only once
        Assert.assertEquals(mailSender.getClass(), MockMailSender.class);
        List<MimeMessage> emails = ((MockMailSender)mailSender).getMessages();
        Assert.assertEquals(1, emails.size());
        Assert.assertEquals("Password recovery", emails.get(0).getSubject());
        Assert.assertEquals(emailForm.getEmail(), emails.get(0).getAllRecipients()[0].toString());

        // Check that the link exists in the database and that it is associated to the same username
        String emailBody = emails.get(0).getContent().toString();
        Pattern r = Pattern.compile("login/reset-password/(.*)");
        Matcher m = r.matcher(emailBody);
        m.find();

        log.info("Email body: `" + emailBody + "`");

        String link = m.group(1);
        NewPasswordRequest passwordRequest = passwordRequestRepository.findByToken(link);
        Assert.assertNotNull(passwordRequest);
        Assert.assertEquals(passwordRequest.getUser().getUsername(), emailForm.getEmail());

        //greenMail.stop();
    }

    @Test
    public void requestNewPasswordDoesNothingForUnknownUser() throws Exception {
        EmailRepresentation emailForm = new EmailRepresentation("somebody@nowhere.com");
        //greenMail.start();

        // Perform the request and ensure we get a correct result status
        mockMvc.perform(MockMvcRequestBuilders.put("/password/request-new")
                .content(new ObjectMapper().writeValueAsString(emailForm))
                .contentType("application/json")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(new ResultHandler() {
                    @Override
                    public void handle(MvcResult result) throws Exception {
                        log.info("TEST: requestNewPassword()\n" +
                                result.getResponse().getStatus() +
                                "\n" +
                                result.getResponse().getContentAsString());
                    }
                })
                .andExpect(status().isOk());

        // Check that no email has been sent
        List<MimeMessage> emails = ((MockMailSender)mailSender).getMessages();
        Assert.assertEquals(0, emails.size());

        //greenMail.stop();
    }

    @Test
    public void resetPassword() throws Exception {
        // Insert a password reset request in the database
        User user = userRepository.findByUsername("palga@dntp.thehyve.nl");
        NewPasswordRequest npr = new NewPasswordRequest(user);
        passwordRequestRepository.saveAndFlush(npr);

        // Call password reset with the token
        NewPasswordRepresentation npRepr = new NewPasswordRepresentation("12345678", npr.getToken());
        mockMvc.perform(MockMvcRequestBuilders.post("/password/reset")
                .content(new ObjectMapper().writeValueAsString(npRepr))
                .contentType("application/json")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(new ResultHandler() {
                    @Override
                    public void handle(MvcResult result) throws Exception {
                        log.info("TEST: setPassword()\n" +
                                result.getResponse().getStatus() +
                                "\n" +
                                result.getResponse().getContentAsString());
                    }
                })
                .andExpect(status().isOk());

        // Check that the password has been changed
        user = userRepository.findOne(user.getId());
        Assert.assertTrue(passwordEncoder.matches("12345678", user.getPassword()));

        // Check that the reset link doesn't exist anymore
        Assert.assertNull(passwordRequestRepository.findByToken(npr.getToken()));
    }
}
