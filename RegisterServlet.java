package com.pixora.controller;

import com.pixora.dao.AuditDAO;
import com.pixora.dao.PhotographerDAO;
import com.pixora.dao.UserDAO;
import com.pixora.util.PasswordUtil;
import com.pixora.util.ValidationUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {
    private final UserDAO userDAO=new UserDAO();
    private final PhotographerDAO photographerDAO=new PhotographerDAO();
    private final AuditDAO auditDAO=new AuditDAO();

    @Override protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
        req.getRequestDispatcher("/WEB-INF/views/public/register.jsp").forward(req,resp);
    }

    @Override protected void doPost(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
        String role=req.getParameter("role"),username=req.getParameter("username"),email=req.getParameter("email"),
                fullName=req.getParameter("fullName"),phone=req.getParameter("phone"),password=req.getParameter("password"),confirm=req.getParameter("confirmPassword");
        if(!"CUSTOMER".equals(role)&&!"PHOTOGRAPHER".equals(role)){req.setAttribute("error","Choose Customer or Photographer.");doGet(req,resp);return;}
        if(ValidationUtil.blank(username)||ValidationUtil.blank(fullName)||!ValidationUtil.email(email)||!ValidationUtil.phone(phone)){
            req.setAttribute("error","Complete all required fields with a valid email/phone.");doGet(req,resp);return;
        }
        if(password==null||password.length()<8||!password.equals(confirm)){req.setAttribute("error","Passwords must match and contain at least 8 characters.");doGet(req,resp);return;}
        try{
            if(userDAO.existsUsernameOrEmail(username.trim(),email.trim())){req.setAttribute("error","Username or email already exists.");doGet(req,resp);return;}
            int id=userDAO.create(role,username.trim(),email.trim(),PasswordUtil.hash(password),fullName.trim(),phone==null?null:phone.trim());
            if("PHOTOGRAPHER".equals(role))photographerDAO.createPendingProfile(id);
            auditDAO.log(id,"CREATE","USER",id,"Self-registration as "+role);
            req.getSession().setAttribute("flashType","success");req.getSession().setAttribute("flashMessage","Registration successful. Please sign in.");
            resp.sendRedirect(req.getContextPath()+"/login");
        }catch(Exception e){req.setAttribute("error",e.getMessage());doGet(req,resp);}
    }
}
