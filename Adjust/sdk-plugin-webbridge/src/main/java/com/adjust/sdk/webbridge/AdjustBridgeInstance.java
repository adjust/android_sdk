package com.adjust.sdk.webbridge;

import android.annotation.TargetApi;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.app.Application;

import com.adjust.sdk.Adjust;
import com.adjust.sdk.AdjustAttribution;
import com.adjust.sdk.AdjustConfig;
import com.adjust.sdk.AdjustEvent;
import com.adjust.sdk.AdjustEventFailure;
import com.adjust.sdk.AdjustEventSuccess;
import com.adjust.sdk.AdjustFactory;
import com.adjust.sdk.AdjustSessionFailure;
import com.adjust.sdk.AdjustSessionSuccess;
import com.adjust.sdk.AdjustTestOptions;
import com.adjust.sdk.AdjustThirdPartySharing;
import com.adjust.sdk.LogLevel;
import com.adjust.sdk.OnAttributionChangedListener;
import com.adjust.sdk.OnDeeplinkResponseListener;
import com.adjust.sdk.OnDeviceIdsRead;
import com.adjust.sdk.OnEventTrackingFailedListener;
import com.adjust.sdk.OnEventTrackingSucceededListener;
import com.adjust.sdk.OnSessionTrackingFailedListener;
import com.adjust.sdk.OnSessionTrackingSucceededListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * Created by uerceg on 22/07/16.
 */
public class AdjustBridgeInstance {
    private static final String LOG_LEVEL_VERBOSE = "VERBOSE";
    private static final String LOG_LEVEL_DEBUG = "DEBUG";
    private static final String LOG_LEVEL_INFO = "INFO";
    private static final String LOG_LEVEL_WARN = "WARN";
    private static final String LOG_LEVEL_ERROR = "ERROR";
    private static final String LOG_LEVEL_ASSERT = "ASSERT";
    private static final String LOG_LEVEL_SUPPRESS = "SUPPRESS";

    private static final String JAVASCRIPT_INTERFACE_NAME = "AdjustBridge";
    private static final String FB_JAVASCRIPT_INTERFACE_NAME_PREFIX = "fbmq_";

    private WebView webView;
    private Application application;
    private boolean isInitialized = false;
    private boolean shouldDeferredDeeplinkBeLaunched = true;
    private FacebookSDKJSInterface facebookSDKJSInterface = null;

    AdjustBridgeInstance() {}

    AdjustBridgeInstance(Application application, WebView webView) {
        this.application = application;
        setWebView(webView);
    }

    // Automatically subscribe to Android lifecycle callbacks to properly handle session tracking.
    // This requires user to have minimal supported API level set to 14.
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
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

    private boolean isInitialized() {
        if (webView == null) {
            AdjustBridgeUtil.getLogger().error("Webview missing. Call AdjustBridge.setWebView before");
            return false;
        }
        if (application == null) {
            AdjustBridgeUtil.getLogger().error("Application context missing. Call AdjustBridge.setApplicationContext before");
            return false;
        }
        return true;
    }

