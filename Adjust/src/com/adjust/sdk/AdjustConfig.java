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
    Map<String, String> callbackPermanentParameters;
    Map<String, String> partnerPermanentParameters;
    String defaultTracker;
    OnFinishedListener onFinishedListener;
    Integer attributionMaxTimeMilliseconds;

    public static final String SANDBOX_ENVIRONMENT = "sandbox";
    public static final String PRODUCTION_ENVIRONMENT = "production";

    private AdjustConfig () {}

    public static AdjustConfig getInstance(Context context, String appToken, String environment) {
        if (!canInit(context, appToken, environment)) {
            return null;
        }
        AdjustConfig adjustConfig = new AdjustConfig();

        adjustConfig.context = context.getApplicationContext();
        adjustConfig.appToken = appToken;
        adjustConfig.environment = environment;


        // default values
        adjustConfig.logLevel = Logger.LogLevel.INFO;
        adjustConfig.eventBufferingEnabled = false;

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

    public void addCallbackParameter(String key, String value) {
        if (callbackPermanentParameters == null) {
            callbackPermanentParameters = new HashMap<String, String>();
        }

        callbackPermanentParameters.put(key, value);
    }

    public void addPartnerParameter(String key, String value) {
        if (partnerPermanentParameters == null) {
            partnerPermanentParameters = new HashMap<String, String>();
        }

        partnerPermanentParameters.put(key, value);
    }

    public void setDefaultTracker(String defaultTracker) {
        this.defaultTracker = defaultTracker;
    }

    public void setOnFinishedListener(OnFinishedListener onFinishedListener) {
        this.onFinishedListener = onFinishedListener;
    }

    public void setAttributionMaxTime(int milliseconds) {
        this.attributionMaxTimeMilliseconds = milliseconds;
    }

    private static boolean canInit(Context context, String appToken, String environment) {
        Logger logger = AdjustFactory.getLogger();
        if (appToken == null) {
            logger.error("Missing App Token.");
            return false;
        }

        if (appToken.length() != 12) {
            logger.error("Malformed App Token '%s'", appToken);
            return false;
        }

        if (context == null) {
            logger.error("Missing context");
            return false;
        }

        context = context.getApplicationContext();
        if (!checkPermission(context, android.Manifest.permission.INTERNET)) {
            logger.error("Missing permission: INTERNET");
            return false;
        }

        if (!checkPermission(context, android.Manifest.permission.ACCESS_WIFI_STATE)) {
            logger.warn("Missing permission: ACCESS_WIFI_STATE");
        }

        if (environment == null) {
            logger.Assert("Missing environment");
            return false;
        }

        if (environment == AdjustConfig.SANDBOX_ENVIRONMENT) {
            logger.Assert("SANDBOX: Adjust is running in Sandbox mode. " +
                    "Use this setting for testing. " +
                    "Don't forget to set the environment to `production` before publishing!");
        } else if (environment == AdjustConfig.PRODUCTION_ENVIRONMENT) {
            logger.Assert(
                    "PRODUCTION: Adjust is running in Production mode. " +
                            "Use this setting only for the build that you want to publish. " +
                            "Set the environment to `sandbox` if you want to test your app!");
        } else {
            logger.Assert("Malformed environment '%s'", environment);
            return false;
        }

        return true;
    }

    private static boolean checkPermission(Context context, String permission) {
        int result = context.checkCallingOrSelfPermission(permission);
        return result == PackageManager.PERMISSION_GRANTED;
    }

}
