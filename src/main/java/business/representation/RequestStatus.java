package business.representation;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum RequestStatus {

    OPEN ("Open"),
    REVIEW ("Review"),
    APPROVAL ("Approval"),
    DATA_DELIVERY ("DataDelivery"),
    SELECTION_REVIEW ("SelectionReview"),
    LAB_REQUEST ("LabRequest"),
    REJECTED ("Rejected"),
    CLOSED ("Closed"),
    NONE ("None");

    static Log log = LogFactory.getLog(RequestStatus.class);

    private final String description;

    RequestStatus(final String description) {
        this.description = description;
    }

    @JsonValue
    public final String toString() {
        return description;
    }

    private static final Map<String, RequestStatus> mapping = new HashMap<>();
    static {
        for (RequestStatus status: RequestStatus.values()) {
            mapping.put(status.toString(), status);
        }
    }

    @JsonCreator
    public static RequestStatus forDescription(String description) {
        if (mapping.containsKey(description)) {
            return mapping.get(description);
        } else {
            log.error("Unknown request status: " + description);
            return NONE;
        }
    }

}
