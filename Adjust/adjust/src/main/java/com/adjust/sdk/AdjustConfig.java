package com.adjust.sdk;

import android.content.Context;
import android.content.pm.PackageManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by pfms on 06/11/14.
 */
public class AdjustConfig {
    Context context;
    String appToken;
    String environment;
    Logger.LogLevel logLevel;
    String sdkPrefix;
    Boolean eventBufferingEnabled;
    String defaultTracker;
    OnFinishedListener onFinishedListener;
    String referrer;
    Boolean knowDevice;

    public static final String SANDBOX_ENVIRONMENT = "sandbox";
    public static final String PRODUCTION_ENVIRONMENT = "production";

    private AdjustConfig(Context context, String appToken, String environment) {
        this.context = context;
        this.appToken = appToken;
        this.environment = environment;

        // default values
        this.logLevel = Logger.LogLevel.INFO;
        this.eventBufferingEnabled = false;
    }

    public static AdjustConfig getInstance(Context context, String appToken, String environment) {
        if (!checkAppToken(appToken)) return null;
        if (!checkEnvironment(environment, true)) return null;
        if (!checkContext(context, true)) return null;

        AdjustConfig adjustConfig = new AdjustConfig(context, appToken, environment);

        return adjustConfig;
    }

    public void setEventBufferingEnabled(Boolean eventBufferingEnabled) {
        this.eventBufferingEnabled = eventBufferingEnabled;
    }

    public void setLogLevel(Logger.LogLevel logLevel) {
        this.logLevel = logLevel;
    }

    public void setSdkPrefix(String sdkPrefix) {
        this.sdkPrefix = sdkPrefix;
    }

    public void setDefaultTracker(String defaultTracker) {
        this.defaultTracker = defaultTracker;
    }

    public void setOnFinishedListener(OnFinishedListener onFinishedListener) {
        this.onFinishedListener = onFinishedListener;
    }

    public boolean hasDelegate() { return onFinishedListener != null; }

    public boolean isValid() {
        if (!checkAppToken(appToken)) return false;
        if (!checkEnvironment(environment, false)) return false;
        if (!checkContext(context, false)) return false;

        return true;
    }

    private static boolean checkContext(Context context, boolean logNonError) {
        Logger logger = AdjustFactory.getLogger();
        if (context == null) {
            logger.error("Missing context");
            return false;
        }

        if (!checkPermission(context, android.Manifest.permission.INTERNET)) {
            logger.error("Missing permission: INTERNET");
            return false;
        }

        if (!checkPermission(context, android.Manifest.permission.ACCESS_WIFI_STATE)) {
            if (logNonError) {
                logger.warn("Missing permission: ACCESS_WIFI_STATE");
            }
        }

        return true;
    }

    private static boolean checkAppToken(String appToken) {
        Logger logger = AdjustFactory.getLogger();
        if (appToken == null) {
            logger.error("Missing App Token.");
            return false;
        }

        if (appToken.length() != 12) {
            logger.error("Malformed App Token '%s'", appToken);
            return false;
        }

        return true;
    }

    private static boolean checkEnvironment(String environment, boolean logNonError) {
        Logger logger = AdjustFactory.getLogger();
        if (environment == null) {
            logger.error("Missing environment");
            return false;
        }

        if (environment == AdjustConfig.SANDBOX_ENVIRONMENT) {
            if (logNonError) {
                logger.Assert("SANDBOX: Adjust is running in Sandbox mode. " +
                        "Use this setting for testing. " +
                        "Don't forget to set the environment to `production` before publishing!");
            }
            return true;
        }
        if (environment == AdjustConfig.PRODUCTION_ENVIRONMENT) {
            if (logNonError) {
                logger.Assert(
                        "PRODUCTION: Adjust is running in Production mode. " +
                                "Use this setting only for the build that you want to publish. " +
                                "Set the environment to `sandbox` if you want to test your app!");
            }
            return true;
        }

        logger.error("Malformed environment '%s'", environment);
        return false;
    }

    private static boolean checkPermission(Context context, String permission) {
        int result = context.checkCallingOrSelfPermission(permission);
        return result == PackageManager.PERMISSION_GRANTED;
    }

}