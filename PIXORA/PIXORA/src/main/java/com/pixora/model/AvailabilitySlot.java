package com.pixora.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class AvailabilitySlot {
    private int availabilityId;
    private int photographerUserId;
    private LocalDate availableDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean active;

    public AvailabilitySlot() {}

    public int getAvailabilityId() { return availabilityId; }
    public void setAvailabilityId(int availabilityId) { this.availabilityId = availabilityId; }
    public int getPhotographerUserId() { return photographerUserId; }
    public void setPhotographerUserId(int photographerUserId) { this.photographerUserId = photographerUserId; }
    public LocalDate getAvailableDate() { return availableDate; }
    public void setAvailableDate(LocalDate availableDate) { this.availableDate = availableDate; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    public boolean getActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public boolean isActive() { return active; }
}
