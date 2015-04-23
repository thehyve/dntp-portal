package business.models;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import business.controllers.UserController.EmailAddressNotAvailableException;

@Service
@Transactional
public class RequestPropertiesService {

    @Autowired
    RequestPropertiesRepository requestPropertiesRepository;
    
    public RequestProperties save(RequestProperties properties) {
        return requestPropertiesRepository.save(properties);
    }
    
    public RequestProperties findByProcessInstanceId(String processInstanceId) {
        return requestPropertiesRepository.findByProcessInstanceId(processInstanceId);
    }
    
}
