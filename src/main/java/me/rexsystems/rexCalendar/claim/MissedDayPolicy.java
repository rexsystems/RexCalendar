package me.rexsystems.rexCalendar.claim;

import java.util.Locale;

public enum MissedDayPolicy {
    TODAY_ONLY,
    CLAIM_PAST;

    public static MissedDayPolicy parse(String raw) {
        if (raw != null && raw.trim().toLowerCase(Locale.ROOT).equals("claim-past")) {
            return CLAIM_PAST;
        }
        return TODAY_ONLY;
    }

    public ClaimAccess access(int day, int today) {
        if (day < 1 || day > today) {
            return ClaimAccess.TOO_EARLY;
        }
        if (this == TODAY_ONLY && day < today) {
            return ClaimAccess.MISSED;
        }
        return ClaimAccess.OPEN;
    }
}
