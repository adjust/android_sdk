package com.adjust.sdk;

import java.lang.ref.WeakReference;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by pfms on 08/05/15.
 */
public class TimerOnce {
    private WeakReference<CustomScheduledExecutor> scheduledExecutorWeakRef;
    private ScheduledFuture waitingTask;
    private String name;
    private Runnable command;
    private ILogger logger;

    public TimerOnce(CustomScheduledExecutor scheduler, Runnable command, String name) {
        this.name = name;
        this.scheduledExecutorWeakRef = new WeakReference<CustomScheduledExecutor>(scheduler);
        this.command = command;
        this.logger = AdjustFactory.getLogger();
    }

    public void startIn(long fireIn) {
        // cancel previous
        cancel(false);

        CustomScheduledExecutor scheduledExecutor = scheduledExecutorWeakRef.get();
        if (scheduledExecutor == null) {
            return;
        }

        String fireInSeconds = Util.SecondsDisplayFormat.format(fireIn / 1000.0);

        logger.verbose("%s starting. Launching in %s seconds", name, fireInSeconds);

        waitingTask = scheduledExecutor.schedule(new Runnable() {
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
        if (scheduledExecutorWeakRef != null) {
            scheduledExecutorWeakRef.clear();
        }
        scheduledExecutorWeakRef = null;
    }
}
