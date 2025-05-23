package com.example.demo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.HashSet;
import java.util.Set; // For storing multiple roles if needed, or just a String for one role

@Document(collection = "employees")
public class Employee {

    @Id
    private String employeeId; // Primary key, e.g., service number

    private String firstName;
    private String lastName;
    private String email; // Should be unique
    private String phoneNumber;
    private String address;
    private String position;
    private String department;
    private Date jobStartDate;
    private String password; // Will be hashed
    private String role; // Example: "ROLE_USER", "ROLE_MANAGER", "ROLE_TTO"

    // Default constructor
    public Employee() {
    }

    // Constructor with all fields
    public Employee(String employeeId, String firstName, String lastName, String email, String phoneNumber,
                    String address, String position, String department, Date jobStartDate, String password, String role) {
        this.employeeId = employeeId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.position = position;
        this.department = department;
        this.jobStartDate = jobStartDate;
        this.password = password;
        // Ensure role is prefixed correctly if not already
        if (role != null && !role.startsWith("ROLE_")) {
            this.role = "ROLE_" + role.toUpperCase();
        } else if (role != null) {
            this.role = role.toUpperCase();
        } else {
            this.role = "ROLE_USER"; // Default role
        }
    }

    // Getters and Setters
    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public Date getJobStartDate() { return jobStartDate; }
    public void setJobStartDate(Date jobStartDate) { this.jobStartDate = jobStartDate; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getRole() { return role; }
    public void setRole(String role) {
        if (role != null && !role.startsWith("ROLE_")) {
            this.role = "ROLE_" + role.toUpperCase();
        } else if (role != null) {
            this.role = role.toUpperCase();
        } else {
             this.role = "ROLE_USER"; // Default if null is passed
        }
    }

    @Override
    public String toString() {
        return "Employee{" +
                "employeeId='" + employeeId + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}
