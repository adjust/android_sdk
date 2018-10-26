package com.adjust.sdk.scheduler;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * Created by nonelse on 12.09.17.
 */

public interface ThreadExecutor {
    Future<?> submit(Runnable task);
    <T> Future<T> submit(Callable<T> task);
    void teardown();
}
