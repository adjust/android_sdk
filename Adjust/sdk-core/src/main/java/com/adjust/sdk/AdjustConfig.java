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
    Class deepLinkComponent;
    OnEventTrackingSucceededListener onEventTrackingSucceededListener;
    OnEventTrackingFailedListener onEventTrackingFailedListener;
    OnSessionTrackingSucceededListener onSessionTrackingSucceededListener;
    OnSessionTrackingFailedListener onSessionTrackingFailedListener;
    OnDeeplinkResponseListener onDeeplinkResponseListener;
    boolean sendInBackground;
    AdjustInstance.PreLaunchActions preLaunchActions;
    ILogger logger;
    String pushToken;
    Boolean startEnabled;
    boolean startOffline;
    String externalDeviceId;
    boolean preinstallTrackingEnabled;
    Boolean needsCost;
    List<String> urlStrategy;
    Boolean useSubdomains;
    Boolean isDataResidency;
    String preinstallFilePath;
    String fbAppId;
    boolean shouldReadDeviceIdsOnce;
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
            setLogLevel(LogLevel.SUPRESS, environment);
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
        this.sendInBackground = false;
        this.preinstallTrackingEnabled = false;
        this.shouldReadDeviceIdsOnce = false;
    }


    public void setSendInBackground(boolean sendInBackground) {
        this.sendInBackground = sendInBackground;
    }

    public void setLogLevel(LogLevel logLevel) {
        setLogLevel(logLevel, environment);
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

    public void setOnAttributionChangedListener(OnAttributionChangedListener onAttributionChangedListener) {
        this.onAttributionChangedListener = onAttributionChangedListener;
    }

    public void setDeepLinkComponent(Class deepLinkComponent) {
        this.deepLinkComponent = deepLinkComponent;
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

    public void setOnDeeplinkResponseListener(OnDeeplinkResponseListener onDeeplinkResponseListener) {
        this.onDeeplinkResponseListener = onDeeplinkResponseListener;
    }

    public void setExternalDeviceId(String externalDeviceId) {
        this.externalDeviceId = externalDeviceId;
    }

    public void setPreinstallTrackingEnabled(boolean preinstallTrackingEnabled) {
        this.preinstallTrackingEnabled = preinstallTrackingEnabled;
    }

    public void setPreinstallFilePath(String preinstallFilePath) {
        this.preinstallFilePath = preinstallFilePath;
    }

    public void setNeedsCost(boolean needsCost) {
        this.needsCost = needsCost;
    }

    public void setFbAppId(String fbAppId) {
        this.fbAppId = fbAppId;
    }

    public boolean isValid() {
        if (!checkAppToken(appToken)) return false;
        if (!checkEnvironment(environment)) return false;
        if (!checkContext(context)) return false;

        return true;
    }

    public void setUrlStrategy(List<String> domains, Boolean useSubdomains ,Boolean isDataResidency) {
        if (domains == null || domains.isEmpty()) {
            logger.error("Invalid url strategy");
            return;
        }
        if (useSubdomains == null) {
            logger.error("useSubdomains cannot be null");
            return;
        }
        if (isDataResidency == null) {
            logger.error("isDataResidency cannot be null");
            return;
        }
        this.urlStrategy = domains;
        this.useSubdomains = useSubdomains;
        this.isDataResidency = isDataResidency;
    }

    public void readDeviceIdsOnce() {
        this.shouldReadDeviceIdsOnce = true;
    }

    public void setEventDeduplicationIdsMaxSize(Integer eventDeduplicationIdsMaxSize) {
        this.eventDeduplicationIdsMaxSize = eventDeduplicationIdsMaxSize;
    }

    public String getBasePath() {
        return basePath;
    }

    public String getGdprPath() {
        return gdprPath;
    }

    public String getSubscriptionPath() {
        return subscriptionPath;
    }

    public String getPurchaseVerificationPath() {
        return purchaseVerificationPath;
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

    public OnAttributionChangedListener getOnAttributionChangedListener() {
        return onAttributionChangedListener;
    }

    public Class getDeepLinkComponent() {
        return deepLinkComponent;
    }

    public Integer getEventDeduplicationIdsMaxSize() {
        return eventDeduplicationIdsMaxSize;
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

    public OnDeeplinkResponseListener getOnDeeplinkResponseListener() {
        return onDeeplinkResponseListener;
    }

    public boolean isSendInBackground() {
        return sendInBackground;
    }

    public AdjustInstance.PreLaunchActions getPreLaunchActions() {
        return preLaunchActions;
    }

    public ILogger getLogger() {
        return logger;
    }

    public String getPushToken() {
        return pushToken;
    }

    public Boolean getStartEnabled() {
        return startEnabled;
    }

    public boolean isStartOffline() {
        return startOffline;
    }

    public String getExternalDeviceId() {
        return externalDeviceId;
    }

    public boolean isPreinstallTrackingEnabled() {
        return preinstallTrackingEnabled;
    }

    public Boolean getNeedsCost() {
        return needsCost;
    }

    public List<String> getUrlStrategy() {
        return urlStrategy;
    }

    public String getPreinstallFilePath() {
        return preinstallFilePath;
    }

    public String getFbAppId() {
        return fbAppId;
    }

    public boolean shouldReadDeviceIdsOnce() {
        return shouldReadDeviceIdsOnce;
    }

    private void setLogLevel(LogLevel logLevel, String environment) {
        logger.setLogLevel(logLevel, AdjustConfig.ENVIRONMENT_PRODUCTION.equals(environment));
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
}