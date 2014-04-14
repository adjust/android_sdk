package com.adjust.sdk;

public enum ActivityKind {
    UNKNOWN, SESSION, EVENT, REVENUE, REATTRIBUTION;

    public static ActivityKind  fromString(String string) {
        if ("session".equals(string)) {
            return SESSION;
        } else if ("event".equals(string)) {
            return EVENT;
        } else if ("revenue".equals(string)) {
            return REVENUE;
        } else if ("reattribution".equals(string)) {
            return REATTRIBUTION;
        } else {
            return UNKNOWN;
        }
    }

    @Override
    public String toString() {
        switch(this) {
        case SESSION:       return "session";
        case EVENT:         return "event";
        case REVENUE:       return "revenue";
        case REATTRIBUTION: return "reattribution";
        default:            return "unknown";
        }
    }
}
