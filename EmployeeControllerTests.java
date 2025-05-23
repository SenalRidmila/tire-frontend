package com.example.demo.controller;

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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser; // For testing secured endpoints
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.hasSize;

@WebMvcTest(EmployeeController.class)
@WithMockUser(username = "testuser", roles = {"MANAGER"}) // Default mock user for tests requiring authentication
public class EmployeeControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeRepository employeeRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private AppUserDetailsService appUserDetailsService; // Mock this for SecurityConfig

    @Autowired
    private ObjectMapper objectMapper;

    private Employee employee1;
    private Employee employee2;

    @BeforeEach
    void setUp() {
        employee1 = new Employee("E001", "Alice", "Smith", "alice@example.com", "password123", "ROLE_USER");
        employee2 = new Employee("E002", "Bob", "Johnson", "bob@example.com", "password456", "ROLE_TTO");
    }

    @Test
    void createEmployee_success() throws Exception {
        when(employeeRepository.findByEmployeeId(anyString())).thenReturn(Optional.empty());
        when(employeeRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> {
            Employee emp = invocation.getArgument(0);
            // Simulate ID generation if not set, and password encoding
            if (emp.getEmployeeId() == null) emp.setEmployeeId("E_NEW");
            emp.setPassword("encodedPassword");
            return emp;
        });

        mockMvc.perform(post("/api/employees/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employee1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email", is(employee1.getEmail())))
                .andExpect(jsonPath("$.password").doesNotExist()); // Ensure password is not returned

        verify(employeeRepository).save(any(Employee.class));
        verify(passwordEncoder).encode("password123");
    }

    @Test
    void createEmployee_emailExists() throws Exception {
        when(employeeRepository.findByEmail(employee1.getEmail())).thenReturn(Optional.of(employee1));

        mockMvc.perform(post("/api/employees/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employee1)))
                .andExpect(status().isConflict())
                .andExpect(content().string("Error: Email is already in use!"));

        verify(employeeRepository, never()).save(any(Employee.class));
    }
    
    @Test
    void createEmployee_employeeIdExists() throws Exception {
        when(employeeRepository.findByEmployeeId(employee1.getEmployeeId())).thenReturn(Optional.of(employee1));
        // Make sure email check passes for this specific test case, or is handled first by controller
        when(employeeRepository.findByEmail(employee1.getEmail())).thenReturn(Optional.empty());


        mockMvc.perform(post("/api/employees/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employee1)))
                .andExpect(status().isConflict())
                .andExpect(content().string("Error: Employee ID is already taken!"));

        verify(employeeRepository, never()).save(any(Employee.class));
    }


    @Test
    void getEmployeeById_success() throws Exception {
        when(employeeRepository.findByEmployeeId("E001")).thenReturn(Optional.of(employee1));

        mockMvc.perform(get("/api/employees/{employeeId}", "E001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is(employee1.getEmail())))
                .andExpect(jsonPath("$.employeeId", is("E001")))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void getEmployeeById_notFound() throws Exception {
        when(employeeRepository.findByEmployeeId("E_NON_EXISTENT")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/employees/{employeeId}", "E_NON_EXISTENT"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateEmployee_success() throws Exception {
        Employee updatedDetails = new Employee("E001", "Alice", "Jones", "alice.jones@example.com", "newPassword", "ROLE_MANAGER");
        
        when(employeeRepository.findByEmployeeId("E001")).thenReturn(Optional.of(employee1)); // Return existing employee
        when(employeeRepository.findByEmail(updatedDetails.getEmail())).thenReturn(Optional.empty()); // New email is not taken
        when(passwordEncoder.encode("newPassword")).thenReturn("newEncodedPassword");
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(put("/api/employees/{employeeId}", "E001")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is(updatedDetails.getEmail())))
                .andExpect(jsonPath("$.lastName", is("Jones")))
                .andExpect(jsonPath("$.role", is("ROLE_MANAGER")))
                .andExpect(jsonPath("$.password").doesNotExist());

        verify(employeeRepository).save(any(Employee.class));
        verify(passwordEncoder).encode("newPassword");
    }
    
    @Test
    void updateEmployee_onlyRoleUpdate() throws Exception {
        Employee updatedDetails = new Employee(); // Only role is set for update
        updatedDetails.setRole("ROLE_TTO");
        // Keep other fields null to test if existing values are preserved
        updatedDetails.setFirstName(employee1.getFirstName()); // Required by controller logic for non-null
        updatedDetails.setLastName(employee1.getLastName()); // Required by controller logic for non-null
        updatedDetails.setEmail(employee1.getEmail()); // Required by controller logic for non-null

        when(employeeRepository.findByEmployeeId("E001")).thenReturn(Optional.of(employee1));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));
        // No password change, so passwordEncoder.encode should not be called.

        mockMvc.perform(put("/api/employees/{employeeId}", "E001")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role", is("ROLE_TTO")))
                .andExpect(jsonPath("$.email", is(employee1.getEmail()))); // Ensure other fields are preserved

        verify(employeeRepository).save(any(Employee.class));
        verify(passwordEncoder, never()).encode(anyString()); // Password not changed
    }


    @Test
    void updateEmployee_notFound() throws Exception {
        Employee updatedDetails = new Employee("E_NON_EXISTENT", "Test", "User", "test@example.com", "password", "ROLE_USER");
        when(employeeRepository.findByEmployeeId("E_NON_EXISTENT")).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/employees/{employeeId}", "E_NON_EXISTENT")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedDetails)))
                .andExpect(status().isNotFound());
    }
    
    @Test
    void updateEmployee_emailConflict() throws Exception {
        Employee existingEmployeeWithEmail = new Employee("E002", "Other", "User", "alice.jones@example.com", "pwd", "ROLE_USER");
        Employee employeeToUpdate = new Employee("E001", "Alice", "Smith", "alice.smith@example.com", "pwd", "ROLE_USER"); // Original email
        Employee updateData = new Employee("E001", "Alice", "Smith", "alice.jones@example.com", "pwd", "ROLE_USER"); // Attempting to change email to existing one

        when(employeeRepository.findByEmployeeId("E001")).thenReturn(Optional.of(employeeToUpdate));
        when(employeeRepository.findByEmail("alice.jones@example.com")).thenReturn(Optional.of(existingEmployeeWithEmail)); // This email is taken by E002

        mockMvc.perform(put("/api/employees/{employeeId}", "E001")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateData)))
                .andExpect(status().isConflict())
                .andExpect(content().string("Error: New email is already in use!"));
    }


    @Test
    void getAllEmployees_success() throws Exception {
        List<Employee> employees = Arrays.asList(employee1, employee2);
        when(employeeRepository.findAll()).thenReturn(employees);

        mockMvc.perform(get("/api/employees/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].email", is(employee1.getEmail())))
                .andExpect(jsonPath("$[1].email", is(employee2.getEmail())))
                .andExpect(jsonPath("$[0].password").doesNotExist())
                .andExpect(jsonPath("$[1].password").doesNotExist());
    }

    @Test
    void deleteEmployee_success() throws Exception {
        when(employeeRepository.existsById("E001")).thenReturn(true);
        doNothing().when(employeeRepository).deleteById("E001");

        mockMvc.perform(delete("/api/employees/{employeeId}", "E001"))
                .andExpect(status().isNoContent());

        verify(employeeRepository).deleteById("E001");
    }

    @Test
    void deleteEmployee_notFound() throws Exception {
        when(employeeRepository.existsById("E_NON_EXISTENT")).thenReturn(false);

        mockMvc.perform(delete("/api/employees/{employeeId}", "E_NON_EXISTENT"))
                .andExpect(status().isNotFound());

        verify(employeeRepository, never()).deleteById(anyString());
    }
}
