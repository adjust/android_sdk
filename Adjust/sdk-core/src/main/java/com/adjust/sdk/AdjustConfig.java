package com.adjust.sdk;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pfms on 06/11/14.
 */
public class AdjustConfig {
    String basePath;
    String gdprPath;
    String subscriptionPath;
    String purchaseVerificationPath;
    Context context;
    String appToken;
    String environment;
    String processName;
    String sdkPrefix;
    String defaultTracker;
    OnAttributionChangedListener onAttributionChangedListener;
    OnEventTrackingSucceededListener onEventTrackingSucceededListener;
    OnEventTrackingFailedListener onEventTrackingFailedListener;
    OnSessionTrackingSucceededListener onSessionTrackingSucceededListener;
    OnSessionTrackingFailedListener onSessionTrackingFailedListener;
    OnDeferredDeeplinkResponseListener onDeferredDeeplinkResponseListener;
    boolean isSendingInBackgroundEnabled;
    AdjustInstance.PreLaunchActions preLaunchActions;
    ILogger logger;
    String pushToken;
    Boolean startEnabled;
    boolean startOffline;
    String externalDeviceId;
    boolean isPreinstallTrackingEnabled;
    Boolean isCostDataInAttributionEnabled;
    List<String> urlStrategyDomains;
    boolean useSubdomains;
    boolean isDataResidency;
    String preinstallFilePath;
    boolean coppaComplianceEnabled;
    boolean playStoreKidsComplianceEnabled;
    String fbAppId;
    boolean isDeviceIdsReadingOnceEnabled;
    OnDeeplinkResolvedListener cachedDeeplinkResolutionCallback;
    ArrayList<OnAdidReadListener> cachedAdidReadCallbacks = new ArrayList<>();
    Integer eventDeduplicationIdsMaxSize;
    ArrayList<OnAttributionReadListener> cachedAttributionReadCallbacks = new ArrayList<>();

    public static final String ENVIRONMENT_SANDBOX = "sandbox";
    public static final String ENVIRONMENT_PRODUCTION = "production";

    public AdjustConfig(Context context, String appToken, String environment) {
        init(context, appToken, environment, false);
    }

    public AdjustConfig(Context context, String appToken, String environment, boolean allowSuppressLogLevel) {
        init(context, appToken, environment, allowSuppressLogLevel);
    }

    // Beware that some of these values might be null. isValid() would check their validity later.
    private void init(Context context, String appToken, String environment, boolean allowSuppressLogLevel) {
        logger = AdjustFactory.getLogger();
        // default values
        if (allowSuppressLogLevel && AdjustConfig.ENVIRONMENT_PRODUCTION.equals(environment)) {
            setLogLevel(LogLevel.SUPPRESS, environment);
        } else {
            setLogLevel(LogLevel.INFO, environment);
        }

        // Always use application context
        if (context != null) {
            context = context.getApplicationContext();
        }

        this.context = context;
        this.appToken = appToken;
        this.environment = environment;

        // default values
        this.isSendingInBackgroundEnabled = false;
        this.isPreinstallTrackingEnabled = false;
        this.isDeviceIdsReadingOnceEnabled = false;
        this.coppaComplianceEnabled = false;
        this.playStoreKidsComplianceEnabled = false;
    }

    public void setLogLevel(LogLevel logLevel) {
        setLogLevel(logLevel, environment);
    }

    private void setLogLevel(LogLevel logLevel, String environment) {
        logger.setLogLevel(logLevel, AdjustConfig.ENVIRONMENT_PRODUCTION.equals(environment));
    }

