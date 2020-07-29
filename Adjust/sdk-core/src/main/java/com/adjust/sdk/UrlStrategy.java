package com.adjust.sdk;

public enum UrlStrategy {
    DEFAULT_URL,
    FALLBACK_URL,
    FALLBACK_IP;

    public static UrlStrategy get(int attemptCount) {
        switch(attemptCount) {
            case 2: return FALLBACK_URL;
            case 3: return FALLBACK_IP;
            default: return DEFAULT_URL;
        }
    }
}
