package business.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultHandler;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class Util {
    public static void performHttpRequest(MockMvc mockMvc, HttpMethod method, final String route, Object content, ResultMatcher resultMatcher)
            throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.request(method, route)
                .content(new ObjectMapper().writeValueAsString(content))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                //.with(user("palga@dntp.thehyve.nl")))
                .andDo(new ResultHandler() {
                    @Override
                    public void handle(MvcResult result) throws Exception {
                        LogFactory.getLog(getClass()).info(
                                String.format("TEST: %s\n%s\n%s",
                                        route,
                                        result.getResponse().getStatus(),
                                        result.getResponse().getContentAsString()));
                    }
                })
                .andExpect(resultMatcher);
    }

    public static void performHttpRequest(MockMvc mockMvc, HttpMethod method, final String route, Object content)
            throws Exception {
        performHttpRequest(mockMvc, method, route, content, status().isOk());
    }

    public static void resetDb()
    {

    }
}
