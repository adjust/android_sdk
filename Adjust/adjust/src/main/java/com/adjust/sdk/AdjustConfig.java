package com.adjust.sdk;

import android.content.Context;

import java.util.*;

import static android.R.attr.data;
import static com.adjust.sdk.Constants.*;

/**
 * Created by pfms on 06/11/14.
 */
public class AdjustConfig implements IStateable {
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
    String pushToken;

    public static final String ENVIRONMENT_SANDBOX = "sandbox";
    public static final String ENVIRONMENT_PRODUCTION = "production";

    public AdjustConfig(Context context, String appToken, String environment) {
        init(context, appToken, environment, false);
    }

    public AdjustConfig(Context context, String appToken, String environment, boolean allowSuppressLogLevel) {
        init(context, appToken, environment, allowSuppressLogLevel);
    }

    private void init(Context context, String appToken, String environment, boolean allowSuppressLogLevel) {
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

    public void setEventBufferingEnabled(Boolean eventBufferingEnabled) {
        if (eventBufferingEnabled == null) {
            this.eventBufferingEnabled = false;
            return;
        }
        this.eventBufferingEnabled = eventBufferingEnabled;
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

    public void setDeviceKnown(boolean deviceKnown) {
        this.deviceKnown = deviceKnown;
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

    public void setDelayStart(double delayStart) {
        this.delayStart = delayStart;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public boolean hasListener() {
        return onAttributionChangedListener != null
                || onEventTrackingSucceededListener != null
                || onEventTrackingFailedListener != null
                || onSessionTrackingSucceededListener != null
                || onSessionTrackingFailedListener != null;
    }

    public boolean isValid() {
        return appToken != null;
    }

    private boolean isValid(Context context, String appToken, String environment) {
        if (!checkAppToken(appToken)) return false;
        if (!checkEnvironment(environment)) return false;
        if (!checkContext(context)) return false;

        return true;
    }

    private void setLogLevel(LogLevel logLevel, String environment) {
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

    @Override
    public Map<String, Object> getState() {
        Map<String, Object> data = new HashMap<>();
        data.put(STATE_DEFAULT_TRACKER, defaultTracker);
        data.put(STATE_IS_ATTRIBUTION_CALLBACK_IMPLEMENNTED, onAttributionChangedListener != null);
        data.put(STATE_IS_EVENT_TRACKING_SUCCEEDED_CALLBACK_IMPLEMENTED, onEventTrackingFailedListener != null);
        data.put(STATE_IS_EVENT_TRACKING_FAILED_CALLBACK_IMPLEMENTED, onEventTrackingFailedListener != null);
        data.put(STATE_IS_SESSION_TRACKING_SUCCEEDED_CALLBACK_IMPLEMENTED, onSessionTrackingSucceededListener != null);
        data.put(STATE_IS_SESSION_TRACKING_FAILED_CALLBACK_IMPLEMENTED, onSessionTrackingFailedListener != null);
        data.put(STATE_IS_DEFERRED_DEEPLINK_CALLBACK_IMPLEMENTED, onDeeplinkResponseListener != null);
        data.put(STATE_ALLOW_SUPPRESS_LOG_LEVEL, allowSuppressLogLevel);
        data.put(STATE_USER_AGENT, userAgent);
        data.put(STATE_APP_TOKEN, appToken);
        data.put(STATE_ENVIRONMENT, environment);
        data.put(STATE_PROCESS_NAME, processName);
        data.put(STATE_SDK_PREFIX, sdkPrefix);
        data.put(STATE_REFERRER, referrer);
        data.put(STATE_DELAY_START, delayStart);

        return data;
    }
}