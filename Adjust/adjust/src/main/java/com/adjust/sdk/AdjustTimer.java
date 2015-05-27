package com.adjust.sdk;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by pfms on 08/05/15.
 */
public class AdjustTimer {
    private ScheduledExecutorService scheduler;
    private ScheduledFuture waitingTask;
    private Runnable command;
    private long initialDelay;
    private Long cycleDelay;
    private long fireIn;

    private AdjustTimer(ScheduledExecutorService scheduler, Runnable command, long initialDelay, Long cycleDelay) {
        if (scheduler == null) {
            this.scheduler = Executors.newSingleThreadScheduledExecutor();
        } else {
            this.scheduler = scheduler;
        }
        this.command = command;
        this.initialDelay = initialDelay;
        this.cycleDelay = cycleDelay;
        setFireIn(0);
    }

    // one fire timer
    public AdjustTimer(ScheduledExecutorService scheduler, Runnable command) {
        this(scheduler, command, 0, null);
    }

    // cycle timer
    public AdjustTimer(Runnable command, long initialDelay, Long cycleDelay) {
        this(null, command, initialDelay, cycleDelay);
    }

    public void resume() {
        if (waitingTask != null) {
            return;
        }
        long start;
        if (fireIn > 0) {
            start = fireIn;
        } else {
            start = initialDelay;
        }

        if (cycleDelay == null) {
            waitingTask = scheduler.schedule(command, start, TimeUnit.MILLISECONDS);
        } else {
            waitingTask = scheduler.scheduleWithFixedDelay(command, start, cycleDelay, TimeUnit.MILLISECONDS);
        }
    }

    public void suspend() {
        // get the time until the event is fired
        long savedFireIn = getFireIn();

        cancel();

        // save the time left until the timer is resumed
        setFireIn(savedFireIn);
    }

    public void cancel() {
        if (waitingTask == null) {
            return;
        }
        // cancel the timer
        waitingTask.cancel(false);
        waitingTask = null;
        // reset the time of the next fire
        setFireIn(0);
    }

    public long getFireIn() {
        if (waitingTask == null) {
            return 0;
        }
        return waitingTask.getDelay(TimeUnit.MILLISECONDS);
    }

    public void setFireIn(long fireIn) {
        if (fireIn < 0) {
            this.fireIn = 0;
            return;
        }
        this.fireIn = fireIn;
    }
}
