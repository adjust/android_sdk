//
//  ActivityHandler.java
//  Adjust
//
//  Created by Christian Wellenbrock on 2013-06-25.
//  Copyright (c) 2013 adjust GmbH. All rights reserved.
//  See the file MIT-LICENSE for copying permission.
//

package com.adjust.sdk;

import static com.adjust.sdk.Constants.ACTIVITY_STATE_FILENAME;
import static com.adjust.sdk.Constants.ATTRIBUTION_FILENAME;
import static com.adjust.sdk.Constants.GLOBAL_CALLBACK_PARAMETERS_FILENAME;
import static com.adjust.sdk.Constants.GLOBAL_PARTNER_PARAMETERS_FILENAME;
import static com.adjust.sdk.Constants.REFERRER_API_HUAWEI_ADS;
import static com.adjust.sdk.Constants.REFERRER_API_HUAWEI_APP_GALLERY;
import static com.adjust.sdk.Constants.REFERRER_API_META;
import static com.adjust.sdk.Constants.REFERRER_API_SAMSUNG;
import static com.adjust.sdk.Constants.REFERRER_API_VIVO;
import static com.adjust.sdk.Constants.REFERRER_API_XIAOMI;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.adjust.sdk.SystemLifecycle.SystemLifecycleCallback;
import com.adjust.sdk.network.ActivityPackageSender;
import com.adjust.sdk.network.IActivityPackageSender;
import com.adjust.sdk.network.UtilNetworking;
import com.adjust.sdk.scheduler.SingleThreadCachedScheduler;
import com.adjust.sdk.scheduler.ThreadExecutor;
import com.adjust.sdk.scheduler.TimerCycle;
import com.adjust.sdk.scheduler.TimerOnce;

