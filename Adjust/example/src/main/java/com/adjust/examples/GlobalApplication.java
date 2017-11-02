package com.adjust.examples;

import android.app.Activity;
import android.app.Application;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;

import com.adjust.sdk.Adjust;
import com.adjust.sdk.AdjustAttribution;
import com.adjust.sdk.AdjustConfig;
import com.adjust.sdk.AdjustEventFailure;
import com.adjust.sdk.AdjustEventSuccess;
import com.adjust.sdk.LogLevel;
import com.adjust.sdk.OnAttributionChangedListener;
import com.adjust.sdk.OnDeeplinkResponseListener;
import com.adjust.sdk.OnEventTrackingFailedListener;
import com.adjust.sdk.OnEventTrackingSucceededListener;
import com.adjust.sdk.OnSessionTrackingFailedListener;
import com.adjust.sdk.OnSessionTrackingSucceededListener;
import com.adjust.sdk.AdjustSessionFailure;
import com.adjust.sdk.AdjustSessionSuccess;
import com.squareup.leakcanary.LeakCanary;

/**
 * Created by pfms on 17/12/14.
 */
public class GlobalApplication extends Application {
    @Override
    public void onCreate() {
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        super.onCreate();

        LeakCanary.install(this);

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build());

        // Configure adjust SDK.
        String appToken = "2fm9gkqubvpc";
        String environment = AdjustConfig.ENVIRONMENT_SANDBOX;

        AdjustConfig config = new AdjustConfig(this, appToken, environment);

        // Change the log level.
        config.setLogLevel(LogLevel.VERBOSE);

        // Set attribution delegate.
        config.setOnAttributionChangedListener(new OnAttributionChangedListener() {
            @Override
            public void onAttributionChanged(AdjustAttribution attribution) {
                Log.d("example", "Attribution callback called!");
                Log.d("example", "Attribution: " + attribution.toString());
            }
        });

        // Set event success tracking delegate.
        config.setOnEventTrackingSucceededListener(new OnEventTrackingSucceededListener() {
            @Override
            public void onFinishedEventTrackingSucceeded(AdjustEventSuccess eventSuccessResponseData) {
                Log.d("example", "Event success callback called!");
                Log.d("example", "Event success data: " + eventSuccessResponseData.toString());
            }
        });

        // Set event failure tracking delegate.
        config.setOnEventTrackingFailedListener(new OnEventTrackingFailedListener() {
            @Override
            public void onFinishedEventTrackingFailed(AdjustEventFailure eventFailureResponseData) {
                Log.d("example", "Event failure callback called!");
                Log.d("example", "Event failure data: " + eventFailureResponseData.toString());
            }
        });

        // Set session success tracking delegate.
        config.setOnSessionTrackingSucceededListener(new OnSessionTrackingSucceededListener() {
            @Override
            public void onFinishedSessionTrackingSucceeded(AdjustSessionSuccess sessionSuccessResponseData) {
                Log.d("example", "Session success callback called!");
                Log.d("example", "Session success data: " + sessionSuccessResponseData.toString());
            }
        });

        // Set session failure tracking delegate.
        config.setOnSessionTrackingFailedListener(new OnSessionTrackingFailedListener() {
            @Override
            public void onFinishedSessionTrackingFailed(AdjustSessionFailure sessionFailureResponseData) {
                Log.d("example", "Session failure callback called!");
                Log.d("example", "Session failure data: " + sessionFailureResponseData.toString());
            }
        });

        // Evaluate deferred deep link to be launched.
        config.setOnDeeplinkResponseListener(new OnDeeplinkResponseListener() {
            @Override
            public boolean launchReceivedDeeplink(Uri deeplink) {
                Log.d("example", "Deferred deep link callback called!");
                Log.d("example", "Deep link URL: " + deeplink);

                return true;
            }
        });

        // Set default tracker.
        // config.setDefaultTracker("{YourDefaultTracker}");

        // Set process name.
        // config.setProcessName("com.adjust.examples");

        // Allow to send in the background.
        config.setSendInBackground(true);

        // Enable event buffering.
        // config.setEventBufferingEnabled(true);

        // Delay first session.
        // config.setDelayStart(7);

        // Add session callback parameters.
        Adjust.addSessionCallbackParameter("sc_foo", "sc_bar");
        Adjust.addSessionCallbackParameter("sc_key", "sc_value");

        // Add session partner parameters.
        Adjust.addSessionPartnerParameter("sp_foo", "sp_bar");
        Adjust.addSessionPartnerParameter("sp_key", "sp_value");

        // Remove session callback parameters.
        Adjust.removeSessionCallbackParameter("sc_foo");

        // Remove session partner parameters.
        Adjust.removeSessionPartnerParameter("sp_key");

        // Remove all session callback parameters.
        Adjust.resetSessionCallbackParameters();

        // Remove all session partner parameters.
        Adjust.resetSessionPartnerParameters();

        // Initialise the adjust SDK.
        Adjust.onCreate(config);

        // Abort delay for the first session introduced with setDelayStart method.
        // Adjust.sendFirstPackages();

        // Register onResume and onPause events of all activities
        // for applications with minSdkVersion >= 14.
        registerActivityLifecycleCallbacks(new AdjustLifecycleCallbacks());

        // Put the SDK in offline mode.
        // Adjust.setOfflineMode(true);

        // Disable the SDK
        // Adjust.setEnabled(false);

        // Send push notification token.
        // Adjust.setPushToken("token");
    }

    // You can use this class if your app is for Android 4.0 or higher
    private static final class AdjustLifecycleCallbacks implements ActivityLifecycleCallbacks {
        @Override
        public void onActivityResumed(Activity activity) {
            Adjust.onResume();
        }

        @Override
        public void onActivityPaused(Activity activity) {
            Adjust.onPause();
        }

        @Override
        public void onActivityStopped(Activity activity) {}

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}

        @Override
        public void onActivityDestroyed(Activity activity) {}

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {}

        @Override
        public void onActivityStarted(Activity activity) {}
    }
}
