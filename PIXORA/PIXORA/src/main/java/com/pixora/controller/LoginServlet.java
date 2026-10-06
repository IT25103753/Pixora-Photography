package com.pixora.controller;

import com.pixora.dao.UserDAO;
import com.pixora.model.User;
import com.pixora.util.PasswordUtil;
import com.pixora.util.ValidationUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private final UserDAO userDAO=new UserDAO();

    @Override protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
        req.getRequestDispatcher("/WEB-INF/views/public/login.jsp").forward(req,resp);
    }

    @Override protected void doPost(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
        String login=req.getParameter("login"),password=req.getParameter("password");
        if(ValidationUtil.blank(login)||ValidationUtil.blank(password)){req.setAttribute("error","Username/email and password are required.");doGet(req,resp);return;}
        try{
            User user=userDAO.findByLogin(login.trim());
            if(user==null||!PasswordUtil.verify(password,user.getPasswordHash())){
                req.setAttribute("error","Invalid username/email or password.");doGet(req,resp);return;
            }
            if(!"ACTIVE".equals(user.getStatus())){req.setAttribute("error","This account is suspended or inactive.");doGet(req,resp);return;}
            HttpSession session=req.getSession(true);session.invalidate();session=req.getSession(true);
            user.setPasswordHash(null);
            session.setAttribute("authUser",user);
            session.setMaxInactiveInterval(30*60);
            resp.sendRedirect(req.getContextPath()+"/dashboard");
        }catch(Exception e){e.printStackTrace();req.setAttribute("error","Login failed. Check database configuration if the problem continues.");doGet(req,resp);}
    }
}
