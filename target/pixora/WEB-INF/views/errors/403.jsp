<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Access denied"/><jsp:include page="/WEB-INF/views/common/header.jsp"/>
<div class="container py-5 narrow"><div class="panel-card text-center py-5"><div class="error-code">403</div><h1>Access denied</h1><p class="text-muted">You do not have permission to access this PIXORA area.</p><a class="btn btn-warning" href="${pageContext.request.contextPath}/home">Return home</a></div></div>
<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
