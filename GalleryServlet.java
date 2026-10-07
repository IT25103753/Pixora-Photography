package com.pixora.controller;

import com.pixora.dao.GalleryDAO;
import com.pixora.dao.NotificationDAO;
import com.pixora.model.Gallery;
import com.pixora.model.User;
import com.pixora.util.FileStorageUtil;
import com.pixora.util.ValidationUtil;
import com.pixora.util.WebUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet("/galleries")
@MultipartConfig
public class GalleryServlet extends HttpServlet {
    private final GalleryDAO dao = new GalleryDAO();
    private final NotificationDAO notifications = new NotificationDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = WebUtil.user(req);
        try {
            String view = req.getParameter("view");
            if ("detail".equals(view)) {
                int id = ValidationUtil.positiveInt(req.getParameter("id"), 0);
                Gallery gallery = dao.findById(id);
                if (gallery == null || !dao.canAccess(gallery, user.getUserId(), user.getRoleCode())) {
                    resp.sendError(403);
                    return;
                }
                req.setAttribute("gallery", gallery);
                req.setAttribute("albums", dao.albums(id, user.getUserId(), user.getRoleCode()));
                req.setAttribute("photos", dao.photos(id, user.getUserId(), user.getRoleCode()));
                req.getRequestDispatcher("/WEB-INF/views/gallery/view.jsp").forward(req, resp);
                return;
            }
            if ("manage".equals(view)) {
                if (!"PHOTOGRAPHER".equals(user.getRoleCode())) {
                    resp.sendError(403);
                    return;
                }
                int id = ValidationUtil.positiveInt(req.getParameter("id"), 0);
                if (id > 0) {
                    Gallery gallery = dao.findById(id);
                    if (gallery == null || gallery.getPhotographerUserId() != user.getUserId()) {
                        resp.sendError(403);
                        return;
                    }
                    req.setAttribute("gallery", gallery);
                    req.setAttribute("albums", dao.albums(id, user.getUserId(), user.getRoleCode()));
                    req.setAttribute("photos", dao.photos(id, user.getUserId(), user.getRoleCode()));
                }
                req.setAttribute("eligibleBookings", dao.completedBookingsWithoutGallery(user.getUserId()));
                req.getRequestDispatcher("/WEB-INF/views/gallery/manage.jsp").forward(req, resp);
                return;
            }
            req.setAttribute("galleries", dao.findForUser(user.getUserId(), user.getRoleCode()));
            req.getRequestDispatcher("/WEB-INF/views/gallery/list.jsp").forward(req, resp);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = WebUtil.user(req);
        String action = req.getParameter("action");
        int returnGalleryId = ValidationUtil.positiveInt(req.getParameter("galleryId"), 0);
        try {
            if ("create".equals(action)) {
                requirePhotographer(user);
                int id = dao.create(ValidationUtil.positiveInt(req.getParameter("bookingId"), 0), user.getUserId(), req.getParameter("title"));
                WebUtil.flash(req, "success", "Gallery created.");
                WebUtil.redirect(req, resp, "/galleries?view=manage&id=" + id);
                return;
            }
            if ("album-create".equals(action)) {
                requirePhotographer(user);
                dao.createAlbum(returnGalleryId, user.getUserId(), req.getParameter("name"), req.getParameter("description"));
                WebUtil.flash(req, "success", "Album created.");
                WebUtil.redirect(req, resp, "/galleries?view=manage&id=" + returnGalleryId);
                return;
            }
            if ("album-archive".equals(action)) {
                requirePhotographer(user);
                dao.archiveAlbum(ValidationUtil.positiveInt(req.getParameter("albumId"), 0), user.getUserId());
                WebUtil.flash(req, "success", "Album archived. Existing photos are retained.");
                WebUtil.redirect(req, resp, "/galleries?view=manage&id=" + returnGalleryId);
                return;
            }
            if ("upload".equals(action)) {
                requirePhotographer(user);
                Part image = req.getPart("image");
                if (image == null || image.getSize() == 0) throw new Exception("Choose an image.");
                int rawAlbumId = ValidationUtil.positiveInt(req.getParameter("albumId"), 0);
                Integer albumId = rawAlbumId > 0 ? rawAlbumId : null;
                String path = FileStorageUtil.saveImage(image, "gallery/" + returnGalleryId);
                try {
                    dao.addPhoto(returnGalleryId, user.getUserId(), albumId, req.getParameter("caption"), path,
                            image.getSubmittedFileName(), image.getSize());
                } catch (Exception e) {
                    FileStorageUtil.deleteQuietly(path);
                    throw e;
                }
                WebUtil.flash(req, "success", "Photo uploaded.");
                WebUtil.redirect(req, resp, "/galleries?view=manage&id=" + returnGalleryId);
                return;
            }
            if ("status".equals(action)) {
                requirePhotographer(user);
                String status = req.getParameter("status");
                dao.updateStatus(returnGalleryId, user.getUserId(), status);
                Gallery gallery = dao.findById(returnGalleryId);
                if ("READY_FOR_VIEWING".equals(status) || "DELIVERED".equals(status)) {
                    notifications.create(gallery.getCustomerUserId(), "Gallery ready",
                            "Your event gallery is ready to view.", "/galleries?view=detail&id=" + returnGalleryId);
                }
                WebUtil.flash(req, "success", "Gallery status updated.");
                WebUtil.redirect(req, resp, "/galleries?view=manage&id=" + returnGalleryId);
                return;
            }
            if ("favorite".equals(action)) {
                if (!"CUSTOMER".equals(user.getRoleCode())) throw new Exception("Customer access required.");
                int photoId = ValidationUtil.positiveInt(req.getParameter("photoId"), 0);
                dao.toggleFavorite(photoId, returnGalleryId, user.getUserId());
                WebUtil.redirect(req, resp, "/galleries?view=detail&id=" + returnGalleryId);
                return;
            }
            if ("photo-delete".equals(action)) {
                requirePhotographer(user);
                int photoId = ValidationUtil.positiveInt(req.getParameter("photoId"), 0);
                String path = dao.softDeletePhoto(photoId, user.getUserId());
                FileStorageUtil.deleteQuietly(path);
                WebUtil.flash(req, "success", "Photo removed from the gallery.");
                WebUtil.redirect(req, resp, "/galleries?view=manage&id=" + returnGalleryId);
                return;
            }
            if ("archive".equals(action)) {
                requirePhotographer(user);
                dao.archive(returnGalleryId, user.getUserId());
                WebUtil.flash(req, "success", "Gallery archived.");
                WebUtil.redirect(req, resp, "/galleries");
                return;
            }
            throw new Exception("Unknown gallery action.");
        } catch (Exception e) {
            WebUtil.flash(req, "danger", e.getMessage());
            WebUtil.redirect(req, resp, returnGalleryId > 0 ? "/galleries?view=manage&id=" + returnGalleryId : "/galleries");
        }
    }

    private void requirePhotographer(User user) throws Exception {
        if (user == null || !"PHOTOGRAPHER".equals(user.getRoleCode())) throw new Exception("Photographer access required.");
    }
}
