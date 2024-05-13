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
        String VERSION = "!SDK-VERSION-STRING!:com.adjust.sdk:adjust-android:5.0.0";

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
    public static void onCreate(AdjustConfig adjustConfig) {
        AdjustInstance adjustInstance = Adjust.getDefaultInstance();
        adjustInstance.onCreate(adjustConfig);
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
     * Called to disable/enable SDK.
     *
     * @param enabled boolean indicating whether SDK should be enabled or disabled
     */
    public static void setEnabled(boolean enabled) {
        AdjustInstance adjustInstance = Adjust.getDefaultInstance();
        adjustInstance.setEnabled(enabled);
    }

    /**
     * Get information if SDK is enabled or not.
     *
     * @return boolean indicating whether SDK is enabled or not
     */
    public static void isEnabled(final OnIsEnabledListener isEnabledListener) {
        if (isEnabledListener == null) {
            AdjustFactory.getLogger().error("Callback for getting isEnabled can't be null");
            return;
        }
        AdjustInstance adjustInstance = Adjust.getDefaultInstance();
        adjustInstance.isEnabled(isEnabledListener);
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
     * Called to process deep link.
     *
     * @param url Deep link URL to process
     *
     * @deprecated Use {@link #appWillOpenUrl(Uri, Context)}} instead.
     */
    @Deprecated
    public static void appWillOpenUrl(Uri url) {
        AdjustInstance adjustInstance = Adjust.getDefaultInstance();
        adjustInstance.appWillOpenUrl(url);
    }

    /**
     * Called to process deep link.
     *
     * @param url Deep link URL to process
     * @param context Application context
     */
    public static void appWillOpenUrl(Uri url, Context context) {
        AdjustInstance adjustInstance = Adjust.getDefaultInstance();
        adjustInstance.appWillOpenUrl(url, extractApplicationContext(context));
    }

    /**
     * Process the deep link that has opened an app and potentially get a resolved link.
     *
     * @param url Deep link URL to process
     * @param callback  Callback where either resolved or echoed deep link will be sent.
     * @param context Application context
     */
    public static void processDeeplink(Uri url, Context context, OnDeeplinkResolvedListener callback) {
        AdjustInstance adjustInstance = Adjust.getDefaultInstance();
        adjustInstance.processDeeplink(url, extractApplicationContext(context), callback);
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
     * Called to set SDK to offline or online mode.
     *
     * @param enabled boolean indicating should SDK be in offline mode (true) or not (false)
     */
    public static void setOfflineMode(boolean enabled) {
        AdjustInstance adjustInstance = Adjust.getDefaultInstance();
        adjustInstance.setOfflineMode(enabled);
    }

    /**
     * Called if SDK initialisation was delayed and you would like to stop waiting for timer.
     */
    public static void sendFirstPackages() {
        AdjustInstance adjustInstance = Adjust.getDefaultInstance();
        adjustInstance.sendFirstPackages();
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
     * @param token Push notifications token
     * @deprecated use {@link #setPushToken(String, Context)} instead.
     */
    public static void setPushToken(String token) {
        AdjustInstance adjustInstance = Adjust.getDefaultInstance();
        adjustInstance.setPushToken(token);
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
     * Called to enable COPPA compliance.
     *
     * @param context Application context
     */
    public static void enableCoppaCompliance(final Context context) {
        AdjustInstance adjustInstance = Adjust.getDefaultInstance();
        adjustInstance.enableCoppaCompliance(context);
    }

    /**
     * Called to disable COPPA compliance.
     *
     * @param context Application context
     */
    public static void disableCoppaCompliance(final Context context) {
        AdjustInstance adjustInstance = Adjust.getDefaultInstance();
        adjustInstance.disableCoppaCompliance(context);
    }

    /**
     * Called to enable Google Play Store Kids app setting.
     *
     * @param context Application context
     */
    public static void enablePlayStoreKidsApp(final Context context) {
        AdjustInstance adjustInstance = Adjust.getDefaultInstance();
        adjustInstance.enablePlayStoreKidsApp(context);
    }

    /**
     * Called to disable Google Play Store Kids app setting.
     *
     * @param context Application context
     */
    public static void disablePlayStoreKidsApp(final Context context) {
        AdjustInstance adjustInstance = Adjust.getDefaultInstance();
        adjustInstance.disablePlayStoreKidsApp(context);
    }

    /**
     * Called to get value of Google Play Advertising Identifier.
     *
     * @param context        Application context
     * @param onDeviceIdRead Callback to get triggered once identifier is obtained
     */
    public static void getGoogleAdId(Context context, OnDeviceIdsRead onDeviceIdRead) {
        Context appContext = null;
        if (context != null) {
            appContext = context.getApplicationContext();
        }

        Util.getGoogleAdId(appContext, onDeviceIdRead);
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
            String message = "null context";
            AdjustFactory.getLogger().error(message);
            onAmazonAdIdReadListener.onFail(message);
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
     * @return AdjustAttribution object with current attribution value
     */
    public static AdjustAttribution getAttribution() {
        AdjustInstance adjustInstance = Adjust.getDefaultInstance();
        return adjustInstance.getAttribution();
    }

    /**
     * Called to get Google Install Referrer.
     *
     * @param context Application context
     * @param onGooglePlayInstallReferrerReadListener Callback to obtain install referrer.
     */
    public static void getGooglePlayInstallReferrer(Context context, OnGooglePlayInstallReferrerReadListener onGooglePlayInstallReferrerReadListener) {
        AdjustInstance adjustInstance = Adjust.getDefaultInstance();
        adjustInstance.getGooglePlayInstallReferrer(context ,onGooglePlayInstallReferrerReadListener);
    }
    /**
     * Called to get native SDK version string.
     *
     * @return Native SDK version string.
     */
    public static String getSdkVersion() {
        AdjustInstance adjustInstance = Adjust.getDefaultInstance();
        return adjustInstance.getSdkVersion();
    }

    /**
     * Verify in app purchase from Google Play.
     *
     * @param purchase  AdjustPurchase object to be tracked
     * @param callback  Callback to obtain verification results
     */
    public static void verifyPurchase(final AdjustPurchase purchase, OnPurchaseVerificationFinishedListener callback) {
        AdjustInstance adjustInstance = Adjust.getDefaultInstance();
        adjustInstance.verifyPurchase(purchase, callback);
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
