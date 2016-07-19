/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business.controllers;

import java.util.Comparator;

import org.springframework.stereotype.Component;

import business.representation.RequestListElement;

/**
 * Sort requests by submission date.
 */
@Component
public class RequestListRepresentationComparator implements Comparator<RequestListElement> {

    @Override
    public int compare(RequestListElement arg0, RequestListElement arg1) {
        if (arg0 == arg1) {
            return 0;
        }
        if (arg0 == null) {
            return -1;
        }
        if (arg1 == null) {
            return 1;
        }
        if (arg0.getProcessInstanceId().equals(arg1.getProcessInstanceId())) {
            return 0;
        }
        if (arg0.getDateSubmitted() == arg1.getDateSubmitted()) {
            return 0;
        }
        if (arg0.getDateSubmitted() == null) {
            return -1;
        }
        if (arg1.getDateSubmitted() == null) {
            return 1;
        }
        long submitted0 = arg0.getDateSubmitted().getTime();
        long submitted1 = arg1.getDateSubmitted().getTime();
        if (submitted0 < submitted1) {
            return -1;
        } else if (submitted1 < submitted0) {
            return 1;
        }
        return 0;
    }

}
