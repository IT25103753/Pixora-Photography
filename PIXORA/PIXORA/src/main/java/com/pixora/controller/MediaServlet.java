package com.pixora.controller;

import com.pixora.dao.ComplaintDAO;
import com.pixora.dao.GalleryDAO;
import com.pixora.dao.PhotographerDAO;
import com.pixora.dao.UserDAO;
import com.pixora.model.*;
import com.pixora.util.FileStorageUtil;
import com.pixora.util.ValidationUtil;
import com.pixora.util.WebUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@WebServlet({"/media/photo","/media/portfolio","/media/profile","/media/evidence"})
public class MediaServlet extends HttpServlet {
    private final GalleryDAO galleryDAO=new GalleryDAO();
    private final ComplaintDAO complaintDAO=new ComplaintDAO();
    private final PhotographerDAO photographerDAO=new PhotographerDAO();
    private final UserDAO userDAO=new UserDAO();

    @Override protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws IOException{
        String servletPath=req.getServletPath();
        try{
            if("/media/photo".equals(servletPath)){serveGalleryPhoto(req,resp);return;}
            if("/media/portfolio".equals(servletPath)){servePortfolio(req,resp);return;}
            if("/media/profile".equals(servletPath)){serveProfile(req,resp);return;}
            if("/media/evidence".equals(servletPath)){serveEvidence(req,resp);return;}
            resp.sendError(404);
        }catch(Exception e){resp.sendError(404);}
    }

    private void serveGalleryPhoto(HttpServletRequest req,HttpServletResponse resp)throws Exception{
        User u=WebUtil.user(req);if(u==null){resp.sendError(401);return;}
        int id=ValidationUtil.positiveInt(req.getParameter("id"),0);Photo p=galleryDAO.findPhoto(id);if(p==null){resp.sendError(404);return;}
        Gallery g=galleryDAO.findById(p.getGalleryId());if(g==null||!galleryDAO.canAccess(g,u.getUserId(),u.getRoleCode())){resp.sendError(403);return;}
        servePath(resp,p.getFilePath(),p.getOriginalName(),"1".equals(req.getParameter("download")));
    }

    private void servePortfolio(HttpServletRequest req,HttpServletResponse resp)throws Exception{
        PortfolioItem item=photographerDAO.findPortfolioItem(ValidationUtil.positiveInt(req.getParameter("id"),0));if(item==null){resp.sendError(404);return;}
        User owner=userDAO.findById(item.getPhotographerUserId());PhotographerProfile profile=photographerDAO.findProfile(item.getPhotographerUserId());
        User viewer=WebUtil.user(req);
        boolean publicApproved=owner!=null&&"ACTIVE".equals(owner.getStatus())&&profile!=null&&"APPROVED".equals(profile.getApprovalStatus());
        boolean privileged=viewer!=null&&(viewer.getUserId()==item.getPhotographerUserId()||"SYSTEM_ADMIN".equals(viewer.getRoleCode()));
        if(!publicApproved&&!privileged){resp.sendError(403);return;}
        servePath(resp,item.getImagePath(),"portfolio-image",false);
    }

    private void serveProfile(HttpServletRequest req,HttpServletResponse resp)throws Exception{
        int userId=ValidationUtil.positiveInt(req.getParameter("userId"),0);PhotographerProfile profile=photographerDAO.findProfile(userId);User owner=userDAO.findById(userId);
        if(profile==null||profile.getProfileImagePath()==null){resp.sendError(404);return;}
        User viewer=WebUtil.user(req);
        boolean publicApproved=owner!=null&&"ACTIVE".equals(owner.getStatus())&&"APPROVED".equals(profile.getApprovalStatus());
        boolean privileged=viewer!=null&&(viewer.getUserId()==userId||"SYSTEM_ADMIN".equals(viewer.getRoleCode()));
        if(!publicApproved&&!privileged){resp.sendError(403);return;}
        servePath(resp,profile.getProfileImagePath(),"profile-image",false);
    }


    private void serveEvidence(HttpServletRequest req,HttpServletResponse resp)throws Exception{
        User viewer=WebUtil.user(req);
        if(viewer==null){resp.sendError(401);return;}
        Complaint complaint=complaintDAO.findById(ValidationUtil.positiveInt(req.getParameter("id"),0));
        if(complaint==null||complaint.getEvidencePath()==null){resp.sendError(404);return;}
        boolean allowed=complaint.getCustomerUserId()==viewer.getUserId()||"CUSTOMER_RELATIONS".equals(viewer.getRoleCode())||"SYSTEM_ADMIN".equals(viewer.getRoleCode());
        if(!allowed){resp.sendError(403);return;}
        String relative=complaint.getEvidencePath();
        String ext="";int dot=relative.lastIndexOf('.');if(dot>=0)ext=relative.substring(dot);
        servePath(resp,relative,complaint.getComplaintRef()+ext,"1".equals(req.getParameter("download")));
    }

    private void servePath(HttpServletResponse resp,String relative,String originalName,boolean download)throws Exception{
        Path path=FileStorageUtil.resolve(relative);if(!Files.exists(path)){resp.sendError(404);return;}
        String mime=Files.probeContentType(path);resp.setContentType(mime==null?"application/octet-stream":mime);
        if(download)resp.setHeader("Content-Disposition","attachment; filename=\""+safe(originalName)+"\"");
        resp.setContentLengthLong(Files.size(path));Files.copy(path,resp.getOutputStream());
    }
    private String safe(String name){return name==null?"file":name.replaceAll("[^A-Za-z0-9._-]","_");}
}
