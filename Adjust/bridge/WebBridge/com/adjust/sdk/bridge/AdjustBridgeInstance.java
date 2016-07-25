package com.adjust.sdk.bridge;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.app.Application;

import com.adjust.sdk.*;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by uerceg on 22/07/16.
 */
public class AdjustBridgeInstance {
    private static final String KEY_APP_TOKEN                   = "appToken";
    private static final String KEY_ENVIRONMENT                 = "environment";
    private static final String KEY_LOG_LEVEL                   = "logLevel";
    private static final String KEY_SDK_PREFIX                  = "sdkPrefix";
    private static final String KEY_PROCESS_NAME                = "processName";
    private static final String KEY_DEFAULT_TRACKER             = "defaultTracker";
    private static final String KEY_SEND_IN_BACKGROUND          = "sendInBackground";
    private static final String KEY_OPEN_DEFERRED_DEEPLINK      = "openDeferredDeeplink";
    private static final String KEY_EVENT_BUFFERING_ENABLED     = "eventBufferingEnabled";

    private static final String KEY_EVENT_TOKEN                 = "eventToken";
    private static final String KEY_REVENUE                     = "revenue";
    private static final String KEY_CURRENCY                    = "currency";
    private static final String KEY_CALLBACK_PARAMETERS         = "callbackParameters";
    private static final String KEY_PARTNER_PARAMETERS          = "partnerParameters";

    private static final String LOG_LEVEL_VERBOSE               = "VERBOSE";
    private static final String LOG_LEVEL_DEBUG                 = "DEBUG";
    private static final String LOG_LEVEL_INFO                  = "INFO";
    private static final String LOG_LEVEL_WARN                  = "WARN";
    private static final String LOG_LEVEL_ERROR                 = "ERROR";
    private static final String LOG_LEVEL_ASSERT                = "ASSERT";

    private WebView webView;
    private Application application;

    private boolean isInitialized = false;
    private boolean shouldDeferredDeeplinkBeLaunched = true;

    private String attributionCallbackName;
    private String sessionSuccessCallbackName;
    private String sessionFailureCallbackName;
    private String eventSuccessCallbackName;
    private String eventFailureCallbackName;
    private String deferredDeeplinkCallbackName;

    private ArrayList<Uri> deeplinkQueue;

    // Automatically subscribe to Android lifecycle callbacks to properly handle session tracking.
    // This requires user to have minimal supported API level set to 14.
    private static final class AdjustLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {
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

    public void setWebView(WebView webView) {
        this.webView = webView;
    }

    public void setApplicationContext(Application application) {
        this.application = application;
    }

