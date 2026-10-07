package com.pixora.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class Booking {
    private int bookingId;
    private String bookingRef;
    private int customerUserId;
    private int photographerUserId;
    private int packageId;
    private String customerName;
    private String photographerName;
    private String packageName;
    private String eventType;
    private LocalDate eventDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String venue;
    private String notes;
    private BigDecimal totalAmount;
    private String status;
    private String cancellationReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Booking() {}

    public int getBookingId() { return bookingId; }
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }
    public String getBookingRef() { return bookingRef; }
    public void setBookingRef(String bookingRef) { this.bookingRef = bookingRef; }
    public int getCustomerUserId() { return customerUserId; }
    public void setCustomerUserId(int customerUserId) { this.customerUserId = customerUserId; }
    public int getPhotographerUserId() { return photographerUserId; }
    public void setPhotographerUserId(int photographerUserId) { this.photographerUserId = photographerUserId; }
    public int getPackageId() { return packageId; }
    public void setPackageId(int packageId) { this.packageId = packageId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getPhotographerName() { return photographerName; }
    public void setPhotographerName(String photographerName) { this.photographerName = photographerName; }
    public String getPackageName() { return packageName; }
    public void setPackageName(String packageName) { this.packageName = packageName; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public LocalDate getEventDate() { return eventDate; }
    public void setEventDate(LocalDate eventDate) { this.eventDate = eventDate; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCancellationReason() { return cancellationReason; }
    public void setCancellationReason(String cancellationReason) { this.cancellationReason = cancellationReason; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}