package com.pixora.controller;

import com.pixora.util.AppConfig;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;

import java.net.URLEncoder;

import java.nio.charset.StandardCharsets;

import java.security.SecureRandom;

import java.util.Base64;

@WebServlet("/auth/google")
public class GoogleLoginServlet
        extends HttpServlet {

    private static final SecureRandom RANDOM =
            new SecureRandom();

    @Override
    protected void doGet(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws IOException {

        String clientId =
                AppConfig.get(
                                "google.clientId",
                                ""
                        )
                        .trim();

        /*
         * Prevent a broken Google redirect if
         * configuration has not yet been added.
         */
        if (clientId.isEmpty()) {

            req.getSession()
                    .setAttribute(
                            "flashType",
                            "danger"
                    );

            req.getSession()
                    .setAttribute(
                            "flashMessage",
                            "Google sign-in is not configured yet."
                    );

            resp.sendRedirect(
                    req.getContextPath()
                            + "/login"
            );

            return;
        }

        /*
         * We use the same OAuth flow from:
         *
         * Login page
         * Register page
         */
        String source =
                "register".equals(
                        req.getParameter("source")
                )
                        ? "register"
                        : "login";

        HttpSession session =
                req.getSession(true);

        /*
         * State protects the OAuth request against
         * forged callback requests.
         */
        String state =
                createState();

        session.setAttribute(
                "googleOAuthState",
                state
        );

        session.setAttribute(
                "googleOAuthSource",
                source
        );

        String redirectUri =
                redirectUri(req);

        /*
         * PIXORA only requests:
         *
         * openid
         * email
         * profile
         *
         * We do NOT request Gmail, Drive,
         * Calendar, Contacts etc.
         */
        String authorizationUrl =
                "https://accounts.google.com/o/oauth2/v2/auth"

                        + "?client_id="
                        + encode(clientId)

                        + "&redirect_uri="
                        + encode(redirectUri)

                        + "&response_type=code"

                        + "&scope="
                        + encode(
                        "openid email profile"
                )

                        + "&state="
                        + encode(state)

                        + "&prompt=select_account";

        resp.sendRedirect(
                authorizationUrl
        );
    }

    /*
     * We prefer the configured URI.
     *
     * If it has not been configured,
     * generate it from the current request.
     */
    public static String redirectUri(
            HttpServletRequest req
    ) {

        String configured =
                AppConfig.get(
                                "google.redirectUri",
                                ""
                        )
                        .trim();

        if (!configured.isEmpty()) {
            return configured;
        }

        StringBuilder base =
                new StringBuilder()
                        .append(
                                req.getScheme()
                        )
                        .append(
                                "://"
                        )
                        .append(
                                req.getServerName()
                        );

        int port =
                req.getServerPort();

        boolean customHttpPort =
                "http".equals(
                        req.getScheme()
                )
                        && port != 80;

        boolean customHttpsPort =
                "https".equals(
                        req.getScheme()
                )
                        && port != 443;

        if (customHttpPort
                || customHttpsPort) {

            base.append(':')
                    .append(port);
        }

        return base
                .append(
                        req.getContextPath()
                )
                .append(
                        "/auth/google/callback"
                )
                .toString();
    }

    private static String createState() {

        byte[] bytes =
                new byte[32];

        RANDOM.nextBytes(
                bytes
        );

        return Base64
                .getUrlEncoder()
                .withoutPadding()
                .encodeToString(
                        bytes
                );
    }

    private static String encode(
            String value
    ) {

        return URLEncoder.encode(
                value,
                StandardCharsets.UTF_8
        );
    }
}
