package me.rexsystems.rexCalendar.claim;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IpHashesTest {

    @Test
    void hashIsStableAndIsNotTheAddress() {
        String address = "203.0.113.10";
        String hash = IpHashes.sha256Hex(address);

        assertEquals(hash, IpHashes.sha256Hex(address));
        assertEquals("631f08140b24b7274d12df3c37a1a80ce5876dafd7007d772e0114fddf88b682", hash);
        assertNotEquals(address, hash);
        assertFalse(hash.contains("."));
        assertTrue(hash.matches("[0-9a-f]{64}"));
    }

    @Test
    void differentAddressesDifferAndBlankIsEmpty() {
        assertNotEquals(IpHashes.sha256Hex("203.0.113.10"), IpHashes.sha256Hex("203.0.113.11"));
        assertEquals("", IpHashes.sha256Hex(null));
        assertEquals("", IpHashes.sha256Hex(""));
    }
}
