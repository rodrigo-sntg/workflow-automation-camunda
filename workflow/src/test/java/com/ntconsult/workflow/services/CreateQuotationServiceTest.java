package com.ntconsult.workflow.services;

import com.ntconsult.workflow.entities.TravelRequest;
import com.ntconsult.workflow.helpers.TravelRequestCreator;
import com.ntconsult.workflow.services.taskservice.CreateQuotationService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.Date;

import static org.mockito.Mockito.*;

class CreateQuotationServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private DelegateExecution execution;

    @InjectMocks
    private CreateQuotationService createQuotationService;

    @Value("${quotation.api_url}")
    private String apiTravelRequests = "http://example.com/api/quotations";

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        createQuotationService = new CreateQuotationService(restTemplate);

        createQuotationService.setApiTravelRequests(apiTravelRequests);
    }

    @Test
    public void execute_ShouldCreateQuotation() throws Exception {
        TravelRequest travelRequest = new TravelRequest(
                "Vendas", "São Paulo", "Rio de Janeiro",
                new Date(), new Date(), 1000.0, 2000.0, "test@example.com");

        when(execution.getVariable("departamento")).thenReturn(travelRequest.getDepartamento());
        when(execution.getVariable("origem")).thenReturn(travelRequest.getOrigem());
        when(execution.getVariable("destino")).thenReturn(travelRequest.getDestino());
        when(execution.getVariable("dataInicio")).thenReturn(travelRequest.getDataInicio());
        when(execution.getVariable("dataFim")).thenReturn(travelRequest.getDataFim());
        when(execution.getVariable("valorAdiantamento")).thenReturn(travelRequest.getValorAdiantamento());
        when(execution.getVariable("valorTotal")).thenReturn(travelRequest.getValorTotal());
        when(execution.getVariable("emailSolicitante")).thenReturn(travelRequest.getEmailSolicitante());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<TravelRequest> entity = new HttpEntity<>(travelRequest, headers);

        ResponseEntity<String> response = mock(ResponseEntity.class);
        when(response.getStatusCode()).thenReturn(org.springframework.http.HttpStatus.OK);
        when(restTemplate.postForEntity(eq("http://example.com/api/quotations"), eq(entity), eq(String.class))).thenReturn(response);

        createQuotationService.execute(execution);

        verify(execution).setVariable("quotationStatus", "created");
    }

    @Test
    public void execute_ShouldHandleApiFailure() throws Exception {
        TravelRequest travelRequest = new TravelRequest(
                "Vendas", "São Paulo", "Rio de Janeiro",
                new Date(), new Date(), 1000.0, 2000.0, "test@example.com");

        when(TravelRequestCreator.fromExecution(execution)).thenReturn(travelRequest);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<TravelRequest> entity = new HttpEntity<>(travelRequest, headers);
        ResponseEntity<String> response = mock(ResponseEntity.class);
        when(response.getStatusCode()).thenReturn(HttpStatus.BAD_REQUEST);
        when(restTemplate.postForEntity(apiTravelRequests, entity, String.class)).thenReturn(response);

        createQuotationService.execute(execution);

        verify(execution).setVariable("quotationStatus", "manual");
    }
}
