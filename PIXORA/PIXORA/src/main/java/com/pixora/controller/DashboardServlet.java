package com.pixora.controller;

import com.pixora.dao.NotificationDAO;
import com.pixora.dao.ReportDAO;
import com.pixora.model.User;
import com.pixora.util.WebUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {
    private final ReportDAO reportDAO=new ReportDAO();
    private final NotificationDAO notificationDAO=new NotificationDAO();

    @Override protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
        User u=WebUtil.user(req);
        try{
            req.setAttribute("stats",reportDAO.dashboard(u.getRoleCode(),u.getUserId()));
            req.setAttribute("notifications",notificationDAO.findByUser(u.getUserId(),5));
            req.setAttribute("unreadCount",notificationDAO.unreadCount(u.getUserId()));
        }catch(Exception e){req.setAttribute("error",e.getMessage());}
        req.getRequestDispatcher("/WEB-INF/views/dashboard.jsp").forward(req,resp);
    }
}
