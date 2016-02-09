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
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import java.lang.ref.WeakReference;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.adjust.sdk.Constants.ACTIVITY_STATE_FILENAME;
import static com.adjust.sdk.Constants.ATTRIBUTION_FILENAME;
import static com.adjust.sdk.Constants.LOGTAG;

public class ActivityHandler extends HandlerThread implements IActivityHandler {

    private static long TIMER_INTERVAL;
    private static long TIMER_START;
    private static long SESSION_INTERVAL;
    private static long SUBSESSION_INTERVAL;
    private static final String TIME_TRAVEL = "Time travel!";
    private static final String ADJUST_PREFIX = "adjust_";
    private static final String ACTIVITY_STATE_NAME = "Activity state";
    private static final String ATTRIBUTION_NAME = "Attribution";

    private SessionHandler sessionHandler;
    private IPackageHandler packageHandler;
    private ActivityState activityState;
    private ILogger logger;
    private TimerCycle timer;
    private boolean enabled;
    private boolean offline;

    private DeviceInfo deviceInfo;
    private AdjustConfig adjustConfig; // always valid after construction
    private AdjustAttribution attribution;
    private IAttributionHandler attributionHandler;

    private ActivityHandler(AdjustConfig adjustConfig) {
        super(LOGTAG, MIN_PRIORITY);
        setDaemon(true);
        start();

        logger = AdjustFactory.getLogger();
        sessionHandler = new SessionHandler(getLooper(), this);
        enabled = true;
        init(adjustConfig);

        Message message = Message.obtain();
        message.arg1 = SessionHandler.INIT;
        sessionHandler.sendMessage(message);
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
    public void trackSubsessionStart() {
        Message message = Message.obtain();
        message.arg1 = SessionHandler.START;
        sessionHandler.sendMessage(message);
    }

    @Override
    public void trackSubsessionEnd() {
        Message message = Message.obtain();
        message.arg1 = SessionHandler.END;
        sessionHandler.sendMessage(message);
    }

    @Override
    public void trackEvent(AdjustEvent event) {
        if (activityState == null) {
            logger.warn("Event triggered before first application launch.\n" +
                    "This will trigger the SDK start and an install without user interaction" +
                    "Please check https://github.com/adjust/android_sdk#can-i-trigger-an-event-at-application-launch for more information.");
            trackSubsessionStart();
        }

        Message message = Message.obtain();
        message.arg1 = SessionHandler.EVENT;
        message.obj = event;
        sessionHandler.sendMessage(message);
    }

    @Override
    public void finishedTrackingActivity(ResponseData responseData) {
        // redirect session responses to attribution handler to check for attribution information
        if (responseData.activityKind == ActivityKind.SESSION) {
            attributionHandler.checkSessionResponse(responseData);
            return;
        }
        // check if it's an event response
        if (responseData.activityKind == ActivityKind.EVENT) {
            launchEventResponseTasks(responseData);
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (!hasChangedState(isEnabled(), enabled,
                "Adjust already enabled", "Adjust already disabled")) {
            return;
        }

        this.enabled = enabled;

        if (activityState == null) {
            trackSubsessionStart();
        } else {
            activityState.enabled = enabled;
            writeActivityState();
        }

        updateStatus(!enabled,
                "Pausing package handler and attribution handler to disable the SDK",
                "Package and attribution handler remain paused due to the SDK is offline",
                "Resuming package handler and attribution handler to enabled the SDK");
    }

    private void updateStatus(boolean pausingState, String pausingMessage,
                              String remainsPausedMessage, String unPausingMessage)
    {
        if (pausingState) {
            logger.info(pausingMessage);
            trackSubsessionEnd();
            return;
        }

        if (paused()) {
            logger.info(remainsPausedMessage);
        } else {
            logger.info(unPausingMessage);
            trackSubsessionStart();
        }
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
        if (!hasChangedState(this.offline, offline,
                "Adjust already in offline mode",
                "Adjust already in online mode")) {
            return;
        }

        this.offline = offline;

        if (activityState == null) {
            trackSubsessionStart();
        }

        updateStatus(offline,
                "Pausing package and attribution handler to put in offline mode",
                "Package and attribution handler remain paused because the SDK is disabled",
                "Resuming package handler and attribution handler to put in online mode");
    }

    @Override
    public boolean isEnabled() {
        if (activityState != null) {
            return activityState.enabled;
        } else {
            return enabled;
        }
    }

    @Override
    public void readOpenUrl(Uri url, long clickTime) {
        Message message = Message.obtain();
        message.arg1 = SessionHandler.DEEP_LINK;
        UrlClickTime urlClickTime = new UrlClickTime(url, clickTime);
        message.obj = urlClickTime;
        sessionHandler.sendMessage(message);
    }

    private void saveAttribution(AdjustAttribution attribution) {
        this.attribution = attribution;
        writeAttribution();
    }

    @Override
    public void setAskingAttribution(boolean askingAttribution) {
        activityState.askingAttribution = askingAttribution;
        writeActivityState();
    }

    @Override
    public ActivityPackage getAttributionPackage() {
        long now = System.currentTimeMillis();
        PackageBuilder attributionBuilder = new PackageBuilder(adjustConfig,
                deviceInfo,
                activityState,
                now);
        return attributionBuilder.buildAttributionPackage();
    }

    @Override
    public void sendReferrer(String referrer, long clickTime) {
        Message message = Message.obtain();
        message.arg1 = SessionHandler.SEND_REFERRER;
        ReferrerClickTime referrerClickTime = new ReferrerClickTime(referrer, clickTime);
        message.obj = referrerClickTime;
        sessionHandler.sendMessage(message);
    }

    @Override
    public void launchEventResponseTasks(ResponseData responseData) {
        Message message = Message.obtain();
        message.arg1 = SessionHandler.EVENT_TASKS;
        message.obj = responseData;
        sessionHandler.sendMessage(message);
    }

    @Override
    public void launchSessionResponseTasks(ResponseData responseData) {
        Message message = Message.obtain();
        message.arg1 = SessionHandler.SESSION_TASKS;
        message.obj = responseData;
        sessionHandler.sendMessage(message);
    }

    @Override
    public void launchAttributionResponseTasks(ResponseData responseData) {
        Message message = Message.obtain();
        message.arg1 = SessionHandler.ATTRIBUTION_TASKS;
        message.obj = responseData;
        sessionHandler.sendMessage(message);
    }


    private class UrlClickTime {
        Uri url;
        long clickTime;

        UrlClickTime(Uri url, long clickTime) {
            this.url = url;
            this.clickTime = clickTime;
        }
    }

    private class ReferrerClickTime {
        String referrer;
        long clickTime;

        ReferrerClickTime(String referrer, long clickTime) {
            this.referrer = referrer;
            this.clickTime = clickTime;
        }
    }

    private void updateHandlersStatus() {
        Message message = Message.obtain();
        message.arg1 = SessionHandler.UPDATE_HANDLERS_STATUS;
        sessionHandler.sendMessage(message);
    }

    private void timerFired() {
        Message message = Message.obtain();
        message.arg1 = SessionHandler.TIMER_FIRED;
        sessionHandler.sendMessage(message);
    }

    private static final class SessionHandler extends Handler {
        private static final int BASE_ADDRESS = 72630;
        private static final int INIT = BASE_ADDRESS + 1;
        private static final int START = BASE_ADDRESS + 2;
        private static final int END = BASE_ADDRESS + 3;
        private static final int EVENT = BASE_ADDRESS + 4;
        private static final int EVENT_TASKS = BASE_ADDRESS + 5;
        private static final int DEEP_LINK = BASE_ADDRESS + 6;
        private static final int SEND_REFERRER = BASE_ADDRESS + 7;
        private static final int UPDATE_HANDLERS_STATUS = BASE_ADDRESS + 8;
        private static final int TIMER_FIRED = BASE_ADDRESS + 9;
        private static final int SESSION_TASKS = BASE_ADDRESS + 10;
        private static final int ATTRIBUTION_TASKS = BASE_ADDRESS + 11;

        private final WeakReference<ActivityHandler> sessionHandlerReference;

        protected SessionHandler(Looper looper, ActivityHandler sessionHandler) {
            super(looper);
            this.sessionHandlerReference = new WeakReference<ActivityHandler>(sessionHandler);
        }

        @Override
        public void handleMessage(Message message) {
            super.handleMessage(message);

            ActivityHandler sessionHandler = sessionHandlerReference.get();
            if (sessionHandler == null) {
                return;
            }

            switch (message.arg1) {
                case INIT:
                    sessionHandler.initInternal();
                    break;
                case START:
                    sessionHandler.startInternal();
                    break;
                case END:
                    sessionHandler.endInternal();
                    break;
                case EVENT:
                    AdjustEvent event = (AdjustEvent) message.obj;
                    sessionHandler.trackEventInternal(event);
                    break;
                case EVENT_TASKS:
                    ResponseData eventResponseData = (ResponseData) message.obj;
                    sessionHandler.launchEventResponseTasksInternal(eventResponseData);
                    break;
                case DEEP_LINK:
                    UrlClickTime urlClickTime = (UrlClickTime) message.obj;
                    sessionHandler.readOpenUrlInternal(urlClickTime.url, urlClickTime.clickTime);
                    break;
                case SEND_REFERRER:
                    ReferrerClickTime referrerClickTime = (ReferrerClickTime) message.obj;
                    sessionHandler.sendReferrerInternal(referrerClickTime.referrer, referrerClickTime.clickTime);
                    break;
                case UPDATE_HANDLERS_STATUS:
                    sessionHandler.updateHandlersStatusInternal();
                    break;
                case TIMER_FIRED:
                    sessionHandler.timerFiredInternal();
                    break;
                case SESSION_TASKS:
                    ResponseData sessionResponseData = (ResponseData) message.obj;
                    sessionHandler.launchSessionResponseTasksInternal(sessionResponseData);
                    break;
                case ATTRIBUTION_TASKS:
                    ResponseData attributionResponseData = (ResponseData) message.obj;
                    sessionHandler.launchAttributionResponseTasksInternal(attributionResponseData);
                    break;
            }
        }
    }

    private void initInternal() {
        TIMER_INTERVAL = AdjustFactory.getTimerInterval();
        TIMER_START = AdjustFactory.getTimerStart();
        SESSION_INTERVAL = AdjustFactory.getSessionInterval();
        SUBSESSION_INTERVAL = AdjustFactory.getSubsessionInterval();

        deviceInfo = new DeviceInfo(adjustConfig.context, adjustConfig.sdkPrefix);

        if (AdjustConfig.ENVIRONMENT_PRODUCTION.equals(adjustConfig.environment)) {
            logger.setLogLevel(LogLevel.ASSERT);
        } else {
            logger.setLogLevel(adjustConfig.logLevel);
        }

        if (adjustConfig.eventBufferingEnabled) {
            logger.info("Event buffering is enabled");
        }

        String playAdId = Util.getPlayAdId(adjustConfig.context);
        if (playAdId == null) {
            logger.info("Unable to get Google Play Services Advertising ID at start time");
        }

        if (adjustConfig.defaultTracker != null) {
            logger.info("Default tracker: '%s'", adjustConfig.defaultTracker);
        }

        if (adjustConfig.referrer != null) {
            sendReferrer(adjustConfig.referrer, adjustConfig.referrerClickTime); // send to background queue to make sure that activityState is valid
        }

        readAttribution();
        readActivityState();

        packageHandler = AdjustFactory.getPackageHandler(this, adjustConfig.context, paused());

        ActivityPackage attributionPackage = getAttributionPackage();
        attributionHandler = AdjustFactory.getAttributionHandler(this,
                attributionPackage,
                paused(),
                adjustConfig.hasAttributionChangedListener());

        timer = new TimerCycle(new Runnable() {
            @Override
            public void run() {
                timerFired();
            }
        },TIMER_START, TIMER_INTERVAL);
    }

    private void startInternal() {
        // it shouldn't start if it was disabled after a first session
        if (activityState != null
                && !activityState.enabled) {
            return;
        }

        updateHandlersStatusInternal();

        processSession();

        checkAttributionState();

        startTimer();
    }

    private void processSession() {
        long now = System.currentTimeMillis();

        // very first session
        if (activityState == null) {
            activityState = new ActivityState();
            activityState.sessionCount = 1; // this is the first session

            transferSessionPackage(now);
            activityState.resetSessionAttributes(now);
            activityState.enabled = this.enabled;
            writeActivityState();
            return;
        }

        long lastInterval = now - activityState.lastActivity;

        if (lastInterval < 0) {
            logger.error(TIME_TRAVEL);
            activityState.lastActivity = now;
            writeActivityState();
            return;
        }

        // new session
        if (lastInterval > SESSION_INTERVAL) {
            activityState.sessionCount++;
            activityState.lastInterval = lastInterval;

            transferSessionPackage(now);
            activityState.resetSessionAttributes(now);
            writeActivityState();
            return;
        }

        // new subsession
        if (lastInterval > SUBSESSION_INTERVAL) {
            activityState.subsessionCount++;
            activityState.sessionLength += lastInterval;
            activityState.lastActivity = now;
            writeActivityState();
            logger.info("Started subsession %d of session %d",
                    activityState.subsessionCount,
                    activityState.sessionCount);
        }
    }

    private void checkAttributionState() {
        if (!checkActivityState(activityState)) { return; }

        // if it's a new session
        if (activityState.subsessionCount <= 1) {
            return;
        }

        // if there is already an attribution saved and there was no attribution being asked
        if (attribution != null && !activityState.askingAttribution) {
            return;
        }

        attributionHandler.getAttribution();
    }

    private void endInternal() {
        packageHandler.pauseSending();
        attributionHandler.pauseSending();
        stopTimer();
        if (updateActivityState(System.currentTimeMillis())) {
            writeActivityState();
        }
    }

    private void trackEventInternal(AdjustEvent event) {
        if (!checkActivityState(activityState)) return;
        if (!isEnabled()) return;
        if (!checkEvent(event)) return;

        long now = System.currentTimeMillis();

        activityState.eventCount++;
        updateActivityState(now);

        PackageBuilder eventBuilder = new PackageBuilder(adjustConfig, deviceInfo, activityState, now);
        ActivityPackage eventPackage = eventBuilder.buildEventPackage(event);
        packageHandler.addPackage(eventPackage);

        if (adjustConfig.eventBufferingEnabled) {
            logger.info("Buffered event %s", eventPackage.getSuffix());
        } else {
            packageHandler.sendFirstPackage();
        }

        writeActivityState();
    }

    private void launchEventResponseTasksInternal(final ResponseData responseData) {
        Handler handler = new Handler(adjustConfig.context.getMainLooper());

        // success callback
        if (responseData.success && adjustConfig.onTrackingSucceededListener != null) {
            logger.debug("Launching success event tracking listener");
            // add it to the handler queue
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    adjustConfig.onTrackingSucceededListener.onFinishedTrackingSucceeded(responseData.getSuccessResponseData());
                }
            };
            handler.post(runnable);

            return;
        }
        // failure callback
        if (!responseData.success && adjustConfig.onTrackingFailedListener != null) {
            logger.debug("Launching failed event tracking listener");
            // add it to the handler queue
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    adjustConfig.onTrackingFailedListener.onFinishedTrackingFailed(responseData.getFailureResponseData());
                }
            };
            handler.post(runnable);

