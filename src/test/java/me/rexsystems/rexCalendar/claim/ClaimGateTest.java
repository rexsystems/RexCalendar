package me.rexsystems.rexCalendar.claim;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClaimGateTest {

    @Test
    void secondBeginIsRejectedUntilAbort() {
        ClaimGate gate = new ClaimGate();
        assertTrue(gate.begin("player", "2026-12-01"));
        assertFalse(gate.begin("player", "2026-12-01"));
        assertTrue(gate.holds("player", "2026-12-01"));
        gate.abort("player", "2026-12-01");
        assertFalse(gate.holds("player", "2026-12-01"));
        assertTrue(gate.begin("player", "2026-12-01"));
    }

    @Test
    void otherPlayerOrDayIsIndependent() {
        ClaimGate gate = new ClaimGate();
        assertTrue(gate.begin("one", "2026-12-01"));
        assertTrue(gate.begin("two", "2026-12-01"));
        assertTrue(gate.begin("one", "2026-12-02"));
        assertFalse(gate.begin(null, "2026-12-01"));
        assertFalse(gate.begin("one", ""));
    }

    @Test
    void concurrentBeginsAllowOne() throws Exception {
        ClaimGate gate = new ClaimGate();
        int threads = 8;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger wins = new AtomicInteger();
        for (int i = 0; i < threads; i++) {
            pool.submit(() -> {
                start.await();
                if (gate.begin("same", "2026-12-01")) {
                    wins.incrementAndGet();
                }
                return null;
            });
        }
        start.countDown();
        pool.shutdown();
        assertTrue(pool.awaitTermination(5, TimeUnit.SECONDS));
        assertEquals(1, wins.get());
    }
}
