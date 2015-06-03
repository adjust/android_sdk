package com.adjust.sdk;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by pfms on 08/05/15.
 */
public class TimerOnce {
    private ScheduledExecutorService scheduler;
    private ScheduledFuture waitingTask;
    private Runnable command;
    private boolean isRunning;

    public TimerOnce(ScheduledExecutorService scheduler, Runnable command) {
        this.scheduler = scheduler;
        this.command = command;
        this.isRunning = false;
    }

    public void startIn(long fireIn) {
        // cancel previous
        if (waitingTask != null) {
            waitingTask.cancel(false);
        }
        waitingTask = scheduler.schedule(command, fireIn, TimeUnit.MILLISECONDS);
    }

    public long getFireIn() {
        if (waitingTask == null) {
            return 0;
        }
        return waitingTask.getDelay(TimeUnit.MILLISECONDS);
    }
}
