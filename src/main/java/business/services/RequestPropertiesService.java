package business.services;

import java.util.Date;
import java.util.Set;

import javax.transaction.Transactional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import business.models.RequestProperties;
import business.models.RequestProperties.ReviewStatus;
import business.models.RequestPropertiesRepository;

@Service
@Transactional
public class RequestPropertiesService {

    Log log = LogFactory.getLog(getClass());
    
    @Autowired
    RequestPropertiesRepository requestPropertiesRepository;
    
    public RequestProperties save(RequestProperties properties) {
        return requestPropertiesRepository.save(properties);
    }
    
    public RequestProperties findByProcessInstanceId(String processInstanceId) {
        Date start = new Date();
        RequestProperties properties = requestPropertiesRepository.findByProcessInstanceId(processInstanceId);
        if (properties == null) {
            properties = new RequestProperties(processInstanceId);
        }
        Date end = new Date();
        if ((end.getTime()-start.getTime()) > 10) {
            log.warn("RequestPropertiesService: query took " + (end.getTime() - start.getTime()) + " ms.");
        }
        return properties;
    }

    public ReviewStatus getRequestReviewStatus(String id) {
        return requestPropertiesRepository.getRequestReviewStatusByProcessInstanceId(id);
    }

    public Set<String> getProcessInstanceIdsByReviewStatus(ReviewStatus reviewStatus) {
        return requestPropertiesRepository.getProcessInstanceIdsByReviewStatus(reviewStatus);
    }

}
