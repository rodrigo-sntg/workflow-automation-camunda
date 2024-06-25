package com.ntconsult.workflow.controllers;

import com.ntconsult.workflow.controller.TaskController;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TaskControllerTest {

    @Mock
    private TaskService taskService;

    @InjectMocks
    private TaskController taskController;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(taskController).build();
    }

    @Test
    void getTasks_ShouldReturnOk() throws Exception {
        Task task = mock(Task.class);
        when(task.getName()).thenReturn("Test Task");
        when(task.getId()).thenReturn("1");

        TaskQuery taskQuery = mock(TaskQuery.class);
        when(taskService.createTaskQuery()).thenReturn(taskQuery);
        when(taskQuery.list()).thenReturn(List.of(task));

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk());

        verify(taskService).createTaskQuery();
    }

    @Test
    void getTask_ShouldReturnOk() throws Exception {
        Task task = mock(Task.class);
        when(task.getId()).thenReturn("1");
        when(task.getName()).thenReturn("Test Task");

        TaskQuery taskQuery = mock(TaskQuery.class);
        when(taskService.createTaskQuery()).thenReturn(taskQuery);
        when(taskQuery.taskId("1")).thenReturn(taskQuery);
        when(taskQuery.singleResult()).thenReturn(task);

        mockMvc.perform(get("/api/tasks/1"))
                .andExpect(status().isOk());
    }

    @Test
    void completeTask_ShouldReturnOk() throws Exception {
        mockMvc.perform(post("/api/tasks/1/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"variable\":\"value\"}"))
                .andExpect(status().isOk());

        verify(taskService).complete(eq("1"), anyMap());
    }
}
