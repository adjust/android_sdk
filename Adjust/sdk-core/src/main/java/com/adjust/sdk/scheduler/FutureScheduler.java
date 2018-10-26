package com.adjust.sdk.scheduler;

import java.util.concurrent.ScheduledFuture;

/**
 * Created by nonelse on 30.05.2018
 */
public interface FutureScheduler {
    ScheduledFuture<?> scheduleFuture(Runnable command, long millisecondDelay);
    ScheduledFuture<?> scheduleFutureWithFixedDelay(Runnable command,
                                                    long initialMillisecondDelay,
                                                    long millisecondDelay);
    void teardown();
}
