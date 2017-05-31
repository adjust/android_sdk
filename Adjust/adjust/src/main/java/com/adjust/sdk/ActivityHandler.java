//
//  ActivityHandler.java
//  Adjust
//
//  Created by Christian Wellenbrock on 2013-06-25.
//  Copyright (c) 2013 adjust GmbH. All rights reserved.
//  See the file MIT-LICENSE for copying permission.
//

package com.adjust.sdk;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.net.UrlQuerySanitizer;
import android.os.Handler;
import android.util.*;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static com.adjust.sdk.Constants.ACTIVITY_STATE_FILENAME;
import static com.adjust.sdk.Constants.ATTRIBUTION_FILENAME;
import static com.adjust.sdk.Constants.SESSION_CALLBACK_PARAMETERS_FILENAME;
import static com.adjust.sdk.Constants.SESSION_PARTNER_PARAMETERS_FILENAME;

public class ActivityHandler implements IActivityHandler {
    private static long FOREGROUND_TIMER_INTERVAL;
    private static long FOREGROUND_TIMER_START;
    private static long BACKGROUND_TIMER_INTERVAL;
    private static long SESSION_INTERVAL;
    private static long SUBSESSION_INTERVAL;
    private static final String TIME_TRAVEL = "Time travel!";
    private static final String ADJUST_PREFIX = "adjust_";
    private static final String ACTIVITY_STATE_NAME = "Activity state";
    private static final String ATTRIBUTION_NAME = "Attribution";
    private static final String FOREGROUND_TIMER_NAME = "Foreground timer";
    private static final String BACKGROUND_TIMER_NAME = "Background timer";
    private static final String DELAY_START_TIMER_NAME = "Delay Start timer";
    private static final String SESSION_CALLBACK_PARAMETERS_NAME = "Session Callback parameters";
    private static final String SESSION_PARTNER_PARAMETERS_NAME = "Session Partner parameters";

    private CustomScheduledExecutor scheduledExecutor;
    private IPackageHandler packageHandler;
    private ActivityState activityState;
    private ILogger logger;
    private TimerCycle foregroundTimer;
    private TimerOnce backgroundTimer;
    private TimerOnce delayStartTimer;
    private InternalState internalState;
    private String basePath;

    private DeviceInfo deviceInfo;
    private AdjustConfig adjustConfig; // always valid after construction
    private AdjustAttribution attribution;
    private IAttributionHandler attributionHandler;
    private ISdkClickHandler sdkClickHandler;
    private SessionParameters sessionParameters;

    @Override
    public void teardown(boolean deleteState) {
        if (backgroundTimer != null) {
            backgroundTimer.teardown();
        }
        if (foregroundTimer != null) {
            foregroundTimer.teardown();
        }
        if (delayStartTimer != null) {
            delayStartTimer.teardown();
        }
        if (scheduledExecutor != null) {
            try {
                scheduledExecutor.shutdownNow();
            } catch(SecurityException se) {}
        }
        if (packageHandler != null) {
            packageHandler.teardown(deleteState);
        }
        if (attributionHandler != null) {
            attributionHandler.teardown();
        }
        if (sdkClickHandler != null) {
            sdkClickHandler.teardown();
        }
        if (sessionParameters != null) {
            if (sessionParameters.callbackParameters != null) {
                sessionParameters.callbackParameters.clear();
            }
            if (sessionParameters.partnerParameters != null) {
                sessionParameters.partnerParameters.clear();
            }
        }

        teardownActivityStateS(deleteState);
        teardownAttributionS(deleteState);
        teardownAllSessionParametersS(deleteState);

        packageHandler = null;
        logger = null;
        foregroundTimer = null;
        scheduledExecutor = null;
        backgroundTimer = null;
        delayStartTimer = null;
        internalState = null;
        deviceInfo = null;
        adjustConfig = null;
        attributionHandler = null;
        sdkClickHandler = null;
        sessionParameters = null;
    }

    static void deleteState(Context context) {
        deleteActivityState(context);
        deleteAttribution(context);
        deleteSessionCallbackParameters(context);
        deleteSessionPartnerParameters(context);
    }

    public class InternalState {
        boolean enabled;
        boolean offline;
        boolean background;
        boolean delayStart;
        boolean updatePackages;
        boolean firstLaunch;
        boolean sessionResponseProcessed;

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

        public boolean isBackground() {
            return background;
        }

        public boolean isForeground() {
            return !background;
        }

        public boolean isDelayStart() {
            return delayStart;
        }

        public boolean isToStartNow() {
            return !delayStart;
        }

        public boolean isToUpdatePackages() {
            return updatePackages;
        }

        public boolean isFirstLaunch() {
            return firstLaunch;
        }

