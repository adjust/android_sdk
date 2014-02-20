package com.adjust.sdk;

public enum ActivityKind {
    UNKNOWN, SESSION, EVENT, REVENUE;

    public static ActivityKind  fromString(String string) {
        if ("session".equals(string)) {
            return SESSION;
        } else if ("event".equals(string)) {
            return EVENT;
        } else if ("revenue".equals(string)) {
            return REVENUE;
        } else {
            return UNKNOWN;
        }
    }

    public String toString() {
        switch(this) {
        case SESSION: return "session";
        case EVENT:   return "event";
        case REVENUE: return "revenue";
        default:      return "unknown";
        }
    }
}
