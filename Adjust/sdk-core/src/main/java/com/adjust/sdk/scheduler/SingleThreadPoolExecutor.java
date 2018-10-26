package com.adjust.sdk.scheduler;

import com.adjust.sdk.AdjustFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SingleThreadPoolExecutor implements ThreadExecutor  {
    ThreadPoolExecutor threadPoolExecutor;

    public SingleThreadPoolExecutor(final String source) {
        this.threadPoolExecutor = new ThreadPoolExecutor(
            1,
            1,
            10L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(),
            new ThreadFactoryWrapper(source),
            new RejectedExecutionHandler() {     // Logs rejected runnables rejected from the entering the pool
                @Override
                public void rejectedExecution(Runnable runnable, ThreadPoolExecutor executor) {
                    AdjustFactory.getLogger().warn("Runnable [%s] rejected from [%s] ",
                            runnable.toString(), source);
                }
            }
        );

        threadPoolExecutor.allowCoreThreadTimeOut(true);
    }
    @Override
    public void submit(Runnable task) {
        threadPoolExecutor.submit(new RunnableWrapper(task));
    }

    @Override
    public void teardown() {
        threadPoolExecutor.shutdownNow();
    }
}
