package com.adjust.sdk;

import android.net.Uri;
import android.content.Context;

import com.adjust.sdk.scheduler.AsyncTaskExecutor;
import com.adjust.sdk.scheduler.SingleThreadCachedScheduler;
import com.adjust.sdk.scheduler.ThreadExecutor;

import java.util.List;
import java.util.ArrayList;

/**
 * Class used to forward instructions to SDK which user gives as part of Adjust class interface.
 *
 * @author Pedro Silva (@nonelse)
 * @since 12th April 2014
 */
public class AdjustInstance {
    public static class PreLaunchActions {
        public List<IRunActivityHandler> preLaunchActionsArray;
        public List<AdjustThirdPartySharing> preLaunchAdjustThirdPartySharingArray;
        public Boolean lastMeasurementConsentTracked;

        public PreLaunchActions() {
            preLaunchActionsArray = new ArrayList<>();
            preLaunchAdjustThirdPartySharingArray = new ArrayList<>();
            lastMeasurementConsentTracked = null;
        }
    }

    /**
     * Push notifications token.
     */
    private String pushToken;

    /**
     * Is SDK enabled or not.
     */
    private Boolean startEnabled = null;

    /**
     * Is SDK offline or not.
     */
    private boolean startOffline = false;

    /**
     * ActivityHandler instance.
     */
    private IActivityHandler activityHandler;

    private PreLaunchActions preLaunchActions = new PreLaunchActions();

    private OnDeeplinkResolvedListener cachedDeeplinkResolutionCallback;

    private ArrayList<OnAdidReadListener> cachedAdidReadCallbacks = new ArrayList<>();
    private ArrayList<OnAttributionReadListener> cachedAttributionReadCallbacks = new ArrayList<>();
    /**
     * Base path for Adjust packages.
     */
    private String basePath;

    /**
     * Path for GDPR package.
     */
    private String gdprPath;

    /**
     * Path for subscription package.
     */
    private String subscriptionPath;

    /**
     * Path for purchase verification package.
     */
    private String purchaseVerificationPath;

    /**
     * Called upon SDK initialisation.
     *
     * @param adjustConfig AdjustConfig object used for SDK initialisation
     */
    public void initSdk(final AdjustConfig adjustConfig) {
        if (!AdjustSigner.isPresent()) {
            AdjustFactory.getLogger().error("Missing signature library, SDK can't be initialised");
            return;
        }
        if (adjustConfig == null) {
            AdjustFactory.getLogger().error("AdjustConfig missing");
            return;
        }
        if (!adjustConfig.isValid()) {
            AdjustFactory.getLogger().error("AdjustConfig not initialized correctly");
            return;
        }
        if (activityHandler != null) {
            AdjustFactory.getLogger().error("Adjust already initialized");
            return;
        }

        adjustConfig.preLaunchActions = preLaunchActions;
        adjustConfig.pushToken = pushToken;
        adjustConfig.startEnabled = startEnabled;
        adjustConfig.startOffline = startOffline;
        adjustConfig.basePath = this.basePath;
        adjustConfig.gdprPath = this.gdprPath;
        adjustConfig.subscriptionPath = this.subscriptionPath;
        adjustConfig.purchaseVerificationPath = this.purchaseVerificationPath;
        adjustConfig.cachedDeeplinkResolutionCallback = cachedDeeplinkResolutionCallback;
        adjustConfig.cachedAdidReadCallbacks = cachedAdidReadCallbacks;
        adjustConfig.cachedAttributionReadCallbacks = cachedAttributionReadCallbacks;

        activityHandler = AdjustFactory.getActivityHandler(adjustConfig);
        setSendingReferrersAsNotSent(adjustConfig.context);
    }

    /**
     * Called to track event.
     *
     * @param event AdjustEvent object to be tracked
     */
    public void trackEvent(final AdjustEvent event) {
        if (!checkActivityHandler("trackEvent")) {
            return;
        }
        activityHandler.trackEvent(event);
    }

