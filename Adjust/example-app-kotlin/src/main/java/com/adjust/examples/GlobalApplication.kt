package com.adjust.examples

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustConfig
import com.adjust.sdk.LogLevel

class GlobalApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Configure adjust SDK.
        val appToken = "2fm9gkqubvpc"
        val environment = AdjustConfig.ENVIRONMENT_SANDBOX

        val config = AdjustConfig(this, appToken, environment)

        // Change the log level.
        config.setLogLevel(LogLevel.VERBOSE)

        // Set attribution delegate.
        config.setOnAttributionChangedListener { attribution ->
            Log.d("example", "Attribution callback called!")
            Log.d("example", "Attribution: $attribution")
        }

        // Set event success tracking delegate.
        config.setOnEventTrackingSucceededListener { eventSuccessResponseData ->
            Log.d("example", "Event success callback called!")
            Log.d("example", "Event success data: $eventSuccessResponseData")
        }

        // Set event failure tracking delegate.
        config.setOnEventTrackingFailedListener { eventFailureResponseData ->
            Log.d("example", "Event failure callback called!")
            Log.d("example", "Event failure data: $eventFailureResponseData")
        }

        // Set session success tracking delegate.
        config.setOnSessionTrackingSucceededListener { sessionSuccessResponseData ->
            Log.d("example", "Session success callback called!")
            Log.d("example", "Session success data: $sessionSuccessResponseData")
        }

        // Set session failure tracking delegate.
        config.setOnSessionTrackingFailedListener { sessionFailureResponseData ->
            Log.d("example", "Session failure callback called!")
            Log.d("example", "Session failure data: $sessionFailureResponseData")
        }

        // Evaluate deferred deep link to be launched.
        config.setOnDeeplinkResponseListener { deeplink ->
            Log.d("example", "Deferred deep link callback called!")
            Log.d("example", "Deep link URL: $deeplink")

            true
        }

        // Set default tracker.
        // config.setDefaultTracker("{YourDefaultTracker}");

        // Set process name.
        // config.setProcessName("com.adjust.examples");

        // Allow to send in the background.
        config.setSendInBackground(true)

        // Enable event buffering.
        // config.setEventBufferingEnabled(true);

        // Delay first session.
        // config.setDelayStart(7);

        // Allow tracking preinstall
        // config.setPreinstallTrackingEnabled(true);

        // Add session callback parameters.
        Adjust.addGlobalCallbackParameter("sc_foo", "sc_bar")
        Adjust.addGlobalCallbackParameter("sc_key", "sc_value")

        // Add global partner parameters.
        Adjust.addGlobalPartnerParameter("sp_foo", "sp_bar")
        Adjust.addGlobalPartnerParameter("sp_key", "sp_value")

        // Remove session callback parameters.
        Adjust.removeGlobalCallbackParameter("sc_foo")

        // Remove global partner parameters.
        Adjust.removeGlobalPartnerParameter("sp_key")

        // Remove all session callback parameters.
        Adjust.resetGlobalCallbackParameters()

        // Remove all global partner parameters.
        Adjust.resetGlobalPartnerParameters()

        // Enable IMEI reading ONLY IF:
        // - IMEI plugin is added to your app.
        // - Your app is NOT distributed in Google Play Store.
        // AdjustImei.readImei()

        // Enable OAID reading ONLY IF:
        // - OAID plugin is added to your app.
        // - Your app is NOT distributed in Google Play Store & supports OAID.
        // AdjustOaid.readOaid()

        // Initialise the adjust SDK.
        Adjust.onCreate(config)

        // Abort delay for the first session introduced with setDelayStart method.
        // Adjust.sendFirstPackages();

        // Register onResume and onPause events of all activities
        // for applications with minSdkVersion >= 14.
        registerActivityLifecycleCallbacks(AdjustLifecycleCallbacks())

        // Put the SDK in offline mode.
        // Adjust.setOfflineMode(true);

        // Disable the SDK
        // Adjust.setEnabled(false);

        // Send push notification token.
        // Adjust.setPushToken("token");

    }

    // You can use this class if your app is for Android 4.0 or higher
    private class AdjustLifecycleCallbacks : Application.ActivityLifecycleCallbacks {
        override fun onActivityResumed(activity: Activity) {
            Adjust.onResume()
        }

        override fun onActivityPaused(activity: Activity) {
            Adjust.onPause()
        }

        override fun onActivityStopped(activity: Activity) {}

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

        override fun onActivityDestroyed(activity: Activity) {}

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

        override fun onActivityStarted(activity: Activity) {}
    }
}
