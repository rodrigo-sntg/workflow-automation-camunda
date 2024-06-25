package com.ntconsult.quotation.repositories;

import com.ntconsult.quotation.entities.TravelRequest;

import org.springframework.data.repository.CrudRepository;

public interface TravelRequestRepository extends CrudRepository<TravelRequest, Long> {
}
