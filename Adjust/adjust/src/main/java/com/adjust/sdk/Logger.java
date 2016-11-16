//
//  Logger.java
//  Adjust
//
//  Created by Christian Wellenbrock on 2013-04-18.
//  Copyright (c) 2013 adjust GmbH. All rights reserved.
//  See the file MIT-LICENSE for copying permission.
//

package com.adjust.sdk;

import android.util.Log;

import java.util.Arrays;
import java.util.Locale;

import static com.adjust.sdk.Constants.LOGTAG;

public class Logger implements ILogger {
    private LogLevel logLevel;
    private boolean logLevelLocked;
    private static String formatErrorMessage = "Error formating log message: %s, with params: %s";

    public Logger() {
        setLogLevel(LogLevel.INFO);
        logLevelLocked = false;
    }

    @Override
    public final void setLogLevel(final LogLevel logLevel) {
        if (logLevelLocked) {
            return;
        }
        this.logLevel = logLevel;
    }

    @Override
    public final void setLogLevelString(final String logLevelString) {
        if (null != logLevelString) {
            try {
                setLogLevel(LogLevel.valueOf(logLevelString.toUpperCase(Locale.US)));
            } catch (IllegalArgumentException iae) {
                error("Malformed logLevel '%s', falling back to 'info'", logLevelString);
            }
        }
    }

    @Override
    public final void verbose(final String message, final Object... parameters) {
        if (logLevel.androidLogLevel <= Log.VERBOSE) {
            try {
                Log.v(LOGTAG, String.format(Locale.US, message, parameters));
            } catch (Exception e) {
                Log.e(LOGTAG, String.format(Locale.US, formatErrorMessage, message, Arrays.toString(parameters)));
            }
        }
    }

    @Override
    public final void debug(final String message, final Object... parameters) {
        if (logLevel.androidLogLevel <= Log.DEBUG) {
            try {
                Log.d(LOGTAG, String.format(Locale.US, message, parameters));
            } catch (Exception e) {
                Log.e(LOGTAG, String.format(Locale.US, formatErrorMessage, message, Arrays.toString(parameters)));
            }
        }
    }

    @Override
    public final void info(final String message, final Object... parameters) {
        if (logLevel.androidLogLevel <= Log.INFO) {
            try {
                Log.i(LOGTAG, String.format(Locale.US, message, parameters));
            } catch (Exception e) {
                Log.e(LOGTAG, String.format(Locale.US, formatErrorMessage, message, Arrays.toString(parameters)));
            }
        }
    }

    @Override
    public final void warn(final String message, final Object... parameters) {
        if (logLevel.androidLogLevel <= Log.WARN) {
            try {
                Log.w(LOGTAG, String.format(Locale.US, message, parameters));
            } catch (Exception e) {
                Log.e(LOGTAG, String.format(Locale.US, formatErrorMessage, message, Arrays.toString(parameters)));
            }
        }
    }

    @Override
    public void error(final String message, final Object... parameters) {
        if (logLevel.androidLogLevel <= Log.ERROR) {
            try {
                Log.e(LOGTAG, String.format(Locale.US, message, parameters));
            } catch (Exception e) {
                Log.e(LOGTAG, String.format(Locale.US, formatErrorMessage, message, Arrays.toString(parameters)));
            }
        }
    }

    @Override
    public final void Assert(final String message, final Object... parameters) {
        if (logLevel.androidLogLevel <= Log.ASSERT) {
            try {
                Log.println(Log.ASSERT, LOGTAG, String.format(Locale.US, message, parameters));
            } catch (Exception e) {
                Log.e(LOGTAG, String.format(Locale.US, formatErrorMessage, message, Arrays.toString(parameters)));
            }
        }
    }

    @Override
    public void lockLogLevel() {
        logLevelLocked = true;
    }
}
