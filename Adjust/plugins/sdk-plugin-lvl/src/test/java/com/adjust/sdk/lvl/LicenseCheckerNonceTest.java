package com.adjust.sdk.lvl;

import org.junit.Test;

import static org.junit.Assert.*;

public class LicenseCheckerNonceTest {

    @Test
    public void testGenerateNonce_containsVersionByte() {
        long timestamp = System.currentTimeMillis();
        long nonce = LicenseChecker.generateNonce(timestamp);
        assertEquals("LSB should be 0x01 (version/flag)", 0x01, nonce & 0xFF);
    }

    @Test
    public void testGenerateNonce_timestampPacking_isCorrect() {
        long timestamp = 1720000000000L; // Epoch in ms
        long expectedSeconds =timestamp & 0x00FFFFFFFFFFFFFFL;
        long nonce = LicenseChecker.generateNonce(timestamp);

        long extractedSeconds = (nonce >>> 8); // remove version byte
        assertEquals("Timestamp in nonce should match expected seconds", expectedSeconds, extractedSeconds);
    }

    @Test
    public void testGenerateNonce_zeroTimestamp_shouldStillWork() {
        long nonce = LicenseChecker.generateNonce(0L);
        assertEquals("Should only have version byte set", 0x01, nonce);
    }

    @Test
    public void testGenerateNonce_nearLimit_doesNotOverflowSafe() {
        // Use a timestamp that still fits in millis without overflowing long
        long safeMillis = (1L << 53); // 53 bits (won't overflow when *1000)

        long nonce = LicenseChecker.generateNonce(safeMillis);
        long extractedMillis = nonce >>> 8;

        assertEquals("Should safely extract 53-bit timestamp", safeMillis, extractedMillis);
    }



    @Test
    public void testGenerateNonce_truncatesIfAbove56Bits() {
        long overMaxTimestampMs = ((1L << 56) + 9999L); // milliseconds that overflow 56-bit seconds

        long nonce = LicenseChecker.generateNonce(overMaxTimestampMs);
        long extracted = (nonce >>> 8);

        assertTrue("Extracted timestamp must be less than 2^56", extracted < (1L << 56));
    }

    @Test
    public void testGenerateNonce_multipleCalls_differentResults() {
        long t1 = LicenseChecker.generateNonce(System.currentTimeMillis());
        long t2 = LicenseChecker.generateNonce(System.currentTimeMillis() + 2000);
        assertNotEquals("Nonces from different times should differ", t1, t2);
    }
}
