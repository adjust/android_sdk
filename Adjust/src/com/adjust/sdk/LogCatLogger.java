//
//  Logger.java
//  Adjust
//
//  Created by Christian Wellenbrock on 2013-04-18.
//  Copyright (c) 2013 adeven. All rights reserved.
//  See the file MIT-LICENSE for copying permission.
//

package com.adjust.sdk;

import java.util.Locale;
import android.util.Log;
import static com.adjust.sdk.Constants.LOGTAG;

public class LogCatLogger implements Logger {

    private LogLevel logLevel;

    public LogCatLogger() {
        setLogLevel(LogLevel.INFO);
    }

    @Override
    public void setLogLevel(LogLevel logLevel) {
        this.logLevel = logLevel;
    }

    @Override
    public void setLogLevelString(String logLevelString) {
        if (null != logLevelString) {
            try {
                setLogLevel(LogLevel.valueOf(logLevelString.toUpperCase(Locale.US)));
            } catch (IllegalArgumentException iae) {
                error(String.format("Malformed logLevel '%s', falling back to 'info'", logLevelString));
            }
        }
    }

    @Override
    public void verbose(String message) {
        if (logLevel.androidLogLevel <= Log.VERBOSE) {
            Log.v(LOGTAG, message);
        }
    }

    @Override
    public void debug(String message) {
        if (logLevel.androidLogLevel <= Log.DEBUG) {
            Log.d(LOGTAG, message);
        }
    }

    @Override
    public void info(String message) {
        if (logLevel.androidLogLevel <= Log.INFO) {
            Log.i(LOGTAG, message);
        }
    }

    @Override
    public void warn(String message) {
        if (logLevel.androidLogLevel <= Log.WARN) {
            Log.w(LOGTAG, message);
        }
    }

    @Override
    public void error(String message) {
        if (logLevel.androidLogLevel <= Log.ERROR) {
            Log.e(LOGTAG, message);
        }
    }

    @Override
    public void Assert(String message) {
        Log.println(Log.ASSERT, LOGTAG, message);
    }
}
