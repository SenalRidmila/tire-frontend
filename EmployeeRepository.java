package com.example.demo.repository;

import com.example.demo.model.Employee;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeRepository extends MongoRepository<Employee, String> {

    Optional<Employee> findByEmail(String email); // Used for login

    Optional<Employee> findByEmployeeId(String employeeId);

}
