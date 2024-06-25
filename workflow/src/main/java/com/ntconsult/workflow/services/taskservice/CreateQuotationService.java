package com.ntconsult.workflow.services.taskservice;

import com.ntconsult.workflow.entities.TravelRequest;
import com.ntconsult.workflow.helpers.TravelRequestCreator;
import lombok.Setter;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class CreateQuotationService implements JavaDelegate {
    private static final Logger logger = LoggerFactory.getLogger(CreateQuotationService.class);



    @Value("${quotation.api_url}")
    @Setter
    private String apiTravelRequests;

    private final RestTemplate restTemplate;

    @Autowired
    public CreateQuotationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public void execute(DelegateExecution execution) {
        try {
            logger.info("Iniciando a criação da cotação...");
            TravelRequest travelRequest = TravelRequestCreator.fromExecution(execution);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<TravelRequest> entity = new HttpEntity<>(travelRequest, headers);

            logger.info("Enviando solicitação para API de cotações: {}", apiTravelRequests);
            ResponseEntity<String> response = restTemplate.postForEntity(apiTravelRequests, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("Cotação criada com sucesso. Status Code: {}", response.getStatusCode());
                execution.setVariable("quotationStatus", "created");
            } else {
                handleErrorResponse(response);
            }
        } catch (Exception e) {
            logger.error("Erro ao criar a cotação: {}", e.getMessage());
            execution.setVariable("quotationStatus", "manual");
        }
    }

    private void handleErrorResponse(ResponseEntity<String> response) throws Exception {
        logger.error("Falha na chamada da API. Status Code: {}", response.getStatusCode());
        throw new Exception("API call failed with status code: " + response.getStatusCode());
    }

}
