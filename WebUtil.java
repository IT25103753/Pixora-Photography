package com.pixora.util;

import com.pixora.model.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public final class WebUtil {
    private WebUtil() {}

    public static User user(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        return session == null ? null : (User) session.getAttribute("authUser");
    }

    public static boolean hasRole(HttpServletRequest req, String... roles) {
        User user = user(req);
        if (user == null) return false;
        for (String role : roles) if (role.equals(user.getRoleCode())) return true;
        return false;
    }

    public static void flash(HttpServletRequest req, String type, String message) {
        req.getSession().setAttribute("flashType", type);
        req.getSession().setAttribute("flashMessage", message);
    }

    public static void redirect(HttpServletRequest req, javax.servlet.http.HttpServletResponse resp, String path) throws IOException {
        resp.sendRedirect(req.getContextPath() + path);
    }
}
