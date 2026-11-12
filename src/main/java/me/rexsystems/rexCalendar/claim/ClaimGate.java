package me.rexsystems.rexCalendar.claim;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class ClaimGate {

    private final Set<String> held = ConcurrentHashMap.newKeySet();

    public boolean begin(String playerId, String dayKey) {
        if (playerId == null || playerId.isEmpty() || dayKey == null || dayKey.isEmpty()) {
            return false;
        }
        return held.add(playerId + "\0" + dayKey);
    }

    public void abort(String playerId, String dayKey) {
        if (playerId == null || dayKey == null) {
            return;
        }
        held.remove(playerId + "\0" + dayKey);
    }

    public boolean holds(String playerId, String dayKey) {
        if (playerId == null || dayKey == null) {
            return false;
        }
        return held.contains(playerId + "\0" + dayKey);
    }
}
