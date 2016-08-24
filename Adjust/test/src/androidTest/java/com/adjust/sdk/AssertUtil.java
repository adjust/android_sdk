package com.adjust.sdk;

import android.net.Uri;

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

    public String test(String message, Object... parameters) {
        MockLogger.ContainsReturn containsTestMessage = mockLogger.containsTestMessage(String.format(Locale.US, message, parameters));
        Assert.assertTrue(mockLogger.toString(), containsTestMessage.containsMessage);
        return containsTestMessage.matchMessage;
    }

    public String verbose(String message, Object... parameters) {
        MockLogger.ContainsReturn containsVerboseMessage = mockLogger.containsMessage(VERBOSE, String.format(Locale.US, message, parameters));
        Assert.assertTrue(mockLogger.toString(), containsVerboseMessage.containsMessage);
        return containsVerboseMessage.matchMessage;
    }

    public String debug(String message, Object... parameters) {
        MockLogger.ContainsReturn containsDebugMessage = mockLogger.containsMessage(DEBUG, String.format(Locale.US, message, parameters));
        Assert.assertTrue(mockLogger.toString(), containsDebugMessage.containsMessage);
        return containsDebugMessage.matchMessage;
    }

    public String info(String message, Object... parameters) {
        MockLogger.ContainsReturn containsInfoMessage = mockLogger.containsMessage(INFO, String.format(Locale.US, message, parameters));
        Assert.assertTrue(mockLogger.toString(), containsInfoMessage.containsMessage);
        return containsInfoMessage.matchMessage;
    }

    public String warn(String message, Object... parameters) {
        MockLogger.ContainsReturn containsWarnMessage = mockLogger.containsMessage(WARN, String.format(Locale.US, message, parameters));
        Assert.assertTrue(mockLogger.toString(), containsWarnMessage.containsMessage);
        return containsWarnMessage.matchMessage;
    }

    public String error(String message, Object... parameters) {
        MockLogger.ContainsReturn containsErrorMessage = mockLogger.containsMessage(ERROR, String.format(Locale.US, message, parameters));
        Assert.assertTrue(mockLogger.toString(), containsErrorMessage.containsMessage);
        return containsErrorMessage.matchMessage;
    }

    public String Assert(String message, Object... parameters) {
        MockLogger.ContainsReturn containsAssertMessage = mockLogger.containsMessage(ASSERT, String.format(Locale.US, message, parameters));
        Assert.assertTrue(mockLogger.toString(), containsAssertMessage.containsMessage);
        return containsAssertMessage.matchMessage;
    }

    public void notInTest(String message, Object... parameters) {
        MockLogger.ContainsReturn containsTestMessage = mockLogger.containsTestMessage(String.format(Locale.US, message, parameters));
        Assert.assertFalse(mockLogger.toString(), containsTestMessage.containsMessage);
    }

    public void notInVerbose(String message, Object... parameters) {
        MockLogger.ContainsReturn containsVerboseMessage = mockLogger.containsMessage(VERBOSE, String.format(Locale.US, message, parameters));
        Assert.assertFalse(mockLogger.toString(), containsVerboseMessage.containsMessage);
    }

    public void notInDebug(String message, Object... parameters) {
        MockLogger.ContainsReturn containsDebugMessage = mockLogger.containsMessage(DEBUG, String.format(Locale.US, message, parameters));
        Assert.assertFalse(mockLogger.toString(), containsDebugMessage.containsMessage);
    }

    public void notInInfo(String message, Object... parameters) {
        MockLogger.ContainsReturn containsInfoMessage = mockLogger.containsMessage(INFO, String.format(Locale.US, message, parameters));
        Assert.assertFalse(mockLogger.toString(), containsInfoMessage.containsMessage);
    }

    public void notInWarn(String message, Object... parameters) {
        MockLogger.ContainsReturn containsWarnMessage = mockLogger.containsMessage(WARN, String.format(Locale.US, message, parameters));
        Assert.assertFalse(mockLogger.toString(), containsWarnMessage.containsMessage);
    }

    public void notInError(String message, Object... parameters) {
        MockLogger.ContainsReturn containsErrorMessage = mockLogger.containsMessage(ERROR, String.format(Locale.US, message, parameters));
        Assert.assertFalse(mockLogger.toString(), containsErrorMessage.containsMessage);
    }

    public void notInAssert(String message, Object... parameters) {
        MockLogger.ContainsReturn containsAssertMessage = mockLogger.containsMessage(ASSERT, String.format(Locale.US, message, parameters));
        Assert.assertFalse(mockLogger.toString(), containsAssertMessage.containsMessage);
    }

    public void isNull(Object object) {
        Assert.assertNull(mockLogger.toString(), object);
    }

    public void isNotNull(Object object) {
        Assert.assertNotNull(mockLogger.toString(), object);
    }

    public void isTrue(boolean value) {
        Assert.assertTrue(mockLogger.toString(), value);
    }

    public void isFalse(boolean value) {
        Assert.assertFalse(mockLogger.toString(), value);
    }

    public void isEqual(String expected, String actual) {
        Assert.assertEquals(mockLogger.toString(), expected, actual);
    }

    public void isEqual(boolean expected, boolean actual) {
        Assert.assertEquals(mockLogger.toString(), expected, actual);
    }

    public void isEqual(int expected, int actual) {
        Assert.assertEquals(mockLogger.toString(), expected, actual);
    }

    public void isEqual(Uri expected, Uri actual) {
        Assert.assertEquals(mockLogger.toString(), expected, actual);
    }

    public void fail() {
        Assert.fail(mockLogger.toString());
    }

    public void fail(String extraMessage) {
        Assert.fail(extraMessage + "\n" + mockLogger.toString());
    }

}