        public boolean isSessionResponseProcessed() {
            return sessionResponseProcessed;
        }
    }

    private ActivityHandler(AdjustConfig adjustConfig) {
        init(adjustConfig);

        // init logger to be available everywhere
        logger = AdjustFactory.getLogger();

        logger.lockLogLevel();

        scheduledExecutor = new CustomScheduledExecutor("ActivityHandler", false);
        internalState = new InternalState();

        // enabled by default
        internalState.enabled = true;
        // online by default
        internalState.offline = false;
        // in the background by default
        internalState.background = true;
        // delay start not configured by default
        internalState.delayStart = false;
        // does not need to update packages by default
        internalState.updatePackages = false;
        // does not have the session response by default
        internalState.sessionResponseProcessed = false;

        scheduledExecutor.submit(new Runnable() {
            @Override
            public void run() {
                initI();
            }
        });
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

            for (ActivityManager.RunningAppProcessInfo processInfo : manager.getRunningAppProcesses()) {
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
        internalState.background = false;

        scheduledExecutor.submit(new Runnable() {
            @Override
            public void run() {
                delayStartI();

                stopBackgroundTimerI();

                startForegroundTimerI();

                logger.verbose("Subsession start");

                startI();
            }
        });
    }

    @Override
    public void onPause() {
        internalState.background = true;

        scheduledExecutor.submit(new Runnable() {
            @Override
            public void run() {
                stopForegroundTimerI();

                startBackgroundTimerI();

                logger.verbose("Subsession end");

                endI();
            }
        });
    }

    @Override
    public void trackEvent(final AdjustEvent event) {
        scheduledExecutor.submit(new Runnable() {
            @Override
            public void run() {
                if (activityState == null) {
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
            attributionHandler.checkSessionResponse((SessionResponseData)responseData);
            return;
        }
        // redirect sdk click responses to attribution handler to check for attribution information
        if (responseData instanceof SdkClickResponseData) {
            attributionHandler.checkSdkClickResponse((SdkClickResponseData)responseData);
            return;
        }
        // check if it's an event response
        if (responseData instanceof EventResponseData) {
            launchEventResponseTasks((EventResponseData)responseData);
            return;
        }
    }

    @Override
    public void setEnabled(final boolean enabled) {
        // compare with the saved or internal state
        if (!hasChangedState(this.isEnabled(), enabled,
                "Adjust already enabled", "Adjust already disabled")) {
            return;
        }

        // save new enabled state in internal state
        internalState.enabled = enabled;

        if (activityState == null) {
            updateStatus(!enabled,
                    "Handlers will start as paused due to the SDK being disabled",
                    "Handlers will still start as paused",
                    "Handlers will start as active due to the SDK being enabled");
            return;
        }

        // save new enabled state in activity state
        Runnable saveEnabled = new Runnable() {
            @Override
            public void run() {
                activityState.enabled = enabled;
            }
        };
        writeActivityStateS(saveEnabled);

        updateStatus(!enabled,
                "Pausing handlers due to SDK being disabled",
                "Handlers remain paused",
                "Resuming handlers due to SDK being enabled");
    }

    private void updateStatus(boolean pausingState, String pausingMessage,
                              String remainsPausedMessage, String unPausingMessage)
    {
        // it is changing from an active state to a pause state
        if (pausingState) {
            logger.info(pausingMessage);
        }
        // check if it's remaining in a pause state
        else if (pausedI(false)) {                      // safe to use internal version of paused (read only), can suffer from phantom read but not an issue
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

        updateHandlersStatusAndSend();
    }

    private boolean hasChangedState(boolean previousState, boolean newState,
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

    @Override
    public void setOfflineMode(boolean offline) {
        // compare with the internal state
        if (!hasChangedState(internalState.isOffline(), offline,
                "Adjust already in offline mode",
                "Adjust already in online mode")) {
            return;
        }

        internalState.offline = offline;

        if (activityState == null) {
            updateStatus(offline,
                    "Handlers will start paused due to SDK being offline",
                    "Handlers will still start as paused",
                    "Handlers will start as active due to SDK being online");
            return;
        }

        updateStatus(offline,
                "Pausing handlers to put SDK offline mode",
                "Handlers remain paused",
                "Resuming handlers to put SDK in online mode");
    }

    @Override
    public boolean isEnabled() {
        return isEnabledI();
    }

    private boolean isEnabledI() {
        if (activityState != null) {
            return activityState.enabled;
        } else {
            return internalState.isEnabled();
        }
    }

    @Override
    public void readOpenUrl(final Uri url, final long clickTime) {
        scheduledExecutor.submit(new Runnable() {
            @Override
            public void run() {
                readOpenUrlI(url, clickTime);
            }
        });
    }

    private void updateAdidI(final String adid) {
        if (adid == null) {
            return;
        }

        if (adid.equals(activityState.adid)) {
            return;
        }

        activityState.adid = adid;
        writeActivityStateI();
        return;
    }

    @Override
    public boolean updateAttributionI(AdjustAttribution attribution) {
        if (attribution == null) {
            return false;
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
        Runnable saveAskingAttribution = new Runnable() {
            @Override
            public void run() {
                activityState.askingAttribution = askingAttribution;
            }
        };

        writeActivityStateS(saveAskingAttribution);
    }

    @Override
    public void sendReferrer(final String referrer, final long clickTime) {
        scheduledExecutor.submit(new Runnable() {
            @Override
            public void run() {
                sendReferrerI(referrer, clickTime);
            }
        });
    }

    @Override
    public void launchEventResponseTasks(final EventResponseData eventResponseData) {
        scheduledExecutor.submit(new Runnable() {
            @Override
            public void run() {
                launchEventResponseTasksI(eventResponseData);
            }
        });
    }

    @Override
    public void launchSdkClickResponseTasks(final SdkClickResponseData sdkClickResponseData) {
        scheduledExecutor.submit(new Runnable() {
            @Override
            public void run() {
                launchSdkClickResponseTasksI(sdkClickResponseData);
            }
        });
    }

    @Override
    public void launchSessionResponseTasks(final SessionResponseData sessionResponseData) {
        scheduledExecutor.submit(new Runnable() {
            @Override
            public void run() {
                launchSessionResponseTasksI(sessionResponseData);
            }
        });
    }

    @Override
    public void launchAttributionResponseTasks(final AttributionResponseData attributionResponseData) {
        scheduledExecutor.submit(new Runnable() {
            @Override
            public void run() {
                launchAttributionResponseTasksI(attributionResponseData);
            }
        });
    }

    @Override
    public void sendFirstPackages () {
        scheduledExecutor.submit(new Runnable() {
            @Override
            public void run() {
                sendFirstPackagesI();
            }
        });
    }

    @Override
    public void addSessionCallbackParameter(final String key, final String value) {
        scheduledExecutor.submit(new Runnable() {
            @Override
            public void run() {
                addSessionCallbackParameterI(key, value);
            }
        });
    }

    @Override
    public void addSessionPartnerParameter(final String key, final String value) {
        scheduledExecutor.submit(new Runnable() {
            @Override
            public void run() {
                addSessionPartnerParameterI(key, value);
            }
        });
    }

    @Override
    public void removeSessionCallbackParameter(final String key) {
        scheduledExecutor.submit(new Runnable() {
            @Override
            public void run() {
                removeSessionCallbackParameterI(key);
            }
        });
    }

    @Override
    public void removeSessionPartnerParameter(final String key) {
        scheduledExecutor.submit(new Runnable() {
            @Override
            public void run() {
                removeSessionPartnerParameterI(key);
            }
        });
    }

    @Override
    public void resetSessionCallbackParameters() {
        scheduledExecutor.submit(new Runnable() {
            @Override
            public void run() {
                resetSessionCallbackParametersI();
            }
        });
    }

    @Override
    public void resetSessionPartnerParameters() {
        scheduledExecutor.submit(new Runnable() {
            @Override
            public void run() {
                resetSessionPartnerParametersI();
            }
        });
    }

    @Override
    public void setPushToken(final String token) {
        scheduledExecutor.submit(new Runnable() {
            @Override
            public void run() {
                if (activityState == null) {
                    startI();
                }
                setPushTokenI(token);
            }
        });
    }

    public void foregroundTimerFired() {
        scheduledExecutor.submit(new Runnable() {
            @Override
            public void run() {
                foregroundTimerFiredI();
            }
        });
    }

    public void backgroundTimerFired() {
        scheduledExecutor.submit(new Runnable() {
            @Override
            public void run() {
                backgroundTimerFiredI();
            }
        });
    }

    @Override
    public String getAdid() {
        if (activityState == null) {
            return null;
        }
        return activityState.adid;
    }

    @Override
    public AdjustAttribution getAttribution() {
        return attribution;
    }

    @Override
    public String getBasePath() {
        return this.basePath;
    }

    public ActivityPackage getAttributionPackageI() {
        long now = System.currentTimeMillis();
        PackageBuilder attributionBuilder = new PackageBuilder(adjustConfig,
                deviceInfo,
                activityState,
                now);
        return attributionBuilder.buildAttributionPackage();
    }

    public InternalState getInternalState() {
        return internalState;
    }

    private void updateHandlersStatusAndSend() {
        scheduledExecutor.submit(new Runnable() {
            @Override
            public void run() {
                updateHandlersStatusAndSendI();
            }
        });
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

        sessionParameters = new SessionParameters();
        readSessionCallbackParametersI(adjustConfig.context);
        readSessionPartnerParametersI(adjustConfig.context);

        if (activityState != null) {
            internalState.enabled = activityState.enabled;
            internalState.updatePackages = activityState.updatePackages;
            internalState.firstLaunch = false;
        } else {
            internalState.firstLaunch = true; // first launch if activity state is null
        }

        readConfigFile(adjustConfig.context);

        deviceInfo = new DeviceInfo(adjustConfig.context, adjustConfig.sdkPrefix);

        if (adjustConfig.eventBufferingEnabled) {
            logger.info("Event buffering is enabled");
        }

        String playAdId = Util.getPlayAdId(adjustConfig.context);
        if (playAdId == null) {
            logger.warn("Unable to get Google Play Services Advertising ID at start time");
            if (deviceInfo.macSha1 == null &&
                    deviceInfo.macShortMd5 == null &&
                    deviceInfo.androidId == null)
            {
                logger.error("Unable to get any device id's. Please check if Proguard is correctly set with Adjust SDK");
            }
        } else {
            logger.info("Google Play Services Advertising ID read correctly at start time");
        }

        if (adjustConfig.defaultTracker != null) {
            logger.info("Default tracker: '%s'", adjustConfig.defaultTracker);
        }

        if (adjustConfig.pushToken != null) {
            logger.info("Push token: '%s'", adjustConfig.pushToken);
            if (activityState != null) {
                setPushToken(adjustConfig.pushToken);
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
        if (adjustConfig.sendInBackground) {
            logger.info("Send in background configured");

            backgroundTimer = new TimerOnce(new Runnable() {
                @Override
                public void run() {
                    backgroundTimerFired();
                }
            }, BACKGROUND_TIMER_NAME);
        }

        // configure delay start timer
        if (activityState == null &&
                adjustConfig.delayStart != null &&
                adjustConfig.delayStart > 0.0)
        {
            logger.info("Delay start configured");
            internalState.delayStart = true;
            delayStartTimer = new TimerOnce(new Runnable() {
                @Override
                public void run() {
                    sendFirstPackages();
                }
            }, DELAY_START_TIMER_NAME);
        }

        UtilNetworking.setUserAgent(adjustConfig.userAgent);

        this.basePath = adjustConfig.basePath;

        packageHandler = AdjustFactory.getPackageHandler(this, adjustConfig.context, toSendI(false));

        ActivityPackage attributionPackage = getAttributionPackageI();

        attributionHandler = AdjustFactory.getAttributionHandler(this,
                attributionPackage,
                toSendI(false));

        sdkClickHandler = AdjustFactory.getSdkClickHandler(this, toSendI(true));

        if (isToUpdatePackagesI()) {
            updatePackagesI();
        }

        if (adjustConfig.referrer != null) {
            sendReferrerI(adjustConfig.referrer, adjustConfig.referrerClickTime);
        }

        sessionParametersActionsI(adjustConfig.sessionParametersActionsArray);
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

    private void sessionParametersActionsI(List<IRunActivityHandler> sessionParametersActionsArray) {
        if (sessionParametersActionsArray == null) {
            return;
        }

        for (IRunActivityHandler sessionParametersAction : sessionParametersActionsArray) {
            sessionParametersAction.run(this);
        }
    }

    private void startI() {
        // it shouldn't start if it was disabled after a first session
        if (activityState != null
                && !activityState.enabled) {
            return;
        }

        updateHandlersStatusAndSendI();

        processSessionI();

        checkAttributionStateI();
    }

    private void processSessionI() {
        long now = System.currentTimeMillis();

        // very first session
        if (activityState == null) {
            activityState = new ActivityState();
            activityState.sessionCount = 1; // this is the first session
            activityState.pushToken = adjustConfig.pushToken;

            transferSessionPackageI(now);
            activityState.resetSessionAttributes(now);
            activityState.enabled = internalState.isEnabled();
            activityState.updatePackages = internalState.isToUpdatePackages();
            writeActivityStateI();
            return;
        }

        long lastInterval = now - activityState.lastActivity;

        if (lastInterval < 0) {
            logger.error(TIME_TRAVEL);
            activityState.lastActivity = now;
            writeActivityStateI();
            return;
        }

        // new session
        if (lastInterval > SESSION_INTERVAL) {
            activityState.sessionCount++;
            activityState.lastInterval = lastInterval;

            transferSessionPackageI(now);
            activityState.resetSessionAttributes(now);
            writeActivityStateI();
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
            return;
        }

        logger.verbose("Time span since last activity too short for a new subsession");
    }

    private void checkAttributionStateI() {
        if (!checkActivityStateI(activityState)) { return; }

        // if it's the first launch
        if (internalState.isFirstLaunch()) {
            // and it hasn't received the session response
            if (!internalState.isSessionResponseProcessed()) {
                return;
            }
        }

        // if there is already an attribution saved and there was no attribution being asked
        if (attribution != null && !activityState.askingAttribution) {
            return;
        }

        attributionHandler.getAttribution();
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
        if (!checkOrderIdI(event.orderId)) return;

        long now = System.currentTimeMillis();

        activityState.eventCount++;
        updateActivityStateI(now);

        PackageBuilder eventBuilder = new PackageBuilder(adjustConfig, deviceInfo, activityState, now);
        ActivityPackage eventPackage = eventBuilder.buildEventPackage(event, sessionParameters, internalState.isDelayStart());
        packageHandler.addPackage(eventPackage);

        if (adjustConfig.eventBufferingEnabled) {
            logger.info("Buffered event %s", eventPackage.getSuffix());
        } else {
            packageHandler.sendFirstPackage();
        }

        // if it is in the background and it can send, start the background timer
        if (adjustConfig.sendInBackground && internalState.isBackground()) {
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
                    adjustConfig.onEventTrackingSucceededListener.onFinishedEventTrackingSucceeded(eventResponseData.getSuccessResponseData());
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
                    adjustConfig.onEventTrackingFailedListener.onFinishedEventTrackingFailed(eventResponseData.getFailureResponseData());
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
    }

    private void launchSessionResponseTasksI(SessionResponseData sessionResponseData) {
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
                    adjustConfig.onSessionTrackingSucceededListener.onFinishedSessionTrackingSucceeded(sessionResponseData.getSuccessResponseData());
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
                    adjustConfig.onSessionTrackingFailedListener.onFinishedSessionTrackingFailed(sessionResponseData.getFailureResponseData());
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
                if (adjustConfig.onDeeplinkResponseListener != null) {
                    toLaunchDeeplink = adjustConfig.onDeeplinkResponseListener.launchReceivedDeeplink(deeplink);
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
        if (adjustConfig.deepLinkComponent == null) {
            mapIntent = new Intent(Intent.ACTION_VIEW, deeplink);
        } else {
            mapIntent = new Intent(Intent.ACTION_VIEW, deeplink, adjustConfig.context, adjustConfig.deepLinkComponent);
        }
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
            logger.error("Unable to open deferred deep link (%s)", deeplink);
            return;
        }

        // add it to the handler queue
        logger.info("Open deferred deep link (%s)", deeplink);
        adjustConfig.context.startActivity(deeplinkIntent);
    }

    private void sendReferrerI(String referrer, long clickTime) {
        if (referrer == null || referrer.length() == 0 ) {
            return;
        }

        logger.verbose("Referrer to parse (%s)", referrer);

        UrlQuerySanitizer querySanitizer = new UrlQuerySanitizer();
        querySanitizer.setUnregisteredParameterValueSanitizer(UrlQuerySanitizer.getAllButNulLegal());
        querySanitizer.setAllowUnregisteredParamaters(true);
        querySanitizer.parseQuery(referrer);

        PackageBuilder clickPackageBuilder = queryStringClickPackageBuilderI(querySanitizer.getParameterList());

        if (clickPackageBuilder == null) {
            return;
        }

        clickPackageBuilder.referrer = referrer;
        clickPackageBuilder.clickTime = clickTime;
        ActivityPackage clickPackage = clickPackageBuilder.buildClickPackage(Constants.REFTAG, sessionParameters);

        sdkClickHandler.sendSdkClick(clickPackage);
    }

    private void readOpenUrlI(Uri url, long clickTime) {
        if (url == null) {
            return;
        }

        String urlString = url.toString();
        if (urlString == null || urlString.length() == 0) {
            return;
        }
        logger.verbose("Url to parse (%s)", url);

        UrlQuerySanitizer querySanitizer = new UrlQuerySanitizer();
        querySanitizer.setUnregisteredParameterValueSanitizer(UrlQuerySanitizer.getAllButNulLegal());
        querySanitizer.setAllowUnregisteredParamaters(true);
        querySanitizer.parseUrl(urlString);

        PackageBuilder clickPackageBuilder = queryStringClickPackageBuilderI(querySanitizer.getParameterList());
        if (clickPackageBuilder == null) {
            return;
        }

        clickPackageBuilder.deeplink = url.toString();
        clickPackageBuilder.clickTime = clickTime;
        ActivityPackage clickPackage = clickPackageBuilder.buildClickPackage(Constants.DEEPLINK, sessionParameters);

        sdkClickHandler.sendSdkClick(clickPackage);
    }

    private PackageBuilder queryStringClickPackageBuilderI(
            List<UrlQuerySanitizer.ParameterValuePair> queryList)
    {
        if (queryList == null) {
            return null;
        }

        Map<String, String> queryStringParameters = new LinkedHashMap<String, String>();
        AdjustAttribution queryStringAttribution = new AdjustAttribution();

        for (UrlQuerySanitizer.ParameterValuePair parameterValuePair : queryList) {
            readQueryStringI(parameterValuePair.mParameter,
                    parameterValuePair.mValue,
                    queryStringParameters, queryStringAttribution);
        }

        String reftag = queryStringParameters.remove(Constants.REFTAG);

        long now = System.currentTimeMillis();
        // check of activity state != null
        //  because referrer can be called before onResume
        if (activityState != null) {
            long lastInterval = now - activityState.lastActivity;
            activityState.lastInterval = lastInterval;
        }
        PackageBuilder builder = new PackageBuilder(adjustConfig, deviceInfo, activityState, now);
        builder.extraParameters = queryStringParameters;
        builder.attribution = queryStringAttribution;
        builder.reftag = reftag;

        return builder;
    }

    private boolean readQueryStringI(String key, String value,
                                     Map<String, String> extraParameters,
                                     AdjustAttribution queryStringAttribution) {
        if (key == null || value == null) { return false; }

        // parameter key does not start with "adjust_"
        if (!key.startsWith(ADJUST_PREFIX)) { return false; }

        String keyWOutPrefix = key.substring(ADJUST_PREFIX.length());
        if (keyWOutPrefix.length() == 0) { return false; }

        if (value.length() == 0) { return false;}

        if (!trySetAttributionI(queryStringAttribution, keyWOutPrefix, value)) {
            extraParameters.put(keyWOutPrefix, value);
        }

        return true;
    }

    private boolean trySetAttributionI(AdjustAttribution queryStringAttribution,
                                       String key,
                                       String value) {
        if (key.equals("tracker")) {
            queryStringAttribution.trackerName = value;
            return true;
        }

        if (key.equals("campaign")) {
            queryStringAttribution.campaign = value;
            return true;
        }

        if (key.equals("adgroup")) {
            queryStringAttribution.adgroup = value;
            return true;
        }

        if (key.equals("creative")) {
            queryStringAttribution.creative = value;
            return true;
        }

        return false;
    }

    private void updateHandlersStatusAndSendI() {
        // check if it should stop sending
        if (!toSendI()) {
            pauseSendingI();
            return;
        }

        resumeSendingI();

        // try to send
        if (!adjustConfig.eventBufferingEnabled) {
            packageHandler.sendFirstPackage();
        }
    }

    private void pauseSendingI() {
        attributionHandler.pauseSending();
        packageHandler.pauseSending();
        // the conditions to pause the sdk click handler are less restrictive
        // it's possible for the sdk click handler to be active while others are paused
        if (!toSendI(true)) {
            sdkClickHandler.pauseSending();
        } else {
            sdkClickHandler.resumeSending();
        }
    }

    private void resumeSendingI() {
        attributionHandler.resumeSending();
        packageHandler.resumeSending();
        sdkClickHandler.resumeSending();
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

    public static boolean deleteSessionCallbackParameters(Context context) {
        return context.deleteFile(SESSION_CALLBACK_PARAMETERS_FILENAME);
    }

    public static boolean deleteSessionPartnerParameters(Context context) {
        return context.deleteFile(SESSION_PARTNER_PARAMETERS_FILENAME);
    }

    private void transferSessionPackageI(long now) {
        PackageBuilder builder = new PackageBuilder(adjustConfig, deviceInfo, activityState, now);
        ActivityPackage sessionPackage = builder.buildSessionPackage(sessionParameters, internalState.isDelayStart());
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

    private void delayStartI() {
        // it's not configured to start delayed or already finished
        if (internalState.isToStartNow()) {
            return;
        }

        // the delay has already started
        if (isToUpdatePackagesI()) {
            return;
        }

        // check against max start delay
        double delayStartSeconds = adjustConfig.delayStart != null ? adjustConfig.delayStart : 0.0;
        long maxDelayStartMilli = AdjustFactory.getMaxDelayStart();

        long delayStartMilli = (long)(delayStartSeconds * 1000);
        if (delayStartMilli > maxDelayStartMilli) {
            double maxDelayStartSeconds = maxDelayStartMilli / 1000;
            String delayStartFormatted = Util.SecondsDisplayFormat.format(delayStartSeconds);
            String maxDelayStartFormatted = Util.SecondsDisplayFormat.format(maxDelayStartSeconds);

            logger.warn("Delay start of %s seconds bigger than max allowed value of %s seconds", delayStartFormatted, maxDelayStartFormatted);
            delayStartMilli = maxDelayStartMilli;
            delayStartSeconds = maxDelayStartSeconds;
        }

        String delayStartFormatted = Util.SecondsDisplayFormat.format(delayStartSeconds);
        logger.info("Waiting %s seconds before starting first session", delayStartFormatted);

        delayStartTimer.startIn(delayStartMilli);

        internalState.updatePackages = true;

        if (activityState != null) {
            activityState.updatePackages = true;
            writeActivityStateI();
        }
    }

    private void sendFirstPackagesI() {
        if (internalState.isToStartNow()) {
            logger.info("Start delay expired or never configured");
            return;
        }

        // update packages in queue
        updatePackagesI();
        // no longer is in delay start
        internalState.delayStart = false;
        // cancel possible still running timer if it was called by user
        delayStartTimer.cancel();
        // and release timer
        delayStartTimer = null;
        // update the status and try to send first package
        updateHandlersStatusAndSendI();
    }

    private void updatePackagesI() {
        // update activity packages
        packageHandler.updatePackages(sessionParameters);
        // no longer needs to update packages
        internalState.updatePackages = false;
        if (activityState != null) {
            activityState.updatePackages = false;
            writeActivityStateI();
        }
    }

    private boolean isToUpdatePackagesI() {
        if (activityState != null) {
            return activityState.updatePackages;
        } else {
            return internalState.isToUpdatePackages();
        }
    }

    public void addSessionCallbackParameterI(String key, String value) {
        if (!Util.isValidParameter(key, "key", "Session Callback")) return;
        if (!Util.isValidParameter(value, "value", "Session Callback")) return;

        if (sessionParameters.callbackParameters == null) {
            sessionParameters.callbackParameters = new LinkedHashMap<String, String>();
        }

        String oldValue = sessionParameters.callbackParameters.get(key);

        if (value.equals(oldValue)) {
            logger.verbose("Key %s already present with the same value", key);
            return;
        }

        if (oldValue != null) {
            logger.warn("Key %s will be overwritten", key);
        }

        sessionParameters.callbackParameters.put(key, value);

        writeSessionCallbackParametersI();
    }

    public void addSessionPartnerParameterI(String key, String value) {
        if (!Util.isValidParameter(key, "key", "Session Partner")) return;
        if (!Util.isValidParameter(value, "value", "Session Partner")) return;

        if (sessionParameters.partnerParameters == null) {
            sessionParameters.partnerParameters = new LinkedHashMap<String, String>();
        }

        String oldValue = sessionParameters.partnerParameters.get(key);

        if (value.equals(oldValue)) {
            logger.verbose("Key %s already present with the same value", key);
            return;
        }

        if (oldValue != null) {
            logger.warn("Key %s will be overwritten", key);
        }

        sessionParameters.partnerParameters.put(key, value);

        writeSessionPartnerParametersI();
    }

    public void removeSessionCallbackParameterI(String key) {
        if (!Util.isValidParameter(key, "key", "Session Callback")) return;

        if (sessionParameters.callbackParameters == null) {
            logger.warn("Session Callback parameters are not set");
            return;
        }

        String oldValue = sessionParameters.callbackParameters.remove(key);

        if (oldValue == null) {
            logger.warn("Key %s does not exist", key);
            return;
        }

        logger.debug("Key %s will be removed", key);

        writeSessionCallbackParametersI();
    }

    public void removeSessionPartnerParameterI(String key) {
        if (!Util.isValidParameter(key, "key", "Session Partner")) return;

        if (sessionParameters.partnerParameters == null) {
            logger.warn("Session Partner parameters are not set");
            return;
        }

        String oldValue = sessionParameters.partnerParameters.remove(key);

        if (oldValue == null) {
            logger.warn("Key %s does not exist", key);
            return;
        }

        logger.debug("Key %s will be removed", key);

        writeSessionPartnerParametersI();
    }

    public void resetSessionCallbackParametersI() {
        if (sessionParameters.callbackParameters == null) {
            logger.warn("Session Callback parameters are not set");
        }

        sessionParameters.callbackParameters = null;

        writeSessionCallbackParametersI();
    }

    public void resetSessionPartnerParametersI() {
        if (sessionParameters.partnerParameters == null) {
            logger.warn("Session Partner parameters are not set");
        }

        sessionParameters.partnerParameters = null;

        writeSessionPartnerParametersI();
    }

    private void setPushTokenI(String token) {
        if (token == null) {
            return;
        }

        if (token.equals(activityState.pushToken)) {
            return;
        }

        // save new push token
        activityState.pushToken = token;
        writeActivityStateI();

        long now = System.currentTimeMillis();
        PackageBuilder infoPackageBuilder = new PackageBuilder(adjustConfig, deviceInfo, activityState, now);

        ActivityPackage infoPackage = infoPackageBuilder.buildInfoPackage(Constants.PUSH);
        packageHandler.addPackage(infoPackage);
        packageHandler.sendFirstPackage();
    }

    private void readActivityStateI(Context context) {
        try {
            activityState = Util.readObject(context, ACTIVITY_STATE_FILENAME, ACTIVITY_STATE_NAME, ActivityState.class);
        } catch (Exception e) {
            logger.error("Failed to read %s file (%s)", ACTIVITY_STATE_NAME, e.getMessage());
            activityState = null;
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

    private void readSessionCallbackParametersI(Context context) {
        try {
            sessionParameters.callbackParameters = Util.readObject(context,
                    SESSION_CALLBACK_PARAMETERS_FILENAME,
                    SESSION_CALLBACK_PARAMETERS_NAME,
                    (Class<Map<String,String>>)(Class)Map.class);
        } catch (Exception e) {
            logger.error("Failed to read %s file (%s)", SESSION_CALLBACK_PARAMETERS_NAME, e.getMessage());
            sessionParameters.callbackParameters = null;
        }
    }

    private void readSessionPartnerParametersI(Context context) {
        try {
            sessionParameters.partnerParameters = Util.readObject(context,
                    SESSION_PARTNER_PARAMETERS_FILENAME,
                    SESSION_PARTNER_PARAMETERS_NAME,
                    (Class<Map<String,String>>)(Class)Map.class);
        } catch (Exception e) {
            logger.error("Failed to read %s file (%s)", SESSION_PARTNER_PARAMETERS_NAME, e.getMessage());
            sessionParameters.partnerParameters = null;
        }
    }

    private void writeActivityStateI() {
        writeActivityStateS(null);
    }

    private void writeActivityStateS(Runnable changeActivityState) {
        synchronized (ActivityState.class) {
            if (activityState == null) {
                return;
            }
            if (changeActivityState != null) {
                changeActivityState.run();
            }
            Util.writeObject(activityState, adjustConfig.context, ACTIVITY_STATE_FILENAME, ACTIVITY_STATE_NAME);
        }
    }

    private void teardownActivityStateS(boolean toDelete) {
        synchronized (ActivityState.class) {
            if (activityState == null) {
                return;
            }
            if (toDelete && adjustConfig != null && adjustConfig.context != null) {
                deleteActivityState(adjustConfig.context);
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

    private void teardownAttributionS(boolean toDelete) {
        synchronized (AdjustAttribution.class) {
            if (attribution == null) {
                return;
            }
            if (toDelete && adjustConfig != null && adjustConfig.context != null) {
                deleteAttribution(adjustConfig.context);
            }
            attribution = null;
        }
    }

    private void writeSessionCallbackParametersI() {
        synchronized (SessionParameters.class) {
            if (sessionParameters == null) {
                return;
            }
            Util.writeObject(sessionParameters.callbackParameters, adjustConfig.context, SESSION_CALLBACK_PARAMETERS_FILENAME, SESSION_CALLBACK_PARAMETERS_NAME);
        }
    }

    private void writeSessionPartnerParametersI() {
        synchronized (SessionParameters.class) {
            if (sessionParameters == null) {
                return;
            }
            Util.writeObject(sessionParameters.partnerParameters, adjustConfig.context, SESSION_PARTNER_PARAMETERS_FILENAME, SESSION_PARTNER_PARAMETERS_NAME);
        }
    }

    private void teardownAllSessionParametersS(boolean toDelete) {
        synchronized (SessionParameters.class) {
            if (sessionParameters == null) {
                return;
            }
            if (toDelete && adjustConfig != null && adjustConfig.context != null) {
                deleteSessionCallbackParameters(adjustConfig.context);
                deleteSessionPartnerParameters(adjustConfig.context);
            }
            sessionParameters = null;
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

    private boolean checkOrderIdI(String orderId) {
        if (orderId == null || orderId.isEmpty()) {
            return true;  // no order ID given
        }

        if (activityState.findOrderId(orderId)) {
            logger.info("Skipping duplicated order ID '%s'", orderId);
            return false; // order ID found -> used already
        }

        activityState.addOrderId(orderId);
        logger.verbose("Added order ID '%s'", orderId);
        // activity state will get written by caller
        return true;
    }

    private boolean checkActivityStateI(ActivityState activityState) {
        if (activityState == null) {
            logger.error("Missing activity state");
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
                !isEnabledI()               ||      // is disabled
                internalState.isDelayStart();       // is in delayed start
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
        if (adjustConfig.sendInBackground) {
            return true;
        }

        // doesn't have the option -> depends on being on the background/foreground
        return internalState.isForeground();
    }
}
