package business.services;

import java.util.Calendar;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import business.models.RequestNumber;
import business.models.RequestNumberRepository;

@Service
public class RequestNumberService {

    @Autowired
    RequestNumberRepository requestNumberRepository;

    @Transactional
    String getNewRequestNumber() {
        int year = Calendar.getInstance().get(Calendar.YEAR);
        RequestNumber number = requestNumberRepository.findByYear(year);
        if (number == null) {
            number = new RequestNumber(year);
            number = requestNumberRepository.save(number);
        }
        number.setSerialNumber(number.getSerialNumber() + 1);
        number = requestNumberRepository.save(number);
        return number.toString();
    }

}
