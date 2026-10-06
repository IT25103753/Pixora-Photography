package com.pixora.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.pixora.dao.AuditDAO;
import com.pixora.dao.UserDAO;

import com.pixora.model.User;

import com.pixora.util.AppConfig;

import javax.servlet.annotation.WebServlet;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;

import java.net.URI;
import java.net.URLEncoder;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import java.nio.charset.StandardCharsets;

import java.time.Duration;

@WebServlet("/auth/google/callback")
public class GoogleCallbackServlet
        extends HttpServlet {

    private static final ObjectMapper JSON =
            new ObjectMapper();

    private static final HttpClient HTTP =
            HttpClient
                    .newBuilder()
                    .connectTimeout(
                            Duration.ofSeconds(10)
                    )
                    .build();

    private final UserDAO userDAO =
            new UserDAO();

    private final AuditDAO auditDAO =
            new AuditDAO();

    @Override
    protected void doGet(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws IOException {

        HttpSession session =
                req.getSession(false);

        /*
         * OAuth must have started from PIXORA.
         */
        if (session == null) {

            fail(
                    req,
                    resp,
                    "Your Google sign-in session expired. Please try again."
            );

            return;
        }

        String expectedState =
                (String)
                        session.getAttribute(
                                "googleOAuthState"
                        );

        String returnedState =
                req.getParameter(
                        "state"
                );

        /*
         * State is one-use only.
         */
        session.removeAttribute(
                "googleOAuthState"
        );

        if (expectedState == null
                || returnedState == null
                || !expectedState.equals(
                returnedState
        )) {

            fail(
                    req,
                    resp,
                    "Google sign-in could not be verified. Please try again."
            );

            return;
        }

        /*
         * User clicked Cancel / denied access.
         */
        if (req.getParameter(
                "error"
        ) != null) {

            fail(
                    req,
                    resp,
                    "Google sign-in was cancelled or permission was not granted."
            );

            return;
        }

        String code =
                req.getParameter(
                        "code"
                );

        if (code == null
                || code.isBlank()) {

            fail(
                    req,
                    resp,
                    "Google did not return an authorization code."
            );

            return;
        }

        try {

            /*
             * Step 1:
             * Exchange temporary code
             * for an access token.
             */
            String accessToken =
                    exchangeCodeForAccessToken(
                            req,
                            code
                    );

            /*
             * Step 2:
             * Ask Google for verified identity.
             */
            JsonNode profile =
                    loadGoogleProfile(
                            accessToken
                    );

            String googleSub =
                    text(
                            profile,
                            "sub"
                    );

            String email =
                    text(
                            profile,
                            "email"
                    );

            String fullName =
                    text(
                            profile,
                            "name"
                    );

            boolean emailVerified =
                    profile
                            .path(
                                    "email_verified"
                            )
                            .asBoolean(
                                    false
                            );

            /*
             * PIXORA requires verified identity.
             */
            if (googleSub == null
                    || email == null
                    || !emailVerified) {

                fail(
                        req,
                        resp,
                        "Your Google account does not provide a verified email address."
                );

                return;
            }

            /*
             * Fallback in the unlikely case that
             * Google provides no display name.
             */
            if (fullName == null
                    || fullName.isBlank()) {

                int at =
                        email.indexOf('@');

                fullName =
                        at > 0
                                ? email.substring(
                                0,
                                at
                        )
                                : email;
            }

            /*
             * -------------------------------------------------
             * First: find an already-linked Google account.
             * -------------------------------------------------
             */
            User user =
                    userDAO.findByGoogleSub(
                            googleSub
                    );

            /*
             * -------------------------------------------------
             * Not already linked?
             *
             * Try matching the verified Google email with an
             * existing PIXORA account.
             * -------------------------------------------------
             */
            if (user == null) {

                user =
                        userDAO.findByEmail(
                                email
                        );

                if (user != null) {

                    /*
                     * Safety check.
                     */
                    if (user.getGoogleSub() != null
                            && !googleSub.equals(
                            user.getGoogleSub()
                    )) {

                        fail(
                                req,
                                resp,
                                "This PIXORA account is already linked to another Google account."
                        );

                        return;
                    }

                    /*
                     * Existing PIXORA account:
                     *
                     * LOCAL -> BOTH
                     */
                    userDAO.linkGoogleAccount(
                            user.getUserId(),
                            googleSub
                    );

                    auditDAO.log(
                            user.getUserId(),
                            "UPDATE",
                            "USER",
                            user.getUserId(),
                            "Google account linked to existing PIXORA account"
                    );

                    user =
                            userDAO.findById(
                                    user.getUserId()
                            );

                } else {

                    /*
                     * -------------------------------------------------
                     * Completely new user.
                     *
                     * Google registrations become CUSTOMER only.
                     * -------------------------------------------------
                     */

                    String username =
                            userDAO
                                    .generateGoogleUsername(
                                            email
                                    );

                    int userId =
                            userDAO
                                    .createGoogleCustomer(
                                            username,
                                            email,
                                            fullName,
                                            googleSub
                                    );

                    user =
                            userDAO.findById(
                                    userId
                            );

                    auditDAO.log(
                            userId,
                            "CREATE",
                            "USER",
                            userId,
                            "Customer account created using Google sign-in"
                    );
                }
            }

            /*
             * Same PIXORA status rules as normal login.
             */
            if (!"ACTIVE".equals(
                    user.getStatus()
            )) {

                fail(
                        req,
                        resp,
                        "This PIXORA account is suspended or inactive."
                );

                return;
            }

            /*
             * Prevent session fixation:
             *
             * destroy old session and issue a new one.
             */
            session.invalidate();

            session =
                    req.getSession(
                            true
                    );

            /*
             * Password hash should never be stored
             * inside the web session.
             */
            user.setPasswordHash(
                    null
            );

            session.setAttribute(
                    "authUser",
                    user
            );

            /*
             * Same timeout as your original LoginServlet.
             */
            session.setMaxInactiveInterval(
                    30 * 60
            );

            auditDAO.log(
                    user.getUserId(),
                    "LOGIN",
                    "USER",
                    user.getUserId(),
                    "Signed in with Google"
            );

            /*
             * Existing PIXORA dashboard routing takes over
             * from here according to the user's role.
             */
            resp.sendRedirect(
                    req.getContextPath()
                            + "/dashboard"
            );

        } catch (Exception e) {

            e.printStackTrace();

            fail(
                    req,
                    resp,
                    "Google sign-in failed. Please try again or use your PIXORA password."
            );
        }
    }

    /* =====================================================
       EXCHANGE AUTHORIZATION CODE FOR ACCESS TOKEN
       ===================================================== */

    private String exchangeCodeForAccessToken(
            HttpServletRequest req,
            String code
    ) throws Exception {

        String clientId =
                AppConfig.get(
                                "google.clientId",
                                ""
                        )
                        .trim();

        String clientSecret =
                AppConfig.get(
                                "google.clientSecret",
                                ""
                        )
                        .trim();

        if (clientId.isEmpty()
                || clientSecret.isEmpty()) {

            throw new IllegalStateException(
                    "Google OAuth credentials are missing."
            );
        }

        String requestBody =
                "code="
                        + encode(code)

                        + "&client_id="
                        + encode(clientId)

                        + "&client_secret="
                        + encode(clientSecret)

                        + "&redirect_uri="
                        + encode(
                        GoogleLoginServlet
                                .redirectUri(req)
                )

                        + "&grant_type="
                        + "authorization_code";

        HttpRequest tokenRequest =
                HttpRequest
                        .newBuilder(
                                URI.create(
                                        "https://oauth2.googleapis.com/token"
                                )
                        )
                        .timeout(
                                Duration.ofSeconds(20)
                        )
                        .header(
                                "Content-Type",
                                "application/x-www-form-urlencoded"
                        )
                        .POST(
                                HttpRequest
                                        .BodyPublishers
                                        .ofString(
                                                requestBody
                                        )
                        )
                        .build();

        HttpResponse<String> tokenResponse =
                HTTP.send(
                        tokenRequest,
                        HttpResponse
                                .BodyHandlers
                                .ofString()
                );

        if (tokenResponse.statusCode() < 200
                || tokenResponse.statusCode() >= 300) {

            throw new IOException(
                    "Google token exchange failed with HTTP "
                            + tokenResponse.statusCode()
            );
        }

        JsonNode json =
                JSON.readTree(
                        tokenResponse.body()
                );

        String token =
                text(
                        json,
                        "access_token"
                );

        if (token == null) {

            throw new IOException(
                    "Google token response did not contain an access token."
            );
        }

        return token;
    }

    /* =====================================================
       LOAD GOOGLE IDENTITY
       ===================================================== */

    private JsonNode loadGoogleProfile(
            String accessToken
    ) throws Exception {

        HttpRequest profileRequest =
                HttpRequest
                        .newBuilder(
                                URI.create(
                                        "https://openidconnect.googleapis.com/v1/userinfo"
                                )
                        )
                        .timeout(
                                Duration.ofSeconds(20)
                        )
                        .header(
                                "Authorization",
                                "Bearer " + accessToken
                        )
                        .GET()
                        .build();

        HttpResponse<String> profileResponse =
                HTTP.send(
                        profileRequest,
                        HttpResponse
                                .BodyHandlers
                                .ofString()
                );

        if (profileResponse.statusCode() < 200
                || profileResponse.statusCode() >= 300) {

            throw new IOException(
                    "Google user info request failed with HTTP "
                            + profileResponse.statusCode()
            );
        }

        return JSON.readTree(
                profileResponse.body()
        );
    }

    /* =====================================================
       ERROR HANDLING
       ===================================================== */

    private void fail(
            HttpServletRequest req,
            HttpServletResponse resp,
            String message
    ) throws IOException {

        HttpSession session =
                req.getSession(true);

        String source =
                (String)
                        session.getAttribute(
                                "googleOAuthSource"
                        );

        session.removeAttribute(
                "googleOAuthSource"
        );

        session.setAttribute(
                "flashType",
                "danger"
        );

        session.setAttribute(
                "flashMessage",
                message
        );

        /*
         * If OAuth started from registration,
         * return there.
         *
         * Otherwise return to login.
         */
        if ("register".equals(source)) {

            resp.sendRedirect(
                    req.getContextPath()
                            + "/register"
            );

        } else {

            resp.sendRedirect(
                    req.getContextPath()
                            + "/login"
            );
        }
    }

    private static String text(
            JsonNode node,
            String field
    ) {

        JsonNode value =
                node.get(field);

        if (value == null
                || value.isNull()) {

            return null;
        }

        return value.asText();
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