<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Manage Gallery"/>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<div class="container py-5">
    <jsp:include page="/WEB-INF/views/common/messages.jsp"/>
    <c:choose>
        <c:when test="${empty gallery}">
            <div class="panel-card narrow mx-auto">
                <span class="eyebrow">NEW GALLERY</span>
                <h2 class="mt-2">Create from a completed event</h2>
                <p class="text-secondary">A secure gallery can be created only for completed work assigned to you.</p>
                <form method="post" action="${pageContext.request.contextPath}/galleries" class="row g-3">
                    <input type="hidden" name="action" value="create">
                    <div class="col-12">
                        <label class="form-label">Completed booking</label>
                        <select class="form-select" name="bookingId" required>
                            <option value="">Select booking</option>
                            <c:forEach items="${eligibleBookings}" var="b">
                                <option value="${b.bookingId}"><c:out value="${b.bookingRef}"/> — <c:out value="${b.eventType}"/></option>
                            </c:forEach>
                        </select>
                        <c:if test="${empty eligibleBookings}">
                            <div class="form-text">No completed, un-galleried events are currently assigned to you.</div>
                        </c:if>
                    </div>
                    <div class="col-12">
                        <label class="form-label">Gallery title</label>
                        <input class="form-control" name="title" maxlength="180" required placeholder="e.g. Perera Wedding Collection">
                    </div>
                    <div class="col-12"><button class="btn btn-warning" ${empty eligibleBookings ? 'disabled' : ''}>Create gallery</button></div>
                </form>
            </div>
        </c:when>

        <c:otherwise>
            <div class="d-flex flex-wrap justify-content-between align-items-center gap-3 mb-4">
                <div>
                    <span class="eyebrow">GALLERY STUDIO</span>
                    <h1 class="mb-1"><c:out value="${gallery.title}"/></h1>
                    <span class="text-secondary">Booking #${gallery.bookingId}</span>
                </div>
                <span class="status-badge">${gallery.status}</span>
            </div>

            <div class="row g-4">
                <div class="col-lg-4">
                    <div class="panel-card mb-4">
                        <h4>Create album</h4>
                        <p class="small text-secondary">Organize delivered photos into sections such as Ceremony, Reception, or Highlights.</p>
                        <form method="post" action="${pageContext.request.contextPath}/galleries">
                            <input type="hidden" name="action" value="album-create">
                            <input type="hidden" name="galleryId" value="${gallery.galleryId}">
                            <div class="mb-3"><input class="form-control" name="name" maxlength="160" required placeholder="Album name"></div>
                            <div class="mb-3"><textarea class="form-control" name="description" maxlength="600" rows="2" placeholder="Optional description"></textarea></div>
                            <button class="btn btn-outline-dark w-100">Add album</button>
                        </form>
                    </div>

                    <div class="panel-card mb-4">
                        <h4>Upload photo</h4>
                        <form method="post" enctype="multipart/form-data" action="${pageContext.request.contextPath}/galleries">
                            <input type="hidden" name="action" value="upload">
                            <input type="hidden" name="galleryId" value="${gallery.galleryId}">
                            <div class="mb-3">
                                <label class="form-label">Album</label>
                                <select class="form-select" name="albumId">
                                    <option value="">Uncategorized</option>
                                    <c:forEach items="${albums}" var="a">
                                        <c:if test="${a.active}"><option value="${a.albumId}"><c:out value="${a.name}"/></option></c:if>
                                    </c:forEach>
                                </select>
                            </div>
                            <div class="mb-3">
                                <label class="form-label">Image</label>
                                <input class="form-control" type="file" name="image" accept=".jpg,.jpeg,.png,.webp" required>
                                <div class="form-text">JPG, PNG, or WebP. Server-side size/type validation also applies.</div>
                            </div>
                            <div class="mb-3"><input class="form-control" name="caption" maxlength="300" placeholder="Caption"></div>
                            <button class="btn btn-dark w-100">Upload</button>
                        </form>
                    </div>

                    <div class="panel-card mb-4">
                        <h4>Delivery status</h4>
                        <form method="post" action="${pageContext.request.contextPath}/galleries">
                            <input type="hidden" name="action" value="status">
                            <input type="hidden" name="galleryId" value="${gallery.galleryId}">
                            <select class="form-select mb-3" name="status">
                                <option value="UPLOADING" ${gallery.status == 'UPLOADING' ? 'selected' : ''}>Uploading</option>
                                <option value="PROCESSING" ${gallery.status == 'PROCESSING' ? 'selected' : ''}>Processing</option>
                                <option value="READY_FOR_VIEWING" ${gallery.status == 'READY_FOR_VIEWING' ? 'selected' : ''}>Ready for Viewing</option>
                                <option value="DELIVERED" ${gallery.status == 'DELIVERED' ? 'selected' : ''}>Delivered</option>
                            </select>
                            <button class="btn btn-warning w-100">Update status</button>
                        </form>
                    </div>

                    <div class="panel-card">
                        <h4>Albums</h4>
                        <c:forEach items="${albums}" var="a">
                            <div class="d-flex justify-content-between gap-2 py-2 border-bottom">
                                <div>
                                    <strong><c:out value="${a.name}"/></strong>
                                    <c:if test="${!a.active}"><span class="badge text-bg-secondary ms-1">Archived</span></c:if>
                                    <c:if test="${not empty a.description}"><div class="small text-secondary"><c:out value="${a.description}"/></div></c:if>
                                </div>
                                <c:if test="${a.active}">
                                    <form method="post" action="${pageContext.request.contextPath}/galleries" data-confirm="Archive this album? Photos will remain in the gallery.">
                                        <input type="hidden" name="action" value="album-archive">
                                        <input type="hidden" name="galleryId" value="${gallery.galleryId}">
                                        <input type="hidden" name="albumId" value="${a.albumId}">
                                        <button class="btn btn-sm btn-outline-secondary">Archive</button>
                                    </form>
                                </c:if>
                            </div>
                        </c:forEach>
                        <c:if test="${empty albums}"><div class="small text-secondary">No albums yet.</div></c:if>
                    </div>
                </div>

                <div class="col-lg-8">
                    <div class="d-flex justify-content-between align-items-center mb-3">
                        <h3 class="mb-0">Photos</h3><span class="text-secondary">${photos.size()} item(s)</span>
                    </div>
                    <div class="row g-3">
                        <c:forEach items="${photos}" var="p">
                            <div class="col-md-6">
                                <div class="photo-tile h-100">
                                    <img src="${pageContext.request.contextPath}/media/photo?id=${p.photoId}" alt="<c:out value='${p.caption}'/>">
                                    <div class="p-3">
                                        <c:if test="${not empty p.albumName}"><span class="badge text-bg-light border mb-2"><c:out value="${p.albumName}"/></span></c:if>
                                        <p class="mb-3"><c:out value="${empty p.caption ? 'Untitled photo' : p.caption}"/></p>
                                        <form method="post" action="${pageContext.request.contextPath}/galleries" data-confirm="Remove this photo?">
                                            <input type="hidden" name="action" value="photo-delete">
                                            <input type="hidden" name="photoId" value="${p.photoId}">
                                            <input type="hidden" name="galleryId" value="${gallery.galleryId}">
                                            <button class="btn btn-sm btn-outline-danger">Remove</button>
                                        </form>
                                    </div>
                                </div>
                            </div>
                        </c:forEach>
                        <c:if test="${empty photos}"><div class="empty-state">Upload your first photo.</div></c:if>
                    </div>
                </div>
            </div>
        </c:otherwise>
    </c:choose>
</div>
<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
