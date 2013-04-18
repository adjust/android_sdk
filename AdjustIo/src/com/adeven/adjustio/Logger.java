//
//  Logger.java
//  AdjustIo
//
//  Created by Benjamin Weiss on 17.4.13
//  Copyright (c) 2012 adeven. All rights reserved.
//  See the file MIT-LICENSE for copying permission.
//

package com.adeven.adjustio;

import android.util.Log;

/**
 * A Wrapper that allows easy toggles of Logging.
 *
 * @author keyboardsurfer
 * @since 17.4.13
 */
public class Logger {
    protected static final String LOGTAG = "AdjustIo";

    private static int logLevel = Log.INFO;

    public static void setLogLevel(int logLevel) {
        Logger.logLevel = logLevel;
    }

    public static void verbose(String message) {
        if (logLevel <= Log.VERBOSE) {
            Log.v(LOGTAG, message);
        }
    }

    public static void verbose(String context, String name, String value) {
        verbose("[" + context + "] " + name + ": '" + value + "'");
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

    public static void error(String message, Throwable throwable) {
        if (logLevel <= Log.ERROR) {
            Log.e(LOGTAG, message, throwable);
        }
    }
}
