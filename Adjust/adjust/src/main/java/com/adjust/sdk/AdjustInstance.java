package com.adjust.sdk;

import android.net.Uri;
import android.content.Context;

import java.util.List;
import java.util.ArrayList;

/**
 * Class used to forward instructions to SDK which user gives as part of Adjust class interface.
 *
 * @author Pedro Silva (nonelse)
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
    private ActivityHandler activityHandler;

    /**
     * Array of actions that were requested before SDK initialisation.
     */
    private List<IRunActivityHandler> preLaunchActionsArray;

    /**
     * Called upon SDK initialisation.
     *
     * @param adjustConfig AdjustConfig object used for SDK initialisation
     */
    public void onCreate(final AdjustConfig adjustConfig) {
        if (activityHandler != null) {
            AdjustFactory.getLogger().error("Adjust already initialized");
            return;
        }

        adjustConfig.preLaunchActionsArray = preLaunchActionsArray;
        adjustConfig.pushToken = pushToken;
        adjustConfig.startEnabled = startEnabled;
        adjustConfig.startOffline = startOffline;

        activityHandler = ActivityHandler.getInstance(adjustConfig);

        // Scan for referrers.
        scanForReferrers(adjustConfig.context);
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
            return false;
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
     * Called to process referrer information sent with INSTALL_REFERRER intent.
     *
     * @param referrer Referrer content
     * @param context  Application context
     */
    public void sendReferrer(final String referrer, final Context context) {
        long clickTime = System.currentTimeMillis();

        // Check for referrer validity. If invalid, return.
        if (referrer == null || referrer.length() == 0) {
            return;
        }

        // Don't save referrer if SDK is disabled.
        if (checkActivityHandler("referrer")) {
            if (activityHandler.isEnabled() && isInstanceEnabled()) {
                saveReferrer(clickTime, referrer, context);
                activityHandler.sendReferrer(referrer, clickTime);
            }
        } else {
            if (isInstanceEnabled()) {
                saveReferrer(clickTime, referrer, context);
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
     *
     * @param deleteState boolean indicating should internal Adjust files also be removed or not
     */
    public void teardown(final boolean deleteState) {
        if (!checkActivityHandler()) {
            return;
        }
        activityHandler.teardown(deleteState);
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
            activityHandler.setPushToken(token);
        }
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
                AdjustFactory.getLogger().warn("Adjust not initialized, but %s saved for launch",
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
     * @param clickTime Referrer click time
     * @param content   Referrer content
     * @param context   Application context
     */
    private void saveReferrer(final long clickTime, final String content, final Context context) {
        SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(context);
        sharedPreferencesManager.saveReferrer(clickTime, content);
    }

    /**
     * Check saved referrers before app being killed and if any found with isBeingSent set to true, revert it to false.
     *
     * @param context Application context
     */
    private void scanForReferrers(final Context context) {
        SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(context);
        sharedPreferencesManager.scanForSavedReferrers();
    }

    private boolean isInstanceEnabled() {
        return this.startEnabled == null || this.startEnabled;
    }
}
