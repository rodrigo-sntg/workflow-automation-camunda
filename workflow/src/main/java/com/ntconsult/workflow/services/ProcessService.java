package com.ntconsult.workflow.services;

import com.ntconsult.workflow.entities.TravelRequest;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.Execution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProcessService {

    private final RuntimeService runtimeService;

    @Autowired
    public ProcessService(RuntimeService runtimeService) {
        this.runtimeService = runtimeService;
    }

    public void startProcess(TravelRequest travelRequest) {
        runtimeService.startProcessInstanceByKey("Process_Travel_Approval",
                travelRequest.toMap());
    }

    public void cancelProcess(String processInstanceId) {
        List<Execution> executions = runtimeService.createExecutionQuery()
                .processInstanceId(processInstanceId)
                .signalEventSubscriptionName("cancelSignal")
                .list();

        for (Execution execution : executions) {
            runtimeService.signalEventReceived("cancelSignal", execution.getId());
        }
    }
}