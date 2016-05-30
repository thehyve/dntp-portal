/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business.controllers;

import java.util.Comparator;

import org.springframework.stereotype.Component;

import business.representation.RequestListRepresentation;

@Component
public class RequestListRepresentationStartTimeComparator implements Comparator<RequestListRepresentation> {

    @Override
    public int compare(RequestListRepresentation arg0, RequestListRepresentation arg1) {
        if (arg0.getProcessInstanceId().equals(arg1.getProcessInstanceId())) {
            return 0;
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
