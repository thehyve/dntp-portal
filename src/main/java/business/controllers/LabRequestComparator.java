/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business.controllers;

import java.util.Comparator;

import org.springframework.stereotype.Component;

import business.representation.LabRequestRepresentation;

@Component
public class LabRequestComparator implements Comparator<LabRequestRepresentation> {

    public int compareLabRequestNumbers(String number0, String number1) {
        int year0 = -1;
        int year1 = -1;
        long n0 = -1L;
        long n1 = -1L;
        long lab0 = -1L;
        long lab1 = -1L;
        String[] parts0 = number0.split("-");
        String[] parts1 = number1.split("-");
        if (parts0.length < parts1.length) {
            return -1;
        }
        if (parts1.length > parts0.length) {
            return 1;
        }
        if (parts0.length != 3) {
            return number0.compareTo(number1);
        }
        year0 = Integer.parseInt(parts0[0]);
        n0 = Long.parseLong(parts0[1]);
        lab0 = Long.parseLong(parts0[2]);
        year1 = Integer.parseInt(parts1[0]);
        n1 = Long.parseLong(parts1[1]);
        lab1 = Long.parseLong(parts1[2]);
        if (year0 < year1) {
            return -1;
        } else if (year1 < year0) {
            return 1;
        }
        if (n0 < n1) {
            return -1;
        } else if (n1 < n0) {
            return 1;
        }
        if (lab0 < lab1) {
            return -1;
        } else if (lab1 < lab0) {
            return 1;
        }
        return 0;
    }

    @Override
    public int compare(LabRequestRepresentation arg0, LabRequestRepresentation arg1) {
        if (arg0 == arg1) {
            return 0;
        }
        if (arg0 == null) {
            return -1;
        }
        if (arg1 == null) {
            return 1;
        }
        if (arg0.getLabRequestCode() == arg1.getLabRequestCode()) {
            return 0;
        }
        if (arg0.getLabRequestCode() == null) {
            return -1;
        }
        if (arg1.getLabRequestCode() == null) {
            return 1;
        }
        if (arg0.getLabRequestCode().equals(arg1.getLabRequestCode())) {
            return 0;
        }
        return compareLabRequestNumbers(arg0.getLabRequestCode(), arg1.getLabRequestCode());
    }

}
