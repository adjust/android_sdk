package com.adjust.sdk;

/**
 * Created by pfms on 06/04/16.
 */
public enum BackoffStrategy {
    SHORT_WAIT(1,                       // min retries
            2 * Constants.ONE_MINUTE,   // milliseconds multiplier
            24 * Constants.ONE_HOUR,    // max wait time
            50,                         // min jitter multiplier
            100),                       // max jitter multiplier

    LONG_WAIT(1,                    // min retries
            100,                    // milliseconds multiplier
            Constants.ONE_MINUTE,   // max wait time
            50,                     // min jitter multiplier
            100);                   // max jitter multiplier

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
