package me.rexsystems.rexCalendar.claim;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MissedDayPolicyTest {

    @Test
    void todayOnlyOpensTheCurrentDoor() {
        MissedDayPolicy policy = MissedDayPolicy.TODAY_ONLY;
        assertEquals(ClaimAccess.OPEN, policy.access(5, 5));
        assertEquals(ClaimAccess.MISSED, policy.access(4, 5));
        assertEquals(ClaimAccess.TOO_EARLY, policy.access(6, 5));
        assertEquals(ClaimAccess.TOO_EARLY, policy.access(0, 5));
    }

    @Test
    void claimPastOpensEarlierDoors() {
        MissedDayPolicy policy = MissedDayPolicy.CLAIM_PAST;
        assertEquals(ClaimAccess.OPEN, policy.access(5, 5));
        assertEquals(ClaimAccess.OPEN, policy.access(1, 24));
        assertEquals(ClaimAccess.TOO_EARLY, policy.access(25, 24));
        assertEquals(ClaimAccess.TOO_EARLY, policy.access(0, 24));
    }

    @Test
    void unknownValueFallsBackToTodayOnly() {
        assertEquals(MissedDayPolicy.CLAIM_PAST, MissedDayPolicy.parse("claim-past"));
        assertEquals(MissedDayPolicy.CLAIM_PAST, MissedDayPolicy.parse(" CLAIM-PAST "));
        assertEquals(MissedDayPolicy.TODAY_ONLY, MissedDayPolicy.parse("today-only"));
        assertEquals(MissedDayPolicy.TODAY_ONLY, MissedDayPolicy.parse("nope"));
        assertEquals(MissedDayPolicy.TODAY_ONLY, MissedDayPolicy.parse(null));
    }
}
