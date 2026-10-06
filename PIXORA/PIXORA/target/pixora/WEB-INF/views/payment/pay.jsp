<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="pageTitle" value="Secure Payment"/>

<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<section class="pixora-checkout">

    <div class="container">

        <div class="checkout-shell">

            <!-- =========================================
                 LEFT SIDE - PAYMENT FORM
                 ========================================= -->

            <div class="checkout-main">

                <div class="checkout-heading">

                    <span class="checkout-eyebrow">
                        SECURE CHECKOUT
                    </span>

                    <h1>
                        Complete your payment
                    </h1>

                    <p>
                        Choose your preferred payment method and
                        complete the transaction securely.
                    </p>

                </div>


                <form
                        method="post"
                        action="${pageContext.request.contextPath}/payments"
                        id="paymentForm">

                    <input
                            type="hidden"
                            name="action"
                            value="pay">

                    <input
                            type="hidden"
                            name="bookingId"
                            value="${booking.bookingId}">

                    <!-- Hidden backend simulation value -->
                    <input
                            type="hidden"
                            name="simulation"
                            value="SUCCESS">


                    <!-- =================================
                         PAYMENT METHOD
                         ================================= -->

                    <div class="checkout-section">

                        <div class="checkout-section-title">

                            <span>
                                Payment method
                            </span>

                            <i class="bi bi-shield-lock"></i>

                        </div>


                        <div class="payment-method-grid">

                            <!-- CARD -->

                            <label class="payment-method-card">

                                <input
                                        type="radio"
                                        name="method"
                                        value="CARD_DEMO"
                                        checked>

                                <span class="payment-method-content">

                                    <span class="payment-method-icon card-icon">

                                        <i class="bi bi-credit-card-2-front"></i>

                                    </span>

                                    <span class="payment-method-text">

                                        <strong>
                                            Credit / Debit Card
                                        </strong>

                                        <small>
                                            Visa, Mastercard
                                        </small>

                                    </span>

                                    <span class="payment-method-check">

                                        <i class="bi bi-check-circle-fill"></i>

                                    </span>

                                </span>

                            </label>


                            <!-- BANK -->

                            <label class="payment-method-card">

                                <input
                                        type="radio"
                                        name="method"
                                        value="BANK_TRANSFER_DEMO">

                                <span class="payment-method-content">

                                    <span class="payment-method-icon">

                                        <i class="bi bi-bank"></i>

                                    </span>

                                    <span class="payment-method-text">

                                        <strong>
                                            Bank Transfer
                                        </strong>

                                        <small>
                                            Online bank payment
                                        </small>

                                    </span>

                                    <span class="payment-method-check">

                                        <i class="bi bi-check-circle-fill"></i>

                                    </span>

                                </span>

                            </label>


                            <!-- DIGITAL WALLET -->

                            <label class="payment-method-card">

                                <input
                                        type="radio"
                                        name="method"
                                        value="DIGITAL_WALLET">

                                <span class="payment-method-content">

                                    <span class="payment-method-icon">

                                        <i class="bi bi-wallet2"></i>

                                    </span>

                                    <span class="payment-method-text">

                                        <strong>
                                            Digital Wallet
                                        </strong>

                                        <small>
                                            Fast wallet payment
                                        </small>

                                    </span>

                                    <span class="payment-method-check">

                                        <i class="bi bi-check-circle-fill"></i>

                                    </span>

                                </span>

                            </label>

                        </div>

                    </div>


                    <!-- =================================
                         CARD VISUAL
                         ================================= -->

                    <div
                            class="checkout-card-preview"
                            id="cardPreview">

                        <div class="card-preview-top">

                            <span>
                                PIXORA
                            </span>

                            <i class="bi bi-wifi"></i>

                        </div>

                        <div class="card-chip">

                            <i class="bi bi-credit-card-2-front"></i>

                        </div>

                        <div class="card-number">
                            •••• &nbsp; •••• &nbsp; •••• &nbsp; 4821
                        </div>

                        <div class="card-preview-bottom">

                            <div>

                                <small>
                                    CARD HOLDER
                                </small>

                                <strong>
                                    ${sessionScope.authUser.fullName}
                                </strong>

                            </div>

                            <div>

                                <small>
                                    PAYMENT
                                </small>

                                <strong>
                                    SECURE
                                </strong>

                            </div>

                        </div>

                    </div>


                    <!-- =================================
                         SECURITY MESSAGE
                         ================================= -->

                    <div class="checkout-security">

                        <div class="checkout-security-icon">

                            <i class="bi bi-shield-check"></i>

                        </div>

                        <div>

                            <strong>
                                Secure payment environment
                            </strong>

                            <p>
                                Your transaction is processed through
                                PIXORA's secure academic payment workflow.
                                No real banking credentials are stored.
                            </p>

                        </div>

                    </div>


                    <!-- =================================
                         PAY BUTTON
                         ================================= -->

                    <button
                            type="submit"
                            class="checkout-pay-button">

                        <span>

                            <i class="bi bi-lock-fill"></i>

                            Pay LKR ${booking.totalAmount}

                        </span>

                        <i class="bi bi-arrow-right"></i>

                    </button>


                    <a
                            href="${pageContext.request.contextPath}/bookings?view=detail&bookingId=${booking.bookingId}"
                            class="checkout-back-link">

                        <i class="bi bi-arrow-left"></i>

                        Back to booking

                    </a>

                </form>

            </div>


            <!-- =========================================
                 RIGHT SIDE - BOOKING SUMMARY
                 ========================================= -->

            <aside class="checkout-summary">

                <div class="checkout-summary-header">

                    <span>
                        BOOKING SUMMARY
                    </span>

                    <i class="bi bi-receipt"></i>

                </div>


                <div class="checkout-reference">

                    <small>
                        Booking reference
                    </small>

                    <strong>
                        ${booking.bookingRef}
                    </strong>

                </div>


                <div class="checkout-summary-details">

                    <div class="summary-row">

                        <span>

                            <i class="bi bi-camera"></i>

                            Photographer

                        </span>

                        <strong>
                            ${booking.photographerName}
                        </strong>

                    </div>


                    <div class="summary-row">

                        <span>

                            <i class="bi bi-box"></i>

                            Package

                        </span>

                        <strong>
                            ${booking.packageName}
                        </strong>

                    </div>


                    <div class="summary-row">

                        <span>

                            <i class="bi bi-stars"></i>

                            Event

                        </span>

                        <strong>
                            ${booking.eventType}
                        </strong>

                    </div>


                    <div class="summary-row">

                        <span>

                            <i class="bi bi-calendar3"></i>

                            Event date

                        </span>

                        <strong>
                            ${booking.eventDate}
                        </strong>

                    </div>


                    <div class="summary-row">

                        <span>

                            <i class="bi bi-clock"></i>

                            Time

                        </span>

                        <strong>
                            ${booking.startTime}
                            -
                            ${booking.endTime}
                        </strong>

                    </div>


                    <div class="summary-row">

                        <span>

                            <i class="bi bi-geo-alt"></i>

                            Venue

                        </span>

                        <strong>
                            ${booking.venue}
                        </strong>

                    </div>

                </div>


                <div class="checkout-divider"></div>


                <div class="checkout-total-row">

                    <div>

                        <span>
                            Total payment
                        </span>

                        <small>
                            Amount due
                        </small>

                    </div>

                    <strong>
                        LKR ${booking.totalAmount}
                    </strong>

                </div>


                <div class="checkout-guarantee">

                    <i class="bi bi-patch-check-fill"></i>

                    <span>
                        Payment linked securely to your
                        PIXORA booking.
                    </span>

                </div>

            </aside>

        </div>

    </div>

</section>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
