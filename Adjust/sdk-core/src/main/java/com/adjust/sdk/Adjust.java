package com.adjust.sdk;

import android.content.Context;
import android.net.Uri;

import java.util.Map;

/**
 * The main interface to Adjust.
 * Use the methods of this class to tell Adjust about the usage of your app.
 * See the README for details.
 *
 * @author Christian Wellenbrock (@wellle)
 * @since 11th November 2011
 */
public class Adjust {
    /**
     * Singleton Adjust SDK instance.
     */
    private static AdjustInstance defaultInstance;

    /**
     * Private constructor.
     */
    private Adjust() {
    }

    /**
     * Method used to obtain Adjust SDK singleton instance.
     *
     * @return Adjust SDK singleton instance.
     */
    public static synchronized AdjustInstance getDefaultInstance() {
        @SuppressWarnings("unused")
        String VERSION = "!SDK-VERSION-STRING!:com.adjust.sdk:adjust-android:5.0.2";

        if (defaultInstance == null) {
            defaultInstance = new AdjustInstance();
        }
        return defaultInstance;
    }

    /**
     * Called upon SDK initialisation.
     *
     * @param adjustConfig AdjustConfig object used for SDK initialisation
     */
    public static void initSdk(AdjustConfig adjustConfig) {
        AdjustInstance adjustInstance = Adjust.getDefaultInstance();
        adjustInstance.initSdk(adjustConfig);
    }

    /**
     * Called to track event.
     *
     * @param event AdjustEvent object to be tracked
     */
    public static void trackEvent(AdjustEvent event) {
        AdjustInstance adjustInstance = Adjust.getDefaultInstance();
        adjustInstance.trackEvent(event);
    }

    /**
     * Called upon each Activity's onResume() method call.
     */
    public static void onResume() {
        AdjustInstance adjustInstance = Adjust.getDefaultInstance();
        adjustInstance.onResume();
    }

    /**
     * Called upon each Activity's onPause() method call.
     */
    public static void onPause() {
        AdjustInstance adjustInstance = Adjust.getDefaultInstance();
        adjustInstance.onPause();
    }

    /**
     * Called to enable SDK.
     *
     */
    public static void enable() {
        AdjustInstance adjustInstance = Adjust.getDefaultInstance();
        adjustInstance.enable();
    }
    /**
     * Called to disable SDK.
     *
     */
    public static void disable() {
        AdjustInstance adjustInstance = Adjust.getDefaultInstance();
        adjustInstance.disable();
    }

    /**
     * Get information if SDK is enabled or not.
     *
     * @param context Application context
     * @param isEnabledListener Callback to get triggered once information is obtained
     */
    public static void isEnabled(final Context context, final OnIsEnabledListener isEnabledListener) {
        if (context == null) {
            AdjustFactory.getLogger().error("null context");
            return;
        }
        if (isEnabledListener == null) {
            AdjustFactory.getLogger().error("Callback for getting isEnabled can't be null");
            return;
        }

        AdjustInstance adjustInstance = Adjust.getDefaultInstance();
        adjustInstance.isEnabled(extractApplicationContext(context), isEnabledListener);
    }

    /**
     * Get information if the payload originates from Adjust.
     *
     * @return boolean indicating whether payload originates from Adjust or not.
     */
    public static boolean isAdjustUninstallDetectionPayload(Map<String, String> payload) {
        return Util.isAdjustUninstallDetectionPayload(payload);
    }

    /**
     * Called to process deeplink.
     *
     * @param adjustDeeplink Deeplink object to process
     * @param context Application context
     */
    public static void processDeeplink(AdjustDeeplink adjustDeeplink, Context context) {
        AdjustInstance adjustInstance = Adjust.getDefaultInstance();
        adjustInstance.processDeeplink(adjustDeeplink, extractApplicationContext(context));
    }

    /**
     * Process the deeplink that has opened an app and potentially get a resolved link.
     *
     * @param adjustDeeplink Deeplink object to process
     * @param callback  Callback where either resolved or echoed deeplink will be sent.
     * @param context Application context
     */
    public static void processAndResolveDeeplink(AdjustDeeplink adjustDeeplink, Context context, OnDeeplinkResolvedListener callback) {
        AdjustInstance adjustInstance = Adjust.getDefaultInstance();
        adjustInstance.processAndResolveDeeplink(adjustDeeplink, extractApplicationContext(context), callback);
    }

    /**
     * Called to process referrer information sent with INSTALL_REFERRER intent.
     *
     * @param referrer Referrer content
     * @param context  Application context
     */
    public static void setReferrer(String referrer, Context context) {
        AdjustInstance adjustInstance = Adjust.getDefaultInstance();
        adjustInstance.sendReferrer(referrer, extractApplicationContext(context));
    }

    /**
     * Called to set SDK to offline mode.
     *
     */
    public static void switchToOfflineMode() {
        AdjustInstance adjustInstance = Adjust.getDefaultInstance();
        adjustInstance.switchToOfflineMode();
    }
    /**
     * Called to set SDK to online mode.
     *
     */
    public static void switchBackToOnlineMode() {
        AdjustInstance adjustInstance = Adjust.getDefaultInstance();
        adjustInstance.switchBackToOnlineMode();
    }

