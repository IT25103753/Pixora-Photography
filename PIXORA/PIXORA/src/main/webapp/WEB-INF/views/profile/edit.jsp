<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="My profile"/>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>
<div class="container py-5 narrow"><div class="panel-card"><span class="eyebrow">ACCOUNT</span><h2 class="mt-2">My profile</h2><p class="text-muted">Keep your account contact information current.</p>
<form method="post" action="${pageContext.request.contextPath}/profile" class="row g-3">
<div class="col-md-6"><label class="form-label">Full name</label><input class="form-control" name="fullName" value="<c:out value='${user.fullName}'/>" required></div>
<div class="col-md-6"><label class="form-label">Username</label><input class="form-control" value="<c:out value='${user.username}'/>" disabled></div>
<div class="col-md-6"><label class="form-label">Email</label><input class="form-control" type="email" name="email" value="<c:out value='${user.email}'/>" required></div>
<div class="col-md-6"><label class="form-label">Phone</label><input class="form-control" name="phone" value="<c:out value='${user.phone}'/>"></div>
<div class="col-12"><span class="badge bg-dark">${user.roleCode}</span> <span class="badge bg-success">${user.status}</span></div>
<div class="col-12"><button class="btn btn-warning">Save profile</button></div></form></div></div>
<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