    @JavascriptInterface
    public void onCreate(String adjustConfigString) {
        // Initialise SDK only if it's not already initialised.
        if (isInitialized) {
            return;
        }

        // Without application reference, initialisation is not possible.
        if (application == null) {
            return;
        }

        // TODO: Do we need to check for web view in this moment?
        /*
        if (webView == null) {
            return;
        }
        */

        try {
            JSONObject jsonAdjustConfig = new JSONObject(adjustConfigString);

            Object appToken = jsonAdjustConfig.get(KEY_APP_TOKEN);
            Object environment = jsonAdjustConfig.get(KEY_ENVIRONMENT);
            Object logLevel = jsonAdjustConfig.get(KEY_LOG_LEVEL);
            Object sdkPrefix = jsonAdjustConfig.get(KEY_SDK_PREFIX);
            Object defaultTracker = jsonAdjustConfig.get(KEY_DEFAULT_TRACKER);
            Object processName = jsonAdjustConfig.get(KEY_PROCESS_NAME);
            Object sendInBackground = jsonAdjustConfig.get(KEY_SEND_IN_BACKGROUND);
            Object eventBufferingEnabled = jsonAdjustConfig.get(KEY_EVENT_BUFFERING_ENABLED);
            Object shouldOpenDeferredDeeplink = jsonAdjustConfig.get(KEY_OPEN_DEFERRED_DEEPLINK);

            if (!AdjustBridgeUtil.isFieldValid(appToken) || !AdjustBridgeUtil.isFieldValid(environment)) {
                return;
            }

            AdjustConfig adjustConfig = new AdjustConfig(application.getApplicationContext(), appToken.toString(), environment.toString());

            if (!adjustConfig.isValid()) {
                return;
            }

            // Log level
            if (AdjustBridgeUtil.isFieldValid(logLevel)) {
                if (logLevel.toString().equalsIgnoreCase(LOG_LEVEL_VERBOSE)) {
                    adjustConfig.setLogLevel(LogLevel.VERBOSE);
                } else if (logLevel.toString().equalsIgnoreCase(LOG_LEVEL_DEBUG)) {
                    adjustConfig.setLogLevel(LogLevel.DEBUG);
                } else if (logLevel.toString().equalsIgnoreCase(LOG_LEVEL_INFO)) {
                    adjustConfig.setLogLevel(LogLevel.INFO);
                } else if (logLevel.toString().equalsIgnoreCase(LOG_LEVEL_WARN)) {
                    adjustConfig.setLogLevel(LogLevel.WARN);
                } else if (logLevel.toString().equalsIgnoreCase(LOG_LEVEL_ERROR)) {
                    adjustConfig.setLogLevel(LogLevel.ERROR);
                } else if (logLevel.toString().equalsIgnoreCase(LOG_LEVEL_ASSERT)) {
                    adjustConfig.setLogLevel(LogLevel.ASSERT);
                } else {
                    adjustConfig.setLogLevel(LogLevel.INFO);
                }
            }

            // SDK prefix
            if (AdjustBridgeUtil.isFieldValid(sdkPrefix)) {
                adjustConfig.setSdkPrefix(sdkPrefix.toString());
            }

            // Event buffering
            if (AdjustBridgeUtil.isFieldValid(eventBufferingEnabled)) {
                if (eventBufferingEnabled.toString().equalsIgnoreCase("true") || eventBufferingEnabled.toString().equalsIgnoreCase("false")) {
                    adjustConfig.setEventBufferingEnabled(Boolean.valueOf(eventBufferingEnabled.toString()));
                }
            }

            // Tracking in background
            if (AdjustBridgeUtil.isFieldValid(sendInBackground)) {
                if (sendInBackground.toString().equalsIgnoreCase("true") || sendInBackground.toString().equalsIgnoreCase("false")) {
                    adjustConfig.setSendInBackground(Boolean.valueOf(sendInBackground.toString()));
                }
            }

            // Main process name
            if (AdjustBridgeUtil.isFieldValid(processName)) {
                adjustConfig.setProcessName(processName.toString());
            }

            // Default tracker
            if (AdjustBridgeUtil.isFieldValid(defaultTracker)) {
                adjustConfig.setDefaultTracker(defaultTracker.toString());
            }

            // Should deferred deep link be opened?
            if (AdjustBridgeUtil.isFieldValid(shouldOpenDeferredDeeplink)) {
                if (shouldOpenDeferredDeeplink.toString().equalsIgnoreCase("true") || shouldOpenDeferredDeeplink.toString().equalsIgnoreCase("false")) {
                    shouldDeferredDeeplinkBeLaunched = Boolean.valueOf(shouldOpenDeferredDeeplink.toString());
                }
            }

            // Attribution callback
            if (attributionCallbackName != null && !attributionCallbackName.isEmpty()) {
                adjustConfig.setOnAttributionChangedListener(new OnAttributionChangedListener() {
                    @Override
                    public void onAttributionChanged(AdjustAttribution attribution) {
                        AdjustBridgeUtil.execAttributionCallbackCommand(webView, attributionCallbackName, attribution);
                    }
                });
            }

            // Session success callback
            if (sessionSuccessCallbackName != null && !sessionSuccessCallbackName.isEmpty()) {
                adjustConfig.setOnSessionTrackingSucceededListener(new OnSessionTrackingSucceededListener() {
                    @Override
                    public void onFinishedSessionTrackingSucceeded(AdjustSessionSuccess sessionSuccessResponseData) {
                        AdjustBridgeUtil.execSessionSuccessCallbackCommand(webView, sessionSuccessCallbackName, sessionSuccessResponseData);
                    }
                });
            }

            // Session failure callback
            if (sessionFailureCallbackName != null && !sessionFailureCallbackName.isEmpty()) {
                adjustConfig.setOnSessionTrackingFailedListener(new OnSessionTrackingFailedListener() {
                    @Override
                    public void onFinishedSessionTrackingFailed(AdjustSessionFailure failureResponseData) {
                        AdjustBridgeUtil.execSessionFailureCallbackCommand(webView, sessionFailureCallbackName, failureResponseData);
                    }
                });
            }

            // Event success callback
            if (eventSuccessCallbackName != null && !eventSuccessCallbackName.isEmpty()) {
                adjustConfig.setOnEventTrackingSucceededListener(new OnEventTrackingSucceededListener() {
                    @Override
                    public void onFinishedEventTrackingSucceeded(AdjustEventSuccess eventSuccessResponseData) {
                        AdjustBridgeUtil.execEventSuccessCallbackCommand(webView, eventSuccessCallbackName, eventSuccessResponseData);
                    }
                });
            }

            // Event failure callback
            if (eventFailureCallbackName != null && !eventFailureCallbackName.isEmpty()) {
                adjustConfig.setOnEventTrackingFailedListener(new OnEventTrackingFailedListener() {
                    @Override
                    public void onFinishedEventTrackingFailed(AdjustEventFailure eventFailureResponseData) {
                        AdjustBridgeUtil.execEventFailureCallbackCommand(webView, eventFailureCallbackName, eventFailureResponseData);
                    }
                });
            }

            // Deferred deeplink callback
            if (deferredDeeplinkCallbackName != null && !deferredDeeplinkCallbackName.isEmpty()) {
                adjustConfig.setOnDeeplinkResponseListener(new OnDeeplinkResponseListener() {
                    @Override
                    public boolean launchReceivedDeeplink(Uri deeplink) {
                        AdjustBridgeUtil.execDeferredDeeplinkCallbackCommand(webView, deferredDeeplinkCallbackName, deeplink.toString());
                        return shouldDeferredDeeplinkBeLaunched;
                    }
                });
            }

            // Manually call onResume() because web view initialisation will happen a bit delayed.
            // With this delay, it will miss lifecycle callback onResume() initial firing.
            Adjust.onCreate(adjustConfig);
            Adjust.onResume();

            isInitialized = true;
            application.registerActivityLifecycleCallbacks(new AdjustLifecycleCallbacks());

            // Check if any deeplink got queued before SDK initialised and process them if any.
            dequeueDeeplinks();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @JavascriptInterface
    public void trackEvent(String adjustEventString) {
        if (!isInitialized) {
            return;
        }

        // TODO: Do we need to check for web view in this moment?
        /*
        if (webView == null) {
            return;
        }
        */

        try {
            JSONObject jsonAdjustEvent = new JSONObject(adjustEventString);

            Object eventToken = jsonAdjustEvent.get(KEY_EVENT_TOKEN);
            Object revenue = jsonAdjustEvent.get(KEY_REVENUE);
            Object currency = jsonAdjustEvent.get(KEY_CURRENCY);

            JSONArray partnerParametersJson = (JSONArray)jsonAdjustEvent.get(KEY_PARTNER_PARAMETERS);
            JSONArray callbackParametersJson = (JSONArray)jsonAdjustEvent.get(KEY_CALLBACK_PARAMETERS);
            String[] partnerParameters = AdjustBridgeUtil.jsonArrayToArray(partnerParametersJson);
            String[] callbackParameters = AdjustBridgeUtil.jsonArrayToArray(callbackParametersJson);

            if (!AdjustBridgeUtil.isFieldValid(eventToken)) {
                return;
            }

            AdjustEvent adjustEvent = new AdjustEvent(eventToken.toString());

            if (!adjustEvent.isValid()) {
                return;
            }

            if (AdjustBridgeUtil.isFieldValid(revenue) && AdjustBridgeUtil.isFieldValid(currency)) {
                try {
                    double revenueValue = Double.parseDouble(revenue.toString());
                    adjustEvent.setRevenue(revenueValue, currency.toString());
                } catch (Exception e) {
                    ILogger logger = AdjustFactory.getLogger();
                    logger.error("Unable to parse given revenue value");
                }
            }

            for (int i = 0; i < callbackParameters.length; i +=2) {
                String key = callbackParameters[i];
                String value = callbackParameters[i+1];

                adjustEvent.addCallbackParameter(key, value);
            }

            for (int i = 0; i < partnerParameters.length; i += 2) {
                String key = partnerParameters[i];
                String value = partnerParameters[i+1];

                adjustEvent.addPartnerParameter(key, value);
            }

            Adjust.trackEvent(adjustEvent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @JavascriptInterface
    public void setOfflineMode(String isOffline) {
        // TODO: Do we need to check if SDK is initialised in this moment?
        if (!isInitialized) {
            return;
        }

        if (isOffline == null) {
            return;
        }

        if (isOffline.isEmpty()) {
            return;
        }

        Adjust.setOfflineMode(Boolean.valueOf(isOffline));
    }

    @JavascriptInterface
    public void setEnabled(String isEnabled) {
        // TODO: Do we need to check if SDK is initialised in this moment?
        if (!isInitialized) {
            return;
        }

        if (isEnabled == null) {
            return;
        }

        if (isEnabled.isEmpty()) {
            return;
        }

        Adjust.setEnabled(Boolean.valueOf(isEnabled));
    }

    @JavascriptInterface
    public void isEnabled(String callback) {
        // TODO: Do we need to check if SDK is initialised in this moment?
        if (!isInitialized) {
            return;
        }

        if (webView == null) {
            return;
        }

        boolean isEnabled = Adjust.isEnabled();
        AdjustBridgeUtil.execIsEnabledCallbackCommand(webView, callback, String.valueOf(isEnabled));
    }

    @JavascriptInterface
    public void getGoogleAdId(final String callback) {
        // TODO: Do we need to check if SDK is initialised in this moment?
        if (!isInitialized) {
            return;
        }

        if (webView == null) {
            return;
        }

        Adjust.getGoogleAdId(application.getApplicationContext(), new OnDeviceIdsRead() {
            @Override
            public void onGoogleAdIdRead(String googleAdId) {
                AdjustBridgeUtil.execGetGoogleAdIdCallbackCommand(webView, callback, googleAdId);
            }
        });
    }

    @JavascriptInterface
    public void setAttributionCallback(String callback) {
        if (callback == null) {
            return;
        }

        if (callback.isEmpty()) {
            return;
        }

        attributionCallbackName = callback;
    }

    @JavascriptInterface
    public void setSessionSuccessCallback(String callback) {
        if (callback == null) {
            return;
        }

        if (callback.isEmpty()) {
            return;
        }

        sessionSuccessCallbackName = callback;
    }

    @JavascriptInterface
    public void setSessionFailureCallback(String callback) {
        if (callback == null) {
            return;
        }

        if (callback.isEmpty()) {
            return;
        }

        sessionFailureCallbackName = callback;
    }

    @JavascriptInterface
    public void setEventSuccessCallback(String callback) {
        if (callback == null) {
            return;
        }

        if (callback.isEmpty()) {
            return;
        }

        eventSuccessCallbackName = callback;
    }

    @JavascriptInterface
    public void setEventFailureCallback(String callback) {
        if (callback == null) {
            return;
        }

        if (callback.isEmpty()) {
            return;
        }

        eventFailureCallbackName = callback;
    }

    @JavascriptInterface
    public void setDeferredDeeplinkCallback(String callback) {
        if (callback == null) {
            return;
        }

        if (callback.isEmpty()) {
            return;
        }

        deferredDeeplinkCallbackName = callback;
    }

    public void deeplinkReceived(final Uri deeplink) {
        if (deeplink == null) {
            return;
        }

        if (!isInitialized) {
            // Deeplink was passed to the bridge, but SDK was still not initialised.
            // Queue it and wait for SDK to initialise and process it.
            queueDeeplink(deeplink);

            return;
        }

        checkForDeeplinkReattributions(deeplink);
        AdjustBridgeUtil.sendDeeplinkToWebView(webView, deeplink);
    }

    private void queueDeeplink(Uri deeplink) {
        if (deeplinkQueue == null) {
            deeplinkQueue = new ArrayList<Uri>();
        }

        deeplinkQueue.add(deeplink);
    }

    private void dequeueDeeplinks() {
        if (deeplinkQueue == null) {
            return;
        }

        for (final Uri deeplink : deeplinkQueue) {
            checkForDeeplinkReattributions(deeplink);
            AdjustBridgeUtil.sendDeeplinkToWebView(webView, deeplink);
        }

        deeplinkQueue.clear();
    }

    private void checkForDeeplinkReattributions(Uri deeplink) {
        // If the SDK is initialised, check for deeplink reattributions automatically.
        // No need to bother the user to do this in Javascript.
        if (isInitialized) {
            Adjust.appWillOpenUrl(deeplink);
        }
    }
}
