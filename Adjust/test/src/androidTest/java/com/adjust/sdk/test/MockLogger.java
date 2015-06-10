package com.adjust.sdk.test;

import android.util.Log;
import android.util.SparseArray;

import com.adjust.sdk.ILogger;
import com.adjust.sdk.LogLevel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static com.adjust.sdk.Constants.LOGTAG;

public class MockLogger implements ILogger {

    private StringBuffer logBuffer;
    private SparseArray<ArrayList<String>> logMap;

    public MockLogger() {
        reset();
    }

    public void reset() {
        logBuffer = new StringBuffer();
        logMap = new SparseArray<ArrayList<String>>(7);
        logMap.put(LogLevel.ASSERT.getAndroidLogLevel(), new ArrayList<String>());
        logMap.put(LogLevel.DEBUG.getAndroidLogLevel(), new ArrayList<String>());
        logMap.put(LogLevel.ERROR.getAndroidLogLevel(), new ArrayList<String>());
        logMap.put(LogLevel.INFO.getAndroidLogLevel(), new ArrayList<String>());
        logMap.put(LogLevel.VERBOSE.getAndroidLogLevel(), new ArrayList<String>());
        logMap.put(LogLevel.WARN.getAndroidLogLevel(), new ArrayList<String>());
        // logging test level == 1
        logMap.put(1, new ArrayList<String>());

        test("Logger reset");
    }

    @Override
    public String toString() {
        String logging = logBuffer.toString();
        //Log.v("TestLogger ", logging);
        return logging;
    }

    @Override
    public void setLogLevel(LogLevel logLevel) {
        test("MockLogger setLogLevel: " + logLevel);
    }

    @Override
    public void setLogLevelString(String logLevelString) {
    }

    private void logMessage(String message, Integer iLoglevel, String messagePrefix, int priority) {
        logBuffer.append(messagePrefix + message + System.getProperty("line.separator"));
        Log.println(priority, LOGTAG, messagePrefix + message);

        List<String> prefixedList = logMap.get(iLoglevel);
        prefixedList.add(message);
    }

    @Override
    public void verbose(String message, Object... parameters) {
        logMessage(String.format(Locale.US, message, parameters),
                LogLevel.VERBOSE.getAndroidLogLevel(),
                "v ",
                Log.VERBOSE);
    }

    @Override
    public void debug(String message, Object... parameters) {
        logMessage(String.format(Locale.US, message, parameters),
                LogLevel.DEBUG.getAndroidLogLevel(),
                "d ",
                Log.DEBUG);
    }

    @Override
    public void info(String message, Object... parameters) {
        logMessage(String.format(Locale.US, message, parameters),
                LogLevel.INFO.getAndroidLogLevel(),
                "i ",
                Log.INFO);
    }

    @Override
    public void warn(String message, Object... parameters) {
        logMessage(String.format(Locale.US, message, parameters),
                LogLevel.WARN.getAndroidLogLevel(),
                "w ",
                Log.WARN);
    }

    @Override
    public void error(String message, Object... parameters) {
        logMessage(String.format(Locale.US, message, parameters),
                LogLevel.ERROR.getAndroidLogLevel(),
                "e ",
                Log.ERROR);
    }

    @Override
    public void Assert(String message, Object... parameters) {
        logMessage(String.format(Locale.US, message, parameters),
                LogLevel.ASSERT.getAndroidLogLevel(),
                "a ",
                Log.ASSERT);
    }

    public void test(String message) {
        logMessage(message, 1, "t ", Log.VERBOSE);
    }

    private Boolean mapContainsMessage(int level, String beginsWith) {
        ArrayList<String> list = logMap.get(level);
        @SuppressWarnings("unchecked")
        ArrayList<String> listCopy = (ArrayList<String>) list.clone();
        String sList = Arrays.toString(list.toArray());
        for (String log : list) {
            listCopy.remove(0);
            if (log.startsWith(beginsWith)) {
                //test(log + " found");
                Log.println(Log.ASSERT, LOGTAG, String.format(Locale.US, "%s found", log));
                logMap.put(level, listCopy);
                return true;
            }
        }
        Log.println(Log.ASSERT, LOGTAG, String.format(Locale.US, "%s does not contain %s", sList, beginsWith));

        return false;
    }

    public Boolean containsMessage(LogLevel level, String beginsWith) {
        return mapContainsMessage(level.getAndroidLogLevel(), beginsWith);
    }

    public Boolean containsTestMessage(String beginsWith) {
        return mapContainsMessage(1, beginsWith);
    }
}
