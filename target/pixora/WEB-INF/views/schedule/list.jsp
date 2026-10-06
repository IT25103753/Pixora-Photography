<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Event Schedule"/>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>
<section class="page-hero compact">
    <div class="container d-flex flex-wrap justify-content-between align-items-end gap-3">
        <div><span class="eyebrow">EVENT COORDINATION</span><h1>Schedule & assignments</h1><p>Confirmed events, photographer assignments and conflict-aware changes.</p></div>
        <c:if test="${sessionScope.authUser.roleCode == 'EVENT_COORDINATOR' || sessionScope.authUser.roleCode == 'OPERATIONS_MANAGER' || sessionScope.authUser.roleCode == 'SYSTEM_ADMIN'}"><a class="btn btn-warning" href="${pageContext.request.contextPath}/schedule?view=form">Create schedule</a></c:if>
    </div>
</section>
<div class="container py-5">
    <jsp:include page="/WEB-INF/views/common/messages.jsp"/>
    <div class="panel-card mb-4">
        <form class="row g-3 align-items-end" method="get" action="${pageContext.request.contextPath}/schedule">
            <div class="col-sm-4 col-lg-3"><label class="form-label">View</label><select class="form-select" name="period"><option value="all" ${period == 'all' ? 'selected' : ''}>All schedules</option><option value="day" ${period == 'day' ? 'selected' : ''}>Daily</option><option value="week" ${period == 'week' ? 'selected' : ''}>Weekly</option><option value="month" ${period == 'month' ? 'selected' : ''}>Monthly</option></select></div>
            <div class="col-sm-4 col-lg-3"><label class="form-label">Reference date</label><input class="form-control" type="date" name="date" value="${anchorDate}"></div>
            <div class="col-sm-4 col-lg-2"><button class="btn btn-dark w-100">Apply</button></div>
            <div class="col-lg-4 text-lg-end text-secondary small">Daily/weekly/monthly views use the selected reference date.</div>
        </form>
    </div>
    <div class="row g-3">
        <c:forEach items="${schedules}" var="s">
            <div class="col-md-6 col-xl-4"><div class="panel-card h-100">
                <div class="d-flex justify-content-between"><strong><c:out value="${s.bookingRef}"/></strong><span class="status-badge">${s.status}</span></div>
                <h5 class="mt-3">${s.eventDate}</h5>
                <p class="mb-1"><i class="bi bi-clock"></i> ${s.startTime} – ${s.endTime}</p>
                <p class="mb-1"><i class="bi bi-geo-alt"></i> <c:out value="${s.venue}"/></p>
                <p><i class="bi bi-camera"></i> <c:out value="${s.photographerName}"/></p>
                <c:if test="${sessionScope.authUser.roleCode == 'EVENT_COORDINATOR' || sessionScope.authUser.roleCode == 'OPERATIONS_MANAGER' || sessionScope.authUser.roleCode == 'SYSTEM_ADMIN'}"><a class="btn btn-sm btn-outline-dark" href="${pageContext.request.contextPath}/schedule?view=form&id=${s.scheduleId}">Edit / reassign</a></c:if>
            </div></div>
        </c:forEach>
        <c:if test="${empty schedules}"><div class="empty-state">No schedules found for this view.</div></c:if>
    </div>
</div>
<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
