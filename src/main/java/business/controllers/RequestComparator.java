package business.controllers;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.history.HistoricProcessInstance;
import org.springframework.stereotype.Component;

@Component
public class RequestComparator implements Comparator<HistoricProcessInstance> {

    public static List<String> statuses;
    public static Map<String, Integer> statusOrder;
    {
        statuses = new ArrayList<String>();
        statuses.add("Open");
        statuses.add("Review");
        statuses.add("Approval");
        statuses.add("DataDelivery");
        statuses.add("LabRequest");
        statuses.add("Rejected");
        statuses.add("Closed");
        statusOrder = new HashMap<String, Integer>();
        int i = 1;
        for (String status: statuses) {
            statusOrder.put(status, i);
            i++;
        }
    }
    
    
    @Override
    public int compare(HistoricProcessInstance arg0, HistoricProcessInstance arg1) {
        if (arg0.getId().equals(arg1.getId())) {
            return 0;
        }
        Integer s0 = statusOrder.get(arg0.getProcessVariables().get("status"));
        Integer s1 = statusOrder.get(arg1.getProcessVariables().get("status"));
        if (s0 == null || s1 == null) {
            throw new IllegalArgumentException("Status null");
        }
        if (s0 < s1) { return -1; }
        else if (s1 < s0) { return 1; }
        else {
            Long id0 = Long.valueOf(arg0.getId());
            Long id1 = Long.valueOf(arg1.getId());
            if (id0 > id1) { return -1; }
            else if (id1 > id0) { return 1; }
        }
        return 0;
    }

    
}