    /**
     * Called to add global callback parameter that will be sent with each session and event.
     *
     * @param key   Global callback parameter key
     * @param value Global callback parameter value
     */
    public static void addGlobalCallbackParameter(String key, String value) {
        AdjustInstance adjustInstance = Adjust.getDefaultInstance();
        adjustInstance.addGlobalCallbackParameter(key, value);
    }

    /**
     * Called to add global partner parameter that will be sent with each session and event.
     *
     * @param key   Global partner parameter key
     * @param value Global partner parameter value
     */
    public static void addGlobalPartnerParameter(String key, String value) {
        AdjustInstance adjustInstance = Adjust.getDefaultInstance();
        adjustInstance.addGlobalPartnerParameter(key, value);
    }

    /**
     * Called to remove global callback parameter from session and event packages.
     *
     * @param key Global callback parameter key
     */
    public static void removeGlobalCallbackParameter(String key) {
        AdjustInstance adjustInstance = Adjust.getDefaultInstance();
        adjustInstance.removeGlobalCallbackParameter(key);
    }

    /**
     * Called to remove global partner parameter from session and event packages.
     *
     * @param key Global partner parameter key
     */
    public static void removeGlobalPartnerParameter(String key) {
        AdjustInstance adjustInstance = Adjust.getDefaultInstance();
        adjustInstance.removeGlobalPartnerParameter(key);
    }

    /**
     * Called to remove all added global callback parameters.
     */
    public static void removeGlobalCallbackParameters() {
        AdjustInstance adjustInstance = Adjust.getDefaultInstance();
        adjustInstance.removeGlobalCallbackParameters();
    }

    /**
     * Called to remove all added global partner parameters.
     */
    public static void removeGlobalPartnerParameters() {
        AdjustInstance adjustInstance = Adjust.getDefaultInstance();
        adjustInstance.removeGlobalPartnerParameters();
    }

    /**
     * Called to set user's push notifications token.
     *
     * @param token   Push notifications token
     * @param context Application context
     */
    public static void setPushToken(final String token, final Context context) {
        AdjustInstance adjustInstance = Adjust.getDefaultInstance();
        adjustInstance.setPushToken(token, extractApplicationContext(context));
    }

    /**
     * Called to forget the user in accordance with GDPR law.
     *
     * @param context Application context
     */
    public static void gdprForgetMe(final Context context) {
        AdjustInstance adjustInstance = Adjust.getDefaultInstance();
        adjustInstance.gdprForgetMe(extractApplicationContext(context));
    }

    public static void trackThirdPartySharing(
            final AdjustThirdPartySharing adjustThirdPartySharing)
    {
        AdjustInstance adjustInstance = Adjust.getDefaultInstance();
        adjustInstance.trackThirdPartySharing(adjustThirdPartySharing);
    }

    public static void trackMeasurementConsent(final boolean consentMeasurement) {
        AdjustInstance adjustInstance = Adjust.getDefaultInstance();
        adjustInstance.trackMeasurementConsent(consentMeasurement);
    }

    /**
     * Track ad revenue from a source provider
     *
     * @param adjustAdRevenue Adjust ad revenue information like source, revenue, currency etc
     */
    public static void trackAdRevenue(final AdjustAdRevenue adjustAdRevenue) {
        AdjustInstance adjustInstance = Adjust.getDefaultInstance();
        adjustInstance.trackAdRevenue(adjustAdRevenue);
    }

    /**
     * Track subscription from Google Play.
     *
     * @param subscription AdjustPlayStoreSubscription object to be tracked
     */
    public static void trackPlayStoreSubscription(final AdjustPlayStoreSubscription subscription) {
        AdjustInstance adjustInstance = Adjust.getDefaultInstance();
        adjustInstance.trackPlayStoreSubscription(subscription);
    }

    /**
     * Called to get value of Google Play Advertising Identifier.
     *
     * @param context                  Application context
     * @param onGoogleAdIdReadListener Callback to get triggered once identifier is obtained
     */
    public static void getGoogleAdId(final Context context,final OnGoogleAdIdReadListener onGoogleAdIdReadListener) {
        if (onGoogleAdIdReadListener == null) {
            AdjustFactory.getLogger().error("onGoogleAdIdReadListener cannot be null");
            return;
        }
        if (context == null) {
            AdjustFactory.getLogger().error("getGoogleAdId: null context");
            return;
        }
        Util.getGoogleAdId(context.getApplicationContext(), onGoogleAdIdReadListener);
    }

    /**
     * Called to get value of Amazon Advertising Identifier.
     *
     * @param context                  Application context
     * @param onAmazonAdIdReadListener Callback to get triggered once identifier is obtained
     */
    public static void getAmazonAdId(final Context context,final OnAmazonAdIdReadListener onAmazonAdIdReadListener) {
        if (onAmazonAdIdReadListener == null) {
            AdjustFactory.getLogger().error("onAmazonAdIdReadListener cannot be null");
            return;
        }
        Context appContext = extractApplicationContext(context);

        if (appContext == null) {
            AdjustFactory.getLogger().error("getAmazonAdId: null context");
            return;
        }
        AdjustInstance adjustInstance = Adjust.getDefaultInstance();
        adjustInstance.getAmazonAdId(appContext, onAmazonAdIdReadListener);
    }

