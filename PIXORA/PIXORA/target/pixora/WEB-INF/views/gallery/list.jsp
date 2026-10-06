<%@ page contentType="text/html;charset=UTF-8" language="java" %><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Galleries"/><jsp:include page="/WEB-INF/views/common/header.jsp"/>
<section class="page-hero compact"><div class="container d-flex justify-content-between align-items-end"><div><span class="eyebrow">PHOTO DELIVERY</span><h1>Galleries</h1><p>Secure booking-linked photo delivery and status tracking.</p></div><c:if test="${sessionScope.authUser.roleCode == 'PHOTOGRAPHER'}"><a class="btn btn-warning" href="${pageContext.request.contextPath}/galleries?view=manage">Create gallery</a></c:if></div></section>
<div class="container py-5"><div class="row g-4"><c:forEach items="${galleries}" var="g"><div class="col-md-6 col-xl-4"><div class="gallery-card"><c:choose>
    <c:when test="${not empty g.coverPhotoId}">
        <img src="${pageContext.request.contextPath}/media/photo?id=${g.coverPhotoId}"
             onerror="this.onerror=null;this.src='${pageContext.request.contextPath}/assets/images/gallery-placeholder.svg';"
             alt="<c:out value='${g.title}'/>">
    </c:when>
    <c:otherwise>
        <img src="${pageContext.request.contextPath}/media/profile?userId=${g.photographerUserId}"
             onerror="this.onerror=null;this.src='${pageContext.request.contextPath}/assets/images/gallery-placeholder.svg';"
             alt="<c:out value='${g.title}'/>">
    </c:otherwise>
</c:choose><div class="p-4"><div class="d-flex justify-content-between"><strong><c:out value="${g.title}"/></strong><span class="status-badge">${g.status}</span></div><p class="text-muted small mt-2">Booking #${g.bookingId}</p><a class="btn btn-sm btn-dark" href="${pageContext.request.contextPath}/galleries?view=detail&id=${g.galleryId}">Open gallery</a><c:if test="${sessionScope.authUser.roleCode == 'PHOTOGRAPHER'}"> <a class="btn btn-sm btn-outline-dark" href="${pageContext.request.contextPath}/galleries?view=manage&id=${g.galleryId}">Manage</a></c:if></div></div></div></c:forEach><c:if test="${empty galleries}"><div class="empty-state">No galleries yet.</div></c:if></div></div>
<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