import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class ActivityHandler
  implements IActivityHandler, SystemLifecycleCallback
{
    private static long FOREGROUND_TIMER_INTERVAL;
    private static long FOREGROUND_TIMER_START;
    private static long BACKGROUND_TIMER_INTERVAL;
    private static long SESSION_INTERVAL;
    private static long SUBSESSION_INTERVAL;
    private static final String TIME_TRAVEL = "Time travel!";
    private static final String ACTIVITY_STATE_NAME = "Activity state";
    private static final String ATTRIBUTION_NAME = "Attribution";
    private static final String FOREGROUND_TIMER_NAME = "Foreground timer";
    private static final String BACKGROUND_TIMER_NAME = "Background timer";
    private static final String DELAY_START_TIMER_NAME = "Delay Start timer";
    private static final String GLOBAL_CALLBACK_PARAMETERS_NAME = "Global Callback parameters";
    private static final String GLOBAL_PARTNER_PARAMETERS_NAME = "Global Partner parameters";
    private static final String GLOBAL_PARAMETERS_NAME = "Global parameters";

    private ThreadExecutor executor;
    private IPackageHandler packageHandler;
    private ActivityState activityState;
    private ILogger logger;
    private TimerCycle foregroundTimer;
    private TimerOnce backgroundTimer;
    private InternalState internalState;
    private String basePath;
    private String gdprPath;
    private String subscriptionPath;

    private DeviceInfo deviceInfo;
    private AdjustConfig adjustConfig; // always valid after construction
    private AdjustAttribution attribution;
    private IAttributionHandler attributionHandler;
    private ISdkClickHandler sdkClickHandler;
    private IPurchaseVerificationHandler purchaseVerificationHandler;
    private GlobalParameters globalParameters;
    private InstallReferrer installReferrer;
    private OnDeeplinkResolvedListener cachedDeeplinkResolutionCallback;
    private ArrayList<OnAdidReadListener> cachedAdidReadCallbacks = new ArrayList<>();
    private SystemLifecycle systemLifecycle;
    private ArrayList<OnAttributionReadListener> cachedAttributionReadCallbacks = new ArrayList<>();

    @Override
    public void teardown() {
        if (backgroundTimer != null) {
            backgroundTimer.teardown();
        }
        if (foregroundTimer != null) {
            foregroundTimer.teardown();
        }
        if (executor != null) {
            executor.teardown();
        }
        if (packageHandler != null) {
            packageHandler.teardown();
        }
        if (attributionHandler != null) {
            attributionHandler.teardown();
        }
        if (sdkClickHandler != null) {
            sdkClickHandler.teardown();
        }
        if (purchaseVerificationHandler != null) {
            purchaseVerificationHandler.teardown();
        }
        if (globalParameters != null) {
            if (globalParameters.callbackParameters != null) {
                globalParameters.callbackParameters.clear();
            }
            if (globalParameters.partnerParameters != null) {
                globalParameters.partnerParameters.clear();
            }
        }

        teardownActivityStateS();
        teardownAttributionS();
        teardownAllGlobalParametersS();

        packageHandler = null;
        logger = null;
        foregroundTimer = null;
        executor = null;
        backgroundTimer = null;
        internalState = null;
        deviceInfo = null;
        adjustConfig = null;
        attributionHandler = null;
        sdkClickHandler = null;
        purchaseVerificationHandler = null;
        globalParameters = null;
    }

    static void deleteState(Context context) {
        deleteActivityState(context);
        deleteAttribution(context);
        deleteGlobalCallbackParameters(context);
        deleteGlobalPartnerParameters(context);

        SharedPreferencesManager.getDefaultInstance(context).clear();
    }

    public class InternalState {
        boolean enabled;
        boolean offline;
        boolean firstLaunch;
        boolean sessionResponseProcessed;
        boolean firstSdkStart;
        boolean preinstallHasBeenRead;
        Boolean foregroundOrElseBackground;

        public boolean isEnabled() {
            return enabled;
        }

        public boolean isDisabled() {
            return !enabled;
        }

        public boolean isOffline() {
            return offline;
        }

        public boolean isOnline() {
            return !offline;
        }

        public boolean isInBackground() {
            return foregroundOrElseBackground != null
              && ! foregroundOrElseBackground.booleanValue();
        }

        public boolean isInForeground() {
            return foregroundOrElseBackground != null
              && foregroundOrElseBackground.booleanValue();
        }

        public boolean isFirstLaunch() {
            return firstLaunch;
        }

        public boolean isNotFirstLaunch() {
            return !firstLaunch;
        }

        public boolean hasSessionResponseNotBeenProcessed() {
            return !sessionResponseProcessed;
        }

        public boolean hasFirstSdkStartOcurred() {
            return firstSdkStart;
        }

        public boolean hasFirstSdkStartNotOcurred() {
            return !firstSdkStart;
        }

        public boolean hasPreinstallBeenRead() {
            return preinstallHasBeenRead;
        }
    }

    // region SystemLifecycleCallback
    @Override public void onActivityLifecycle(final boolean foregroundOrElseBackground) {
        executor.submit(() -> {
            if (internalState.foregroundOrElseBackground != null
              && internalState.foregroundOrElseBackground.booleanValue()
                == foregroundOrElseBackground)
            {
                return;
            }
            // received foregroundOrElseBackground is strictly different from internal state one

            this.internalState.foregroundOrElseBackground = foregroundOrElseBackground;

            if (foregroundOrElseBackground) {
                onResumeI();
            } else {
                onPauseI();
            }
        });
    }
    // endregion

    private ActivityHandler(AdjustConfig adjustConfig) {
        init(adjustConfig);

        // init logger to be available everywhere
        logger = AdjustFactory.getLogger();

        logger.lockLogLevel();

        executor = new SingleThreadCachedScheduler("ActivityHandler");
        internalState = new InternalState();

        // enabled by default
        internalState.enabled = adjustConfig.startEnabled != null ? adjustConfig.startEnabled : true;
        // online by default
        internalState.offline = adjustConfig.startOffline;
        // does not have the session response by default
        internalState.sessionResponseProcessed = false;
        // does not have first start by default
        internalState.firstSdkStart = false;
        // preinstall has not been read by default
        internalState.preinstallHasBeenRead = false;

        executor.submit(() -> initI());
    }

    @Override
    public AdjustConfig getAdjustConfig() {
        return adjustConfig;
    }

    @Override
    public DeviceInfo getDeviceInfo() {
        return deviceInfo;
    }

    @Override
    public ActivityState getActivityState() {
        return activityState;
    }

    @Override
    public GlobalParameters getGlobalParameters() {
        return globalParameters;
    }

    @Override
    public void init(AdjustConfig adjustConfig) {
        this.adjustConfig = adjustConfig;
    }

    public static ActivityHandler getInstance(AdjustConfig adjustConfig) {
        if (adjustConfig == null) {
            AdjustFactory.getLogger().error("AdjustConfig missing");
            return null;
        }

        if (!adjustConfig.isValid()) {
            AdjustFactory.getLogger().error("AdjustConfig not initialized correctly");
            return null;
        }

        if (adjustConfig.processName != null) {
            int currentPid = android.os.Process.myPid();
            ActivityManager manager = (ActivityManager) adjustConfig.context.getSystemService(Context.ACTIVITY_SERVICE);

            if (manager == null) {
                return null;
            }

            List<ActivityManager.RunningAppProcessInfo> processInfoList = manager.getRunningAppProcesses();
            if (processInfoList == null) {
                return null;
            }

            for (ActivityManager.RunningAppProcessInfo processInfo : processInfoList) {
                if (processInfo.pid == currentPid) {
                    if (!processInfo.processName.equalsIgnoreCase(adjustConfig.processName)) {
                        AdjustFactory.getLogger().info("Skipping initialization in background process (%s)", processInfo.processName);
                        return null;
                    }
                    break;
                }
            }
        }

        ActivityHandler activityHandler = new ActivityHandler(adjustConfig);
        return activityHandler;
    }

    @Override
    public void onResume() {
        onActivityLifecycle(true);
    }
    public void onResumeI() {
        stopBackgroundTimerI();

        startForegroundTimerI();

        logger.verbose("Subsession start");

        startI();
    }

    @Override
    public void onPause() {
        onActivityLifecycle(false);
    }
    public void onPauseI() {
        stopForegroundTimerI();

        startBackgroundTimerI();

        logger.verbose("Subsession end");

        endI();
    }

    @Override
    public void trackEvent(final AdjustEvent event) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                if (internalState.hasFirstSdkStartNotOcurred()) {
                    logger.warn("Event tracked before first activity resumed.\n" +
                            "If it was triggered in the Application class, it might timestamp or even send an install long before the user opens the app.\n" +
                            "Please check https://github.com/adjust/android_sdk#can-i-trigger-an-event-at-application-launch for more information.");
                    startI();
                }
                trackEventI(event);
            }
        });
    }

    @Override
    public void finishedTrackingActivity(ResponseData responseData) {
        // redirect session responses to attribution handler to check for attribution information
        if (responseData instanceof SessionResponseData) {
            logger.debug("Finished tracking session");
            attributionHandler.checkSessionResponse((SessionResponseData)responseData);
            return;
        }
        // redirect sdk click responses to attribution handler to check for attribution information
        if (responseData instanceof SdkClickResponseData) {
            checkForInstallReferrerInfo((SdkClickResponseData) responseData);
            attributionHandler.checkSdkClickResponse((SdkClickResponseData)responseData);
            return;
        }
        // check if it's an event response
        if (responseData instanceof EventResponseData) {
            launchEventResponseTasks((EventResponseData)responseData);
            return;
        }
        // check if it's a purchase verification response
        if (responseData instanceof PurchaseVerificationResponseData) {
            launchPurchaseVerificationResponseTasks((PurchaseVerificationResponseData)responseData);
        }
    }

    @Override
    public void setEnabled(final boolean enabled) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                setEnabledI(enabled);
            }
        });
    }

    @Override
    public void setOfflineMode(final boolean offline) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                setOfflineModeI(offline);
            }
        });
    }

    @Override
    public boolean isEnabled() {
        return isEnabledI();
    }

    @Override
    public void isEnabled(OnIsEnabledListener onIsEnabledListener) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                onIsEnabledListener.onIsEnabledRead(isEnabledI());
            }
        });
    }

    private boolean isEnabledI() {
        if (activityState != null) {
            return activityState.enabled;
        } else {
            return internalState.isEnabled();
        }
    }

    @Override
    public void processDeeplink(final Uri url, final long clickTime) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                processDeeplinkI(url, clickTime);
            }
        });
    }

    @Override
    public void processAndResolveDeeplink(final Uri url, final long clickTime, final OnDeeplinkResolvedListener callback) {
        this.cachedDeeplinkResolutionCallback = callback;
        executor.submit(new Runnable() {
            @Override
            public void run() {
                processDeeplinkI(url, clickTime);
            }
        });
    }

    private void updateAdidI(final String adid) {
        if (adid == null) {
            return;
        }

        if (!adid.equals(activityState.adid)) {
            activityState.adid = adid;
            writeActivityStateI();
        }

        if (! cachedAdidReadCallbacks.isEmpty()) {
            final ArrayList<OnAdidReadListener> cachedAdidReadCallbacksCopy =
              new ArrayList<>(cachedAdidReadCallbacks);

            cachedAdidReadCallbacks.clear();
            new Handler(adjustConfig.context.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    for (OnAdidReadListener onAdidReadListener : cachedAdidReadCallbacksCopy) {
                        if (onAdidReadListener != null) {
                            onAdidReadListener.onAdidRead(adid);
                        }
                    }
                }
            });
        }
    }

    @Override
    public boolean updateAttributionI(AdjustAttribution attribution) {
        if (attribution == null) {
            return false;
        }

        if (activityState.askingAttribution) {
            return false;
        }

        if (! cachedAttributionReadCallbacks.isEmpty()) {
            final ArrayList<OnAttributionReadListener> cachedAttributionReadCallbacksCopy =
                    new ArrayList<>(cachedAttributionReadCallbacks);

            cachedAttributionReadCallbacks.clear();
            new Handler(adjustConfig.context.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    for (OnAttributionReadListener onAttributionReadListener : cachedAttributionReadCallbacksCopy) {
                        if (onAttributionReadListener != null) {
                            onAttributionReadListener.onAttributionRead(attribution);
                        }
                    }
                }
            });
        }

        if (attribution.equals(this.attribution)) {
            return false;
        }

        this.attribution = attribution;
        writeAttributionI();

        return true;
    }

    @Override
    public void setAskingAttribution(final boolean askingAttribution) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                setAskingAttributionI(askingAttribution);
            }
        });
    }

    @Override
    public void sendReftagReferrer() {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                sendReftagReferrerI();
            }
        });
    }

    @Override
    public void sendPreinstallReferrer() {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                sendPreinstallReferrerI();
            }
        });
    }

    @Override
    public void sendInstallReferrer(final ReferrerDetails referrerDetails,
                                    final String referrerApi) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                sendInstallReferrerI(referrerDetails, referrerApi);
            }
        });
    }

    @Override
    public void launchEventResponseTasks(final EventResponseData eventResponseData) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                launchEventResponseTasksI(eventResponseData);
            }
        });
    }

    @Override
    public void launchSdkClickResponseTasks(final SdkClickResponseData sdkClickResponseData) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                launchSdkClickResponseTasksI(sdkClickResponseData);
            }
        });
    }

    @Override
    public void launchSessionResponseTasks(final SessionResponseData sessionResponseData) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                launchSessionResponseTasksI(sessionResponseData);
            }
        });
    }

    @Override
    public void launchAttributionResponseTasks(final AttributionResponseData attributionResponseData) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                launchAttributionResponseTasksI(attributionResponseData);
            }
        });
    }

    @Override
    public void launchPurchaseVerificationResponseTasks(final PurchaseVerificationResponseData purchaseVerificationResponseData) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                launchPurchaseVerificationResponseTasksI(purchaseVerificationResponseData);
            }
        });
    }

    @Override
    public void addGlobalCallbackParameter(final String key, final String value) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                addGlobalCallbackParameterI(key, value);
            }
        });
    }

    @Override
    public void addGlobalPartnerParameter(final String key, final String value) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                addGlobalPartnerParameterI(key, value);
            }
        });
    }

    @Override
    public void removeGlobalCallbackParameter(final String key) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                removeGlobalCallbackParameterI(key);
            }
        });
    }

    @Override
    public void removeGlobalPartnerParameter(final String key) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                removeGlobalPartnerParameterI(key);
            }
        });
    }

    @Override
    public void removeGlobalCallbackParameters() {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                removeGlobalCallbackParametersI();
            }
        });
    }

    @Override
    public void removeGlobalPartnerParameters() {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                removeGlobalPartnerParametersI();
            }
        });
    }

    @Override
    public void setPushToken(final String token, final boolean preSaved) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                if (!preSaved) {
                    SharedPreferencesManager.getDefaultInstance(getContext()).savePushToken(token);
                }

                if (internalState.hasFirstSdkStartNotOcurred()) {
                    // No install has been tracked so far.
                    // Push token is saved, ready for the session package to pick it up.
                    return;
                } else {
                    setPushTokenI(token);
                }
            }
        });
    }

    @Override
    public void gdprForgetMe() {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                gdprForgetMeI();
            }
        });
    }

    @Override
    public void trackThirdPartySharing(final AdjustThirdPartySharing adjustThirdPartySharing) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                trackThirdPartySharingI(adjustThirdPartySharing);
            }
        });
    }

    @Override
    public void trackMeasurementConsent(final boolean consentMeasurement) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                trackMeasurementConsentI(consentMeasurement);
            }
        });
    }

    @Override
    public void trackAdRevenue(final AdjustAdRevenue adjustAdRevenue) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                trackAdRevenueI(adjustAdRevenue);
            }
        });
    }

    @Override
    public void trackPlayStoreSubscription(final AdjustPlayStoreSubscription subscription) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                trackPlayStoreSubscriptionI(subscription);
            }
        });
    }

    @Override
    public void gotOptOutResponse() {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                gotOptOutResponseI();
            }
        });
    }

    @Override
    public Context getContext() {
        return adjustConfig.context;
    }

    public void foregroundTimerFired() {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                foregroundTimerFiredI();
            }
        });
    }

    public void backgroundTimerFired() {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                backgroundTimerFiredI();
            }
        });
    }

    @Override
    public void getAdid(OnAdidReadListener callback) {
        if (activityState != null && activityState.adid != null) {
            new Handler(adjustConfig.context.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    callback.onAdidRead(activityState.adid);
                }
            });
        } else {
            if (activityState == null) {
                logger.warn("SDK needs to be initialized before getting adid");
            }
            cachedAdidReadCallbacks.add(callback);
        }
    }

    @Override
    public void getAttribution(OnAttributionReadListener onAttributionReadListener) {
        if (attribution != null) {
            new Handler(adjustConfig.context.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    onAttributionReadListener.onAttributionRead(attribution);
                }
            });
        }else {
            cachedAttributionReadCallbacks.add(onAttributionReadListener);
        }
    }

    @Override
    public void verifyPlayStorePurchase(final AdjustPlayStorePurchase purchase, final OnPurchaseVerificationFinishedListener callback) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                verifyPlayStorePurchaseI(purchase, callback);
            }
        });
    }

    @Override
    public void verifyAndTrackPlayStorePurchase(AdjustEvent event, OnPurchaseVerificationFinishedListener callback) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                verifyAndTrackPlayStorePurchaseI(event, callback);
            }
        });
    }

    @Override
    public InternalState getInternalState() {
        return internalState;
    }

    private void initI() {
        SESSION_INTERVAL = AdjustFactory.getSessionInterval();
        SUBSESSION_INTERVAL = AdjustFactory.getSubsessionInterval();
        // get timer values
        FOREGROUND_TIMER_INTERVAL = AdjustFactory.getTimerInterval();
        FOREGROUND_TIMER_START = AdjustFactory.getTimerStart();
        BACKGROUND_TIMER_INTERVAL = AdjustFactory.getTimerInterval();

        // has to be read in the background
        readAttributionI(adjustConfig.context);
        readActivityStateI(adjustConfig.context);

        globalParameters = new GlobalParameters();
        readGlobalCallbackParametersI(adjustConfig.context);
        readGlobalPartnerParametersI(adjustConfig.context);

        if (activityState != null) {
            activityState.setEventDeduplicationIdsMaxSize(adjustConfig.getEventDeduplicationIdsMaxSize());
        }

        if (adjustConfig.startEnabled != null) {
            adjustConfig.preLaunchActions.preLaunchActionsArray.add(new IRunActivityHandler() {
                @Override
                public void run(ActivityHandler activityHandler) {
                    activityHandler.setEnabledI(adjustConfig.startEnabled);
                }
            });
        }

        if (internalState.hasFirstSdkStartOcurred()) {
            internalState.enabled = activityState.enabled;
            internalState.firstLaunch = false;
        } else {
            internalState.firstLaunch = true; // first launch if activity state is null
        }

        readConfigFile(adjustConfig.context);

        deviceInfo = new DeviceInfo(adjustConfig);

        deviceInfo.reloadPlayIds(adjustConfig);
        if (deviceInfo.playAdId == null) {
            if (!Util.canReadPlayIds(adjustConfig)) {
                logger.info("Cannot read Google Play Services Advertising ID with COPPA or play store kids app enabled");
            } else {
                logger.warn("Unable to get Google Play Services Advertising ID at start time");
            }

            if (deviceInfo.androidId == null) {
                if (! Util.canReadNonPlayIds(adjustConfig)) {
                    logger.info("Cannot read non Play IDs with COPPA or play store kids app enabled");
                } else {
                    logger.error("Unable to get any Device IDs. Please check if Proguard is correctly set with Adjust SDK");
                }
            }
        } else {
            logger.info("Google Play Services Advertising ID read correctly at start time");
        }

        if (adjustConfig.defaultTracker != null) {
            logger.info("Default tracker: '%s'", adjustConfig.defaultTracker);
        }

        // push token
        if (adjustConfig.pushToken != null) {
            logger.info("Push token: '%s'", adjustConfig.pushToken);
            if (internalState.hasFirstSdkStartOcurred()) {
                // since sdk has already started, try to send current push token
                setPushToken(adjustConfig.pushToken, false);
            } else {
                // since sdk has not yet started, save current push token for when it does
                SharedPreferencesManager.getDefaultInstance(getContext()).savePushToken(adjustConfig.pushToken);
            }
        } else {
            // since sdk has already started, check if there is a saved push from previous runs
            if (internalState.hasFirstSdkStartOcurred()) {
                String savedPushToken = SharedPreferencesManager.getDefaultInstance(getContext()).getPushToken();
                if(savedPushToken!=null)
                    setPushToken(savedPushToken, true);
            }
        }

        // cached deeplink resolution callback
        if (this.cachedDeeplinkResolutionCallback == null) {
            this.cachedDeeplinkResolutionCallback = adjustConfig.cachedDeeplinkResolutionCallback;
        }

        handleAdidCallbackI();
        handleAttributionCallbackI();

        // GDPR
        if (internalState.hasFirstSdkStartOcurred()) {
            SharedPreferencesManager sharedPreferencesManager = SharedPreferencesManager.getDefaultInstance(getContext());
            if (sharedPreferencesManager.getGdprForgetMe()) {
                gdprForgetMe();
            }
        }

        foregroundTimer = new TimerCycle(
                new Runnable() {
                    @Override
                    public void run() {
                        foregroundTimerFired();
                    }
                }, FOREGROUND_TIMER_START, FOREGROUND_TIMER_INTERVAL, FOREGROUND_TIMER_NAME);

        // create background timer
        if (adjustConfig.isSendingInBackgroundEnabled) {
            logger.info("Send in background configured");

            backgroundTimer = new TimerOnce(new Runnable() {
                @Override
                public void run() {
                    backgroundTimerFired();
                }
            }, BACKGROUND_TIMER_NAME);
        }

        IActivityPackageSender packageHandlerActivitySender =
                new ActivityPackageSender(
                        adjustConfig.urlStrategyDomains,
                        adjustConfig.useSubdomains,
                        adjustConfig.basePath,
                        adjustConfig.gdprPath,
                        adjustConfig.subscriptionPath,
                        adjustConfig.purchaseVerificationPath,
                        deviceInfo.clientSdk,
                        adjustConfig.context);
        packageHandler = AdjustFactory.getPackageHandler(
                this,
                adjustConfig.context,
                toSendI(false),
                packageHandlerActivitySender);

        IActivityPackageSender attributionHandlerActivitySender =
                new ActivityPackageSender(
                        adjustConfig.urlStrategyDomains,
                        adjustConfig.useSubdomains,
                        adjustConfig.basePath,
                        adjustConfig.gdprPath,
                        adjustConfig.subscriptionPath,
                        adjustConfig.purchaseVerificationPath,
                        deviceInfo.clientSdk,
                        adjustConfig.context);

        attributionHandler = AdjustFactory.getAttributionHandler(
                this,
                toSendI(false),
                attributionHandlerActivitySender);

        IActivityPackageSender sdkClickHandlerActivitySender =
                new ActivityPackageSender(
                        adjustConfig.urlStrategyDomains,
                        adjustConfig.useSubdomains,
                        adjustConfig.basePath,
                        adjustConfig.gdprPath,
                        adjustConfig.subscriptionPath,
                        adjustConfig.purchaseVerificationPath,
                        deviceInfo.clientSdk,
                        adjustConfig.context);

        sdkClickHandler = AdjustFactory.getSdkClickHandler(
                this,
                toSendI(true),
                sdkClickHandlerActivitySender);

        IActivityPackageSender purchaseVerificationHandlerActivitySender =
                new ActivityPackageSender(
                        adjustConfig.urlStrategyDomains,
                        adjustConfig.useSubdomains,
                        adjustConfig.basePath,
                        adjustConfig.gdprPath,
                        adjustConfig.subscriptionPath,
                        adjustConfig.purchaseVerificationPath,
                        deviceInfo.clientSdk,
                        adjustConfig.context);

        purchaseVerificationHandler = AdjustFactory.getPurchaseVerificationHandler(
                this,
                toSendI(true),
                purchaseVerificationHandlerActivitySender);

        installReferrer = new InstallReferrer(adjustConfig.context, new InstallReferrerReadListener() {
            @Override
            public void onInstallReferrerRead(ReferrerDetails referrerDetails, String referrerApi) {
                sendInstallReferrer(referrerDetails, referrerApi);
            }

            @Override
            public void onFail(String message) {
                logger.debug(message);
            }

        });

        preLaunchActionsI(adjustConfig.preLaunchActions.preLaunchActionsArray);
        sendReftagReferrerI();

        bootstrapLifecycleI();
    }

    private void handleAttributionCallbackI() {
        cachedAttributionReadCallbacks.addAll(adjustConfig.cachedAttributionReadCallbacks);
        adjustConfig.cachedAttributionReadCallbacks.clear();

        if (! cachedAttributionReadCallbacks.isEmpty()
                && attribution != null)
        {
            final ArrayList<OnAttributionReadListener> cachedAttributionReadCallbacksCopy =
                    new ArrayList<>(cachedAttributionReadCallbacks);
            final AdjustAttribution attributionCopy = attribution;

            cachedAttributionReadCallbacks.clear();
            new Handler(adjustConfig.context.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    for (OnAttributionReadListener onAttributionReadListener : cachedAttributionReadCallbacksCopy) {
                        if (onAttributionReadListener != null) {
                            onAttributionReadListener.onAttributionRead(attributionCopy);
                        }
                    }
                }
            });
        }
    }

    private void handleAdidCallbackI() {
        cachedAdidReadCallbacks.addAll(adjustConfig.cachedAdidReadCallbacks);
        adjustConfig.cachedAdidReadCallbacks.clear();

        if (! cachedAdidReadCallbacks.isEmpty()
          && activityState != null
          && activityState.adid != null)
        {
            final ArrayList<OnAdidReadListener> cachedAdidReadCallbacksCopy =
              new ArrayList<>(cachedAdidReadCallbacks);
            final String adidCopy = activityState.adid;

            cachedAdidReadCallbacks.clear();
            new Handler(adjustConfig.context.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    for (OnAdidReadListener onAdidReadListener : cachedAdidReadCallbacksCopy) {
                        if (onAdidReadListener != null) {
                            onAdidReadListener.onAdidRead(adidCopy);
                        }
                    }
                }
            });
        }
    }

    private void bootstrapLifecycleI() {
        this.systemLifecycle = SystemLifecycle.getSingletonInstance();

        for (@NonNull final String message : systemLifecycle.logMessageList) {
            logger.debug("Lifecycle: %s", message);
        }

        systemLifecycle.overwriteCallback(this);

        if (AdjustFactory.isSystemLifecycleBootstrapIgnored()) {
            return;
        }

        this.internalState.foregroundOrElseBackground =
          systemLifecycle.foregroundOrElseBackgroundCached();

        if (internalState.isInForeground()) {
            onResumeI();
        }
    }

    private void checkForPreinstallI() {
        if (activityState == null) return;
        if (!activityState.enabled) return;
        if (activityState.isGdprForgotten) return;

        // sending preinstall referrer doesn't require preinstall tracking flag to be enabled
        sendPreinstallReferrerI();

        if (!adjustConfig.isPreinstallTrackingEnabled) return;
        if (internalState.hasPreinstallBeenRead()) return;

        if (deviceInfo.packageName == null || deviceInfo.packageName.isEmpty()) {
            logger.debug("Can't read preinstall payload, invalid package name");
            return;
        }

        SharedPreferencesManager sharedPreferencesManager = SharedPreferencesManager.getDefaultInstance(getContext());
        long readStatus = sharedPreferencesManager.getPreinstallPayloadReadStatus();

        if (PreinstallUtil.hasAllLocationsBeenRead(readStatus)) {
            internalState.preinstallHasBeenRead = true;
            return;
        }

        // 1. try reading preinstall payload from standard system property
        if (PreinstallUtil.hasNotBeenRead(Constants.SYSTEM_PROPERTIES, readStatus)) {
            String payloadSystemProperty = PreinstallUtil.getPayloadFromSystemProperty(
                    deviceInfo.packageName, logger);

            if (payloadSystemProperty != null && !payloadSystemProperty.isEmpty()) {
                sdkClickHandler.sendPreinstallPayload(payloadSystemProperty, Constants.SYSTEM_PROPERTIES);
            } else {
                readStatus = PreinstallUtil.markAsRead(Constants.SYSTEM_PROPERTIES, readStatus);
            }
        }

        // 2. try reading preinstall payload from system property using reflection
        if (PreinstallUtil.hasNotBeenRead(Constants.SYSTEM_PROPERTIES_REFLECTION, readStatus)) {
            String payloadSystemPropertyReflection = PreinstallUtil.getPayloadFromSystemPropertyReflection(
                    deviceInfo.packageName, logger);

            if (payloadSystemPropertyReflection != null && !payloadSystemPropertyReflection.isEmpty()) {
                sdkClickHandler.sendPreinstallPayload(payloadSystemPropertyReflection, Constants.SYSTEM_PROPERTIES_REFLECTION);
            } else {
                readStatus = PreinstallUtil.markAsRead(Constants.SYSTEM_PROPERTIES_REFLECTION, readStatus);
            }
        }

        // 3. try reading preinstall payload from system property file path
        if (PreinstallUtil.hasNotBeenRead(Constants.SYSTEM_PROPERTIES_PATH, readStatus)) {
            String payloadSystemPropertyFilePath = PreinstallUtil.getPayloadFromSystemPropertyFilePath(
                    deviceInfo.packageName, logger);

            if (payloadSystemPropertyFilePath != null && !payloadSystemPropertyFilePath.isEmpty()) {
                sdkClickHandler.sendPreinstallPayload(payloadSystemPropertyFilePath, Constants.SYSTEM_PROPERTIES_PATH);
            } else {
                readStatus = PreinstallUtil.markAsRead(Constants.SYSTEM_PROPERTIES_PATH, readStatus);
            }
        }

        // 4. try reading preinstall payload from system property file path using reflection
        if (PreinstallUtil.hasNotBeenRead(Constants.SYSTEM_PROPERTIES_PATH_REFLECTION, readStatus)) {
            String payloadSystemPropertyFilePathReflection = PreinstallUtil.getPayloadFromSystemPropertyFilePathReflection(
                    deviceInfo.packageName, logger);

            if (payloadSystemPropertyFilePathReflection != null && !payloadSystemPropertyFilePathReflection.isEmpty()) {
                sdkClickHandler.sendPreinstallPayload(payloadSystemPropertyFilePathReflection, Constants.SYSTEM_PROPERTIES_PATH_REFLECTION);
            } else {
                readStatus = PreinstallUtil.markAsRead(Constants.SYSTEM_PROPERTIES_PATH_REFLECTION, readStatus);
            }
        }

        // 5. try reading preinstall payload from default content uri
        if (PreinstallUtil.hasNotBeenRead(Constants.CONTENT_PROVIDER, readStatus)) {
            String payloadContentProviderDefault = PreinstallUtil.getPayloadFromContentProviderDefault(
                    adjustConfig.context,
                    deviceInfo.packageName,
                    logger);

            if (payloadContentProviderDefault != null && !payloadContentProviderDefault.isEmpty()) {
                sdkClickHandler.sendPreinstallPayload(payloadContentProviderDefault, Constants.CONTENT_PROVIDER);
            } else {
                readStatus = PreinstallUtil.markAsRead(Constants.CONTENT_PROVIDER, readStatus);
            }
        }

        // 6. try reading preinstall payload from all content provider with intent action and with install permission
        if (PreinstallUtil.hasNotBeenRead(Constants.CONTENT_PROVIDER_INTENT_ACTION, readStatus)) {
            List<String> payloadListContentProviderIntentAction = PreinstallUtil.getPayloadsFromContentProviderIntentAction(
                    adjustConfig.context,
                    deviceInfo.packageName,
                    logger);

            if (payloadListContentProviderIntentAction != null && !payloadListContentProviderIntentAction.isEmpty()) {
                for (String payload : payloadListContentProviderIntentAction) {
                    sdkClickHandler.sendPreinstallPayload(payload, Constants.CONTENT_PROVIDER_INTENT_ACTION);
                }
            } else {
                readStatus = PreinstallUtil.markAsRead(Constants.CONTENT_PROVIDER_INTENT_ACTION, readStatus);
            }
        }

        // 7. try reading preinstall payload from all content provider with intent action and without install permission
        if (PreinstallUtil.hasNotBeenRead(Constants.CONTENT_PROVIDER_NO_PERMISSION, readStatus)) {
            List<String> payloadListContentProviderIntentAction = PreinstallUtil.getPayloadsFromContentProviderNoPermission(
                    adjustConfig.context,
                    deviceInfo.packageName,
                    logger);

            if (payloadListContentProviderIntentAction != null && !payloadListContentProviderIntentAction.isEmpty()) {
                for (String payload : payloadListContentProviderIntentAction) {
                    sdkClickHandler.sendPreinstallPayload(payload, Constants.CONTENT_PROVIDER_NO_PERMISSION);
                }
            } else {
                readStatus = PreinstallUtil.markAsRead(Constants.CONTENT_PROVIDER_NO_PERMISSION, readStatus);
            }
        }

        // 8. try reading preinstall payload from file system (world readable)
        if (PreinstallUtil.hasNotBeenRead(Constants.FILE_SYSTEM, readStatus)) {
            String payloadFileSystem = PreinstallUtil.getPayloadFromFileSystem(
                    deviceInfo.packageName,
                    adjustConfig.preinstallFilePath,
                    logger);

            if (payloadFileSystem != null && !payloadFileSystem.isEmpty()) {
                sdkClickHandler.sendPreinstallPayload(payloadFileSystem, Constants.FILE_SYSTEM);
            } else {
                readStatus = PreinstallUtil.markAsRead(Constants.FILE_SYSTEM, readStatus);
            }
        }

        sharedPreferencesManager.setPreinstallPayloadReadStatus(readStatus);

        internalState.preinstallHasBeenRead = true;
    }

    private void readConfigFile(Context context) {
        Properties properties;

        try  {
            InputStream inputStream = context.getAssets().open("adjust_config.properties");
            properties = new Properties();
            properties.load(inputStream);
        } catch (Exception e) {
            logger.debug("%s file not found in this app", e.getMessage());
            return;
        }

        logger.verbose("adjust_config.properties file read and loaded");

        String defaultTracker = properties.getProperty("defaultTracker");

        if (defaultTracker != null) {
            adjustConfig.defaultTracker = defaultTracker;
        }
    }

    private void preLaunchActionsI(List<IRunActivityHandler> preLaunchActionsArray) {
        if (preLaunchActionsArray == null) {
            return;
        }

        for (IRunActivityHandler preLaunchAction : preLaunchActionsArray) {
            preLaunchAction.run(this);
        }
    }

    private void startI() {
        // check if it's the first sdk start
        if (internalState.hasFirstSdkStartNotOcurred()) {
            AdjustSigner.onResume(adjustConfig.logger);
            startFirstSessionI();
            return;
        } else {
            // check if third party sharing request came, then send it first
            for (AdjustThirdPartySharing adjustThirdPartySharing :
                    adjustConfig.preLaunchActions.preLaunchAdjustThirdPartySharingArray)
            {
                trackThirdPartySharingI(adjustThirdPartySharing);
            }

            if (adjustConfig.preLaunchActions.lastMeasurementConsentTracked != null) {
                trackMeasurementConsentI(
                        adjustConfig.preLaunchActions.
                                lastMeasurementConsentTracked.booleanValue());
            }

            adjustConfig.preLaunchActions.preLaunchAdjustThirdPartySharingArray =
                    new ArrayList<>();
            adjustConfig.preLaunchActions.lastMeasurementConsentTracked = null;
        }

        // it shouldn't start if it was disabled after a first session
        if (!activityState.enabled) {
            return;
        }

        AdjustSigner.onResume(adjustConfig.logger);

        updateHandlersStatusAndSendI();

        processCoppaComplianceI();

        processSessionI();

        checkAttributionStateI();

        processCachedDeeplinkI();
    }

    private void startFirstSessionI() {
        activityState = new ActivityState();
        internalState.firstSdkStart = true;

        activityState.setEventDeduplicationIdsMaxSize(adjustConfig.getEventDeduplicationIdsMaxSize());

        // still update handlers status
        updateHandlersStatusAndSendI();

        long now = System.currentTimeMillis();

        SharedPreferencesManager sharedPreferencesManager = SharedPreferencesManager.getDefaultInstance(getContext());
        activityState.pushToken = sharedPreferencesManager.getPushToken();
        // activityState.isGdprForgotten = sharedPreferencesManager.getGdprForgetMe();

        // track the first session package only if it's enabled
        if (internalState.isEnabled()) {
            if (sharedPreferencesManager.getGdprForgetMe()) {
                gdprForgetMeI();
            } else {
                processCoppaComplianceI();

                // check if third party sharing request came, then send it first
                for (AdjustThirdPartySharing adjustThirdPartySharing :
                        adjustConfig.preLaunchActions.preLaunchAdjustThirdPartySharingArray)
                {
                    trackThirdPartySharingI(adjustThirdPartySharing);
                }

                if (adjustConfig.preLaunchActions.lastMeasurementConsentTracked != null) {
                    trackMeasurementConsentI(
                            adjustConfig.preLaunchActions.
                                    lastMeasurementConsentTracked.booleanValue());
                }

                adjustConfig.preLaunchActions.preLaunchAdjustThirdPartySharingArray =
                        new ArrayList<>();
                adjustConfig.preLaunchActions.lastMeasurementConsentTracked = null;


                activityState.sessionCount = 1; // this is the first session
                transferSessionPackageI(now);
                checkAfterNewStartI(sharedPreferencesManager);
            }
        }

        activityState.resetSessionAttributes(now);
        activityState.enabled = internalState.isEnabled();

        writeActivityStateI();
        sharedPreferencesManager.removePushToken();
        sharedPreferencesManager.removeGdprForgetMe();

        // check for cached deeplinks
        processCachedDeeplinkI();

        // don't check attribution right after first sdk start
    }

    private void processSessionI() {
        if (activityState.isGdprForgotten) {
            return;
        }

        long now = System.currentTimeMillis();

        long lastInterval = now - activityState.lastActivity;

        if (lastInterval < 0) {
            logger.error(TIME_TRAVEL);
            activityState.lastActivity = now;
            writeActivityStateI();
            return;
        }

        // new session
        if (lastInterval > SESSION_INTERVAL) {
            trackNewSessionI(now);
            checkAfterNewStartI();
            return;
        }

        // new subsession
        if (lastInterval > SUBSESSION_INTERVAL) {
            activityState.subsessionCount++;
            activityState.sessionLength += lastInterval;
            activityState.lastActivity = now;
            logger.verbose("Started subsession %d of session %d",
                    activityState.subsessionCount,
                    activityState.sessionCount);
            writeActivityStateI();

            checkForPreinstallI();

            // Try to check if there's new referrer information.
            installReferrer.startConnection();
            readInstallReferrerMeta();
            readInstallReferrerHuaweiAds();
            readInstallReferrerHuaweiAppGallery();
            readInstallReferrerSamsung();
            readInstallReferrerXiaomi();
            readInstallReferrerVivo();

            return;
        }

        logger.verbose("Time span since last activity too short for a new subsession");
    }

    private void readInstallReferrerMeta() {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                ReferrerDetails referrerDetails = Reflection.getMetaReferrer(getContext(), adjustConfig.fbAppId, logger);
                if (referrerDetails != null) {
                    sendInstallReferrer(referrerDetails, REFERRER_API_META);
                }
            }
        });
    }

    private void readInstallReferrerHuaweiAds() {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                ReferrerDetails referrerDetails = Reflection.getHuaweiAdsReferrer(getContext(), logger);
                if (referrerDetails != null) {
                    sendInstallReferrer(referrerDetails, REFERRER_API_HUAWEI_ADS);
                }
            }
        });
    }

    private void readInstallReferrerHuaweiAppGallery() {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                ReferrerDetails referrerDetails = Reflection.getHuaweiAppGalleryReferrer(getContext(), logger);
                if (referrerDetails != null) {
                    sendInstallReferrer(referrerDetails, REFERRER_API_HUAWEI_APP_GALLERY);
                }
            }
        });
    }

    private void readInstallReferrerSamsung() {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                ReferrerDetails referrerDetails = Reflection.getSamsungReferrer(getContext(), logger);
                if (referrerDetails != null) {
                    sendInstallReferrer(referrerDetails, REFERRER_API_SAMSUNG);
                }
            }
        });
    }

    private void readInstallReferrerXiaomi() {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                ReferrerDetails referrerDetails = Reflection.getXiaomiReferrer(getContext(), logger);
                if (referrerDetails != null) {
                    sendInstallReferrer(referrerDetails, REFERRER_API_XIAOMI);
                }
            }
        });
    }

    private void readInstallReferrerVivo() {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                ReferrerDetails referrerDetails = Reflection.getVivoReferrer(getContext(), logger);
                if (referrerDetails != null) {
                    sendInstallReferrer(referrerDetails, REFERRER_API_VIVO);
                }
            }
        });
    }

    private void trackNewSessionI(final long now) {
        long lastInterval = now - activityState.lastActivity;

        activityState.sessionCount++;
        activityState.lastInterval = lastInterval;

        transferSessionPackageI(now);
        activityState.resetSessionAttributes(now);
        writeActivityStateI();
    }

    private void checkAttributionStateI() {
        if (!checkActivityStateI(activityState)) { return; }

        // if it's the first launch
        if (internalState.isFirstLaunch()) {
            // and it hasn't received the session response
            if (internalState.hasSessionResponseNotBeenProcessed()) {
                return;
            }
        }

        // if there is already an attribution saved and there was no attribution being asked
        if (attribution != null && !activityState.askingAttribution) {
            return;
        }

        attributionHandler.getAttribution();
    }

    private void processCachedDeeplinkI() {
        if (!checkActivityStateI(activityState)) {
            return;
        }

        SharedPreferencesManager sharedPreferencesManager = SharedPreferencesManager.getDefaultInstance(getContext());
        String cachedDeeplinkUrl = sharedPreferencesManager.getDeeplinkUrl();
        long cachedDeeplinkClickTime = sharedPreferencesManager.getDeeplinkClickTime();

        if (cachedDeeplinkUrl == null) {
            return;
        }
        if (cachedDeeplinkClickTime == -1) {
            return;
        }

        processDeeplink(Uri.parse(cachedDeeplinkUrl), cachedDeeplinkClickTime);

        sharedPreferencesManager.removeDeeplink();
    }

    private void endI() {
        // pause sending if it's not allowed to send
        if (!toSendI()) {
            pauseSendingI();
        }

        if (updateActivityStateI(System.currentTimeMillis())) {
            writeActivityStateI();
        }
    }

    private void trackEventI(AdjustEvent event) {
        if (!checkActivityStateI(activityState)) return;
        if (!isEnabledI()) return;
        if (!checkEventI(event)) return;
        if (activityState.isGdprForgotten) return;
        if (!shouldProcessEventI(event.deduplicationId)) return;

        long now = System.currentTimeMillis();

        activityState.eventCount++;
        updateActivityStateI(now);

        PackageBuilder eventBuilder = new PackageBuilder(adjustConfig, deviceInfo, activityState, globalParameters, now);
        eventBuilder.internalState = internalState;
        ActivityPackage eventPackage = eventBuilder.buildEventPackage(event);
        packageHandler.addPackage(eventPackage);

        packageHandler.sendFirstPackage();

        // if it is in the background and it can send, start the background timer
        if (adjustConfig.isSendingInBackgroundEnabled && internalState.isInBackground()) {
            startBackgroundTimerI();
        }

        writeActivityStateI();
    }

    private void launchEventResponseTasksI(final EventResponseData eventResponseData) {
        // try to update adid from response
        updateAdidI(eventResponseData.adid);

        Handler handler = new Handler(adjustConfig.context.getMainLooper());

        // success callback
        if (eventResponseData.success && adjustConfig.onEventTrackingSucceededListener != null) {
            logger.debug("Launching success event tracking listener");
            // add it to the handler queue
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    if (adjustConfig == null) {
                        return;
                    }
                    if (adjustConfig.onEventTrackingSucceededListener == null) {
                        return;
                    }
                    adjustConfig.onEventTrackingSucceededListener.onEventTrackingSucceeded(eventResponseData.getSuccessResponseData());
                }
            };
            handler.post(runnable);

            return;
        }
        // failure callback
        if (!eventResponseData.success && adjustConfig.onEventTrackingFailedListener != null) {
            logger.debug("Launching failed event tracking listener");
            // add it to the handler queue
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    if (adjustConfig == null) {
                        return;
                    }
                    if (adjustConfig.onEventTrackingFailedListener == null) {
                        return;
                    }
                    adjustConfig.onEventTrackingFailedListener.onEventTrackingFailed(eventResponseData.getFailureResponseData());
                }
            };
            handler.post(runnable);

            return;
        }
    }

    private void launchSdkClickResponseTasksI(SdkClickResponseData sdkClickResponseData) {
        // try to update adid from response
        updateAdidI(sdkClickResponseData.adid);

        // use the same handler to ensure that all tasks are executed sequentially
        Handler handler = new Handler(adjustConfig.context.getMainLooper());

        // try to update the attribution
        boolean attributionUpdated = updateAttributionI(sdkClickResponseData.attribution);

        // if attribution changed, launch attribution changed delegate
        if (attributionUpdated) {
            launchAttributionListenerI(handler);
        }

        if (!TextUtils.isEmpty(sdkClickResponseData.resolvedDeeplink)) {
            OnDeeplinkResolvedListener onDeeplinkResolvedListener = cachedDeeplinkResolutionCallback;
            cachedDeeplinkResolutionCallback = null;
            if (onDeeplinkResolvedListener != null) {
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        onDeeplinkResolvedListener.onDeeplinkResolved(sdkClickResponseData.resolvedDeeplink);
                    }
                };
                handler.post(runnable);
            }
        }
    }

    private void launchSessionResponseTasksI(SessionResponseData sessionResponseData) {
        logger.debug("Launching SessionResponse tasks");

        // try to update adid from response
        updateAdidI(sessionResponseData.adid);

        // use the same handler to ensure that all tasks are executed sequentially
        Handler handler = new Handler(adjustConfig.context.getMainLooper());

        // try to update the attribution
        boolean attributionUpdated = updateAttributionI(sessionResponseData.attribution);

        // if attribution changed, launch attribution changed delegate
        if (attributionUpdated) {
            launchAttributionListenerI(handler);
        }

        // if attribution didn't update and it's still null
        // ask for attribution
        if (this.attribution == null && activityState.askingAttribution == false) {
            this.attributionHandler.getAttribution();
        }

        // mark install as tracked on success
        if (sessionResponseData.success) {
            SharedPreferencesManager.getDefaultInstance(getContext()).setInstallTracked();
        }

        // launch Session tracking listener if available
        launchSessionResponseListenerI(sessionResponseData, handler);

        // mark session response has proccessed
        internalState.sessionResponseProcessed = true;
    }

    private void launchSessionResponseListenerI(final SessionResponseData sessionResponseData, Handler handler) {
        // success callback
        if (sessionResponseData.success && adjustConfig.onSessionTrackingSucceededListener != null) {
            logger.debug("Launching success session tracking listener");
            // add it to the handler queue
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    if (adjustConfig == null) {
                        return;
                    }
                    if (adjustConfig.onSessionTrackingSucceededListener == null) {
                        return;
                    }
                    adjustConfig.onSessionTrackingSucceededListener.onSessionTrackingSucceeded(sessionResponseData.getSuccessResponseData());
                }
            };
            handler.post(runnable);

            return;
        }
        // failure callback
        if (!sessionResponseData.success && adjustConfig.onSessionTrackingFailedListener != null) {
            logger.debug("Launching failed session tracking listener");
            // add it to the handler queue
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    if (adjustConfig == null) {
                        return;
                    }
                    if (adjustConfig.onSessionTrackingFailedListener == null) {
                        return;
                    }
                    adjustConfig.onSessionTrackingFailedListener.onSessionTrackingFailed(sessionResponseData.getFailureResponseData());
                }
            };
            handler.post(runnable);

            return;
        }
    }

    private void launchAttributionResponseTasksI(AttributionResponseData attributionResponseData) {
        // try to update adid from response
        updateAdidI(attributionResponseData.adid);

        Handler handler = new Handler(adjustConfig.context.getMainLooper());

        // try to update the attribution
        boolean attributionUpdated = updateAttributionI(attributionResponseData.attribution);

        // if attribution changed, launch attribution changed delegate
        if (attributionUpdated) {
            launchAttributionListenerI(handler);
        }

        // if there is any, try to launch the deeplink
        prepareDeeplinkI(attributionResponseData.deeplink, handler);
    }

    private void launchAttributionListenerI(Handler handler) {
        if (adjustConfig.onAttributionChangedListener == null) {
            return;
        }
        // add it to the handler queue
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (adjustConfig == null) {
                    return;
                }
                if (adjustConfig.onAttributionChangedListener == null) {
                    return;
                }
                adjustConfig.onAttributionChangedListener.onAttributionChanged(attribution);
            }
        };
        handler.post(runnable);
    }

    private void launchPurchaseVerificationResponseTasksI(PurchaseVerificationResponseData purchaseVerificationResponseData) {
        // use the same handler to ensure that all tasks are executed sequentially
        Handler handler = new Handler(adjustConfig.context.getMainLooper());
        JSONObject jsonResponse = purchaseVerificationResponseData.jsonResponse;

        // check and parse response data
        AdjustPurchaseVerificationResult verificationResult;
        if (jsonResponse == null) {
            verificationResult = new AdjustPurchaseVerificationResult(
                    "not_verified",
                    101,
                    purchaseVerificationResponseData.message);
        } else {
            verificationResult = new AdjustPurchaseVerificationResult(
                    UtilNetworking.extractJsonString(jsonResponse, "verification_status"),
                    UtilNetworking.extractJsonInt(jsonResponse, "code"),
                    UtilNetworking.extractJsonString(jsonResponse, "message"));
        }

        // trigger purchase verification callback with the verification result
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                purchaseVerificationResponseData.activityPackage.getPurchaseVerificationCallback().onVerificationFinished(verificationResult);
            }
        };
        handler.post(runnable);

        if (purchaseVerificationResponseData.activityPackage != null &&
          purchaseVerificationResponseData.activityPackage.event != null)
        {
            this.trackEventI(purchaseVerificationResponseData.activityPackage.event);
        }
    }

    private void prepareDeeplinkI(final Uri deeplink, final Handler handler) {
        if (deeplink == null) {
            return;
        }

        logger.info("Deferred deeplink received (%s)", deeplink);

        final Intent deeplinkIntent = createDeeplinkIntentI(deeplink);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (adjustConfig == null) {
                    return;
                }
                boolean toLaunchDeeplink = true;
                if (adjustConfig.onDeferredDeeplinkResponseListener != null) {
                    toLaunchDeeplink = adjustConfig.onDeferredDeeplinkResponseListener.launchReceivedDeeplink(deeplink);
                }
                if (toLaunchDeeplink) {
                    launchDeeplinkMain(deeplinkIntent, deeplink);
                }
            }
        };
        handler.post(runnable);
    }

    private Intent createDeeplinkIntentI(Uri deeplink) {
        Intent mapIntent;
        mapIntent = new Intent(Intent.ACTION_VIEW, deeplink);
        mapIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mapIntent.setPackage(adjustConfig.context.getPackageName());
        return mapIntent;
    }

    private void launchDeeplinkMain(Intent deeplinkIntent, Uri deeplink) {
        // Verify it resolves
        PackageManager packageManager = adjustConfig.context.getPackageManager();
        List<ResolveInfo> activities = packageManager.queryIntentActivities(deeplinkIntent, 0);
        boolean isIntentSafe = activities.size() > 0;

        // Start an activity if it's safe
        if (!isIntentSafe) {
            logger.error("Unable to open deferred deeplink (%s)", deeplink);
            return;
        }

        // add it to the handler queue
        logger.info("Open deferred deeplink (%s)", deeplink);
        adjustConfig.context.startActivity(deeplinkIntent);
    }

    private void setEnabledI(boolean enabled) {
        // compare with the saved or internal state
        if (!hasChangedStateI(this.isEnabledI(), enabled,
                "Adjust already enabled", "Adjust already disabled")) {
            return;
        }

        if (enabled) {
            if (activityState != null && activityState.isGdprForgotten) {
                logger.error("Re-enabling SDK not possible for forgotten user");
                return;
            }
        }

        // save new enabled state in internal state
        internalState.enabled = enabled;

        if (internalState.hasFirstSdkStartNotOcurred()) {
            updateStatusI(!enabled,
                    "Handlers will start as paused due to the SDK being disabled",
                    "Handlers will still start as paused",
                    "Handlers will start as active due to the SDK being enabled");
            return;
        }

        activityState.enabled = enabled;
        writeActivityStateI();

        if (enabled) {
            SharedPreferencesManager sharedPreferencesManager = SharedPreferencesManager.getDefaultInstance(getContext());

            if (sharedPreferencesManager.getGdprForgetMe()) {
                gdprForgetMeI();
            } else {
                processCoppaComplianceI();

                for (AdjustThirdPartySharing adjustThirdPartySharing :
                        adjustConfig.preLaunchActions.preLaunchAdjustThirdPartySharingArray)
                {
                    trackThirdPartySharingI(adjustThirdPartySharing);
                }
                if (adjustConfig.preLaunchActions.lastMeasurementConsentTracked != null) {
                    trackMeasurementConsentI(
                            adjustConfig.preLaunchActions.
                                    lastMeasurementConsentTracked.booleanValue());
                }

                adjustConfig.preLaunchActions.preLaunchAdjustThirdPartySharingArray =
                        new ArrayList<>();
                adjustConfig.preLaunchActions.lastMeasurementConsentTracked = null;
            }

            // check if install was tracked
            if (!sharedPreferencesManager.getInstallTracked()) {
                logger.debug("Detected that install was not tracked at enable time");
                long now = System.currentTimeMillis();
                trackNewSessionI(now);
            }
            checkAfterNewStartI(sharedPreferencesManager);
        }

        updateStatusI(!enabled,
                "Pausing handlers due to SDK being disabled",
                "Handlers remain paused",
                "Resuming handlers due to SDK being enabled");
    }


    private void checkAfterNewStartI() {
        checkAfterNewStartI(SharedPreferencesManager.getDefaultInstance(getContext()));
    }

    private void checkAfterNewStartI(SharedPreferencesManager sharedPreferencesManager) {
        // check if there is a saved push token to send
        String pushToken = sharedPreferencesManager.getPushToken();

        if (pushToken != null && !pushToken.equals(activityState.pushToken)) {
            // queue set push token
            setPushToken(pushToken, true);
        }

        // check if there are token to send
        Object referrers = sharedPreferencesManager.getRawReferrerArray();
        if (referrers != null) {
            // queue send referrer tag
            sendReftagReferrer();
        }

        checkForPreinstallI();

        // try to read and send the install referrer
        installReferrer.startConnection();
        readInstallReferrerMeta();
        readInstallReferrerHuaweiAds();
        readInstallReferrerHuaweiAppGallery();
        readInstallReferrerSamsung();
        readInstallReferrerXiaomi();
        readInstallReferrerVivo();
    }

    private void setOfflineModeI(boolean offline) {
        // compare with the internal state
        if (!hasChangedStateI(internalState.isOffline(), offline,
                "Adjust already in offline mode",
                "Adjust already in online mode")) {
            return;
        }

        internalState.offline = offline;

        if (internalState.hasFirstSdkStartNotOcurred()) {
            updateStatusI(offline,
                    "Handlers will start paused due to SDK being offline",
                    "Handlers will still start as paused",
                    "Handlers will start as active due to SDK being online");
            return;
        }

        updateStatusI(offline,
                "Pausing handlers to put SDK offline mode",
                "Handlers remain paused",
                "Resuming handlers to put SDK in online mode");

    }

    private boolean hasChangedStateI(boolean previousState, boolean newState,
                                     String trueMessage, String falseMessage)
    {
        if (previousState != newState) {
            return true;
        }

        if (previousState) {
            logger.debug(trueMessage);
        } else {
            logger.debug(falseMessage);
        }

        return false;
    }

    private void updateStatusI(boolean pausingState, String pausingMessage,
                               String remainsPausedMessage, String unPausingMessage)
    {
        // it is changing from an active state to a pause state
        if (pausingState) {
            logger.info(pausingMessage);
        }
        // check if it's remaining in a pause state
        else if (pausedI(false)) {
            // including the sdk click handler
            if (pausedI(true)) {
                logger.info(remainsPausedMessage);
            } else {
                logger.info(remainsPausedMessage + ", except the Sdk Click Handler");
            }
        } else {
            // it is changing from a pause state to an active state
            logger.info(unPausingMessage);
        }

        updateHandlersStatusAndSendI();
    }

    private void setAskingAttributionI(boolean askingAttribution) {
        activityState.askingAttribution = askingAttribution;

        writeActivityStateI();
    }

    private void sendReftagReferrerI() {
        if (!isEnabledI()) {
            return;
        }
        if (internalState.hasFirstSdkStartNotOcurred()) {
            return;
        }

        sdkClickHandler.sendReftagReferrers();
    }

    private void sendPreinstallReferrerI() {
        if (!isEnabledI()) {
            return;
        }
        if (internalState.hasFirstSdkStartNotOcurred()) {
            return;
        }

        String referrerPayload = SharedPreferencesManager.getDefaultInstance(getContext()).getPreinstallReferrer();

        if (referrerPayload == null || referrerPayload.isEmpty()) {
            return;
        }

        sdkClickHandler.sendPreinstallPayload(referrerPayload, Constants.SYSTEM_INSTALLER_REFERRER);
    }

    private void sendInstallReferrerI(ReferrerDetails referrerDetails, String referrerApi) {
        if (!isEnabledI()) {
            return;
        }

        if (!isValidReferrerDetails(referrerDetails)) {
            return;
        }

        if (Util.isEqualReferrerDetails(referrerDetails, referrerApi, activityState)) {
            // Same click already sent before, nothing to be done.
            return;
        }

        // Create sdk click
        ActivityPackage sdkClickPackage = PackageFactory.buildInstallReferrerSdkClickPackage(
                referrerDetails,
                referrerApi,
                activityState,
                adjustConfig,
                deviceInfo,
                globalParameters,
                internalState);

        sdkClickHandler.sendSdkClick(sdkClickPackage);
    }

    private boolean isValidReferrerDetails(final ReferrerDetails referrerDetails) {
        if (referrerDetails == null) {
            return false;
        }

        if (referrerDetails.installReferrer == null) {
            return false;
        }

        return referrerDetails.installReferrer.length() != 0;
    }

    private void processDeeplinkI(Uri url, long clickTime) {
        if (!isEnabledI()) {
            return;
        }

        if (Util.isUrlFilteredOut(url)) {
            logger.debug("Deeplink (" + url.toString() + ") processing skipped");
            return;
        }

        ActivityPackage sdkClickPackage = PackageFactory.buildDeeplinkSdkClickPackage(
                url,
                clickTime,
                activityState,
                adjustConfig,
                deviceInfo,
                globalParameters,
                internalState);

        if (sdkClickPackage == null) {
            return;
        }

        sdkClickHandler.sendSdkClick(sdkClickPackage);
    }

    private void updateHandlersStatusAndSendI() {
        // check if it should stop sending
        if (!toSendI()) {
            pauseSendingI();
            return;
        }

        resumeSendingI();

        // try to send
        packageHandler.sendFirstPackage();
    }

    private void pauseSendingI() {
        attributionHandler.pauseSending();
        packageHandler.pauseSending();
        // the conditions to pause the sdk click handler are less restrictive
        // it's possible for the sdk click handler to be active while others are paused
        if (!toSendI(true)) {
            sdkClickHandler.pauseSending();
            purchaseVerificationHandler.pauseSending();
        } else {
            sdkClickHandler.resumeSending();
            purchaseVerificationHandler.resumeSending();
        }
    }

    private void resumeSendingI() {
        attributionHandler.resumeSending();
        packageHandler.resumeSending();
        sdkClickHandler.resumeSending();
        purchaseVerificationHandler.resumeSending();
    }

    private boolean updateActivityStateI(long now) {
        if (!checkActivityStateI(activityState)) { return false; }

        long lastInterval = now - activityState.lastActivity;

        // ignore late updates
        if (lastInterval > SESSION_INTERVAL) {
            return false;
        }
        activityState.lastActivity = now;

        if (lastInterval < 0) {
            logger.error(TIME_TRAVEL);
        } else {
            activityState.sessionLength += lastInterval;
            activityState.timeSpent += lastInterval;
        }
        return true;
    }

    public static boolean deleteActivityState(Context context) {
        return context.deleteFile(ACTIVITY_STATE_FILENAME);
    }

    public static boolean deleteAttribution(Context context) {
        return context.deleteFile(ATTRIBUTION_FILENAME);
    }

    public static boolean deleteGlobalCallbackParameters(Context context) {
        return context.deleteFile(GLOBAL_CALLBACK_PARAMETERS_FILENAME);
    }

    public static boolean deleteGlobalPartnerParameters(Context context) {
        return context.deleteFile(GLOBAL_PARTNER_PARAMETERS_FILENAME);
    }

    private void transferSessionPackageI(long now) {
        PackageBuilder builder = new PackageBuilder(adjustConfig, deviceInfo, activityState,
                globalParameters, now);
        builder.internalState = internalState;
        ActivityPackage sessionPackage = builder.buildSessionPackage();
        packageHandler.addPackage(sessionPackage);
        packageHandler.sendFirstPackage();
    }

    private void startForegroundTimerI() {
        // don't start the timer if it's disabled
        if (!isEnabledI()) {
            return;
        }

        foregroundTimer.start();
    }

    private void stopForegroundTimerI() {
        foregroundTimer.suspend();
    }

    private void foregroundTimerFiredI() {
        // stop the timer cycle if it's disabled
        if (!isEnabledI()) {
            stopForegroundTimerI();
            return;
        }

        if (toSendI()) {
            packageHandler.sendFirstPackage();
        }

        if (updateActivityStateI(System.currentTimeMillis())) {
            writeActivityStateI();
        }
    }

    private void startBackgroundTimerI() {
        if (backgroundTimer == null) {
            return;
        }

        // check if it can send in the background
        if (!toSendI()) {
            return;
        }

        // background timer already started
        if (backgroundTimer.getFireIn() > 0) {
            return;
        }

        backgroundTimer.startIn(BACKGROUND_TIMER_INTERVAL);
    }

    private void stopBackgroundTimerI() {
        if (backgroundTimer == null) {
            return;
        }

        backgroundTimer.cancel();
    }

    private void backgroundTimerFiredI() {
        if (toSendI()) {
            packageHandler.sendFirstPackage();
        }
    }

    public void addGlobalCallbackParameterI(String key, String value) {
        if (!Util.isValidParameter(key, "key", "Global Callback")) return;
        if (!Util.isValidParameter(value, "value", "Global Callback")) return;

        if (globalParameters.callbackParameters == null) {
            globalParameters.callbackParameters = new LinkedHashMap<String, String>();
        }

        String oldValue = globalParameters.callbackParameters.get(key);

        if (value.equals(oldValue)) {
            logger.verbose("Key %s already present with the same value", key);
            return;
        }

        if (oldValue != null) {
            logger.warn("Key %s will be overwritten", key);
        }

        globalParameters.callbackParameters.put(key, value);

        writeGlobalCallbackParametersI();
    }

    public void addGlobalPartnerParameterI(String key, String value) {
        if (!Util.isValidParameter(key, "key", "Global Partner")) return;
        if (!Util.isValidParameter(value, "value", "Global Partner")) return;

        if (globalParameters.partnerParameters == null) {
            globalParameters.partnerParameters = new LinkedHashMap<String, String>();
        }

        String oldValue = globalParameters.partnerParameters.get(key);

        if (value.equals(oldValue)) {
            logger.verbose("Key %s already present with the same value", key);
            return;
        }

        if (oldValue != null) {
            logger.warn("Key %s will be overwritten", key);
        }

        globalParameters.partnerParameters.put(key, value);

        writeGlobalPartnerParametersI();
    }

    public void removeGlobalCallbackParameterI(String key) {
        if (!Util.isValidParameter(key, "key", "Session Callback")) return;

        if (globalParameters.callbackParameters == null) {
            logger.warn("Session Callback parameters are not set");
            return;
        }

        String oldValue = globalParameters.callbackParameters.remove(key);

        if (oldValue == null) {
            logger.warn("Key %s does not exist", key);
            return;
        }

        logger.debug("Key %s will be removed", key);

        writeGlobalCallbackParametersI();
    }

    public void removeGlobalPartnerParameterI(String key) {
        if (!Util.isValidParameter(key, "key", "Session Partner")) return;

        if (globalParameters.partnerParameters == null) {
            logger.warn("Session Partner parameters are not set");
            return;
        }

        String oldValue = globalParameters.partnerParameters.remove(key);

        if (oldValue == null) {
            logger.warn("Key %s does not exist", key);
            return;
        }

        logger.debug("Key %s will be removed", key);

        writeGlobalPartnerParametersI();
    }

    public void removeGlobalCallbackParametersI() {
        if (globalParameters.callbackParameters == null) {
            logger.warn("Session Callback parameters are not set");
        }

        globalParameters.callbackParameters = null;

        writeGlobalCallbackParametersI();
    }

    public void removeGlobalPartnerParametersI() {
        if (globalParameters.partnerParameters == null) {
            logger.warn("Session Partner parameters are not set");
        }

        globalParameters.partnerParameters = null;

        writeGlobalPartnerParametersI();
    }

    private void setPushTokenI(String token) {
        if (!checkActivityStateI(activityState)) { return; }
        if (!isEnabledI()) { return; }
        if (activityState.isGdprForgotten) { return; }

        if (token == null) { return; }
        if (token.equals(activityState.pushToken)) { return; }

        // save new push token
        activityState.pushToken = token;
        writeActivityStateI();

        long now = System.currentTimeMillis();
        PackageBuilder infoPackageBuilder = new PackageBuilder(adjustConfig, deviceInfo, activityState, globalParameters, now);
        infoPackageBuilder.internalState = internalState;

        ActivityPackage infoPackage = infoPackageBuilder.buildInfoPackage(Constants.PUSH);
        packageHandler.addPackage(infoPackage);

        // If push token was cached, remove it.
        SharedPreferencesManager.getDefaultInstance(getContext()).removePushToken();

        packageHandler.sendFirstPackage();
    }

    private void gdprForgetMeI() {
        if (!checkActivityStateI(activityState)) { return; }
        if (!isEnabledI()) { return; }
        if (activityState.isGdprForgotten) { return; }

        activityState.isGdprForgotten = true;
        writeActivityStateI();

        long now = System.currentTimeMillis();
        PackageBuilder gdprPackageBuilder = new PackageBuilder(adjustConfig, deviceInfo, activityState, globalParameters, now);
        gdprPackageBuilder.internalState = internalState;

        ActivityPackage gdprPackage = gdprPackageBuilder.buildGdprPackage();
        packageHandler.addPackage(gdprPackage);

        // If GDPR choice was cached, remove it.
        SharedPreferencesManager.getDefaultInstance(getContext()).removeGdprForgetMe();

        packageHandler.sendFirstPackage();
    }

    private void trackThirdPartySharingI(final AdjustThirdPartySharing adjustThirdPartySharing) {
        if (!checkActivityStateI(activityState)) {
            adjustConfig.preLaunchActions.preLaunchAdjustThirdPartySharingArray.add(
                    adjustThirdPartySharing);
            return;
        }
        if (!isEnabledI()) { return; }
        if (activityState.isGdprForgotten) { return; }
        if (adjustConfig.coppaComplianceEnabled) {
            logger.warn("Calling third party sharing API not allowed when COPPA enabled");
            return;
        }
        long now = System.currentTimeMillis();
        PackageBuilder packageBuilder = new PackageBuilder(
                adjustConfig, deviceInfo, activityState, globalParameters, now);
        packageBuilder.internalState = internalState;

        ActivityPackage activityPackage =
                packageBuilder.buildThirdPartySharingPackage(adjustThirdPartySharing);
        packageHandler.addPackage(activityPackage);

        packageHandler.sendFirstPackage();
    }

    private void trackMeasurementConsentI(final boolean consentMeasurement) {
        if (!checkActivityStateI(activityState)) {
            adjustConfig.preLaunchActions.lastMeasurementConsentTracked = consentMeasurement;
            return;
        }
        if (!isEnabledI()) { return; }
        if (activityState.isGdprForgotten) { return; }

        long now = System.currentTimeMillis();
        PackageBuilder packageBuilder = new PackageBuilder(
                adjustConfig, deviceInfo, activityState, globalParameters, now);
        packageBuilder.internalState = internalState;

        ActivityPackage activityPackage =
                packageBuilder.buildMeasurementConsentPackage(consentMeasurement);
        packageHandler.addPackage(activityPackage);

        packageHandler.sendFirstPackage();
    }

    private void trackAdRevenueI(AdjustAdRevenue adjustAdRevenue) {
        if (!checkActivityStateI(activityState)) { return; }
        if (!isEnabledI()) { return; }
        if (!checkAdjustAdRevenue(adjustAdRevenue)) { return; }
        if (activityState.isGdprForgotten) { return; }

        long now = System.currentTimeMillis();

        PackageBuilder packageBuilder = new PackageBuilder(adjustConfig, deviceInfo, activityState, globalParameters, now);
        packageBuilder.internalState = internalState;

        ActivityPackage adRevenuePackage = packageBuilder.buildAdRevenuePackage(adjustAdRevenue);
        packageHandler.addPackage(adRevenuePackage);
        packageHandler.sendFirstPackage();
    }

    private void trackPlayStoreSubscriptionI(final AdjustPlayStoreSubscription subscription) {
        if (!checkActivityStateI(activityState)) { return; }
        if (!isEnabledI()) { return; }
        if (activityState.isGdprForgotten) { return; }

        long now = System.currentTimeMillis();

        PackageBuilder packageBuilder = new PackageBuilder(adjustConfig, deviceInfo, activityState, globalParameters, now);
        packageBuilder.internalState = internalState;

        ActivityPackage subscriptionPackage = packageBuilder.buildSubscriptionPackage(subscription);
        packageHandler.addPackage(subscriptionPackage);
        packageHandler.sendFirstPackage();
    }

    private void verifyPlayStorePurchaseI(final AdjustPlayStorePurchase purchase,
                                          final OnPurchaseVerificationFinishedListener callback) {
        if (callback == null) {
            logger.warn("Purchase verification aborted because verification callback is null");
            return;
        }
        // from this moment on we know that we can ping client callback in case of error
        if (adjustConfig.isDataResidency) {
            logger.warn("Purchase verification not available for data residency users right now");
            AdjustPurchaseVerificationResult result = new AdjustPurchaseVerificationResult(
                    "not_verified",
                    109,
                    "Purchase verification not available for data residency users right now");
            callback.onVerificationFinished(result);
            return;
        }
        if (!checkActivityStateI(activityState)) {
            AdjustPurchaseVerificationResult result = new AdjustPurchaseVerificationResult(
                    "not_verified",
                    102,
                    "Purchase verification aborted because SDK is still not initialized");
            callback.onVerificationFinished(result);
            logger.warn("Purchase verification aborted because SDK is still not initialized");
            return;
        }
        if (!isEnabledI()) {
            AdjustPurchaseVerificationResult result = new AdjustPurchaseVerificationResult(
                    "not_verified",
                    103,
                    "Purchase verification aborted because SDK is disabled");
            callback.onVerificationFinished(result);
            logger.warn("Purchase verification aborted because SDK is disabled");
            return;
        }
        if (activityState.isGdprForgotten) {
            AdjustPurchaseVerificationResult result = new AdjustPurchaseVerificationResult(
                    "not_verified",
                    104,
                    "Purchase verification aborted because user is GDPR forgotten");
            callback.onVerificationFinished(result);
            logger.warn("Purchase verification aborted because user is GDPR forgotten");
            return;
        }
        if (purchase == null) {
            logger.warn("Purchase verification aborted because purchase instance is null");
            AdjustPurchaseVerificationResult verificationResult =
                    new AdjustPurchaseVerificationResult(
                            "not_verified",
                            105,
                            "Purchase verification aborted because purchase instance is null");
            callback.onVerificationFinished(verificationResult);
            return;
        }

        long now = System.currentTimeMillis();
        PackageBuilder packageBuilder = new PackageBuilder(adjustConfig, deviceInfo, activityState, globalParameters, now);
        packageBuilder.internalState = internalState;

        ActivityPackage verificationPackage = packageBuilder.buildVerificationPackage(purchase, callback);
        if (verificationPackage == null) {
            logger.warn("Purchase verification aborted because verification package is null");
            AdjustPurchaseVerificationResult verificationResult =
                    new AdjustPurchaseVerificationResult(
                            "not_verified",
                            106,
                            "Purchase verification aborted because verification package is null");
            callback.onVerificationFinished(verificationResult);
            return;
        }
        purchaseVerificationHandler.sendPurchaseVerificationPackage(verificationPackage);
    }

    private void verifyAndTrackPlayStorePurchaseI(final AdjustEvent event,
                                                  final OnPurchaseVerificationFinishedListener callback) {
        if (callback == null) {
            logger.warn("Purchase verification aborted because verification callback is null");
            return;
        }
        // from this moment on we know that we can ping client callback in case of error
        if (adjustConfig.isDataResidency) {
            logger.warn("Purchase verification not available for data residency users right now");
            AdjustPurchaseVerificationResult result = new AdjustPurchaseVerificationResult(
                    "not_verified",
                    109,
                    "Purchase verification not available for data residency users right now");
            callback.onVerificationFinished(result);
            return;
        }
        if (!checkActivityStateI(activityState)) {
            AdjustPurchaseVerificationResult result = new AdjustPurchaseVerificationResult(
                    "not_verified",
                    102,
                    "Purchase verification aborted because SDK is still not initialized");
            callback.onVerificationFinished(result);
            logger.warn("Purchase verification aborted because SDK is still not initialized");
            return;
        }
        if (!isEnabledI()) {
            AdjustPurchaseVerificationResult result = new AdjustPurchaseVerificationResult(
                    "not_verified",
                    103,
                    "Purchase verification aborted because SDK is disabled");
            callback.onVerificationFinished(result);
            logger.warn("Purchase verification aborted because SDK is disabled");
            return;
        }
        if (activityState.isGdprForgotten) {
            AdjustPurchaseVerificationResult result = new AdjustPurchaseVerificationResult(
                    "not_verified",
                    104,
                    "Purchase verification aborted because user is GDPR forgotten");
            callback.onVerificationFinished(result);
            logger.warn("Purchase verification aborted because user is GDPR forgotten");
            return;
        }
        if (event == null) {
            logger.warn("Purchase verification aborted because event instance is null");
            AdjustPurchaseVerificationResult verificationResult =
                    new AdjustPurchaseVerificationResult(
                            "not_verified",
                            106,
                            "Purchase verification aborted because event instance is null");
            callback.onVerificationFinished(verificationResult);
            return;
        }

        long now = System.currentTimeMillis();
        PackageBuilder packageBuilder = new PackageBuilder(adjustConfig, deviceInfo, activityState, globalParameters, now);
        ActivityPackage verificationPackage = packageBuilder.buildVerificationPackage(event, callback);
        if (verificationPackage == null) {
            logger.warn("Purchase verification aborted because verification package is null");
            AdjustPurchaseVerificationResult verificationResult =
                    new AdjustPurchaseVerificationResult(
                            "not_verified",
                            107,
                            "Purchase verification aborted because verification package is null");
            callback.onVerificationFinished(verificationResult);
            return;
        }
        verificationPackage.event = event;
        purchaseVerificationHandler.sendPurchaseVerificationPackage(verificationPackage);
    }

    private void gotOptOutResponseI() {
        activityState.isGdprForgotten = true;
        writeActivityStateI();

        packageHandler.flush();
        setEnabledI(false);
    }

    private void readActivityStateI(Context context) {
        try {
            activityState = Util.readObject(context, ACTIVITY_STATE_FILENAME, ACTIVITY_STATE_NAME, ActivityState.class);
        } catch (Exception e) {
            logger.error("Failed to read %s file (%s)", ACTIVITY_STATE_NAME, e.getMessage());
            activityState = null;
        }
        if (activityState != null) {
            internalState.firstSdkStart = true;
        }
    }

    private void readAttributionI(Context context) {
        try {
            attribution = Util.readObject(context, ATTRIBUTION_FILENAME, ATTRIBUTION_NAME, AdjustAttribution.class);
        } catch (Exception e) {
            logger.error("Failed to read %s file (%s)", ATTRIBUTION_NAME, e.getMessage());
            attribution = null;
        }
    }

    @SuppressWarnings("unchecked")
    private void readGlobalCallbackParametersI(Context context) {
        try {
            globalParameters.callbackParameters = Util.readObject(context,
                    GLOBAL_CALLBACK_PARAMETERS_FILENAME,
                    GLOBAL_CALLBACK_PARAMETERS_NAME,
                    (Class<Map<String,String>>)(Class)Map.class);
        } catch (Exception e) {
            logger.error("Failed to read %s file (%s)", GLOBAL_CALLBACK_PARAMETERS_NAME, e.getMessage());
            globalParameters.callbackParameters = null;
        }
    }

    @SuppressWarnings("unchecked")
    private void readGlobalPartnerParametersI(Context context) {
        try {
            globalParameters.partnerParameters = Util.readObject(context,
                    GLOBAL_PARTNER_PARAMETERS_FILENAME,
                    GLOBAL_PARTNER_PARAMETERS_NAME,
                    (Class<Map<String,String>>)(Class)Map.class);
        } catch (Exception e) {
            logger.error("Failed to read %s file (%s)", GLOBAL_PARTNER_PARAMETERS_NAME, e.getMessage());
            globalParameters.partnerParameters = null;
        }
    }

    private void writeActivityStateI() {
        synchronized (ActivityState.class) {
            if (activityState == null) {
                return;
            }
            Util.writeObject(activityState, adjustConfig.context, ACTIVITY_STATE_FILENAME, ACTIVITY_STATE_NAME);
        }
    }

    private void teardownActivityStateS() {
        synchronized (ActivityState.class) {
            if (activityState == null) {
                return;
            }
            activityState = null;
        }
    }

    private void writeAttributionI() {
        synchronized (AdjustAttribution.class) {
            if (attribution == null) {
                return;
            }
            Util.writeObject(attribution, adjustConfig.context, ATTRIBUTION_FILENAME, ATTRIBUTION_NAME);
        }
    }

    private void teardownAttributionS() {
        synchronized (AdjustAttribution.class) {
            if (attribution == null) {
                return;
            }
            attribution = null;
        }
    }

    private void writeGlobalCallbackParametersI() {
        synchronized (GlobalParameters.class) {
            if (globalParameters == null) {
                return;
            }
            Util.writeObject(globalParameters.callbackParameters, adjustConfig.context, GLOBAL_CALLBACK_PARAMETERS_FILENAME, GLOBAL_CALLBACK_PARAMETERS_NAME);
        }
    }

    private void writeGlobalPartnerParametersI() {
        synchronized (GlobalParameters.class) {
            if (globalParameters == null) {
                return;
            }
            Util.writeObject(globalParameters.partnerParameters, adjustConfig.context, GLOBAL_PARTNER_PARAMETERS_FILENAME, GLOBAL_PARTNER_PARAMETERS_NAME);
        }
    }

    private void teardownAllGlobalParametersS() {
        synchronized (GlobalParameters.class) {
            if (globalParameters == null) {
                return;
            }
            globalParameters = null;
        }
    }

    private boolean checkEventI(AdjustEvent event) {
        if (event == null) {
            logger.error("Event missing");
            return false;
        }

        if (!event.isValid()) {
            logger.error("Event not initialized correctly");
            return false;
        }

        return true;
    }

    private boolean shouldProcessEventI(String deduplicationId) {
        if (deduplicationId == null || deduplicationId.isEmpty()) {
            return true;  // no deduplication ID given
        }

        if (activityState.eventDeduplicationIdExists(deduplicationId)) {
            logger.info("Skipping duplicate event with deduplication ID '%s'", deduplicationId);
            return false; // deduplication ID found -> used already
        }

        activityState.addDeduplicationId(deduplicationId);
        logger.verbose("Added deduplication ID '%s'", deduplicationId);
        // activity state will get written by caller
        return true;
    }

    private boolean checkAdjustAdRevenue(AdjustAdRevenue adjustAdRevenue) {
        if (adjustAdRevenue == null) {
            logger.error("Ad revenue object missing");
            return false;
        }

        if (!adjustAdRevenue.isValid()) {
            logger.error("Ad revenue object not initialized correctly");
            return false;
        }

        return true;
    }

    private boolean checkActivityStateI(ActivityState activityState) {
        if (internalState.hasFirstSdkStartNotOcurred()) {
            logger.error("Sdk did not yet start");
            return false;
        }
        return true;
    }

    private boolean pausedI() {
        return pausedI(false);
    }

    private boolean pausedI(boolean sdkClickHandlerOnly) {
        if (sdkClickHandlerOnly) {
            // sdk click handler is paused if either:
            return internalState.isOffline() ||     // it's offline
                    !isEnabledI();                  // is disabled
        }
        // other handlers are paused if either:
        return internalState.isOffline()    ||      // it's offline
                !isEnabledI();                      // is disabled
    }

    private boolean toSendI() {
        return toSendI(false);
    }

    private boolean toSendI(boolean sdkClickHandlerOnly) {
        // don't send when it's paused
        if (pausedI(sdkClickHandlerOnly)) {
            return false;
        }

        // has the option to send in the background -> is to send
        if (adjustConfig.isSendingInBackgroundEnabled) {
            return true;
        }

        // doesn't have the option -> depends on being on the background/foreground
        return internalState.isInForeground();
    }

    private void checkForInstallReferrerInfo(final SdkClickResponseData responseData) {
        if (!responseData.isInstallReferrer) {
            return;
        }

        boolean isInstallReferrerHuaweiAds =
                responseData.referrerApi != null &&
                (responseData.referrerApi.equalsIgnoreCase(Constants.REFERRER_API_HUAWEI_ADS));
        if (isInstallReferrerHuaweiAds) {
            activityState.clickTimeHuawei = responseData.clickTime;
            activityState.installBeginHuawei    = responseData.installBegin;
            activityState.installReferrerHuawei = responseData.installReferrer;

            writeActivityStateI();
            return;
        }

        boolean isInstallReferrerHuaweiAppGallery =
                responseData.referrerApi != null &&
                (responseData.referrerApi.equalsIgnoreCase(Constants.REFERRER_API_HUAWEI_APP_GALLERY));

        if (isInstallReferrerHuaweiAppGallery) {
            activityState.clickTimeHuawei = responseData.clickTime;
            activityState.installBeginHuawei = responseData.installBegin;
            activityState.installReferrerHuaweiAppGallery = responseData.installReferrer;

            writeActivityStateI();
            return;
        }

        boolean isInstallReferrerMeta =
                responseData.referrerApi != null &&
                        (responseData.referrerApi.equalsIgnoreCase(REFERRER_API_META));

        if (isInstallReferrerMeta) {
            activityState.clickTimeMeta = responseData.clickTime;
            activityState.installReferrerMeta = responseData.installReferrer;
            activityState.isClickMeta = responseData.isClick;

            writeActivityStateI();
            return;
        }

        boolean isInstallReferrerSamsung =
                responseData.referrerApi != null &&
                (responseData.referrerApi.equalsIgnoreCase(REFERRER_API_SAMSUNG));

        if (isInstallReferrerSamsung) {
            activityState.clickTimeSamsung = responseData.clickTime;
            activityState.installBeginSamsung = responseData.installBegin;
            activityState.installReferrerSamsung = responseData.installReferrer;

            writeActivityStateI();
            return;
        }

        boolean isInstallReferrerXiaomi =
                responseData.referrerApi != null &&
                (responseData.referrerApi.equalsIgnoreCase(REFERRER_API_XIAOMI));

        if (isInstallReferrerXiaomi) {
            activityState.clickTimeXiaomi = responseData.clickTime;
            activityState.installBeginXiaomi = responseData.installBegin;
            activityState.installReferrerXiaomi = responseData.installReferrer;
            activityState.clickTimeServerXiaomi = responseData.clickTimeServer;
            activityState.installBeginServerXiaomi = responseData.installBeginServer;
            activityState.installVersionXiaomi = responseData.installVersion;

            writeActivityStateI();
            return;
        }

        boolean isInstallReferrerVivo =
                responseData.referrerApi != null &&
                (responseData.referrerApi.equalsIgnoreCase(REFERRER_API_VIVO));

        if (isInstallReferrerVivo) {
            activityState.clickTimeVivo = responseData.clickTime;
            activityState.installBeginVivo = responseData.installBegin;
            activityState.installReferrerVivo = responseData.installReferrer;
            activityState.installVersionVivo = responseData.installVersion;

            writeActivityStateI();
            return;
        }

        activityState.clickTime = responseData.clickTime;
        activityState.installBegin = responseData.installBegin;
        activityState.installReferrer = responseData.installReferrer;
        activityState.clickTimeServer = responseData.clickTimeServer;
        activityState.installBeginServer = responseData.installBeginServer;
        activityState.installVersion = responseData.installVersion;
        activityState.googlePlayInstant = responseData.googlePlayInstant;

        writeActivityStateI();
    }

    private void processCoppaComplianceI() {
        if (!adjustConfig.coppaComplianceEnabled) {
            resetThirdPartySharingCoppaActivityStateI();
            return;
        }

        disableThirdPartySharingForCoppaEnabledI();
    }

    private void disableThirdPartySharingForCoppaEnabledI() {
        if (!shouldDisableThirdPartySharingWhenCoppaEnabled()) {
            return;
        }

        activityState.isThirdPartySharingDisabledForCoppa = true;
        writeActivityStateI();
        AdjustThirdPartySharing adjustThirdPartySharing =
                new AdjustThirdPartySharing(false);

        long now = System.currentTimeMillis();
        PackageBuilder packageBuilder = new PackageBuilder(
                adjustConfig, deviceInfo, activityState, globalParameters, now);

        ActivityPackage activityPackage =
                packageBuilder.buildThirdPartySharingPackage(adjustThirdPartySharing);
        packageHandler.addPackage(activityPackage);

        packageHandler.sendFirstPackage();
    }

    private void resetThirdPartySharingCoppaActivityStateI() {

        if (activityState == null) { return; }
        if (activityState.isThirdPartySharingDisabledForCoppa) {
            activityState.isThirdPartySharingDisabledForCoppa = false;
            writeActivityStateI();
        }
    }

    private boolean shouldDisableThirdPartySharingWhenCoppaEnabled() {
        if (activityState == null) {
            return false;
        }

        if (!isEnabledI()) {
            return false;
        }

        if (activityState.isGdprForgotten) {
            return false;
        }

        return !activityState.isThirdPartySharingDisabledForCoppa;
    }
}
