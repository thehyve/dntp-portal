/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business;

import static org.junit.Assert.assertNotEquals;

import java.util.Calendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import business.models.RequestNumber;
import business.models.RequestNumberRepository;
import business.services.RequestNumberService;

@Profile("dev")
@SpringApplicationConfiguration(classes = {Application.class})
@WebIntegrationTest("server.port = 8093")
public class RequestNumberServiceTests extends AbstractTestNGSpringContextTests {

    Log log = LogFactory.getLog(this.getClass());

    @Autowired RequestNumberService requestNumberService;

    @Autowired RequestNumberRepository requestNumberRepository;

    @BeforeClass
    public void setUp() throws Exception {
    }

    @Test
    public void incrementRequestNumber() {
        int year = Calendar.getInstance().get(Calendar.YEAR);
        RequestNumber n1 = requestNumberRepository.findByYear(year);
        if (n1 == null) {
            n1 = new RequestNumber(year);
            n1 = requestNumberRepository.save(n1);
        }
        String n = requestNumberService.getNewRequestNumber();
        assertNotEquals("Sequence number should be incremented.", n1.toString(), n);
        RequestNumber n2 = requestNumberRepository.findByYear(year);
        assertNotEquals("Sequence number should be incremented.", n1, n2);
    }

    @Test
    public void subsequentRequestNumbers() {
        String n1 = requestNumberService.getNewRequestNumber();
        String n2 = requestNumberService.getNewRequestNumber();
        assertNotEquals("Subsequent request numbers should be different.", n1, n2);
    }

}
