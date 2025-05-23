package com.example.demo.controller;

import com.example.demo.model.Employee;
import com.example.demo.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Create a new employee - secured by SecurityConfig for ROLE_MANAGER
    @PostMapping("/")
    // @PreAuthorize("hasRole('ROLE_MANAGER')") // Example if using method security
    public ResponseEntity<?> createEmployee(@RequestBody Employee employee) {
        if (employee.getEmployeeId() != null && employeeRepository.findByEmployeeId(employee.getEmployeeId()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Error: Employee ID is already taken!");
        }
        if (employee.getEmail() != null && employeeRepository.findByEmail(employee.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Error: Email is already in use!");
        }
        
        employee.setPassword(passwordEncoder.encode(employee.getPassword()));
        
        // Set role: if null or empty, default to "ROLE_USER". Ensure "ROLE_" prefix.
        String role = employee.getRole();
        if (role == null || role.trim().isEmpty()) {
            employee.setRole("ROLE_USER");
        } else if (!role.startsWith("ROLE_")) {
            employee.setRole("ROLE_" + role.toUpperCase());
        } else {
            employee.setRole(role.toUpperCase());
        }
        
        Employee savedEmployee = employeeRepository.save(employee);
        savedEmployee.setPassword(null); // Don't return password
        return ResponseEntity.status(HttpStatus.CREATED).body(savedEmployee);
    }

    // Get employee details by ID - secured by SecurityConfig
    @GetMapping("/{employeeId}")
    public ResponseEntity<Employee> getEmployeeById(@PathVariable String employeeId) {
        Optional<Employee> employeeOpt = employeeRepository.findByEmployeeId(employeeId);
        if (employeeOpt.isPresent()) {
            Employee employee = employeeOpt.get();
            employee.setPassword(null); // Don't return password
            return ResponseEntity.ok(employee);
        }
        return ResponseEntity.notFound().build();
    }

    // Update employee details - secured by SecurityConfig for ROLE_MANAGER
    @PutMapping("/{employeeId}")
    public ResponseEntity<?> updateEmployee(@PathVariable String employeeId, @RequestBody Employee employeeDetails) {
        Optional<Employee> optionalEmployee = employeeRepository.findByEmployeeId(employeeId);
        if (optionalEmployee.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Employee existingEmployee = optionalEmployee.get();
        existingEmployee.setFirstName(employeeDetails.getFirstName());
        existingEmployee.setLastName(employeeDetails.getLastName());

        // Check if email is being changed and if the new email already exists for another user
        if (!existingEmployee.getEmail().equals(employeeDetails.getEmail())) {
            if (employeeRepository.findByEmail(employeeDetails.getEmail()).isPresent()) {
                 return ResponseEntity.status(HttpStatus.CONFLICT).body("Error: New email is already in use!");
            }
            existingEmployee.setEmail(employeeDetails.getEmail());
        }
        
        existingEmployee.setPhoneNumber(employeeDetails.getPhoneNumber());
        existingEmployee.setAddress(employeeDetails.getAddress());
        existingEmployee.setPosition(employeeDetails.getPosition());
        existingEmployee.setDepartment(employeeDetails.getDepartment());
        existingEmployee.setJobStartDate(employeeDetails.getJobStartDate());

        // Update role, ensuring "ROLE_" prefix
        String newRole = employeeDetails.getRole();
        if (newRole != null && !newRole.trim().isEmpty()) {
            if (!newRole.startsWith("ROLE_")) {
                existingEmployee.setRole("ROLE_" + newRole.toUpperCase());
            } else {
                existingEmployee.setRole(newRole.toUpperCase());
            }
        } // If null or empty, retain existing role implicitly

        if (employeeDetails.getPassword() != null && !employeeDetails.getPassword().isEmpty()) {
            existingEmployee.setPassword(passwordEncoder.encode(employeeDetails.getPassword()));
        }

        Employee updatedEmployee = employeeRepository.save(existingEmployee);
        updatedEmployee.setPassword(null); // Don't return password
        return ResponseEntity.ok(updatedEmployee);
    }

    // Get all employees - secured by SecurityConfig for ROLE_MANAGER
    @GetMapping("/")
    public ResponseEntity<List<Employee>> getAllEmployees() {
        List<Employee> employees = employeeRepository.findAll();
        employees.forEach(emp -> emp.setPassword(null)); // Don't return passwords
        return ResponseEntity.ok(employees);
    }

    // Delete an employee - secured by SecurityConfig for ROLE_MANAGER
    @DeleteMapping("/{employeeId}")
    public ResponseEntity<HttpStatus> deleteEmployee(@PathVariable String employeeId) {
        Optional<Employee> employee = employeeRepository.findByEmployeeId(employeeId);
        if (employee.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        // Consider what happens to TireRequests if an employee is deleted.
        // For now, direct deletion.
        employeeRepository.deleteById(employeeId); 
        return ResponseEntity.noContent().build();
    }
}
