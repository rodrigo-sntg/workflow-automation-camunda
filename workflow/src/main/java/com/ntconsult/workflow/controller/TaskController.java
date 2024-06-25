package com.ntconsult.workflow.controller;

import com.ntconsult.workflow.dto.TaskDto;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Tag(name = "Controlador de Tarefas", description = "Operações relacionadas a tarefas")
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private static final Logger logger = LoggerFactory.getLogger(TaskController.class);

    @Autowired
    private TaskService taskService;

    @Autowired
    private MeterRegistry meterRegistry;

    @Operation(summary = "Obter todas as tarefas", description = "Obtém uma lista de todas as tarefas em execução.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tarefas obtidas com sucesso"),
            @ApiResponse(responseCode = "500", description = "Erro ao obter tarefas")
    })
    @GetMapping
    @Timed(value = "tasks.get", description = "Tempo decorrido para buscar as tarefas")
    public List<TaskDto> getTasks() {
        long startTime = System.nanoTime();
        meterRegistry.counter("tasks.requests", "type", "get").increment();
        logger.info("Buscando todas as tarefas");
        List<Task> tasks = taskService.createTaskQuery().list();
        long duration = System.nanoTime() - startTime;
        meterRegistry.timer("tasks.latency", "type", "get").record(duration, TimeUnit.NANOSECONDS);
        logger.info("Tarefas recuperadas com sucesso");
        meterRegistry.gauge("tasks.availability", tasks.size());
        return tasks.stream()
                .map(task -> new TaskDto(task.getId(), task.getName()))
                .collect(Collectors.toList());
    }

    @Operation(summary = "Obter uma tarefa específica", description = "Obtém os detalhes de uma tarefa específica pelo ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tarefa obtida com sucesso"),
            @ApiResponse(responseCode = "404", description = "Tarefa não encontrada"),
            @ApiResponse(responseCode = "500", description = "Erro ao obter tarefa")
    })
    @GetMapping("/{id}")
    @Timed(value = "task.get", description = "Tempo decorrido para buscar uma tarefa específica")
    public TaskDto getTask(@PathVariable String id) {
        long startTime = System.nanoTime();
        meterRegistry.counter("tasks.requests", "type", "get_specific").increment();
        logger.info("Buscando tarefa com ID: {}", id);
        Task task = taskService.createTaskQuery().taskId(id).singleResult();
        long duration = System.nanoTime() - startTime;
        meterRegistry.timer("tasks.latency", "type", "get_specific").record(duration, TimeUnit.NANOSECONDS);
        if (task != null) {
            logger.info("Tarefa encontrada: {} - {}", task.getName(), task.getId());
            meterRegistry.gauge("tasks.availability", 1);
            return new TaskDto(task.getId(), task.getName());
        } else {
            logger.warn("Tarefa com ID {} não encontrada", id);
            meterRegistry.counter("tasks.errors", "type", "get_specific").increment();
            meterRegistry.gauge("tasks.availability", 0);
            return null;
        }
    }

    @Operation(summary = "Completar uma tarefa", description = "Completa uma tarefa específica pelo ID.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Exemplo de Variáveis",
                                    value = "{\"approvalStatus\": \"approved\"}"
                            )
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Tarefa completada com sucesso"),
                    @ApiResponse(responseCode = "404", description = "Tarefa não encontrada"),
                    @ApiResponse(responseCode = "500", description = "Erro ao completar tarefa")
            })
    @PostMapping("/{id}/complete")
    @Timed(value = "tasks.complete", description = "Tempo levado para completar uma tarefa")
    public void completeTask(@PathVariable String id, @RequestBody Map<String, Object> variables) {
        long startTime = System.nanoTime();
        meterRegistry.counter("tasks.requests", "type", "complete").increment();
        logger.info("Completando tarefa com ID: {}", id);
        try {
            taskService.complete(id, variables);
            long duration = System.nanoTime() - startTime;
            meterRegistry.timer("tasks.latency", "type", "complete").record(duration, TimeUnit.NANOSECONDS);
            logger.info("Tarefa com ID {} completada com sucesso", id);
            meterRegistry.gauge("tasks.availability", 1);
        } catch (Exception e) {
            long duration = System.nanoTime() - startTime;
            meterRegistry.timer("tasks.latency", "type", "complete").record(duration, TimeUnit.NANOSECONDS);
            meterRegistry.counter("tasks.errors", "type", "complete").increment();
            logger.error("Erro ao completar tarefa com ID: {}", id, e);
            meterRegistry.gauge("tasks.availability", 0);
            throw e;
        }
    }
}
