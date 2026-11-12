package me.rexsystems.rexCalendar.claim;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

public final class DayKeys {

    public static final ZoneId DEFAULT_ZONE = ZoneId.of("Europe/Bucharest");

    private DayKeys() {
    }

    public static ZoneId zoneOrDefault(String id) {
        if (id == null || id.isBlank()) {
            return DEFAULT_ZONE;
        }
        try {
            return ZoneId.of(id.trim());
        } catch (DateTimeException ex) {
            return DEFAULT_ZONE;
        }
    }

    public static LocalDate today(ZoneId zone, Instant instant) {
        ZoneId resolved = zone == null ? DEFAULT_ZONE : zone;
        Instant moment = instant == null ? Instant.EPOCH : instant;
        return LocalDate.ofInstant(moment, resolved);
    }

    public static String key(Instant instant, ZoneId zone) {
        return today(zone, instant).toString();
    }

    public static int dayOfMonth(Instant instant, ZoneId zone) {
        return today(zone, instant).getDayOfMonth();
    }

    public static String keyForDoor(LocalDate today, int door) {
        if (today == null || door < 1 || door > today.lengthOfMonth()) {
            return null;
        }
        return today.withDayOfMonth(door).toString();
    }

    public static String monthStart(LocalDate today) {
        if (today == null) {
            return "";
        }
        return today.withDayOfMonth(1).toString();
    }

    public static String nextMonthStart(LocalDate today) {
        if (today == null) {
            return "";
        }
        return today.withDayOfMonth(1).plusMonths(1).toString();
    }
}
