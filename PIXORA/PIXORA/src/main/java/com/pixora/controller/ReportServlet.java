package com.pixora.controller;

import com.pixora.dao.ReportDAO;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet("/reports")
public class ReportServlet extends HttpServlet {
    private final ReportDAO dao=new ReportDAO();

    @Override protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
        try{
            req.setAttribute("bookingStatus",dao.bookingsByStatus());
            req.setAttribute("bookingEventType",dao.bookingsByEventType());
            req.setAttribute("bookingPackage",dao.bookingsByPackage());
            req.setAttribute("bookingPhotographer",dao.bookingsByPhotographer());
            req.setAttribute("complaintStatus",dao.complaintsByStatus());
            req.setAttribute("galleryStatus",dao.galleriesByStatus());
            req.setAttribute("paymentStatus",dao.paymentsByStatus());
            req.setAttribute("refundStatus",dao.refundsByStatus());
            req.setAttribute("photographerRatings",dao.photographerRatings());
            req.setAttribute("monthlyRevenue",dao.monthlyRevenue());
            req.setAttribute("revenue",dao.totalRevenue());
            req.setAttribute("outstanding",dao.outstandingBalance());
            req.setAttribute("storageBytes",dao.galleryStorageBytes());
            req.setAttribute("upcomingEvents",dao.upcomingEvents());
            req.setAttribute("assignmentChanges",dao.assignmentChanges());
            req.setAttribute("invoiceCount",dao.invoiceCount());
            req.setAttribute("openComplaints",dao.openComplaints());
            req.setAttribute("averageResolutionHours",dao.averageResolutionHours());
        }catch(Exception e){req.setAttribute("error",e.getMessage());}
        req.getRequestDispatcher("/WEB-INF/views/reports/index.jsp").forward(req,resp);
    }
}
