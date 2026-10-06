<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="pageTitle" value="Request booking"/>

<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<div class="container py-5 narrow">

    <div class="panel-card">

        <span class="eyebrow">
            ${empty booking ? 'NEW BOOKING' : 'EDIT BOOKING'}
        </span>

        <h2 class="mt-2">Tell us about your event</h2>


        <form method="post"
              action="${pageContext.request.contextPath}/bookings"
              class="row g-3"
              id="bookingForm">


            <!-- ACTION -->

            <input type="hidden"
                   name="action"
                   value="${empty booking ? 'create' : 'update'}">


            <!-- BOOKING ID -->

            <input type="hidden"
                   name="id"
                   value="${booking.bookingId}">


            <!-- PHOTOGRAPHER ID -->

            <input type="hidden"
                   name="photographerId"
                   id="photographerId"
                   value="${photographerId}">


            <!-- ========================================= -->
            <!-- PACKAGE -->
            <!-- ========================================= -->

            <div class="col-12">

                <label class="form-label">
                    Package
                </label>

                <select class="form-select"
                        name="packageId"
                        id="packageId"
                        required>

                    <option value="">
                        Choose package
                    </option>

                    <c:forEach items="${packages}" var="p">

                        <option
                                value="${p.packageId}"

                                data-duration="${p.durationHours}"

                            ${not empty booking &&
                                    booking.packageId == p.packageId
                                    ? 'selected'
                                    : ''}>

                            <c:out value="${p.name}"/>

                            — LKR ${p.price}

                            (${p.durationHours}h)

                        </option>

                    </c:forEach>

                </select>

            </div>


            <!-- ========================================= -->
            <!-- EVENT TYPE -->
            <!-- ========================================= -->

            <div class="col-md-6">

                <label class="form-label">
                    Event type
                </label>

                <input
                        class="form-control"
                        name="eventType"
                        value="<c:out value='${booking.eventType}'/>"
                        required
                        placeholder="Wedding, birthday, corporate...">

            </div>


            <!-- ========================================= -->
            <!-- EVENT DATE -->
            <!-- ========================================= -->

            <div class="col-md-6">

                <label class="form-label">
                    Event date
                </label>

                <input
                        class="form-control"
                        type="date"
                        name="eventDate"
                        id="eventDate"
                        value="${booking.eventDate}"
                        required>

            </div>


            <!-- ========================================= -->
            <!-- PHOTOGRAPHER AVAILABILITY -->
            <!-- ========================================= -->

            <div class="col-12">

                <div id="availabilityPanel"
                     class="availability-panel">

                    <div class="d-flex
                                justify-content-between
                                align-items-center
                                flex-wrap
                                gap-2">

                        <div>

                            <strong>
                                Photographer availability
                            </strong>

                            <div id="availabilityMessage"
                                 class="text-muted small mt-1">

                                Select an event date to see this photographer's availability.

                            </div>

                        </div>


                        <button
                                type="button"
                                class="btn btn-outline-dark btn-sm"
                                id="viewAvailabilityBtn">

                            <i class="bi bi-calendar3"></i>

                            View availability

                        </button>

                    </div>


                    <div id="availabilitySlots"
                         class="mt-3">
                    </div>

                </div>

            </div>


            <!-- ========================================= -->
            <!-- START TIME -->
            <!-- ========================================= -->

            <div class="col-md-6">

                <label class="form-label">
                    Start time
                </label>

                <input
                        class="form-control"
                        type="time"
                        name="startTime"
                        id="startTime"
                        value="${booking.startTime}"
                        required>

            </div>


            <!-- ========================================= -->
            <!-- END TIME -->
            <!-- ========================================= -->

            <div class="col-md-6">

                <label class="form-label">
                    End time
                </label>

                <input
                        class="form-control"
                        type="time"
                        name="endTime"
                        id="endTime"
                        value="${booking.endTime}"
                        required>

            </div>


            <!-- ========================================= -->
            <!-- LIVE TIME VALIDATION -->
            <!-- ========================================= -->

            <div class="col-12"
                 id="selectedTimeStatusContainer"
                 style="display:none;">

                <div id="selectedTimeStatus"
                     class="booking-status-box">
                </div>

            </div>


            <!-- ========================================= -->
            <!-- VENUE -->
            <!-- ========================================= -->

            <div class="col-12">

                <label class="form-label">
                    Venue / location
                </label>

                <input
                        class="form-control"
                        name="venue"
                        value="<c:out value='${booking.venue}'/>"
                        required>

            </div>


            <!-- ========================================= -->
            <!-- NOTES -->
            <!-- ========================================= -->

            <div class="col-12">

                <label class="form-label">
                    Notes
                </label>

                <textarea
                        class="form-control"
                        rows="4"
                        name="notes"><c:out value="${booking.notes}"/></textarea>

            </div>


            <!-- ========================================= -->
            <!-- SUBMIT -->
            <!-- ========================================= -->

            <div class="col-12">

                <button
                        type="submit"
                        class="btn btn-warning btn-lg"
                        id="bookingSubmitButton">

                    ${empty booking
                            ? 'Submit booking request'
                            : 'Save booking changes'}

                </button>

            </div>

        </form>

    </div>

