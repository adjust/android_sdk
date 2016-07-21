package com.adjust.sdk.bridge;

import android.app.Activity;
import android.app.Application;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.adjust.sdk.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by uerceg on 10/06/16.
 */
public class AdjustBridge {
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

    private static AdjustBridge defaultInstance;

    private static WebView webView;
    private static Application application;

    private static boolean isInitialized = false;
    private static boolean shouldDeferredDeeplinkBeLaunched = true;

    private static String attributionCallbackName;
    private static String sessionSuccessCallbackName;
    private static String sessionFailureCallbackName;
    private static String eventSuccessCallbackName;
    private static String eventFailureCallbackName;
    private static String deferredDeeplinkCallbackName;

    private static ArrayList<Uri> deeplinkQueue;

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

    @JavascriptInterface
    public static void appDidLaunch(String adjustConfigString) {
        if (isInitialized) {
            return;
        }

        if (webView == null) {
            return;
        }

        if (application == null) {
            return;
        }

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

            if (!isFieldValid(appToken) || !isFieldValid(environment)) {
                return;
            }

            AdjustConfig adjustConfig = new AdjustConfig(application.getApplicationContext(), appToken.toString(), environment.toString());

            if (!adjustConfig.isValid()) {
                return;
            }

            // Log level
            if (isFieldValid(logLevel)) {
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
            if (isFieldValid(sdkPrefix)) {
                adjustConfig.setSdkPrefix(sdkPrefix.toString());
            }

            // Event buffering
            if (isFieldValid(eventBufferingEnabled)) {
                if (eventBufferingEnabled.toString().equalsIgnoreCase("true") || eventBufferingEnabled.toString().equalsIgnoreCase("false")) {
                    adjustConfig.setEventBufferingEnabled(Boolean.valueOf(eventBufferingEnabled.toString()));
                }
            }

            // Tracking in background
            if (isFieldValid(sendInBackground)) {
                if (sendInBackground.toString().equalsIgnoreCase("true") || sendInBackground.toString().equalsIgnoreCase("false")) {
                    adjustConfig.setSendInBackground(Boolean.valueOf(sendInBackground.toString()));
                }
            }

            // Main process name
            if (isFieldValid(processName)) {
                adjustConfig.setProcessName(processName.toString());
            }

            // Default tracker
            if (isFieldValid(defaultTracker)) {
                adjustConfig.setDefaultTracker(defaultTracker.toString());
            }

            // Should deferred deep link be opened?
            if (isFieldValid(shouldOpenDeferredDeeplink)) {
                if (shouldOpenDeferredDeeplink.toString().equalsIgnoreCase("true") || shouldOpenDeferredDeeplink.toString().equalsIgnoreCase("false")) {
                    shouldDeferredDeeplinkBeLaunched = Boolean.valueOf(shouldOpenDeferredDeeplink.toString());
                }
            }

            // Attribution callback
            if (attributionCallbackName != null && !attributionCallbackName.isEmpty()) {
                adjustConfig.setOnAttributionChangedListener(new OnAttributionChangedListener() {
                    @Override
                    public void onAttributionChanged(AdjustAttribution attribution) {
                        execAttributionCallbackCommand(attributionCallbackName, attribution);
                    }
                });
            }

            // Session success callback
            if (sessionSuccessCallbackName != null && !sessionSuccessCallbackName.isEmpty()) {
                adjustConfig.setOnSessionTrackingSucceededListener(new OnSessionTrackingSucceededListener() {
                    @Override
                    public void onFinishedSessionTrackingSucceeded(AdjustSessionSuccess sessionSuccessResponseData) {
                        execSessionSuccessCallbackCommand(sessionSuccessCallbackName, sessionSuccessResponseData);
                    }
                });
            }

            // Session failure callback
            if (sessionFailureCallbackName != null && !sessionFailureCallbackName.isEmpty()) {
                adjustConfig.setOnSessionTrackingFailedListener(new OnSessionTrackingFailedListener() {
                    @Override
                    public void onFinishedSessionTrackingFailed(AdjustSessionFailure failureResponseData) {
                        execSessionFailureCallbackCommand(sessionFailureCallbackName, failureResponseData);
                    }
                });
            }

            // Event success callback
            if (eventSuccessCallbackName != null && !eventSuccessCallbackName.isEmpty()) {
                adjustConfig.setOnEventTrackingSucceededListener(new OnEventTrackingSucceededListener() {
                    @Override
                    public void onFinishedEventTrackingSucceeded(AdjustEventSuccess eventSuccessResponseData) {
                        execEventSuccessCallbackCommand(eventSuccessCallbackName, eventSuccessResponseData);
                    }
                });
            }

            // Event failure callback
            if (eventFailureCallbackName != null && !eventFailureCallbackName.isEmpty()) {
                adjustConfig.setOnEventTrackingFailedListener(new OnEventTrackingFailedListener() {
                    @Override
                    public void onFinishedEventTrackingFailed(AdjustEventFailure eventFailureResponseData) {
                        execEventFailureCallbackCommand(eventFailureCallbackName, eventFailureResponseData);
                    }
                });
            }

            // Deferred deeplink callback
            if (deferredDeeplinkCallbackName != null && !deferredDeeplinkCallbackName.isEmpty()) {
                adjustConfig.setOnDeeplinkResponseListener(new OnDeeplinkResponseListener() {
                    @Override
                    public boolean launchReceivedDeeplink(Uri deeplink) {
                        execDeferredDeeplinkCallbackCommand(deferredDeeplinkCallbackName, deeplink.toString());
                        return shouldDeferredDeeplinkBeLaunched;
                    }
                });
            }

            Adjust.onCreate(adjustConfig);
            Adjust.onResume();

            isInitialized = true;
            application.registerActivityLifecycleCallbacks(new AdjustLifecycleCallbacks());

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

        if (webView == null) {
            return;
        }

        try {
            JSONObject jsonAdjustEvent = new JSONObject(adjustEventString);

            Object eventToken = jsonAdjustEvent.get(KEY_EVENT_TOKEN);
            Object revenue = jsonAdjustEvent.get(KEY_REVENUE);
            Object currency = jsonAdjustEvent.get(KEY_CURRENCY);

            JSONArray partnerParametersJson = (JSONArray)jsonAdjustEvent.get(KEY_PARTNER_PARAMETERS);
            JSONArray callbackParametersJson = (JSONArray)jsonAdjustEvent.get(KEY_CALLBACK_PARAMETERS);
            String[] partnerParameters = jsonArrayToArray(partnerParametersJson);
            String[] callbackParameters = jsonArrayToArray(callbackParametersJson);

            if (!isFieldValid(eventToken)) {
                return;
            }

            AdjustEvent adjustEvent = new AdjustEvent(eventToken.toString());

            if (!adjustEvent.isValid()) {
                return;
            }

            if (isFieldValid(revenue) && isFieldValid(currency)) {
                try {
                    double revenueValue = Double.parseDouble(revenue.toString());
                    adjustEvent.setRevenue(revenueValue, currency.toString());
                } catch (Exception e) {
                    ILogger logger = AdjustFactory.getLogger();
                    logger.error("Unable to parse revenue");
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
        if (!isInitialized) {
            return;
        }

        if (webView == null) {
            return;
        }

        boolean isEnabled = Adjust.isEnabled();

        execIsEnabledCallbackCommand(callback, String.valueOf(isEnabled));
    }

    @JavascriptInterface
    public void getGoogleAdId(final String callback) {
        if (!isInitialized) {
            return;
        }

        if (webView == null) {
            return;
        }

        Adjust.getGoogleAdId(application.getApplicationContext(), new OnDeviceIdsRead() {
            @Override
            public void onGoogleAdIdRead(String googleAdId) {
                execGetGoogleAdIdCallbackCommand(callback, googleAdId);
            }
        });
    }

    @JavascriptInterface
    public void setAttributionCallback(String callback) {
        if (!isInitialized) {
            return;
        }

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
        if (!isInitialized) {
            return;
        }

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
        if (!isInitialized) {
            return;
        }

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
        if (!isInitialized) {
            return;
        }

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
        if (!isInitialized) {
            return;
        }

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
        if (!isInitialized) {
            return;
        }

        if (callback == null) {
            return;
        }

        if (callback.isEmpty()) {
            return;
        }

        deferredDeeplinkCallbackName = callback;
    }

    public static synchronized AdjustBridge getDefaultInstance() {
        if (defaultInstance == null) {
            defaultInstance = new AdjustBridge();
        }

        return defaultInstance;
    }

    public static void setWebView(WebView pWebView) {
        webView = pWebView;
    }

    public static void setApplicationContext(Application pApplication) {
        application = pApplication;
    }

    public static void sendDeeplinkToWebView(final Uri deeplink) {
        if (deeplink == null) {
            return;
        }

        if (isInitialized) {
            if (webView != null) {
                webView.post(new Runnable() {
                    @Override
                    public void run() {
                        String command = "javascript:adjust_deeplink('" + deeplink.toString() + "');";
                        webView.loadUrl(command);
                    }
                });
            }

            Adjust.appWillOpenUrl(deeplink);
        } else {
            queueDeeplink(deeplink);
        }
    }

    private static void queueDeeplink(Uri deeplink) {
        if (deeplinkQueue == null) {
            deeplinkQueue = new ArrayList<Uri>();
        }

        deeplinkQueue.add(deeplink);
    }

    private static void dequeueDeeplinks() {
        if (deeplinkQueue == null) {
            return;
        }

        for (final Uri deeplink : deeplinkQueue) {
            if (isInitialized) {
                Adjust.appWillOpenUrl(deeplink);
            }

            if (webView != null) {
                webView.post(new Runnable() {
                    @Override
                    public void run() {
                        String command = "javascript:adjust_deeplink('" + deeplink.toString() + "');";
                        webView.loadUrl(command);
                    }
                });
            }
        }

        deeplinkQueue.clear();
    }

    private static boolean isFieldValid(Object field) {
        if (field != null) {
            if (!field.toString().equals("") && !field.toString().equals("null")) {
                return true;
            }
        }

        return false;
    }

    private static void execAttributionCallbackCommand(final String commandName, final AdjustAttribution attribution) {
        if (webView == null) {
            return;
        }

        webView.post(new Runnable() {
            @Override
            public void run() {
                JSONObject jsonAttribution = new JSONObject();

                try {
                    jsonAttribution.put("trackerName", attribution.trackerName == null ? JSONObject.NULL : attribution.trackerName);
                    jsonAttribution.put("trackerToken", attribution.trackerToken == null ? JSONObject.NULL : attribution.trackerToken);
                    jsonAttribution.put("campaign", attribution.campaign == null ? JSONObject.NULL : attribution.campaign);
                    jsonAttribution.put("network", attribution.network == null ? JSONObject.NULL : attribution.network);
                    jsonAttribution.put("creative", attribution.creative == null ? JSONObject.NULL : attribution.creative);
                    jsonAttribution.put("adgroup", attribution.adgroup == null ? JSONObject.NULL : attribution.adgroup);
                    jsonAttribution.put("clickLabel", attribution.clickLabel == null ? JSONObject.NULL : attribution.clickLabel);

                    String command = "javascript:" + commandName + "(" + jsonAttribution.toString() + ");";

                    webView.loadUrl(command);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private static void execSessionSuccessCallbackCommand(final String commandName, final AdjustSessionSuccess sessionSuccess) {
        if (webView == null) {
            return;
        }

        webView.post(new Runnable() {
            @Override
            public void run() {
                JSONObject jsonSessionSuccess = new JSONObject();

                try {
                    jsonSessionSuccess.put("message", sessionSuccess.message == null ? JSONObject.NULL : sessionSuccess.message);
                    jsonSessionSuccess.put("adid", sessionSuccess.adid == null ? JSONObject.NULL : sessionSuccess.adid);
                    jsonSessionSuccess.put("timestamp", sessionSuccess.timestamp == null ? JSONObject.NULL : sessionSuccess.timestamp);
                    jsonSessionSuccess.put("jsonResponse", sessionSuccess.jsonResponse == null ? JSONObject.NULL : sessionSuccess.jsonResponse);

                    String command = "javascript:" + commandName + "(" + jsonSessionSuccess.toString() + ");";

                    webView.loadUrl(command);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private static void execSessionFailureCallbackCommand(final String commandName, final AdjustSessionFailure sessionFailure) {
        if (webView == null) {
            return;
        }

        webView.post(new Runnable() {
            @Override
            public void run() {
                JSONObject jsonSessionFailure = new JSONObject();

                try {
                    jsonSessionFailure.put("message", sessionFailure.message == null ? JSONObject.NULL : sessionFailure.message);
                    jsonSessionFailure.put("adid", sessionFailure.adid == null ? JSONObject.NULL : sessionFailure.adid);
                    jsonSessionFailure.put("timestamp", sessionFailure.timestamp == null ? JSONObject.NULL : sessionFailure.timestamp);
                    jsonSessionFailure.put("willRetry", sessionFailure.willRetry ? String.valueOf(true) : String.valueOf(false));
                    jsonSessionFailure.put("jsonResponse", sessionFailure.jsonResponse == null ? JSONObject.NULL : sessionFailure.jsonResponse);

                    String command = "javascript:" + commandName + "(" + jsonSessionFailure.toString() + ");";

                    webView.loadUrl(command);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private static void execEventSuccessCallbackCommand(final String commandName, final AdjustEventSuccess eventSuccess) {
        if (webView == null) {
            return;
        }

        webView.post(new Runnable() {
            @Override
            public void run() {
                JSONObject jsonEventSuccess = new JSONObject();

                try {
                    jsonEventSuccess.put("eventToken", eventSuccess.eventToken == null ? JSONObject.NULL : eventSuccess.eventToken);
                    jsonEventSuccess.put("message", eventSuccess.message == null ? JSONObject.NULL : eventSuccess.message);
                    jsonEventSuccess.put("adid", eventSuccess.adid == null ? JSONObject.NULL : eventSuccess.adid);
                    jsonEventSuccess.put("timestamp", eventSuccess.timestamp == null ? JSONObject.NULL : eventSuccess.timestamp);
                    jsonEventSuccess.put("jsonResponse", eventSuccess.jsonResponse == null ? JSONObject.NULL : eventSuccess.jsonResponse);

                    String command = "javascript:" + commandName + "(" + jsonEventSuccess.toString() + ");";

                    webView.loadUrl(command);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private static void execEventFailureCallbackCommand(final String commandName, final AdjustEventFailure eventFailure) {
        if (webView == null) {
            return;
        }

        webView.post(new Runnable() {
            @Override
            public void run() {
                JSONObject jsonEventFailure = new JSONObject();

                try {
                    jsonEventFailure.put("eventToken", eventFailure.eventToken == null ? JSONObject.NULL : eventFailure.eventToken);
                    jsonEventFailure.put("message", eventFailure.message == null ? JSONObject.NULL : eventFailure.message);
                    jsonEventFailure.put("adid", eventFailure.adid == null ? JSONObject.NULL : eventFailure.adid);
                    jsonEventFailure.put("timestamp", eventFailure.timestamp == null ? JSONObject.NULL : eventFailure.timestamp);
                    jsonEventFailure.put("willRetry", eventFailure.willRetry ? String.valueOf(true) : String.valueOf(false));
                    jsonEventFailure.put("jsonResponse", eventFailure.jsonResponse == null ? JSONObject.NULL : eventFailure.jsonResponse);

                    String command = "javascript:" + commandName + "(" + jsonEventFailure.toString() + ");";

                    webView.loadUrl(command);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private static void execDeferredDeeplinkCallbackCommand(final String commandName, final String deeplink) {
        if (webView == null) {
            return;
        }

        webView.post(new Runnable() {
            @Override
            public void run() {
                String command = "javascript:" + commandName + "(" + deeplink + ");";
                webView.loadUrl(command);
            }
        });
    }

    private static void execIsEnabledCallbackCommand(final String commandName, final String isEnabled) {
        if (webView == null) {
            return;
        }

        webView.post(new Runnable() {
            @Override
            public void run() {
                String command = "javascript:" + commandName + "(" + isEnabled + ");";
                webView.loadUrl(command);
            }
        });
    }

    private static void execGetGoogleAdIdCallbackCommand(final String commandName, final String gpsAdid) {
        if (webView == null) {
            return;
        }

        webView.post(new Runnable() {
            @Override
            public void run() {
                String command = "javascript:" + commandName + "(" + gpsAdid + ");";
                webView.loadUrl(command);
            }
        });
    }

    private String[] jsonArrayToArray(JSONArray jsonArray) throws JSONException {
        if (jsonArray != null) {
            String[] array = new String[jsonArray.length()];

            for (int i = 0; i < jsonArray.length(); i++) {
                array[i] = jsonArray.get(i).toString();
            }

            return array;
        }

        return null;
    }
}
