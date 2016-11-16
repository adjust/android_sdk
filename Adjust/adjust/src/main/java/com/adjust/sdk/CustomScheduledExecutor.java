package com.adjust.sdk;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
//import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by pfms on 05/08/2016.
 */
public class CustomScheduledExecutor {
    private ScheduledThreadPoolExecutor executor;
//    private AtomicInteger threadCounter = new AtomicInteger(1);

    public CustomScheduledExecutor(final String source) {
        executor = new ScheduledThreadPoolExecutor(1,                                        // Single thread
                new ThreadFactory() {                   // Creator of daemon threads
                    @Override
                    public Thread newThread(final Runnable runnable) {
                        Thread thread = Executors.defaultThreadFactory().newThread(runnable);
                        thread.setPriority(Thread.MIN_PRIORITY);
                        thread.setName(Constants.THREAD_PREFIX + thread.getName() + source);
                        thread.setDaemon(true);
                        thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                            @Override
                            public void uncaughtException(Thread th, Throwable tr) {
                                AdjustFactory.getLogger().error("Thread %s with error %s", th.getName(), tr.getMessage());
                            }
                        });
//                        AdjustFactory.getLogger().verbose("Thread %s created", thread.getName());
                        return thread;
                    }
                }, new RejectedExecutionHandler() {     // Logs rejected runnables rejected from the entering the pool
            @Override
            public void rejectedExecution(Runnable runnable, ThreadPoolExecutor executor) {
                AdjustFactory.getLogger().warn("Runnable %s rejected from %s ", runnable.toString(), source);
            }
        }
        );
        executor.setKeepAliveTime(10L, TimeUnit.MILLISECONDS);
        executor.allowCoreThreadTimeOut(true);
    }

    public final Future<?> submit(final Runnable task) {
        return executor.submit(task);
    }

    public final void shutdownNow() {
        executor.shutdownNow();
    }

    public final ScheduledFuture<?> scheduleWithFixedDelay(final Runnable command,
                                                           final long initialDelay,
                                                           final long delay,
                                                           final TimeUnit unit) {
        return executor.scheduleWithFixedDelay(new RunnableWrapper(command), initialDelay, delay, unit);
    }

    public final ScheduledFuture<?> schedule(final Runnable command,
                                       final long delay,
                                       final TimeUnit unit) {
        return executor.schedule(new RunnableWrapper(command), delay, unit);
    }

    private static class RunnableWrapper implements Runnable {
        private Runnable runnable;
//        private long created;
//        private int threadNumber;

        RunnableWrapper(final Runnable runnable) {
            this.runnable = runnable;
//            created = System.currentTimeMillis();
//            threadNumber = threadCounter.getAndIncrement();
//            AdjustFactory.getLogger().verbose("RunnableWrapper %d from %s created at %d", threadNumber, source, created);
        }

        @Override
        public final void run() {
            try {
//                long before = System.currentTimeMillis();
//                AdjustFactory.getLogger().verbose("RunnableWrapper %d from %s source, before running at %d", threadNumber, source, before);
                runnable.run();
//                long after = System.currentTimeMillis();
//                AdjustFactory.getLogger().verbose("RunnableWrapper %d from %s source, after running at %d", threadNumber, source, after);
            } catch (Throwable t) {
                AdjustFactory.getLogger().error("Runnable error %s", t.getMessage());
            }
        }
    }
}
