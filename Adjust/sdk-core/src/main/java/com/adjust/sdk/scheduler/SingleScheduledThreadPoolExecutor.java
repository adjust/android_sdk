package com.adjust.sdk.scheduler;

import com.adjust.sdk.AdjustFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SingleScheduledThreadPoolExecutor implements ThreadScheduler {
    private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;

    public SingleScheduledThreadPoolExecutor(final String source, boolean doKeepAlive) {
        this.scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(
                1,
                new ThreadFactoryWrapper(source),
                new RejectedExecutionHandler() {     // Logs rejected runnables rejected from the entering the pool
                    @Override
                    public void rejectedExecution(Runnable runnable, ThreadPoolExecutor executor) {
                        AdjustFactory.getLogger().warn("Runnable [%s] rejected from [%s] ",
                                runnable.toString(), source);
                    }
                }
        );

        if (!doKeepAlive) {
            scheduledThreadPoolExecutor.setKeepAliveTime(10L, TimeUnit.MILLISECONDS);
            scheduledThreadPoolExecutor.allowCoreThreadTimeOut(true);
        }
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable command, long millisecondDelay) {
        return scheduledThreadPoolExecutor.schedule(new RunnableWrapper(command), millisecondDelay, TimeUnit.MILLISECONDS);
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialMillisecondDelay, long millisecondDelay) {
        return scheduledThreadPoolExecutor.scheduleWithFixedDelay(new RunnableWrapper(command), initialMillisecondDelay, millisecondDelay, TimeUnit.MILLISECONDS);
    }

    @Override
    public Future<?> submit(Runnable task) {
        return scheduledThreadPoolExecutor.submit(new RunnableWrapper(task));
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return scheduledThreadPoolExecutor.submit(new CallableWrapper<T>(task));
    }

    @Override
    public void teardown() {
        scheduledThreadPoolExecutor.shutdownNow();
    }
}