    /**
     * Called upon each Activity's onResume() method call.
     */
    public void onResume() {
        if (!checkActivityHandler("onResume")) {
            return;
        }
        activityHandler.onResume();
    }

    /**
     * Called upon each Activity's onPause() method call.
     */
    public void onPause() {
        if (!checkActivityHandler("onPause")) {
            return;
        }
        activityHandler.onPause();
    }

    /**
     * Called to enable SDK.
     *
     */
    public void enable() {
        this.startEnabled = true;
        if (checkActivityHandler(true, "enabled mode", "disabled mode")) {
            activityHandler.setEnabled(true);
        }
    }
    /**
     * Called to disable SDK.
     *
     */
    public void disable() {
        this.startEnabled = false;
        if (checkActivityHandler(false, "enabled mode", "disabled mode")) {
            activityHandler.setEnabled(false);
        }
    }

    /**
     * Get information if SDK is enabled or not.
     *
     * @param context Application context
     * @param isEnabledListener Callback to get triggered once information is obtained
     */
    public void isEnabled(final Context context, final OnIsEnabledListener isEnabledListener) {
        if (!checkActivityHandler("isEnabled")) {
            new AsyncTaskExecutor<Context,Boolean>(){
                @Override
                protected Boolean doInBackground(Context... contexts) {
                    return Util.isEnabledFromActivityStateFile(contexts[0]);
                }

                @Override
                protected void onPostExecute(Boolean isEnabled) {
                    isEnabledListener.onIsEnabledRead(isEnabled);
                }
            }.execute(context);
            return;
        }
        activityHandler.isEnabled(isEnabledListener);
    }

    /**
     * Called to process deeplink.
     *
     * @param adjustDeeplink     Deeplink object to process
     * @param context Application context
     */
    public void processDeeplink(final AdjustDeeplink adjustDeeplink, final Context context) {
        // Check for deeplink validity. If invalid, return.
        if (adjustDeeplink == null || !adjustDeeplink.isValid()) {
            AdjustFactory.getLogger().warn(
                    "Skipping deeplink processing (null or empty)");
            return;
        }

        cacheDeeplink(adjustDeeplink.url, context);

        long clickTime = System.currentTimeMillis();
        if (!checkActivityHandler("processDeeplink", true)) {
            saveDeeplink(adjustDeeplink.url, clickTime, context);
            return;
        }

        activityHandler.processDeeplink(adjustDeeplink.url, clickTime);
    }

    /**
     * Process the deeplink that has opened an app and potentially get a resolved link.
     *
     * @param adjustDeeplink      Deeplink object to process
     * @param callback Callback where either resolved or echoed deeplink will be sent.
     * @param context  Application context
     */
    public void processAndResolveDeeplink(AdjustDeeplink adjustDeeplink, Context context, OnDeeplinkResolvedListener callback) {
        // Check for deeplink validity. If invalid, return.
        if (adjustDeeplink == null || !adjustDeeplink.isValid()) {
            AdjustFactory.getLogger().warn(
                    "Skipping deeplink processing (null or empty)");
            return;
        }

        // if resolution result is not wanted, fallback to default method
        if (callback == null) {
            processDeeplink(adjustDeeplink, context);
            return;
        }

        cacheDeeplink(adjustDeeplink.url, context);

        // if deeplink processing is triggered prior to SDK being initialized
        long clickTime = System.currentTimeMillis();
        if (!checkActivityHandler("processAndResolveDeeplink", true)) {
            saveDeeplink(adjustDeeplink.url, clickTime, context);
            this.cachedDeeplinkResolutionCallback = callback;
            return;
        }

        // if deeplink processing was triggered with SDK being initialized
        activityHandler.processAndResolveDeeplink(adjustDeeplink.url, clickTime, callback);
    }

