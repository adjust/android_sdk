//
//  Logger.java
//  AdjustIo
//
//  Created by Christian Wellenbrock on 2013-04-18.
//  Copyright (c) 2013 adeven. All rights reserved.
//  See the file MIT-LICENSE for copying permission.
//

package com.adeven.adjustio;

import android.util.Log;

import static com.adeven.adjustio.Constants.LOGTAG;

public class Logger {

    public enum LogLevel {
        VERBOSE(Log.VERBOSE), DEBUG(Log.DEBUG), INFO(Log.INFO), WARN(Log.WARN), ERROR(Log.ERROR), ASSERT(Log.ASSERT);
        private final int androidLogLevel;

        LogLevel(final int androidLogLevel) {
            this.androidLogLevel = androidLogLevel;
        }

        public int getAndroidLogLevel() {
            return androidLogLevel;
        }

        public boolean isEnabled() {
            return androidLogLevel <= Logger.androidLogLevel;
        }
    }

    private static         int      androidLogLevel = Log.INFO;
    private static         LogLevel logLevel        = LogLevel.INFO;

    protected static void setLogLevel(LogLevel logLevel) {
        Logger.logLevel = logLevel;
        Logger.androidLogLevel = logLevel.getAndroidLogLevel();
    }

    /**
     * Use {@link Logger#setLogLevel(com.adeven.adjustio.Logger.LogLevel)} instead
     *
     * @param androidLogLevel The log level which can be obtained from one of the {@link Log} constants.
     */
    @Deprecated
    protected static void setLogLevel(int androidLogLevel) {
        Logger.androidLogLevel = androidLogLevel;
        //For the sake of compatibility. Only required until setLogLevel is being removed.
        for (LogLevel logLevel : LogLevel.values()) {
            if (logLevel.androidLogLevel == androidLogLevel) {
                Logger.logLevel = logLevel;
                return;
            }
        }
    }

    protected static int getLogLevel() {
        return androidLogLevel;
    }

    protected static void verbose(String message) {
        if (logLevel.isEnabled()) {
            Log.v(LOGTAG, message);
        }
    }

    protected static void debug(String message) {
        if (logLevel.isEnabled()) {
            Log.d(LOGTAG, message);
        }
    }

    protected static void info(String message) {
        if (logLevel.isEnabled()) {
            Log.i(LOGTAG, message);
        }
    }

    protected static void warn(String message) {
        if (logLevel.isEnabled()) {
            Log.w(LOGTAG, message);
        }
    }

    protected static void error(String message) {
        if (logLevel.isEnabled()) {
            Log.e(LOGTAG, message);
        }
    }

    protected static void Assert(String message) {
        Log.println(Log.ASSERT, LOGTAG, message);
    }
}
