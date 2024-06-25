package com.ntconsult.quotation.services;

import com.ntconsult.quotation.entities.TravelRequest;
import com.ntconsult.quotation.repositories.TravelRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TravelRequestService {

    @Autowired
    private TravelRequestRepository travelRequestRepository;

    public TravelRequest createTravelRequest(TravelRequest travelRequest) {
        return travelRequestRepository.save(travelRequest);
    }

    public Iterable<TravelRequest> getAllTravelRequests() {
        return travelRequestRepository.findAll();
    }
}
