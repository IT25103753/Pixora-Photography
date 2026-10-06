package com.pixora.controller;

import com.pixora.dao.*;
import com.pixora.model.*;
import com.pixora.util.ValidationUtil;
import com.pixora.util.WebUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;

@WebServlet("/schedule")
public class ScheduleServlet extends HttpServlet {
    private final ScheduleDAO dao=new ScheduleDAO();
    private final BookingDAO bookingDAO=new BookingDAO();
    private final UserDAO userDAO=new UserDAO();
    private final NotificationDAO notifications=new NotificationDAO();

    @Override protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
        User u=WebUtil.user(req);
        try{
            if("form".equals(req.getParameter("view"))){
                if(!WebUtil.hasRole(req,"EVENT_COORDINATOR","OPERATIONS_MANAGER","SYSTEM_ADMIN")){resp.sendError(403);return;}
                int id=ValidationUtil.positiveInt(req.getParameter("id"),0);if(id>0)req.setAttribute("schedule",dao.findById(id));
                req.setAttribute("bookings",bookingDAO.confirmedWithoutSchedule());
                req.setAttribute("photographers",userDAO.findByRole("PHOTOGRAPHER"));
                req.getRequestDispatcher("/WEB-INF/views/schedule/form.jsp").forward(req,resp);return;
            }
            String period=req.getParameter("period");
            if(!"day".equals(period)&&!"week".equals(period)&&!"month".equals(period)&&!"all".equals(period))period="all";
            LocalDate anchor=ValidationUtil.date(req.getParameter("date"));if(anchor==null)anchor=LocalDate.now();
            req.setAttribute("period",period);req.setAttribute("anchorDate",anchor);
            req.setAttribute("schedules",dao.findForUser(u.getUserId(),u.getRoleCode(),period,anchor));
            req.getRequestDispatcher("/WEB-INF/views/schedule/list.jsp").forward(req,resp);
        }catch(Exception e){throw new ServletException(e);}
    }

    @Override protected void doPost(HttpServletRequest req,HttpServletResponse resp)throws IOException{
        User u=WebUtil.user(req);if(!WebUtil.hasRole(req,"EVENT_COORDINATOR","OPERATIONS_MANAGER","SYSTEM_ADMIN")){resp.sendError(403);return;}
        try{
            String action=req.getParameter("action");
            if("archive".equals(action)){dao.archive(ValidationUtil.positiveInt(req.getParameter("id"),0),u.getUserId(),req.getParameter("reason"));}
            else{
                int id=ValidationUtil.positiveInt(req.getParameter("id"),0),bookingId=ValidationUtil.positiveInt(req.getParameter("bookingId"),0),photographerId=ValidationUtil.positiveInt(req.getParameter("photographerId"),0);
                LocalDate date=ValidationUtil.date(req.getParameter("eventDate"));LocalTime start=ValidationUtil.time(req.getParameter("startTime"));LocalTime end=ValidationUtil.time(req.getParameter("endTime"));
                if(date==null||start==null||end==null||!end.isAfter(start))throw new Exception("Invalid schedule date/time.");
                if(id>0)dao.update(id,u.getUserId(),photographerId,date,start,end,req.getParameter("venue"),req.getParameter("timeline"),req.getParameter("reason"));
                else id=dao.create(bookingId,u.getUserId(),photographerId,date,start,end,req.getParameter("venue"),req.getParameter("timeline"));
                EventSchedule s=dao.findById(id);Booking b=bookingDAO.findById(s.getBookingId());
                notifications.create(photographerId,"Event schedule updated","Schedule for "+b.getBookingRef()+" is available.","/schedule");
                notifications.create(b.getCustomerUserId(),"Event schedule updated","Schedule for "+b.getBookingRef()+" was updated.","/schedule");
            }
            WebUtil.flash(req,"success","Schedule saved.");WebUtil.redirect(req,resp,"/schedule");
        }catch(Exception e){WebUtil.flash(req,"danger",e.getMessage());WebUtil.redirect(req,resp,"/schedule");}
    }
}
