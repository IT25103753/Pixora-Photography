package com.pixora.controller;

import com.pixora.dao.AuditDAO;
import com.pixora.dao.PhotographerDAO;
import com.pixora.model.PhotographerProfile;
import com.pixora.model.User;
import com.pixora.util.FileStorageUtil;
import com.pixora.util.ValidationUtil;
import com.pixora.util.WebUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@WebServlet("/photographer/manage")
@MultipartConfig
public class PhotographerManageServlet extends HttpServlet {
    private final PhotographerDAO dao=new PhotographerDAO();
    private final AuditDAO audit=new AuditDAO();

    @Override protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
        User u=WebUtil.user(req);if(!WebUtil.hasRole(req,"PHOTOGRAPHER")){resp.sendError(403);return;}
        try{
            req.setAttribute("profile",dao.findProfile(u.getUserId()));
            req.setAttribute("portfolio",dao.portfolio(u.getUserId()));
            req.setAttribute("packages",dao.packages(u.getUserId(),false));
            req.setAttribute("availability",dao.availability(u.getUserId(),false));
            int editPackageId=ValidationUtil.positiveInt(req.getParameter("editPackageId"),0);
            if(editPackageId>0){com.pixora.model.PhotographyPackage ep=dao.findPackage(editPackageId);if(ep!=null&&ep.getPhotographerUserId()==u.getUserId())req.setAttribute("editPackage",ep);}
            int editAvailabilityId=ValidationUtil.positiveInt(req.getParameter("editAvailabilityId"),0);
            if(editAvailabilityId>0){com.pixora.model.AvailabilitySlot ea=dao.findAvailability(editAvailabilityId);if(ea!=null&&ea.getPhotographerUserId()==u.getUserId())req.setAttribute("editAvailability",ea);}
        }catch(Exception e){req.setAttribute("error",e.getMessage());}
        req.getRequestDispatcher("/WEB-INF/views/photographer/manage.jsp").forward(req,resp);
    }

    @Override protected void doPost(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
        User u=WebUtil.user(req);if(!WebUtil.hasRole(req,"PHOTOGRAPHER")){resp.sendError(403);return;}
        String action=req.getParameter("action");
        try{
            switch(action==null?"":action){
                case "profile":{
                    String bio=req.getParameter("bio"),specialty=req.getParameter("specialty"),location=req.getParameter("location");
                    String path=null;Part image=req.getPart("profileImage");if(image!=null&&image.getSize()>0)path=FileStorageUtil.saveImage(image,"profiles");
                    dao.updateProfile(u.getUserId(),bio,specialty,location,path);
                    audit.log(u.getUserId(),"UPDATE","PHOTOGRAPHER_PROFILE",u.getUserId(),"Profile updated");break;
                }
                case "portfolio-add":{
                    String title=req.getParameter("title");Part image=req.getPart("image");
                    if(ValidationUtil.blank(title)||image==null||image.getSize()==0)throw new Exception("Title and image are required.");
                    String path=FileStorageUtil.saveImage(image,"portfolio");
                    dao.addPortfolio(u.getUserId(),title.trim(),req.getParameter("description"),path);break;
                }
                case "portfolio-update":{
                    int id=ValidationUtil.positiveInt(req.getParameter("id"),0);String title=req.getParameter("title");
                    if(ValidationUtil.blank(title))throw new Exception("Portfolio title is required.");
                    dao.updatePortfolio(id,u.getUserId(),title.trim(),req.getParameter("description"));break;
                }
                case "portfolio-delete":{
                    int id=ValidationUtil.positiveInt(req.getParameter("id"),0);String path=dao.deletePortfolio(id,u.getUserId());FileStorageUtil.deleteQuietly(path);break;
                }
                case "package-save":{
                    int id=ValidationUtil.positiveInt(req.getParameter("id"),0);BigDecimal price=ValidationUtil.money(req.getParameter("price"));
                    int hours=ValidationUtil.positiveInt(req.getParameter("durationHours"),0);
                    if(ValidationUtil.blank(req.getParameter("name"))||price==null||hours<=0)throw new Exception("Valid package name, price and duration are required.");
                    dao.savePackage(u.getUserId(),id,req.getParameter("name").trim(),req.getParameter("description"),price,hours);break;
                }
                case "package-delete": dao.deactivatePackage(ValidationUtil.positiveInt(req.getParameter("id"),0),u.getUserId());break;
                case "availability-add":
                case "availability-update":{
                    LocalDate date=ValidationUtil.date(req.getParameter("date"));LocalTime start=ValidationUtil.time(req.getParameter("start"));LocalTime end=ValidationUtil.time(req.getParameter("end"));
                    if(date==null||start==null||end==null||!end.isAfter(start)||date.isBefore(LocalDate.now()))throw new Exception("Enter a valid future availability period.");
                    int id=ValidationUtil.positiveInt(req.getParameter("id"),0);
                    if("availability-update".equals(action)&&id>0)dao.updateAvailability(id,u.getUserId(),date,start,end);else dao.addAvailability(u.getUserId(),date,start,end);break;
                }
                case "availability-delete":dao.removeAvailability(ValidationUtil.positiveInt(req.getParameter("id"),0),u.getUserId());break;
                default:throw new Exception("Unknown action.");
            }
            WebUtil.flash(req,"success","Changes saved.");WebUtil.redirect(req,resp,"/photographer/manage");
        }catch(Exception e){req.setAttribute("error",e.getMessage());doGet(req,resp);}
    }
}
