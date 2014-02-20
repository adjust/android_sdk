package com.adjust.sdk;

import android.util.Log;

public interface Logger {

    public enum LogLevel {
        VERBOSE(Log.VERBOSE), DEBUG(Log.DEBUG), INFO(Log.INFO), WARN(Log.WARN), ERROR(Log.ERROR), ASSERT(Log.ASSERT);
        final int androidLogLevel;

        LogLevel(final int androidLogLevel) {
            this.androidLogLevel = androidLogLevel;
        }

        public int getAndroidLogLevel() {
            return androidLogLevel;
        }
    }

    public void setLogLevel(LogLevel logLevel);

    public void setLogLevelString(String logLevelString);

    public void verbose(String message);

    public void debug(String message);

    public void info(String message);

    public void warn(String message);

    public void error(String message);

    public void Assert(String message);

}
