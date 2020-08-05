package com.adjust.sdk;

public enum UrlStrategy {
    DEFAULT_URL,
    FALLBACK_URL,
    FALLBACK_IP;

    private static UrlStrategy firstAttemptStrategy = DEFAULT_URL;
    private static UrlStrategy secondAttemptStrategy = FALLBACK_URL;
    private static UrlStrategy thirdAttemptStrategy = FALLBACK_IP;

    public static UrlStrategy getStrategy(int attemptCount) {
        switch(attemptCount) {
            case 2: return secondAttemptStrategy;
            case 3: return thirdAttemptStrategy;
            default: return firstAttemptStrategy;
        }
    }

    public static void updateWorkingStrategy(UrlStrategy workingUrlStrategy) {
        switch (workingUrlStrategy) {
            case DEFAULT_URL :
                firstAttemptStrategy = DEFAULT_URL;
                secondAttemptStrategy = FALLBACK_URL;
                thirdAttemptStrategy = FALLBACK_IP;
                break;

            case FALLBACK_URL :
                firstAttemptStrategy = FALLBACK_URL;
                secondAttemptStrategy = FALLBACK_IP;
                thirdAttemptStrategy = DEFAULT_URL;
                break;

            case FALLBACK_IP :
                firstAttemptStrategy = FALLBACK_IP;
                secondAttemptStrategy = DEFAULT_URL;
                thirdAttemptStrategy = FALLBACK_URL;
                break;
        }
    }
}
