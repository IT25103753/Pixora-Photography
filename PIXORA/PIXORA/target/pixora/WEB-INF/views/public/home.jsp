<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Event Photography, beautifully managed"/>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<section class="home-hero">
    <div class="hero-grid">
        <div class="hero-copy">
            <span class="eyebrow">EVENT PHOTOGRAPHY · SIMPLIFIED</span>
            <h1>Every celebration, held onto properly.</h1>
            <p class="lead">PIXORA connects you with approved event photographers, real availability, secure galleries and traceable payments — for weddings, birthdays, corporate events and more.</p>
            <div class="d-flex flex-wrap gap-2 mt-2">
                <a class="btn btn-warning btn-lg" href="${pageContext.request.contextPath}/photographers"><i class="bi bi-search me-2"></i>Find a photographer</a>
                <c:if test="${empty sessionScope.authUser}"><a class="btn btn-outline-dark btn-lg" href="${pageContext.request.contextPath}/register">Create account</a></c:if>
            </div>
            <div class="hero-trust"><span><i class="bi bi-shield-check"></i>Secure galleries</span><span><i class="bi bi-calendar-check"></i>Availability checks</span><span><i class="bi bi-receipt"></i>Traceable payments</span></div>
        </div>
        <div class="hero-collage">
            <div class="tile"><img src="${pageContext.request.contextPath}/assets/images/portfolio/wedding.jpg" alt="Wedding photography"></div>
            <div class="tile"><img src="${pageContext.request.contextPath}/assets/images/portfolio/graduation.jpg" alt="Graduation photography"></div>
            <div class="tile"><img src="${pageContext.request.contextPath}/assets/images/portfolio/outdoor.jpg" alt="Outdoor photoshoot"></div>
            <div class="tile"><img src="${pageContext.request.contextPath}/assets/images/portfolio/birthday.jpg" alt="Birthday celebration photography"></div>
            <div class="tile"><img src="${pageContext.request.contextPath}/assets/images/portfolio/portrait.jpg" alt="Portrait session"></div>
        </div>
    </div>
</section>

<section class="container category-section">
    <div class="category-head">
        <h2>What we shoot</h2>
        <span>Scroll to explore &rarr;</span>
    </div>
    <div class="category-rail">
        <a class="category-tile" href="${pageContext.request.contextPath}/photographers?category=wedding">
            <div class="cat-img"><img src="${pageContext.request.contextPath}/assets/images/portfolio/wedding-2.jpg" alt="Wedding photography"></div>
            <div class="cat-name">Weddings<span class="cat-count">Ceremonies &amp; receptions</span></div>
        </a>
        <a class="category-tile" href="${pageContext.request.contextPath}/photographers?category=birthday">
            <div class="cat-img"><img src="${pageContext.request.contextPath}/assets/images/portfolio/birthday.jpg" alt="Birthday photography"></div>
            <div class="cat-name">Birthdays<span class="cat-count">All ages, every theme</span></div>
        </a>
        <a class="category-tile" href="${pageContext.request.contextPath}/photographers?category=corporate">
            <div class="cat-img"><img src="${pageContext.request.contextPath}/assets/images/portfolio/corporate.jpg" alt="Corporate event photography"></div>
            <div class="cat-name">Corporate &amp; Events<span class="cat-count">Conferences, launches</span></div>
        </a>
        <a class="category-tile" href="${pageContext.request.contextPath}/photographers?category=portrait">
            <div class="cat-img"><img src="${pageContext.request.contextPath}/assets/images/portfolio/portrait.jpg" alt="Portrait session"></div>
            <div class="cat-name">Portraits<span class="cat-count">Studio &amp; on-location</span></div>
        </a>
        <a class="category-tile" href="${pageContext.request.contextPath}/photographers?category=outdoor">
            <div class="cat-img"><img src="${pageContext.request.contextPath}/assets/images/portfolio/outdoor.jpg" alt="Outdoor photoshoot"></div>
            <div class="cat-name">Outdoor<span class="cat-count">Nature &amp; lifestyle</span></div>
        </a>
        <a class="category-tile" href="${pageContext.request.contextPath}/photographers?category=graduation">
            <div class="cat-img"><img src="${pageContext.request.contextPath}/assets/images/portfolio/graduation.jpg" alt="Graduation photography"></div>
            <div class="cat-name">Graduations<span class="cat-count">Convocation day</span></div>
        </a>
    </div>
</section>

<section class="container py-5">
    <div class="section-heading reveal"><span>FEATURED</span><h2>Photographers ready for your next event</h2><p>Only approved photographer profiles are shown to customers.</p></div>
    <div class="row g-4">
        <c:forEach items="${featuredPhotographers}" var="p" varStatus="loop">
            <div class="col-md-4"><div class="photographer-card h-100 reveal" style="transition-delay:${loop.index * 0.1}s">
                <img src="${pageContext.request.contextPath}/media/profile?userId=${p.userId}" onerror="this.onerror=null;this.src='${pageContext.request.contextPath}/assets/images/photographer-placeholder.svg';" alt="" class="card-cover">
                <div class="p-4"><h5><c:out value="${p.fullName}"/></h5><p class="text-muted mb-3">Professional event photographer</p><a class="btn btn-outline-dark" href="${pageContext.request.contextPath}/photographers?id=${p.userId}">View profile</a></div>
            </div></div>
        </c:forEach>
        <c:if test="${empty featuredPhotographers}"><div class="col-12"><div class="empty-state">Approved photographers will appear here after the administrator approves profiles.</div></div></c:if>
    </div>
</section>

<section class="how-section py-5">
    <div class="container">

        <div class="section-heading text-center reveal how-heading">
            <span>HOW IT WORKS</span>
            <h2>One connected event journey</h2>
        </div>

        <div class="row g-3 justify-content-center">

            <div class="col">
                <div class="step-card reveal">
                    <div class="step-icon">
                        <i class="bi bi-search"></i>
                    </div>
                    <strong>Discover</strong>
                </div>
            </div>

            <div class="col">
                <div class="step-card reveal" style="transition-delay:.08s">
                    <div class="step-icon">
                        <i class="bi bi-send"></i>
                    </div>
                    <strong>Request</strong>
                </div>
            </div>

            <div class="col">
                <div class="step-card reveal" style="transition-delay:.16s">
                    <div class="step-icon">
                        <i class="bi bi-check-circle"></i>
                    </div>
                    <strong>Confirm</strong>
                </div>
            </div>

            <div class="col">
                <div class="step-card reveal" style="transition-delay:.24s">
                    <div class="step-icon">
                        <i class="bi bi-calendar-check"></i>
                    </div>
                    <strong>Schedule</strong>
                </div>
            </div>

            <div class="col">
                <div class="step-card reveal" style="transition-delay:.32s">
                    <div class="step-icon">
                        <i class="bi bi-credit-card"></i>
                    </div>
                    <strong>Pay</strong>
                </div>
            </div>

            <div class="col">
                <div class="step-card reveal" style="transition-delay:.40s">
                    <div class="step-icon">
                        <i class="bi bi-images"></i>
                    </div>
                    <strong>Receive</strong>
                </div>
            </div>

            <div class="col">
                <div class="step-card reveal" style="transition-delay:.48s">
                    <div class="step-icon">
                        <i class="bi bi-star"></i>
                    </div>
                    <strong>Review</strong>
                </div>
            </div>

        </div>

    </div>
</section>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
