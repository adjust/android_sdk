package com.adjust.sdk;

import android.net.Uri;
import android.content.Context;

import org.json.JSONObject;

import java.util.List;
import java.util.ArrayList;

/**
 * Class used to forward instructions to SDK which user gives as part of Adjust class interface.
 *
 * @author Pedro Silva (@nonelse)
 * @since 12th April 2014
 */
public class AdjustInstance {
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

    /**
     * Array of actions that were requested before SDK initialisation.
     */
    private List<IRunActivityHandler> preLaunchActionsArray;

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
     * Called upon SDK initialisation.
     *
     * @param adjustConfig AdjustConfig object used for SDK initialisation
     */
    public void onCreate(final AdjustConfig adjustConfig) {
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

        adjustConfig.preLaunchActionsArray = preLaunchActionsArray;
        adjustConfig.pushToken = pushToken;
        adjustConfig.startEnabled = startEnabled;
        adjustConfig.startOffline = startOffline;
        adjustConfig.basePath = this.basePath;
        adjustConfig.gdprPath = this.gdprPath;
        adjustConfig.subscriptionPath = this.subscriptionPath;

        activityHandler = AdjustFactory.getActivityHandler(adjustConfig);
        setSendingReferrersAsNotSent(adjustConfig.context);
    }

    /**
     * Called to track event.
     *
     * @param event AdjustEvent object to be tracked
     */
    public void trackEvent(final AdjustEvent event) {
        if (!checkActivityHandler()) {
            return;
        }
        activityHandler.trackEvent(event);
    }

    /**
     * Called upon each Activity's onResume() method call.
     */
    public void onResume() {
        if (!checkActivityHandler()) {
            return;
        }
        activityHandler.onResume();
    }

    /**
     * Called upon each Activity's onPause() method call.
     */
    public void onPause() {
        if (!checkActivityHandler()) {
            return;
        }
        activityHandler.onPause();
    }

    /**
     * Called to disable/enable SDK.
     *
     * @param enabled boolean indicating whether SDK should be enabled or disabled
     */
    public void setEnabled(final boolean enabled) {
        this.startEnabled = enabled;
        if (checkActivityHandler(enabled, "enabled mode", "disabled mode")) {
            activityHandler.setEnabled(enabled);
        }
    }

    /**
     * Get information if SDK is enabled or not.
     *
     * @return boolean indicating whether SDK is enabled or not
     */
    public boolean isEnabled() {
        if (!checkActivityHandler()) {
            return isInstanceEnabled();
        }
        return activityHandler.isEnabled();
    }

    /**
     * Called to process deep link.
     *
     * @param url Deep link URL to process
     */
    public void appWillOpenUrl(final Uri url) {
        if (!checkActivityHandler()) {
            return;
        }
        long clickTime = System.currentTimeMillis();
        activityHandler.readOpenUrl(url, clickTime);
    }

    /**
     * Called to process deep link.
     *
     * @param url     Deep link URL to process
     * @param context Application context
     */
    public void appWillOpenUrl(final Uri url, final Context context) {
        long clickTime = System.currentTimeMillis();
        if (!checkActivityHandler()) {
            SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(context);
            sharedPreferencesManager.saveDeeplink(url, clickTime);
            return;
        }

        activityHandler.readOpenUrl(url, clickTime);
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
            return;
        }

