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

public class Logger {
    protected static final String LOGTAG = "AdjustIo";

    private static int logLevel = Log.INFO;

    protected static void setLogLevel(int logLevel) {
        Logger.logLevel = logLevel;
    }

    protected static int getLogLevel() {
        return logLevel;
    }

    public static void verbose(String message) {
        if (logLevel <= Log.VERBOSE) {
            Log.v(LOGTAG, message);
        }
    }

    public static void debug(String message) {
        if (logLevel <= Log.DEBUG) {
            Log.d(LOGTAG, message);
        }
    }

    public static void info(String message) {
        if (logLevel <= Log.INFO) {
            Log.i(LOGTAG, message);
        }
    }

    public static void warn(String message) {
        if (logLevel <= Log.WARN) {
            Log.w(LOGTAG, message);
        }
    }

    public static void error(String message) {
        if (logLevel <= Log.ERROR) {
            Log.e(LOGTAG, message);
        }
    }

    public static void Assert(String message) {
        Log.println(Log.ASSERT, LOGTAG, message);
    }
}
