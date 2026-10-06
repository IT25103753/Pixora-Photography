</main>

<footer class="pixora-footer mt-5">
    <div class="container py-4">
        <div class="row g-4 g-lg-5">

            <!-- Brand -->
            <div class="col-lg-5 col-md-6">
                <div class="d-flex align-items-center gap-2 mb-3">
                    <img src="${pageContext.request.contextPath}/assets/images/logo.svg"
                         width="34" alt="">
                    <strong class="footer-brand">PIXORA</strong>
                </div>

                <p class="footer-description">
                    Find the right photographer for your celebration and manage
                    every detail, from booking to gallery delivery, in one
                    seamless experience.
                </p>

                <div class="footer-socials">
                    <span class="footer-social-item">
                        <i class="bi bi-facebook" aria-hidden="true"></i>
                        Supun Madusanka
                    </span>

                    <a class="footer-social-item"
                       href="https://www.instagram.com/s_s_s_supun/"
                       target="_blank" rel="noopener noreferrer">
                        <i class="bi bi-instagram" aria-hidden="true"></i>
                        @s_s_s_supun
                    </a>
                </div>
            </div>

            <!-- Navigation -->
            <div class="col-lg-3 col-md-6">
                <h6 class="footer-heading">Explore PIXORA</h6>

                <nav class="footer-links" aria-label="Footer navigation">
                    <a href="${pageContext.request.contextPath}/">
                        Home
                    </a>
                    <a href="${pageContext.request.contextPath}/photographers">
                        Find photographers
                    </a>
                </nav>
            </div>

            <!-- Contact -->
            <div class="col-lg-4 col-md-12">
                <h6 class="footer-heading">Get in touch</h6>

                <div class="footer-contact">
                    <a href="mailto:ssupunmadusanka07@gmail.com">
                        <i class="bi bi-envelope" aria-hidden="true"></i>
                        <span>ssupunmadusanka07@gmail.com</span>
                    </a>

                    <a href="tel:+94750425373">
                        <i class="bi bi-telephone" aria-hidden="true"></i>
                        <span>075 042 5373</span>
                    </a>
                </div>
            </div>

        </div>

        <div class="footer-bottom">
            <span>&copy; <%= java.time.Year.now().getValue() %> PIXORA.</span>
            <span>Project: Group 2026-Y2-S1-KU-39</span>
        </div>
    </div>
</footer>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/app.js?v=3"></script>
</body>
</html>
