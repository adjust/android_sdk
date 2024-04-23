package com.adjust.sdk;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.concurrent.CopyOnWriteArrayList;

public class SystemLifecycle implements Application.ActivityLifecycleCallbacks
{
    public interface SystemLifecycleCallback {
        void onActivityLifecycle(final boolean foregroundOrElseBackground);
    }

    public static class SystemLifecycleCache implements SystemLifecycleCallback {
        public volatile @Nullable Boolean foregroundOrElseBackgroundCache = null;

        @Override
        public void onActivityLifecycle(final boolean foregroundOrElseBackground) {
            this.foregroundOrElseBackgroundCache = foregroundOrElseBackground;
        }
    }

    // region Injected dependencies
    private @Nullable Application application;

    private volatile @NonNull SystemLifecycleCallback callback;
    // endregion

    // region Internal variables
    private static volatile SystemLifecycle instance;
    // CopyOnWrite to allow iterating and mutation in different threads
    public @NonNull final CopyOnWriteArrayList<String> logMessageList;
    private final @NonNull SystemLifecycleCache callbackCache;
    // endregion

    // region Instantiation
    public SystemLifecycle() {
        this.application = null;
        this.callbackCache = new SystemLifecycleCache();
        this.callback = callbackCache;
        this.logMessageList = new CopyOnWriteArrayList<>();
    }

    @NonNull
    public static SystemLifecycle getSingletonInstance() {
        @Nullable SystemLifecycle localInstance = instance;
        if (localInstance == null) {
            synchronized (SystemLifecycle.class) {
                localInstance = instance;
                if (localInstance == null) {
                    localInstance = new SystemLifecycle();
                    instance = localInstance;
                }
            }
        }
        return localInstance;
    }
    // endregion

    // region Application.ActivityLifecycleCallbacks
    @Override public void onActivityResumed(@NonNull final Activity activity) {
        callback.onActivityLifecycle(true);
    }

    @Override public void onActivityPaused(@NonNull final Activity activity) {
        callback.onActivityLifecycle(false);
    }
    // region Unused
    @Override
    public void onActivityCreated(@NonNull final Activity activity,
                                  @Nullable final Bundle savedInstanceState)
    { }

    @Override public void onActivityStarted(@NonNull final Activity activity) { }

    @Override public void onActivityStopped(@NonNull final Activity activity) { }

    @Override
    public void onActivitySaveInstanceState(@NonNull final Activity activity,
                                            @NonNull final Bundle outState)
    { }

    @Override public void onActivityDestroyed(@NonNull final Activity activity) { }
    // endregion
    // endregion

    // region Public API
    // synchronized to prevent double registering
    public synchronized void registerActivityLifecycleCallbacks(
      @Nullable final Context context)
    {
        if (application != null) {
            logMessageList.add("Cannot register activity lifecycle callbacks more than once");
            return;
        }

        if (context == null) {
            logMessageList.add("Cannot register activity lifecycle callbacks without context");
            return;
        }

        @Nullable final Context applicationContext = context.getApplicationContext();
        if (! (applicationContext instanceof Application)) {
            logMessageList.add(
              "Cannot register activity lifecycle callbacks without application context as Application");
            return;
        }

        logMessageList.add("Registering activity lifecycle callbacks");

        application = (Application) applicationContext;
        application.registerActivityLifecycleCallbacks(this);
    }

    public void overwriteCallback(
      @NonNull final SystemLifecycleCallback systemLifecycleCallback)
    {
        this.callback = systemLifecycleCallback;
    }

    @Nullable
    public Boolean foregroundOrElseBackgroundCached() {
        return callbackCache.foregroundOrElseBackgroundCache;
    }
    // endregion
}