    /**
     * Called to get value of unique Adjust device identifier.
     *
     * @param onAdidReadListener Callback to get triggered once identifier is obtained.
     */
    public static void getAdid(final OnAdidReadListener onAdidReadListener) {
        if (onAdidReadListener == null) {
            AdjustFactory.getLogger().error("Callback for getting adid can't be null");
            return;
        }
        AdjustInstance adjustInstance = Adjust.getDefaultInstance();
        adjustInstance.getAdid(onAdidReadListener);
    }

    /**
     * Called to get user's current attribution value.
     *
     *  @param attributionReadListener Callback to get triggered once attribution is obtained
     */
    public static void getAttribution(final OnAttributionReadListener attributionReadListener) {
        if (attributionReadListener == null) {
            AdjustFactory.getLogger().error("Callback for getting attribution can't be null");
            return;
        }
        AdjustInstance adjustInstance = Adjust.getDefaultInstance();
        adjustInstance.getAttribution(attributionReadListener);
    }

    /**
     * Called to get Google Install Referrer.
     *
     * @param context Application context
     * @param onGooglePlayInstallReferrerReadListener Callback to obtain install referrer.
     */
    public static void getGooglePlayInstallReferrer(final Context context, final OnGooglePlayInstallReferrerReadListener onGooglePlayInstallReferrerReadListener) {
        if (onGooglePlayInstallReferrerReadListener == null) {
            AdjustFactory.getLogger().error("onGooglePlayInstallReferrerReadListener cannot be null");
            return;
        }
        if (context == null) {
            AdjustFactory.getLogger().error("null context");
            return;
        }
        AdjustInstance adjustInstance = Adjust.getDefaultInstance();
        adjustInstance.getGooglePlayInstallReferrer(context ,onGooglePlayInstallReferrerReadListener);
    }

    /**
     * Called to get last opened deeplink.
     *
     * @param context Application context
     * @param onLastDeeplinkReadListener Callback to obtain last opened deeplink.
     */
    public static void getLastDeeplink(final Context context, final OnLastDeeplinkReadListener onLastDeeplinkReadListener) {
        if (onLastDeeplinkReadListener == null) {
            AdjustFactory.getLogger().error("onLastDeeplinkReadListener cannot be null");
            return;
        }
        if (context == null) {
            AdjustFactory.getLogger().error("null context");
            return;
        }
        AdjustInstance adjustInstance = Adjust.getDefaultInstance();
        adjustInstance.getLastDeeplink(context, onLastDeeplinkReadListener);
    }

    /**
     * Called to get native SDK version string.
     *
     * @param onSdkVersionReadListener Callback to get triggered once SDK version is obtained
     */
    public static void getSdkVersion(final OnSdkVersionReadListener onSdkVersionReadListener) {
        if (onSdkVersionReadListener == null) {
            AdjustFactory.getLogger().error("onSdkVersionReadListener cannot be null");
            return;
        }
        AdjustInstance adjustInstance = Adjust.getDefaultInstance();
        adjustInstance.getSdkVersion(onSdkVersionReadListener);
    }

    /**
     * Verify in app purchase from Google Play.
     *
     * @param purchase  AdjustPurchase object to be tracked
     * @param callback  Callback to obtain verification results
     */
    public static void verifyPlayStorePurchase(final AdjustPlayStorePurchase purchase,
                                               final OnPurchaseVerificationFinishedListener callback) {
        if (callback == null) {
            AdjustFactory.getLogger().error("Purchase verification aborted because verification callback is null");
            return;
        }
        AdjustInstance adjustInstance = Adjust.getDefaultInstance();
        adjustInstance.verifyPlayStorePurchase(purchase, callback);
    }

    /**
     * Verify in app purchase from Google Play and track Adjust event associated with it.
     *
     * @param event     AdjustEvent object to be tracked
     * @param callback  Callback to obtain verification results
     */
    public static void verifyAndTrackPlayStorePurchase(final AdjustEvent event, OnPurchaseVerificationFinishedListener callback) {
        AdjustInstance adjustInstance = Adjust.getDefaultInstance();
        adjustInstance.verifyAndTrackPlayStorePurchase(event, callback);
    }

    /**
     * Used for testing purposes only. Do NOT use this method.
     *
     * @param testOptions Adjust integration tests options
     */
    public static void setTestOptions(AdjustTestOptions testOptions) {
        if (testOptions.teardown != null && testOptions.teardown.booleanValue()) {
            if (defaultInstance != null) {
                defaultInstance.teardown();
            }
            defaultInstance = null;
            AdjustFactory.teardown(testOptions.context);
        }

        AdjustInstance adjustInstance = Adjust.getDefaultInstance();
        adjustInstance.setTestOptions(testOptions);
    }

    private static Context extractApplicationContext(final Context context) {
        if (context == null) {
            return null;
        }

        return context.getApplicationContext();
    }
}
