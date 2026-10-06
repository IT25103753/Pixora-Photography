package com.pixora.controller;

import com.pixora.dao.*;
import com.pixora.model.*;
import com.pixora.util.ValidationUtil;
import com.pixora.util.WebUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.math.BigDecimal;

@WebServlet("/payments")
public class PaymentServlet extends HttpServlet {
    private final PaymentDAO dao=new PaymentDAO();
    private final BookingDAO bookingDAO=new BookingDAO();
    private final NotificationDAO notifications=new NotificationDAO();
    private final UserDAO userDAO=new UserDAO();

    @Override protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException{
        User u=WebUtil.user(req);
        try{
            String view=req.getParameter("view");
            if("pay".equals(view)){
                if(!"CUSTOMER".equals(u.getRoleCode())){resp.sendError(403);return;}
                Booking b=bookingDAO.findById(ValidationUtil.positiveInt(req.getParameter("bookingId"),0));
                if(b==null||b.getCustomerUserId()!=u.getUserId()){resp.sendError(404);return;}
                req.setAttribute("booking",b);req.getRequestDispatcher("/WEB-INF/views/payment/pay.jsp").forward(req,resp);return;
            }
            if("invoice".equals(view)){
                int paymentId=ValidationUtil.positiveInt(req.getParameter("paymentId"),0);Payment p=dao.findById(paymentId);
                if(p==null||("CUSTOMER".equals(u.getRoleCode())&&p.getCustomerUserId()!=u.getUserId())){resp.sendError(404);return;}
                req.setAttribute("payment",p);req.setAttribute("booking",bookingDAO.findById(p.getBookingId()));req.setAttribute("invoiceRef",dao.invoiceRefForPayment(paymentId));
                req.getRequestDispatcher("/WEB-INF/views/payment/invoice.jsp").forward(req,resp);return;
            }
            if("refund".equals(view)){
                int paymentId=ValidationUtil.positiveInt(req.getParameter("paymentId"),0);Payment p=dao.findById(paymentId);
                if(p==null){resp.sendError(404);return;}req.setAttribute("payment",p);req.getRequestDispatcher("/WEB-INF/views/payment/refund.jsp").forward(req,resp);return;
            }
            req.setAttribute("payments",dao.findForUser(u.getUserId(),u.getRoleCode()));
            req.setAttribute("refunds",dao.refundsForUser(u.getUserId(),u.getRoleCode()));
            if("CUSTOMER".equals(u.getRoleCode()))req.setAttribute("bookings",bookingDAO.findForUser(u.getUserId(),u.getRoleCode()));
            req.getRequestDispatcher("/WEB-INF/views/payment/list.jsp").forward(req,resp);
        }catch(Exception e){throw new ServletException(e);}
    }

    @Override protected void doPost(HttpServletRequest req,HttpServletResponse resp)throws IOException{
        User u=WebUtil.user(req);String action=req.getParameter("action");
        try{
            if("pay".equals(action)){
                if(!"CUSTOMER".equals(u.getRoleCode()))throw new Exception("Customer access required.");
                Payment p=dao.pay(ValidationUtil.positiveInt(req.getParameter("bookingId"),0),u.getUserId(),req.getParameter("method"),req.getParameter("simulation"));
                if("PAID".equals(p.getStatus())){
                    WebUtil.flash(
                            req,
                            "success",
                            "Payment completed successfully. Your invoice is ready."
                    );
                    notifications.create(u.getUserId(),"Payment successful","Payment "+p.getPaymentRef()+" was approved and your invoice is ready.","/payments?view=invoice&paymentId="+p.getPaymentId());
                }else{
                    WebUtil.flash(
                            req,
                            "danger",
                            "Payment could not be completed: " + p.getGatewayMessage()
                    );
                    notifications.create(u.getUserId(),"Payment failed","Payment attempt "+p.getPaymentRef()+" was not approved. You may retry.","/payments");
                }
            }else if("refund-request".equals(action)){
                if(!"CUSTOMER".equals(u.getRoleCode()))throw new Exception("Customer access required.");
                BigDecimal amount=ValidationUtil.money(req.getParameter("amount"));int refundId=dao.requestRefund(ValidationUtil.positiveInt(req.getParameter("paymentId"),0),u.getUserId(),amount,req.getParameter("reason"));
                for(User staff:userDAO.findByRole("OPERATIONS_MANAGER"))notifications.create(staff.getUserId(),"Refund request","Refund #"+refundId+" requires review.","/payments");
                for(User staff:userDAO.findByRole("SYSTEM_ADMIN"))notifications.create(staff.getUserId(),"Refund request","Refund #"+refundId+" requires review.","/payments");
                WebUtil.flash(req,"success","Refund request submitted for staff review.");
            }else if("refund-process".equals(action)){
                if(!WebUtil.hasRole(req,"OPERATIONS_MANAGER","SYSTEM_ADMIN"))throw new Exception("Authorized staff access required.");
                int refundId=ValidationUtil.positiveInt(req.getParameter("refundId"),0);boolean approve="APPROVE".equals(req.getParameter("decision"));
                dao.processRefund(refundId,u.getUserId(),approve,req.getParameter("simulation"),req.getParameter("note"));
                Refund refund=dao.findRefund(refundId);Payment payment=dao.findById(refund.getPaymentId());
                notifications.create(payment.getCustomerUserId(),"Refund updated","Refund "+refund.getRefundRef()+" is now "+refund.getStatus()+".","/payments");
                WebUtil.flash(req,"success","Refund updated.");
            }else throw new Exception("Unknown payment action.");
        }catch(Exception e){WebUtil.flash(req,"danger",e.getMessage());}
        WebUtil.redirect(req,resp,"/payments");
    }
}
