package com.adjust.example;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import com.adjust.sdk.Adjust;
import com.adjust.sdk.AdjustAttribution;
import com.adjust.sdk.AdjustConfig;
import com.adjust.sdk.EventFailureResponseData;
import com.adjust.sdk.EventSuccessResponseData;
import com.adjust.sdk.LogLevel;
import com.adjust.sdk.OnAttributionChangedListener;
import com.adjust.sdk.OnEventTrackingFailedListener;
import com.adjust.sdk.OnEventTrackingSucceededListener;
import com.adjust.sdk.OnSessionTrackingFailedListener;
import com.adjust.sdk.OnSessionTrackingSucceededListener;
import com.adjust.sdk.SessionFailureResponseData;
import com.adjust.sdk.SessionSuccessResponseData;

/**
 * Created by pfms on 17/12/14.
 */
public class GlobalApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // configure Adjust
        String appToken = "{YourAppToken}";
        String environment = AdjustConfig.ENVIRONMENT_SANDBOX;
        AdjustConfig config = new AdjustConfig(this, appToken, environment);

        // change the log level
        config.setLogLevel(LogLevel.VERBOSE);

        // enable event buffering
        //config.setEventBufferingEnabled(true);

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
            public void onFinishedEventTrackingSucceeded(EventSuccessResponseData eventSuccessResponseData) {
                Log.d("example", "success event tracking: " + eventSuccessResponseData.toString());
            }
        });

        // set event failure tracking delegate
        config.setOnEventTrackingFailedListener(new OnEventTrackingFailedListener() {
            @Override
            public void onFinishedEventTrackingFailed(EventFailureResponseData eventFailureResponseData) {
                Log.d("example", "failed event tracking: " + eventFailureResponseData.toString());
            }
        });

        // set session success tracking delegate
        config.setOnSessionTrackingSucceededListener(new OnSessionTrackingSucceededListener() {
            @Override
            public void onFinishedSessionTrackingSucceeded(SessionSuccessResponseData sessionSuccessResponseData) {
                Log.d("example", "success session tracking: " + sessionSuccessResponseData.toString());
            }
        });

        // set session failure tracking delegate
        config.setOnSessionTrackingFailedListener(new OnSessionTrackingFailedListener() {
            @Override
            public void onFinishedSessionTrackingFailed(SessionFailureResponseData sessionFailureResponseData) {
                Log.d("example", "failed session tracking: " + sessionFailureResponseData.toString());
            }
        });

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
