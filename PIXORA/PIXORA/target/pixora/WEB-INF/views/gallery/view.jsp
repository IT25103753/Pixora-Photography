<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="${gallery.title}"/>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<section class="page-hero compact">
    <div class="container">
        <span class="eyebrow">SECURE GALLERY</span>
        <h1><c:out value="${gallery.title}"/></h1>
        <p>Status <span class="status-badge">${gallery.status}</span></p>
    </div>
</section>

<div class="container py-5">
    <jsp:include page="/WEB-INF/views/common/messages.jsp"/>
    <c:if test="${not empty albums}">
        <div class="d-flex flex-wrap gap-2 mb-4" aria-label="Gallery albums">
            <span class="small text-secondary align-self-center me-1">Albums:</span>
            <c:forEach items="${albums}" var="a">
                <span class="badge rounded-pill text-bg-light border"><c:out value="${a.name}"/></span>
            </c:forEach>
        </div>
    </c:if>

    <div class="masonry-grid">
        <c:forEach items="${photos}" var="p">
            <article class="photo-tile">
                <img src="${pageContext.request.contextPath}/media/photo?id=${p.photoId}" alt="<c:out value='${empty p.caption ? "Event photograph" : p.caption}'/>">
                <div class="p-3 d-flex justify-content-between align-items-center gap-3">
                    <div>
                        <c:if test="${not empty p.albumName}"><span class="badge text-bg-light border mb-2"><c:out value="${p.albumName}"/></span></c:if>
                        <div><c:out value="${empty p.caption ? 'Event photograph' : p.caption}"/></div>
                    </div>
                    <div class="d-flex gap-2">
                        <a class="btn btn-sm btn-outline-dark" href="${pageContext.request.contextPath}/media/photo?id=${p.photoId}&download=1" aria-label="Download photo"><i class="bi bi-download"></i></a>
                        <c:if test="${sessionScope.authUser.roleCode == 'CUSTOMER'}">
                            <form method="post" action="${pageContext.request.contextPath}/galleries">
                                <input type="hidden" name="action" value="favorite">
                                <input type="hidden" name="photoId" value="${p.photoId}">
                                <input type="hidden" name="galleryId" value="${gallery.galleryId}">
                                <button class="btn btn-sm ${p.favorite ? 'btn-warning' : 'btn-outline-secondary'}" aria-label="Toggle favorite"><i class="bi bi-heart${p.favorite ? '-fill' : ''}"></i></button>
                            </form>
                        </c:if>
                    </div>
                </div>
            </article>
        </c:forEach>
    </div>
    <c:if test="${empty photos}"><div class="empty-state">No photos have been delivered yet.</div></c:if>
</div>
<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