            return;
        }
    }

    private void launchSessionResponseTasksInternal(ResponseData responseData) {
        // use the same handler to ensure that all tasks are executed sequentially
        Handler handler = new Handler(adjustConfig.context.getMainLooper());

        // try to update the attribution
        boolean attributionUpdated = updateAttribution(responseData.attribution);

        // if attribution changed, launch attribution changed delegate
        if (attributionUpdated) {
            launchAttributionListener(handler);
        }

        // if there is any, try to launch the deeplink
        launchDeeplink(responseData, handler);
    }

    private void launchAttributionResponseTasksInternal(ResponseData responseData) {
        Handler handler = new Handler(adjustConfig.context.getMainLooper());

        // try to update the attribution
        boolean attributionUpdated = updateAttribution(responseData.attribution);

        // if attribution not changed exit
        if (!attributionUpdated) {
            return;
        }

        // launch attribution changed delegate
        launchAttributionListener(handler);
    }

    private boolean updateAttribution(AdjustAttribution attribution) {
        if (attribution == null) {
            return false;
        }

        if (attribution.equals(this.attribution)) {
            return false;
        }

        saveAttribution(attribution);
        return true;
    }

    private void launchAttributionListener(Handler handler) {
        if (adjustConfig.onAttributionChangedListener == null) {
            return;
        }
        // add it to the handler queue
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                adjustConfig.onAttributionChangedListener.onAttributionChanged(attribution);
            }
        };
        handler.post(runnable);
    }

    private void launchDeeplink(ResponseData responseData, Handler handler) {
        if (responseData.jsonResponse == null) {
            return;
        }

        final String deeplink = responseData.jsonResponse.optString("deeplink", null);

        if (deeplink == null) {
            return;
        }

        Uri location = Uri.parse(deeplink);
        final Intent mapIntent;
        if (adjustConfig.deepLinkComponent == null) {
            mapIntent = new Intent(Intent.ACTION_VIEW, location);
        } else {
            mapIntent = new Intent(Intent.ACTION_VIEW, location, adjustConfig.context, adjustConfig.deepLinkComponent);
        }
        mapIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        mapIntent.setPackage(adjustConfig.context.getPackageName());

        // Verify it resolves
        PackageManager packageManager = adjustConfig.context.getPackageManager();
        List<ResolveInfo> activities = packageManager.queryIntentActivities(mapIntent, 0);
        boolean isIntentSafe = activities.size() > 0;

        // Start an activity if it's safe
        if (!isIntentSafe) {
            logger.error("Unable to open deep link (%s)", deeplink);
            return;
        }

        // add it to the handler queue
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                logger.info("Open deep link (%s)", deeplink);
                adjustConfig.context.startActivity(mapIntent);
            }
        };
        handler.post(runnable);
    }

    private void sendReferrerInternal(String referrer, long clickTime) {
        ActivityPackage clickPackage = buildQueryStringClickPackage(referrer,
                Constants.REFTAG,
                clickTime);
        if (clickPackage == null) {
            return;
        }

        packageHandler.addPackage(clickPackage);
    }

    private void readOpenUrlInternal(Uri url, long clickTime) {
        if (url == null) {
            return;
        }

        String queryString = url.getQuery();

        ActivityPackage clickPackage = buildQueryStringClickPackage(queryString, "deeplink", clickTime);
        if (clickPackage == null) {
            return;
        }

        packageHandler.addPackage(clickPackage);
    }

    private ActivityPackage buildQueryStringClickPackage(String queryString, String source, long clickTime) {
        if (queryString == null) {
            return null;
        }

        Map<String, String> queryStringParameters = new LinkedHashMap<String, String>();
        AdjustAttribution queryStringAttribution = new AdjustAttribution();
        boolean hasAdjustTags = false;

        logger.verbose("Reading query string (%s) from %s", queryString, source);

        String[] queryPairs = queryString.split("&");
        for (String pair : queryPairs) {
            if (readQueryString(pair, queryStringParameters, queryStringAttribution)) {
                hasAdjustTags = true;
            }
        }

        if (!hasAdjustTags) {
            return null;
        }

        String reftag = queryStringParameters.remove(Constants.REFTAG);

        long now = System.currentTimeMillis();
        PackageBuilder builder = new PackageBuilder(adjustConfig, deviceInfo, activityState, now);
        builder.extraParameters = queryStringParameters;
        builder.attribution = queryStringAttribution;
        builder.reftag = reftag;
        if (source == Constants.REFTAG) {
            builder.referrer = queryString;
        }

        ActivityPackage clickPackage = builder.buildClickPackage(source, clickTime);
        return clickPackage;
    }

    private boolean readQueryString(String queryString,
                                    Map<String, String> extraParameters,
                                    AdjustAttribution queryStringAttribution) {
        String[] pairComponents = queryString.split("=");
        if (pairComponents.length != 2) return false;

        String key = pairComponents[0];
        if (!key.startsWith(ADJUST_PREFIX)) return false;

        String value = pairComponents[1];
        if (value.length() == 0) return false;

        String keyWOutPrefix = key.substring(ADJUST_PREFIX.length());
        if (keyWOutPrefix.length() == 0) return false;

        if (!trySetAttribution(queryStringAttribution, keyWOutPrefix, value)) {
            extraParameters.put(keyWOutPrefix, value);
        }

        return true;
    }

    private boolean trySetAttribution(AdjustAttribution queryStringAttribution,
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

    private void updateHandlersStatusInternal() {
        updateAttributionHandlerStatus();
        updatePackageHandlerStatus();
    }

    private void updateAttributionHandlerStatus() {
        if (paused()) {
            attributionHandler.pauseSending();
        } else {
            attributionHandler.resumeSending();
        }
    }

    private void updatePackageHandlerStatus() {
        if (paused()) {
            packageHandler.pauseSending();
        } else {
            packageHandler.resumeSending();
        }
    }


    private boolean updateActivityState(long now) {
        if (!checkActivityState(activityState)) { return false; }

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

    private void transferSessionPackage(long now) {
        PackageBuilder builder = new PackageBuilder(adjustConfig, deviceInfo, activityState, now);
        ActivityPackage sessionPackage = builder.buildSessionPackage();
        packageHandler.addPackage(sessionPackage);
        packageHandler.sendFirstPackage();
    }

    private void startTimer() {
        // don't start the timer if it's disabled/offline
        if (paused()) {
            return;
        }

        timer.start();
    }

    private void stopTimer() {
        timer.suspend();
    }

    private void timerFiredInternal() {
        if (paused()) {
            // stop the timer cycle if it's disabled/offline
            stopTimer();
            return;
        }

        logger.debug("Session timer fired");
        packageHandler.sendFirstPackage();

        if (updateActivityState(System.currentTimeMillis())) {
            writeActivityState();
        }
    }

    private void readActivityState() {
        try {
            activityState = Util.readObject(adjustConfig.context, ACTIVITY_STATE_FILENAME, ACTIVITY_STATE_NAME, ActivityState.class);
        } catch (Exception e) {
            logger.error("Failed to read %s file (%s)", ACTIVITY_STATE_NAME, e.getMessage());
            activityState = null;
        }
    }

    private void readAttribution() {
        try {
            attribution = Util.readObject(adjustConfig.context, ATTRIBUTION_FILENAME, ATTRIBUTION_NAME, AdjustAttribution.class);
        } catch (Exception e) {
            logger.error("Failed to read %s file (%s)", ATTRIBUTION_NAME, e.getMessage());
            attribution = null;
        }
    }

    private synchronized void writeActivityState() {
        Util.writeObject(activityState, adjustConfig.context, ACTIVITY_STATE_FILENAME, ACTIVITY_STATE_NAME);
    }

    private void writeAttribution() {
        Util.writeObject(attribution, adjustConfig.context, ATTRIBUTION_FILENAME, ATTRIBUTION_NAME);
    }

    private boolean checkEvent(AdjustEvent event) {
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

    private boolean checkActivityState(ActivityState activityState) {
        if (activityState == null) {
            logger.error("Missing activity state.");
            return false;
        }
        return true;
    }

    private boolean paused() {
        return offline || !isEnabled();
    }
}
