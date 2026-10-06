package com.pixora.controller;

import com.pixora.dao.*;
import com.pixora.model.User;
import com.pixora.util.ValidationUtil;
import com.pixora.util.WebUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet("/admin")
public class AdminServlet extends HttpServlet {
    private final UserDAO users=new UserDAO();
    private final PhotographerDAO photographers=new PhotographerDAO();
    private final NotificationDAO notifications=new NotificationDAO();
    private final AuditDAO audit=new AuditDAO();

    @Override protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
        try{req.setAttribute("users",users.findAll());req.setAttribute("pendingPhotographers",photographers.pendingPhotographers());}
        catch(Exception e){req.setAttribute("error",e.getMessage());}
        req.getRequestDispatcher("/WEB-INF/views/admin/index.jsp").forward(req,resp);
    }

    @Override protected void doPost(HttpServletRequest req,HttpServletResponse resp)throws IOException{
        User actor=WebUtil.user(req);
        try{
            String action=req.getParameter("action");int id=ValidationUtil.positiveInt(req.getParameter("userId"),0);
            if(id==actor.getUserId()&&"status".equals(action))throw new Exception("You cannot suspend your own account.");
            if("status".equals(action)){
                String status=req.getParameter("status");if(!java.util.Set.of("ACTIVE","SUSPENDED","INACTIVE").contains(status))throw new Exception("Invalid status.");
                users.updateStatus(id,status);audit.log(actor.getUserId(),"UPDATE","USER",id,"Status set to "+status);
            }else if("approval".equals(action)){
                String status=req.getParameter("status");if(!java.util.Set.of("APPROVED","REJECTED","PENDING").contains(status))throw new Exception("Invalid approval status.");
                photographers.setApproval(id,status);notifications.create(id,"Photographer profile review","Your profile is now "+status+".","/photographer/manage");audit.log(actor.getUserId(),"UPDATE","PHOTOGRAPHER_PROFILE",id,"Approval "+status);
            }else throw new Exception("Unknown admin action.");
            WebUtil.flash(req,"success","Administrator action completed.");
        }catch(Exception e){WebUtil.flash(req,"danger",e.getMessage());}
        WebUtil.redirect(req,resp,"/admin");
    }
}
