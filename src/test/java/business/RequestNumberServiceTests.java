/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business;

import static org.junit.Assert.assertNotEquals;

import java.util.Calendar;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import business.models.RequestNumber;
import business.models.RequestNumberRepository;
import business.services.RequestNumberService;

@Profile("dev")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@ContextConfiguration
public class RequestNumberServiceTests {

    private final Logger log = LoggerFactory.getLogger(RequestNumberServiceTests.class);

    @Autowired RequestNumberService requestNumberService;

    @Autowired RequestNumberRepository requestNumberRepository;

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
