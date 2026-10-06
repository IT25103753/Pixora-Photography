package com.pixora.model;

import java.math.BigDecimal;

public class PhotographyPackage {
    private int packageId;
    private int photographerUserId;
    private String name;
    private String description;
    private BigDecimal price;
    private int durationHours;
    private boolean active;

    public PhotographyPackage() {}

    public int getPackageId() { return packageId; }
    public void setPackageId(int packageId) { this.packageId = packageId; }
    public int getPhotographerUserId() { return photographerUserId; }
    public void setPhotographerUserId(int photographerUserId) { this.photographerUserId = photographerUserId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public int getDurationHours() { return durationHours; }
    public void setDurationHours(int durationHours) { this.durationHours = durationHours; }
    public boolean getActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public boolean isActive() { return active; }
}
