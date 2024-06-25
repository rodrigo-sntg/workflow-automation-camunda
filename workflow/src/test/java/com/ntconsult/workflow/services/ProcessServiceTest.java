package com.ntconsult.workflow.services;

import com.ntconsult.workflow.entities.TravelRequest;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ExecutionQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Date;
import java.util.List;

import static org.mockito.Mockito.*;

class ProcessServiceTest {

    @Mock
    private RuntimeService runtimeService;

    @InjectMocks
    private ProcessService processService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void startProcess_ShouldStartProcessInstance() {
        TravelRequest travelRequest = new TravelRequest(
                "Vendas", "São Paulo", "Rio de Janeiro",
                new Date(), new Date(), 1000.0, 2000.0, "test@example.com");

        processService.startProcess(travelRequest);

        verify(runtimeService).startProcessInstanceByKey("Process_Travel_Approval", travelRequest.toMap());
    }

    @Test
    public void cancelProcess_ShouldSendCancelSignal() {
        Execution execution = mock(Execution.class);
        ExecutionQuery executionQuery = mock(ExecutionQuery.class);
        when(execution.getId()).thenReturn("1");
        when(runtimeService.createExecutionQuery()).thenReturn(executionQuery);
        when(executionQuery.processInstanceId("1")).thenReturn(executionQuery);
        when(executionQuery.signalEventSubscriptionName("cancelSignal")).thenReturn(executionQuery);
        when(executionQuery.list()).thenReturn(List.of(execution));

        processService.cancelProcess("1");

        verify(runtimeService).signalEventReceived("cancelSignal", "1");
    }

    @Test
    public void cancelProcess_ShouldThrowExceptionWhenNoExecutionsFound() {
        ExecutionQuery executionQuery = mock(ExecutionQuery.class);
        when(runtimeService.createExecutionQuery()).thenReturn(executionQuery);
        when(executionQuery.processInstanceId("1")).thenReturn(executionQuery);
        when(executionQuery.signalEventSubscriptionName("cancelSignal")).thenReturn(executionQuery);
        when(executionQuery.list()).thenReturn(List.of());

        try {
            processService.cancelProcess("1");
        } catch (IllegalStateException e) {
            // expected
        }

        verify(runtimeService, never()).signalEventReceived(anyString(), anyString());
    }
}
