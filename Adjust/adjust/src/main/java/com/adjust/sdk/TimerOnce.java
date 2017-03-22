package com.adjust.sdk;

import java.lang.ref.WeakReference;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by pfms on 08/05/15.
 */
public class TimerOnce {
    private CustomScheduledExecutor executor;

    private ScheduledFuture waitingTask;
    private String name;
    private Runnable command;
    private ILogger logger;

    public TimerOnce(Runnable command, String name) {
        this.name = name;
        this.executor = new CustomScheduledExecutor(name, true);
        this.command = command;
        this.logger = AdjustFactory.getLogger();
    }

    public void startIn(long fireIn) {
        // cancel previous
        cancel(false);

        String fireInSeconds = Util.SecondsDisplayFormat.format(fireIn / 1000.0);

        logger.verbose("%s starting. Launching in %s seconds", name, fireInSeconds);

        waitingTask = executor.schedule(new Runnable() {
            @Override
            public void run() {
                logger.verbose("%s fired", name);
                command.run();
                waitingTask = null;
            }
        }, fireIn, TimeUnit.MILLISECONDS);
    }

    public long getFireIn() {
        if (waitingTask == null) {
            return 0;
        }
        return waitingTask.getDelay(TimeUnit.MILLISECONDS);
    }

    private void cancel(boolean mayInterruptIfRunning) {
        if (waitingTask != null) {
            waitingTask.cancel(mayInterruptIfRunning);
        }
        waitingTask = null;

        logger.verbose("%s canceled", name);
    }

    public void cancel() {
        cancel(false);
    }

    public void teardown() {
        cancel(true);

        executor = null;
    }
}
