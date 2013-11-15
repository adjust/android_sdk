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
    }

    private static LogLevel logLevel = LogLevel.INFO;

    public static void setLogLevel(LogLevel logLevel) {
        Logger.logLevel = logLevel;
    }

    public static void verbose(String message) {
        if (logLevel.androidLogLevel <= Log.VERBOSE) {
            Log.v(LOGTAG, message);
        }
    }

    public static void debug(String message) {
        if (logLevel.androidLogLevel <= Log.DEBUG) {
            Log.d(LOGTAG, message);
        }
    }

    public static void info(String message) {
        if (logLevel.androidLogLevel <= Log.INFO) {
            Log.i(LOGTAG, message);
        }
    }

    public static void warn(String message) {
        if (logLevel.androidLogLevel <= Log.WARN) {
            Log.w(LOGTAG, message);
        }
    }

    public static void error(String message) {
        if (logLevel.androidLogLevel <= Log.ERROR) {
            Log.e(LOGTAG, message);
        }
    }

    public static void Assert(String message) {
        Log.println(Log.ASSERT, LOGTAG, message);
    }
}
