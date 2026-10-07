package com.pixora.dao;

import com.pixora.model.Booking;
import com.pixora.model.EventSchedule;
import com.pixora.util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ScheduleDAO {
    private final PhotographerDAO photographerDAO=new PhotographerDAO();
    private final BookingDAO bookingDAO=new BookingDAO();

    public int create(int bookingId,int coordinatorUserId,int photographerUserId,LocalDate date,LocalTime start,LocalTime end,String venue,String timeline)throws SQLException{
        Booking b=bookingDAO.findById(bookingId);
        if(b==null||!"CONFIRMED".equals(b.getStatus()))throw new SQLException("A confirmed booking is required.");
        if(!photographerDAO.isAvailable(photographerUserId,date,start,end))throw new SQLException("Photographer is not available.");
        if(hasAssignmentConflict(photographerUserId,date,start,end,0))throw new SQLException("Photographer has an overlapping active assignment.");
        try(Connection c=DBConnection.getConnection()){
            c.setAutoCommit(false);
            int id;
            try(PreparedStatement ps=c.prepareStatement("INSERT INTO event_schedules(booking_id,event_date,start_time,end_time,venue,timeline,status,coordinator_user_id) VALUES(?,?,?,?,?,?,'ACTIVE',?)",Statement.RETURN_GENERATED_KEYS)){
                ps.setInt(1,bookingId);ps.setDate(2,Date.valueOf(date));ps.setTime(3,Time.valueOf(start));ps.setTime(4,Time.valueOf(end));ps.setString(5,venue);ps.setString(6,timeline);ps.setInt(7,coordinatorUserId);ps.executeUpdate();
                try(ResultSet keys=ps.getGeneratedKeys()){if(!keys.next())throw new SQLException("Schedule was not created.");id=keys.getInt(1);}
            }
            try(PreparedStatement ps=c.prepareStatement("INSERT INTO photographer_assignments(schedule_id,photographer_user_id,status) VALUES(?,?,'ACTIVE')")){
                ps.setInt(1,id);ps.setInt(2,photographerUserId);ps.executeUpdate();
            }
            addHistory(c,id,coordinatorUserId,"CREATE","Schedule created and photographer assigned");
            c.commit();return id;
        }
    }

    public void update(int scheduleId,int actorUserId,int photographerUserId,LocalDate date,LocalTime start,LocalTime end,String venue,String timeline,String reason)throws SQLException{
        if(reason==null||reason.isBlank())throw new SQLException("A reason is required for schedule changes.");
        if(!photographerDAO.isAvailable(photographerUserId,date,start,end))throw new SQLException("Photographer is not available.");
        if(hasAssignmentConflict(photographerUserId,date,start,end,scheduleId))throw new SQLException("Photographer has an overlapping active assignment.");
        try(Connection c=DBConnection.getConnection()){
            c.setAutoCommit(false);
            try(PreparedStatement ps=c.prepareStatement("UPDATE event_schedules SET event_date=?,start_time=?,end_time=?,venue=?,timeline=?,updated_at=SYSDATETIME() WHERE schedule_id=? AND status='ACTIVE'")){
                ps.setDate(1,Date.valueOf(date));ps.setTime(2,Time.valueOf(start));ps.setTime(3,Time.valueOf(end));ps.setString(4,venue);ps.setString(5,timeline);ps.setInt(6,scheduleId);
                if(ps.executeUpdate()!=1)throw new SQLException("Active schedule not found.");
            }
            try(PreparedStatement ps=c.prepareStatement("UPDATE photographer_assignments SET status='INACTIVE',updated_at=SYSDATETIME() WHERE schedule_id=? AND status='ACTIVE'")){ps.setInt(1,scheduleId);ps.executeUpdate();}
            try(PreparedStatement ps=c.prepareStatement("INSERT INTO photographer_assignments(schedule_id,photographer_user_id,status) VALUES(?,?,'ACTIVE')")){ps.setInt(1,scheduleId);ps.setInt(2,photographerUserId);ps.executeUpdate();}
            addHistory(c,scheduleId,actorUserId,"UPDATE",reason);
            c.commit();
        }
    }

    public void archive(int scheduleId,int actorUserId,String reason)throws SQLException{
        if(reason==null||reason.isBlank())throw new SQLException("A reason is required.");
        try(Connection c=DBConnection.getConnection()){
            c.setAutoCommit(false);
            try(PreparedStatement ps=c.prepareStatement("UPDATE event_schedules SET status='ARCHIVED',updated_at=SYSDATETIME() WHERE schedule_id=?")){ps.setInt(1,scheduleId);ps.executeUpdate();}
            try(PreparedStatement ps=c.prepareStatement("UPDATE photographer_assignments SET status='INACTIVE',updated_at=SYSDATETIME() WHERE schedule_id=?")){ps.setInt(1,scheduleId);ps.executeUpdate();}
            addHistory(c,scheduleId,actorUserId,"ARCHIVE",reason);c.commit();
        }
    }

    public EventSchedule findById(int id)throws SQLException{
        String sql=baseSelect()+" WHERE es.schedule_id=?";
        try(Connection c=DBConnection.getConnection();PreparedStatement ps=c.prepareStatement(sql)){ps.setInt(1,id);try(ResultSet rs=ps.executeQuery()){return rs.next()?map(rs):null;}}
    }

    public List<EventSchedule> findAllForUser(int userId,String role)throws SQLException{
        return findForUser(userId,role,"all",LocalDate.now());
    }

    public List<EventSchedule> findForUser(int userId,String role,String period,LocalDate anchor)throws SQLException{
        if(anchor==null)anchor=LocalDate.now();
        StringBuilder where=new StringBuilder("1=1");
        boolean bindUser=false;
        if("PHOTOGRAPHER".equals(role)){where.append(" AND pa.photographer_user_id=? AND pa.status='ACTIVE'");bindUser=true;}
        else if("CUSTOMER".equals(role)){where.append(" AND b.customer_user_id=?");bindUser=true;}

        LocalDate from=null,to=null;
        if("day".equals(period)){from=anchor;to=anchor;}
        else if("week".equals(period)){
            from=anchor.minusDays(anchor.getDayOfWeek().getValue()-1L);
            to=from.plusDays(6);
        }else if("month".equals(period)){
            from=anchor.withDayOfMonth(1);
            to=anchor.withDayOfMonth(anchor.lengthOfMonth());
        }else{period="all";}
        if(from!=null)where.append(" AND es.event_date BETWEEN ? AND ?");

        String sql=baseSelect()+" WHERE "+where+" ORDER BY es.event_date,es.start_time";
        List<EventSchedule> list=new ArrayList<>();
        try(Connection c=DBConnection.getConnection();PreparedStatement ps=c.prepareStatement(sql)){
            int index=1;
            if(bindUser)ps.setInt(index++,userId);
            if(from!=null){ps.setDate(index++,Date.valueOf(from));ps.setDate(index,Date.valueOf(to));}
            try(ResultSet rs=ps.executeQuery()){while(rs.next())list.add(map(rs));}
        }
        return list;
    }

    public boolean hasAssignmentConflict(int photographerUserId,LocalDate date,LocalTime start,LocalTime end,int excludeScheduleId)throws SQLException{
        String sql="SELECT COUNT(*) FROM event_schedules es JOIN photographer_assignments pa ON pa.schedule_id=es.schedule_id AND pa.status='ACTIVE' "+
                "WHERE pa.photographer_user_id=? AND es.status='ACTIVE' AND es.event_date=? AND es.schedule_id<>? AND es.start_time<? AND es.end_time>?";
        try(Connection c=DBConnection.getConnection();PreparedStatement ps=c.prepareStatement(sql)){ps.setInt(1,photographerUserId);ps.setDate(2,Date.valueOf(date));ps.setInt(3,excludeScheduleId);ps.setTime(4,Time.valueOf(end));ps.setTime(5,Time.valueOf(start));try(ResultSet rs=ps.executeQuery()){return rs.next()&&rs.getInt(1)>0;}}
    }

    private void addHistory(Connection c,int scheduleId,int actor,String action,String reason)throws SQLException{
        try(PreparedStatement ps=c.prepareStatement("INSERT INTO schedule_change_history(schedule_id,changed_by_user_id,action_type,reason) VALUES(?,?,?,?)")){ps.setInt(1,scheduleId);ps.setInt(2,actor);ps.setString(3,action);ps.setString(4,reason);ps.executeUpdate();}
    }

    private String baseSelect(){
        return "SELECT es.*,b.booking_ref,pa.photographer_user_id assigned_photographer_user_id,u.full_name photographer_name FROM event_schedules es "+
               "JOIN bookings b ON b.booking_id=es.booking_id LEFT JOIN photographer_assignments pa ON pa.schedule_id=es.schedule_id AND pa.status='ACTIVE' "+
               "LEFT JOIN users u ON u.user_id=pa.photographer_user_id";
    }
    private EventSchedule map(ResultSet rs)throws SQLException{
        EventSchedule e=new EventSchedule();e.setScheduleId(rs.getInt("schedule_id"));e.setBookingId(rs.getInt("booking_id"));e.setBookingRef(rs.getString("booking_ref"));
        e.setEventDate(rs.getDate("event_date").toLocalDate());e.setStartTime(rs.getTime("start_time").toLocalTime());e.setEndTime(rs.getTime("end_time").toLocalTime());
        e.setVenue(rs.getString("venue"));e.setTimeline(rs.getString("timeline"));e.setStatus(rs.getString("status"));e.setCoordinatorUserId(rs.getInt("coordinator_user_id"));
        int p=rs.getInt("assigned_photographer_user_id");if(!rs.wasNull())e.setAssignedPhotographerUserId(p);e.setPhotographerName(rs.getString("photographer_name"));Timestamp t=rs.getTimestamp("updated_at");if(t!=null)e.setUpdatedAt(t.toLocalDateTime());return e;
    }
}
