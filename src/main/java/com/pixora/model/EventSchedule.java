package com.pixora.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class EventSchedule {
    private int scheduleId;
    private int bookingId;
    private String bookingRef;
    private LocalDate eventDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String venue;
    private String timeline;
    private String status;
    private int coordinatorUserId;
    private int assignedPhotographerUserId;
    private String photographerName;
    private LocalDateTime updatedAt;

    public EventSchedule() {}

    public int getScheduleId() { return scheduleId; }
    public void setScheduleId(int scheduleId) { this.scheduleId = scheduleId; }
    public int getBookingId() { return bookingId; }
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }
    public String getBookingRef() { return bookingRef; }
    public void setBookingRef(String bookingRef) { this.bookingRef = bookingRef; }
    public LocalDate getEventDate() { return eventDate; }
    public void setEventDate(LocalDate eventDate) { this.eventDate = eventDate; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }
    public String getTimeline() { return timeline; }
    public void setTimeline(String timeline) { this.timeline = timeline; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getCoordinatorUserId() { return coordinatorUserId; }
    public void setCoordinatorUserId(int coordinatorUserId) { this.coordinatorUserId = coordinatorUserId; }
    public int getAssignedPhotographerUserId() { return assignedPhotographerUserId; }
    public void setAssignedPhotographerUserId(int assignedPhotographerUserId) { this.assignedPhotographerUserId = assignedPhotographerUserId; }
    public String getPhotographerName() { return photographerName; }
    public void setPhotographerName(String photographerName) { this.photographerName = photographerName; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}