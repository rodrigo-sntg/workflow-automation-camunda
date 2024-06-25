package com.ntconsult.workflow.controller;

import com.ntconsult.workflow.dto.ProcessInstanceDTO;
import com.ntconsult.workflow.services.ProcessService;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Tag(name = "Controlador de Processos Camunda", description = "Operações relacionadas aos processos Camunda")
@RestController
@RequestMapping("/api/camunda-processes")
public class CamundaProcessController {

    private static final Logger logger = LoggerFactory.getLogger(CamundaProcessController.class);

    private final RuntimeService runtimeService;
    private final TaskService taskService;
    private final ProcessService processService;
    private final MeterRegistry meterRegistry;

    @Autowired
    public CamundaProcessController(RuntimeService runtimeService, TaskService taskService, ProcessService processService, MeterRegistry meterRegistry) {
        this.runtimeService = runtimeService;
        this.taskService = taskService;
        this.processService = processService;
        this.meterRegistry = meterRegistry;
    }

    @Operation(summary = "Obter todas as instâncias de processos", description = "Obtém uma lista de todas as instâncias de processos em execução.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Instâncias de processos obtidas com sucesso"),
            @ApiResponse(responseCode = "500", description = "Erro ao obter instâncias de processos")
    })
    @GetMapping
    @Timed(value = "processes.getAll", description = "Tempo decorrido para buscar todas as instâncias de processos")
    public List<ProcessInstanceDTO> getAllProcessInstances() {
        long startTime = System.nanoTime();
        meterRegistry.counter("processes.requests", "type", "getAll").increment();
        logger.info("Obtendo todas as instâncias de processos");
        List<ProcessInstance> instances = runtimeService.createProcessInstanceQuery().list();
        long duration = System.nanoTime() - startTime;
        meterRegistry.timer("processes.latency", "type", "getAll").record(duration, TimeUnit.NANOSECONDS);
        logger.info("Número de instâncias de processos recuperadas: {}", instances.size());
        meterRegistry.gauge("processes.availability", instances.size());
        return instances.stream().map(instance -> new ProcessInstanceDTO(instance.getId())).toList();
    }

    @Operation(summary = "Obter uma instância de processo específica", description = "Obtém os detalhes de uma instância de processo específica pelo ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Instância de processo obtida com sucesso"),
            @ApiResponse(responseCode = "404", description = "Instância de processo não encontrada"),
            @ApiResponse(responseCode = "500", description = "Erro ao obter instância de processo")
    })
    @GetMapping("/{processInstanceId}")
    @Timed(value = "processes.get", description = "Tempo decorrido para buscar uma instância de processo específica")
    public ProcessInstanceDTO getProcessInstance(@PathVariable String processInstanceId) {
        long startTime = System.nanoTime();
        meterRegistry.counter("processes.requests", "type", "get").increment();
        logger.info("Buscando instância de processo com ID: {}", processInstanceId);
        ProcessInstance instance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        long duration = System.nanoTime() - startTime;
        meterRegistry.timer("processes.latency", "type", "get").record(duration, TimeUnit.NANOSECONDS);
        if (instance != null) {
            logger.info("Instância de processo encontrada: ID {}", instance.getId());
            meterRegistry.gauge("processes.availability", 1);
            return new ProcessInstanceDTO(instance.getId());
        } else {
            logger.warn("Instância de processo com ID {} não encontrada", processInstanceId);
            meterRegistry.counter("processes.errors", "type", "get").increment();
            meterRegistry.gauge("processes.availability", 0);
            return null;
        }
    }

    @Operation(summary = "Obter as tarefas atuais de uma instância de processo", description = "Obtém uma lista de tarefas atuais para uma instância de processo específica.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tarefas obtidas com sucesso"),
            @ApiResponse(responseCode = "500", description = "Erro ao obter tarefas")
    })
    @GetMapping("/{processInstanceId}/tasks")
    @Timed(value = "processes.getCurrentTasks", description = "Tempo decorrido para buscar as tarefas atuais de uma instância de processo")
    public List<String> getCurrentTasks(@PathVariable String processInstanceId) {
        long startTime = System.nanoTime();
        meterRegistry.counter("processes.requests", "type", "getCurrentTasks").increment();
        logger.info("Obtendo tarefas atuais para a instância de processo ID: {}", processInstanceId);
        List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstanceId).list();
        long duration = System.nanoTime() - startTime;
        meterRegistry.timer("processes.latency", "type", "getCurrentTasks").record(duration, TimeUnit.NANOSECONDS);
        logger.info("Número de tarefas recuperadas: {}", tasks.size());
        return tasks.stream().map(task -> "Task ID: " + task.getId() + ", Task Name: " + task.getName()).toList();
    }

    @Operation(summary = "Cancelar uma instância de processo", description = "Cancela uma instância de processo específica pelo ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Processo cancelado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Processo não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro ao cancelar o processo")
    })
    @GetMapping("/{id}/cancel")
    @Timed(value = "processes.cancel", description = "Tempo decorrido para cancelar uma instância de processo")
    public ResponseEntity<Void> cancelTravelRequest(@PathVariable String id) {
        long startTime = System.nanoTime();
        meterRegistry.counter("processes.requests", "type", "cancel").increment();
        logger.info("Tentando cancelar o processo com ID: {}", id);
        try {
            processService.cancelProcess(id);
            long duration = System.nanoTime() - startTime;
            meterRegistry.timer("processes.latency", "type", "cancel").record(duration, TimeUnit.NANOSECONDS);
            logger.info("Processo com ID {} cancelado com sucesso", id);
            meterRegistry.gauge("processes.availability", 1);
            return ResponseEntity.ok().build();  // HTTP 200 with no content
        } catch (Exception e) {
            long duration = System.nanoTime() - startTime;
            meterRegistry.timer("processes.latency", "type", "cancel").record(duration, TimeUnit.NANOSECONDS);
            meterRegistry.counter("processes.errors", "type", "cancel").increment();
            logger.error("Erro ao cancelar o processo com ID: {}", id, e);
            meterRegistry.gauge("processes.availability", 0);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Processo não encontrado.", e);
        }
    }
}
