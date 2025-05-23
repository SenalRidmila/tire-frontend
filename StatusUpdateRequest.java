package com.example.demo.dto;

public class StatusUpdateRequest {
    private String status; // "Approved", "Rejected"
    private String approvedBy; // Employee ID of the user updating the status
    private String comments; // Optional comments for approval/rejection

    // Getters and Setters
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }
}
