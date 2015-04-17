package business.controllers;

import business.Application;
import business.mocks.MockAuthenticationToken;
import business.models.UserRepository;
import business.util.DefaultUsers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.EmbeddedWebApplicationContext;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {Application.class})
@WebIntegrationTest("server.port = 8093")
public class PasswordChangeTests {
    @Autowired
    private EmbeddedWebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordController passController;

    @Autowired
    private business.security.DefaultUsers defaultUsers;

    private MockMvc mockMvc;
    private MockAuthenticationToken authToken;

    @Before
    public void setUp() throws Exception {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .build();

        this.authToken = new MockAuthenticationToken(userRepository.findByUsername(DefaultUsers.Admin.getEmail()));
    }

    @Test
    public void dummyTest() {}

    /*
    @Test
    public void changePasswordWorks() throws Exception {
        // Send password change request
        PasswordChangeRepresentation passwordChange = new PasswordChangeRepresentation(DefaultUsers.Admin.getPassword(), "12345678");
        passController.changePassword(this.authToken, passwordChange);

        // Check that the password has been changed
        User user = this.userRepository.findByUsername(DefaultUsers.Admin.getEmail());
        Assert.assertEquals(user.getPassword(), "12345678");

        // Restore original password
        user.setPassword(DefaultUsers.Admin.getPassword());
        userRepository.save(user);
    }

    @Test
    public void changePasswordRejectsInvalidPassword() throws Exception {
        // Send password change request
        // 1234 is an invalid password since it has less than 8 characters
        PasswordChangeRepresentation passwordChange = new PasswordChangeRepresentation(DefaultUsers.Admin.getPassword(), "1234");
        passController.changePassword(this.authToken, passwordChange);

        // Check that the password hasn't been changed
        User user = this.userRepository.findByUsername(DefaultUsers.Admin.getEmail());
        Assert.assertEquals(user.getPassword(), DefaultUsers.Admin.getPassword());
    }

    @Test
    public void changePasswordRejectsWrongOldPassword() throws Exception {
        // Send password change request
        PasswordChangeRepresentation passwordChange = new PasswordChangeRepresentation("wrong_password", "12345678");
        passController.changePassword(this.authToken, passwordChange);

        // Check that the password hasn't been changed
        User user = this.userRepository.findByUsername(DefaultUsers.Admin.getEmail());
        Assert.assertEquals(user.getPassword(), DefaultUsers.Admin.getPassword());
    }*/
}
