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
    private String name;
    private Runnable command;
    private long initialDelay;
    private long cycleDelay;
    private boolean isPaused;
    private ILogger logger;

    public TimerCycle(Runnable command, long initialDelay, long cycleDelay, String name) {
        this.scheduler = Executors.newSingleThreadScheduledExecutor();

        this.name = name;
        this.command = command;
        this.initialDelay = initialDelay;
        this.cycleDelay = cycleDelay;
        this.isPaused = true;
        this.logger = AdjustFactory.getLogger();
    }

    public void start() {
        if (!isPaused) {
            logger.verbose("%s is already started", name);
            return;
        }

        logger.verbose("%s starting in %d seconds and cycle every %d seconds", name, TimeUnit.MILLISECONDS.toSeconds(initialDelay), TimeUnit.MILLISECONDS.toSeconds(cycleDelay));

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

        logger.verbose("%s suspended with %d seconds left", name, TimeUnit.MILLISECONDS.toSeconds(initialDelay));

        isPaused = true;
    }
}
