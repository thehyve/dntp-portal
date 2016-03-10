/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business.controllers;

import java.util.Comparator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import business.representation.RequestListRepresentation;

@Component
public class RequestListRepresentationComparator implements Comparator<RequestListRepresentation> {

    public int compareRequestNumbers(String number0, String number1) {
        int year0 = -1;
        int year1 = -1;
        long n0 = -1L;
        long n1 = -1L;
        if (number0 != null && number0.length() > 5) {
            year0 = Integer.parseInt(number0.substring(0, 4));
            n0 = Long.parseLong(number0.substring(5, number0.length()));
        }
        if (number1 != null && number1.length() > 5) {
            year1 = Integer.parseInt(number1.substring(0, 4));
            n1 = Long.parseLong(number1.substring(5, number1.length()));
        }
        if (year0 < year1) {
            return -1;
        } else if (year1 < year0) {
            return 1;
        } else {
            if (n0 < n1) {
                return -1;
            } else if (n1 < n0) {
                return 1;
            }
        }
        return 0;
    }

    @Override
    public int compare(RequestListRepresentation arg0, RequestListRepresentation arg1) {
        if (arg0.getProcessInstanceId().equals(arg1.getProcessInstanceId())) {
            return 0;
        }
        int value = compareRequestNumbers(arg0.getRequestNumber(), arg1.getRequestNumber());
        if (value != 0) {
            return value;
        }
        long created0 = arg0.getDateCreated().getTime();
        long created1 = arg1.getDateCreated().getTime();
        if (created0 < created1) {
            return -1;
        } else if (created1 < created0) {
            return 1;
        }
        return 0;
    }

}
