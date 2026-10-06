<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib
        prefix="c"
        uri="http://java.sun.com/jsp/jstl/core" %>

<c:set
        var="pageTitle"
        value="Create account"/>

<jsp:include
        page="/WEB-INF/views/common/header.jsp"/>


<section class="pixora-auth">

    <div class="container">

        <div class="pixora-auth-shell pixora-auth-shell-register">


            <%-- ============================================
                 VISUAL PANEL
                 ============================================ --%>

            <aside class="pixora-auth-visual">

                <div class="pixora-auth-visual-overlay"></div>

                <div class="pixora-auth-visual-content">

                    <span class="auth-kicker">
                        JOIN PIXORA
                    </span>

                    <h1>
                        Every great event starts with
                        the right connection.
                    </h1>

                    <p>
                        Plan events, discover photographers
                        and keep your entire photography
                        experience beautifully organized.
                    </p>


                    <div class="auth-feature-list">

                        <div>
                            <i class="bi bi-search"></i>

                            <span>
                                Discover professionals
                            </span>
                        </div>

                        <div>
                            <i class="bi bi-calendar-event"></i>

                            <span>
                                Plan and book events
                            </span>
                        </div>

                        <div>
                            <i class="bi bi-cloud-check"></i>

                            <span>
                                Receive galleries securely
                            </span>
                        </div>

                    </div>

                </div>

            </aside>


            <%-- ============================================
                 REGISTER PANEL
                 ============================================ --%>

            <div class="pixora-auth-panel pixora-register-panel">


                <div class="auth-heading">

                    <span class="eyebrow">
                        CREATE YOUR ACCOUNT
                    </span>

                    <h2>
                        Join PIXORA
                    </h2>

                    <p>
                        Choose how you'd like to get started.
                    </p>

                </div>


                <%-- ========================================
                     GOOGLE CUSTOMER REGISTRATION
                     ======================================== --%>

                <a
                        href="${pageContext.request.contextPath}/auth/google?source=register"
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


                <div class="google-customer-note">

                    <i class="bi bi-info-circle"></i>

                    <span>
                        Google sign-up creates a
                        <strong>Customer / Event Organizer</strong>
                        account.
                        Professional photographers should
                        register using the form below.
                    </span>

                </div>


                <div class="auth-divider">

                    <span>
                        or create your account manually
                    </span>

                </div>


                <%-- ========================================
                     NORMAL REGISTRATION
                     ======================================== --%>

                <form
                        method="post"
                        action="${pageContext.request.contextPath}/register"
                        class="row g-3"
                        novalidate>


                    <div class="col-md-6">

                        <label class="form-label">
                            I am a
                        </label>

                        <select
                                class="form-select auth-control"
                                name="role"
                                required>

                            <option value="CUSTOMER">
                                Customer / Event Organizer
                            </option>

                            <option value="PHOTOGRAPHER">
                                Professional Photographer
                            </option>

                        </select>

                    </div>


                    <div class="col-md-6">

                        <label class="form-label">
                            Full name
                        </label>

                        <input
                                class="form-control auth-control"
                                name="fullName"
                                required
                                value="${param.fullName}"
                                placeholder="Your full name">

                    </div>


                    <div class="col-md-6">

                        <label class="form-label">
                            Username
                        </label>

                        <input
                                class="form-control auth-control"
                                name="username"
                                required
                                value="${param.username}"
                                autocomplete="username"
                                placeholder="Choose a username">

                    </div>


                    <div class="col-md-6">

                        <label class="form-label">
                            Email
                        </label>

                        <input
                                class="form-control auth-control"
                                type="email"
                                name="email"
                                required
                                value="${param.email}"
                                autocomplete="email"
                                placeholder="you@example.com">

                    </div>


                    <div class="col-md-6">

                        <label class="form-label">
                            Phone
                        </label>

                        <input
                                class="form-control auth-control"
                                name="phone"
                                value="${param.phone}"
                                autocomplete="tel"
                                placeholder="+94 77 123 4567">

                    </div>


                    <div class="col-md-6">

                        <div class="photographer-approval-note">

                            <i class="bi bi-patch-check"></i>

                            <span>
                                Photographer accounts require
                                administrator approval.
                            </span>

                        </div>

                    </div>


                    <div class="col-md-6">

                        <label
                                class="form-label"
                                for="registerPassword">

                            Password

                        </label>

                        <div class="auth-password-wrap">

                            <i class="bi bi-lock auth-input-icon"></i>

                            <input
                                    id="registerPassword"
                                    class="form-control auth-control auth-password-input"
                                    type="password"
                                    name="password"
                                    minlength="8"
                                    required
                                    autocomplete="new-password"
                                    placeholder="Minimum 8 characters">

                            <button
                                    class="auth-password-toggle"
                                    type="button"
                                    data-toggle-password="#registerPassword">

                                <i class="bi bi-eye"></i>

                            </button>

                        </div>

                    </div>


                    <div class="col-md-6">

                        <label
                                class="form-label"
                                for="confirmPassword">

                            Confirm password

                        </label>

                        <div class="auth-password-wrap">

                            <i class="bi bi-shield-lock auth-input-icon"></i>

                            <input
                                    id="confirmPassword"
                                    class="form-control auth-control auth-password-input"
                                    type="password"
                                    name="confirmPassword"
                                    minlength="8"
                                    required
                                    autocomplete="new-password"
                                    placeholder="Repeat your password">

                            <button
                                    class="auth-password-toggle"
                                    type="button"
                                    data-toggle-password="#confirmPassword">

                                <i class="bi bi-eye"></i>

                            </button>

                        </div>

                    </div>


                    <div class="col-12 mt-4">

                        <button
                                class="btn pixora-auth-primary w-100"
                                type="submit">

                            Create account

                            <i class="bi bi-arrow-right ms-2"></i>

                        </button>

                    </div>

                </form>


                <div class="auth-register-link">

                    <span>
                        Already have a PIXORA account?
                    </span>

                    <a
                            href="${pageContext.request.contextPath}/login">

                        Sign in

                    </a>

                </div>

            </div>

        </div>

    </div>

</section>


<jsp:include
        page="/WEB-INF/views/common/footer.jsp"/>