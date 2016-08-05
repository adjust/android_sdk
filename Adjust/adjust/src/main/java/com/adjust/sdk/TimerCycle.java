package com.adjust.sdk;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by pfms on 08/05/15.
 */
public class TimerCycle {
    private CustomScheduledExecutor scheduler;
    private ScheduledFuture waitingTask;
    private String name;
    private Runnable command;
    private long initialDelay;
    private long cycleDelay;
    private String cycleDelaySeconds;
    private boolean isPaused;
    private ILogger logger;

    public TimerCycle(CustomScheduledExecutor scheduler, Runnable command, long initialDelay, long cycleDelay, String name) {
        this.scheduler = scheduler;

        this.name = name;
        this.command = command;
        this.initialDelay = initialDelay;
        this.cycleDelay = cycleDelay;
        this.isPaused = true;
        this.logger = AdjustFactory.getLogger();

        this.cycleDelaySeconds = Util.SecondsDisplayFormat.format(cycleDelay / 1000.0);
    }

    public void start() {
        if (!isPaused) {
            logger.verbose("%s is already started", name);
            return;
        }

        String initialDelaySeconds = Util.SecondsDisplayFormat.format(initialDelay / 1000.0);

        logger.verbose("%s starting in %s seconds and cycle every %s seconds", name, initialDelaySeconds, cycleDelaySeconds);

        waitingTask = scheduler.scheduleWithFixedDelay(new Runnable() {
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
        waitingTask = null;

        String initialDelaySeconds = Util.SecondsDisplayFormat.format(initialDelay / 1000.0);

        logger.verbose("%s suspended with %s seconds left", name, initialDelaySeconds);

        isPaused = true;
    }

    public void cancel(boolean mayInterruptIfRunning) {
        if (waitingTask != null) {
            waitingTask.cancel(mayInterruptIfRunning);
        }
    }
}
