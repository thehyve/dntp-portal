package business.services;

import business.models.User;
import business.representation.RequestStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional(readOnly = true)
public class RequestQueryService {

    private final Logger log = LoggerFactory.getLogger(RequestQueryService.class);

    @Autowired
    NamedParameterJdbcTemplate template;

    /**
     * Fetches all process instance ids of requests of which the user is the requester (creator)
     * or for which the user is the associated principal investigator or pathologist.
     * Optionally, a request status can be specified to filter on.
     *
     * For the requester, the requests are fetch through an identity link: such link is established when creating or
     * processing a request. Because users only have a single role in the system and for requesters such links
     * are only established at request creation, this effectively fetches all requests for which the user is the requester.
     *
     * For principal investigators and pathologists, their email address is matches with the 'pathologist_email' and
     * 'contact_person_email' variables of the process.
     *
     * @param user The user with role requester to fetch the requests for.
     * @param status The request status to filter on. If null, the requests are not filtered on status.
     * @return the list of process instance ids.
     */
    public List<String> getRequestsForRequesterByStatus(User user, RequestStatus status) {
        Date start = new Date();

        Map<String, Object> params = new HashMap<>();
        if (status != null) {
            params.put("status", status.toString());
        }
        params.put("user_id", user.getId().toString());
        params.put("user_email", user.getUsername());

        List<String> requestsIds = template.queryForList(
                "(select distinct p.id_" +
                        " from act_hi_procinst p" +
                        (status == null ? "" : " inner join act_hi_varinst status_var on p.id_ = status_var.execution_id_ ") +
                        " inner join act_hi_identitylink id on p.id_ = id.proc_inst_id_ " +
                        " where p.delete_reason_ is null" +
                        " and id.user_id_ = :user_id" +
                        (status == null ? "" : " and status_var.name_ = 'status' and status_var.text_ = :status") +
                        ")" +
                    " union (select distinct p.id_" +
                        " from act_hi_procinst p" +
                        (status == null ? "" : " inner join act_hi_varinst status_var on p.id_ = status_var.execution_id_ ") +
                        " inner join act_hi_varinst email_var on p.id_ = email_var.execution_id_ " +
                        " where p.delete_reason_ is null" +
                        " and (email_var.name_ = 'pathologist_email' or email_var.name_ = 'contact_person_email')" +
                        " and email_var.text_ = :user_email" +
                        (status == null ? "" : " and status_var.name_ = 'status' and status_var.text_ = :status") +
                        ")",
                params,
                String.class
        );

        Date end = new Date();

        log.info("Query took {} ms.", (end.getTime() - start.getTime()));

        return requestsIds;
    }

    /**
     * Fetches all process instance ids of requests for a Palga user.
     * This included all requests that are already passed the status 'Open' and reopened requests
     * that are in status 'Open'.
     *
     * @return the list of process instance ids.
     */
    public List<String> getPalgaRequests() {
        List<String> result = new ArrayList<>();
        Date start = new Date();

        Map<String, Object> params = new HashMap<>();
        params.put("status", RequestStatus.OPEN.toString());

        List<String> reopenedRequests = template.queryForList(
                "select p.id_" +
                        " from act_hi_procinst p" +
                        " inner join act_hi_varinst v1 on p.id_ = v1.execution_id_ " +
                        " inner join act_hi_varinst v2 on p.id_ = v2.execution_id_ " +
                        " where p.delete_reason_ is null" +
                        " and v1.name_ = 'status' and v1.text_ = :status" +
                        " and v2.name_ = 'reopen_request' and v2.long_ = 1",
                params,
                String.class
        );
        result.addAll(reopenedRequests);

        Date t1 = new Date();

        List<String> submittedRequests = template.queryForList(
                "select p.id_" +
                        " from act_hi_procinst p" +
                        " inner join act_hi_varinst v1 on p.id_ = v1.execution_id_ " +
                        " where p.delete_reason_ is null" +
                        " and v1.name_ = 'status' and not v1.text_ = :status",
                params,
                String.class
        );
        result.addAll(submittedRequests);

        Date t2 = new Date();

        log.info("Query took {} ms. ({} + {})",
                (t2.getTime() - start.getTime()),
                (t1.getTime() - start.getTime()),
                (t2.getTime() - t1.getTime()));

        return result;
    }

}