</div>



<!-- ===================================================== -->
<!-- AVAILABILITY MODAL -->
<!-- ===================================================== -->

<div class="modal fade"
     id="availabilityModal"
     tabindex="-1"
     aria-hidden="true">

    <div class="modal-dialog modal-dialog-centered">

        <div class="modal-content">

            <div class="modal-header">

                <h5 class="modal-title">

                    <i class="bi bi-calendar-check me-2"></i>

                    Photographer Availability

                </h5>


                <button
                        type="button"
                        class="btn-close"
                        data-bs-dismiss="modal"
                        aria-label="Close">
                </button>

            </div>


            <div class="modal-body">

                <p class="text-muted">

                    Choose an event date to see the photographer's
                    available and booked periods.

                </p>


                <div id="modalAvailabilityContent">

                    Select a date first.

                </div>

            </div>


            <div class="modal-footer">

                <button
                        type="button"
                        class="btn btn-dark"
                        data-bs-dismiss="modal">

                    Close

                </button>

            </div>

        </div>

    </div>

</div>



<!-- ===================================================== -->
<!-- JAVASCRIPT -->
<!-- ===================================================== -->

<script>

    document.addEventListener("DOMContentLoaded", function () {

        const photographerId =
            document.getElementById("photographerId").value;

        const eventDate =
            document.getElementById("eventDate");

        const startTime =
            document.getElementById("startTime");

        const endTime =
            document.getElementById("endTime");

        const packageSelect =
            document.getElementById("packageId");

        const availabilityMessage =
            document.getElementById("availabilityMessage");

        const availabilitySlots =
            document.getElementById("availabilitySlots");

        const selectedTimeStatus =
            document.getElementById("selectedTimeStatus");

        const selectedTimeStatusContainer =
            document.getElementById(
                "selectedTimeStatusContainer"
            );

        const submitButton =
            document.getElementById(
                "bookingSubmitButton"
            );

        const viewAvailabilityBtn =
            document.getElementById(
                "viewAvailabilityBtn"
            );

        const modalContent =
            document.getElementById(
                "modalAvailabilityContent"
            );


        let currentSlots = [];

        let currentBookings = [];

        let availabilityLoaded = false;



        /*
         * =====================================================
         * FORMAT TIME TO 12-HOUR AM / PM
         * =====================================================
         */

        function formatTime(time) {

            if (!time) {
                return "";
            }


            const parts =
                time.split(":");


            let hour =
                parseInt(parts[0]);


            const minute =
                parts[1];


            const ampm =
                hour >= 12
                    ? "PM"
                    : "AM";


            hour =
                hour % 12 || 12;


            return (
                hour +
                ":" +
                minute +
                " " +
                ampm
            );
        }



        /*
         * =====================================================
         * CONVERT HH:mm TO MINUTES
         * =====================================================
         */

        function timeToMinutes(time) {

            if (!time) {
                return 0;
            }


            const parts =
                time.split(":");


            return (
                parseInt(parts[0]) * 60 +
                parseInt(parts[1])
            );
        }



        /*
         * =====================================================
         * LOAD AVAILABILITY FROM SERVLET
         * =====================================================
         */

        async function loadAvailability() {

            const date =
                eventDate.value;


            /*
             * No date selected
             */

            if (!date) {

                availabilityLoaded =
                    false;


                availabilityMessage.textContent =
                    "Select an event date to see this photographer's availability.";


                availabilitySlots.innerHTML =
                    "";


                currentSlots =
                    [];


                currentBookings =
                    [];


                validateSelectedTime();


                return false;
            }


            /*
             * No photographer ID
             */

            if (!photographerId ||
                photographerId === "0") {

                availabilityLoaded =
                    false;


                availabilityMessage.innerHTML =
                    '<span class="text-danger">' +
                    '<i class="bi bi-exclamation-circle me-1"></i>' +
                    'Photographer information is missing.' +
                    '</span>';


                return false;
            }


            /*
             * Loading UI
             */

            availabilityMessage.innerHTML =
                '<span class="spinner-border spinner-border-sm me-2"></span>' +
                'Checking availability...';


            availabilitySlots.innerHTML =
                "";


            availabilityLoaded =
                false;


            try {

                /*
                 * IMPORTANT:
                 * This JSP expression is valid.
                 *
                 * It is evaluated by JSP only once
                 * to create the application context URL.
                 */

                const contextPath =
                    "${pageContext.request.contextPath}";


                const url =
                    contextPath +
                    "/bookings" +
                    "?view=availability" +
                    "&photographerId=" +
                    encodeURIComponent(
                        photographerId
                    ) +
                    "&date=" +
                    encodeURIComponent(
                        date
                    );


                console.log(
                    "Availability URL:",
                    url
                );


                const response =
                    await fetch(
                        url,
                        {
                            method: "GET",
                            headers: {
                                "Accept": "application/json"
                            }
                        }
                    );


                /*
                 * Server error
                 */

                if (!response.ok) {

                    throw new Error(
                        "Server returned HTTP " +
                        response.status
                    );
                }


                /*
                 * Read response text first
                 *
                 * This gives a better error if
                 * servlet returns HTML instead of JSON.
                 */

                const responseText =
                    await response.text();


                console.log(
                    "Availability raw response:",
                    responseText
                );


                let data;


                try {

                    data =
                        JSON.parse(
                            responseText
                        );

                } catch (jsonError) {

                    throw new Error(
                        "Server did not return JSON."
                    );
                }


                if (!data.success) {

                    throw new Error(
                        data.message ||
                        "Unable to load availability."
                    );
                }


                currentSlots =
                    Array.isArray(data.slots)
                        ? data.slots
                        : [];


                currentBookings =
                    Array.isArray(data.bookings)
                        ? data.bookings
                        : [];


                availabilityLoaded =
                    true;


                renderAvailability();


                validateSelectedTime();


                return true;


            } catch (error) {

                console.error(
                    "Availability error:",
                    error
                );


                currentSlots =
                    [];


                currentBookings =
                    [];


                availabilityLoaded =
                    false;


                availabilityMessage.innerHTML =
                    '<span class="text-danger">' +
                    '<i class="bi bi-exclamation-circle me-1"></i>' +
                    'Unable to load photographer availability.' +
                    '</span>';


                availabilitySlots.innerHTML =
                    '<div class="alert alert-danger mb-0">' +
                    'Could not load availability from the server.' +
                    '</div>';


                validateSelectedTime();


                return false;
            }
        }



        /*
         * =====================================================
         * RENDER AVAILABILITY ON BOOKING PAGE
         * =====================================================
         */

        function renderAvailability() {

            if (currentSlots.length === 0) {

                availabilityMessage.innerHTML =
                    '<span class="text-danger">' +
                    '<i class="bi bi-x-circle me-1"></i>' +
                    'Photographer has no availability for this date.' +
                    '</span>';


                availabilitySlots.innerHTML =
                    '<div class="availability-empty">' +
                    'No availability has been added for this date.' +
                    '</div>';


                return;
            }


            availabilityMessage.innerHTML =
                '<span class="text-success">' +
                '<i class="bi bi-check-circle-fill me-1"></i>' +
                'Photographer is available on this date.' +
                '</span>';


            let html =
                "";


            /*
             * AVAILABLE SLOTS
             */

            currentSlots.forEach(
                function (slot) {

                    html +=
                        '<div class="availability-slot available">' +

                        '<i class="bi bi-clock me-2"></i>' +

                        '<strong>' +
                        formatTime(slot.start) +
                        ' - ' +
                        formatTime(slot.end) +
                        '</strong>' +

                        '<span class="badge text-bg-success ms-auto">' +
                        'Available' +
                        '</span>' +

                        '</div>';
                }
            );


            /*
             * CONFIRMED BOOKINGS
             */

            if (currentBookings.length > 0) {

                html +=
                    '<div class="small text-muted mt-3 mb-2">' +
                    'Already booked periods' +
                    '</div>';


                currentBookings.forEach(
                    function (booking) {

                        html +=
                            '<div class="availability-slot booked">' +

                            '<i class="bi bi-calendar-x me-2"></i>' +

                            '<strong>' +
                            formatTime(booking.start) +
                            ' - ' +
                            formatTime(booking.end) +
                            '</strong>' +

                            '<span class="badge text-bg-secondary ms-auto">' +
                            'Booked' +
                            '</span>' +

                            '</div>';
                    }
                );
            }


            availabilitySlots.innerHTML =
                html;
        }



        /*
         * =====================================================
         * CHECK SELECTED START / END TIME
         * =====================================================
         */

        function validateSelectedTime() {

            const date =
                eventDate.value;


            const start =
                startTime.value;


            const end =
                endTime.value;


            /*
             * Not enough data yet
             */

            if (!date ||
                !start ||
                !end) {

                selectedTimeStatusContainer.style.display =
                    "none";


                /*
                 * Keep enabled until user has
                 * entered all date/time data.
                 */

                submitButton.disabled =
                    false;


                return;
            }


            selectedTimeStatusContainer.style.display =
                "block";


            /*
             * Availability not loaded
             */

            if (!availabilityLoaded) {

                selectedTimeStatus.className =
                    "booking-status-box unavailable";


                selectedTimeStatus.innerHTML =
                    '<i class="bi bi-exclamation-circle-fill me-2"></i>' +

                    '<strong>Availability not checked</strong>' +

                    '<div class="small mt-1">' +
                    'Please wait until photographer availability is loaded.' +
                    '</div>';


                submitButton.disabled =
                    true;


                return;
            }


            const startMinutes =
                timeToMinutes(
                    start
                );


            const endMinutes =
                timeToMinutes(
                    end
                );


            /*
             * End must be after start
             */

            if (endMinutes <=
                startMinutes) {

                showUnavailable(
                    "End time must be after start time."
                );


                return;
            }


            /*
             * Selected time must fit completely
             * inside an availability slot.
             */

            const insideAvailability =
                currentSlots.some(
                    function (slot) {

                        const slotStart =
                            timeToMinutes(
                                slot.start
                            );


                        const slotEnd =
                            timeToMinutes(
                                slot.end
                            );


                        return (
                            startMinutes >=
                            slotStart
                            &&
                            endMinutes <=
                            slotEnd
                        );
                    }
                );


            if (!insideAvailability) {

                showUnavailable(
                    "This time is outside the photographer's available hours."
                );


                return;
            }


            /*
             * Check confirmed booking overlap
             *
             * overlap condition:
             *
             * selectedStart < bookingEnd
             * AND
             * selectedEnd > bookingStart
             */

            const conflict =
                currentBookings.some(
                    function (booking) {

                        const bookingStart =
                            timeToMinutes(
                                booking.start
                            );


                        const bookingEnd =
                            timeToMinutes(
                                booking.end
                            );


                        return (
                            startMinutes <
                            bookingEnd
                            &&
                            endMinutes >
                            bookingStart
                        );
                    }
                );


            if (conflict) {

                showUnavailable(
                    "The photographer already has another confirmed booking during this time."
                );


                return;
            }


            /*
             * AVAILABLE
             */

            selectedTimeStatus.className =
                "booking-status-box available";


            selectedTimeStatus.innerHTML =
                '<i class="bi bi-check-circle-fill me-2"></i>' +

                '<strong>Available</strong>' +

                '<div class="small mt-1">' +
                'This photographer is available for your selected date and time.' +
                '</div>';


            submitButton.disabled =
                false;
        }



        /*
         * =====================================================
         * SHOW UNAVAILABLE STATUS
         * =====================================================
         */

        function showUnavailable(message) {

            selectedTimeStatusContainer.style.display =
                "block";


            selectedTimeStatus.className =
                "booking-status-box unavailable";


            selectedTimeStatus.innerHTML =
                '<i class="bi bi-x-circle-fill me-2"></i>' +

                '<strong>Unavailable</strong>' +

                '<div class="small mt-1">' +
                message +
                '</div>';


            submitButton.disabled =
                true;
        }



        /*
         * =====================================================
         * CALCULATE END TIME USING PACKAGE DURATION
         * =====================================================
         */

        function calculateEndTime() {

            const option =
                packageSelect.options[
                    packageSelect.selectedIndex
                    ];


            if (!option ||
                !option.dataset.duration ||
                !startTime.value) {

                return;
            }


            const duration =
                parseInt(
                    option.dataset.duration
                );


            if (isNaN(duration) ||
                duration <= 0) {

                return;
            }


            const startMinutes =
                timeToMinutes(
                    startTime.value
                );


            const totalMinutes =
                startMinutes +
                duration * 60;


            /*
             * Prevent next-day time
             */

            if (totalMinutes >=
                24 * 60) {

                endTime.value =
                    "";


                showUnavailable(
                    "The selected package would finish after midnight. Please choose an earlier start time."
                );


                return;
            }


            const hours =
                Math.floor(
                    totalMinutes / 60
                );


            const minutes =
                totalMinutes % 60;


            endTime.value =
                String(hours).padStart(
                    2,
                    "0"
                ) +
                ":" +
                String(minutes).padStart(
                    2,
                    "0"
                );


            validateSelectedTime();
        }



        /*
         * =====================================================
         * DATE CHANGED
         * =====================================================
         */

        eventDate.addEventListener(
            "change",
            async function () {

                /*
                 * Clear time status first
                 */

                selectedTimeStatusContainer.style.display =
                    "none";


                await loadAvailability();


                validateSelectedTime();
            }
        );



        /*
         * =====================================================
         * START TIME CHANGED
         * =====================================================
         */

        startTime.addEventListener(
            "change",
            function () {

                calculateEndTime();


                validateSelectedTime();
            }
        );



        /*
         * =====================================================
         * END TIME CHANGED
         * =====================================================
         */

        endTime.addEventListener(
            "change",
            function () {

                validateSelectedTime();
            }
        );



        /*
         * =====================================================
         * PACKAGE CHANGED
         * =====================================================
         */

        packageSelect.addEventListener(
            "change",
            function () {

                if (startTime.value) {

                    calculateEndTime();
                }


                validateSelectedTime();
            }
        );



        /*
         * =====================================================
         * VIEW AVAILABILITY MODAL
         * =====================================================
         */

        viewAvailabilityBtn.addEventListener(
            "click",
            async function () {

                /*
                 * No date selected
                 */

                if (!eventDate.value) {

                    modalContent.innerHTML =
                        '<div class="alert alert-warning mb-0">' +

                        '<i class="bi bi-info-circle me-2"></i>' +

                        'Please select an event date first.' +

                        '</div>';


                    openAvailabilityModal();


                    return;
                }


                /*
                 * Reload availability before popup
                 */

                modalContent.innerHTML =
                    '<div class="text-center py-3">' +

                    '<div class="spinner-border spinner-border-sm me-2"></div>' +

                    'Loading availability...' +

                    '</div>';


                openAvailabilityModal();


                const loaded =
                    await loadAvailability();


                /*
                 * API failed
                 */

                if (!loaded) {

                    modalContent.innerHTML =
                        '<div class="alert alert-danger mb-0">' +

                        '<i class="bi bi-exclamation-circle me-2"></i>' +

                        'Unable to load photographer availability.' +

                        '</div>';


                    return;
                }


                /*
                 * No availability
                 */

                if (currentSlots.length === 0) {

                    modalContent.innerHTML =
                        '<div class="alert alert-warning mb-0">' +

                        '<i class="bi bi-calendar-x me-2"></i>' +

                        'Photographer has no availability for the selected date.' +

                        '</div>';


                    return;
                }


                /*
                 * Build modal content
                 */

                let html =
                    '<h6 class="mb-3">' +
                    eventDate.value +
                    '</h6>';


                html +=
                    '<div class="small text-muted mb-2">' +
                    'Available periods' +
                    '</div>';


                currentSlots.forEach(
                    function (slot) {

                        html +=
                            '<div class="availability-slot available">' +

                            '<i class="bi bi-check-circle-fill me-2"></i>' +

                            '<strong>' +
                            formatTime(slot.start) +
                            ' - ' +
                            formatTime(slot.end) +
                            '</strong>' +

                            '<span class="badge text-bg-success ms-auto">' +
                            'Available' +
                            '</span>' +

                            '</div>';
                    }
                );


                /*
                 * Booked periods
                 */

                if (currentBookings.length > 0) {

                    html +=
                        '<div class="small text-muted mt-3 mb-2">' +
                        'Already booked periods' +
                        '</div>';


                    currentBookings.forEach(
                        function (booking) {

                            html +=
                                '<div class="availability-slot booked">' +

                                '<i class="bi bi-x-circle me-2"></i>' +

                                '<strong>' +
                                formatTime(
                                    booking.start
                                ) +
                                ' - ' +
                                formatTime(
                                    booking.end
                                ) +
                                '</strong>' +

                                '<span class="badge text-bg-secondary ms-auto">' +
                                'Booked' +
                                '</span>' +

                                '</div>';
                        }
                    );

                } else {

                    html +=
                        '<div class="small text-muted mt-3">' +

                        '<i class="bi bi-check2-circle me-1"></i>' +

                        'No confirmed bookings during this date.' +

                        '</div>';
                }


                modalContent.innerHTML =
                    html;
            }
        );



        /*
         * =====================================================
         * OPEN MODAL
         * =====================================================
         */

        function openAvailabilityModal() {

            const modalElement =
                document.getElementById(
                    "availabilityModal"
                );


            if (typeof bootstrap ===
                "undefined") {

                console.error(
                    "Bootstrap JavaScript is not loaded."
                );


                return;
            }


            const modal =
                bootstrap.Modal.getOrCreateInstance(
                    modalElement
                );


            modal.show();
        }



        /*
         * =====================================================
         * BEFORE SUBMIT
         *
         * Front-end check only.
         * Server-side validation must still remain.
         * =====================================================
         */

        document.getElementById(
            "bookingForm"
        ).addEventListener(
            "submit",
            function (event) {

                if (submitButton.disabled) {

                    event.preventDefault();


                    return;
                }
            }
        );



        /*
         * =====================================================
         * INITIAL LOAD
         *
         * Useful when editing an existing booking.
         * =====================================================
         */

        if (eventDate.value) {

            loadAvailability();
        }

    });

</script>


<jsp:include page="/WEB-INF/views/common/footer.jsp"/>