/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business.controllers;

import org.activiti.engine.HistoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.runtime.ProcessInstance;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import business.models.ApprovalVoteRepository;
import business.models.CommentRepository;
import business.models.ExcerptListRepository;
import business.models.LabRequestRepository;
import business.models.PathologyItemRepository;
import business.models.RequestNumberRepository;
import business.models.RequestPropertiesRepository;

@Profile("dev")
@RestController
public class TestController {

    Log log = LogFactory.getLog(getClass());

    @Autowired RequestNumberRepository requestNumberRepository;

    @Autowired ExcerptListRepository excerptListRepository;

    @Autowired RequestPropertiesRepository requestPropertiesRepository;

    @Autowired CommentRepository commentRepository;

    @Autowired ApprovalVoteRepository approvalVoteRepository;

    @Autowired PathologyItemRepository pathologyItemRepository;

    @Autowired LabRequestRepository labRequestRepository;

    @Autowired TaskService taskService;

    @Autowired RuntimeService runtimeService;

    @Autowired HistoryService historyService;

    @RequestMapping(value = "/test/clear", method = RequestMethod.GET)
    public void clearAll() {
        log.warn("GET /test/clear");
        //pathologyItemRepository.deleteAll();
        labRequestRepository.deleteAll();
        excerptListRepository.deleteAll();
        requestPropertiesRepository.deleteAll();
        commentRepository.deleteAll();
        approvalVoteRepository.deleteAll();
        requestNumberRepository.deleteAll();

        for (ProcessInstance instance: runtimeService.createProcessInstanceQuery().list()) {
            runtimeService.deleteProcessInstance(instance.getId(), null);
        }
        for (HistoricProcessInstance instance: historyService.createHistoricProcessInstanceQuery().list()) {
            historyService.deleteHistoricProcessInstance(instance.getId());
        }
    }

}
