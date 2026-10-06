package com.pixora.model;

import java.time.LocalDateTime;

public class PortfolioItem {
    private int portfolioId;
    private int photographerUserId;
    private String title;
    private String description;
    private String imagePath;
    private LocalDateTime createdAt;

    public PortfolioItem() {}

    public int getPortfolioId() { return portfolioId; }
    public void setPortfolioId(int portfolioId) { this.portfolioId = portfolioId; }
    public int getPhotographerUserId() { return photographerUserId; }
    public void setPhotographerUserId(int photographerUserId) { this.photographerUserId = photographerUserId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}