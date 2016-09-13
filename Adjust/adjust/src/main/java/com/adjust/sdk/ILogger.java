package com.adjust.sdk;

public interface ILogger {
    void setLogLevel(LogLevel logLevel);

    void setLogLevelString(String logLevelString);

    void verbose(String message, Object... parameters);

    void debug(String message, Object... parameters);

    void info(String message, Object... parameters);

    void warn(String message, Object... parameters);

    void error(String message, Object... parameters);

    void Assert(String message, Object... parameters);

    void lockLogLevel();
}
