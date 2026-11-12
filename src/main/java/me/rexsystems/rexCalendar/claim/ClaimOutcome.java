package me.rexsystems.rexCalendar.claim;

public final class ClaimOutcome {

    private final ClaimResult result;
    private final boolean stored;
    private final int day;
    private final String date;

    private ClaimOutcome(ClaimResult result, boolean stored, int day, String date) {
        this.result = result;
        this.stored = stored;
        this.day = day;
        this.date = date == null ? "" : date;
    }

    public static ClaimOutcome of(ClaimResult result, boolean stored, int day, String date) {
        return new ClaimOutcome(result, stored, day, date);
    }

    public ClaimResult result() {
        return result;
    }

    public boolean stored() {
        return stored;
    }

    public int day() {
        return day;
    }

    public String date() {
        return date;
    }
}
