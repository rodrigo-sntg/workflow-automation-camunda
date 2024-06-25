package com.ntconsult.workflow.controllers;

import com.ntconsult.workflow.controller.TravelRequestController;
import com.ntconsult.workflow.entities.TravelRequest;
import com.ntconsult.workflow.services.ProcessService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TravelRequestControllerTest {

    @Mock
    private ProcessService processService;

    @InjectMocks
    private TravelRequestController travelRequestController;

    private MockMvc mockMvc;

    @Captor
    private ArgumentCaptor<TravelRequest> travelRequestCaptor;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(travelRequestController).build();
    }

    @Test
    void createTravelRequest_ShouldReturnOk() throws Exception {
        String jsonRequest = "{\"departamento\":\"Vendas\",\"origem\":\"São Paulo\",\"destino\":\"Rio de Janeiro\"," +
                "\"dataInicio\":\"2023-05-20T00:00:00Z\",\"dataFim\":\"2023-05-21T00:00:00Z\"," +
                "\"valorAdiantamento\":1000.0,\"valorTotal\":2000.0,\"emailSolicitante\":\"test@example.com\"}";

        mockMvc.perform(post("/api/travel-requests-process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk());

        verify(processService).startProcess(travelRequestCaptor.capture());
        TravelRequest capturedRequest = travelRequestCaptor.getValue();

        // Verifique os campos do TravelRequest
        assertEquals("Vendas", capturedRequest.getDepartamento());
        assertEquals("São Paulo", capturedRequest.getOrigem());
        assertEquals("Rio de Janeiro", capturedRequest.getDestino());
        assertEquals(1000.0, capturedRequest.getValorAdiantamento());
        assertEquals(2000.0, capturedRequest.getValorTotal());
        assertEquals("test@example.com", capturedRequest.getEmailSolicitante());

        // Verifique os campos de data
        DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT.withZone(ZoneOffset.UTC);
        Instant expectedDataInicio = Instant.parse("2023-05-20T00:00:00Z");
        Instant expectedDataFim = Instant.parse("2023-05-21T00:00:00Z");
        assertEquals(expectedDataInicio, capturedRequest.getDataInicio().toInstant());
        assertEquals(expectedDataFim, capturedRequest.getDataFim().toInstant());
    }
}