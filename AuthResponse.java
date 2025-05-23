package com.example.demo.dto;

import com.example.demo.model.Employee;

public class AuthResponse {
    private String message;
    private String token; // For JWT, can be null for now
    private Employee employeeDetails; // Optional: send some user details back

    public AuthResponse(String message) {
        this.message = message;
    }

    public AuthResponse(String message, String token) {
        this.message = message;
        this.token = token;
    }

    public AuthResponse(String message, Employee employeeDetails) {
        this.message = message;
        this.employeeDetails = employeeDetails;
    }

    public AuthResponse(String message, String token, Employee employeeDetails) {
        this.message = message;
        this.token = token;
        this.employeeDetails = employeeDetails;
    }

    // Getters and Setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Employee getEmployeeDetails() {
        return employeeDetails;
    }

    public void setEmployeeDetails(Employee employeeDetails) {
        this.employeeDetails = employeeDetails;
    }
}
