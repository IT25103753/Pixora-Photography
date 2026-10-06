package com.pixora.controller;

import com.pixora.dao.*;
import com.pixora.model.*;
import com.pixora.util.FileStorageUtil;
import com.pixora.util.ValidationUtil;
import com.pixora.util.WebUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet("/complaints")
@MultipartConfig
public class ComplaintServlet extends HttpServlet {
    private final ComplaintDAO dao=new ComplaintDAO();
    private final BookingDAO bookingDAO=new BookingDAO();
    private final UserDAO userDAO=new UserDAO();
    private final NotificationDAO notifications=new NotificationDAO();

    @Override protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
        User u=WebUtil.user(req);String view=req.getParameter("view");
        try{
            if("new".equals(view)){
                if(!"CUSTOMER".equals(u.getRoleCode())){resp.sendError(403);return;}
                req.setAttribute("bookings",bookingDAO.findForUser(u.getUserId(),"CUSTOMER"));
                req.getRequestDispatcher("/WEB-INF/views/complaint/form.jsp").forward(req,resp);return;
            }
            if("detail".equals(view)){
                Complaint c=dao.findById(ValidationUtil.positiveInt(req.getParameter("id"),0));
                if(c==null){resp.sendError(404);return;}
                if("CUSTOMER".equals(u.getRoleCode())&&c.getCustomerUserId()!=u.getUserId()){resp.sendError(403);return;}
                if("PHOTOGRAPHER".equals(u.getRoleCode())){Booking linked=bookingDAO.findById(c.getBookingId());if(linked==null||linked.getPhotographerUserId()!=u.getUserId()){resp.sendError(403);return;}}
                req.setAttribute("complaint",c);req.setAttribute("actions",dao.actions(c.getComplaintId()));
                if(WebUtil.hasRole(req,"CUSTOMER_RELATIONS","SYSTEM_ADMIN"))req.setAttribute("supportUsers",userDAO.findByRole("CUSTOMER_RELATIONS"));
                req.getRequestDispatcher("/WEB-INF/views/complaint/view.jsp").forward(req,resp);return;
            }
            if("review".equals(view)){
                if(!"CUSTOMER".equals(u.getRoleCode())){resp.sendError(403);return;}
                req.setAttribute("booking",bookingDAO.findById(ValidationUtil.positiveInt(req.getParameter("bookingId"),0)));
                req.getRequestDispatcher("/WEB-INF/views/complaint/review.jsp").forward(req,resp);return;
            }
            req.setAttribute("complaints",dao.findForUser(u.getUserId(),u.getRoleCode()));
            if("SYSTEM_ADMIN".equals(u.getRoleCode()))req.setAttribute("reviews",dao.allReviews());
            req.getRequestDispatcher("/WEB-INF/views/complaint/list.jsp").forward(req,resp);
        }catch(Exception e){throw new ServletException(e);}
    }

    @Override protected void doPost(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
        User u=WebUtil.user(req);String action=req.getParameter("action");
        try{
            if("create".equals(action)){
                if(!"CUSTOMER".equals(u.getRoleCode()))throw new Exception("Customer access required.");
                Part evidence=req.getPart("evidence");String path=(evidence!=null&&evidence.getSize()>0)?FileStorageUtil.saveEvidence(evidence,"evidence"):null;
                int id;
                try{
                    id=dao.create(ValidationUtil.positiveInt(req.getParameter("bookingId"),0),u.getUserId(),req.getParameter("category"),req.getParameter("description"),path);
                }catch(Exception ex){
                    FileStorageUtil.deleteQuietly(path);
                    throw ex;
                }
                for(User s:userDAO.findByRole("CUSTOMER_RELATIONS"))notifications.create(s.getUserId(),"New complaint","A new complaint requires review.","/complaints?view=detail&id="+id);
            }else if("workflow".equals(action)){
                if(!WebUtil.hasRole(req,"CUSTOMER_RELATIONS","SYSTEM_ADMIN"))throw new Exception("Support access required.");
                int id=ValidationUtil.positiveInt(req.getParameter("id"),0),assigned=ValidationUtil.positiveInt(req.getParameter("assignedTo"),0);
                dao.updateWorkflow(id,assigned==0?null:assigned,req.getParameter("priority"),req.getParameter("status"),req.getParameter("resolution"),u.getUserId(),req.getParameter("actionText"));
                Complaint c=dao.findById(id);notifications.create(c.getCustomerUserId(),"Complaint updated","Complaint "+c.getComplaintRef()+" is now "+c.getStatus()+".","/complaints?view=detail&id="+id);
            }else if("review-create".equals(action)){
                if(!"CUSTOMER".equals(u.getRoleCode()))throw new Exception("Customer access required.");
                dao.createReview(ValidationUtil.positiveInt(req.getParameter("bookingId"),0),u.getUserId(),ValidationUtil.positiveInt(req.getParameter("rating"),0),req.getParameter("comment"));
            }else if("review-moderate".equals(action)){
                if(!"SYSTEM_ADMIN".equals(u.getRoleCode()))throw new Exception("Administrator access required.");
                dao.moderateReview(ValidationUtil.positiveInt(req.getParameter("reviewId"),0),"SHOW".equals(req.getParameter("decision")));
            }else throw new Exception("Unknown complaint action.");
            WebUtil.flash(req,"success","Operation completed.");
        }catch(Exception e){WebUtil.flash(req,"danger",e.getMessage());}
        WebUtil.redirect(req,resp,"/complaints");
    }
}
