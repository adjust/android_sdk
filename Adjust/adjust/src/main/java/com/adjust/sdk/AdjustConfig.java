package com.adjust.sdk;

import android.content.Context;

import java.util.List;

/**
 * Created by pfms on 06/11/14.
 */
public class AdjustConfig {
    Context context;
    String appToken;
    String environment;
    String processName;
    String sdkPrefix;
    boolean eventBufferingEnabled;
    String defaultTracker;
    OnAttributionChangedListener onAttributionChangedListener;
    String referrer;
    long referrerClickTime;
    Boolean deviceKnown;
    Class deepLinkComponent;
    OnEventTrackingSucceededListener onEventTrackingSucceededListener;
    OnEventTrackingFailedListener onEventTrackingFailedListener;
    OnSessionTrackingSucceededListener onSessionTrackingSucceededListener;
    OnSessionTrackingFailedListener onSessionTrackingFailedListener;
    OnDeeplinkResponseListener onDeeplinkResponseListener;
    boolean sendInBackground;
    Double delayStart;
    List<IRunActivityHandler> sessionParametersActionsArray;
    boolean allowSuppressLogLevel;
    ILogger logger;
    String userAgent;

    public static final String ENVIRONMENT_SANDBOX = "sandbox";
    public static final String ENVIRONMENT_PRODUCTION = "production";

    public AdjustConfig(final Context context, final String appToken, final String environment) {
        init(context, appToken, environment, false);
    }

    public AdjustConfig(final Context context, final String appToken, final String environment, final boolean allowSuppressLogLevel) {
        init(context, appToken, environment, allowSuppressLogLevel);
    }

    private void init(final Context context, final String appToken, final String environment, final boolean allowSuppressLogLevel) {
        this.allowSuppressLogLevel = allowSuppressLogLevel;
        logger = AdjustFactory.getLogger();
        // default values
        setLogLevel(LogLevel.INFO, environment);

        if (!isValid(context, appToken, environment)) {
            return;
        }

        this.context = context.getApplicationContext();
        this.appToken = appToken;
        this.environment = environment;

        // default values
        this.eventBufferingEnabled = false;
        this.sendInBackground = false;
    }

    public final void setEventBufferingEnabled(final Boolean eventBufferingEnabled) {
        if (eventBufferingEnabled == null) {
            this.eventBufferingEnabled = false;
            return;
        }
        this.eventBufferingEnabled = eventBufferingEnabled;
    }

    public final void setSendInBackground(final boolean sendInBackground) {
        this.sendInBackground = sendInBackground;
    }

    public final void setLogLevel(final LogLevel logLevel) {
        setLogLevel(logLevel, environment);
    }

    public final void setSdkPrefix(final String sdkPrefix) {
        this.sdkPrefix = sdkPrefix;
    }

    public final void setProcessName(final String processName) { this.processName = processName; }

    public final void setDefaultTracker(final String defaultTracker) {
        this.defaultTracker = defaultTracker;
    }

    public final void setOnAttributionChangedListener(final OnAttributionChangedListener onAttributionChangedListener) {
        this.onAttributionChangedListener = onAttributionChangedListener;
    }

    public final void setDeviceKnown(final boolean deviceKnown) {
        this.deviceKnown = deviceKnown;
    }

    public final void setDeepLinkComponent(final Class deepLinkComponent) {
        this.deepLinkComponent = deepLinkComponent;
    }

    public final void setOnEventTrackingSucceededListener(final OnEventTrackingSucceededListener onEventTrackingSucceededListener) {
        this.onEventTrackingSucceededListener = onEventTrackingSucceededListener;
    }

    public final void setOnEventTrackingFailedListener(final OnEventTrackingFailedListener onEventTrackingFailedListener) {
        this.onEventTrackingFailedListener = onEventTrackingFailedListener;
    }

    public final void setOnSessionTrackingSucceededListener(final OnSessionTrackingSucceededListener onSessionTrackingSucceededListener) {
        this.onSessionTrackingSucceededListener = onSessionTrackingSucceededListener;
    }

    public final void setOnSessionTrackingFailedListener(final OnSessionTrackingFailedListener onSessionTrackingFailedListener) {
        this.onSessionTrackingFailedListener = onSessionTrackingFailedListener;
    }

    public final void setOnDeeplinkResponseListener(final OnDeeplinkResponseListener onDeeplinkResponseListener) {
        this.onDeeplinkResponseListener = onDeeplinkResponseListener;
    }

    public final void setDelayStart(final double delayStart) {
        this.delayStart = delayStart;
    }

    public final void setUserAgent(final String userAgent) {
        this.userAgent = userAgent;
    }

    public final boolean hasAttributionChangedListener() {
        return onAttributionChangedListener != null;
    }

    public final boolean hasListener() {
        return onAttributionChangedListener != null
                || onEventTrackingSucceededListener != null
                || onEventTrackingFailedListener != null
                || onSessionTrackingSucceededListener != null
                || onSessionTrackingFailedListener != null;
    }

    public final boolean isValid() {
        return appToken != null;
    }

    private boolean isValid(final Context context, final String appToken, final String environment) {
        return checkAppToken(appToken) && checkEnvironment(environment) && checkContext(context);
    }

    private void setLogLevel(final LogLevel logLevel, final String environment) {
        LogLevel newLogLevel = null;
        if (ENVIRONMENT_PRODUCTION.equals(environment)) {
            // production && allows supress -> Supress
            if (allowSuppressLogLevel) {
                newLogLevel = LogLevel.SUPRESS;
            } else {
                // production && not allow supress -> Assert
                newLogLevel = LogLevel.ASSERT;
            }
        } else {
            // not allow supress && try supress -> Assert
            if (!allowSuppressLogLevel &&
                    logLevel == LogLevel.SUPRESS) {
                newLogLevel = LogLevel.ASSERT;
            } else {
                newLogLevel = logLevel;
            }
        }
        logger.setLogLevel(newLogLevel);
    }

    private boolean checkContext(final Context context) {
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

    private boolean checkAppToken(final String appToken) {
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

    private boolean checkEnvironment(final String environment) {
        if (environment == null) {
            logger.error("Missing environment");
            return false;
        }

        if (environment.equals(AdjustConfig.ENVIRONMENT_SANDBOX)) {
            logger.Assert("SANDBOX: Adjust is running in Sandbox mode. " +
                    "Use this setting for testing. " +
                    "Don't forget to set the environment to `production` before publishing!");
            return true;
        }
        if (environment.equals(AdjustConfig.ENVIRONMENT_PRODUCTION)) {
            logger.Assert(
                    "PRODUCTION: Adjust is running in Production mode. " +
                            "Use this setting only for the build that you want to publish. " +
                            "Set the environment to `sandbox` if you want to test your app!");
            return true;
        }

        logger.error("Unknown environment '%s'", environment);
        return false;
    }
}