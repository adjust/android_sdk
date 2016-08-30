package com.adjust.sdk;

import java.lang.ref.WeakReference;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by pfms on 08/05/15.
 */
public class TimerCycle {
    private WeakReference<CustomScheduledExecutor> scheduledExecutorWeakRef;
    private ScheduledFuture waitingTask;
    private String name;
    private Runnable command;
    private long initialDelay;
    private long cycleDelay;
    private boolean isPaused;
    private ILogger logger;

    public TimerCycle(CustomScheduledExecutor scheduler, Runnable command, long initialDelay, long cycleDelay, String name) {
        this.scheduledExecutorWeakRef = new WeakReference<CustomScheduledExecutor>(scheduler);

        this.name = name;
        this.command = command;
        this.initialDelay = initialDelay;
        this.cycleDelay = cycleDelay;
        this.isPaused = true;
        this.logger = AdjustFactory.getLogger();

        String cycleDelaySecondsString = Util.SecondsDisplayFormat.format(cycleDelay / 1000.0);

        String initialDelaySecondsString = Util.SecondsDisplayFormat.format(initialDelay / 1000.0);

        logger.verbose("%s configured to fire after %s seconds of starting and cycles every %s seconds", name, initialDelaySecondsString, cycleDelaySecondsString);
    }

    public void start() {
        if (!isPaused) {
            logger.verbose("%s is already started", name);
            return;
        }

        CustomScheduledExecutor scheduledExecutor = scheduledExecutorWeakRef.get();
        if (scheduledExecutor == null) {
            return;
        }

        //String initialDelaySeconds = Util.SecondsDisplayFormat.format(initialDelay / 1000.0);
        //logger.verbose("%s starting in %s seconds and cycle every %s seconds", name, initialDelaySeconds, cycleDelaySeconds);

        logger.verbose("%s starting", name);

        waitingTask = scheduledExecutor.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                logger.verbose("%s fired", name);
                command.run();
            }
        }, initialDelay, cycleDelay, TimeUnit.MILLISECONDS);

        isPaused = false;
    }

    public void suspend() {
        if (isPaused) {
            logger.verbose("%s is already suspended", name);
            return;
        }

        // get the remaining delay
        initialDelay = waitingTask.getDelay(TimeUnit.MILLISECONDS);

        // cancel the timer
        waitingTask.cancel(false);

        String initialDelaySeconds = Util.SecondsDisplayFormat.format(initialDelay / 1000.0);

        logger.verbose("%s suspended with %s seconds left", name, initialDelaySeconds);

        isPaused = true;
    }

    private void cancel(boolean mayInterruptIfRunning) {
        if (waitingTask != null) {
            waitingTask.cancel(mayInterruptIfRunning);
        }

        waitingTask = null;
    }

    public void teardown() {
        cancel(true);
        if (scheduledExecutorWeakRef != null) {
            scheduledExecutorWeakRef.clear();
        }
        scheduledExecutorWeakRef = null;
    }
}
