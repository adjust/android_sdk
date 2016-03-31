package com.adjust.sdk;

/**
 * Created by pfms on 06/04/16.
 */
public enum BackoffStrategy {
    LONG_WAIT(1,                        // min retries
            2 * Constants.ONE_MINUTE,   // milliseconds multiplier
            24 * Constants.ONE_HOUR,    // max wait time
            50,                         // min jitter multiplier
            100),                       // max jitter multiplier

    // 0.1-0.2, 0.2-0.4, 0.4-0.8, ... 1m
    SHORT_WAIT(1,                   // min retries
            200,                    // milliseconds multiplier
            Constants.ONE_MINUTE,   // max wait time
            50,                     // min jitter multiplier
            100),                   // max jitter multiplier

    TEST_WAIT(1,                    // min retries
            200,                    // milliseconds multiplier
            1000,                   // max wait time
            50,                     // min jitter multiplier
            100),                   // max jitter multiplier

    NO_WAIT(100,                    // min retries
            1,                      // milliseconds multiplier
            Constants.ONE_SECOND,   // max wait time
            1,                      // min jitter multiplier
            1);                     // max jitter multiplier

    int minRetries;
    long milliSecondMultiplier;
    long maxWait;
    int minJitter;
    int maxJitter;

    BackoffStrategy(int minRetries,
                    long milliSecondMultiplier,
                    long maxWait,
                    int minJitter,
                    int maxJitter) {
        this.minRetries = minRetries;
        this.milliSecondMultiplier = milliSecondMultiplier;
        this.maxWait = maxWait;
        this.minJitter = minJitter;
        this.maxJitter = maxJitter;
    }
}
