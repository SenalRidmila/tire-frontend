package com.example.demo.controller;

import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.LoginRequest;
import com.example.demo.model.Employee;
import com.example.demo.repository.EmployeeRepository;
import com.example.demo.service.AppUserDetailsService; // Needed for SecurityConfig
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.is;


@WebMvcTest(AuthController.class)
public class AuthControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeRepository employeeRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private AuthenticationManager authenticationManager;
    
    @MockBean
    private AppUserDetailsService appUserDetailsService; // Mock this as SecurityConfig might try to wire it

    @Autowired
    private ObjectMapper objectMapper;

    private Employee employee;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        employee = new Employee("E001", "John", "Doe", "john.doe@example.com", "password123", "ROLE_USER");
        loginRequest = new LoginRequest("john.doe@example.com", "password123");
    }

    @Test
    void registerEmployee_success() throws Exception {
        when(employeeRepository.findByEmployeeId(anyString())).thenReturn(Optional.empty());
        when(employeeRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> {
            Employee emp = invocation.getArgument(0);
            emp.setPassword("encodedPassword"); // Simulate password encoding being set
            return emp;
        });

        ResultActions resultActions = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employee)));

        resultActions.andExpect(status().isCreated())
                .andExpect(jsonPath("$.message", is("Employee registered successfully!")))
                .andExpect(jsonPath("$.employeeDetails.email", is(employee.getEmail())))
                .andExpect(jsonPath("$.employeeDetails.password").doesNotExist()); // Ensure password is not returned

        verify(employeeRepository).save(any(Employee.class));
        verify(passwordEncoder).encode("password123");
    }

    @Test
    void registerEmployee_emailExists() throws Exception {
        when(employeeRepository.findByEmail(employee.getEmail())).thenReturn(Optional.of(employee));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", is("Error: Email is already in use!")));

        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void registerEmployee_employeeIdExists() throws Exception {
        when(employeeRepository.findByEmployeeId(employee.getEmployeeId())).thenReturn(Optional.of(employee));
        when(employeeRepository.findByEmail(anyString())).thenReturn(Optional.empty()); // Email is different for this test case

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", is("Error: Employee ID is already taken!")));

        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void authenticateUser_success() throws Exception {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            loginRequest.getEmail(), loginRequest.getPassword()
        ); // A mock Authentication object
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        
        // Prepare a version of the employee without password, as it would be after fetching from DB
        Employee employeeFromDb = new Employee(employee.getEmployeeId(), employee.getFirstName(), employee.getLastName(), employee.getEmail(), "encodedPassword", employee.getRole());
        when(employeeRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(employeeFromDb));


        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Login successful!")))
                .andExpect(jsonPath("$.employeeDetails.email", is(loginRequest.getEmail())))
                .andExpect(jsonPath("$.employeeDetails.password").doesNotExist());
    }

    @Test
    void authenticateUser_invalidCredentials() throws Exception {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", is("Error: Invalid email or password.")));
    }
}
