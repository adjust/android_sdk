//
//  ActivityHandler.java
//  Adjust
//
//  Created by Christian Wellenbrock on 2013-06-25.
//  Copyright (c) 2013 adjust GmbH. All rights reserved.
//  See the file MIT-LICENSE for copying permission.
//

package com.adjust.sdk;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.adjust.sdk.Constants.LOGTAG;
import static com.adjust.sdk.Constants.SESSION_STATE_FILENAME;

public class ActivityHandler extends HandlerThread {

    private static long TIMER_INTERVAL;
    private static long SESSION_INTERVAL;
    private static long SUBSESSION_INTERVAL;
    private static final String TIME_TRAVEL = "Time travel!";
    private static final String ADJUST_PREFIX = "adjust_";

    private        SessionHandler           sessionHandler;
    private        IPackageHandler          packageHandler;
    private        OnFinishedListener       onFinishedListener;
    private        ActivityState            activityState;
    private        Logger                   logger;
    private static ScheduledExecutorService timer;
    private        String                   defaultTracker;
    private        boolean                  dropOfflineActivities;
    private        boolean                  enabled;

    private DeviceInfo deviceInfo;
    private AdjustConfig adjustConfig;

    public ActivityHandler(AdjustConfig adjustConfig) {
        super(LOGTAG, MIN_PRIORITY);
        setDaemon(true);
        start();

        sessionHandler = new SessionHandler(getLooper(), this);
        enabled = true;

        Message message = Message.obtain();
        message.arg1 = SessionHandler.INIT;
        message.obj = adjustConfig;
        sessionHandler.sendMessage(message);
    }

    public void setOnFinishedListener(OnFinishedListener listener) {
        onFinishedListener = listener;
    }

    public void trackSubsessionStart() {
        Message message = Message.obtain();
        message.arg1 = SessionHandler.START;
        sessionHandler.sendMessage(message);
    }

    public void trackSubsessionEnd() {
        Message message = Message.obtain();
        message.arg1 = SessionHandler.END;
        sessionHandler.sendMessage(message);
    }

    public void trackEvent(Event event) {
        Message message = Message.obtain();
        message.arg1 = SessionHandler.EVENT;
        message.obj = event;
        sessionHandler.sendMessage(message);
    }

