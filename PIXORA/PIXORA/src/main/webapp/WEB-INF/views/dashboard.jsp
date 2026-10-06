<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Dashboard"/>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>
<section class="page-hero compact"><div class="container"><span class="eyebrow">${sessionScope.authUser.roleCode}</span><h1>Hello, <c:out value="${sessionScope.authUser.fullName}"/></h1><p>Your PIXORA workspace at a glance.</p></div></section>
<div class="container py-5">
    <div class="row g-3 mb-5"><c:forEach items="${stats}" var="s"><div class="col-6 col-lg-3"><div class="stat-card"><span><c:out value="${s.key}"/></span><strong><c:out value="${s.value}"/></strong></div></div></c:forEach></div>
    <div class="row g-4">
        <div class="col-lg-8"><div class="panel-card"><div class="d-flex justify-content-between align-items-center"><h4>Quick actions</h4><span class="badge text-bg-light">Role based</span></div><div class="quick-grid mt-3">
            <c:if test="${sessionScope.authUser.roleCode == 'CUSTOMER'}"><a href="${pageContext.request.contextPath}/photographers"><i class="bi bi-camera"></i><span>Find photographer</span></a><a href="${pageContext.request.contextPath}/bookings"><i class="bi bi-calendar2-check"></i><span>My bookings</span></a><a href="${pageContext.request.contextPath}/payments"><i class="bi bi-credit-card"></i><span>Payments</span></a><a href="${pageContext.request.contextPath}/galleries"><i class="bi bi-images"></i><span>My galleries</span></a></c:if>
            <c:if test="${sessionScope.authUser.roleCode == 'PHOTOGRAPHER'}"><a href="${pageContext.request.contextPath}/photographer/manage"><i class="bi bi-person-badge"></i><span>Studio profile</span></a><a href="${pageContext.request.contextPath}/bookings"><i class="bi bi-inbox"></i><span>Booking requests</span></a><a href="${pageContext.request.contextPath}/schedule"><i class="bi bi-calendar3"></i><span>Assignments</span></a><a href="${pageContext.request.contextPath}/galleries"><i class="bi bi-cloud-arrow-up"></i><span>Gallery delivery</span></a></c:if>
            <c:if test="${sessionScope.authUser.roleCode == 'EVENT_COORDINATOR'}"><a href="${pageContext.request.contextPath}/schedule?view=form"><i class="bi bi-calendar-plus"></i><span>Create schedule</span></a><a href="${pageContext.request.contextPath}/schedule"><i class="bi bi-diagram-3"></i><span>Assignments</span></a><a href="${pageContext.request.contextPath}/bookings"><i class="bi bi-journal-check"></i><span>Bookings</span></a></c:if>
            <c:if test="${sessionScope.authUser.roleCode == 'OPERATIONS_MANAGER'}"><a href="${pageContext.request.contextPath}/reports"><i class="bi bi-graph-up"></i><span>Reports</span></a><a href="${pageContext.request.contextPath}/payments"><i class="bi bi-cash-stack"></i><span>Payments & refunds</span></a><a href="${pageContext.request.contextPath}/schedule"><i class="bi bi-calendar-week"></i><span>Operations calendar</span></a></c:if>
            <c:if test="${sessionScope.authUser.roleCode == 'CUSTOMER_RELATIONS'}"><a href="${pageContext.request.contextPath}/complaints"><i class="bi bi-headset"></i><span>Complaint queue</span></a><a href="${pageContext.request.contextPath}/notifications"><i class="bi bi-bell"></i><span>Notifications</span></a></c:if>
            <c:if test="${sessionScope.authUser.roleCode == 'SYSTEM_ADMIN'}"><a href="${pageContext.request.contextPath}/admin"><i class="bi bi-shield-lock"></i><span>Administration</span></a><a href="${pageContext.request.contextPath}/reports"><i class="bi bi-bar-chart"></i><span>Reports</span></a><a href="${pageContext.request.contextPath}/complaints"><i class="bi bi-chat-square-text"></i><span>Moderation</span></a></c:if>
        </div></div></div>
        <div class="col-lg-4"><div class="panel-card"><div class="d-flex justify-content-between"><h4>Recent notifications</h4><a href="${pageContext.request.contextPath}/notifications">View all</a></div>
            <c:forEach items="${notifications}" var="n"><a class="notification-mini" href="${pageContext.request.contextPath}${n.linkUrl}"><strong><c:out value="${n.title}"/></strong><small><c:out value="${n.message}"/></small></a></c:forEach>
            <c:if test="${empty notifications}"><div class="empty-state small">Nothing new yet.</div></c:if>
        </div></div>
    </div>
</div>
<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
