<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="Reports"/>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>
<section class="page-hero compact"><div class="container"><span class="eyebrow">OPERATIONS & REPORTING</span><h1>PIXORA reports</h1><p>Operational, financial, gallery and customer-relations summaries derived from live system records.</p></div></section>
<div class="container py-5">
    <c:if test="${not empty error}"><div class="alert alert-danger"><c:out value="${error}"/></div></c:if>
    <div class="row g-3 mb-4">
        <div class="col-md-6 col-xl-3"><div class="stat-card"><span>Paid revenue</span><strong>LKR <fmt:formatNumber value="${revenue}" minFractionDigits="2"/></strong></div></div>
        <div class="col-md-6 col-xl-3"><div class="stat-card"><span>Outstanding balances</span><strong>LKR <fmt:formatNumber value="${outstanding}" minFractionDigits="2"/></strong></div></div>
        <div class="col-md-6 col-xl-3"><div class="stat-card"><span>Upcoming events</span><strong>${upcomingEvents}</strong></div></div>
        <div class="col-md-6 col-xl-3"><div class="stat-card"><span>Open complaints</span><strong>${openComplaints}</strong></div></div>
        <div class="col-md-6 col-xl-3"><div class="stat-card"><span>Issued invoices</span><strong>${invoiceCount}</strong></div></div>
        <div class="col-md-6 col-xl-3"><div class="stat-card"><span>Assignment changes</span><strong>${assignmentChanges}</strong></div></div>
        <div class="col-md-6 col-xl-3"><div class="stat-card"><span>Gallery storage</span><strong><fmt:formatNumber value="${storageBytes / 1048576.0}" maxFractionDigits="2"/> MB</strong></div></div>
        <div class="col-md-6 col-xl-3"><div class="stat-card"><span>Avg. complaint resolution</span><strong>${averageResolutionHours} h</strong></div></div>
    </div>

    <div class="row g-4">
        <div class="col-lg-4"><div class="panel-card h-100"><h4>Bookings by status</h4><c:forEach items="${bookingStatus}" var="x"><div class="report-row"><span><c:out value="${x.key}"/></span><strong>${x.value}</strong></div></c:forEach></div></div>
        <div class="col-lg-4"><div class="panel-card h-100"><h4>Bookings by event type</h4><c:forEach items="${bookingEventType}" var="x"><div class="report-row"><span><c:out value="${x.key}"/></span><strong>${x.value}</strong></div></c:forEach></div></div>
        <div class="col-lg-4"><div class="panel-card h-100"><h4>Bookings by package</h4><c:forEach items="${bookingPackage}" var="x"><div class="report-row"><span><c:out value="${x.key}"/></span><strong>${x.value}</strong></div></c:forEach></div></div>
        <div class="col-lg-4"><div class="panel-card h-100"><h4>Bookings by photographer</h4><c:forEach items="${bookingPhotographer}" var="x"><div class="report-row"><span><c:out value="${x.key}"/></span><strong>${x.value}</strong></div></c:forEach></div></div>
        <div class="col-lg-4"><div class="panel-card h-100"><h4>Payments by status</h4><c:forEach items="${paymentStatus}" var="x"><div class="report-row"><span><c:out value="${x.key}"/></span><strong>${x.value}</strong></div></c:forEach></div></div>
        <div class="col-lg-4"><div class="panel-card h-100"><h4>Refunds by status</h4><c:forEach items="${refundStatus}" var="x"><div class="report-row"><span><c:out value="${x.key}"/></span><strong>${x.value}</strong></div></c:forEach><c:if test="${empty refundStatus}"><div class="text-secondary small">No refund records yet.</div></c:if></div></div>
        <div class="col-lg-4"><div class="panel-card h-100"><h4>Galleries by status</h4><c:forEach items="${galleryStatus}" var="x"><div class="report-row"><span><c:out value="${x.key}"/></span><strong>${x.value}</strong></div></c:forEach></div></div>
        <div class="col-lg-4"><div class="panel-card h-100"><h4>Complaints by status</h4><c:forEach items="${complaintStatus}" var="x"><div class="report-row"><span><c:out value="${x.key}"/></span><strong>${x.value}</strong></div></c:forEach></div></div>
        <div class="col-lg-4"><div class="panel-card h-100"><h4>Photographer ratings</h4><c:forEach items="${photographerRatings}" var="x"><div class="report-row"><span><c:out value="${x.key}"/></span><strong>${x.value} review(s)</strong></div></c:forEach></div></div>
        <div class="col-lg-8"><div class="panel-card h-100"><h4>Monthly paid transaction totals</h4><c:forEach items="${monthlyRevenue}" var="x"><div class="report-row"><span><c:out value="${x.key}"/></span><strong>LKR <fmt:formatNumber value="${x.value}" minFractionDigits="2"/></strong></div></c:forEach><c:if test="${empty monthlyRevenue}"><div class="text-secondary small">No paid transactions yet.</div></c:if></div></div>
    </div>
</div>
<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
