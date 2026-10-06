package com.pixora.dao;

import com.pixora.model.Booking;
import com.pixora.model.Payment;
import com.pixora.model.Refund;
import com.pixora.service.SimulatedPaymentGateway;
import com.pixora.util.DBConnection;
import com.pixora.util.ReferenceGenerator;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PaymentDAO {
    private final BookingDAO bookingDAO=new BookingDAO();

    public Payment pay(int bookingId,int customerUserId,String method,String simulationMode)throws SQLException{
        Booking b=bookingDAO.findById(bookingId);
        if(b==null||b.getCustomerUserId()!=customerUserId||!List.of("CONFIRMED","COMPLETED").contains(b.getStatus()))throw new SQLException("Only your confirmed or completed booking can be paid.");
        if(hasSuccessfulPayment(bookingId))throw new SQLException("This booking is already paid.");
        SimulatedPaymentGateway.Result result=new SimulatedPaymentGateway().authorize(simulationMode);
        String status=result==SimulatedPaymentGateway.Result.SUCCESS?"PAID":"FAILED";
        String message=result.name();
        String ref=ReferenceGenerator.payment();
        try(Connection c=DBConnection.getConnection()){
            c.setAutoCommit(false);
            int id;
            try(PreparedStatement ps=c.prepareStatement("INSERT INTO payments(payment_ref,booking_id,customer_user_id,amount,method,status,gateway_message) VALUES(?,?,?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS)){
                ps.setString(1,ref);ps.setInt(2,bookingId);ps.setInt(3,customerUserId);ps.setBigDecimal(4,b.getTotalAmount());ps.setString(5,method);ps.setString(6,status);ps.setString(7,message);ps.executeUpdate();
                try(ResultSet keys=ps.getGeneratedKeys()){if(!keys.next())throw new SQLException("Payment record failed.");id=keys.getInt(1);}
            }
            if("PAID".equals(status)){
                try(PreparedStatement ps=c.prepareStatement("INSERT INTO invoices(invoice_ref,booking_id,payment_id,total_amount,status) VALUES(?,?,?,?, 'ISSUED')")){
                    ps.setString(1,ReferenceGenerator.invoice());ps.setInt(2,bookingId);ps.setInt(3,id);ps.setBigDecimal(4,b.getTotalAmount());ps.executeUpdate();
                }
            }
            c.commit();return findById(id);
        }
    }

    public boolean hasSuccessfulPayment(int bookingId)throws SQLException{
        try(Connection c=DBConnection.getConnection();PreparedStatement ps=c.prepareStatement("SELECT COUNT(*) FROM payments WHERE booking_id=? AND status='PAID'")){
            ps.setInt(1,bookingId);try(ResultSet rs=ps.executeQuery()){return rs.next()&&rs.getInt(1)>0;}
        }
    }

    public Payment findById(int id)throws SQLException{
        try(Connection c=DBConnection.getConnection();PreparedStatement ps=c.prepareStatement("SELECT * FROM payments WHERE payment_id=?")){ps.setInt(1,id);try(ResultSet rs=ps.executeQuery()){return rs.next()?mapPayment(rs):null;}}
    }

    public Payment findPaidByBooking(int bookingId)throws SQLException{
        try(Connection c=DBConnection.getConnection();PreparedStatement ps=c.prepareStatement("SELECT TOP 1 * FROM payments WHERE booking_id=? AND status='PAID' ORDER BY created_at DESC")){ps.setInt(1,bookingId);try(ResultSet rs=ps.executeQuery()){return rs.next()?mapPayment(rs):null;}}
    }

    public List<Payment> findForUser(int userId,String role)throws SQLException{
        String where="CUSTOMER".equals(role)?"customer_user_id=?":"1=?";
        List<Payment> list=new ArrayList<>();
        try(Connection c=DBConnection.getConnection();PreparedStatement ps=c.prepareStatement("SELECT * FROM payments WHERE "+where+" ORDER BY created_at DESC")){
            ps.setInt(1,"CUSTOMER".equals(role)?userId:1);try(ResultSet rs=ps.executeQuery()){while(rs.next())list.add(mapPayment(rs));}
        }return list;
    }

    public String invoiceRefForPayment(int paymentId)throws SQLException{
        try(Connection c=DBConnection.getConnection();PreparedStatement ps=c.prepareStatement("SELECT invoice_ref FROM invoices WHERE payment_id=?")){ps.setInt(1,paymentId);try(ResultSet rs=ps.executeQuery()){return rs.next()?rs.getString(1):null;}}
    }

    public int requestRefund(int paymentId,int customerUserId,BigDecimal amount,String reason)throws SQLException{
        Payment p=findById(paymentId);if(p==null||p.getCustomerUserId()!=customerUserId||!"PAID".equals(p.getStatus()))throw new SQLException("Only your paid transaction can be refunded.");
        Booking b=bookingDAO.findById(p.getBookingId());if(b==null||!"CANCELLED".equals(b.getStatus()))throw new SQLException("Demo policy: refund requests are accepted only after booking cancellation.");
        if(amount==null||amount.signum()<=0||amount.compareTo(p.getAmount())>0)throw new SQLException("Refund amount is invalid.");
        try(Connection checkConnection=DBConnection.getConnection();PreparedStatement check=checkConnection.prepareStatement("SELECT COALESCE(SUM(amount),0) FROM refunds WHERE payment_id=? AND status IN ('PENDING','REFUNDED')")){
            check.setInt(1,paymentId);try(ResultSet rs=check.executeQuery()){if(rs.next()&&rs.getBigDecimal(1).add(amount).compareTo(p.getAmount())>0)throw new SQLException("Refund requests exceed the paid amount.");}
        }
        try(Connection c=DBConnection.getConnection();PreparedStatement ps=c.prepareStatement("INSERT INTO refunds(refund_ref,payment_id,amount,reason,status) VALUES(?,?,?,?, 'PENDING')",Statement.RETURN_GENERATED_KEYS)){
            ps.setString(1,ReferenceGenerator.refund());ps.setInt(2,paymentId);ps.setBigDecimal(3,amount);ps.setString(4,reason);ps.executeUpdate();try(ResultSet k=ps.getGeneratedKeys()){if(k.next())return k.getInt(1);}
        }throw new SQLException("Refund request failed.");
    }

    public void processRefund(int refundId,int staffUserId,boolean approve,String simulationMode,String note)throws SQLException{
        Refund r=findRefund(refundId);if(r==null||!"PENDING".equals(r.getStatus()))throw new SQLException("Pending refund not found.");
        String status;
        if(!approve)status="REJECTED";
        else{
            SimulatedPaymentGateway.Result result=new SimulatedPaymentGateway().refund(simulationMode);
            status=result==SimulatedPaymentGateway.Result.SUCCESS?"REFUNDED":"FAILED";
        }
        try(Connection c=DBConnection.getConnection()){
            c.setAutoCommit(false);
            try(PreparedStatement ps=c.prepareStatement("UPDATE refunds SET status=?,processed_by_user_id=?,processed_at=SYSDATETIME(),staff_note=? WHERE refund_id=? AND status='PENDING'")){
                ps.setString(1,status);ps.setInt(2,staffUserId);ps.setString(3,note);ps.setInt(4,refundId);ps.executeUpdate();
            }
            if("REFUNDED".equals(status)){
                try(PreparedStatement ps=c.prepareStatement("UPDATE payments SET status='REFUNDED' WHERE payment_id=?")){ps.setInt(1,r.getPaymentId());ps.executeUpdate();}
            }
            c.commit();
        }
    }

    public Refund findRefund(int id)throws SQLException{
        try(Connection c=DBConnection.getConnection();PreparedStatement ps=c.prepareStatement("SELECT * FROM refunds WHERE refund_id=?")){ps.setInt(1,id);try(ResultSet rs=ps.executeQuery()){return rs.next()?mapRefund(rs):null;}}
    }

    public List<Refund> refundsForUser(int userId,String role)throws SQLException{
        String sql="SELECT r.* FROM refunds r JOIN payments p ON p.payment_id=r.payment_id WHERE "+("CUSTOMER".equals(role)?"p.customer_user_id=?":"1=?")+" ORDER BY r.created_at DESC";
        List<Refund> list=new ArrayList<>();
        try(Connection c=DBConnection.getConnection();PreparedStatement ps=c.prepareStatement(sql)){ps.setInt(1,"CUSTOMER".equals(role)?userId:1);try(ResultSet rs=ps.executeQuery()){while(rs.next())list.add(mapRefund(rs));}}return list;
    }

    private Payment mapPayment(ResultSet rs)throws SQLException{
        Payment p=new Payment();p.setPaymentId(rs.getInt("payment_id"));p.setPaymentRef(rs.getString("payment_ref"));p.setBookingId(rs.getInt("booking_id"));p.setCustomerUserId(rs.getInt("customer_user_id"));p.setAmount(rs.getBigDecimal("amount"));p.setMethod(rs.getString("method"));p.setStatus(rs.getString("status"));p.setGatewayMessage(rs.getString("gateway_message"));Timestamp t=rs.getTimestamp("created_at");if(t!=null)p.setCreatedAt(t.toLocalDateTime());return p;
    }
    private Refund mapRefund(ResultSet rs)throws SQLException{
        Refund r=new Refund();r.setRefundId(rs.getInt("refund_id"));r.setRefundRef(rs.getString("refund_ref"));r.setPaymentId(rs.getInt("payment_id"));r.setAmount(rs.getBigDecimal("amount"));r.setReason(rs.getString("reason"));r.setStatus(rs.getString("status"));int v=rs.getInt("processed_by_user_id");if(!rs.wasNull())r.setProcessedByUserId(v);Timestamp t=rs.getTimestamp("created_at");if(t!=null)r.setCreatedAt(t.toLocalDateTime());return r;
    }
}