    /**
     * Called to process referrer information sent with INSTALL_REFERRER intent.
     *
     * @param rawReferrer Raw referrer content
     * @param context     Application context
     */
    public void sendReferrer(final String rawReferrer, final Context context) {
        long clickTime = System.currentTimeMillis();

        // Check for referrer validity. If invalid, return.
        if (rawReferrer == null || rawReferrer.length() == 0) {
            AdjustFactory.getLogger().warn(
                    "Skipping INSTALL_REFERRER intent referrer processing (null or empty)");
            return;
        }

        saveRawReferrer(rawReferrer, clickTime, context);
        if (checkActivityHandler("referrer", true)) {
            if (activityHandler.isEnabled()) {
                activityHandler.sendReftagReferrer();
            }
        }
    }

    /**
     * Called to process preinstall payload information sent with SYSTEM_INSTALLER_REFERRER intent.
     *
     * @param referrer Preinstall referrer content
     * @param context  Application context
     */
    public void sendPreinstallReferrer(final String referrer, final Context context) {
        // Check for referrer validity. If invalid, return.
        if (referrer == null || referrer.length() == 0) {
            AdjustFactory.getLogger().warn(
                    "Skipping SYSTEM_INSTALLER_REFERRER preinstall referrer processing (null or empty)");
            return;
        }

        savePreinstallReferrer(referrer, context);
        if (checkActivityHandler("preinstall referrer", true)) {
            if (activityHandler.isEnabled()) {
                activityHandler.sendPreinstallReferrer();
            }
        }
    }

    /**
     * Called to set SDK to offline or online mode.
     *
     */
    public void switchToOfflineMode() {
        if (!checkActivityHandler(true, "offline mode", "online mode")) {
            this.startOffline = true;
        } else {
            activityHandler.setOfflineMode(true);
        }
    }

    /**
     * Called to set SDK to online or online mode.
     *
     */
    public void switchBackToOnlineMode() {
        if (!checkActivityHandler(false, "offline mode", "online mode")) {
            this.startOffline = false;
        } else {
            activityHandler.setOfflineMode(false);
        }
    }

    /**
     * Called to add global callback parameter that will be sent with each session and event.
     *
     * @param key   Global callback parameter key
     * @param value Global callback parameter value
     */
    public void addGlobalCallbackParameter(final String key, final String value) {
        if (checkActivityHandler("adding global callback parameter", true)) {
            activityHandler.addGlobalCallbackParameter(key, value);
            return;
        }

        preLaunchActions.preLaunchActionsArray.add(new IRunActivityHandler() {
            @Override
            public void run(final ActivityHandler activityHandler) {
                activityHandler.addGlobalCallbackParameterI(key, value);
            }
        });
    }

    /**
     * Called to add global partner parameter that will be sent with each session and event.
     *
     * @param key   Global partner parameter key
     * @param value Global partner parameter value
     */
    public void addGlobalPartnerParameter(final String key, final String value) {
        if (checkActivityHandler("adding global partner parameter", true)) {
            activityHandler.addGlobalPartnerParameter(key, value);
            return;
        }
        preLaunchActions.preLaunchActionsArray.add(new IRunActivityHandler() {
            @Override
            public void run(final ActivityHandler activityHandler) {
                activityHandler.addGlobalPartnerParameterI(key, value);
            }
        });
    }

    /**
     * Called to remove global callback parameter from session and event packages.
     *
     * @param key Global callback parameter key
     */
    public void removeGlobalCallbackParameter(final String key) {
        if (checkActivityHandler("removing global callback parameter", true)) {
            activityHandler.removeGlobalCallbackParameter(key);
            return;
        }
        preLaunchActions.preLaunchActionsArray.add(new IRunActivityHandler() {
            @Override
            public void run(final ActivityHandler activityHandler) {
                activityHandler.removeGlobalCallbackParameterI(key);
            }
        });
    }

