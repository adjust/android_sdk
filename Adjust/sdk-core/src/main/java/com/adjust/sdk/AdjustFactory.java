package com.adjust.sdk;

import android.content.Context;

import com.adjust.sdk.network.IActivityPackageSender;
import com.adjust.sdk.network.UtilNetworking;

import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class AdjustFactory {
    private static IPackageHandler packageHandler = null;
    private static IAttributionHandler attributionHandler = null;
    private static IActivityHandler activityHandler = null;
    private static ILogger logger = null;
    private static ISdkClickHandler sdkClickHandler = null;
    private static IPurchaseVerificationHandler purchaseVerificationHandler = null;

    private static long timerInterval = -1;
    private static long timerStart = -1;
    private static long sessionInterval = -1;
    private static long subsessionInterval = -1;
    private static BackoffStrategy sdkClickBackoffStrategy = null;
    private static BackoffStrategy packageHandlerBackoffStrategy = null;
    private static BackoffStrategy installSessionBackoffStrategy = null;
    private static String baseUrl = null;
    private static String gdprUrl = null;
    private static String subscriptionUrl = null;
    private static String purchaseVerificationUrl = null;
    private static UtilNetworking.IConnectionOptions connectionOptions = null;
    private static UtilNetworking.IHttpsURLConnectionProvider httpsURLConnectionProvider = null;
    private static boolean tryInstallReferrer = true;
    private static boolean ignoreSystemLifecycleBootstrap = false;

    public static class URLGetConnection {
        HttpsURLConnection httpsURLConnection;
        URL url;
        URLGetConnection(HttpsURLConnection httpsURLConnection, URL url) {
            this.httpsURLConnection = httpsURLConnection;
            this.url = url;
        }
    }

    public static IPackageHandler getPackageHandler(
            IActivityHandler activityHandler,
            Context context,
            boolean startsSending,
            IActivityPackageSender packageHandlerActivityPackageSender)
    {
        if (packageHandler == null) {
            return new PackageHandler(activityHandler,
                    context,
                    startsSending,
                    packageHandlerActivityPackageSender);
        }
        packageHandler.init(activityHandler,
                context,
                startsSending,
                packageHandlerActivityPackageSender);
        return packageHandler;
    }

    public static ILogger getLogger() {
        if (logger == null) {
            // Logger needs to be "static" to retain the configuration throughout the app
            logger = new Logger();
        }
        return logger;
    }

    public static long getTimerInterval() {
        if (timerInterval == -1) {
            return Constants.ONE_MINUTE;
        }
        return timerInterval;
    }

    public static long getTimerStart() {
        if (timerStart == -1) {
            return Constants.ONE_MINUTE;
        }
        return timerStart;
    }

    public static long getSessionInterval() {
        if (sessionInterval == -1) {
            return Constants.THIRTY_MINUTES;
        }
        return sessionInterval;
    }

    public static long getSubsessionInterval() {
        if (subsessionInterval == -1) {
            return Constants.ONE_SECOND;
        }
        return subsessionInterval;
    }

    public static BackoffStrategy getSdkClickBackoffStrategy() {
        if (sdkClickBackoffStrategy == null) {
            return BackoffStrategy.SHORT_WAIT;
        }
        return sdkClickBackoffStrategy;
    }

    public static BackoffStrategy getPackageHandlerBackoffStrategy() {
        if (packageHandlerBackoffStrategy == null) {
            return BackoffStrategy.LONG_WAIT;
        }
        return packageHandlerBackoffStrategy;
    }

    public static BackoffStrategy getInstallSessionBackoffStrategy() {
        if (installSessionBackoffStrategy == null) {
            return BackoffStrategy.SHORT_WAIT;
        }
        return installSessionBackoffStrategy;
    }

    public static IActivityHandler getActivityHandler(AdjustConfig config) {
        if (activityHandler == null) {
            return ActivityHandler.getInstance(config);
        }
        activityHandler.init(config);
        return activityHandler;
    }

    public static IAttributionHandler getAttributionHandler(
            IActivityHandler activityHandler,
            boolean startsSending,
            IActivityPackageSender packageHandlerActivityPackageSender)
    {
        if (attributionHandler == null) {
            return new AttributionHandler(activityHandler,
                    startsSending,
                    packageHandlerActivityPackageSender);
        }
        attributionHandler.init(activityHandler,
                startsSending,
                packageHandlerActivityPackageSender);
        return attributionHandler;
    }

    public static ISdkClickHandler getSdkClickHandler(
            IActivityHandler activityHandler,
            boolean startsSending,
            IActivityPackageSender packageHandlerActivityPackageSender)
    {
        if (sdkClickHandler == null) {
            return new SdkClickHandler(activityHandler,
                    startsSending,
                    packageHandlerActivityPackageSender);
        }

        sdkClickHandler.init(activityHandler, startsSending, packageHandlerActivityPackageSender);
        return sdkClickHandler;
    }

    public static IPurchaseVerificationHandler getPurchaseVerificationHandler(
            IActivityHandler activityHandler,
            boolean startsSending,
            IActivityPackageSender packageHandlerActivityPackageSender)
    {
        if (purchaseVerificationHandler == null) {
            return new PurchaseVerificationHandler(activityHandler,
                    startsSending,
                    packageHandlerActivityPackageSender);
        }

        purchaseVerificationHandler.init(activityHandler, startsSending, packageHandlerActivityPackageSender);
        return purchaseVerificationHandler;
    }

    public static String getBaseUrl() {
        return AdjustFactory.baseUrl;
    }

    public static String getGdprUrl() {
        return AdjustFactory.gdprUrl;
    }

    public static String getSubscriptionUrl() {
        return AdjustFactory.subscriptionUrl;
    }

    public static String getPurchaseVerificationUrl() {
        return AdjustFactory.purchaseVerificationUrl;
    }

    public static UtilNetworking.IConnectionOptions getConnectionOptions() {
        if (connectionOptions == null) {
            return UtilNetworking.createDefaultConnectionOptions();
        }
        return connectionOptions;
    }

    public static UtilNetworking.IHttpsURLConnectionProvider getHttpsURLConnectionProvider() {
        if (httpsURLConnectionProvider == null) {
            return UtilNetworking.createDefaultHttpsURLConnectionProvider();
        }
        return httpsURLConnectionProvider;
    }

    public static boolean getTryInstallReferrer() {
        return tryInstallReferrer;
    }

    public static void setPackageHandler(IPackageHandler packageHandler) {
        AdjustFactory.packageHandler = packageHandler;
    }

    public static void setLogger(ILogger logger) {
        AdjustFactory.logger = logger;
    }

    public static void setTimerInterval(long timerInterval) {
        AdjustFactory.timerInterval = timerInterval;
    }

    public static void setTimerStart(long timerStart) {
        AdjustFactory.timerStart = timerStart;
    }

    public static void setSessionInterval(long sessionInterval) {
        AdjustFactory.sessionInterval = sessionInterval;
    }

    public static void setSubsessionInterval(long subsessionInterval) {
        AdjustFactory.subsessionInterval = subsessionInterval;
    }

    public static void setSdkClickBackoffStrategy(BackoffStrategy sdkClickBackoffStrategy) {
        AdjustFactory.sdkClickBackoffStrategy = sdkClickBackoffStrategy;
    }

    public static void setPackageHandlerBackoffStrategy(BackoffStrategy packageHandlerBackoffStrategy) {
        AdjustFactory.packageHandlerBackoffStrategy = packageHandlerBackoffStrategy;
    }

    public static void setActivityHandler(IActivityHandler activityHandler) {
        AdjustFactory.activityHandler = activityHandler;
    }

    public static void setAttributionHandler(IAttributionHandler attributionHandler) {
        AdjustFactory.attributionHandler = attributionHandler;
    }

    public static void setSdkClickHandler(ISdkClickHandler sdkClickHandler) {
        AdjustFactory.sdkClickHandler = sdkClickHandler;
    }

    public static void setBaseUrl(String baseUrl) {
        AdjustFactory.baseUrl = baseUrl;
    }

    public static void setGdprUrl(String gdprUrl) {
        AdjustFactory.gdprUrl = gdprUrl;
    }

    public static void setSubscriptionUrl(String subscriptionUrl) {
        AdjustFactory.subscriptionUrl = subscriptionUrl;
    }

    public static void setPurchaseVerificationUrl(String purchaseVerificationUrl) {
        AdjustFactory.purchaseVerificationUrl = purchaseVerificationUrl;
    }

    public static void setConnectionOptions(UtilNetworking.IConnectionOptions connectionOptions) {
        AdjustFactory.connectionOptions = connectionOptions;
    }

    public static void setHttpsURLConnectionProvider(
            UtilNetworking.IHttpsURLConnectionProvider httpsURLConnectionProvider)
    {
        AdjustFactory.httpsURLConnectionProvider = httpsURLConnectionProvider;
    }

    public static void setTryInstallReferrer(boolean tryInstallReferrer) {
        AdjustFactory.tryInstallReferrer = tryInstallReferrer;
    }

    public static void setIgnoreSystemLifecycleBootstrap(
      final boolean ignoreSystemLifecycleBootstrap)
    {
        AdjustFactory.ignoreSystemLifecycleBootstrap = ignoreSystemLifecycleBootstrap;
    }
    public static boolean isSystemLifecycleBootstrapIgnored() {
        return AdjustFactory.ignoreSystemLifecycleBootstrap;
    }

    private static String byte2HexFormatted(byte[] arr) {
        StringBuilder str = new StringBuilder(arr.length * 2);

        for (int i = 0; i < arr.length; i++) {
            String h = Integer.toHexString(arr[i]);
            int l = h.length();

            if (l == 1) {
                h = "0" + h;
            }

            if (l > 2) {
                h = h.substring(l - 2, l);
            }

            str.append(h.toUpperCase());

            // if (i < (arr.length - 1)) str.append(':');
        }
        return str.toString();
    }

    public static void teardown(Context context) {
        if(context != null) {
            ActivityHandler.deleteState(context);
            PackageHandler.deleteState(context);
        }
        packageHandler = null;
        attributionHandler = null;
        activityHandler = null;
        logger = null;
        sdkClickHandler = null;

        timerInterval = -1;
        timerStart = -1;
        sessionInterval = -1;
        subsessionInterval = -1;
        sdkClickBackoffStrategy = null;
        packageHandlerBackoffStrategy = null;
        baseUrl = Constants.BASE_URL;
        gdprUrl = Constants.GDPR_URL;
        subscriptionUrl = Constants.SUBSCRIPTION_URL;
        purchaseVerificationUrl = Constants.PURCHASE_VERIFICATION_URL;
        connectionOptions = null;
        httpsURLConnectionProvider = null;
        tryInstallReferrer = true;
    }
}
