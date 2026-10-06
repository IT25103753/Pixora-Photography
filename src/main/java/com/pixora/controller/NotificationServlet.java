package com.pixora.controller;

import com.pixora.dao.NotificationDAO;
import com.pixora.model.User;
import com.pixora.util.ValidationUtil;
import com.pixora.util.WebUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet("/notifications")
public class NotificationServlet extends HttpServlet {
    private final NotificationDAO dao=new NotificationDAO();
    @Override protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
        User u=WebUtil.user(req);try{req.setAttribute("notifications",dao.findByUser(u.getUserId(),100));}catch(Exception e){req.setAttribute("error",e.getMessage());}
        req.getRequestDispatcher("/WEB-INF/views/notification/list.jsp").forward(req,resp);
    }
    @Override protected void doPost(HttpServletRequest req,HttpServletResponse resp)throws IOException{
        User u=WebUtil.user(req);try{if("all".equals(req.getParameter("mode")))dao.markAllRead(u.getUserId());else dao.markRead(ValidationUtil.positiveInt(req.getParameter("id"),0),u.getUserId());}catch(Exception ignored){}
        WebUtil.redirect(req,resp,"/notifications");
    }
}
