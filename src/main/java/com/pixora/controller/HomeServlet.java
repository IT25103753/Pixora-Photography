package com.pixora.controller;

import com.pixora.dao.PhotographerDAO;
import com.pixora.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;

@WebServlet("/home")
public class HomeServlet extends HttpServlet {
    private final PhotographerDAO photographerDAO=new PhotographerDAO();

    @Override protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
        try{
            List<User> photographers=photographerDAO.searchApproved(null,null,null,null,null);
            if(photographers.size()>3)photographers=photographers.subList(0,3);
            req.setAttribute("featuredPhotographers",photographers);
        }catch(Exception e){req.setAttribute("featuredPhotographers",java.util.Collections.emptyList());}
        req.getRequestDispatcher("/WEB-INF/views/public/home.jsp").forward(req,resp);
    }
}
