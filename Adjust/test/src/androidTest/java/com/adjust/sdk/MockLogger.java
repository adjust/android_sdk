package com.adjust.sdk;

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
    public static final int TEST_LEVEL = 6;
    public static final int CHECK_LEVEL = 7;
    private long startTime;
    private long lastTime;

    public class ContainsReturn {
        boolean containsMessage;
        String matchMessage;
        ContainsReturn(boolean containsMessage, String matchMessage) {
            this.containsMessage = containsMessage;
            this.matchMessage = matchMessage;
        }
    }

    public MockLogger() {
        reset();
    }

    public void reset() {
        startTime = System.currentTimeMillis();
        lastTime = startTime;

        logBuffer = new StringBuffer();
        logMap = new SparseArray<ArrayList<String>>(8);
        logMap.put(LogLevel.VERBOSE.getAndroidLogLevel(), new ArrayList<String>());
        logMap.put(LogLevel.DEBUG.getAndroidLogLevel(), new ArrayList<String>());
        logMap.put(LogLevel.INFO.getAndroidLogLevel(), new ArrayList<String>());
        logMap.put(LogLevel.WARN.getAndroidLogLevel(), new ArrayList<String>());
        logMap.put(LogLevel.ERROR.getAndroidLogLevel(), new ArrayList<String>());
        logMap.put(LogLevel.ASSERT.getAndroidLogLevel(), new ArrayList<String>());
        logMap.put(TEST_LEVEL, new ArrayList<String>());
        logMap.put(CHECK_LEVEL, new ArrayList<String>());

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
        long now = System.currentTimeMillis();
        long milliSecondsPassed = now - startTime;
        long lastTimePassed = now - lastTime;
        lastTime = now;

        List<String> prefixedList = logMap.get(iLoglevel);
        prefixedList.add(message);

        String longMessage = String.format("%d %d %s %s", milliSecondsPassed, lastTimePassed, messagePrefix, message);
        Log.println(priority, LOGTAG, longMessage);

        String logBufferMessage = String.format("%s%s", longMessage, System.getProperty("line.separator"));
        logBuffer.append(logBufferMessage);
        //String sList = Arrays.toString(prefixedList.toArray());
        //String logBufferList = String.format("In %s", sList);
        //logBuffer.append(logBufferList);
    }

    @Override
    public void verbose(String message, Object... parameters) {
        logMessage(String.format(Locale.US, message, parameters),
                LogLevel.VERBOSE.getAndroidLogLevel(),
                "v",
                Log.VERBOSE);
    }

    @Override
    public void debug(String message, Object... parameters) {
        logMessage(String.format(Locale.US, message, parameters),
                LogLevel.DEBUG.getAndroidLogLevel(),
                "d",
                Log.DEBUG);
    }

    @Override
    public void info(String message, Object... parameters) {
        logMessage(String.format(Locale.US, message, parameters),
                LogLevel.INFO.getAndroidLogLevel(),
                "i",
                Log.INFO);
    }

    @Override
    public void warn(String message, Object... parameters) {
        logMessage(String.format(Locale.US, message, parameters),
                LogLevel.WARN.getAndroidLogLevel(),
                "w",
                Log.WARN);
    }

    @Override
    public void error(String message, Object... parameters) {
        logMessage(String.format(Locale.US, message, parameters),
                LogLevel.ERROR.getAndroidLogLevel(),
                "e",
                Log.ERROR);
    }

    @Override
    public void Assert(String message, Object... parameters) {
        logMessage(String.format(Locale.US, message, parameters),
                LogLevel.ASSERT.getAndroidLogLevel(),
                "a",
                Log.ASSERT);
    }

    @Override
    public void lockLogLevel() {
        test("MockLogger lockLogLevel");
    }

    public void test(String message) {
        logMessage(message, TEST_LEVEL, "t", Log.VERBOSE);
    }

    private void check(String message) {
        logMessage(message, CHECK_LEVEL, "c", Log.VERBOSE);
    }

    private ContainsReturn mapContainsMessage(int level, String beginsWith) {
        ArrayList<String> list = logMap.get(level);
        @SuppressWarnings("unchecked")
        ArrayList<String> listCopy = new ArrayList<String>(list);
        String sList = Arrays.toString(list.toArray());
        for (String log : list) {
            listCopy.remove(0);
            if (log.startsWith(beginsWith)) {
                String foundMessage = String.format(Locale.US, "%s found", log);
                //Log.println(Log.ASSERT, LOGTAG, foundMessage);
                check(foundMessage);
                logMap.put(level, listCopy);
                return new ContainsReturn(true, log);
            }
        }
        String notFoundMessage = String.format(Locale.US, "%s is not in %s", beginsWith, sList);
        check(notFoundMessage);
        //Log.println(Log.ASSERT, LOGTAG, notFoundMessage);

        return new ContainsReturn(false, null);
    }

    public ContainsReturn containsMessage(LogLevel level, String beginsWith) {
        return mapContainsMessage(level.getAndroidLogLevel(), beginsWith);
    }

    public ContainsReturn containsTestMessage(String beginsWith) {
        return mapContainsMessage(TEST_LEVEL, beginsWith);
    }

    public void printLogMap(int level) {
        ArrayList<String> list = logMap.get(level);
        String sList = Arrays.toString(list.toArray());

        String message = String.format(Locale.US, "list level %d: %s", level, sList);
        check(message);
    }
}
