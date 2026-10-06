package com.pixora.model;

import java.time.LocalDateTime;

public class GalleryAlbum {
    private int albumId;
    private int galleryId;
    private String name;
    private String description;
    private boolean active;
    private LocalDateTime createdAt;

    public GalleryAlbum() {}
    public int getAlbumId(){return albumId;}
    public void setAlbumId(int albumId){this.albumId=albumId;}
    public int getGalleryId(){return galleryId;}
    public void setGalleryId(int galleryId){this.galleryId=galleryId;}
    public String getName(){return name;}
    public void setName(String name){this.name=name;}
    public String getDescription(){return description;}
    public void setDescription(String description){this.description=description;}
    public boolean isActive(){return active;}
    public boolean getActive(){return active;}
    public void setActive(boolean active){this.active=active;}
    public LocalDateTime getCreatedAt(){return createdAt;}
    public void setCreatedAt(LocalDateTime createdAt){this.createdAt=createdAt;}
}
