package com.pixora.model;

import java.time.LocalDateTime;

public class Photo {
    private int photoId;
    private int galleryId;
    private Integer albumId;
    private String albumName;
    private String caption;
    private String filePath;
    private String originalName;
    private long fileSize;
    private boolean favorite;
    private LocalDateTime uploadedAt;

    public Photo() {}

    public int getPhotoId() { return photoId; }
    public void setPhotoId(int photoId) { this.photoId = photoId; }
    public int getGalleryId() { return galleryId; }
    public void setGalleryId(int galleryId) { this.galleryId = galleryId; }
    public Integer getAlbumId() { return albumId; }
    public void setAlbumId(Integer albumId) { this.albumId = albumId; }
    public String getAlbumName() { return albumName; }
    public void setAlbumName(String albumName) { this.albumName = albumName; }
    public String getCaption() { return caption; }
    public void setCaption(String caption) { this.caption = caption; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public String getOriginalName() { return originalName; }
    public void setOriginalName(String originalName) { this.originalName = originalName; }
    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }
    public boolean getFavorite() { return favorite; }
    public void setFavorite(boolean favorite) { this.favorite = favorite; }
    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
    public boolean isFavorite() { return favorite; }
}
