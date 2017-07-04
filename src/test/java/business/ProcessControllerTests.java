/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import javax.servlet.Filter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultHandler;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import business.models.Lab;
import business.models.Role;
import business.models.User;
import business.models.UserRepository;
import business.security.UserAuthenticationToken;
import business.services.LabService;
import business.services.PasswordService;
import business.services.UserService;
import org.springframework.web.context.WebApplicationContext;

@Profile("dev")
@ContextConfiguration
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@WebAppConfiguration
public class ProcessControllerTests {

    Log log = LogFactory.getLog(this.getClass());

    UserRepository userRepository = mock(UserRepository.class);

    PasswordService passwordService = mock(PasswordService.class);

    UserService userService = mock(UserService.class);

    LabService labService = mock(LabService.class);

    WebApplicationContext webApplicationContext = mock(WebApplicationContext.class);
    AuthenticationProvider authenticationProvider = mock(AuthenticationProvider.class);


    private Filter springSecurityFilterChain = mock(Filter.class);

    private MockMvc mockMvc;

    private UserAuthenticationToken palga;

    private SecurityContext securityContext;

    protected UserAuthenticationToken getPalga() {
        User user = userService.findByUsername("test+palga@dntp.thehyve.nl");
        user.setPassword(passwordService.getEncoder().encode("palga")); // because of password tests
        userService.save(user);

        Authentication authentication = new UsernamePasswordAuthenticationToken(user, "palga");
        return (UserAuthenticationToken)authenticationProvider.authenticate(authentication);
    }

    @Before
    public void setUp() throws Exception {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .addFilters(springSecurityFilterChain)
                .build();
        palga = getPalga();
        securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(palga);
    }

    @After
    public void shutDown() {
        SecurityContextHolder.clearContext();
    }

    private String users_test_expected_template =
        "{\"id\":%d,\"username\":\"test+palga@dntp.thehyve.nl\",\"password\":\"palga\",\"active\":true,"
        + "\"deleted\":false,\"lab\":null,\"institute\":null,"
        + "\"contactData\":{\"id\":%d,\"telephone\":null,\"email\":\"test+palga@dntp.thehyve.nl\","
        + "\"address1\":null,\"address2\":null,\"postalCode\":null,\"city\":null,"
        + "\"stateProvince\":null,\"country\":\"NL\"},\"roles\":[{\"id\":%d,\"name\":\"palga\"}]}";

    @Test
    public void getUser() throws Exception {
        User user = userRepository.findByUsernameAndActiveTrueAndEmailValidatedTrueAndDeletedFalse("test+palga@dntp.thehyve.nl");
        Role role = null;
        for(Role r: user.getRoles()) {
            role = r;
        }
        String users_test_expected = String.format(users_test_expected_template, 
                user.getId(), 
                user.getContactData()==null ? null : user.getContactData().getId(),
                role.getId());
        mockMvc.perform(MockMvcRequestBuilders.get("/admin/user?username={username}", "test+palga@dntp.thehyve.nl")
            .accept(MediaType.APPLICATION_JSON))
                .andDo(new ResultHandler(){
            @Override
            public void handle(MvcResult result) throws Exception {
                log.debug("TEST: getUser()\n" +
                        result.getResponse().getStatus() +
                        "\n" +
                        result.getResponse().getContentAsString());
            }
                })
            .andExpect(status().isOk());
            //.andExpect(content().json(users_test_expected));
    }

    private String json_put_test_template =
            "{\"id\":%d,\"currentRole\":\"scientific_council\",\"username\":\"test+scientific_council@dntp.thehyve.nl\",\"password\":\"\",\"active\":true,"
            + "\"deleted\":false,\"contactData\":{\"email\":\"test+scientific_council@dntp.thehyve.nl\"}}";
    // tests.users.json_put_test2 = {"id":8,"username":"scientific_council@dntp.thehyve.nl","password":"","active":true,"deleted":false,"lab":null,"institute":null,"contactData":{"id":9,"telephone":null,"email":"scientific_council@dntp.thehyve.nl","address1":null,"address2":null,"postalCode":null,"city":null,"stateProvince":null,"country":"NL"},"roles":[{"id":7,"name":"scientific_council"}]}
    private String json_put_test_expected_template =
            "{\"id\":%d,\"username\":\"test+scientific_council@dntp.thehyve.nl\","
            + "\"password\":\"\",\"active\":true,\"deleted\":false,\"lab\":null,\"institute\":null,"
            +"\"contactData\":{\"id\":9,\"telephone\":null,\"email\":\"test+scientific_council@dntp.thehyve.nl\","
            + "\"address1\":null,\"address2\":null,\"postalCode\":null,\"city\":null,"
            + "\"stateProvince\":null,\"country\":\"NL\"}}";

    @Test
    public void serialiseUser() throws Exception {
        User user = userRepository.findByUsernameAndActiveTrueAndEmailValidatedTrueAndDeletedFalse("test+scientific_council@dntp.thehyve.nl");

        String test_string = String.format(json_put_test_template, user.getId());
        String expected = String.format(json_put_test_expected_template, user.getId());
        mockMvc.perform(MockMvcRequestBuilders.put("/admin/users/{id}", user.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(test_string)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(new ResultHandler(){
            @Override
            public void handle(MvcResult result) throws Exception {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(result.getRequest().getInputStream()));
                log.debug("TEST: serialiseUser()\n" +
                        result.getResponse().getStatus() +
                        "\n" +
                        (result.getResolvedException()==null ? "" : result.getResolvedException().getMessage()+"\n") +
                        result.getResponse().getContentAsString() +
                        "\nRequest:" +
                        reader.readLine()
                        );
            }
                })
                .andExpect(status().isOk());
                //.andExpect(content().json(expected));
    }

    private String lab_put_template =
        "{\"id\":%d,\"number\":%d,\"name\":\"Nijmegen\",\"contactData\":{\"address1\":\"Onderzoeksstraat 12\"}}";

    @Test
    public void putLab() throws Exception {

        Lab lab = new Lab();
        lab.setNumber(3);
        lab.setName("Nijmegen");
        lab = labService.save(lab);

        String test_string = String.format(lab_put_template, lab.getId(), lab.getNumber());
        mockMvc.perform(MockMvcRequestBuilders.put("/admin/labs/{id}", lab.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(test_string)
            .accept(MediaType.APPLICATION_JSON))
                .andDo(new ResultHandler(){
            @Override
            public void handle(MvcResult result) throws Exception {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(result.getRequest().getInputStream()));
                log.debug("TEST: putLab()\n" +
                        result.getResponse().getStatus() +
                        "\n" +
                        (result.getResolvedException()==null ? "" : result.getResolvedException().getMessage()+"\n") +
                        result.getResponse().getContentAsString() +
                        "\nRequest:" +
                        reader.readLine()
                        );
            }
                })
            .andExpect(status().isOk());
    }
}
