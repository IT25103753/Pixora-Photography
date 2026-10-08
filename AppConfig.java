package com.pixora.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public final class AppConfig {

    private static final Properties P = new Properties();

    static {
        try (InputStream in =
                     AppConfig.class
                             .getClassLoader()
                             .getResourceAsStream("db.properties")) {

            if (in == null) {
                throw new IllegalStateException("db.properties not found.");
            }

            P.load(in);

        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private AppConfig() {
    }

    public static int getInt(String key, int fallback) {

        try {
            return Integer.parseInt(
                    P.getProperty(
                            key,
                            String.valueOf(fallback)
                    ).trim()
            );

        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    public static String get(String key, String fallback) {

        /*
         * Example:
         *
         * google.clientId
         * becomes
         * GOOGLE_CLIENT_ID
         *
         * google.clientSecret
         * becomes
         * GOOGLE_CLIENT_SECRET
         */

        String envKey =
                key.replaceAll(
                                "([a-z0-9])([A-Z])",
                                "$1_$2"
                        )
                        .replace('.', '_')
                        .toUpperCase();

        String environmentValue =
                System.getenv(envKey);

        /*
         * Environment variables have priority.
         * This is safer for production deployment.
         */
        if (environmentValue != null
                && !environmentValue.trim().isEmpty()) {

            return environmentValue.trim();
        }

        String value =
                P.getProperty(key, fallback);

        if (value == null) {
            return fallback;
        }

        return value.replace(
                "${user.home}",
                System.getProperty("user.home")
        );
    }

    public static Path uploadRoot() {

        return Paths.get(
                        get(
                                "app.uploadRoot",
                                System.getProperty("user.home")
                                        + "/pixora_uploads"
                        )
                )
                .toAbsolutePath()
                .normalize();
    }
}