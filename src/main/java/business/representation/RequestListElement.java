package business.representation;

import java.util.Date;

public interface RequestListElement {

    String getProcessInstanceId();

    String getRequestNumber();

    Date getDateCreated();

    Date getDateSubmitted();

}
