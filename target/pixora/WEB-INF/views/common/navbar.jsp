<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<nav class="navbar navbar-expand-lg pixora-nav sticky-top">
    <div class="container">
        <a class="navbar-brand d-flex align-items-center gap-2" href="${pageContext.request.contextPath}/home">
            <img src="${pageContext.request.contextPath}/assets/images/logo.svg" alt="PIXORA" width="38" height="38">
            <span>PIXORA</span>
        </a>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#mainNav"><span class="navbar-toggler-icon"></span></button>
        <div class="collapse navbar-collapse" id="mainNav">
            <ul class="navbar-nav me-auto">
                <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/photographers">Photographers</a></li>
                <c:if test="${not empty sessionScope.authUser}">
                    <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/dashboard">Dashboard</a></li>
                    <c:if test="${sessionScope.authUser.roleCode == 'CUSTOMER' || sessionScope.authUser.roleCode == 'PHOTOGRAPHER' || sessionScope.authUser.roleCode == 'EVENT_COORDINATOR' || sessionScope.authUser.roleCode == 'OPERATIONS_MANAGER' || sessionScope.authUser.roleCode == 'SYSTEM_ADMIN'}">
                        <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/bookings">Bookings</a></li>
                    </c:if>
                    <c:if test="${sessionScope.authUser.roleCode != 'CUSTOMER_RELATIONS'}">
                        <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/schedule">Schedule</a></li>
                    </c:if>
                    <c:if test="${sessionScope.authUser.roleCode == 'CUSTOMER' || sessionScope.authUser.roleCode == 'PHOTOGRAPHER' || sessionScope.authUser.roleCode == 'OPERATIONS_MANAGER' || sessionScope.authUser.roleCode == 'SYSTEM_ADMIN'}">
                        <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/galleries">Galleries</a></li>
                    </c:if>
                    <c:if test="${sessionScope.authUser.roleCode == 'CUSTOMER' || sessionScope.authUser.roleCode == 'OPERATIONS_MANAGER' || sessionScope.authUser.roleCode == 'SYSTEM_ADMIN'}">
                        <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/payments">Payments</a></li>
                    </c:if>
                    <c:if test="${sessionScope.authUser.roleCode == 'CUSTOMER' || sessionScope.authUser.roleCode == 'CUSTOMER_RELATIONS' || sessionScope.authUser.roleCode == 'SYSTEM_ADMIN'}">
                        <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/complaints">Support</a></li>
                    </c:if>
                </c:if>
            </ul>
            <div class="d-flex align-items-center gap-2">
                <c:choose>
                    <c:when test="${not empty sessionScope.authUser}">
                        <a class="btn btn-sm btn-outline-light position-relative" href="${pageContext.request.contextPath}/notifications" aria-label="Notifications">
                            <i class="bi bi-bell"></i>
                        </a>
                        <div class="dropdown">
                            <button class="btn btn-sm btn-light dropdown-toggle" data-bs-toggle="dropdown">
                                <i class="bi bi-person-circle me-1"></i><c:out value="${sessionScope.authUser.fullName}"/>
                            </button>
                            <ul class="dropdown-menu dropdown-menu-end">
                                <li><a class="dropdown-item" href="${pageContext.request.contextPath}/profile">My Profile</a></li>
                                <c:if test="${sessionScope.authUser.roleCode == 'PHOTOGRAPHER'}"><li><a class="dropdown-item" href="${pageContext.request.contextPath}/photographer/manage">Photographer Studio</a></li></c:if>
                                <c:if test="${sessionScope.authUser.roleCode == 'SYSTEM_ADMIN'}"><li><a class="dropdown-item" href="${pageContext.request.contextPath}/admin">Administration</a></li></c:if>
                                <c:if test="${sessionScope.authUser.roleCode == 'SYSTEM_ADMIN' || sessionScope.authUser.roleCode == 'OPERATIONS_MANAGER'}"><li><a class="dropdown-item" href="${pageContext.request.contextPath}/reports">Reports</a></li></c:if>
                                <li><hr class="dropdown-divider"></li>
                                <a class="dropdown-item signout-link"
                                   href="${pageContext.request.contextPath}/logout">
                                    Sign out
                                </a>
                            </ul>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <a class="btn btn-sm btn-outline-light" href="${pageContext.request.contextPath}/login">Sign in</a>
                        <a class="btn btn-sm btn-warning" href="${pageContext.request.contextPath}/register">Join PIXORA</a>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </div>
</nav>
