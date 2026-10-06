package com.pixora.model;

import java.time.LocalDateTime;

public class Gallery {
    private int galleryId;
    private int bookingId;
    private int photographerUserId;
    private int customerUserId;
    private String title;
    private String accessToken;
    private String status;
    private boolean archived;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer coverPhotoId;

    public Gallery() {}

    public int getGalleryId() { return galleryId; }
    public void setGalleryId(int galleryId) { this.galleryId = galleryId; }
    public int getBookingId() { return bookingId; }
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }
    public int getPhotographerUserId() { return photographerUserId; }
    public void setPhotographerUserId(int photographerUserId) { this.photographerUserId = photographerUserId; }
    public int getCustomerUserId() { return customerUserId; }
    public void setCustomerUserId(int customerUserId) { this.customerUserId = customerUserId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public boolean getArchived() { return archived; }
    public void setArchived(boolean archived) { this.archived = archived; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public boolean isArchived() { return archived; }
    public Integer getCoverPhotoId() { return coverPhotoId; }
    public void setCoverPhotoId(Integer coverPhotoId) { this.coverPhotoId = coverPhotoId; }
}
