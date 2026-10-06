document.addEventListener("DOMContentLoaded", () => {
  document.querySelectorAll("form[data-confirm]").forEach(form => {
    form.addEventListener("submit", e => {
      if (!window.confirm(form.dataset.confirm || "Are you sure?")) e.preventDefault();
    });
  });
  document.querySelectorAll("[data-toggle-password]").forEach(btn => {
    btn.addEventListener("click", () => {
      const input = document.querySelector(btn.dataset.togglePassword);
      if (!input) return;
      input.type = input.type === "password" ? "text" : "password";
      const icon = btn.querySelector("i");
      if (icon) icon.className = input.type === "password" ? "bi bi-eye" : "bi bi-eye-slash";
    });
  });
  document.querySelectorAll('input[type="date"]').forEach(input => {
    if (!input.min) input.min = new Date().toISOString().split("T")[0];
  });
  document.querySelectorAll("form").forEach(form => {
    form.addEventListener("submit", e => {
      if (!form.checkValidity()) {
        e.preventDefault();
        e.stopPropagation();
        form.classList.add("was-validated");
      }
    });
  });

  const revealEls = document.querySelectorAll(".reveal");
  if (revealEls.length && "IntersectionObserver" in window) {
    const observer = new IntersectionObserver((entries) => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          entry.target.classList.add("is-visible");
          observer.unobserve(entry.target);
        }
      });
    }, { threshold: 0.15, rootMargin: "0px 0px -60px 0px" });
    revealEls.forEach(el => observer.observe(el));
  } else {
    revealEls.forEach(el => el.classList.add("is-visible")); // fallback: no JS support
  }
});

/* Smooth crossfade between two banner photographs */
(() => {
  const scriptUrl = document.currentScript?.src;
  if (!scriptUrl) return;

  const imageFolder = new URL("../images/", scriptUrl);
  const rotationInterval = 5000; // 2 minutes

  async function startBannerRotation() {
    const banners = document.querySelectorAll(".page-hero");
    if (!banners.length) return;

    const loaded = await Promise.all(
        ["page-banner.jpeg", "page-banner-2.jpeg"].map(filename =>
            new Promise(resolve => {
              const image = new Image();
              image.onload = () => resolve(true);
              image.onerror = () => resolve(false);
              image.src = new URL(filename, imageFolder).href;
            })
        )
    );

    if (!loaded.every(Boolean)) return;

    setInterval(() => {
      if (document.hidden) return;

      banners.forEach(banner => {
        banner.classList.toggle("show-second-banner");
      });
    }, rotationInterval);
  }

  if (document.readyState === "loading") {
    document.addEventListener(
        "DOMContentLoaded",
        startBannerRotation,
        { once: true }
    );
  } else {
    startBannerRotation();
  }
})();
