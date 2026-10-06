<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib
        prefix="c"
        uri="http://java.sun.com/jsp/jstl/core" %>

<c:set
        var="pageTitle"
        value="Sign in"/>

<jsp:include
        page="/WEB-INF/views/common/header.jsp"/>


<section class="pixora-auth">

    <div class="container">

        <div class="pixora-auth-shell">

            <%-- ============================================
                 LEFT VISUAL PANEL
                 ============================================ --%>

            <aside class="pixora-auth-visual">

                <div class="pixora-auth-visual-overlay"></div>

                <div class="pixora-auth-visual-content">

                    <span class="auth-kicker">
                        PIXORA
                    </span>

                    <h1>
                        Your moments.<br>
                        Beautifully managed.
                    </h1>

                    <p>
                        Discover trusted photographers,
                        manage your event journey and
                        receive every memory in one
                        beautifully connected experience.
                    </p>

                    <div class="auth-feature-list">

                        <div>
                            <i class="bi bi-camera"></i>

                            <span>
                                Verified photographers
                            </span>
                        </div>

                        <div>
                            <i class="bi bi-calendar2-check"></i>

                            <span>
                                Real-time availability
                            </span>
                        </div>

                        <div>
                            <i class="bi bi-images"></i>

                            <span>
                                Secure digital galleries
                            </span>
                        </div>

                    </div>

                </div>

            </aside>


            <%-- ============================================
                 LOGIN PANEL
                 ============================================ --%>

            <div class="pixora-auth-panel">

                <div class="auth-heading">

                    <span class="eyebrow">
                        WELCOME BACK
                    </span>

                    <h2>
                        Sign in to PIXORA
                    </h2>

                    <p>
                        Continue your event photography
                        journey.
                    </p>

                </div>


                <c:if test="${param.blocked == '1'}">

                    <div class="alert alert-warning">

                        <i class="bi bi-exclamation-triangle me-2"></i>

                        Your account is not active.

                    </div>

                </c:if>


                <%-- ========================================
                     GOOGLE LOGIN
                     ======================================== --%>

                <a
                        href="${pageContext.request.contextPath}/auth/google"
                        class="google-auth-button">

                   <span class="google-auth-icon">

    <svg
            width="20"
            height="20"
            viewBox="0 0 24 24"
            aria-hidden="true">

        <path
                fill="#4285F4"
                d="M21.6 12.227c0-.709-.064-1.391-.182-2.045H12v3.868h5.382a4.6 4.6 0 0 1-1.996 3.018v2.509h3.232c1.891-1.741 2.982-4.309 2.982-7.35z"/>

        <path
                fill="#34A853"
                d="M12 22c2.7 0 4.964-.895 6.618-2.423l-3.232-2.509c-.895.6-2.041.955-3.386.955-2.605 0-4.809-1.759-5.6-4.123H3.059v2.591A9.999 9.999 0 0 0 12 22z"/>

        <path
                fill="#FBBC05"
                d="M6.4 13.9A6.01 6.01 0 0 1 6.086 12c0-.659.114-1.3.314-1.9V7.509H3.059A10.004 10.004 0 0 0 2 12c0 1.614.386 3.141 1.059 4.491L6.4 13.9z"/>

        <path
                fill="#EA4335"
                d="M12 5.977c1.468 0 2.786.505 3.823 1.495l2.868-2.868C16.959 2.991 14.695 2 12 2a9.999 9.999 0 0 0-8.941 5.509L6.4 10.1C7.191 7.736 9.395 5.977 12 5.977z"/>

    </svg>

</span>

                    <span>
                        Continue with Google
                    </span>

                </a>


                <div class="auth-divider">

                    <span>
                        or sign in with your PIXORA account
                    </span>

                </div>


                <%-- ========================================
                     NORMAL PIXORA LOGIN
                     ======================================== --%>

                <form
                        method="post"
                        action="${pageContext.request.contextPath}/login"
                        novalidate>


                    <div class="auth-field">

                        <label
                                class="form-label"
                                for="login">

                            Username or email

                        </label>

                        <div class="auth-input-wrap">

                            <i class="bi bi-person auth-input-icon"></i>

                            <input
                                    id="login"
                                    class="form-control auth-control"
                                    name="login"
                                    type="text"
                                    required
                                    autocomplete="username"
                                    placeholder="Enter username or email">

                        </div>

                    </div>


                    <div class="auth-field">

                        <label
                                class="form-label"
                                for="password">

                            Password

                        </label>

                        <div class="auth-password-wrap">

                            <i class="bi bi-lock auth-input-icon"></i>

                            <input
                                    id="password"
                                    class="form-control auth-control auth-password-input"
                                    type="password"
                                    name="password"
                                    required
                                    autocomplete="current-password"
                                    placeholder="Enter your password">

                            <button
                                    class="auth-password-toggle"
                                    type="button"
                                    data-toggle-password="#password"
                                    aria-label="Show or hide password">

                                <i class="bi bi-eye"></i>

                            </button>

                        </div>

                    </div>


                    <button
                            class="btn pixora-auth-primary w-100"
                            type="submit">

                        Sign in

                        <i class="bi bi-arrow-right ms-2"></i>

                    </button>

                </form>


                <div class="auth-register-link">

                    <span>
                        New to PIXORA?
                    </span>

                    <a
                            href="${pageContext.request.contextPath}/register">

                        Create an account

                    </a>

                </div>


                <p class="auth-security-note">

                    <i class="bi bi-shield-check"></i>

                    Secure authentication powered by
                    PIXORA and Google.

                </p>

            </div>

        </div>

    </div>

</section>


<jsp:include
        page="/WEB-INF/views/common/footer.jsp"/>