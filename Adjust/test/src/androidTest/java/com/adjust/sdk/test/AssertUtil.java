package com.adjust.sdk.test;

import junit.framework.Assert;

import static com.adjust.sdk.LogLevel.ASSERT;
import static com.adjust.sdk.LogLevel.DEBUG;
import static com.adjust.sdk.LogLevel.ERROR;
import static com.adjust.sdk.LogLevel.INFO;
import static com.adjust.sdk.LogLevel.VERBOSE;
import static com.adjust.sdk.LogLevel.WARN;

/**
 * Created by pfms on 09/01/15.
 */
public class AssertUtil {
    private MockLogger mockLogger;

    public AssertUtil(MockLogger mockLogger) {
        this.mockLogger = mockLogger;
    }

    public void test(String message) {
        Assert.assertTrue(mockLogger.toString(),
                mockLogger.containsTestMessage(message));
    }

    public void verbose(String message) {
        Assert.assertTrue(mockLogger.toString(),
                mockLogger.containsMessage(VERBOSE, message));
    }

    public void debug(String message) {
        Assert.assertTrue(mockLogger.toString(),
                mockLogger.containsMessage(DEBUG, message));
    }

    public void info(String message) {
        Assert.assertTrue(mockLogger.toString(),
                mockLogger.containsMessage(INFO, message));
    }

    public void warn(String message) {
        Assert.assertTrue(mockLogger.toString(),
                mockLogger.containsMessage(WARN, message));
    }

    public void error(String message) {
        Assert.assertTrue(mockLogger.toString(),
                mockLogger.containsMessage(ERROR, message));
    }

    public void Assert(String message) {
        Assert.assertTrue(mockLogger.toString(),
                mockLogger.containsMessage(ASSERT, message));
    }

    public void notInTest(String message) {
        Assert.assertFalse(mockLogger.toString(),
                mockLogger.containsTestMessage(message));
    }

    public void notInVerbose(String message) {
        Assert.assertFalse(mockLogger.toString(),
                mockLogger.containsMessage(VERBOSE, message));
    }

    public void notInDebug(String message) {
        Assert.assertFalse(mockLogger.toString(),
                mockLogger.containsMessage(DEBUG, message));
    }

    public void notInInfo(String message) {
        Assert.assertFalse(mockLogger.toString(),
                mockLogger.containsMessage(INFO, message));
    }

    public void notInWarn(String message) {
        Assert.assertFalse(mockLogger.toString(),
                mockLogger.containsMessage(WARN, message));
    }

    public void notInError(String message) {
        Assert.assertFalse(mockLogger.toString(),
                mockLogger.containsMessage(ERROR, message));
    }

    public void notInAssert(String message) {
        Assert.assertFalse(mockLogger.toString(),
                mockLogger.containsMessage(ASSERT, message));
    }

    public void isNull(Object object) {
        Assert.assertNull(mockLogger.toString(),
                object);
    }

    public void isNotNull(Object object) {
        Assert.assertNotNull(mockLogger.toString(),
                object);
    }

    public void isTrue(boolean value) {
        Assert.assertTrue(mockLogger.toString(),
                value);
    }

    public void isFalse(boolean value) {
        Assert.assertFalse(mockLogger.toString(),
                value);
    }

    public void isEqual(String expected, String actual) {
        Assert.assertEquals(mockLogger.toString(),
                expected, actual);
    }

    public void fail() {
        Assert.fail(mockLogger.toString());
    }
}
