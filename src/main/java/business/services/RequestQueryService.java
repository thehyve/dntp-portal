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

    public List<String> getRequestsForRequesterByStatus(User requester, RequestStatus status) {
        Date start = new Date();

        Map<String, Object> params = new HashMap<>();
        params.put("status", status.toString());
        params.put("user_id", requester.getId().toString());

        List<String> requestsIds = template.queryForList(
                "select p.id_" +
                        " from act_hi_procinst p" +
                        " inner join act_hi_varinst v on p.id_ = v.execution_id_ " +
                        " inner join act_hi_identitylink id on p.id_ = id.proc_inst_id_ " +
                        " where v.name_ = 'status' and v.text_ = :status" +
                        " and id.type_ = 'starter' and id.user_id_ = :user_id",
                params,
                String.class
        );

        Date end = new Date();

        log.info("Query took {} ms.", (end.getTime() - start.getTime()));

        return requestsIds;
    }

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
                        " where v1.name_ = 'status' and v1.text_ = :status" +
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
                        " where v1.name_ = 'status' and not v1.text_ = :status",
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