    /**
     * Called to remove global partner parameter from session and event packages.
     *
     * @param key Global partner parameter key
     */
    public void removeGlobalPartnerParameter(final String key) {
        if (checkActivityHandler("removing global partner parameter", true)) {
            activityHandler.removeGlobalPartnerParameter(key);
            return;
        }
        preLaunchActions.preLaunchActionsArray.add(new IRunActivityHandler() {
            @Override
            public void run(final ActivityHandler activityHandler) {
                activityHandler.removeGlobalPartnerParameterI(key);
            }
        });
    }

    /**
     * Called to remove all added global callback parameters.
     */
    public void removeGlobalCallbackParameters() {
        if (checkActivityHandler("resetting global callback parameters", true)) {
            activityHandler.removeGlobalCallbackParameters();
            return;
        }
        preLaunchActions.preLaunchActionsArray.add(new IRunActivityHandler() {
            @Override
            public void run(final ActivityHandler activityHandler) {
                activityHandler.removeGlobalCallbackParametersI();
            }
        });
    }

    /**
     * Called to remove all added global partner parameters.
     */
    public void removeGlobalPartnerParameters() {
        if (checkActivityHandler("resetting global partner parameters", true)) {
            activityHandler.removeGlobalPartnerParameters();
            return;
        }
        preLaunchActions.preLaunchActionsArray.add(new IRunActivityHandler() {
            @Override
            public void run(final ActivityHandler activityHandler) {
                activityHandler.removeGlobalPartnerParametersI();
            }
        });
    }

    /**
     * Called to teardown SDK state.
     * Used only for Adjust tests, shouldn't be used in client apps.
     */
    public void teardown() {
        if (!checkActivityHandler("teardown")) {
            return;
        }
        activityHandler.teardown();
        activityHandler = null;
    }

    /**
     * Called to set user's push notifications token.
     *
     * @param token   Push notifications token
     * @param context Application context
     */
    public void setPushToken(final String token, final Context context) {
        savePushToken(token, context);
        if (checkActivityHandler("push token", true)) {
            if (activityHandler.isEnabled()) {
                activityHandler.setPushToken(token, true);
            }
        }
    }

    /**
     * Called to forget the user in accordance with GDPR law.
     *
     * @param context Application context
     */
    public void gdprForgetMe(final Context context) {
        saveGdprForgetMe(context);
        if (checkActivityHandler("gdpr", true)) {
            if (activityHandler.isEnabled()) {
                activityHandler.gdprForgetMe();
            }
        }
    }

    public void trackThirdPartySharing(final AdjustThirdPartySharing adjustThirdPartySharing) {
        if (!checkActivityHandler("third party sharing", true)) {
            preLaunchActions.preLaunchAdjustThirdPartySharingArray.add(adjustThirdPartySharing);
            return;
        }

        activityHandler.trackThirdPartySharing(adjustThirdPartySharing);
    }

    public void trackMeasurementConsent(final boolean consentMeasurement) {
        if (!checkActivityHandler("measurement consent", true)) {
            preLaunchActions.lastMeasurementConsentTracked = consentMeasurement;
            return;
        }

        activityHandler.trackMeasurementConsent(consentMeasurement);
    }

    /**
     * Track ad revenue from a source provider
     *
     * @param adjustAdRevenue Adjust ad revenue information like source, revenue, currency etc
     */
    public void trackAdRevenue(final AdjustAdRevenue adjustAdRevenue) {
        if (!checkActivityHandler("trackAdRevenue")) {
            return;
        }

        activityHandler.trackAdRevenue(adjustAdRevenue);
    }

    /**
     * Track subscription from Google Play.
     *
     * @param subscription AdjustPlayStoreSubscription object to be tracked
     */
    public void trackPlayStoreSubscription(AdjustPlayStoreSubscription subscription) {
        if (!checkActivityHandler("trackPlayStoreSubscription")) {
            return;
        }
        activityHandler.trackPlayStoreSubscription(subscription);
    }