    public void finishedTrackingActivity(final ResponseData responseData, final String deepLink) {
        if (onFinishedListener == null && deepLink == null) {
            return;
        }

        Handler handler = new Handler(adjustConfig.context.getMainLooper());
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    runDelegateMain(responseData);
                    launchDeepLinkMain(deepLink);
                } catch (NullPointerException e) {
                }
            }
        };
        handler.post(runnable);
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
        if (checkActivityState(activityState))
            activityState.enabled = enabled;
        if (enabled) {
            this.trackSubsessionStart();
        } else {
            this.trackSubsessionEnd();
        }
    }

    public Boolean isEnabled() {
        if (checkActivityState(activityState)) {
            return activityState.enabled;
        } else {
            return this.enabled;
        }
    }

    public void readOpenUrl(Uri url) {
        Message message = Message.obtain();
        message.arg1 = SessionHandler.DEEP_LINK;
        message.obj = url;
        sessionHandler.sendMessage(message);
    }

    private static final class SessionHandler extends Handler {
        private static final int INIT = 72630;
        private static final int START       = 72640;
        private static final int END         = 72650;
        private static final int EVENT       = 72660;
        private static final int DEEP_LINK   = 72680;


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
                    AdjustConfig adjustConfig = (AdjustConfig) message.obj;
                    sessionHandler.initInternal(adjustConfig);
                    break;
                case START:
                    sessionHandler.startInternal();
                    break;
                case END:
                    sessionHandler.endInternal();
                    break;
                case EVENT:
                    Event event = (Event) message.obj;
                    sessionHandler.trackEventInternal(event);
                    break;
                case DEEP_LINK:
                    Uri url = (Uri) message.obj;
                    sessionHandler.readOpenUrlInternal(url);
                    break;
            }
        }
    }

    private void initInternal(AdjustConfig adjustConfig) {
        TIMER_INTERVAL = AdjustFactory.getTimerInterval();
        SESSION_INTERVAL = AdjustFactory.getSessionInterval();
        SUBSESSION_INTERVAL = AdjustFactory.getSubsessionInterval();

        this.adjustConfig = adjustConfig;
        deviceInfo = new DeviceInfo();
        logger = AdjustFactory.getLogger();

        if (adjustConfig.environment == AdjustConfig.PRODUCTION_ENVIRONMENT) {
            logger.setLogLevel(Logger.LogLevel.ASSERT);
        } else {
            logger.setLogLevel(adjustConfig.logLevel);
        }

        if (adjustConfig.sdkPrefix == null) {
            deviceInfo.clientSdk = Constants.CLIENT_SDK;
        } else {
            deviceInfo.clientSdk = String.format("%s@%s", adjustConfig.sdkPrefix, Constants.CLIENT_SDK);
        }

        deviceInfo.pluginKeys = Util.getPluginKeys(adjustConfig.context);

        // TODO check if AdjustDefaultTracker and AdjustDropOfflineActivities are still needed

        if (adjustConfig.eventBufferingEnabled) {
            logger.info("Event buffering is enabled");
        }

        deviceInfo.androidId= Util.getAndroidId(adjustConfig.context);
        deviceInfo.fbAttributionId = Util.getAttributionId(adjustConfig.context);
        deviceInfo.userAgent = Util.getUserAgent(adjustConfig.context);

        String playAdId = Util.getPlayAdId(adjustConfig.context);
        if (playAdId == null) {
            logger.info("Unable to get Google Play Services Advertising ID at start time");
        }

        if  (!Util.isGooglePlayServicesAvailable(adjustConfig.context)) {
            String macAddress = Util.getMacAddress(adjustConfig.context);
            deviceInfo.macSha1 = Util.getMacSha1(macAddress);
            deviceInfo.macShortMd5 = Util.getMacShortMd5(macAddress);
        }

        packageHandler = AdjustFactory.getPackageHandler(this, adjustConfig.context, dropOfflineActivities);

        readActivityState();
    }

    private void startInternal() {
        if (activityState != null
            && !activityState.enabled) {
            return;
        }

        packageHandler.resumeSending();
        startTimer();

        long now = System.currentTimeMillis();

        // very first session
        if (null == activityState) {
            activityState = new ActivityState();
            activityState.sessionCount = 1; // this is the first session
            activityState.createdAt = now;  // starting now

            transferSessionPackage();
            activityState.resetSessionAttributes(now);
            activityState.enabled = this.enabled;
            writeActivityState();
            logger.info("First session");
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
            activityState.createdAt = now;
            activityState.lastInterval = lastInterval;

            transferSessionPackage();
            activityState.resetSessionAttributes(now);
            writeActivityState();
            logger.debug("Session %d", activityState.sessionCount);
            return;
        }

        // new subsession
        if (lastInterval > SUBSESSION_INTERVAL) {
            activityState.subsessionCount++;
            logger.info("Started subsession %d of session %d",
                    activityState.subsessionCount,
                    activityState.sessionCount);
        }
        activityState.sessionLength += lastInterval;
        activityState.lastActivity = now;
        writeActivityState();
    }

    private void endInternal() {
        packageHandler.pauseSending();
        stopTimer();
        updateActivityState(System.currentTimeMillis());
        writeActivityState();
    }

    private void trackEventInternal(Event event) {
        if (!checkActivityState(activityState)) {
            return;
        }

        if (!activityState.enabled) {
            return;
        }

        long now = System.currentTimeMillis();
        activityState.createdAt = now;
        activityState.eventCount++;
        updateActivityState(now);

        PackageBuilder eventBuilder = new PackageBuilder(adjustConfig, deviceInfo);

        activityState.injectEventAttributes(eventBuilder);
        ActivityPackage eventPackage = eventBuilder.buildEventPackage();
        packageHandler.addPackage(eventPackage);

        if (adjustConfig.eventBufferingEnabled) {
            logger.info("Buffered event %s", eventPackage.getSuffix());
        } else {
            packageHandler.sendFirstPackage();
        }

        writeActivityState();
        logger.debug("Event %d", activityState.eventCount);
    }

    private void readOpenUrlInternal(Uri url) {
        if (url == null) {
            return;
        }

        String queryString = url.getQuery();
        if (queryString == null) {
            return;
        }

        Map<String, String> adjustDeepLinks = new HashMap<String, String>();

        String[] queryPairs = queryString.split("&");
        for (String pair : queryPairs) {
            String[] pairComponents = pair.split("=");
            if (pairComponents.length != 2) continue;

            String key = pairComponents[0];
            if (!key.startsWith(ADJUST_PREFIX)) continue;

            String value = pairComponents[1];
            if (value.length() == 0) continue;

            String keyWOutPrefix = key.substring(ADJUST_PREFIX.length());
            if (keyWOutPrefix.length() == 0) continue;

            adjustDeepLinks.put(keyWOutPrefix, value);
        }

        if (adjustDeepLinks.size() == 0) {
            return;
        }

        PackageBuilder builder = new PackageBuilder(adjustConfig, deviceInfo);
        builder.setDeepLinkParameters(adjustDeepLinks);
        ActivityPackage reattributionPackage = builder.buildReattributionPackage();
        packageHandler.addPackage(reattributionPackage);
        packageHandler.sendFirstPackage();

        logger.debug("Reattribution %s", adjustDeepLinks.toString());
    }

    private void runDelegateMain(ResponseData responseData) {
        if (onFinishedListener == null) return;
        if (responseData == null) return;
        onFinishedListener.onFinishedTracking(responseData);
    }

    private void launchDeepLinkMain(String deepLink) {
        if (deepLink == null) return;

        Uri location = Uri.parse(deepLink);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, location);
        mapIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // Verify it resolves
        PackageManager packageManager = adjustConfig.context.getPackageManager();
        List<ResolveInfo> activities = packageManager.queryIntentActivities(mapIntent, 0);
        boolean isIntentSafe = activities.size() > 0;

        // Start an activity if it's safe
        if (!isIntentSafe) {
            logger.error("Unable to open deep link (%s)", deepLink);
            return;
        }

        logger.info("Open deep link (%s)", deepLink);
        adjustConfig.context.startActivity(mapIntent);
    }

    private void updateActivityState(long now) {
        if (!checkActivityState(activityState)) {
            return;
        }

        long lastInterval = now - activityState.lastActivity;
        if (lastInterval < 0) {
            logger.error(TIME_TRAVEL);
            activityState.lastActivity = now;
            return;
        }

        // ignore late updates
        if (lastInterval > SESSION_INTERVAL) {
            return;
        }

        activityState.sessionLength += lastInterval;
        activityState.timeSpent += lastInterval;
        activityState.lastActivity = now;
    }

    private void readActivityState() {
        try {
            FileInputStream inputStream = adjustConfig.context.openFileInput(SESSION_STATE_FILENAME);
            BufferedInputStream bufferedStream = new BufferedInputStream(inputStream);
            ObjectInputStream objectStream = new ObjectInputStream(bufferedStream);

            try {
                activityState = (ActivityState) objectStream.readObject();
                logger.debug("Read activity state: %s uuid:%s", activityState, activityState.uuid);
                return;
            } catch (ClassNotFoundException e) {
                logger.error("Failed to find activity state class");
            } catch (OptionalDataException e) {
                /* no-op */
            } catch (IOException e) {
                logger.error("Failed to read activity states object");
            } catch (ClassCastException e) {
                logger.error("Failed to cast activity state object");
            } finally {
                objectStream.close();
            }

        } catch (FileNotFoundException e) {
            logger.verbose("Activity state file not found");
        } catch (Exception e) {
            logger.error("Failed to open activity state file for reading (%s)", e);
        }

        // start with a fresh activity state in case of any exception
        activityState = null;
    }

    private void writeActivityState() {
        try {
            FileOutputStream outputStream = adjustConfig.context.openFileOutput(SESSION_STATE_FILENAME, Context.MODE_PRIVATE);
            BufferedOutputStream bufferedStream = new BufferedOutputStream(outputStream);
            ObjectOutputStream objectStream = new ObjectOutputStream(bufferedStream);

            try {
                objectStream.writeObject(activityState);
                logger.debug("Wrote activity state: %s", activityState);
            } catch (NotSerializableException e) {
                logger.error("Failed to serialize activity state");
            } finally {
                objectStream.close();
            }

        } catch (Exception e) {
            logger.error("Failed to open activity state for writing (%s)", e);
        }
    }

    public static Boolean deleteActivityState(Context context) {
        return context.deleteFile(SESSION_STATE_FILENAME);
    }

    private void transferSessionPackage() {
        PackageBuilder builder = new PackageBuilder(adjustConfig, deviceInfo);
        injectReferrer(builder);
        activityState.injectSessionAttributes(builder);
        ActivityPackage sessionPackage = builder.buildSessionPackage();
        packageHandler.addPackage(sessionPackage);
        packageHandler.sendFirstPackage();
    }

    private void injectReferrer(PackageBuilder builder) {
        try {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(adjustConfig.context);
            builder.setReferrer(preferences.getString(ReferrerReceiver.REFERRER_KEY, null));
        }
        catch (Exception e) {
            logger.error("Failed to inject referrer (%s)", e);
        }
    }

    private void startTimer() {
        if (timer != null) {
            stopTimer();
        }
        timer = Executors.newSingleThreadScheduledExecutor();
        timer.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                timerFired();
            }
        }, 1000, TIMER_INTERVAL, TimeUnit.MILLISECONDS);
    }

    private void stopTimer() {
        try {
            timer.shutdown();
        } catch (NullPointerException e) {
            logger.error("No timer found");
        }
    }

    private void timerFired() {
        if (null != activityState
            && !activityState.enabled) {
            return;
        }

        packageHandler.sendFirstPackage();

        updateActivityState(System.currentTimeMillis());
        writeActivityState();
    }

    private void setDefaultTracker(String tracker) {
        defaultTracker = tracker;
        if (defaultTracker != null) {
            logger.info("Default tracker: '%s'", defaultTracker);
        }
    }

    private void setDropOfflineActivities(boolean drop) {
        dropOfflineActivities = drop;
        if (dropOfflineActivities) {
            logger.info("Offline activities will get dropped");
        }
    }

    private boolean checkActivityState(ActivityState activityState) {
        if (null == activityState) {
            logger.error("Missing activity state.");
            return false;
        }
        return true;
    }
}
