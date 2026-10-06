<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="${photographer.fullName}"/>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>
<div class="container py-5">
    <div class="profile-hero p-4 p-lg-5 mb-4">
        <div class="row align-items-center g-4"><div class="col-md-3 text-center"><c:choose><c:when test="${not empty profile.profileImagePath}"><img class="profile-avatar" src="${pageContext.request.contextPath}/media/profile?userId=${photographer.userId}" alt="<c:out value='${photographer.fullName}'/>"></c:when><c:otherwise><img class="profile-avatar" src="${pageContext.request.contextPath}/assets/images/photographer-placeholder.svg" alt=""></c:otherwise></c:choose></div>
        <div class="col-md-9"><span class="badge bg-success-subtle text-success mb-2">Approved Photographer</span><h1><c:out value="${photographer.fullName}"/></h1>
            <p class="lead mb-2"><c:out value="${profile.specialty}"/> · <c:out value="${profile.location}"/></p><p class="text-muted"><c:out value="${profile.bio}"/></p>
            <div class="rating"><i class="bi bi-star-fill"></i> ${profile.averageRating}</div>
        </div></div>
    </div>
    <div class="row g-4"><div class="col-lg-8">
        <h3>Portfolio</h3><div class="row g-3 mb-5"><c:forEach items="${portfolio}" var="i"><div class="col-md-6"><div class="portfolio-card"><c:choose><c:when test="${not empty i.imagePath}"><img src="${pageContext.request.contextPath}/media/portfolio?id=${i.portfolioId}" alt="<c:out value='${i.title}'/>"></c:when><c:otherwise><img src="${pageContext.request.contextPath}/assets/images/photographer-placeholder.svg" alt=""></c:otherwise></c:choose><div class="p-3"><strong><c:out value="${i.title}"/></strong><p class="small text-muted mb-0"><c:out value="${i.description}"/></p></div></div></div></c:forEach></div>
        <h3>Customer reviews</h3><c:forEach items="${reviews}" var="r"><div class="review-card"><div class="rating">${r.rating}/5</div><p class="mb-0"><c:out value="${r.comment}"/></p></div></c:forEach><c:if test="${empty reviews}"><p class="text-muted">No published reviews yet.</p></c:if>
    </div><div class="col-lg-4"><div class="sticky-card"><h4>Packages</h4><c:forEach items="${packages}" var="pkg"><div class="package-mini"><div><strong><c:out value="${pkg.name}"/></strong><small>${pkg.durationHours} hours</small></div><span>LKR ${pkg.price}</span></div></c:forEach>
        <c:choose><c:when test="${sessionScope.authUser.roleCode == 'CUSTOMER'}"><a class="btn btn-warning w-100 mt-3" href="${pageContext.request.contextPath}/bookings?view=new&photographerId=${photographer.userId}">Request booking</a></c:when><c:when test="${empty sessionScope.authUser}"><a class="btn btn-warning w-100 mt-3" href="${pageContext.request.contextPath}/login">Sign in to book</a></c:when></c:choose>
    </div></div></div>
</div>
<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
