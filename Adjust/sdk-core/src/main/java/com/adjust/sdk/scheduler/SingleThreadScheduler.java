package com.adjust.sdk.scheduler;

import android.os.Process;

import java.util.ArrayList;
import java.util.List;

public class SingleThreadScheduler implements ThreadScheduler {
    private final List<Runnable> queue;
    private boolean isThreadProcessing;
    private boolean isTeardown;

    public SingleThreadScheduler() {
        this.queue = new ArrayList<>();
        isThreadProcessing = false;
        isTeardown = false;
    }

    @Override
    public void submit(Runnable task) {
        synchronized (queue) {
            if (isTeardown) {
                return;
            }
            if (!isThreadProcessing) {
                isThreadProcessing = true;
                processQueue(task);
            }
            else {
                queue.add(task);
            }
        }
    }

    @Override
    public void schedule(final Runnable task, final long millisecondsDelay) {
        if (isTeardown) {
            return;
        }

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_LOWEST);

                try {
                    Thread.sleep(millisecondsDelay);
                } catch (InterruptedException e) {
                }

                submit(task);
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private void processQueue(final Runnable firstRunnable) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND + Process.THREAD_PRIORITY_MORE_FAVORABLE);

                // execute the first task
                tryExecuteRunnable(firstRunnable);

                Runnable runnable = null;

                // Process all available items in the queue.
                while (true) {
                    // possible teardown happened meanwhile

                    synchronized (queue) {
                        if (isTeardown) {
                            return;
                        }
                        if (queue.isEmpty()) {
                            runnable = new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        Thread.sleep(1000);
                                    } catch (InterruptedException e) {
                                    }
                                }
                            };
                        }
                    }
                    if (runnable != null) {
                        tryExecuteRunnable(runnable);
                    }

                    synchronized (queue) {
                        if (isTeardown) {
                            return;
                        }

                        if (queue.isEmpty()) {
                            isThreadProcessing = false;
                            break;
                        }
                        runnable = queue.get(0);
                        queue.remove(0);
                    }
                    tryExecuteRunnable(runnable);
                    runnable = null;
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private void tryExecuteRunnable(Runnable runnable) {
        try {
            if (isTeardown) {
                return;
            }

            runnable.run();
        } catch (Throwable t) {
        }
    }

    @Override
    public void teardown() {
        synchronized (queue) {
            isTeardown = true;
            queue.clear();
        }
    }
}
