package com.pixora.controller;

import com.pixora.dao.UserDAO;
import com.pixora.model.User;
import com.pixora.util.ValidationUtil;
import com.pixora.util.WebUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet("/profile")
public class ProfileServlet extends HttpServlet {
    private final UserDAO userDAO=new UserDAO();
    @Override protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
        req.setAttribute("user",WebUtil.user(req));req.getRequestDispatcher("/WEB-INF/views/profile/edit.jsp").forward(req,resp);
    }
    @Override protected void doPost(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
        User u=WebUtil.user(req);String name=req.getParameter("fullName"),email=req.getParameter("email"),phone=req.getParameter("phone");
        if(ValidationUtil.blank(name)||!ValidationUtil.email(email)||!ValidationUtil.phone(phone)){req.setAttribute("error","Enter valid profile details.");doGet(req,resp);return;}
        try{
            userDAO.updateOwnProfile(u.getUserId(),name.trim(),email.trim(),phone==null?null:phone.trim());
            User refreshed=userDAO.findById(u.getUserId());refreshed.setPasswordHash(null);req.getSession().setAttribute("authUser",refreshed);
            WebUtil.flash(req,"success","Profile updated.");WebUtil.redirect(req,resp,"/profile");
        }catch(Exception e){req.setAttribute("error",e.getMessage());doGet(req,resp);}
    }
}
