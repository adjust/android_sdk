package com.adjust.example;

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
        LeakCanary.install(this);

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .penaltyDialog()
                .build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build());

        super.onCreate();
        // configure Adjust
        String appToken = "qwerty123456";

        String environment = AdjustConfig.ENVIRONMENT_SANDBOX;
        AdjustConfig config = new AdjustConfig(this, appToken, environment);

        // change the log level
        config.setLogLevel(LogLevel.VERBOSE);

        // set default tracker
        //config.setDefaultTracker("{YourDefaultTracker}");

        // set process name
        //config.setProcessName("com.adjust.example");

        // set attribution delegate
        config.setOnAttributionChangedListener(new OnAttributionChangedListener() {
            @Override
            public void onAttributionChanged(AdjustAttribution attribution) {
                Log.d("example", "attribution: " + attribution.toString());
            }
        });

        // set event success tracking delegate
        config.setOnEventTrackingSucceededListener(new OnEventTrackingSucceededListener() {
            @Override
            public void onFinishedEventTrackingSucceeded(AdjustEventSuccess eventSuccessResponseData) {
                Log.d("example", "success event tracking: " + eventSuccessResponseData.toString());
            }
        });

        // set event failure tracking delegate
        config.setOnEventTrackingFailedListener(new OnEventTrackingFailedListener() {
            @Override
            public void onFinishedEventTrackingFailed(AdjustEventFailure eventFailureResponseData) {
                Log.d("example", "failed event tracking: " + eventFailureResponseData.toString());
            }
        });

        // set session success tracking delegate
        config.setOnSessionTrackingSucceededListener(new OnSessionTrackingSucceededListener() {
            @Override
            public void onFinishedSessionTrackingSucceeded(AdjustSessionSuccess sessionSuccessResponseData) {
                Log.d("example", "success session tracking: " + sessionSuccessResponseData.toString());
            }
        });

        // set session failure tracking delegate
        config.setOnSessionTrackingFailedListener(new OnSessionTrackingFailedListener() {
            @Override
            public void onFinishedSessionTrackingFailed(AdjustSessionFailure sessionFailureResponseData) {
                Log.d("example", "failed session tracking: " + sessionFailureResponseData.toString());
            }
        });

        // evaluate deeplink to be launched
        config.setOnDeeplinkResponseListener(new OnDeeplinkResponseListener() {
            @Override
            public boolean launchReceivedDeeplink(Uri deeplink) {
                Log.d("example", "deeplink to open: " + deeplink);
                return true;
            }
        });

        // allow to send in the background
        config.setSendInBackground(true);

        // enable event buffering
        //config.setEventBufferingEnabled(true);

        Adjust.onCreate(config);

        // register onResume and onPause events of all activities
        // for applications with minSdkVersion >= 14
        registerActivityLifecycleCallbacks(new AdjustLifecycleCallbacks());

        // put the SDK in offline mode
        //Adjust.setOfflineMode(true);

        // disable the SDK
        //Adjust.setEnabled(false);
    }

    // you can use this class if your app is for Android 4.0 or higher
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
        public void onActivityStopped(Activity activity) {

        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {

        }

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

        }

        @Override
        public void onActivityStarted(Activity activity) {

        }
    }
}
