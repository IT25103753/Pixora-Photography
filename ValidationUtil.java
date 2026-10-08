package com.pixora.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

public final class ValidationUtil {
    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE = Pattern.compile("^[+0-9][0-9\\s-]{7,18}$");

    private ValidationUtil() {}

    public static boolean blank(String s) {
        return s == null || s.trim().isEmpty();
    }

    public static boolean email(String s) {
        return !blank(s) && EMAIL.matcher(s.trim()).matches();
    }

    public static boolean phone(String s) {
        return blank(s) || PHONE.matcher(s.trim()).matches();
    }

    public static LocalDate date(String s) {
        try { return LocalDate.parse(s); } catch (DateTimeParseException | NullPointerException ex) { return null; }
    }

    public static LocalTime time(String s) {
        try { return LocalTime.parse(s); } catch (DateTimeParseException | NullPointerException ex) { return null; }
    }

    public static BigDecimal money(String s) {
        try {
            BigDecimal value = new BigDecimal(s);
            return value.signum() >= 0 ? value : null;
        } catch (Exception ex) {
            return null;
        }
    }

    public static int positiveInt(String s, int fallback) {
        try {
            int value = Integer.parseInt(s);
            return value > 0 ? value : fallback;
        } catch (Exception ex) {
            return fallback;
        }
    }
}
