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
import java.util.List;

@WebServlet("/bookings")
public class BookingServlet extends HttpServlet {
    private final BookingDAO dao=new BookingDAO();
    private final PhotographerDAO photographerDAO=new PhotographerDAO();
    private final NotificationDAO notifications=new NotificationDAO();
    private final AuditDAO audit=new AuditDAO();

    @Override protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
        User u=WebUtil.user(req);String view=req.getParameter("view");
        if ("availability".equals(view)) {
            handleAvailability(req, resp);
            return;
        }

        try{
            if("new".equals(view) || "edit".equals(view)){
                if(!"CUSTOMER".equals(u.getRoleCode())){resp.sendError(403);return;}
                if("edit".equals(view)){
                    Booking edit=dao.findById(ValidationUtil.positiveInt(req.getParameter("id"),0));
                    if(edit==null||edit.getCustomerUserId()!=u.getUserId()||!"PENDING".equals(edit.getStatus())){resp.sendError(403);return;}
                    req.setAttribute("booking",edit);req.setAttribute("photographerId",edit.getPhotographerUserId());
                    req.setAttribute("packages",photographerDAO.packages(edit.getPhotographerUserId(),true));
                }else{
                    int photographerId=ValidationUtil.positiveInt(req.getParameter("photographerId"),0);
                    req.setAttribute("photographerId",photographerId);
                    req.setAttribute("packages",photographerDAO.packages(photographerId,true));
                }
                req.getRequestDispatcher("/WEB-INF/views/booking/form.jsp").forward(req,resp);return;
            }
            if("detail".equals(view)){
                int id=ValidationUtil.positiveInt(req.getParameter("id"),0);Booking b=dao.findById(id);
                if(b==null||!canView(u,b)){resp.sendError(404);return;}
                req.setAttribute("booking",b);req.setAttribute("history",dao.history(id));
                req.getRequestDispatcher("/WEB-INF/views/booking/view.jsp").forward(req,resp);return;
            }
            req.setAttribute("bookings",dao.findForUser(u.getUserId(),u.getRoleCode()));
            req.getRequestDispatcher("/WEB-INF/views/booking/list.jsp").forward(req,resp);
        }catch(Exception e){throw new ServletException(e);}
    }

    @Override protected void doPost(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
        User u=WebUtil.user(req);String action=req.getParameter("action");
        try{
            if("create".equals(action)){
                if(!"CUSTOMER".equals(u.getRoleCode()))throw new Exception("Customer access required.");
                int photographerId=ValidationUtil.positiveInt(req.getParameter("photographerId"),0),packageId=ValidationUtil.positiveInt(req.getParameter("packageId"),0);
                LocalDate date=ValidationUtil.date(req.getParameter("eventDate"));LocalTime start=ValidationUtil.time(req.getParameter("startTime"));LocalTime end=ValidationUtil.time(req.getParameter("endTime"));
                if(date==null||start==null||end==null||!end.isAfter(start)||date.isBefore(LocalDate.now())||ValidationUtil.blank(req.getParameter("eventType"))||ValidationUtil.blank(req.getParameter("venue")))throw new Exception("Enter a valid event type, venue, date and time.");
                int id=dao.create(u.getUserId(),photographerId,packageId,req.getParameter("eventType").trim(),date,start,end,req.getParameter("venue").trim(),req.getParameter("notes"));
                notifications.create(photographerId,"New booking request","A customer submitted a new booking request.","/bookings?view=detail&id="+id);
                audit.log(u.getUserId(),"CREATE","BOOKING",id,"Booking request created");
            }else if("update".equals(action)){
                if(!"CUSTOMER".equals(u.getRoleCode()))throw new Exception("Customer access required.");
                int id=ValidationUtil.positiveInt(req.getParameter("id"),0);LocalDate date=ValidationUtil.date(req.getParameter("eventDate"));LocalTime start=ValidationUtil.time(req.getParameter("startTime"));LocalTime end=ValidationUtil.time(req.getParameter("endTime"));
                if(date==null||start==null||end==null||!end.isAfter(start)||date.isBefore(LocalDate.now())||ValidationUtil.blank(req.getParameter("eventType"))||ValidationUtil.blank(req.getParameter("venue")))throw new Exception("Enter valid booking details.");
                dao.updatePending(id,u.getUserId(),req.getParameter("eventType").trim(),date,start,end,req.getParameter("venue").trim(),req.getParameter("notes"));
                audit.log(u.getUserId(),"UPDATE","BOOKING",id,"Pending booking edited");
            }else if("decision".equals(action)){
                if(!"PHOTOGRAPHER".equals(u.getRoleCode()))throw new Exception("Photographer access required.");
                int id=ValidationUtil.positiveInt(req.getParameter("id"),0);boolean accept="ACCEPT".equals(req.getParameter("decision"));
                dao.photographerDecision(id,u.getUserId(),accept,req.getParameter("reason"));Booking b=dao.findById(id);
                notifications.create(b.getCustomerUserId(),"Booking "+(accept?"confirmed":"rejected"),"Booking "+b.getBookingRef()+" status changed.","/bookings?view=detail&id="+id);
                audit.log(u.getUserId(),"UPDATE","BOOKING",id,"Decision: "+(accept?"CONFIRMED":"REJECTED"));
            }else if("cancel".equals(action)){
                int id=ValidationUtil.positiveInt(req.getParameter("id"),0);if(!"CUSTOMER".equals(u.getRoleCode()))throw new Exception("Customer access required.");
                dao.cancel(id,u.getUserId(),req.getParameter("reason"));Booking b=dao.findById(id);notifications.create(b.getPhotographerUserId(),"Booking cancelled","Booking "+b.getBookingRef()+" was cancelled.","/bookings?view=detail&id="+id);
                audit.log(u.getUserId(),"UPDATE","BOOKING",id,"Cancelled");
            }else if("complete".equals(action)){
                if(!WebUtil.hasRole(req,"EVENT_COORDINATOR","OPERATIONS_MANAGER","SYSTEM_ADMIN"))throw new Exception("Staff access required.");
                int id=ValidationUtil.positiveInt(req.getParameter("id"),0);dao.markCompleted(id,u.getUserId());Booking b=dao.findById(id);
                notifications.create(b.getPhotographerUserId(),"Event completed","You can now create the gallery for "+b.getBookingRef()+".","/galleries");
                notifications.create(b.getCustomerUserId(),"Event completed","Your event has been marked completed.","/bookings?view=detail&id="+id);
            }else throw new Exception("Unknown booking action.");
            WebUtil.flash(req,"success","Booking operation completed.");WebUtil.redirect(req,resp,"/bookings");
        }catch(Exception e){WebUtil.flash(req,"danger",e.getMessage());WebUtil.redirect(req,resp,"/bookings");}
    }

    private void handleAvailability(
            HttpServletRequest req,
            HttpServletResponse resp) throws IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {

            int photographerId =
                    ValidationUtil.positiveInt(
                            req.getParameter("photographerId"), 0);

            LocalDate date =
                    ValidationUtil.date(
                            req.getParameter("date"));

            if (photographerId <= 0 || date == null) {
                resp.getWriter().write(
                        "{\"success\":false,\"message\":\"Invalid photographer or date.\"}"
                );
                return;
            }

            List<AvailabilitySlot> slots =
                    photographerDAO.availabilityForDate(
                            photographerId,
                            date);

            List<Booking> confirmed =
                    dao.confirmedBookingsForDate(
                            photographerId,
                            date);

            StringBuilder json = new StringBuilder();

            json.append("{");
            json.append("\"success\":true,");

            // Availability slots
            json.append("\"slots\":[");

            for (int i = 0; i < slots.size(); i++) {

                AvailabilitySlot slot = slots.get(i);

                if (i > 0) {
                    json.append(",");
                }

                json.append("{")
                        .append("\"start\":\"")
                        .append(slot.getStartTime())
                        .append("\",")
                        .append("\"end\":\"")
                        .append(slot.getEndTime())
                        .append("\"")
                        .append("}");
            }

            json.append("],");

            // Confirmed bookings
            json.append("\"bookings\":[");

            for (int i = 0; i < confirmed.size(); i++) {

                Booking booking = confirmed.get(i);

                if (i > 0) {
                    json.append(",");
                }

                json.append("{")
                        .append("\"start\":\"")
                        .append(booking.getStartTime())
                        .append("\",")
                        .append("\"end\":\"")
                        .append(booking.getEndTime())
                        .append("\"")
                        .append("}");
            }

            json.append("]");

            json.append("}");

            resp.getWriter().write(json.toString());

        } catch (Exception e) {

            resp.getWriter().write(
                    "{\"success\":false,\"message\":\"Unable to load availability.\"}"
            );
        }
    }

    private boolean canView(User u,Booking b){
        return "SYSTEM_ADMIN".equals(u.getRoleCode())||"OPERATIONS_MANAGER".equals(u.getRoleCode())||"EVENT_COORDINATOR".equals(u.getRoleCode())||
                b.getCustomerUserId()==u.getUserId()||b.getPhotographerUserId()==u.getUserId();
    }
}
