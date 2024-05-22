package com.adjust.examples;

import android.app.Activity;
import android.app.Application;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.adjust.sdk.Adjust;
import com.adjust.sdk.AdjustAttribution;
import com.adjust.sdk.AdjustConfig;
import com.adjust.sdk.AdjustEventFailure;
import com.adjust.sdk.AdjustEventSuccess;
import com.adjust.sdk.AdjustSessionFailure;
import com.adjust.sdk.AdjustSessionSuccess;
import com.adjust.sdk.GooglePlayInstallReferrerDetails;
import com.adjust.sdk.LogLevel;
import com.adjust.sdk.OnAdidReadListener;
import com.adjust.sdk.OnAmazonAdIdReadListener;
import com.adjust.sdk.OnAttributionChangedListener;
import com.adjust.sdk.OnDeeplinkResponseListener;
import com.adjust.sdk.OnEventTrackingFailedListener;
import com.adjust.sdk.OnEventTrackingSucceededListener;
import com.adjust.sdk.OnGooglePlayInstallReferrerReadListener;
import com.adjust.sdk.OnSessionTrackingFailedListener;
import com.adjust.sdk.OnSessionTrackingSucceededListener;

/**
 * Created by pfms on 17/12/14.
 */
public class GlobalApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

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

        // Allow tracking preinstall
        // config.setPreinstallTrackingEnabled(true);

        // Add session callback parameters.
        Adjust.addGlobalCallbackParameter("sc_foo", "sc_bar");
        Adjust.addGlobalCallbackParameter("sc_key", "sc_value");

        // Add global partner parameters.
        Adjust.addGlobalPartnerParameter("sp_foo", "sp_bar");
        Adjust.addGlobalPartnerParameter("sp_key", "sp_value");

        // Remove session callback parameters.
        Adjust.removeGlobalCallbackParameter("sc_foo");

        // Remove global partner parameters.
        Adjust.removeGlobalPartnerParameter("sp_key");

        // Remove all session callback parameters.
        Adjust.removeGlobalCallbackParameters();

        // Remove all global partner parameters.
        Adjust.removeGlobalPartnerParameters();

        // Enable IMEI reading ONLY IF:
        // - IMEI plugin is added to your app.
        // - Your app is NOT distributed in Google Play Store.
        // AdjustImei.readImei();

        // Enable OAID reading ONLY IF:
        // - OAID plugin is added to your app.
        // - Your app is NOT distributed in Google Play Store & supports OAID.
        // AdjustOaid.readOaid();

        // Initialise the adjust SDK.
        Adjust.onCreate(config);

        // Get the adid.
        Adjust.getAdid(new OnAdidReadListener() {
            @Override
            public void onAdidRead(String adid) {
                Log.d("example", "Adid callback called!");
                Log.d("example", "Adid: " + adid);
            }
        });

        Adjust.getGooglePlayInstallReferrer(this, new OnGooglePlayInstallReferrerReadListener() {
            @Override
            public void onInstallReferrerRead(GooglePlayInstallReferrerDetails referrerDetails) {
                Log.d("example", "referrerApi : " + referrerDetails.toString());
            }

            @Override
            public void onFailure(String message) {
                Log.d("example", "failed : " + message);

            }
        });

        // Register onResume and onPause events of all activities
        // for applications with minSdkVersion >= 14.
        registerActivityLifecycleCallbacks(new AdjustLifecycleCallbacks());

        Adjust.getAmazonAdId(this, new OnAmazonAdIdReadListener() {
            @Override
            public void onAmazonAdIdRead(String amazonAdId) {
                Log.d("example", "amazonAdId : " + amazonAdId);
            }

            @Override
            public void onFail(String message) {
                Log.d("example", "failed : " + message);
            }
        });

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
