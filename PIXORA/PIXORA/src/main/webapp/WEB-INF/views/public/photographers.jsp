<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Find photographers"/>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>
<section class="page-hero"><div class="container"><span class="eyebrow">DISCOVER TALENT</span><h1>Find a photographer</h1><p>Search approved PIXORA photographers by specialty, price, rating and date.</p></div></section>
<div class="container py-5">
    <form class="filter-card row g-3 mb-4" method="get">
        <div class="col-lg-3"><label class="form-label">Specialty</label><input class="form-control" name="specialty" value="${param.specialty}" placeholder="Wedding, portrait..."></div>
        <div class="col-lg-2"><label class="form-label">Min price</label><input class="form-control" type="number" step="0.01" name="minPrice" value="${param.minPrice}"></div>
        <div class="col-lg-2"><label class="form-label">Max price</label><input class="form-control" type="number" step="0.01" name="maxPrice" value="${param.maxPrice}"></div>
        <div class="col-lg-2"><label class="form-label">Date</label><input class="form-control" type="date" name="date" value="${param.date}"></div>
        <div class="col-lg-2"><label class="form-label">Min rating</label><select class="form-select" name="rating"><option value="">Any</option><option value="4">4+</option><option value="3">3+</option></select></div>
        <div class="col-lg-1 d-grid align-items-end"><button class="btn btn-warning mt-auto"><i class="bi bi-search"></i></button></div>
    </form>
    <div class="row g-4">
        <c:forEach items="${photographers}" var="p"><div class="col-md-6 col-xl-4"><div class="photographer-card h-100">
            <img src="${pageContext.request.contextPath}/media/profile?userId=${p.userId}" onerror="this.onerror=null;this.src='${pageContext.request.contextPath}/assets/images/photographer-placeholder.svg';" class="card-cover" alt="">
            <div class="p-4"><h5><c:out value="${p.fullName}"/></h5><p class="text-muted"><c:out value="${p.email}"/></p><a class="btn btn-dark" href="${pageContext.request.contextPath}/photographers?id=${p.userId}">View profile</a></div>
        </div></div></c:forEach>
        <c:if test="${empty photographers}"><div class="empty-state">No approved photographers match these filters.</div></c:if>
    </div>
</div>
<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
