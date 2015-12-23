package business.services;

import java.util.Calendar;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.activiti.engine.history.HistoricTaskInstance;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import business.models.RequestNumber;
import business.models.RequestNumberRepository;
import business.models.RequestProperties;
import business.models.RequestPropertiesRepository;

@Service
@Transactional
public class RequestNumberService {

    Log log = LogFactory.getLog(getClass());

    private Object lock = new Object();

    @PersistenceContext
    private EntityManager em;

    @Autowired
    RequestNumberRepository requestNumberRepository;

    @Autowired
    RequestPropertiesRepository requestPropertiesRepository;

    @Autowired
    RequestService requestService;

    /**
     * Fix existing requests that should have a request number, but do not have
     * one yet, by assigning a new request number.
     */
    public void fixRequestNumbers() {
        log.info("Checking if there are requests without a request number that should have one...");
        List<RequestProperties> requests = requestPropertiesRepository.findByRequestNumberNull();
        for(RequestProperties request: requests) {
            HistoricTaskInstance task = requestService.findHistoricTaskByRequestId(request.getProcessInstanceId(), "palga_request_review");
            if (task != null) {
                // Request has been submitted, but has no request number yet;
                // assign a number now.
                String requestNumber = getNewRequestNumber();
                log.info("Assign number '" + requestNumber + "' to request '" + request.getProcessInstanceId() + "'."); 
                request.setRequestNumber(requestNumber);
                requestPropertiesRepository.save(request);
            }
        }
    }

    /**
     * Generate a new request number with format <code>YYYY-N</code>, where <var>YYYY</var> is the current
     * year and <var>N</var> is a sequence number, starting at 1 for every year.
     * @return the new request number.
     */
    public String getNewRequestNumber() {
        int year = Calendar.getInstance().get(Calendar.YEAR);
        RequestNumber number;
        synchronized (lock) {
            number = requestNumberRepository.findByYear(year);
            if (number == null) {
                number = new RequestNumber(year);
                number = requestNumberRepository.save(number);
            }
        }
        em.refresh(number, LockModeType.PESSIMISTIC_WRITE);
        number.setSerialNumber(number.getSerialNumber() + 1);
        em.persist(number);
        em.flush();
        log.info("Handing out new request number: " + number.toString());
        return number.toString();
    }

}
