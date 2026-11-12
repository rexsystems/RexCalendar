package me.rexsystems.rexCalendar.claim;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class DayKeysTest {

    @Test
    void bucharestMidnightIsNotUtcMidnight() {
        Instant stillToday = Instant.parse("2026-12-01T21:30:00Z");
        Instant nextMorning = Instant.parse("2026-12-01T22:30:00Z");
        ZoneId bucharest = ZoneId.of("Europe/Bucharest");

        assertEquals("2026-12-01", DayKeys.key(stillToday, bucharest));
        assertEquals(1, DayKeys.dayOfMonth(stillToday, bucharest));
        assertEquals("2026-12-02", DayKeys.key(nextMorning, bucharest));
        assertEquals(2, DayKeys.dayOfMonth(nextMorning, bucharest));
        assertEquals("2026-12-01", DayKeys.key(nextMorning, ZoneId.of("UTC")));
    }

    @Test
    void invalidZoneFallsBackToBucharest() {
        assertEquals(DayKeys.DEFAULT_ZONE, DayKeys.zoneOrDefault(null));
        assertEquals(DayKeys.DEFAULT_ZONE, DayKeys.zoneOrDefault("   "));
        assertEquals(DayKeys.DEFAULT_ZONE, DayKeys.zoneOrDefault("Not/AZone"));
        assertEquals(ZoneId.of("UTC"), DayKeys.zoneOrDefault("UTC"));
        assertEquals(ZoneId.of("Europe/Bucharest"), DayKeys.zoneOrDefault(" Europe/Bucharest "));
    }

    @Test
    void doorKeyUsesTheMonthOfToday() {
        assertEquals("2026-03-05", DayKeys.keyForDoor(LocalDate.of(2026, 3, 20), 5));
        assertEquals("2026-04-05", DayKeys.keyForDoor(LocalDate.of(2026, 4, 2), 5));
        assertEquals("2026-12-01", DayKeys.monthStart(LocalDate.of(2026, 12, 20)));
        assertEquals("2027-01-01", DayKeys.nextMonthStart(LocalDate.of(2026, 12, 20)));
    }

    @Test
    void doorPastTheMonthLengthHasNoKey() {
        LocalDate february = LocalDate.of(2026, 2, 10);
        assertEquals("2026-02-28", DayKeys.keyForDoor(february, 28));
        assertNull(DayKeys.keyForDoor(february, 30));
        assertNull(DayKeys.keyForDoor(february, 0));
    }
}
