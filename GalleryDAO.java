package com.pixora.dao;

import com.pixora.model.Booking;
import com.pixora.model.Gallery;
import com.pixora.model.GalleryAlbum;
import com.pixora.model.Photo;
import com.pixora.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GalleryDAO {
    private final BookingDAO bookingDAO = new BookingDAO();

    public int create(int bookingId, int photographerUserId, String title) throws SQLException {
        Booking booking = bookingDAO.findById(bookingId);
        if (booking == null || !"COMPLETED".equals(booking.getStatus()) || !isEligibleGalleryPhotographer(booking, photographerUserId)) {
            throw new SQLException("A completed booking assigned to you is required before creating a gallery.");
        }
        String cleanTitle = title == null ? "" : title.trim();
        if (cleanTitle.isEmpty()) {
            throw new SQLException("Gallery title is required.");
        }
        String sql = "INSERT INTO galleries(booking_id,photographer_user_id,customer_user_id,title,access_token,status,archived) " +
                "VALUES(?,?,?,?,?,'UPLOADING',0)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, bookingId);
            ps.setInt(2, photographerUserId);
            ps.setInt(3, booking.getCustomerUserId());
            ps.setString(4, cleanTitle);
            ps.setString(5, UUID.randomUUID().toString());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        throw new SQLException("Gallery could not be created.");
    }

    private boolean isEligibleGalleryPhotographer(Booking booking, int photographerUserId) throws SQLException {
        if (booking.getPhotographerUserId() == photographerUserId) return true;
        String sql = "SELECT COUNT(*) FROM photographer_assignments pa " +
                "JOIN event_schedules es ON es.schedule_id=pa.schedule_id " +
                "WHERE es.booking_id=? AND pa.photographer_user_id=? AND pa.status='ACTIVE' AND es.status='ACTIVE'";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, booking.getBookingId());
            ps.setInt(2, photographerUserId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public Gallery findById(int id) throws SQLException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM galleries WHERE gallery_id=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapGallery(rs) : null;
            }
        }
    }

    public List<Gallery> findForUser(int userId, String role) throws SQLException {
        String where;
        if ("CUSTOMER".equals(role)) where = "customer_user_id=?";
        else if ("PHOTOGRAPHER".equals(role)) where = "photographer_user_id=?";
        else where = "1=?";
        String sql = "SELECT g.*, (SELECT TOP 1 p.photo_id FROM photos p " +
                "WHERE p.gallery_id=g.gallery_id AND p.deleted=0 " +
                "ORDER BY p.uploaded_at, p.photo_id) AS cover_photo_id " +
                "FROM galleries g WHERE " + where + " ORDER BY g.created_at DESC";
        List<Gallery> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, ("CUSTOMER".equals(role) || "PHOTOGRAPHER".equals(role)) ? userId : 1);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Gallery g = mapGallery(rs);
                    int cover = rs.getInt("cover_photo_id");
                    g.setCoverPhotoId(rs.wasNull() ? null : cover);
                    list.add(g);
                }
            }
        }
        return list;
    }

    public List<Booking> completedBookingsWithoutGallery(int photographerUserId) throws SQLException {
        String sql = "SELECT b.*,cu.full_name customer_name,pu.full_name photographer_name,p.name package_name FROM bookings b " +
                "JOIN users cu ON cu.user_id=b.customer_user_id " +
                "JOIN users pu ON pu.user_id=b.photographer_user_id " +
                "JOIN photography_packages p ON p.package_id=b.package_id " +
                "WHERE b.status='COMPLETED' " +
                "AND (b.photographer_user_id=? OR EXISTS (SELECT 1 FROM event_schedules es JOIN photographer_assignments pa ON pa.schedule_id=es.schedule_id " +
                "    WHERE es.booking_id=b.booking_id AND es.status='ACTIVE' AND pa.status='ACTIVE' AND pa.photographer_user_id=?)) " +
                "AND NOT EXISTS(SELECT 1 FROM galleries g WHERE g.booking_id=b.booking_id AND g.archived=0) " +
                "ORDER BY b.event_date DESC";
        List<Booking> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, photographerUserId);
            ps.setInt(2, photographerUserId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapBooking(rs));
            }
        }
        return list;
    }

    public List<GalleryAlbum> albums(int galleryId, int viewerUserId, String role) throws SQLException {
        Gallery gallery = findById(galleryId);
        if (gallery == null || !canAccess(gallery, viewerUserId, role)) throw new SQLException("Gallery access denied.");
        boolean manager = "PHOTOGRAPHER".equals(role) && gallery.getPhotographerUserId() == viewerUserId;
        String sql = "SELECT * FROM gallery_albums WHERE gallery_id=?" + (manager ? "" : " AND active=1") + " ORDER BY created_at,album_id";
        List<GalleryAlbum> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, galleryId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    GalleryAlbum a = new GalleryAlbum();
                    a.setAlbumId(rs.getInt("album_id"));
                    a.setGalleryId(rs.getInt("gallery_id"));
                    a.setName(rs.getString("name"));
                    a.setDescription(rs.getString("description"));
                    a.setActive(rs.getBoolean("active"));
                    Timestamp created = rs.getTimestamp("created_at");
                    if (created != null) a.setCreatedAt(created.toLocalDateTime());
                    list.add(a);
                }
            }
        }
        return list;
    }

    public void createAlbum(int galleryId, int photographerUserId, String name, String description) throws SQLException {
        Gallery gallery = requireOwnedActiveGallery(galleryId, photographerUserId);
        String cleanName = name == null ? "" : name.trim();
        if (cleanName.isEmpty()) throw new SQLException("Album name is required.");
        String sql = "INSERT INTO gallery_albums(gallery_id,name,description,active) VALUES(?,?,?,1)";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, gallery.getGalleryId());
            ps.setString(2, cleanName);
            ps.setString(3, description == null ? null : description.trim());
            ps.executeUpdate();
        }
    }

    public void archiveAlbum(int albumId, int photographerUserId) throws SQLException {
        String sql = "UPDATE ga SET ga.active=0 FROM gallery_albums ga " +
                "JOIN galleries g ON g.gallery_id=ga.gallery_id " +
                "WHERE ga.album_id=? AND g.photographer_user_id=? AND g.archived=0";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, albumId);
            ps.setInt(2, photographerUserId);
            if (ps.executeUpdate() == 0) throw new SQLException("Album could not be archived.");
        }
    }

    public void addPhoto(int galleryId, int photographerUserId, Integer albumId, String caption,
                         String filePath, String originalName, long size) throws SQLException {
        Gallery gallery = requireOwnedActiveGallery(galleryId, photographerUserId);
        if (albumId != null && albumId > 0 && !isActiveAlbumInGallery(albumId, galleryId)) {
            throw new SQLException("Selected album is not available in this gallery.");
        }
        String sql = "INSERT INTO photos(gallery_id,album_id,caption,file_path,original_name,file_size) VALUES(?,?,?,?,?,?)";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, gallery.getGalleryId());
            if (albumId == null || albumId <= 0) ps.setNull(2, Types.INTEGER); else ps.setInt(2, albumId);
            ps.setString(3, caption);
            ps.setString(4, filePath);
            ps.setString(5, originalName);
            ps.setLong(6, size);
            ps.executeUpdate();
        }
    }

    private boolean isActiveAlbumInGallery(int albumId, int galleryId) throws SQLException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM gallery_albums WHERE album_id=? AND gallery_id=? AND active=1")) {
            ps.setInt(1, albumId);
            ps.setInt(2, galleryId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public List<Photo> photos(int galleryId, int viewerUserId, String role) throws SQLException {
        Gallery gallery = findById(galleryId);
        if (gallery == null || !canAccess(gallery, viewerUserId, role)) throw new SQLException("Gallery access denied.");
        String sql = "SELECT p.*,ga.name album_name,CASE WHEN EXISTS(SELECT 1 FROM photo_favorites f WHERE f.photo_id=p.photo_id AND f.customer_user_id=?) THEN 1 ELSE 0 END AS is_favorite " +
                "FROM photos p LEFT JOIN gallery_albums ga ON ga.album_id=p.album_id " +
                "WHERE p.gallery_id=? AND p.deleted=0 ORDER BY COALESCE(ga.name,''),p.uploaded_at";
        List<Photo> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, viewerUserId);
            ps.setInt(2, galleryId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapPhoto(rs));
            }
        }
        return list;
    }

    public Photo findPhoto(int photoId) throws SQLException {
        String sql = "SELECT p.*,ga.name album_name,0 is_favorite FROM photos p LEFT JOIN gallery_albums ga ON ga.album_id=p.album_id " +
                "WHERE p.photo_id=? AND p.deleted=0";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, photoId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapPhoto(rs) : null;
            }
        }
    }

    public String softDeletePhoto(int photoId, int photographerUserId) throws SQLException {
        String path = null;
        String sql = "SELECT p.file_path FROM photos p JOIN galleries g ON g.gallery_id=p.gallery_id WHERE p.photo_id=? AND g.photographer_user_id=?";
        try (Connection c = DBConnection.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, photoId);
                ps.setInt(2, photographerUserId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) path = rs.getString(1);
                }
            }
            try (PreparedStatement ps = c.prepareStatement("UPDATE p SET deleted=1 FROM photos p JOIN galleries g ON g.gallery_id=p.gallery_id WHERE p.photo_id=? AND g.photographer_user_id=?")) {
                ps.setInt(1, photoId);
                ps.setInt(2, photographerUserId);
                ps.executeUpdate();
            }
        }
        return path;
    }

    public void updateStatus(int galleryId, int photographerUserId, String status) throws SQLException {
        if (!List.of("UPLOADING", "PROCESSING", "READY_FOR_VIEWING", "DELIVERED").contains(status)) {
            throw new SQLException("Invalid gallery status.");
        }
        requireOwnedActiveGallery(galleryId, photographerUserId);
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE galleries SET status=?,updated_at=SYSDATETIME() WHERE gallery_id=? AND photographer_user_id=? AND archived=0")) {
            ps.setString(1, status);
            ps.setInt(2, galleryId);
            ps.setInt(3, photographerUserId);
            ps.executeUpdate();
        }
    }

    public void archive(int galleryId, int photographerUserId) throws SQLException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE galleries SET archived=1,updated_at=SYSDATETIME() WHERE gallery_id=? AND photographer_user_id=?")) {
            ps.setInt(1, galleryId);
            ps.setInt(2, photographerUserId);
            if (ps.executeUpdate() == 0) throw new SQLException("Gallery could not be archived.");
        }
    }

    public void toggleFavorite(int photoId, int galleryId, int customerUserId) throws SQLException {
        Gallery gallery = findById(galleryId);
        if (gallery == null || gallery.getCustomerUserId() != customerUserId) throw new SQLException("Gallery access denied.");
        String valid = "SELECT COUNT(*) FROM photos WHERE photo_id=? AND gallery_id=? AND deleted=0";
        try (Connection c = DBConnection.getConnection(); PreparedStatement check = c.prepareStatement(valid)) {
            check.setInt(1, photoId);
            check.setInt(2, galleryId);
            try (ResultSet rs = check.executeQuery()) {
                if (!rs.next() || rs.getInt(1) == 0) throw new SQLException("Photo was not found in this gallery.");
            }
        }
        String sql = "IF EXISTS(SELECT 1 FROM photo_favorites WHERE photo_id=? AND customer_user_id=?) " +
                "DELETE FROM photo_favorites WHERE photo_id=? AND customer_user_id=? " +
                "ELSE INSERT INTO photo_favorites(photo_id,customer_user_id) VALUES(?,?)";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, photoId); ps.setInt(2, customerUserId);
            ps.setInt(3, photoId); ps.setInt(4, customerUserId);
            ps.setInt(5, photoId); ps.setInt(6, customerUserId);
            ps.executeUpdate();
        }
    }

    public boolean canAccess(Gallery gallery, int userId, String role) {
        return "SYSTEM_ADMIN".equals(role) || "OPERATIONS_MANAGER".equals(role) ||
                gallery.getCustomerUserId() == userId || gallery.getPhotographerUserId() == userId;
    }

    private Gallery requireOwnedActiveGallery(int galleryId, int photographerUserId) throws SQLException {
        Gallery gallery = findById(galleryId);
        if (gallery == null || gallery.getPhotographerUserId() != photographerUserId || gallery.isArchived()) {
            throw new SQLException("You cannot manage this gallery.");
        }
        return gallery;
    }

    private Booking mapBooking(ResultSet rs) throws SQLException {
        Booking b = new Booking();
        b.setBookingId(rs.getInt("booking_id"));
        b.setBookingRef(rs.getString("booking_ref"));
        b.setCustomerUserId(rs.getInt("customer_user_id"));
        b.setPhotographerUserId(rs.getInt("photographer_user_id"));
        b.setPackageId(rs.getInt("package_id"));
        b.setCustomerName(rs.getString("customer_name"));
        b.setPhotographerName(rs.getString("photographer_name"));
        b.setPackageName(rs.getString("package_name"));
        b.setEventType(rs.getString("event_type"));
        b.setEventDate(rs.getDate("event_date").toLocalDate());
        b.setStartTime(rs.getTime("start_time").toLocalTime());
        b.setEndTime(rs.getTime("end_time").toLocalTime());
        b.setVenue(rs.getString("venue"));
        b.setTotalAmount(rs.getBigDecimal("total_amount"));
        b.setStatus(rs.getString("status"));
        return b;
    }

    private Photo mapPhoto(ResultSet rs) throws SQLException {
        Photo p = new Photo();
        p.setPhotoId(rs.getInt("photo_id"));
        p.setGalleryId(rs.getInt("gallery_id"));
        int albumId = rs.getInt("album_id");
        p.setAlbumId(rs.wasNull() ? null : albumId);
        p.setAlbumName(rs.getString("album_name"));
        p.setCaption(rs.getString("caption"));
        p.setFilePath(rs.getString("file_path"));
        p.setOriginalName(rs.getString("original_name"));
        p.setFileSize(rs.getLong("file_size"));
        p.setFavorite(rs.getBoolean("is_favorite"));
        Timestamp uploaded = rs.getTimestamp("uploaded_at");
        if (uploaded != null) p.setUploadedAt(uploaded.toLocalDateTime());
        return p;
    }

    private Gallery mapGallery(ResultSet rs) throws SQLException {
        Gallery g = new Gallery();
        g.setGalleryId(rs.getInt("gallery_id"));
        g.setBookingId(rs.getInt("booking_id"));
        g.setPhotographerUserId(rs.getInt("photographer_user_id"));
        g.setCustomerUserId(rs.getInt("customer_user_id"));
        g.setTitle(rs.getString("title"));
        g.setAccessToken(rs.getString("access_token"));
        g.setStatus(rs.getString("status"));
        g.setArchived(rs.getBoolean("archived"));
        Timestamp created = rs.getTimestamp("created_at"), updated = rs.getTimestamp("updated_at");
        if (created != null) g.setCreatedAt(created.toLocalDateTime());
        if (updated != null) g.setUpdatedAt(updated.toLocalDateTime());
        return g;
    }
}
