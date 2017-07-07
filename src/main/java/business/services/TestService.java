/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class for clearing database for testing purposes.
 */
@Profile("dev")
@Service
@Transactional
public class TestService {

    private final Logger log = LoggerFactory.getLogger(TestService.class);

    @Autowired
    private RequestService requestService;

    @Autowired
    private RequestQueryService requestQueryService;

    /**
     * Delete all:
     * - requests.
     */
    public void clearDatabase() {
        // Delete all requests
        log.info("Deleting all requests ...");
        for (String id: requestQueryService.getPalgaRequests()) {
            requestService.delete("test", id);
        }
    }

}
