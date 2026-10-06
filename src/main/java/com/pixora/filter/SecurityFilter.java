package com.pixora.filter;

import com.pixora.model.User;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.Set;

@WebFilter("/*")
public class SecurityFilter implements Filter {
    private static final Set<String> PUBLIC_PREFIXES = Set.of(
            "/home",
            "/login",
            "/logout",
            "/register",
            "/auth/google",
            "/photographers",
            "/assets/",
            "/media/"
    );

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req=(HttpServletRequest)request;
        HttpServletResponse resp=(HttpServletResponse)response;
        String path=req.getRequestURI().substring(req.getContextPath().length());

        if (path.equals("/") || path.equals("/index.jsp") || isPublic(path)) {
            chain.doFilter(request,response);
            return;
        }

        HttpSession session=req.getSession(false);
        User user=session==null?null:(User)session.getAttribute("authUser");
        if(user==null){
            resp.sendRedirect(req.getContextPath()+"/login?next="+java.net.URLEncoder.encode(path,"UTF-8"));
            return;
        }
        if(!"ACTIVE".equals(user.getStatus())){
            session.invalidate();
            resp.sendRedirect(req.getContextPath()+"/login?blocked=1");
            return;
        }

        String role=user.getRoleCode();
        if(path.startsWith("/admin") && !"SYSTEM_ADMIN".equals(role)){resp.sendError(403);return;}
        if(path.startsWith("/reports") && !Set.of("SYSTEM_ADMIN","OPERATIONS_MANAGER").contains(role)){resp.sendError(403);return;}
        if(path.startsWith("/photographer/manage") && !"PHOTOGRAPHER".equals(role)){resp.sendError(403);return;}
        if(path.startsWith("/bookings") && !Set.of("CUSTOMER","PHOTOGRAPHER","EVENT_COORDINATOR","OPERATIONS_MANAGER","SYSTEM_ADMIN").contains(role)){resp.sendError(403);return;}
        if(path.startsWith("/schedule") && !Set.of("EVENT_COORDINATOR","OPERATIONS_MANAGER","PHOTOGRAPHER","CUSTOMER","SYSTEM_ADMIN").contains(role)){resp.sendError(403);return;}
        if(path.startsWith("/galleries") && !Set.of("CUSTOMER","PHOTOGRAPHER","OPERATIONS_MANAGER","SYSTEM_ADMIN").contains(role)){resp.sendError(403);return;}
        if(path.startsWith("/payments") && !Set.of("CUSTOMER","OPERATIONS_MANAGER","SYSTEM_ADMIN").contains(role)){resp.sendError(403);return;}
        if(path.startsWith("/complaints") && !Set.of("CUSTOMER","PHOTOGRAPHER","CUSTOMER_RELATIONS","SYSTEM_ADMIN").contains(role)){resp.sendError(403);return;}

        chain.doFilter(request,response);
    }

    private boolean isPublic(String path){
        for(String p:PUBLIC_PREFIXES)if(path.startsWith(p))return true;
        return false;
    }
}
