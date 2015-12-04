package com.adjust.sdk.test;

import junit.framework.Assert;

import java.util.Locale;

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

    public void test(String message, Object... parameters) {
        Assert.assertTrue(mockLogger.toString(),
                mockLogger.containsTestMessage(String.format(Locale.US, message, parameters)));
    }

    public void verbose(String message, Object... parameters) {
        Assert.assertTrue(mockLogger.toString(),
                mockLogger.containsMessage(VERBOSE, String.format(Locale.US, message, parameters)));
    }

    public void debug(String message, Object... parameters) {
        Assert.assertTrue(mockLogger.toString(),
                mockLogger.containsMessage(DEBUG, String.format(Locale.US, message, parameters)));
    }

    public void info(String message, Object... parameters) {
        Assert.assertTrue(mockLogger.toString(),
                mockLogger.containsMessage(INFO, String.format(Locale.US, message, parameters)));
    }

    public void warn(String message, Object... parameters) {
        Assert.assertTrue(mockLogger.toString(),
                mockLogger.containsMessage(WARN, String.format(Locale.US, message, parameters)));
    }

    public void error(String message, Object... parameters) {
        Assert.assertTrue(mockLogger.toString(),
                mockLogger.containsMessage(ERROR, String.format(Locale.US, message, parameters)));
    }

    public void Assert(String message, Object... parameters) {
        Assert.assertTrue(mockLogger.toString(),
                mockLogger.containsMessage(ASSERT, String.format(Locale.US, message, parameters)));
    }

    public void notInTest(String message, Object... parameters) {
        Assert.assertFalse(mockLogger.toString(),
                mockLogger.containsTestMessage(message));
    }

    public void notInVerbose(String message, Object... parameters) {
        Assert.assertFalse(mockLogger.toString(),
                mockLogger.containsMessage(VERBOSE, String.format(Locale.US, message, parameters)));
    }

    public void notInDebug(String message, Object... parameters) {
        Assert.assertFalse(mockLogger.toString(),
                mockLogger.containsMessage(DEBUG, String.format(Locale.US, message, parameters)));
    }

    public void notInInfo(String message, Object... parameters) {
        Assert.assertFalse(mockLogger.toString(),
                mockLogger.containsMessage(INFO, String.format(Locale.US, message, parameters)));
    }

    public void notInWarn(String message, Object... parameters) {
        Assert.assertFalse(mockLogger.toString(),
                mockLogger.containsMessage(WARN, String.format(Locale.US, message, parameters)));
    }

    public void notInError(String message, Object... parameters) {
        Assert.assertFalse(mockLogger.toString(),
                mockLogger.containsMessage(ERROR, String.format(Locale.US, message, parameters)));
    }

    public void notInAssert(String message, Object... parameters) {
        Assert.assertFalse(mockLogger.toString(),
                mockLogger.containsMessage(ASSERT, String.format(Locale.US, message, parameters)));
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
