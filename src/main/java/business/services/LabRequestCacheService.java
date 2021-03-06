package business.services;

import business.models.LabRequest;
import business.representation.LabRequestRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class LabRequestCacheService {

    private Logger log = LoggerFactory.getLogger(LabRequestCacheService.class);

    @Autowired
    private LabRequestQueryService labRequestQueryService;

    @Cacheable(value = "labrequestdata", key = "#labRequest.id")
    public LabRequestRepresentation getLabRequestCached(LabRequest labRequest) {
        log.debug("Fetch data for lab request {}", labRequest.getId());
        LabRequestRepresentation representation = new LabRequestRepresentation(labRequest);
        labRequestQueryService.transferLabRequestData(representation, true);
        return representation;
    }

    @Cacheable(value = "detailedlabrequestdata", key = "#labRequest.id")
    public LabRequestRepresentation getDetailedLabRequestCached(LabRequest labRequest) {
        log.debug("Fetch detailed data for lab request {}", labRequest.getId());
        LabRequestRepresentation representation = new LabRequestRepresentation(labRequest);
        labRequestQueryService.transferLabRequestData(representation, true);
        labRequestQueryService.transferLabRequestDetails(representation, labRequest, true);
        return representation;
    }

}
