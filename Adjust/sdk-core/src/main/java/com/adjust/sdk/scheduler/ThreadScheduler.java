package com.adjust.sdk.scheduler;

import java.util.concurrent.ScheduledFuture;

/**
 * Created by nonelse on 30.05.2018
 */
public interface ThreadScheduler extends ThreadExecutor {
    ScheduledFuture<?> schedule(Runnable command, long millisecondDelay);
    ScheduledFuture<?> scheduleWithFixedDelay(Runnable command,
                                              long initialMillisecondDelay,
                                              long millisecondDelay);
    void teardown();
}
