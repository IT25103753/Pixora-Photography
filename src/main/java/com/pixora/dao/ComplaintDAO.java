package com.pixora.dao;

import com.pixora.model.*;
import com.pixora.util.DBConnection;
import com.pixora.util.ReferenceGenerator;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ComplaintDAO {
    private final BookingDAO bookingDAO=new BookingDAO();

    public int create(int bookingId,int customerUserId,String category,String description,String evidencePath)throws SQLException{
        Booking b=bookingDAO.findById(bookingId);
        if(b==null||b.getCustomerUserId()!=customerUserId||"PENDING".equals(b.getStatus()))throw new SQLException("A valid non-pending booking linked to you is required.");
        String sql="INSERT INTO complaints(complaint_ref,booking_id,customer_user_id,category,description,evidence_path,priority,status) VALUES(?,?,?,?,?,?,'MEDIUM','SUBMITTED')";
        try(Connection c=DBConnection.getConnection();PreparedStatement ps=c.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)){
            ps.setString(1,ReferenceGenerator.complaint());ps.setInt(2,bookingId);ps.setInt(3,customerUserId);ps.setString(4,category);ps.setString(5,description);ps.setString(6,evidencePath);ps.executeUpdate();
            try(ResultSet k=ps.getGeneratedKeys()){if(k.next())return k.getInt(1);}
        }throw new SQLException("Complaint could not be submitted.");
    }

    public Complaint findById(int id)throws SQLException{
        try(Connection c=DBConnection.getConnection();PreparedStatement ps=c.prepareStatement("SELECT * FROM complaints WHERE complaint_id=?")){ps.setInt(1,id);try(ResultSet rs=ps.executeQuery()){return rs.next()?map(rs):null;}}
    }

    public List<Complaint> findForUser(int userId,String role)throws SQLException{
        String sql;int bind;
        if("CUSTOMER".equals(role)){sql="SELECT c.* FROM complaints c WHERE c.customer_user_id=? ORDER BY c.created_at DESC";bind=userId;}
        else if("PHOTOGRAPHER".equals(role)){sql="SELECT c.* FROM complaints c JOIN bookings b ON b.booking_id=c.booking_id WHERE b.photographer_user_id=? ORDER BY c.created_at DESC";bind=userId;}
        else{sql="SELECT c.* FROM complaints c WHERE 1=? ORDER BY c.created_at DESC";bind=1;}
        List<Complaint> list=new ArrayList<>();
        try(Connection c=DBConnection.getConnection();PreparedStatement ps=c.prepareStatement(sql)){ps.setInt(1,bind);try(ResultSet rs=ps.executeQuery()){while(rs.next())list.add(map(rs));}}
        return list;
    }

    public void updateWorkflow(int complaintId,Integer assignedTo,String priority,String status,String resolution,int actorUserId,String actionText)throws SQLException{
        if(!List.of("SUBMITTED","UNDER_REVIEW","IN_PROGRESS","RESOLVED","CLOSED").contains(status))throw new SQLException("Invalid complaint status.");
        if(!List.of("LOW","MEDIUM","HIGH","URGENT").contains(priority))throw new SQLException("Invalid priority.");
        try(Connection c=DBConnection.getConnection()){
            c.setAutoCommit(false);
            try(PreparedStatement ps=c.prepareStatement("UPDATE complaints SET assigned_to_user_id=?,priority=?,status=?,resolution=?,updated_at=SYSDATETIME() WHERE complaint_id=?")){
                if(assignedTo==null)ps.setNull(1,Types.INTEGER);else ps.setInt(1,assignedTo);ps.setString(2,priority);ps.setString(3,status);ps.setString(4,resolution);ps.setInt(5,complaintId);ps.executeUpdate();
            }
            try(PreparedStatement ps=c.prepareStatement("INSERT INTO complaint_actions(complaint_id,actor_user_id,action_text) VALUES(?,?,?)")){
                ps.setInt(1,complaintId);ps.setInt(2,actorUserId);ps.setString(3,actionText);ps.executeUpdate();
            }c.commit();
        }
    }

    public List<ComplaintAction> actions(int complaintId)throws SQLException{
        String sql="SELECT a.*,u.full_name actor_name FROM complaint_actions a JOIN users u ON u.user_id=a.actor_user_id WHERE a.complaint_id=? ORDER BY a.created_at";
        List<ComplaintAction> list=new ArrayList<>();
        try(Connection c=DBConnection.getConnection();PreparedStatement ps=c.prepareStatement(sql)){ps.setInt(1,complaintId);try(ResultSet rs=ps.executeQuery()){while(rs.next()){
            ComplaintAction a=new ComplaintAction();a.setActionId(rs.getInt("action_id"));a.setComplaintId(rs.getInt("complaint_id"));a.setActorUserId(rs.getInt("actor_user_id"));a.setActorName(rs.getString("actor_name"));a.setActionText(rs.getString("action_text"));Timestamp t=rs.getTimestamp("created_at");if(t!=null)a.setCreatedAt(t.toLocalDateTime());list.add(a);
        }}}return list;
    }

    public void createReview(int bookingId,int customerUserId,int rating,String comment)throws SQLException{
        Booking b=bookingDAO.findById(bookingId);if(b==null||b.getCustomerUserId()!=customerUserId||!"COMPLETED".equals(b.getStatus()))throw new SQLException("Only a completed booking can be reviewed.");
        if(rating<1||rating>5)throw new SQLException("Rating must be from 1 to 5.");
        try(Connection c=DBConnection.getConnection()){
            c.setAutoCommit(false);
            try(PreparedStatement ps=c.prepareStatement("INSERT INTO reviews(booking_id,customer_user_id,photographer_user_id,rating,comment,visible) VALUES(?,?,?,?,?,1)")){
                ps.setInt(1,bookingId);ps.setInt(2,customerUserId);ps.setInt(3,b.getPhotographerUserId());ps.setInt(4,rating);ps.setString(5,comment);ps.executeUpdate();
            }
            recalcRating(c,b.getPhotographerUserId());c.commit();
        }
    }

    public List<Review> reviewsForPhotographer(int photographerUserId,boolean onlyVisible)throws SQLException{
        String sql="SELECT * FROM reviews WHERE photographer_user_id=? "+(onlyVisible?"AND visible=1 ":"")+"ORDER BY created_at DESC";
        List<Review> list=new ArrayList<>();
        try(Connection c=DBConnection.getConnection();PreparedStatement ps=c.prepareStatement(sql)){ps.setInt(1,photographerUserId);try(ResultSet rs=ps.executeQuery()){while(rs.next()){
            Review r=new Review();r.setReviewId(rs.getInt("review_id"));r.setBookingId(rs.getInt("booking_id"));r.setCustomerUserId(rs.getInt("customer_user_id"));r.setPhotographerUserId(rs.getInt("photographer_user_id"));r.setRating(rs.getInt("rating"));r.setComment(rs.getString("comment"));r.setVisible(rs.getBoolean("visible"));Timestamp t=rs.getTimestamp("created_at");if(t!=null)r.setCreatedAt(t.toLocalDateTime());list.add(r);
        }}}return list;
    }

    public List<Review> allReviews()throws SQLException{
        String sql="SELECT * FROM reviews ORDER BY created_at DESC";List<Review> list=new ArrayList<>();
        try(Connection c=DBConnection.getConnection();PreparedStatement ps=c.prepareStatement(sql);ResultSet rs=ps.executeQuery()){while(rs.next()){
            Review r=new Review();r.setReviewId(rs.getInt("review_id"));r.setBookingId(rs.getInt("booking_id"));r.setCustomerUserId(rs.getInt("customer_user_id"));r.setPhotographerUserId(rs.getInt("photographer_user_id"));r.setRating(rs.getInt("rating"));r.setComment(rs.getString("comment"));r.setVisible(rs.getBoolean("visible"));Timestamp t=rs.getTimestamp("created_at");if(t!=null)r.setCreatedAt(t.toLocalDateTime());list.add(r);
        }}return list;
    }

    public void moderateReview(int reviewId,boolean visible)throws SQLException{
        try(Connection c=DBConnection.getConnection()){
            c.setAutoCommit(false);int photographerId=0;
            try(PreparedStatement ps=c.prepareStatement("SELECT photographer_user_id FROM reviews WHERE review_id=?")){ps.setInt(1,reviewId);try(ResultSet rs=ps.executeQuery()){if(rs.next())photographerId=rs.getInt(1);}}
            try(PreparedStatement ps=c.prepareStatement("UPDATE reviews SET visible=? WHERE review_id=?")){ps.setBoolean(1,visible);ps.setInt(2,reviewId);ps.executeUpdate();}
            if(photographerId>0)recalcRating(c,photographerId);c.commit();
        }
    }

    private void recalcRating(Connection c,int photographerId)throws SQLException{
        try(PreparedStatement ps=c.prepareStatement("UPDATE photographer_profiles SET average_rating=COALESCE((SELECT CAST(AVG(CAST(rating AS DECIMAL(4,2))) AS DECIMAL(4,2)) FROM reviews WHERE photographer_user_id=? AND visible=1),0) WHERE user_id=?")){
            ps.setInt(1,photographerId);ps.setInt(2,photographerId);ps.executeUpdate();
        }
    }

    private Complaint map(ResultSet rs)throws SQLException{
        Complaint c=new Complaint();c.setComplaintId(rs.getInt("complaint_id"));c.setComplaintRef(rs.getString("complaint_ref"));c.setBookingId(rs.getInt("booking_id"));c.setCustomerUserId(rs.getInt("customer_user_id"));int a=rs.getInt("assigned_to_user_id");if(!rs.wasNull())c.setAssignedToUserId(a);c.setCategory(rs.getString("category"));c.setDescription(rs.getString("description"));c.setEvidencePath(rs.getString("evidence_path"));c.setPriority(rs.getString("priority"));c.setStatus(rs.getString("status"));c.setResolution(rs.getString("resolution"));Timestamp ct=rs.getTimestamp("created_at"),ut=rs.getTimestamp("updated_at");if(ct!=null)c.setCreatedAt(ct.toLocalDateTime());if(ut!=null)c.setUpdatedAt(ut.toLocalDateTime());return c;
    }
}
