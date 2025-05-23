package com.example.demo.controller;

import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.LoginRequest;
import com.example.demo.model.Employee;
import com.example.demo.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager; // Autowire AuthenticationManager

    @PostMapping("/register")
    public ResponseEntity<?> registerEmployee(@RequestBody Employee employee) {
        if (employee.getEmployeeId() != null && employeeRepository.findByEmployeeId(employee.getEmployeeId()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new AuthResponse("Error: Employee ID is already taken!"));
        }

        if (employee.getEmail() != null && employeeRepository.findByEmail(employee.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new AuthResponse("Error: Email is already in use!"));
        }

        // Hash password before saving
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
        savedEmployee.setPassword(null); 
        return ResponseEntity.status(HttpStatus.CREATED).body(new AuthResponse("Employee registered successfully!", savedEmployee));
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Optionally, retrieve employee details to return in response (excluding password)
            Optional<Employee> employeeOptional = employeeRepository.findByEmail(loginRequest.getEmail());
            if (employeeOptional.isPresent()) {
                Employee employee = employeeOptional.get();
                employee.setPassword(null); // Don't send password back
                return ResponseEntity.ok(new AuthResponse("Login successful!", employee));
            } else {
                // Should not happen if authentication was successful
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new AuthResponse("Login successful but could not retrieve user details."));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new AuthResponse("Error: Invalid email or password."));
        }
    }
}
