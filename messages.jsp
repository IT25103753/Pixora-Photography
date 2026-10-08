<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<div class="container pt-3">
    <c:if test="${not empty requestScope.error}">
        <div class="alert alert-danger alert-dismissible fade show"><c:out value="${requestScope.error}"/><button class="btn-close" data-bs-dismiss="alert"></button></div>
    </c:if>
    <c:if test="${not empty sessionScope.flashMessage}">
        <div class="alert alert-${empty sessionScope.flashType ? 'info' : sessionScope.flashType} alert-dismissible fade show">
            <c:out value="${sessionScope.flashMessage}"/><button class="btn-close" data-bs-dismiss="alert"></button>
        </div>
        <c:remove var="flashMessage" scope="session"/>
        <c:remove var="flashType" scope="session"/>
    </c:if>
</div>
