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
import com.adjust.sdk.AdjustThirdPartySharing;
import com.adjust.sdk.LogLevel;
import com.adjust.sdk.OnAdidReadListener;
import com.adjust.sdk.OnAmazonAdIdReadListener;
import com.adjust.sdk.OnAttributionChangedListener;
import com.adjust.sdk.OnAttributionReadListener;
import com.adjust.sdk.OnDeeplinkResponseListener;
import com.adjust.sdk.OnDeviceIdsRead;
import com.adjust.sdk.OnEventTrackingFailedListener;
import com.adjust.sdk.OnEventTrackingSucceededListener;
import com.adjust.sdk.OnSdkVersionReadListener;
import com.adjust.sdk.OnIsEnabledListener;
import com.adjust.sdk.OnSessionTrackingFailedListener;
import com.adjust.sdk.OnSessionTrackingSucceededListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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

    private String adjustSdkPrefix = null;

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
    public void initSdk(String adjustConfigString) {
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
            Object sendInBackgroundField = jsonAdjustConfig.get("sendInBackground");
            Object logLevelField = jsonAdjustConfig.get("logLevel");
            Object sdkPrefixField = jsonAdjustConfig.get("sdkPrefix");
            Object processNameField = jsonAdjustConfig.get("processName");
            Object defaultTrackerField = jsonAdjustConfig.get("defaultTracker");
            Object externalDeviceIdField = jsonAdjustConfig.get("externalDeviceId");
            Object attributionCallbackNameField = jsonAdjustConfig.get("attributionCallbackName");
            Object needsCostField = jsonAdjustConfig.get("needsCost");
            Object eventSuccessCallbackNameField = jsonAdjustConfig.get("eventSuccessCallbackName");
            Object eventFailureCallbackNameField = jsonAdjustConfig.get("eventFailureCallbackName");
            Object sessionSuccessCallbackNameField = jsonAdjustConfig.get("sessionSuccessCallbackName");
            Object sessionFailureCallbackNameField = jsonAdjustConfig.get("sessionFailureCallbackName");
            Object openDeferredDeeplinkField = jsonAdjustConfig.get("openDeferredDeeplink");
            Object deferredDeeplinkCallbackNameField = jsonAdjustConfig.get("deferredDeeplinkCallbackName");
            Object fbPixelDefaultEventTokenField = jsonAdjustConfig.get("fbPixelDefaultEventToken");
            Object fbPixelMappingField = jsonAdjustConfig.get("fbPixelMapping");
            Object urlStrategyField = jsonAdjustConfig.get("urlStrategy");
            Object useSubDomainField = jsonAdjustConfig.get("useSubDomain");
            Object isDataResidencyField = jsonAdjustConfig.get("isDataResidency");
            Object preinstallTrackingEnabledField = jsonAdjustConfig.get("preinstallTrackingEnabled");
            Object preinstallFilePathField = jsonAdjustConfig.get("preinstallFilePath");
            Object fbAppIdField = jsonAdjustConfig.get("fbAppId");
            Object shouldReadDeviceIdsOnceField = jsonAdjustConfig.get("shouldReadDeviceIdsOnce");
            Object eventDeduplicationIdsMaxSizeField = jsonAdjustConfig.get("eventDeduplicationIdsMaxSize");

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
                adjustSdkPrefix = sdkPrefix;
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
            String[] urlStrategyArray = AdjustBridgeUtil.jsonArrayToArray((JSONArray) urlStrategyField);
            List<String> urlStrategy = Arrays.asList(urlStrategyArray);

            Boolean useSubDomain = AdjustBridgeUtil.fieldToBoolean(useSubDomainField);
            Boolean isDataResidency = AdjustBridgeUtil.fieldToBoolean(isDataResidencyField);
            if (urlStrategy != null && !urlStrategy.isEmpty() && useSubDomain != null && isDataResidency != null) {
                adjustConfig.setUrlStrategy(urlStrategy,useSubDomain,isDataResidency);
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

            // FB App ID
            String fbAppId = AdjustBridgeUtil.fieldToString(fbAppIdField);
            if (fbAppId != null) {
                adjustConfig.setFbAppId(fbAppId);
            }

            // read device info once
            Boolean shouldReadDeviceIdsOnce = AdjustBridgeUtil.fieldToBoolean(shouldReadDeviceIdsOnceField);
            if (shouldReadDeviceIdsOnce != null && shouldReadDeviceIdsOnce.booleanValue()) {
                adjustConfig.readDeviceIdsOnce();
            }

            Integer eventDeduplicationIdsMaxSize = AdjustBridgeUtil.fieldToInteger(eventDeduplicationIdsMaxSizeField);
            if (eventDeduplicationIdsMaxSize != null) {
                adjustConfig.setEventDeduplicationIdsMaxSize(eventDeduplicationIdsMaxSize);
            }

            // Manually call onResume() because web view initialisation will happen a bit delayed.
            // With this delay, it will miss lifecycle callback onResume() initial firing.
            Adjust.initSdk(adjustConfig);
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
            Object deduplicationIdField = jsonAdjustEvent.get("deduplicationId");
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
            String deduplicationId = AdjustBridgeUtil.fieldToString(deduplicationIdField);
            if (deduplicationId != null) {
                adjustEvent.setDeduplicationId(deduplicationId);
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
    public void enable() {
        if (!isInitialized()) {
            return;
        }
        Adjust.enable();
    }

    @JavascriptInterface
    public void disable() {
        if (!isInitialized()) {
            return;
        }
        Adjust.disable();
    }

    @JavascriptInterface
    public void isEnabled(String callback) {
        if (!isInitialized()) {
            return;
        }
        Adjust.isEnabled(application.getApplicationContext(),new OnIsEnabledListener() {
            @Override
            public void onIsEnabledRead(boolean isEnabled) {
                AdjustBridgeUtil.execSingleValueCallback(webView, callback, String.valueOf(isEnabled));
            }
        });
    }

    @JavascriptInterface
    public void setReferrer(String referrer) {
        if (!isInitialized()) {
            return;
        }
        Adjust.setReferrer(referrer, application.getApplicationContext());
    }

    @JavascriptInterface
    public void switchToOfflineMode() {
        if (!isInitialized()) {
            return;
        }
        Adjust.switchToOfflineMode();
    }

    @JavascriptInterface
    public void switchBackToOnlineMode() {
        if (!isInitialized()) {
            return;
        }
        Adjust.switchBackToOnlineMode();
    }

    @JavascriptInterface
    public void addGlobalCallbackParameter(String key, String value) {
        if (!isInitialized()) {
            return;
        }
        Adjust.addGlobalCallbackParameter(key, value);
    }

    @JavascriptInterface
    public void addGlobalPartnerParameter(String key, String value) {
        if (!isInitialized()) {
            return;
        }
        Adjust.addGlobalPartnerParameter(key, value);
    }

    @JavascriptInterface
    public void removeGlobalCallbackParameter(String key) {
        if (!isInitialized()) {
            return;
        }
        Adjust.removeGlobalCallbackParameter(key);
    }

    @JavascriptInterface
    public void removeGlobalPartnerParameter(String key) {
        if (!isInitialized()) {
            return;
        }
        Adjust.removeGlobalPartnerParameter(key);
    }

    @JavascriptInterface
    public void removeGlobalCallbackParameters() {
        if (!isInitialized()) {
            return;
        }
        Adjust.removeGlobalCallbackParameters();
    }

    @JavascriptInterface
    public void removeGlobalPartnerParameters() {
        if (!isInitialized()) {
            return;
        }
        Adjust.removeGlobalPartnerParameters();
    }

    @JavascriptInterface
    public void gdprForgetMe() {
        if (!isInitialized()) {
            return;
        }
        Adjust.gdprForgetMe(application.getApplicationContext());
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
    public void enableCoppaCompliance() {
        if (!isInitialized()) {
            return;
        }

        Adjust.enableCoppaCompliance(application.getApplicationContext());
    }

    @JavascriptInterface
    public void disableCoppaCompliance() {
        if (!isInitialized()) {
            return;
        }

        Adjust.disableCoppaCompliance(application.getApplicationContext());
    }

    @JavascriptInterface
    public void enablePlayStoreKidsApp() {
        if (!isInitialized()) {
            return;
        }

        Adjust.enablePlayStoreKidsApp(application.getApplicationContext());
    }

    @JavascriptInterface
    public void disablePlayStoreKidsApp() {
        if (!isInitialized()) {
            return;
        }

        Adjust.disablePlayStoreKidsApp(application.getApplicationContext());
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
    public void getAmazonAdId(final String callbackSuccess,final String callbackFail) {
        if (!isInitialized()) {
            return;
        }
        Adjust.getAmazonAdId(application.getApplicationContext(),new OnAmazonAdIdReadListener() {
            @Override
            public void onAmazonAdIdRead(String adid) {
                AdjustBridgeUtil.execSingleValueCallback(webView, callbackSuccess, adid);
            }

            @Override
            public void onFail(String message) {
                AdjustBridgeUtil.execSingleValueCallback(webView, callbackFail, null);
            }
        });
    }

    @JavascriptInterface
    public void getAdid(final String callback) {
        if (!isInitialized()) {
            return;
        }
        Adjust.getAdid(new OnAdidReadListener() {
            @Override
            public void onAdidRead(String adid) {
                AdjustBridgeUtil.execSingleValueCallback(webView, callback, adid);
            }
        });
    }

    @JavascriptInterface
    public void getAttribution(final String callback) {
        if (!isInitialized()) {
            return;
        }
        Adjust.getAttribution(new OnAttributionReadListener() {
            @Override
            public void onAttributionRead(AdjustAttribution attribution) {
                AdjustBridgeUtil.execAttributionCallbackCommand(webView, callback, attribution);
            }

        });
    }

    @JavascriptInterface
    public void getSdkVersion(final String callback) {
        if (!isInitialized()) {
            return;
        }
        Adjust.getSdkVersion(new OnSdkVersionReadListener() {
            @Override
            public void onSdkVersionRead(String sdkVersion) {
                if (adjustSdkPrefix != null && adjustSdkPrefix.length() > 0) {
                    sdkVersion = adjustSdkPrefix + "@" + sdkVersion;
                }
                AdjustBridgeUtil.execSingleValueCallback(webView, callback, sdkVersion);
            }
        });
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
