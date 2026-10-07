<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="${complaint.complaintRef}"/>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>
<div class="container py-5">
    <jsp:include page="/WEB-INF/views/common/messages.jsp"/>
    <div class="row g-4">
        <div class="col-lg-8">
            <div class="panel-card">
                <div class="d-flex justify-content-between gap-3">
                    <div><span class="eyebrow">COMPLAINT</span><h2><c:out value="${complaint.complaintRef}"/></h2></div>
                    <span class="status-badge">${complaint.status}</span>
                </div>
                <hr>
                <p><strong>Category:</strong> <c:out value="${complaint.category}"/></p>
                <p><strong>Priority:</strong> <c:out value="${complaint.priority}"/></p>
                <p><c:out value="${complaint.description}"/></p>
                <c:if test="${not empty complaint.evidencePath && (sessionScope.authUser.roleCode == 'CUSTOMER' || sessionScope.authUser.roleCode == 'CUSTOMER_RELATIONS' || sessionScope.authUser.roleCode == 'SYSTEM_ADMIN')}">
                    <a class="btn btn-sm btn-outline-dark" href="${pageContext.request.contextPath}/media/evidence?id=${complaint.complaintId}&download=1"><i class="bi bi-paperclip me-1"></i>Download supporting evidence</a>
                </c:if>
                <c:if test="${not empty complaint.resolution}"><div class="alert alert-success mt-3"><strong>Resolution:</strong> <c:out value="${complaint.resolution}"/></div></c:if>
            </div>
            <div class="panel-card mt-4">
                <h4>Communication history</h4>
                <c:forEach items="${actions}" var="a"><div class="timeline-item"><strong><c:out value="${a.actorName}"/></strong><small>${a.createdAt}</small><p><c:out value="${a.actionText}"/></p></div></c:forEach>
                <c:if test="${empty actions}"><p class="text-muted">No staff actions recorded yet.</p></c:if>
            </div>
        </div>
        <div class="col-lg-4">
            <c:if test="${sessionScope.authUser.roleCode == 'CUSTOMER_RELATIONS' || sessionScope.authUser.roleCode == 'SYSTEM_ADMIN'}">
                <div class="panel-card">
                    <h4>Support workflow</h4>
                    <form method="post" action="${pageContext.request.contextPath}/complaints">
                        <input type="hidden" name="action" value="workflow"><input type="hidden" name="id" value="${complaint.complaintId}">
                        <label class="form-label">Assigned staff</label>
                        <select class="form-select mb-3" name="assignedTo"><option value="">Unassigned</option><c:forEach items="${supportUsers}" var="s"><option value="${s.userId}" ${complaint.assignedToUserId == s.userId ? 'selected' : ''}><c:out value="${s.fullName}"/></option></c:forEach></select>
                        <label class="form-label">Priority</label>
                        <select class="form-select mb-3" name="priority">
                            <option value="LOW" ${complaint.priority == 'LOW' ? 'selected' : ''}>LOW</option>
                            <option value="MEDIUM" ${complaint.priority == 'MEDIUM' ? 'selected' : ''}>MEDIUM</option>
                            <option value="HIGH" ${complaint.priority == 'HIGH' ? 'selected' : ''}>HIGH</option>
                            <option value="URGENT" ${complaint.priority == 'URGENT' ? 'selected' : ''}>URGENT</option>
                        </select>
                        <label class="form-label">Status</label>
                        <select class="form-select mb-3" name="status">
                            <option value="SUBMITTED" ${complaint.status == 'SUBMITTED' ? 'selected' : ''}>SUBMITTED</option>
                            <option value="UNDER_REVIEW" ${complaint.status == 'UNDER_REVIEW' ? 'selected' : ''}>UNDER REVIEW</option>
                            <option value="IN_PROGRESS" ${complaint.status == 'IN_PROGRESS' ? 'selected' : ''}>IN PROGRESS</option>
                            <option value="RESOLVED" ${complaint.status == 'RESOLVED' ? 'selected' : ''}>RESOLVED</option>
                            <option value="CLOSED" ${complaint.status == 'CLOSED' ? 'selected' : ''}>CLOSED</option>
                        </select>
                        <label class="form-label">Action note</label><textarea class="form-control mb-3" name="actionText" maxlength="1000" required></textarea>
                        <label class="form-label">Resolution</label><textarea class="form-control mb-3" name="resolution" maxlength="1000"><c:out value="${complaint.resolution}"/></textarea>
                        <button class="btn btn-warning w-100">Update complaint</button>
                    </form>
                </div>
            </c:if>
        </div>
    </div>
</div>
<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
