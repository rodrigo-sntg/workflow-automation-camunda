package com.ntconsult.workflow.controller;

import com.ntconsult.workflow.entities.TravelRequest;
import com.ntconsult.workflow.services.ProcessService;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@Tag(name = "Controlador de Solicitações de Viagem", description = "Operações relacionadas a solicitações de viagem")
@RestController
@RequestMapping("/api/travel-requests-process")
public class TravelRequestController {
    private static final Logger logger = LoggerFactory.getLogger(TravelRequestController.class);

    private final ProcessService processService;
    private final MeterRegistry meterRegistry;

    @Autowired
    public TravelRequestController(ProcessService processService, MeterRegistry meterRegistry) {
        this.processService = processService;
        this.meterRegistry = meterRegistry;
    }

    @Operation(summary = "Criar uma nova solicitação de viagem", description = "Cria uma nova solicitação de viagem e inicia o processo.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TravelRequest.class),
                            examples = @ExampleObject(
                                    name = "Exemplo de Solicitação de Viagem",
                                    value = "{\"departamento\": \"Marketing\", \"origem\": \"São Paulo\", \"destino\": \"Rio de Janeiro\", \"dataInicio\": \"2024-06-01\", \"dataFim\": \"2024-06-05\", \"valorAdiantamento\": 300.0, \"valorTotal\": 1000.0, \"emailSolicitante\": \"example@example.com\"}"
                            )
                    )
            ))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Solicitação de viagem criada com sucesso"),
            @ApiResponse(responseCode = "500", description = "Erro ao criar solicitação de viagem")
    })
    @PostMapping
    @Timed(value = "travelRequests.create", description = "Tempo decorrido para criar uma solicitação de viagem")
    public ResponseEntity<TravelRequest> createTravelRequest(@RequestBody TravelRequest travelRequest) {
        long startTime = System.nanoTime();
        meterRegistry.counter("travelRequests.requests", "type", "create").increment();
        logger.info("Iniciando processo de solicitação de viagem.");
        try {
            processService.startProcess(travelRequest);
            long duration = System.nanoTime() - startTime;
            meterRegistry.timer("travelRequests.latency", "type", "create").record(duration, TimeUnit.NANOSECONDS);
            logger.info("Processo de solicitação de viagem iniciado.");
            meterRegistry.gauge("travelRequests.availability", 1);
            return ResponseEntity.ok(travelRequest);
        } catch (Exception e) {
            long duration = System.nanoTime() - startTime;
            meterRegistry.timer("travelRequests.latency", "type", "create").record(duration, TimeUnit.NANOSECONDS);
            meterRegistry.counter("travelRequests.errors", "type", "create").increment();
            meterRegistry.gauge("travelRequests.availability", 0);
            logger.error("Erro ao iniciar o processo de solicitação de viagem.", e);
            throw e;
        }
    }
}
