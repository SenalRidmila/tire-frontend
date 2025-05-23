package com.example.demo.repository;

import com.example.demo.model.TireRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TireRequestRepository extends MongoRepository<TireRequest, String> {

    List<TireRequest> findByStatus(String status);

    List<TireRequest> findByVehicleNo(String vehicleNo);

    List<TireRequest> findByOfficerServiceNo(String officerServiceNo);

}
