package com.adjust.sdk;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by pfms on 08/05/15.
 */
public class TimerCycle {
    private ScheduledExecutorService scheduler;
    private ScheduledFuture waitingTask;
    private Runnable command;
    private long initialDelay;
    private long cycleDelay;
    private boolean isPaused;

    public TimerCycle(Runnable command, long initialDelay, long cycleDelay) {
        this.scheduler = Executors.newSingleThreadScheduledExecutor();

        this.command = command;
        this.initialDelay = initialDelay;
        this.cycleDelay = cycleDelay;
        this.isPaused = true;
    }

    public void start() {
        if (!isPaused) { return; }

        waitingTask = scheduler.scheduleWithFixedDelay(command, initialDelay, cycleDelay, TimeUnit.MILLISECONDS);

        isPaused = false;
    }

    public void suspend() {
        if (isPaused) { return; }

        // get the remaining delay
        initialDelay = waitingTask.getDelay(TimeUnit.MILLISECONDS);

        // cancel the timer
        waitingTask.cancel(false);

        isPaused = true;
    }
}
