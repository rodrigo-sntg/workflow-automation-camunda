package com.ntconsult.quotation.controllers;

import com.ntconsult.quotation.entities.TravelRequest;
import com.ntconsult.quotation.services.TravelRequestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/travel-requests")
public class TravelRequestController {
    private static final Logger logger = LoggerFactory.getLogger(TravelRequestController.class);

    @Autowired
    private TravelRequestService travelRequestService;

    @PostMapping
    public ResponseEntity<TravelRequest> createTravelRequest(@RequestBody TravelRequest travelRequest) {
        logger.info("Criando nova solicitação de viagem: {}", travelRequest);
        TravelRequest savedRequest = travelRequestService.createTravelRequest(travelRequest);
        logger.info("Solicitação de viagem criada: {}", savedRequest);

        return new ResponseEntity<>(savedRequest, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<Iterable<TravelRequest>> getAllTravelRequests() {
        logger.info("Buscando solicitações de viagens.");
        Iterable<TravelRequest> travelRequests = travelRequestService.getAllTravelRequests();
        logger.info("Solicitações de viagens encontradas.");
        return new ResponseEntity<>(travelRequests, HttpStatus.OK);
    }
}
