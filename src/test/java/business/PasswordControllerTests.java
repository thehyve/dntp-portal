package business;

import business.models.NewPasswordRequest;
import business.models.NewPasswordRequestRepository;
import business.representation.EmailRepresentation;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.EmbeddedWebApplicationContext;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultHandler;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.mail.internet.MimeMessage;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {Application.class})
@WebIntegrationTest("server.port = 8093")
public class PasswordControllerTests {
    @Autowired
    private EmbeddedWebApplicationContext webApplicationContext;

    @Autowired
    private NewPasswordRequestRepository passwordRequestRepository;

    private MockMvc mockMvc;
    public GreenMail greenMail;

    @Before
    public void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        this.greenMail = new GreenMail(ServerSetup.ALL);
    }

    @Test
    public void requestNewPasswordSendsCorrectEmail() throws Exception {
        EmailRepresentation emailForm = new EmailRepresentation("palga@dntp.thehyve.nl");
        greenMail.start();

        // Perform the request and ensure we get a correct result status
        mockMvc.perform(MockMvcRequestBuilders.put("/password/request-new")
                .content(new ObjectMapper().writeValueAsString(emailForm))
                .contentType("application/json")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(new ResultHandler() {
                    @Override
                    public void handle(MvcResult result) throws Exception {
                        LogFactory.getLog(getClass()).info("TEST: requestNewPassword()\n" +
                                result.getResponse().getStatus() +
                                "\n" +
                                result.getResponse().getContentAsString());
                    }
                })
                .andExpect(status().isOk());

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
        mockMvc.perform(MockMvcRequestBuilders.put("/password/request-new")
                .content(new ObjectMapper().writeValueAsString(emailForm))
                .contentType("application/json")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(new ResultHandler() {
                    @Override
                    public void handle(MvcResult result) throws Exception {
                        LogFactory.getLog(getClass()).info("TEST: requestNewPassword()\n" +
                                result.getResponse().getStatus() +
                                "\n" +
                                result.getResponse().getContentAsString());
                    }
                })
                .andExpect(status().isOk());

        // Check that no email has been sent
        MimeMessage[] emails = greenMail.getReceivedMessages();
        Assert.assertEquals(0, emails.length);

        greenMail.stop();
    }
}
