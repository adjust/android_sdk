package com.adjust.sdk.scheduler;

import com.adjust.sdk.AdjustFactory;

import java.util.concurrent.Callable;

public class CallableWrapper<V> implements Callable<V> {
    private Callable<V> callable;

    public CallableWrapper(Callable<V> callable) {
        this.callable = callable;
    }

    @Override
    public V call() throws Exception {
        try {
            return callable.call();
        } catch (Exception e) {
            AdjustFactory.getLogger().error("Callabke error [%s] of type [%s]",
                    e.getMessage(), e.getClass().getCanonicalName());
            throw  e;
        } catch (Throwable t) {
            AdjustFactory.getLogger().error("Callabke error [%s] of type [%s]",
                    t.getMessage(), t.getClass().getCanonicalName());
            return null;
        }
    }
}
