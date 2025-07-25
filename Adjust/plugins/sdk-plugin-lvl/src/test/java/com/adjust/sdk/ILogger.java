package com.adjust.sdk;

public interface ILogger {
    void debug(String message, Object... params);
    void info(String message, Object... params);
    void warn(String message, Object... params);
    void error(String message, Object... params);
}

