package com.pixora.model;

import java.time.LocalDateTime;

public class Complaint {
    private int complaintId;
    private String complaintRef;
    private int bookingId;
    private int customerUserId;
    private Integer assignedToUserId;
    private String category;
    private String description;
    private String evidencePath;
    private String priority;
    private String status;
    private String resolution;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Complaint() {}

    public int getComplaintId() { return complaintId; }
    public void setComplaintId(int complaintId) { this.complaintId = complaintId; }
    public String getComplaintRef() { return complaintRef; }
    public void setComplaintRef(String complaintRef) { this.complaintRef = complaintRef; }
    public int getBookingId() { return bookingId; }
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }
    public int getCustomerUserId() { return customerUserId; }
    public void setCustomerUserId(int customerUserId) { this.customerUserId = customerUserId; }
    public Integer getAssignedToUserId() { return assignedToUserId; }
    public void setAssignedToUserId(Integer assignedToUserId) { this.assignedToUserId = assignedToUserId; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getEvidencePath() { return evidencePath; }
    public void setEvidencePath(String evidencePath) { this.evidencePath = evidencePath; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getResolution() { return resolution; }
    public void setResolution(String resolution) { this.resolution = resolution; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}