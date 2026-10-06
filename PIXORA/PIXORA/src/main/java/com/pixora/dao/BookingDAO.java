package com.pixora.dao;

import com.pixora.model.Booking;
import com.pixora.util.DBConnection;
import com.pixora.util.ReferenceGenerator;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class BookingDAO {
    private final PhotographerDAO photographerDAO = new PhotographerDAO();

    public int create(int customerUserId, int photographerUserId, int packageId, String eventType,
                      LocalDate eventDate, LocalTime start, LocalTime end, String venue, String notes) throws SQLException {
        if (!photographerDAO.isAvailable(photographerUserId, eventDate, start, end)) {
            throw new SQLException("The selected photographer is not available for the requested period.");
        }
        if (hasConfirmedConflict(photographerUserId, eventDate, start, end, 0)) {
            throw new SQLException("The photographer already has a confirmed booking during that period.");
        }
        String sql = "INSERT INTO bookings(booking_ref,customer_user_id,photographer_user_id,package_id,event_type,event_date,start_time,end_time,venue,notes,total_amount,status) " +
                "SELECT ?,?,?,?,?,?,?,?,?,?,p.price,'PENDING' FROM photography_packages p " +
                "JOIN photographer_profiles pp ON pp.user_id=p.photographer_user_id " +
                "JOIN users u ON u.user_id=p.photographer_user_id " +
                "WHERE p.package_id=? AND p.photographer_user_id=? AND p.active=1 AND pp.approval_status='APPROVED' AND u.status='ACTIVE'";
        String ref = ReferenceGenerator.booking();
        try (Connection c=DBConnection.getConnection(); PreparedStatement ps=c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, ref); ps.setInt(2,customerUserId); ps.setInt(3,photographerUserId); ps.setInt(4,packageId);
            ps.setString(5,eventType); ps.setDate(6,Date.valueOf(eventDate)); ps.setTime(7,Time.valueOf(start)); ps.setTime(8,Time.valueOf(end));
            ps.setString(9,venue); ps.setString(10,notes); ps.setInt(11,packageId); ps.setInt(12,photographerUserId);
            int changed = ps.executeUpdate();
            if (changed == 0) throw new SQLException("The package or photographer is not eligible for booking.");
            try (ResultSet keys=ps.getGeneratedKeys()) {
                if(keys.next()) {
                    int id=keys.getInt(1);
                    addHistory(c,id,null,"PENDING",customerUserId,"Booking request created");
                    return id;
                }
            }
        }
        throw new SQLException("Booking could not be created.");
    }

    public Booking findById(int id) throws SQLException {
        String sql = baseSelect() + " WHERE b.booking_id=?";
        try(Connection c=DBConnection.getConnection(); PreparedStatement ps=c.prepareStatement(sql)){
            ps.setInt(1,id);
            try(ResultSet rs=ps.executeQuery()){ return rs.next()?map(rs):null; }
        }
    }

    public List<Booking> findForUser(int userId, String roleCode) throws SQLException {
        String where;
        switch(roleCode){
            case "CUSTOMER": where="b.customer_user_id=?"; break;
            case "PHOTOGRAPHER": where="b.photographer_user_id=?"; break;
            default: where="1=?"; break;
        }
        String sql=baseSelect()+" WHERE "+where+" ORDER BY b.event_date DESC,b.start_time DESC";
        List<Booking> list=new ArrayList<>();
        try(Connection c=DBConnection.getConnection(); PreparedStatement ps=c.prepareStatement(sql)){
            ps.setInt(1, roleCode.equals("CUSTOMER")||roleCode.equals("PHOTOGRAPHER")?userId:1);
            try(ResultSet rs=ps.executeQuery()){ while(rs.next())list.add(map(rs)); }
        }
        return list;
    }

    public List<Booking> confirmedWithoutSchedule() throws SQLException {
        String sql=baseSelect()+" WHERE b.status='CONFIRMED' AND NOT EXISTS(SELECT 1 FROM event_schedules es WHERE es.booking_id=b.booking_id AND es.status<>'ARCHIVED') ORDER BY b.event_date,b.start_time";
        List<Booking> list=new ArrayList<>();
        try(Connection c=DBConnection.getConnection(); PreparedStatement ps=c.prepareStatement(sql); ResultSet rs=ps.executeQuery()){
            while(rs.next()) list.add(map(rs));
        }
        return list;
    }

    public void updatePending(int bookingId, int customerUserId, String eventType, LocalDate eventDate,
                              LocalTime start, LocalTime end, String venue, String notes) throws SQLException {
        Booking b=findById(bookingId);
        if(b==null||b.getCustomerUserId()!=customerUserId||!"PENDING".equals(b.getStatus())) throw new SQLException("Only your pending booking can be edited.");
        if(!photographerDAO.isAvailable(b.getPhotographerUserId(),eventDate,start,end)) throw new SQLException("Photographer is not available for the changed period.");
        String sql="UPDATE bookings SET event_type=?,event_date=?,start_time=?,end_time=?,venue=?,notes=?,updated_at=SYSDATETIME() WHERE booking_id=? AND customer_user_id=? AND status='PENDING'";
        try(Connection c=DBConnection.getConnection(); PreparedStatement ps=c.prepareStatement(sql)){
            ps.setString(1,eventType);ps.setDate(2,Date.valueOf(eventDate));ps.setTime(3,Time.valueOf(start));ps.setTime(4,Time.valueOf(end));
            ps.setString(5,venue);ps.setString(6,notes);ps.setInt(7,bookingId);ps.setInt(8,customerUserId);ps.executeUpdate();
        }
    }

    public void photographerDecision(int bookingId,int photographerUserId,boolean accept,String reason) throws SQLException {
        Booking b=findById(bookingId);
        if(b==null||b.getPhotographerUserId()!=photographerUserId||!"PENDING".equals(b.getStatus())) throw new SQLException("This booking cannot be decided.");
        String newStatus=accept?"CONFIRMED":"REJECTED";
        if(accept){
            if(!photographerDAO.isAvailable(photographerUserId,b.getEventDate(),b.getStartTime(),b.getEndTime())) throw new SQLException("Your availability no longer covers this event.");
            if(hasConfirmedConflict(photographerUserId,b.getEventDate(),b.getStartTime(),b.getEndTime(),bookingId)) throw new SQLException("This booking conflicts with another confirmed booking.");
        }
        try(Connection c=DBConnection.getConnection()){
            c.setAutoCommit(false);
            try(PreparedStatement ps=c.prepareStatement("UPDATE bookings SET status=?,decision_reason=?,updated_at=SYSDATETIME() WHERE booking_id=? AND photographer_user_id=? AND status='PENDING'")){
                ps.setString(1,newStatus);ps.setString(2,reason);ps.setInt(3,bookingId);ps.setInt(4,photographerUserId);
                if(ps.executeUpdate()!=1) throw new SQLException("Booking status changed before your decision.");
            }
            addHistory(c,bookingId,"PENDING",newStatus,photographerUserId,reason);
            c.commit();
        }
    }

    public void cancel(int bookingId,int customerUserId,String reason) throws SQLException {
        if(reason==null||reason.trim().isEmpty()) throw new SQLException("Cancellation reason is required.");
        Booking b=findById(bookingId);
        if(b==null||b.getCustomerUserId()!=customerUserId) throw new SQLException("Booking not found.");
        if(!List.of("PENDING","CONFIRMED").contains(b.getStatus())) throw new SQLException("This booking can no longer be cancelled.");
        try(Connection c=DBConnection.getConnection()){
            c.setAutoCommit(false);
            try(PreparedStatement ps=c.prepareStatement("UPDATE bookings SET status='CANCELLED',cancellation_reason=?,updated_at=SYSDATETIME() WHERE booking_id=? AND customer_user_id=?")){
                ps.setString(1,reason);ps.setInt(2,bookingId);ps.setInt(3,customerUserId);ps.executeUpdate();
            }
            addHistory(c,bookingId,b.getStatus(),"CANCELLED",customerUserId,reason);
            try(PreparedStatement ps=c.prepareStatement("UPDATE event_schedules SET status='CANCELLED',updated_at=SYSDATETIME() WHERE booking_id=? AND status<>'ARCHIVED'")){
                ps.setInt(1,bookingId);ps.executeUpdate();
            }
            c.commit();
        }
    }

    public void markCompleted(int bookingId,int staffUserId) throws SQLException {
        Booking b=findById(bookingId);
        if(b==null||!"CONFIRMED".equals(b.getStatus())) throw new SQLException("Only a confirmed booking can be completed.");
        try(Connection c=DBConnection.getConnection()){
            c.setAutoCommit(false);
            try(PreparedStatement ps=c.prepareStatement("UPDATE bookings SET status='COMPLETED',updated_at=SYSDATETIME() WHERE booking_id=? AND status='CONFIRMED'")){
                ps.setInt(1,bookingId);ps.executeUpdate();
            }
            addHistory(c,bookingId,"CONFIRMED","COMPLETED",staffUserId,"Event completed");
            c.commit();
        }
    }

    public boolean hasConfirmedConflict(int photographerUserId,LocalDate date,LocalTime start,LocalTime end,int excludeBookingId) throws SQLException {
        String sql="SELECT COUNT(*) FROM bookings WHERE photographer_user_id=? AND event_date=? AND status='CONFIRMED' AND booking_id<>? AND start_time<? AND end_time>?";
        try(Connection c=DBConnection.getConnection();PreparedStatement ps=c.prepareStatement(sql)){
            ps.setInt(1,photographerUserId);ps.setDate(2,Date.valueOf(date));ps.setInt(3,excludeBookingId);ps.setTime(4,Time.valueOf(end));ps.setTime(5,Time.valueOf(start));
            try(ResultSet rs=ps.executeQuery()){ return rs.next()&&rs.getInt(1)>0; }
        }
    }

    public List<String[]> history(int bookingId) throws SQLException {
        List<String[]> list=new ArrayList<>();
        String sql="SELECT old_status,new_status,reason,changed_at FROM booking_status_history WHERE booking_id=? ORDER BY changed_at";
        try(Connection c=DBConnection.getConnection();PreparedStatement ps=c.prepareStatement(sql)){ps.setInt(1,bookingId);try(ResultSet rs=ps.executeQuery()){
            while(rs.next()) list.add(new String[]{rs.getString(1),rs.getString(2),rs.getString(3),String.valueOf(rs.getTimestamp(4))});
        }}
        return list;
    }

    private void addHistory(Connection c,int bookingId,String oldStatus,String newStatus,int changedBy,String reason)throws SQLException{
        try(PreparedStatement ps=c.prepareStatement("INSERT INTO booking_status_history(booking_id,old_status,new_status,changed_by_user_id,reason) VALUES(?,?,?,?,?)")){
            ps.setInt(1,bookingId);ps.setString(2,oldStatus);ps.setString(3,newStatus);ps.setInt(4,changedBy);ps.setString(5,reason);ps.executeUpdate();
        }
    }

    public List<Booking> confirmedBookingsForDate(
            int photographerUserId,
            LocalDate date) throws SQLException {

        String sql = baseSelect() +
                " WHERE b.photographer_user_id=? " +
                "AND b.event_date=? " +
                "AND b.status='CONFIRMED' " +
                "ORDER BY b.start_time";

        List<Booking> list = new ArrayList<>();

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, photographerUserId);
            ps.setDate(2, Date.valueOf(date));

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }

        return list;
    }

    private String baseSelect(){
        return "SELECT b.*,cu.full_name customer_name,pu.full_name photographer_name,p.name package_name FROM bookings b "+
               "JOIN users cu ON cu.user_id=b.customer_user_id JOIN users pu ON pu.user_id=b.photographer_user_id "+
               "JOIN photography_packages p ON p.package_id=b.package_id";
    }

    private Booking map(ResultSet rs)throws SQLException{
        Booking b=new Booking();b.setBookingId(rs.getInt("booking_id"));b.setBookingRef(rs.getString("booking_ref"));
        b.setCustomerUserId(rs.getInt("customer_user_id"));b.setPhotographerUserId(rs.getInt("photographer_user_id"));b.setPackageId(rs.getInt("package_id"));
        b.setCustomerName(rs.getString("customer_name"));b.setPhotographerName(rs.getString("photographer_name"));b.setPackageName(rs.getString("package_name"));
        b.setEventType(rs.getString("event_type"));b.setEventDate(rs.getDate("event_date").toLocalDate());b.setStartTime(rs.getTime("start_time").toLocalTime());b.setEndTime(rs.getTime("end_time").toLocalTime());
        b.setVenue(rs.getString("venue"));b.setNotes(rs.getString("notes"));b.setTotalAmount(rs.getBigDecimal("total_amount"));b.setStatus(rs.getString("status"));b.setCancellationReason(rs.getString("cancellation_reason"));
        Timestamp c=rs.getTimestamp("created_at"),u=rs.getTimestamp("updated_at");if(c!=null)b.setCreatedAt(c.toLocalDateTime());if(u!=null)b.setUpdatedAt(u.toLocalDateTime());return b;
    }
}
