package business;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.commons.logging.LogFactory;
import org.junit.Before;
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

import business.models.Lab;
import business.models.LabRepository;
import business.models.Role;
import business.models.User;
import business.models.UserRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {Application.class})
@WebIntegrationTest("server.port = 8093")
public class ProcessControllerTests {

    @Autowired UserRepository userRepository;

    @Autowired LabRepository labRepository;

    @Autowired
    private EmbeddedWebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Before
    public void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }
    
    private String processes_expected_template = 
            "[{\"id\":\"5001\",\"activityId\":\"theTask\",\"deploymentId\":\"2501\","
            + "\"name\":null,\"parentId\":null,\"businessKey\":null,"
            + "\"processDefinitionId\":\"example:2:2504\",\"processInstanceId\":\"5001\","
            + "\"tenantId\":\"\",\"processVariables\":"
            + "{\"assignee\":\"user\"},"
            + "\"events\":[],\"identityLinks\":"
            + "[{\"groupId\":null,\"processInstanceId\":\"5001\",\"taskId\":null,"
            + "\"type\":\"participant\",\"userId\":\"user\"}],"
            + "\"tasks\":"
            + "[{\"id\":\"5005\",\"name\":\"my task\",\"deleteReason\":\"\",\"description\":null,"
            + "\"assignee\":\"user\",\"createTime\":1426759545561,\"endTime\":null,"
            + "\"dueDate\":null,\"owner\":null,\"priority\":50,"
            + "\"processInstanceId\":\"5001\",\"parentTaskId\":null,\"formData\":null,"
            + "\"attachments\":null}],"
            + "\"suspended\":false,\"ended\":false,"
            + "\"diagramData\":null,\"diagramAvailable\":false,\"diagramError\":null}]";
    
    @Test
    public void getProcesses() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/processes")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(new ResultHandler(){
            @Override
            public void handle(MvcResult result) throws Exception {
                LogFactory.getLog(getClass()).info("TEST: getProcesses()\n" +
                        result.getResponse().getContentAsString());
            }
                })
                .andExpect(status().isOk())
                .andExpect(content().string(is(processes_expected_template)))
                ;
    }
    
    private String users_test_expected_template = 
        "{\"id\":%d,\"username\":\"palga@dntp.thehyve.nl\",\"password\":\"palga\",\"active\":true,"
        + "\"deleted\":false,\"lab\":null,\"institution\":null,"
        + "\"contactData\":{\"id\":%d,\"telephone\":null,\"email\":\"palga@dntp.thehyve.nl\","
        + "\"address1\":null,\"address2\":null,\"postalCode\":null,\"city\":null,"
        + "\"stateProvince\":null,\"country\":\"NL\"},\"roles\":[{\"id\":%d,\"name\":\"palga\"}]}";
    
    @Test
    public void getUser() throws Exception {
        User user = userRepository.findByUsernameAndActiveTrueAndDeletedFalse("palga@dntp.thehyve.nl");
        Role role = null;
        for(Role r: user.getRoles()) {
            role = r;
        }
        String users_test_expected = String.format(users_test_expected_template, 
                user.getId(), 
                user.getContactData()==null ? null : user.getContactData().getId(),
                role.getId());
        mockMvc.perform(MockMvcRequestBuilders.get("/admin/user?username={username}", "palga@dntp.thehyve.nl")
            .accept(MediaType.APPLICATION_JSON))
                .andDo(new ResultHandler(){
            @Override
            public void handle(MvcResult result) throws Exception {
                LogFactory.getLog(getClass()).info("TEST: getUser()\n" +
                        result.getResponse().getStatus() +
                        "\n" +
                        result.getResponse().getContentAsString());
            }
                })
            .andExpect(status().isOk())
            .andExpect(content().json(users_test_expected));
    }
    
    private String json_put_test_template =
            "{\"id\":%d,\"username\":\"scientific_council@dntp.thehyve.nl\",\"password\":\"\",\"active\":true,"
            + "\"deleted\":false}";
    // tests.users.json_put_test2 = {"id":8,"username":"scientific_council@dntp.thehyve.nl","password":"","active":true,"deleted":false,"lab":null,"institution":null,"contactData":{"id":9,"telephone":null,"email":"scientific_council@dntp.thehyve.nl","address1":null,"address2":null,"postalCode":null,"city":null,"stateProvince":null,"country":"NL"},"roles":[{"id":7,"name":"scientific_council"}]}
    private String json_put_test_expected_template = 
            "{\"id\":%d,\"username\":\"scientific_council@dntp.thehyve.nl\"," 
            + "\"password\":\"\",\"active\":true,\"deleted\":false,\"lab\":null,\"institution\":null,"
            +"\"contactData\":{\"id\":9,\"telephone\":null,\"email\":\"scientific_council@dntp.thehyve.nl\","
            + "\"address1\":null,\"address2\":null,\"postalCode\":null,\"city\":null,"
            + "\"stateProvince\":null,\"country\":\"NL\"}}";
    
    
    @Test
    public void serialiseUser() throws Exception {
        User user = userRepository.findByUsernameAndActiveTrueAndDeletedFalse("scientific_council@dntp.thehyve.nl");
                
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
                LogFactory.getLog(getClass()).info("TEST: serialiseUser()\n" +
                        result.getResponse().getStatus() +
                        "\n" +
                        (result.getResolvedException()==null ? "" : result.getResolvedException().getMessage()+"\n") +
                        result.getResponse().getContentAsString() +
                        "\nRequest:" +
                        reader.readLine()
                        );
            }
                })
            .andExpect(status().isOk())
            .andExpect(content().json(expected));
    }
    
    private String lab_put_template = 
        "{\"id\":%d,\"number\":%d,\"name\":\"Nijmegen\",\"contactData\":{\"address1\":\"Onderzoeksstraat 12\"}}";
    
    @Test
    public void putLab() throws Exception {

        Lab lab = new Lab();
        lab.setNumber(3);
        lab.setName("Nijmegen");
        lab = labRepository.save(lab);
                
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
                LogFactory.getLog(getClass()).info("TEST: putLab()\n" +
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