    public void setSdkPrefix(String sdkPrefix) {
        this.sdkPrefix = sdkPrefix;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public void setDefaultTracker(String defaultTracker) {
        this.defaultTracker = defaultTracker;
    }

    public void setExternalDeviceId(String externalDeviceId) {
        this.externalDeviceId = externalDeviceId;
    }

    public void setPreinstallFilePath(String preinstallFilePath) {
        this.preinstallFilePath = preinstallFilePath;
    }

    public void enableCoppaCompliance() {
        this.coppaComplianceEnabled = true;
    }

    public void enablePlayStoreKidsCompliance() {
        this.playStoreKidsComplianceEnabled = true;
    }

    public void setFbAppId(String fbAppId) {
        this.fbAppId = fbAppId;
    }

    public void setEventDeduplicationIdsMaxSize(Integer eventDeduplicationIdsMaxSize) {
        this.eventDeduplicationIdsMaxSize = eventDeduplicationIdsMaxSize;
    }

    public void setUrlStrategy(List<String> domains, boolean useSubdomains, boolean isDataResidency) {
        if (domains == null || domains.isEmpty()) {
            logger.error("Invalid URL strategy domains array");
            return;
        }
        this.urlStrategyDomains = domains;
        this.useSubdomains = useSubdomains;
        this.isDataResidency = isDataResidency;
    }

    public void enablePreinstallTracking() {
        this.isPreinstallTrackingEnabled = true;
    }

    public void enableCostDataInAttribution() {
        this.isCostDataInAttributionEnabled = true;
    }

    public void enableSendingInBackground() {
        this.isSendingInBackgroundEnabled = true;
    }

    public void enableDeviceIdsReadingOnce() {
        this.isDeviceIdsReadingOnceEnabled = true;
    }

    public void setOnAttributionChangedListener(OnAttributionChangedListener onAttributionChangedListener) {
        this.onAttributionChangedListener = onAttributionChangedListener;
    }

    public void setOnEventTrackingSucceededListener(OnEventTrackingSucceededListener onEventTrackingSucceededListener) {
        this.onEventTrackingSucceededListener = onEventTrackingSucceededListener;
    }

    public void setOnEventTrackingFailedListener(OnEventTrackingFailedListener onEventTrackingFailedListener) {
        this.onEventTrackingFailedListener = onEventTrackingFailedListener;
    }

    public void setOnSessionTrackingSucceededListener(OnSessionTrackingSucceededListener onSessionTrackingSucceededListener) {
        this.onSessionTrackingSucceededListener = onSessionTrackingSucceededListener;
    }

    public void setOnSessionTrackingFailedListener(OnSessionTrackingFailedListener onSessionTrackingFailedListener) {
        this.onSessionTrackingFailedListener = onSessionTrackingFailedListener;
    }

    public void setOnDeferredDeeplinkResponseListener(OnDeferredDeeplinkResponseListener onDeferredDeeplinkResponseListener) {
        this.onDeferredDeeplinkResponseListener = onDeferredDeeplinkResponseListener;
    }

    public Context getContext() {
        return context;
    }

    public String getAppToken() {
        return appToken;
    }

    public String getEnvironment() {
        return environment;
    }

    public String getProcessName() {
        return processName;
    }

    public String getSdkPrefix() {
        return sdkPrefix;
    }

    public String getDefaultTracker() {
        return defaultTracker;
    }

    public String getExternalDeviceId() {
        return externalDeviceId;
    }

    public boolean isPreinstallTrackingEnabled() {
        return isPreinstallTrackingEnabled;
    }

    public Boolean getCostDataInAttributionEnabled() {
        return isCostDataInAttributionEnabled;
    }

    public boolean isSendingInBackgroundEnabled() {
        return isSendingInBackgroundEnabled;
    }

    public Integer getEventDeduplicationIdsMaxSize() {
        return eventDeduplicationIdsMaxSize;
    }

    public List<String> getUrlStrategyDomains() {
        return urlStrategyDomains;
    }

    public String getPreinstallFilePath() {
        return preinstallFilePath;
    }

    public boolean isCoppaComplianceEnabled() {
        return coppaComplianceEnabled;
    }

    public boolean isPlayStoreKidsComplianceEnabled() {
        return playStoreKidsComplianceEnabled;
    }

    public String getFbAppId() {
        return fbAppId;
    }

    public boolean isDeviceIdsReadingOnceEnabled() {
        return isDeviceIdsReadingOnceEnabled;
    }

    public OnAttributionChangedListener getOnAttributionChangedListener() {
        return onAttributionChangedListener;
    }

    public OnEventTrackingSucceededListener getOnEventTrackingSucceededListener() {
        return onEventTrackingSucceededListener;
    }

    public OnEventTrackingFailedListener getOnEventTrackingFailedListener() {
        return onEventTrackingFailedListener;
    }

    public OnSessionTrackingSucceededListener getOnSessionTrackingSucceededListener() {
        return onSessionTrackingSucceededListener;
    }

    public OnSessionTrackingFailedListener getOnSessionTrackingFailedListener() {
        return onSessionTrackingFailedListener;
    }

    public OnDeferredDeeplinkResponseListener getOnDeeplinkResponseListener() {
        return onDeferredDeeplinkResponseListener;
    }

    public ILogger getLogger() {
        return logger;
    }

    private boolean checkContext(Context context) {
        if (context == null) {
            logger.error("Missing context");
            return false;
        }

        if (!Util.checkPermission(context, android.Manifest.permission.INTERNET)) {
            logger.error("Missing permission: INTERNET");
            return false;
        }

        return true;
    }

    private boolean checkAppToken(String appToken) {
        if (appToken == null) {
            logger.error("Missing App Token");
            return false;
        }

        if (appToken.length() != 12) {
            logger.error("Malformed App Token '%s'", appToken);
            return false;
        }

        return true;
    }

    private boolean checkEnvironment(String environment) {
        if (environment == null) {
            logger.error("Missing environment");
            return false;
        }

        if (environment.equals(AdjustConfig.ENVIRONMENT_SANDBOX)) {
            logger.warnInProduction("SANDBOX: Adjust is running in Sandbox mode. " +
                    "Use this setting for testing. " +
                    "Don't forget to set the environment to `production` before publishing!");
            return true;
        }
        if (environment.equals(AdjustConfig.ENVIRONMENT_PRODUCTION)) {
            logger.warnInProduction(
                    "PRODUCTION: Adjust is running in Production mode. " +
                            "Use this setting only for the build that you want to publish. " +
                            "Set the environment to `sandbox` if you want to test your app!");
            return true;
        }

        logger.error("Unknown environment '%s'", environment);
        return false;
    }

    public boolean isValid() {
        if (!checkAppToken(appToken)) {
            return false;
        }
        if (!checkEnvironment(environment)) {
            return false;
        }
        if (!checkContext(context)) {
            return false;
        }
        return true;
    }
}