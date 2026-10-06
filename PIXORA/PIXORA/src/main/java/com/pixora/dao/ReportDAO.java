package com.pixora.dao;

import com.pixora.util.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class ReportDAO {
    public Map<String,Object> dashboard(String role,int userId)throws SQLException{
        Map<String,Object> m=new LinkedHashMap<>();
        if("CUSTOMER".equals(role)){
            m.put("Bookings",scalar("SELECT COUNT(*) FROM bookings WHERE customer_user_id=?",userId));
            m.put("Upcoming",scalar("SELECT COUNT(*) FROM bookings WHERE customer_user_id=? AND status='CONFIRMED' AND event_date>=CAST(GETDATE() AS date)",userId));
            m.put("Galleries",scalar("SELECT COUNT(*) FROM galleries WHERE customer_user_id=? AND archived=0",userId));
            m.put("Open Complaints",scalar("SELECT COUNT(*) FROM complaints WHERE customer_user_id=? AND status NOT IN('RESOLVED','CLOSED')",userId));
        }else if("PHOTOGRAPHER".equals(role)){
            m.put("Pending Requests",scalar("SELECT COUNT(*) FROM bookings WHERE photographer_user_id=? AND status='PENDING'",userId));
            m.put("Confirmed",scalar("SELECT COUNT(*) FROM bookings WHERE photographer_user_id=? AND status='CONFIRMED'",userId));
            m.put("Assignments",scalar("SELECT COUNT(*) FROM photographer_assignments WHERE photographer_user_id=? AND status='ACTIVE'",userId));
            m.put("Galleries",scalar("SELECT COUNT(*) FROM galleries WHERE photographer_user_id=? AND archived=0",userId));
        }else{
            m.put("Active Users",scalarNoParam("SELECT COUNT(*) FROM users WHERE status='ACTIVE'"));
            m.put("Confirmed Bookings",scalarNoParam("SELECT COUNT(*) FROM bookings WHERE status='CONFIRMED'"));
            m.put("Upcoming Events",scalarNoParam("SELECT COUNT(*) FROM event_schedules WHERE status='ACTIVE' AND event_date>=CAST(GETDATE() AS date)"));
            m.put("Open Complaints",scalarNoParam("SELECT COUNT(*) FROM complaints WHERE status NOT IN('RESOLVED','CLOSED')"));
        }
        return m;
    }

    public Map<String,Integer> bookingsByStatus()throws SQLException{return groupedCount("SELECT status label,COUNT(*) total FROM bookings GROUP BY status ORDER BY status");}
    public Map<String,Integer> bookingsByEventType()throws SQLException{return groupedCount("SELECT event_type label,COUNT(*) total FROM bookings GROUP BY event_type ORDER BY total DESC,event_type");}
    public Map<String,Integer> bookingsByPackage()throws SQLException{return groupedCount("SELECT p.name label,COUNT(*) total FROM bookings b JOIN photography_packages p ON p.package_id=b.package_id GROUP BY p.name ORDER BY total DESC,p.name");}
    public Map<String,Integer> bookingsByPhotographer()throws SQLException{return groupedCount("SELECT u.full_name label,COUNT(*) total FROM bookings b JOIN users u ON u.user_id=b.photographer_user_id GROUP BY u.full_name ORDER BY total DESC,u.full_name");}
    public Map<String,Integer> complaintsByStatus()throws SQLException{return groupedCount("SELECT status label,COUNT(*) total FROM complaints GROUP BY status ORDER BY status");}
    public Map<String,Integer> galleriesByStatus()throws SQLException{return groupedCount("SELECT status label,COUNT(*) total FROM galleries WHERE archived=0 GROUP BY status ORDER BY status");}
    public Map<String,Integer> paymentsByStatus()throws SQLException{return groupedCount("SELECT status label,COUNT(*) total FROM payments GROUP BY status ORDER BY status");}
    public Map<String,Integer> refundsByStatus()throws SQLException{return groupedCount("SELECT status label,COUNT(*) total FROM refunds GROUP BY status ORDER BY status");}
    public Map<String,Integer> photographerRatings()throws SQLException{return groupedCount("SELECT CONCAT(u.full_name,' (',CAST(pp.average_rating AS VARCHAR(10)),')') label,COUNT(r.review_id) total FROM photographer_profiles pp JOIN users u ON u.user_id=pp.user_id LEFT JOIN reviews r ON r.photographer_user_id=pp.user_id AND r.visible=1 GROUP BY u.full_name,pp.average_rating ORDER BY pp.average_rating DESC,u.full_name");}

    public Map<String,BigDecimal> monthlyRevenue()throws SQLException{
        String sql="SELECT CONVERT(char(7),created_at,120) label,COALESCE(SUM(amount),0) total FROM payments WHERE status IN('PAID','REFUNDED') GROUP BY CONVERT(char(7),created_at,120) ORDER BY label DESC";
        Map<String,BigDecimal> result=new LinkedHashMap<>();
        try(Connection c=DBConnection.getConnection();PreparedStatement ps=c.prepareStatement(sql);ResultSet rs=ps.executeQuery()){
            while(rs.next())result.put(rs.getString("label"),rs.getBigDecimal("total"));
        }
        return result;
    }

    public BigDecimal totalRevenue()throws SQLException{return moneyScalar("SELECT COALESCE(SUM(amount),0) FROM payments WHERE status='PAID'");}
    public BigDecimal outstandingBalance()throws SQLException{
        String sql="SELECT COALESCE(SUM(CASE WHEN b.total_amount>COALESCE(p.paid,0) THEN b.total_amount-COALESCE(p.paid,0) ELSE 0 END),0) " +
                "FROM bookings b OUTER APPLY (SELECT SUM(amount) paid FROM payments WHERE booking_id=b.booking_id AND status='PAID') p " +
                "WHERE b.status IN('CONFIRMED','COMPLETED')";
        return moneyScalar(sql);
    }
    public long galleryStorageBytes()throws SQLException{return longScalarNoParam("SELECT COALESCE(SUM(file_size),0) FROM photos WHERE deleted=0");}
    public int upcomingEvents()throws SQLException{return scalarNoParam("SELECT COUNT(*) FROM event_schedules WHERE status='ACTIVE' AND event_date>=CAST(GETDATE() AS date)");}
    public int assignmentChanges()throws SQLException{return scalarNoParam("SELECT COUNT(*) FROM schedule_change_history WHERE action_type IN('UPDATE','ARCHIVE')");}
    public int invoiceCount()throws SQLException{return scalarNoParam("SELECT COUNT(*) FROM invoices WHERE status='ISSUED'");}
    public int openComplaints()throws SQLException{return scalarNoParam("SELECT COUNT(*) FROM complaints WHERE status NOT IN('RESOLVED','CLOSED')");}
    public int averageResolutionHours()throws SQLException{return scalarNoParam("SELECT COALESCE(AVG(CASE WHEN status IN('RESOLVED','CLOSED') THEN DATEDIFF(HOUR,created_at,updated_at) END),0) FROM complaints");}

    private int scalar(String sql,int id)throws SQLException{
        try(Connection c=DBConnection.getConnection();PreparedStatement ps=c.prepareStatement(sql)){ps.setInt(1,id);try(ResultSet rs=ps.executeQuery()){return rs.next()?rs.getInt(1):0;}}
    }
    private int scalarNoParam(String sql)throws SQLException{
        try(Connection c=DBConnection.getConnection();PreparedStatement ps=c.prepareStatement(sql);ResultSet rs=ps.executeQuery()){return rs.next()?rs.getInt(1):0;}
    }
    private long longScalarNoParam(String sql)throws SQLException{
        try(Connection c=DBConnection.getConnection();PreparedStatement ps=c.prepareStatement(sql);ResultSet rs=ps.executeQuery()){return rs.next()?rs.getLong(1):0L;}
    }
    private BigDecimal moneyScalar(String sql)throws SQLException{
        try(Connection c=DBConnection.getConnection();PreparedStatement ps=c.prepareStatement(sql);ResultSet rs=ps.executeQuery()){return rs.next()?rs.getBigDecimal(1):BigDecimal.ZERO;}
    }
    private Map<String,Integer> groupedCount(String sql)throws SQLException{
        Map<String,Integer> m=new LinkedHashMap<>();
        try(Connection c=DBConnection.getConnection();PreparedStatement ps=c.prepareStatement(sql);ResultSet rs=ps.executeQuery()){
            while(rs.next())m.put(rs.getString("label"),rs.getInt("total"));
        }
        return m;
    }
}
