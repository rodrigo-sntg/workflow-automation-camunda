package com.ntconsult.workflow.controllers;

import com.ntconsult.workflow.controller.CamundaProcessController;
import com.ntconsult.workflow.dto.ProcessInstanceDTO;
import com.ntconsult.workflow.services.ProcessService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CamundaProcessControllerTest {

    @Mock
    private RuntimeService runtimeService;

    @Mock
    private TaskService taskService;

    @Mock
    private ProcessService processService;

    @InjectMocks
    private CamundaProcessController camundaProcessController;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(camundaProcessController).build();
    }

    @Test
    void getAllProcessInstances_ShouldReturnOk() throws Exception {
        ProcessInstance instance = mock(ProcessInstance.class);
        ProcessInstanceQuery processInstanceQuery = mock(ProcessInstanceQuery.class);

        when(runtimeService.createProcessInstanceQuery()).thenReturn(processInstanceQuery);
        when(processInstanceQuery.list()).thenReturn(List.of(instance));
        when(instance.getId()).thenReturn("1");

        mockMvc.perform(get("/api/camunda-processes"))
                .andExpect(status().isOk());

        verify(runtimeService).createProcessInstanceQuery();
        verify(processInstanceQuery).list();
    }

    @Test
    void getProcessInstance_ShouldReturnOk() throws Exception {
        ProcessInstance instance = mock(ProcessInstance.class);
        ProcessInstanceQuery processInstanceQuery = mock(ProcessInstanceQuery.class);

        when(runtimeService.createProcessInstanceQuery()).thenReturn(processInstanceQuery);
        when(processInstanceQuery.processInstanceId("1")).thenReturn(processInstanceQuery);
        when(processInstanceQuery.singleResult()).thenReturn(instance);
        when(instance.getId()).thenReturn("1");

        mockMvc.perform(get("/api/camunda-processes/1"))
                .andExpect(status().isOk());

        verify(runtimeService).createProcessInstanceQuery();
        verify(processInstanceQuery).processInstanceId("1");
        verify(processInstanceQuery).singleResult();
    }

    @Test
    void getCurrentTasks_ShouldReturnOk() throws Exception {
        Task task = mock(Task.class);
        TaskQuery taskQuery = mock(TaskQuery.class);

        when(taskService.createTaskQuery()).thenReturn(taskQuery);
        when(taskQuery.processInstanceId("1")).thenReturn(taskQuery);
        when(taskQuery.list()).thenReturn(List.of(task));

        mockMvc.perform(get("/api/camunda-processes/1/tasks"))
                .andExpect(status().isOk());

        verify(taskService).createTaskQuery();
        verify(taskQuery).processInstanceId("1");
        verify(taskQuery).list();
    }

    @Test
    void cancelTravelRequest_ShouldReturnOk() throws Exception {
        doNothing().when(processService).cancelProcess("1");

        mockMvc.perform(get("/api/camunda-processes/1/cancel"))
                .andExpect(status().isOk());

        verify(processService).cancelProcess("1");
    }

    @Test
    void cancelTravelRequest_ShouldReturnNotFound() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Processo não encontrado.")).when(processService).cancelProcess("1");

        mockMvc.perform(get("/api/camunda-processes/1/cancel"))
                .andExpect(status().isNotFound());

        verify(processService).cancelProcess("1");
    }
}