    /**
     * Called to get value of unique Adjust device identifier.
     *
     * @param onAdidReadListener Callback to get triggered once identifier is obtained.
     */
    public void getAdid(OnAdidReadListener onAdidReadListener) {
        if (!checkActivityHandler("getAdid")) {
            cachedAdidReadCallbacks.add(onAdidReadListener);
            return;
        }
        activityHandler.getAdid(onAdidReadListener);
    }

    /**
     * Called to get user's current attribution value.
     *
     * @param attributionReadListener Callback to get triggered once attribution is obtained.
     */
    public void getAttribution(OnAttributionReadListener attributionReadListener) {
        if (!checkActivityHandler("getAttribution")) {
            cachedAttributionReadCallbacks.add(attributionReadListener);
            return;
        }
        activityHandler.getAttribution(attributionReadListener);
    }

    /**
     * Called to get native SDK version string.
     *
     * @param onSdkVersionReadListener Callback to get triggered once SDK version is obtained.
     */
    public void getSdkVersion(final OnSdkVersionReadListener onSdkVersionReadListener) {
        new AsyncTaskExecutor<Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                return Util.getSdkVersion();
            }

            @Override
            protected void onPostExecute(String sdkVersion) {
                onSdkVersionReadListener.onSdkVersionRead(sdkVersion);
            }
        }.execute();
    }

    /**
     * Called to get Google Install Referrer.
     *
     * @param context Application context
     * @param onGooglePlayInstallReferrerReadListener Callback to obtain install referrer.
     */
    public void getGooglePlayInstallReferrer(final Context context, final OnGooglePlayInstallReferrerReadListener onGooglePlayInstallReferrerReadListener) {
        InstallReferrer installReferrer = new InstallReferrer(context, new InstallReferrerReadListener() {
            @Override
            public void onInstallReferrerRead(ReferrerDetails referrerDetails, String referrerApi) {
                onGooglePlayInstallReferrerReadListener.onInstallReferrerRead(new GooglePlayInstallReferrerDetails(referrerDetails));
            }

            @Override
            public void onFail(String message) {
                onGooglePlayInstallReferrerReadListener.onFail(message);
            }
        });
        installReferrer.startConnection();
    }


    /**
     * Called to get value of Amazon Advertising Identifier.
     *
     * @param context                  Application context
     * @param onAmazonAdIdReadListener Callback to get triggered once identifier is obtained
     */
    public void getAmazonAdId(final Context context, final OnAmazonAdIdReadListener onAmazonAdIdReadListener) {
        DeviceInfo.getFireAdvertisingIdBypassConditions(context.getContentResolver(),onAmazonAdIdReadListener);
    }

    /**
     * Verify in app purchase from Google Play.
     *
     * @param purchase AdjustPurchase object to be verified
     * @param callback Callback to be pinged with the verification results
     */
    public void verifyPlayStorePurchase(final AdjustPlayStorePurchase purchase,
                                        final OnPurchaseVerificationFinishedListener callback) {
        if (!checkActivityHandler("verifyPurchase")) {
            AdjustPurchaseVerificationResult result = new AdjustPurchaseVerificationResult(
                    "not_verified",
                    100,
                    "SDK needs to be initialized before making purchase verification request");
            callback.onVerificationFinished(result);
            return;
        }
        activityHandler.verifyPlayStorePurchase(purchase, callback);
    }

    /**
     * Verify in app purchase from Google Play and track Adjust event associated with it.
     *
     * @param event    AdjustEvent to be tracked
     * @param callback Callback to be pinged with the verification results
     */
    public void verifyAndTrackPlayStorePurchase(AdjustEvent event, OnPurchaseVerificationFinishedListener callback) {
        if (!checkActivityHandler("verifyAndTrack")) {
            if (callback != null) {
                AdjustPurchaseVerificationResult result = new AdjustPurchaseVerificationResult(
                        "not_verified",
                        100,
                        "SDK needs to be initialized before making purchase verification request");
                callback.onVerificationFinished(result);
            }
            return;
        }
        activityHandler.verifyAndTrackPlayStorePurchase(event, callback);
    }

    /**
     * Called to get last opened deeplink.
     *
     * @param context Application context
     * @param onLastDeeplinkReadListener Callback to obtain last opened deeplink.
     */
    public void getLastDeeplink(final Context context, final OnLastDeeplinkReadListener onLastDeeplinkReadListener) {
        new AsyncTaskExecutor<Void, Uri>() {
            @Override
            protected Uri doInBackground(Void... voids) {
                String cachedDeeplink = getCachedDeeplink(context);
                try {
                    return Uri.parse(cachedDeeplink);
                } catch (Exception e) {
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Uri deeplink) {
                onLastDeeplinkReadListener.onLastDeeplinkRead(deeplink);
            }
        }.execute();
    }

    /**
     * Check if ActivityHandler instance is set or not.
     *
     * @return boolean indicating whether ActivityHandler instance is set or not
     */
    private boolean checkActivityHandler(final String action) {
        return checkActivityHandler(action, false);
    }

    /**
     * Check if ActivityHandler instance is set or not.
     *
     * @param status       Is SDK enabled or not
     * @param trueMessage  Log message to display in case SDK is enabled
     * @param falseMessage Log message to display in case SDK is disabled
     * @return boolean indicating whether ActivityHandler instance is set or not
     */
    private boolean checkActivityHandler(final boolean status, final String trueMessage, final String falseMessage) {
        if (status) {
            return checkActivityHandler(trueMessage, true);
        } else {
            return checkActivityHandler(falseMessage, true);
        }
    }

    /**
     * Check if ActivityHandler instance is set or not.
     *
     * @param action Log message to indicate action that was asked to perform when SDK was disabled
     * @return boolean indicating whether ActivityHandler instance is set or not
     */
    private boolean checkActivityHandler(final String action, final boolean actionSaved) {
        if (activityHandler != null) {
            return true;
        }

        if (action == null) {
            AdjustFactory.getLogger().error("Adjust not initialized correctly");
            return false;
        }

        if (actionSaved) {
            AdjustFactory.getLogger().warn(
                    "Adjust not initialized, but %s saved for launch",
                    action);
        } else {
            AdjustFactory.getLogger().warn(
                    "Adjust not initialized, can't perform %s",
                    action);
        }
        return false;
    }

    /**
     * Save referrer to shared preferences.
     *
     * @param clickTime   Referrer click time
     * @param rawReferrer Raw referrer content
     * @param context     Application context
     */
    private void saveRawReferrer(final String rawReferrer, final long clickTime, final Context context) {
        SharedPreferencesManager.getDefaultInstance(context).saveRawReferrer(rawReferrer, clickTime);
    }

    /**
     * Save preinstall referrer to shared preferences.
     *
     * @param referrer Preinstall referrer content
     * @param context  Application context
     */
    private void savePreinstallReferrer(final String referrer, final Context context) {
        SharedPreferencesManager.getDefaultInstance(context).savePreinstallReferrer(referrer);
    }

    /**
     * Save push token to shared preferences.
     *
     * @param pushToken Push notifications token
     * @param context   Application context
     */
    private void savePushToken(final String pushToken, final Context context) {
        SharedPreferencesManager.getDefaultInstance(context).savePushToken(pushToken);
    }

    /**
     * Save GDPR forget me choice to shared preferences.
     *
     * @param context Application context
     */
    private void saveGdprForgetMe(final Context context) {
        SharedPreferencesManager.getDefaultInstance(context).setGdprForgetMe();
    }

    /**
     * Save deeplink to shared preferences.
     *
     * @param deeplink  Deeplink Uri object
     * @param clickTime Time when processDeeplink(Uri, Context) method was called
     * @param context   Application context
     */
    private void saveDeeplink(final Uri deeplink, final long clickTime, final Context context) {
        SharedPreferencesManager.getDefaultInstance(context).saveDeeplink(deeplink, clickTime);
    }

    /**
     * Cache deeplink to shared preferences.
     *
     * @param deeplink  Deeplink Uri object
     * @param context   Application context
     */
    private void cacheDeeplink(final Uri deeplink, final Context context) {
        SharedPreferencesManager.getDefaultInstance(context).cacheDeeplink(deeplink);
    }

    /**
     * Get cached deeplink from shared preferences.
     *
     * @param context   Application context
     */
    private String getCachedDeeplink(final Context context) {
        return SharedPreferencesManager.getDefaultInstance(context).getCachedDeeplink();
    }

    /**
     * Flag stored referrers as still not sent.
     *
     * @param context Application context
     */
    private void setSendingReferrersAsNotSent(final Context context) {
        ThreadExecutor executor = new SingleThreadCachedScheduler("AdjustInstance");
        executor.submit(() ->  SharedPreferencesManager.getDefaultInstance(context).setSendingReferrersAsNotSent());
    }

    /**
     * Check if AdjustInstance enable flag is set or not.
     *
     * @return boolean indicating whether AdjustInstance is enabled or not
     */
    private boolean isInstanceEnabled() {
        return this.startEnabled == null || this.startEnabled;
    }

    /**
     * Used for testing purposes only. Do NOT use this method.
     *
     * @param testOptions Adjust integration tests options
     */
    public void setTestOptions(AdjustTestOptions testOptions) {
        if (testOptions.basePath != null) {
            this.basePath = testOptions.basePath;
        }
        if (testOptions.gdprPath != null) {
            this.gdprPath = testOptions.gdprPath;
        }
        if (testOptions.subscriptionPath != null) {
            this.subscriptionPath = testOptions.subscriptionPath;
        }
        if (testOptions.purchaseVerificationPath != null) {
            this.purchaseVerificationPath = testOptions.purchaseVerificationPath;
        }
        if (testOptions.baseUrl != null) {
            AdjustFactory.setBaseUrl(testOptions.baseUrl);
        }
        if (testOptions.gdprUrl != null) {
            AdjustFactory.setGdprUrl(testOptions.gdprUrl);
        }
        if (testOptions.subscriptionUrl != null) {
            AdjustFactory.setSubscriptionUrl(testOptions.subscriptionUrl);
        }
        if (testOptions.purchaseVerificationUrl != null) {
            AdjustFactory.setPurchaseVerificationUrl(testOptions.purchaseVerificationUrl);
        }
        if (testOptions.timerIntervalInMilliseconds != null) {
            AdjustFactory.setTimerInterval(testOptions.timerIntervalInMilliseconds);
        }
        if (testOptions.timerStartInMilliseconds != null) {
            AdjustFactory.setTimerStart(testOptions.timerIntervalInMilliseconds);
        }
        if (testOptions.sessionIntervalInMilliseconds != null) {
            AdjustFactory.setSessionInterval(testOptions.sessionIntervalInMilliseconds);
        }
        if (testOptions.subsessionIntervalInMilliseconds != null) {
            AdjustFactory.setSubsessionInterval(testOptions.subsessionIntervalInMilliseconds);
        }
        if (testOptions.tryInstallReferrer != null) {
            AdjustFactory.setTryInstallReferrer(testOptions.tryInstallReferrer);
        }
        if (testOptions.noBackoffWait != null) {
            AdjustFactory.setPackageHandlerBackoffStrategy(BackoffStrategy.NO_WAIT);
            AdjustFactory.setSdkClickBackoffStrategy(BackoffStrategy.NO_WAIT);
        }
        if (testOptions.ignoreSystemLifecycleBootstrap != null) {
            AdjustFactory.setIgnoreSystemLifecycleBootstrap(
              testOptions.ignoreSystemLifecycleBootstrap.booleanValue());
        }
    }
}
