<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Page not found"/><jsp:include page="/WEB-INF/views/common/header.jsp"/>
<div class="container py-5 narrow"><div class="panel-card text-center py-5"><div class="error-code">404</div><h1>Page not found</h1><p class="text-muted">The requested PIXORA page or record could not be found.</p><a class="btn btn-warning" href="${pageContext.request.contextPath}/home">Return home</a></div></div>
<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