        saveRawReferrer(rawReferrer, clickTime, context);
        if (checkActivityHandler("referrer")) {
            if (activityHandler.isEnabled()) {
                activityHandler.sendReftagReferrer();
            }
        }
    }

    /**
     * Called to set SDK to offline or online mode.
     *
     * @param enabled boolean indicating should SDK be in offline mode (true) or not (false)
     */
    public void setOfflineMode(final boolean enabled) {
        if (!checkActivityHandler(enabled, "offline mode", "online mode")) {
            this.startOffline = enabled;
        } else {
            activityHandler.setOfflineMode(enabled);
        }
    }

    /**
     * Called if SDK initialisation was delayed and you would like to stop waiting for timer.
     */
    public void sendFirstPackages() {
        if (!checkActivityHandler()) {
            return;
        }
        activityHandler.sendFirstPackages();
    }

    /**
     * Called to add global callback parameter that will be sent with each session and event.
     *
     * @param key   Global callback parameter key
     * @param value Global callback parameter value
     */
    public void addSessionCallbackParameter(final String key, final String value) {
        if (checkActivityHandler("adding session callback parameter")) {
            activityHandler.addSessionCallbackParameter(key, value);
            return;
        }
        if (preLaunchActionsArray == null) {
            preLaunchActionsArray = new ArrayList<IRunActivityHandler>();
        }
        preLaunchActionsArray.add(new IRunActivityHandler() {
            @Override
            public void run(final ActivityHandler activityHandler) {
                activityHandler.addSessionCallbackParameterI(key, value);
            }
        });
    }

    /**
     * Called to add global partner parameter that will be sent with each session and event.
     *
     * @param key   Global partner parameter key
     * @param value Global partner parameter value
     */
    public void addSessionPartnerParameter(final String key, final String value) {
        if (checkActivityHandler("adding session partner parameter")) {
            activityHandler.addSessionPartnerParameter(key, value);
            return;
        }
        if (preLaunchActionsArray == null) {
            preLaunchActionsArray = new ArrayList<IRunActivityHandler>();
        }
        preLaunchActionsArray.add(new IRunActivityHandler() {
            @Override
            public void run(final ActivityHandler activityHandler) {
                activityHandler.addSessionPartnerParameterI(key, value);
            }
        });
    }

    /**
     * Called to remove global callback parameter from session and event packages.
     *
     * @param key Global callback parameter key
     */
    public void removeSessionCallbackParameter(final String key) {
        if (checkActivityHandler("removing session callback parameter")) {
            activityHandler.removeSessionCallbackParameter(key);
            return;
        }
        if (preLaunchActionsArray == null) {
            preLaunchActionsArray = new ArrayList<IRunActivityHandler>();
        }
        preLaunchActionsArray.add(new IRunActivityHandler() {
            @Override
            public void run(final ActivityHandler activityHandler) {
                activityHandler.removeSessionCallbackParameterI(key);
            }
        });
    }

    /**
     * Called to remove global partner parameter from session and event packages.
     *
     * @param key Global partner parameter key
     */
    public void removeSessionPartnerParameter(final String key) {
        if (checkActivityHandler("removing session partner parameter")) {
            activityHandler.removeSessionPartnerParameter(key);
            return;
        }
        if (preLaunchActionsArray == null) {
            preLaunchActionsArray = new ArrayList<IRunActivityHandler>();
        }
        preLaunchActionsArray.add(new IRunActivityHandler() {
            @Override
            public void run(final ActivityHandler activityHandler) {
                activityHandler.removeSessionPartnerParameterI(key);
            }
        });
    }

    /**
     * Called to remove all added global callback parameters.
     */
    public void resetSessionCallbackParameters() {
        if (checkActivityHandler("resetting session callback parameters")) {
            activityHandler.resetSessionCallbackParameters();
            return;
        }
        if (preLaunchActionsArray == null) {
            preLaunchActionsArray = new ArrayList<IRunActivityHandler>();
        }
        preLaunchActionsArray.add(new IRunActivityHandler() {
            @Override
            public void run(final ActivityHandler activityHandler) {
                activityHandler.resetSessionCallbackParametersI();
            }
        });
    }

    /**
     * Called to remove all added global partner parameters.
     */
    public void resetSessionPartnerParameters() {
        if (checkActivityHandler("resetting session partner parameters")) {
            activityHandler.resetSessionPartnerParameters();
            return;
        }
        if (preLaunchActionsArray == null) {
            preLaunchActionsArray = new ArrayList<IRunActivityHandler>();
        }
        preLaunchActionsArray.add(new IRunActivityHandler() {
            @Override
            public void run(final ActivityHandler activityHandler) {
                activityHandler.resetSessionPartnerParametersI();
            }
        });
    }

    /**
     * Called to teardown SDK state.
     * Used only for Adjust tests, shouldn't be used in client apps.
     */
    public void teardown() {
        if (!checkActivityHandler()) {
            return;
        }
        activityHandler.teardown();
        activityHandler = null;
    }

    /**
     * Called to set user's push notifications token.
     *
     * @param token Push notifications token
     */
    public void setPushToken(final String token) {
        if (!checkActivityHandler("push token")) {
            this.pushToken = token;
        } else {
            activityHandler.setPushToken(token, false);
        }
    }

    /**
     * Called to set user's push notifications token.
     *
     * @param token   Push notifications token
     * @param context Application context
     */
    public void setPushToken(final String token, final Context context) {
        savePushToken(token, context);
        if (checkActivityHandler("push token")) {
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
        if (checkActivityHandler("gdpr")) {
            if (activityHandler.isEnabled()) {
                activityHandler.gdprForgetMe();
            }
        }
    }

    /**
     * Called to disable the third party sharing.
     *
     * @param context Application context
     */
    public void disableThirdPartySharing(final Context context) {
        if (!checkActivityHandler("disable third party sharing")) {
            saveDisableThirdPartySharing(context);
            return;
        }

        activityHandler.disableThirdPartySharing();
    }

    /**
     * Track ad revenue from a source provider
     *
     * @param source Source of ad revenue information, see AdjustConfig.AD_REVENUE_* for some possible sources
     * @param adRevenueJson JsonObject content of the ad revenue information
     */
    public void trackAdRevenue(String source, JSONObject adRevenueJson) {
        if (!checkActivityHandler()) {
            return;
        }
        activityHandler.trackAdRevenue(source, adRevenueJson);
    }

    /**
     * Track subscription from Google Play.
     *
     * @param subscription AdjustPlayStoreSubscription object to be tracked
     */
    public void trackPlayStoreSubscription(AdjustPlayStoreSubscription subscription) {
        if (!checkActivityHandler()) {
            return;
        }
        activityHandler.trackPlayStoreSubscription(subscription);
    }

    /**
     * Called to get value of unique Adjust device identifier.
     *
     * @return Unique Adjust device indetifier
     */
    public String getAdid() {
        if (!checkActivityHandler()) {
            return null;
        }
        return activityHandler.getAdid();
    }

    /**
     * Called to get user's current attribution value.
     *
     * @return AdjustAttribution object with current attribution value
     */
    public AdjustAttribution getAttribution() {
        if (!checkActivityHandler()) {
            return null;
        }
        return activityHandler.getAttribution();
    }

    /**
     * Called to get native SDK version string.
     *
     * @return Native SDK version string.
     */
    public String getSdkVersion() {
        return Util.getSdkVersion();
    }

    /**
     * Check if ActivityHandler instance is set or not.
     *
     * @return boolean indicating whether ActivityHandler instance is set or not
     */
    private boolean checkActivityHandler() {
        return checkActivityHandler(null);
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
            return checkActivityHandler(trueMessage);
        } else {
            return checkActivityHandler(falseMessage);
        }
    }

    /**
     * Check if ActivityHandler instance is set or not.
     *
     * @param savedForLaunchWarningSuffixMessage Log message to indicate action that was asked when SDK was disabled
     * @return boolean indicating whether ActivityHandler instance is set or not
     */
    private boolean checkActivityHandler(final String savedForLaunchWarningSuffixMessage) {
        if (activityHandler == null) {
            if (savedForLaunchWarningSuffixMessage != null) {
                AdjustFactory.getLogger().warn(
                        "Adjust not initialized, but %s saved for launch",
                        savedForLaunchWarningSuffixMessage);
            } else {
                AdjustFactory.getLogger().error("Adjust not initialized correctly");
            }
            return false;
        } else {
            return true;
        }
    }

    /**
     * Save referrer to shared preferences.
     *
     * @param clickTime   Referrer click time
     * @param rawReferrer Raw referrer content
     * @param context     Application context
     */
    private void saveRawReferrer(final String rawReferrer, final long clickTime, final Context context) {
        Runnable command = new Runnable() {
            @Override
            public void run() {
                SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(context);
                sharedPreferencesManager.saveRawReferrer(rawReferrer, clickTime);
            }
        };
        Util.runInBackground(command);
    }

    /**
     * Save push token to shared preferences.
     *
     * @param pushToken Push notifications token
     * @param context   Application context
     */
    private void savePushToken(final String pushToken, final Context context) {
        Runnable command = new Runnable() {
            @Override
            public void run() {
                SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(context);
                sharedPreferencesManager.savePushToken(pushToken);
            }
        };
        Util.runInBackground(command);
    }

    /**
     * Save GDPR forget me choice to shared preferences.
     *
     * @param context Application context
     */
    private void saveGdprForgetMe(final Context context) {
        Runnable command = new Runnable() {
            @Override
            public void run() {
                SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(context);
                sharedPreferencesManager.setGdprForgetMe();
            }
        };
        Util.runInBackground(command);
    }

    /**
     * Save disable third party sharing choice to shared preferences.
     *
     * @param context Application context
     */
    private void saveDisableThirdPartySharing(final Context context) {
        Runnable command = new Runnable() {
            @Override
            public void run() {
                SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(context);
                sharedPreferencesManager.setDisableThirdPartySharing();
            }
        };
        Util.runInBackground(command);
    }

    /**
     * Flag stored referrers as still not sent.
     *
     * @param context Application context
     */
    private void setSendingReferrersAsNotSent(final Context context) {
        Runnable command = new Runnable() {
            @Override
            public void run() {
                SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(context);
                sharedPreferencesManager.setSendingReferrersAsNotSent();

            }
        };
        Util.runInBackground(command);
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
        if (testOptions.baseUrl != null) {
            AdjustFactory.setBaseUrl(testOptions.baseUrl);
        }
        if (testOptions.gdprUrl != null) {
            AdjustFactory.setGdprUrl(testOptions.gdprUrl);
        }
        if (testOptions.subscriptionUrl != null) {
            AdjustFactory.setSubscriptionUrl(testOptions.subscriptionUrl);
        }
        if (testOptions.useTestConnectionOptions != null && testOptions.useTestConnectionOptions.booleanValue()) {
            AdjustFactory.useTestConnectionOptions();
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
        if (testOptions.enableSigning != null && testOptions.enableSigning) {
            AdjustFactory.enableSigning();
        }
        if (testOptions.disableSigning != null && testOptions.disableSigning) {
            AdjustFactory.disableSigning();
        }
    }
}
