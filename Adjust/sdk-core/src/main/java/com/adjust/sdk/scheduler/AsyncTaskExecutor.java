package com.adjust.sdk.scheduler;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class AsyncTaskExecutor<Params, Result> {

    protected abstract Result doInBackground(Params[] params);

    protected void onPreExecute() { }

    protected void onPostExecute(Result result) { }

    @SafeVarargs
    public final AsyncTaskExecutor<Params, Result> execute(final Params ... params) {
        onPreExecute();

        final Handler handler = new Handler(Looper.getMainLooper());
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                final Result result = doInBackground(params);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        onPostExecute(result);
                    }
                });
            }
        });

        return this;
    }
}
