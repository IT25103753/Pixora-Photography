package com.pixora.controller;

import com.pixora.dao.ComplaintDAO;
import com.pixora.dao.PhotographerDAO;
import com.pixora.dao.UserDAO;
import com.pixora.model.User;
import com.pixora.util.ValidationUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;

@WebServlet("/photographers")
public class PhotographerBrowseServlet extends HttpServlet {
    private final PhotographerDAO photographerDAO=new PhotographerDAO();
    private final UserDAO userDAO=new UserDAO();
    private final ComplaintDAO complaintDAO=new ComplaintDAO();

    @Override protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
        try{
            String id=req.getParameter("id");
            if(id!=null&&!id.isBlank()){
                int userId=ValidationUtil.positiveInt(id,0);
                User p=userDAO.findById(userId);
                com.pixora.model.PhotographerProfile profile=photographerDAO.findProfile(userId);
                if(p==null||!"PHOTOGRAPHER".equals(p.getRoleCode())||!"ACTIVE".equals(p.getStatus())||profile==null||!"APPROVED".equals(profile.getApprovalStatus())){resp.sendError(404);return;}
                req.setAttribute("photographer",p);
                req.setAttribute("profile",profile);
                req.setAttribute("portfolio",photographerDAO.portfolio(userId));
                req.setAttribute("packages",photographerDAO.packages(userId,true));
                req.setAttribute("availability",photographerDAO.availability(userId,true));
                req.setAttribute("reviews",complaintDAO.reviewsForPhotographer(userId,true));
                req.getRequestDispatcher("/WEB-INF/views/public/photographer-detail.jsp").forward(req,resp);
                return;
            }
            String specialty=req.getParameter("specialty");
            BigDecimal min=ValidationUtil.money(req.getParameter("minPrice"));
            BigDecimal max=ValidationUtil.money(req.getParameter("maxPrice"));
            LocalDate date=ValidationUtil.date(req.getParameter("date"));
            Integer rating=null;int r=ValidationUtil.positiveInt(req.getParameter("rating"),0);if(r>0)rating=r;
            req.setAttribute("photographers",photographerDAO.searchApproved(specialty,min,max,date,rating));
            req.getRequestDispatcher("/WEB-INF/views/public/photographers.jsp").forward(req,resp);
        }catch(Exception e){throw new ServletException(e);}
    }
}
