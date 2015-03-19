package business;

import static org.hamcrest.Matchers.is;

import business.controllers.ProcessController;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = MockServletContext.class)
@WebAppConfiguration
//@org.springframework.boot.test.WebIntegrationTest
// FIXME: autowired fields in controllers are not properly initialised/injected.
public class ProcessControllerTests {

    private MockMvc mvc;

    @Before
    public void setUp() throws Exception {
        mvc = MockMvcBuilders.standaloneSetup(new ProcessController()).build();
    }

    @Test
    public void getProcesses() throws Exception {
        /*
        mvc.perform(MockMvcRequestBuilders.get("/processes").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(is("Greetings from Spring Boot!\n")));
                */
    }
    
}
