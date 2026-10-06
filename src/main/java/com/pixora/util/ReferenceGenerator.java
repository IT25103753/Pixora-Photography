package com.pixora.util;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public final class ReferenceGenerator {
    private static final SecureRandom RANDOM = new SecureRandom();

    private ReferenceGenerator() {}

    public static String booking() { return build("BK"); }
    public static String complaint() { return build("CMP"); }
    public static String payment() { return build("PAY"); }
    public static String invoice() { return build("INV"); }
    public static String refund() { return build("RFD"); }

    private static String build(String prefix) {
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        int suffix = 100000 + RANDOM.nextInt(900000);
        return prefix + "-" + date + "-" + suffix;
    }
}