    public void registerFacebookSDKJSInterface() {
        // Configure the web view to add fb pixel interface
        String fbApplicationId = FacebookSDKJSInterface.getApplicationId(application.getApplicationContext());
        AdjustFactory.getLogger().info("AdjustBridgeInstance fbApplicationId: %s", fbApplicationId);

        if (fbApplicationId == null) {
            return;
        }

        this.facebookSDKJSInterface = new FacebookSDKJSInterface();

        // Add FB pixel to JS interface.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            this.webView.addJavascriptInterface(facebookSDKJSInterface,
                                                FB_JAVASCRIPT_INTERFACE_NAME_PREFIX
                                                + fbApplicationId
                                               );
        }
    }

    @JavascriptInterface
    public void onCreate(String adjustConfigString) {
        // Initialise SDK only if it's not already initialised.
        if (isInitialized) {
            AdjustBridgeUtil.getLogger().warn("Adjust bridge is already initialized. Ignoring further attempts");
            return;
        }
        if (!isInitialized()) {
            return;
        }

        try {
            AdjustBridgeUtil.getLogger().verbose("Web bridge onCreate adjustConfigString: " + adjustConfigString);

            JSONObject jsonAdjustConfig = new JSONObject(adjustConfigString);
            Object appTokenField = jsonAdjustConfig.get("appToken");
            Object environmentField = jsonAdjustConfig.get("environment");
            Object allowSuppressLogLevelField = jsonAdjustConfig.get("allowSuppressLogLevel");
            Object eventBufferingEnabledField = jsonAdjustConfig.get("eventBufferingEnabled");
            Object sendInBackgroundField = jsonAdjustConfig.get("sendInBackground");
            Object logLevelField = jsonAdjustConfig.get("logLevel");
            Object sdkPrefixField = jsonAdjustConfig.get("sdkPrefix");
            Object processNameField = jsonAdjustConfig.get("processName");
            Object defaultTrackerField = jsonAdjustConfig.get("defaultTracker");
            Object externalDeviceIdField = jsonAdjustConfig.get("externalDeviceId");
            Object attributionCallbackNameField = jsonAdjustConfig.get("attributionCallbackName");
            Object deviceKnownField = jsonAdjustConfig.get("deviceKnown");
            Object needsCostField = jsonAdjustConfig.get("needsCost");
            Object eventSuccessCallbackNameField = jsonAdjustConfig.get("eventSuccessCallbackName");
            Object eventFailureCallbackNameField = jsonAdjustConfig.get("eventFailureCallbackName");
            Object sessionSuccessCallbackNameField = jsonAdjustConfig.get("sessionSuccessCallbackName");
            Object sessionFailureCallbackNameField = jsonAdjustConfig.get("sessionFailureCallbackName");
            Object openDeferredDeeplinkField = jsonAdjustConfig.get("openDeferredDeeplink");
            Object deferredDeeplinkCallbackNameField = jsonAdjustConfig.get("deferredDeeplinkCallbackName");
            Object delayStartField = jsonAdjustConfig.get("delayStart");
            Object userAgentField = jsonAdjustConfig.get("userAgent");
            Object secretIdField = jsonAdjustConfig.get("secretId");
            Object info1Field = jsonAdjustConfig.get("info1");
            Object info2Field = jsonAdjustConfig.get("info2");
            Object info3Field = jsonAdjustConfig.get("info3");
            Object info4Field = jsonAdjustConfig.get("info4");
            Object fbPixelDefaultEventTokenField = jsonAdjustConfig.get("fbPixelDefaultEventToken");
            Object fbPixelMappingField = jsonAdjustConfig.get("fbPixelMapping");
            Object urlStrategyField = jsonAdjustConfig.get("urlStrategy");
            Object preinstallTrackingEnabledField = jsonAdjustConfig.get("preinstallTrackingEnabled");
            Object preinstallFilePathField = jsonAdjustConfig.get("preinstallFilePath");
            Object playStoreKidsAppEnabledField = jsonAdjustConfig.get("playStoreKidsAppEnabled");
            Object coppaCompliantEnabledField = jsonAdjustConfig.get("coppaCompliantEnabled");
            Object finalAttributionEnabledField = jsonAdjustConfig.get("finalAttributionEnabled");
            Object fbAppIdField = jsonAdjustConfig.get("fbAppId");
            Object readDeviceInfoOnceEnabledField = jsonAdjustConfig.get("readDeviceInfoOnceEnabled");

            String appToken = AdjustBridgeUtil.fieldToString(appTokenField);
            String environment = AdjustBridgeUtil.fieldToString(environmentField);
            Boolean allowSuppressLogLevel = AdjustBridgeUtil.fieldToBoolean(allowSuppressLogLevelField);

            AdjustConfig adjustConfig;
            if (allowSuppressLogLevel == null) {
                adjustConfig = new AdjustConfig(application.getApplicationContext(), appToken, environment);
            } else {
                adjustConfig = new AdjustConfig(application.getApplicationContext(), appToken, environment, allowSuppressLogLevel.booleanValue());
            }

            if (!adjustConfig.isValid()) {
                return;
            }

            // Event buffering
            Boolean eventBufferingEnabled = AdjustBridgeUtil.fieldToBoolean(eventBufferingEnabledField);
            if (eventBufferingEnabled != null) {
                adjustConfig.setEventBufferingEnabled(eventBufferingEnabled);
            }

            // Send in the background
            Boolean sendInBackground = AdjustBridgeUtil.fieldToBoolean(sendInBackgroundField);
            if (sendInBackground != null) {
                adjustConfig.setSendInBackground(sendInBackground);
            }

            // Log level
            String logLevelString = AdjustBridgeUtil.fieldToString(logLevelField);
            if (logLevelString != null) {
                if (logLevelString.equalsIgnoreCase(LOG_LEVEL_VERBOSE)) {
                    adjustConfig.setLogLevel(LogLevel.VERBOSE);
                } else if (logLevelString.equalsIgnoreCase(LOG_LEVEL_DEBUG)) {
                    adjustConfig.setLogLevel(LogLevel.DEBUG);
                } else if (logLevelString.equalsIgnoreCase(LOG_LEVEL_INFO)) {
                    adjustConfig.setLogLevel(LogLevel.INFO);
                } else if (logLevelString.equalsIgnoreCase(LOG_LEVEL_WARN)) {
                    adjustConfig.setLogLevel(LogLevel.WARN);
                } else if (logLevelString.equalsIgnoreCase(LOG_LEVEL_ERROR)) {
                    adjustConfig.setLogLevel(LogLevel.ERROR);
                } else if (logLevelString.equalsIgnoreCase(LOG_LEVEL_ASSERT)) {
                    adjustConfig.setLogLevel(LogLevel.ASSERT);
                } else if (logLevelString.equalsIgnoreCase(LOG_LEVEL_SUPPRESS)) {
                    adjustConfig.setLogLevel(LogLevel.SUPRESS);
                }
            }

            // SDK prefix
            String sdkPrefix = AdjustBridgeUtil.fieldToString(sdkPrefixField);
            if (sdkPrefix != null) {
                adjustConfig.setSdkPrefix(sdkPrefix);
            }

            // Main process name
            String processName = AdjustBridgeUtil.fieldToString(processNameField);
            if (processName != null) {
                adjustConfig.setProcessName(processName);
            }

            // Default tracker
            String defaultTracker = AdjustBridgeUtil.fieldToString(defaultTrackerField);
            if (defaultTracker != null) {
                adjustConfig.setDefaultTracker(defaultTracker);
            }

            // External device ID
            String externalDeviceId = AdjustBridgeUtil.fieldToString(externalDeviceIdField);
            if (externalDeviceId != null) {
                adjustConfig.setExternalDeviceId(externalDeviceId);
            }

            // Attribution callback name
            final String attributionCallbackName = AdjustBridgeUtil.fieldToString(attributionCallbackNameField);
            if (attributionCallbackName != null) {
                adjustConfig.setOnAttributionChangedListener(new OnAttributionChangedListener() {
                    @Override
                    public void onAttributionChanged(AdjustAttribution attribution) {
                        AdjustBridgeUtil.execAttributionCallbackCommand(webView, attributionCallbackName, attribution);
                    }
                });
            }

            // Is device known
            Boolean deviceKnown = AdjustBridgeUtil.fieldToBoolean(deviceKnownField);
            if (deviceKnown != null) {
                adjustConfig.setDeviceKnown(deviceKnown);
            }

            // Needs cost
            Boolean needsCost = AdjustBridgeUtil.fieldToBoolean(needsCostField);
            if (needsCost != null) {
                adjustConfig.setNeedsCost(needsCost);
            }

            // Event success callback
            final String eventSuccessCallbackName = AdjustBridgeUtil.fieldToString(eventSuccessCallbackNameField);
            if (eventSuccessCallbackName != null) {
                adjustConfig.setOnEventTrackingSucceededListener(new OnEventTrackingSucceededListener() {
                    public void onFinishedEventTrackingSucceeded(AdjustEventSuccess eventSuccessResponseData) {
                        AdjustBridgeUtil.execEventSuccessCallbackCommand(webView, eventSuccessCallbackName, eventSuccessResponseData);
                    }
                });
            }

            // Event failure callback
            final String eventFailureCallbackName = AdjustBridgeUtil.fieldToString(eventFailureCallbackNameField);
            if (eventFailureCallbackName != null) {
                adjustConfig.setOnEventTrackingFailedListener(new OnEventTrackingFailedListener() {
                    public void onFinishedEventTrackingFailed(AdjustEventFailure eventFailureResponseData) {
                        AdjustBridgeUtil.execEventFailureCallbackCommand(webView, eventFailureCallbackName, eventFailureResponseData);
                    }
                });
            }

            // Session success callback
            final String sessionSuccessCallbackName = AdjustBridgeUtil.fieldToString(sessionSuccessCallbackNameField);
            if (sessionSuccessCallbackName != null) {
                adjustConfig.setOnSessionTrackingSucceededListener(new OnSessionTrackingSucceededListener() {
                    @Override
                    public void onFinishedSessionTrackingSucceeded(AdjustSessionSuccess sessionSuccessResponseData) {
                        AdjustBridgeUtil.execSessionSuccessCallbackCommand(webView, sessionSuccessCallbackName, sessionSuccessResponseData);
                    }
                });
            }

            // Session failure callback
            final String sessionFailureCallbackName = AdjustBridgeUtil.fieldToString(sessionFailureCallbackNameField);
            if (sessionFailureCallbackName != null) {
                adjustConfig.setOnSessionTrackingFailedListener(new OnSessionTrackingFailedListener() {
                    @Override
                    public void onFinishedSessionTrackingFailed(AdjustSessionFailure failureResponseData) {
                        AdjustBridgeUtil.execSessionFailureCallbackCommand(webView, sessionFailureCallbackName, failureResponseData);
                    }
                });
            }

            // Should deferred deep link be opened?
            Boolean openDeferredDeeplink = AdjustBridgeUtil.fieldToBoolean(openDeferredDeeplinkField);
            if (openDeferredDeeplink != null) {
                shouldDeferredDeeplinkBeLaunched = openDeferredDeeplink;
            }

            // Deferred deeplink callback
            final String deferredDeeplinkCallbackName = AdjustBridgeUtil.fieldToString(deferredDeeplinkCallbackNameField);
            if (deferredDeeplinkCallbackName != null) {
                adjustConfig.setOnDeeplinkResponseListener(new OnDeeplinkResponseListener() {
                    @Override
                    public boolean launchReceivedDeeplink(Uri deeplink) {
                        AdjustBridgeUtil.execSingleValueCallback(webView, deferredDeeplinkCallbackName, deeplink.toString());
                        return shouldDeferredDeeplinkBeLaunched;
                    }
                });
            }

            // Delay start
            Double delayStart = AdjustBridgeUtil.fieldToDouble(delayStartField);
            if (delayStart != null) {
                adjustConfig.setDelayStart(delayStart);
            }

            // User agent
            String userAgent = AdjustBridgeUtil.fieldToString(userAgentField);
            if (userAgent != null) {
                adjustConfig.setUserAgent(userAgent);
            }

            // App secret
            Long secretId = AdjustBridgeUtil.fieldToLong(secretIdField);
            Long info1 = AdjustBridgeUtil.fieldToLong(info1Field);
            Long info2 = AdjustBridgeUtil.fieldToLong(info2Field);
            Long info3 = AdjustBridgeUtil.fieldToLong(info3Field);
            Long info4 = AdjustBridgeUtil.fieldToLong(info4Field);
            if (secretId != null && info1 != null && info2 != null && info3 != null && info4 != null) {
                adjustConfig.setAppSecret(secretId, info1, info2, info3, info4);
            }

            // Check Pixel Default Event Token
            String fbPixelDefaultEventToken = AdjustBridgeUtil.fieldToString(fbPixelDefaultEventTokenField);
            if (fbPixelDefaultEventToken != null && this.facebookSDKJSInterface != null) {
                this.facebookSDKJSInterface.setDefaultEventToken(fbPixelDefaultEventToken);
            }

            // Add Pixel mappings
            try {
                String[] fbPixelMapping = AdjustBridgeUtil.jsonArrayToArray((JSONArray)fbPixelMappingField);
                if (fbPixelMapping != null && this.facebookSDKJSInterface != null) {
                    for (int i = 0; i < fbPixelMapping.length; i += 2) {
                        String key = fbPixelMapping[i];
                        String value = fbPixelMapping[i+1];
                        this.facebookSDKJSInterface.addFbPixelEventTokenMapping(key, value);
                    }
                }
            } catch (Exception e) {
                AdjustFactory.getLogger().error("AdjustBridgeInstance.configureFbPixel: %s", e.getMessage());
            }

            // Set url strategy
            String urlStrategy = AdjustBridgeUtil.fieldToString(urlStrategyField);
            if (urlStrategy != null) {
                adjustConfig.setUrlStrategy(urlStrategy);
            }

            // Preinstall tracking
            Boolean preinstallTrackingEnabled = AdjustBridgeUtil.fieldToBoolean(preinstallTrackingEnabledField);
            if (preinstallTrackingEnabled != null) {
                adjustConfig.setPreinstallTrackingEnabled(preinstallTrackingEnabled);
            }

            // Preinstall secondary file path
            String preinstallFilePath = AdjustBridgeUtil.fieldToString(preinstallFilePathField);
            if (preinstallFilePath != null) {
                adjustConfig.setPreinstallFilePath(preinstallFilePath);
            }

            // PlayStore Kids app
            Boolean playStoreKidsAppEnabled = AdjustBridgeUtil.fieldToBoolean(playStoreKidsAppEnabledField);
            if (playStoreKidsAppEnabled != null) {
                adjustConfig.setPlayStoreKidsAppEnabled(playStoreKidsAppEnabled);
            }

            // Coppa compliant
            Boolean coppaCompliantEnabled = AdjustBridgeUtil.fieldToBoolean(coppaCompliantEnabledField);
            if (coppaCompliantEnabled != null) {
                adjustConfig.setCoppaCompliantEnabled(coppaCompliantEnabled);
            }

            // Final attribution config
            Boolean finalAttributionEnabled = AdjustBridgeUtil.fieldToBoolean(finalAttributionEnabledField);
            if (finalAttributionEnabled != null) {
                adjustConfig.setFinalAttributionEnabled(finalAttributionEnabled);
            }

            // FB App ID
            String fbAppId = AdjustBridgeUtil.fieldToString(fbAppIdField);
            if (fbAppId != null) {
                adjustConfig.setFbAppId(fbAppId);
            }

            // read device info once
            Boolean readDeviceInfoOnceEnabled = AdjustBridgeUtil.fieldToBoolean(readDeviceInfoOnceEnabledField);
            if (readDeviceInfoOnceEnabled != null) {
                adjustConfig.setReadDeviceInfoOnceEnabled(readDeviceInfoOnceEnabled);
            }

            // Manually call onResume() because web view initialisation will happen a bit delayed.
            // With this delay, it will miss lifecycle callback onResume() initial firing.
            Adjust.onCreate(adjustConfig);
            Adjust.onResume();

            isInitialized = true;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                application.registerActivityLifecycleCallbacks(new AdjustLifecycleCallbacks());
            }
        } catch (Exception e) {
            AdjustFactory.getLogger().error("AdjustBridgeInstance onCreate: %s", e.getMessage());
        }
    }

    @JavascriptInterface
    public void trackEvent(String adjustEventString) {
        if (!isInitialized()) {
            return;
        }

        try {
            JSONObject jsonAdjustEvent = new JSONObject(adjustEventString);

            Object eventTokenField = jsonAdjustEvent.get("eventToken");
            Object revenueField = jsonAdjustEvent.get("revenue");
            Object currencyField = jsonAdjustEvent.get("currency");
            Object callbackParametersField = jsonAdjustEvent.get("callbackParameters");
            Object partnerParametersField = jsonAdjustEvent.get("partnerParameters");
            Object orderIdField = jsonAdjustEvent.get("orderId");
            Object callbackIdField = jsonAdjustEvent.get("callbackId");

            String eventToken = AdjustBridgeUtil.fieldToString(eventTokenField);
            AdjustEvent adjustEvent = new AdjustEvent(eventToken);

            if (!adjustEvent.isValid()) {
                return;
            }

            // Revenue
            Double revenue = AdjustBridgeUtil.fieldToDouble(revenueField);
            String currency = AdjustBridgeUtil.fieldToString(currencyField);
            if (revenue != null && currency != null) {
                adjustEvent.setRevenue(revenue, currency);
            }

            // Callback parameters
            String[] callbackParameters = AdjustBridgeUtil.jsonArrayToArray((JSONArray)callbackParametersField);
            if (callbackParameters != null) {
                for (int i = 0; i < callbackParameters.length; i += 2) {
                    String key = callbackParameters[i];
                    String value = callbackParameters[i+1];
                    adjustEvent.addCallbackParameter(key, value);
                }
            }

            // Partner parameters
            String[] partnerParameters = AdjustBridgeUtil.jsonArrayToArray((JSONArray)partnerParametersField);
            if (partnerParameters != null) {
                for (int i = 0; i < partnerParameters.length; i += 2) {
                    String key = partnerParameters[i];
                    String value = partnerParameters[i+1];
                    adjustEvent.addPartnerParameter(key, value);
                }
            }

            // Revenue deduplication
            String orderId = AdjustBridgeUtil.fieldToString(orderIdField);
            if (orderId != null) {
                adjustEvent.setOrderId(orderId);
            }

            // Callback id
            String callbackId = AdjustBridgeUtil.fieldToString(callbackIdField);
            if (callbackId != null) {
                adjustEvent.setCallbackId(callbackId);
            }

            // Track event
            Adjust.trackEvent(adjustEvent);
        } catch (Exception e) {
            AdjustFactory.getLogger().error("AdjustBridgeInstance trackEvent: %s", e.getMessage());
        }
    }

    @JavascriptInterface
    public void trackAdRevenue(final String source, final String payload) {
        try {
            // payload JSON string is URL encoded
            String decodedPayload = URLDecoder.decode(payload, "UTF-8");
            JSONObject jsonPayload = new JSONObject(decodedPayload);
            Adjust.trackAdRevenue(source, jsonPayload);
        } catch (JSONException je) {
            AdjustFactory.getLogger().debug("Ad revenue payload does not seem to be a valid JSON string");
        } catch (UnsupportedEncodingException ue) {
            AdjustFactory.getLogger().debug("Unable to URL decode given JSON string");
        }
    }

    @JavascriptInterface
    public void onResume() {
        if (!isInitialized()) {
            return;
        }
        Adjust.onResume();
    }

    @JavascriptInterface
    public void onPause() {
        if (!isInitialized()) {
            return;
        }
        Adjust.onPause();
    }

    @JavascriptInterface
    public void setEnabled(String isEnabledString) {
        if (!isInitialized()) {
            return;
        }
        Boolean isEnabled = AdjustBridgeUtil.fieldToBoolean(isEnabledString);
        if (isEnabled != null) {
            Adjust.setEnabled(isEnabled);
        }
    }

    @JavascriptInterface
    public void isEnabled(String callback) {
        if (!isInitialized()) {
            return;
        }
        boolean isEnabled = Adjust.isEnabled();
        AdjustBridgeUtil.execSingleValueCallback(webView, callback, String.valueOf(isEnabled));
    }

    @JavascriptInterface
    public boolean isEnabled() {
        if (!isInitialized()) {
            return false;
        }
        return Adjust.isEnabled();
    }

    @JavascriptInterface
    public void appWillOpenUrl(String deeplinkString) {
        if (!isInitialized()) {
            return;
        }
        Uri deeplink = null;
        if (deeplinkString != null) {
            deeplink = Uri.parse(deeplinkString);
        }
        Adjust.appWillOpenUrl(deeplink, application.getApplicationContext());
    }

    @JavascriptInterface
    public void setReferrer(String referrer) {
        if (!isInitialized()) {
            return;
        }
        Adjust.setReferrer(referrer, application.getApplicationContext());
    }

    @JavascriptInterface
    public void setOfflineMode(String isOfflineString) {
        if (!isInitialized()) {
            return;
        }
        Boolean isOffline = AdjustBridgeUtil.fieldToBoolean(isOfflineString);
        if (isOffline != null) {
            Adjust.setOfflineMode(isOffline);
        }
    }

    @JavascriptInterface
    public void sendFirstPackages() {
        if (!isInitialized()) {
            return;
        }
        Adjust.sendFirstPackages();
    }

    @JavascriptInterface
    public void addSessionCallbackParameter(String key, String value) {
        if (!isInitialized()) {
            return;
        }
        Adjust.addSessionCallbackParameter(key, value);
    }

    @JavascriptInterface
    public void addSessionPartnerParameter(String key, String value) {
        if (!isInitialized()) {
            return;
        }
        Adjust.addSessionPartnerParameter(key, value);
    }

    @JavascriptInterface
    public void removeSessionCallbackParameter(String key) {
        if (!isInitialized()) {
            return;
        }
        Adjust.removeSessionCallbackParameter(key);
    }

    @JavascriptInterface
    public void removeSessionPartnerParameter(String key) {
        if (!isInitialized()) {
            return;
        }
        Adjust.removeSessionPartnerParameter(key);
    }

    @JavascriptInterface
    public void resetSessionCallbackParameters() {
        if (!isInitialized()) {
            return;
        }
        Adjust.resetSessionCallbackParameters();
    }

    @JavascriptInterface
    public void resetSessionPartnerParameters() {
        if (!isInitialized()) {
            return;
        }
        Adjust.resetSessionPartnerParameters();
    }

    @JavascriptInterface
    public void setPushToken(String pushToken) {
        if (!isInitialized()) {
            return;
        }

        Adjust.setPushToken(pushToken, application.getApplicationContext());
    }

    @JavascriptInterface
    public void gdprForgetMe() {
        if (!isInitialized()) {
            return;
        }
        Adjust.gdprForgetMe(application.getApplicationContext());
    }

    @JavascriptInterface
    public void disableThirdPartySharing() {
        if (!isInitialized()) {
            return;
        }
        Adjust.disableThirdPartySharing(application.getApplicationContext());
    }

    @JavascriptInterface
    public void trackThirdPartySharing(String adjustThirdPartySharingString) {
        if (!isInitialized()) {
            return;
        }

        try {
            JSONObject jsonAdjustThirdPartySharing = new JSONObject(adjustThirdPartySharingString);

            Object isEnabledField =
                    jsonAdjustThirdPartySharing.get("isEnabled");
            Object granularOptionsField = jsonAdjustThirdPartySharing.get("granularOptions");
            Object partnerSharingSettingsField = jsonAdjustThirdPartySharing.get("partnerSharingSettings");

            Boolean isEnabled = AdjustBridgeUtil.fieldToBoolean(isEnabledField);

            AdjustThirdPartySharing adjustThirdPartySharing =
                    new AdjustThirdPartySharing(isEnabled);

            // Granular options
            String[] granularOptions =
                    AdjustBridgeUtil.jsonArrayToArray((JSONArray)granularOptionsField);
            if (granularOptions != null) {
                for (int i = 0; i < granularOptions.length; i += 3) {
                    String partnerName = granularOptions[i];
                    String key = granularOptions[i + 1];
                    String value = granularOptions[i + 2];
                    adjustThirdPartySharing.addGranularOption(partnerName, key, value);
                }
            }

            // Partner sharing settings
            String[] partnerSharingSettings =
                    AdjustBridgeUtil.jsonArrayToArray((JSONArray)partnerSharingSettingsField);
            if (partnerSharingSettings != null) {
                for (int i = 0; i < partnerSharingSettings.length; i += 3) {
                    String partnerName = partnerSharingSettings[i];
                    String key = partnerSharingSettings[i + 1];
                    Boolean value = AdjustBridgeUtil.fieldToBoolean(partnerSharingSettings[i + 2]);
                    if (value != null) {
                        adjustThirdPartySharing.addPartnerSharingSetting(partnerName, key, value);
                    } else {
                        AdjustFactory.getLogger().error("Cannot add partner sharing setting with non boolean value");
                    }
                }
            }

            // Track ThirdPartySharing
            Adjust.trackThirdPartySharing(adjustThirdPartySharing);
        } catch (Exception e) {
            AdjustFactory.getLogger().error(
                    "AdjustBridgeInstance trackThirdPartySharing: %s", e.getMessage());
        }
    }

    @JavascriptInterface
    public void trackMeasurementConsent(String consentMeasurementString) {
        if (!isInitialized()) {
            return;
        }
        Boolean consentMeasurement = AdjustBridgeUtil.fieldToBoolean(consentMeasurementString);
        if (consentMeasurement != null) {
            Adjust.trackMeasurementConsent(consentMeasurement);
        }
    }

    @JavascriptInterface
    public void getGoogleAdId(final String callback) {
        if (!isInitialized()) {
            return;
        }
        Adjust.getGoogleAdId(application.getApplicationContext(), new OnDeviceIdsRead() {
            @Override
            public void onGoogleAdIdRead(String googleAdId) {
                AdjustBridgeUtil.execSingleValueCallback(webView, callback, googleAdId);
            }
        });
    }

    @JavascriptInterface
    public String getAmazonAdId() {
        if (!isInitialized()) {
            return null;
        }
        return Adjust.getAmazonAdId(application.getApplicationContext());
    }

    @JavascriptInterface
    public String getAdid() {
        if (!isInitialized()) {
            return null;
        }
        return Adjust.getAdid();
    }

    @JavascriptInterface
    public void getAttribution(final String callback) {
        if (!isInitialized()) {
            return;
        }
        AdjustAttribution attribution = Adjust.getAttribution();
        AdjustBridgeUtil.execAttributionCallbackCommand(webView, callback, attribution);
    }

    @JavascriptInterface
    public String getSdkVersion() {
        return Adjust.getSdkVersion();
    }

    @JavascriptInterface
    public void fbPixelEvent(String pixelId, String event_name, String jsonString) {
        this.facebookSDKJSInterface.sendEvent(pixelId, event_name, jsonString);
    }

    @JavascriptInterface
    public void teardown() {
        isInitialized = false;
        shouldDeferredDeeplinkBeLaunched = true;
    }

    public void setWebView(WebView webView) {
        this.webView = webView;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            webView.addJavascriptInterface(this, JAVASCRIPT_INTERFACE_NAME);
        }
    }

    public void setApplicationContext(Application application) {
        this.application = application;
    }

    public void unregister() {
        if (!isInitialized()) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            this.webView.removeJavascriptInterface(JAVASCRIPT_INTERFACE_NAME);
        }

        unregisterFacebookSDKJSInterface();

        application = null;
        webView = null;
        isInitialized = false;
    }

    public void unregisterFacebookSDKJSInterface() {
        if (!isInitialized()) {
            return;
        }

        if (this.facebookSDKJSInterface == null) {
            return;
        }

        String fbApplicationId = FacebookSDKJSInterface.getApplicationId(application.getApplicationContext());
        if (fbApplicationId == null) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            this.webView.removeJavascriptInterface(FB_JAVASCRIPT_INTERFACE_NAME_PREFIX + fbApplicationId);
        }

        this.facebookSDKJSInterface = null;
    }
}
