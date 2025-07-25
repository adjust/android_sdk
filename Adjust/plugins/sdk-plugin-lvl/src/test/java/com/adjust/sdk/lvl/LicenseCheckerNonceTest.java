package com.adjust.sdk.lvl;

import org.junit.Test;

import static org.junit.Assert.*;

public class LicenseCheckerNonceTest {

    @Test
    public void testGenerateNonce_packsTimestampCorrectly() {
        long fakeInstallTimeMillis = 1720000000000L;
        long nonce = LicenseChecker.generateNonce(fakeInstallTimeMillis);
        long reducedTimestamp = (fakeInstallTimeMillis / 1000) % (1L << 56);
        long expected = (reducedTimestamp << 8) | 0x01;
        assertEquals(expected, nonce);
    }

    @Test
    public void testGenerateNonce_uniqueForDifferentTimestamps() {
        long t1 = LicenseChecker.generateNonce(1600000000000L);
        long t2 = LicenseChecker.generateNonce(1700000000000L);
        assertNotEquals(t1, t2);
    }
}
