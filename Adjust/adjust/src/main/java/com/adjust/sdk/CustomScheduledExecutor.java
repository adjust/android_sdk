package com.adjust.sdk;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.*;

/**
 * Created by pfms on 05/08/2016.
 */
public final class CustomScheduledExecutor {
    private String source;
    private ScheduledThreadPoolExecutor executor;
    private final AtomicInteger threadCounter = new AtomicInteger(1);


    public CustomScheduledExecutor(final String source, boolean doKeepAlive) {
        this.source = source;

        executor = new ScheduledThreadPoolExecutor(1,   // Single thread
                new ThreadFactory() {                   // Creator of daemon threads
                    @Override
                    public Thread newThread(Runnable runnable) {
                        Thread thread = Executors.defaultThreadFactory().newThread(new RunnableWrapper(runnable));

                        thread.setPriority(Thread.MIN_PRIORITY);
                        thread.setName(Constants.THREAD_PREFIX + thread.getName() + "-" + source);
                        thread.setDaemon(true);

                        thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                            @Override
                            public void uncaughtException(Thread th, Throwable tr) {
                                AdjustFactory.getLogger().error("Thread %s with error %s", th.getName(), tr.getMessage());
                            }
                        });

                        //AdjustFactory.getLogger().verbose("Thread %s created", thread.getName());
                        return thread;
                    }
                }, new RejectedExecutionHandler() {     // Logs rejected runnables rejected from the entering the pool
            @Override
            public void rejectedExecution(Runnable runnable, ThreadPoolExecutor executor) {
                AdjustFactory.getLogger().warn("Runnable %s rejected from %s ", runnable.toString(), source);
            }
        }
        );

        if (!doKeepAlive) {
            executor.setKeepAliveTime(10L, TimeUnit.MILLISECONDS);
            executor.allowCoreThreadTimeOut(true);
        }
    }

    public Future<?> submit(Runnable task) {
        return executor.submit(task);
    }

    public void shutdownNow() {
        executor.shutdownNow();
    }

    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
//        AdjustFactory.getLogger().verbose("CustomScheduledExecutor scheduleWithFixedDelay from %s source, with %d delay and %d initial delay",
//                source, delay, initialDelay);
        return executor.scheduleWithFixedDelay(command, initialDelay, delay, unit);
    }

    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
//        AdjustFactory.getLogger().verbose("CustomScheduledExecutor schedule from %s source, with %d delay", source, delay);
        return executor.schedule(command, delay, unit);
    }

    private class RunnableWrapper implements Runnable {
        private Runnable runnable;
//        private long created;
//        private int threadNumber;

        public RunnableWrapper(Runnable runnable) {
            this.runnable = runnable;
//            created = System.currentTimeMillis();
//            threadNumber = threadCounter.getAndIncrement();
//            AdjustFactory.getLogger().verbose("RunnableWrapper %d from %s created at %d", threadNumber, source, created);
        }

        @Override
        public void run() {
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
