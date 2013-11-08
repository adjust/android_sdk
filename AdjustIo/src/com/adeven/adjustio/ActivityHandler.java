//
//  ActivityHandler.java
//  AdjustIo
//
//  Created by Christian Wellenbrock on 2013-06-25.
//  Copyright (c) 2013 adeven. All rights reserved.
//  See the file MIT-LICENSE for copying permission.
//

package com.adeven.adjustio;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.HandlerThread;
import android.preference.PreferenceManager;
import static com.adeven.adjustio.Constants.ONE_MINUTE;
import static com.adeven.adjustio.Constants.ONE_SECOND;
import static com.adeven.adjustio.Constants.SESSION_STATE_FILENAME;
import static com.adeven.adjustio.Constants.THIRTY_SECONDS;
import static com.adeven.adjustio.Constants.UNKNOWN;
import static com.adeven.adjustio.Logger.LOGTAG;
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
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ActivityHandler extends HandlerThread {

    private static final long   TIMER_INTERVAL      = ONE_MINUTE;
    private static final long   SESSION_INTERVAL    = THIRTY_SECONDS;
    private static final long   SUBSESSION_INTERVAL = ONE_SECOND;
    private static final String TIME_TRAVEL         = "Time travel!";

    private        PackageHandler           packageHandler;
    private        ActivityState            activityState;
    private static ScheduledExecutorService timer;
    private final  Context                  context;
    private        String                   environment;
    private        String                   defaultTracker;
    private        boolean                  bufferEvents;

    private String appToken;
    private String macSha1;
    private String macShortMd5;
    private String androidId;       // everything else here could be persisted
    private String fbAttributionId;
    private String userAgent;       // changes, should be updated periodically
    private String clientSdk;

    protected ActivityHandler(Activity activity) {
        super(LOGTAG, MIN_PRIORITY);
        setDaemon(true);
        start();

        this.context = activity.getApplicationContext();
        initInternal();
    }

    protected void trackSubsessionStart() {
        startInternal();
    }

    protected void trackSubsessionEnd() {
        endInternal();
    }

    protected void trackEvent(String eventToken, Map<String, String> parameters) {
        PackageBuilder builder = new PackageBuilder();
        builder.setEventToken(eventToken);
        builder.setCallbackParameters(parameters);

        trackEventInternal(builder);
    }

    protected void trackRevenue(double amountInCents, String eventToken, Map<String, String> parameters) {
        PackageBuilder builder = new PackageBuilder();
        builder.setAmountInCents(amountInCents);
        builder.setEventToken(eventToken);
        builder.setCallbackParameters(parameters);

        trackRevenueInternal(builder);
    }

    private void initInternal() {
        processApplicationBundle();

        if (!canInit()) {
            return;
        }

        String macAddress = Util.getMacAddress(context);
        String macShort = macAddress.replaceAll(":", "");

        macSha1 = Util.sha1(macAddress);
        macShortMd5 = Util.md5(macShort);
        androidId = Util.getAndroidId(context);
        fbAttributionId = Util.getAttributionId(context);
        userAgent = Util.getUserAgent(context);
        clientSdk = Util.CLIENT_SDK;

        packageHandler = new PackageHandler(context);
        readActivityState();
    }

    private boolean canInit() {
        return checkAppTokenNotNull(appToken) && checkAppTokenLength(appToken) && checkContext(context) &&
          checkPermissions(context);
    }

    private void startInternal() {
        if (!checkAppTokenNotNull(appToken)) {
            return;
        }

        packageHandler.resumeSending();
        startTimer();

        long now = System.currentTimeMillis();

        // very first session
        if (activityState == null) {
            activityState = new ActivityState();
            activityState.sessionCount = 1; // this is the first session
            activityState.createdAt = now;  // starting now

            transferSessionPackage();
            activityState.resetSessionAttributes(now);
            writeActivityState();
            Logger.info("First session");
            return;
        }

        long lastInterval = now - activityState.lastActivity;
        if (lastInterval < 0) {
            Logger.error(TIME_TRAVEL);
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
            Logger.debug(String.format(Locale.US,
                                       "Session %d", activityState.sessionCount));
            return;
        }

        // new subsession
        if (lastInterval > SUBSESSION_INTERVAL) {
            activityState.subsessionCount++;
            Logger.info(String.format(Locale.US,
                                      "Started subsession %d of session %d",
                                      activityState.subsessionCount,
                                      activityState.sessionCount));
        }
        activityState.sessionLength += lastInterval;
        activityState.lastActivity = now;
        writeActivityState();
    }

    private void endInternal() {
        if (!checkAppTokenNotNull(appToken)) {
            return;
        }

        packageHandler.pauseSending();
        stopTimer();
        updateActivityState();
        writeActivityState();
    }

    private void trackEventInternal(PackageBuilder eventBuilder) {
        if (!canTrackRevenueOrEvent(eventBuilder)) {
            return;
        }

        activityState.createdAt = System.currentTimeMillis();
        activityState.eventCount++;
        updateActivityState();

        injectGeneralAttributes(eventBuilder);
        activityState.injectEventAttributes(eventBuilder);
        ActivityPackage eventPackage = eventBuilder.buildEventPackage();
        packageHandler.addPackage(eventPackage);

        if (bufferEvents) {
            Logger.info(String.format("Buffered event %s", eventPackage.getSuffix()));
        } else {
            packageHandler.sendFirstPackage();
        }

        writeActivityState();
        Logger.debug(String.format(Locale.US, "Event %d", activityState.eventCount));
    }


    private void trackRevenueInternal(PackageBuilder revenueBuilder) {
        if (!canTrackRevenueOrEvent(revenueBuilder)) {
            return;
        }

        activityState.createdAt = System.currentTimeMillis();
        activityState.eventCount++;
        updateActivityState();

        injectGeneralAttributes(revenueBuilder);
        activityState.injectEventAttributes(revenueBuilder);
        ActivityPackage eventPackage = revenueBuilder.buildRevenuePackage();
        packageHandler.addPackage(eventPackage);

        if (bufferEvents) {
            Logger.info(String.format("Buffered revenue %s", eventPackage.getSuffix()));
        } else {
            packageHandler.sendFirstPackage();
        }

        writeActivityState();
        Logger.debug(String.format(Locale.US, "Event %d (revenue)", activityState.eventCount));
    }

    private boolean canTrackRevenueOrEvent(PackageBuilder revenueBuilder) {
        return checkAppTokenNotNull(appToken) && checkActivityState(activityState) &&
          checkAmount(revenueBuilder.getAmountInCents()) && checkEventToken(revenueBuilder.getEventToken());

    }

    private void updateActivityState() {
        if (!checkActivityState(activityState)) {
            return;
        }

        long now = System.currentTimeMillis();
        long lastInterval = now - activityState.lastActivity;
        if (lastInterval < 0) {
            Logger.error(TIME_TRAVEL);
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
            FileInputStream inputStream = context.openFileInput(SESSION_STATE_FILENAME);
            BufferedInputStream bufferedStream = new BufferedInputStream(inputStream);
            ObjectInputStream objectStream = new ObjectInputStream(bufferedStream);

            try {
                activityState = (ActivityState) objectStream.readObject();
                Logger.debug(String.format("Read activity state: %s", activityState));
                return;
            } catch (ClassNotFoundException e) {
                Logger.error("Failed to find activity state class");
            } catch (OptionalDataException e) {
                /* no-op */
            } catch (IOException e) {
                Logger.error("Failed to read activity states object");
            } catch (ClassCastException e) {
                Logger.error("Failed to cast activity state object");
            } finally {
                objectStream.close();
            }

        } catch (FileNotFoundException e) {
            Logger.verbose("Activity state file not found");
        } catch (IOException e) {
            Logger.error("Failed to read activity state file");
        }

        // start with a fresh activity state in case of any exception
        activityState = null;
    }

    private void writeActivityState() {
        try {
            FileOutputStream outputStream = context.openFileOutput(SESSION_STATE_FILENAME, Context.MODE_PRIVATE);
            BufferedOutputStream bufferedStream = new BufferedOutputStream(outputStream);
            ObjectOutputStream objectStream = new ObjectOutputStream(bufferedStream);

            try {
                objectStream.writeObject(activityState);
                Logger.verbose(String.format("Wrote activity state: %s", activityState));
            } catch (NotSerializableException e) {
                Logger.error("Failed to serialize activity state");
            } finally {
                objectStream.close();
            }

        } catch (IOException e) {
            Logger.error(String.format("Failed to write activity state (%s)", e));
        }
    }

    private void transferSessionPackage() {
        PackageBuilder builder = new PackageBuilder();
        injectGeneralAttributes(builder);
        injectReferrer(builder);
        activityState.injectSessionAttributes(builder);
        ActivityPackage sessionPackage = builder.buildSessionPackage();
        packageHandler.addPackage(sessionPackage);
        packageHandler.sendFirstPackage();
    }

    private void injectGeneralAttributes(PackageBuilder builder) {
        builder.setAppToken(appToken);
        builder.setMacShortMd5(macShortMd5);
        builder.setMacSha1(macSha1);
        builder.setAndroidId(androidId);
        builder.setFbAttributionId(fbAttributionId);
        builder.setUserAgent(userAgent);
        builder.setClientSdk(clientSdk);
        builder.setEnvironment(environment);
        builder.setDefaultTracker(defaultTracker);
    }

    private void injectReferrer(PackageBuilder builder) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        builder.setReferrer(preferences.getString(ReferrerReceiver.REFERRER_KEY, null));
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
            Logger.error("No timer found");
        }
    }

    private void timerFired() {
        packageHandler.sendFirstPackage();

        updateActivityState();
        writeActivityState();
    }

    private static boolean checkPermissions(Context context) {
        boolean result = true;

        if (!checkPermission(context, android.Manifest.permission.INTERNET)) {
            Logger.error("Missing permission: INTERNET");
            result = false;
        }
        if (!checkPermission(context, android.Manifest.permission.ACCESS_WIFI_STATE)) {
            Logger.warn("Missing permission: ACCESS_WIFI_STATE");
        }

        return result;
    }

    private void processApplicationBundle() {
        Bundle bundle = getApplicationBundle();
        if (bundle == null) {
            return;
        }

        // appToken
        appToken = bundle.getString("AdjustIoAppToken");

        // logLevel
        String logLevel = bundle.getString("AdjustIoLogLevel");
        if (null != logLevel) {
            try {
                Logger.setLogLevel(Logger.LogLevel.valueOf(logLevel));
            } catch (IllegalArgumentException iae) {
                /* no-op */
            }
        }

        // environment
        environment = bundle.getString("AdjustIoEnvironment");
        if (environment == null) {
            Logger.Assert("Missing environment");
            Logger.setLogLevel(Logger.LogLevel.ASSERT);
            environment = UNKNOWN;
        } else if ("sandbox".equalsIgnoreCase(environment)) {
            Logger.Assert(
              "SANDBOX: AdjustIo is running in Sandbox mode. Use this setting for testing. Don't forget to set the environment to `production` before publishing!");
        } else if ("production".equalsIgnoreCase(environment)) {
            Logger.Assert(
              "PRODUCTION: AdjustIo is running in Production mode. Use this setting only for the build that you want to publish. Set the environment to `sandbox` if you want to test your app!");
            Logger.setLogLevel(Logger.LogLevel.ASSERT);
        } else {
            Logger.Assert(String.format("Malformed environment '%s'", environment));
            Logger.setLogLevel(Logger.LogLevel.ASSERT);
            environment = Constants.MALFORMED;
        }

        // eventBuffering
        bufferEvents = bundle.getBoolean("AdjustIoEventBuffering");

        // defaultTracker
        defaultTracker = bundle.getString("AdjustIoDefaultTracker");
    }

    private Bundle getApplicationBundle() {
        final ApplicationInfo applicationInfo;
        try {
            String packageName = this.context.getPackageName();
            applicationInfo =
              this.context.getPackageManager().getApplicationInfo(packageName, PackageManager.GET_META_DATA);
        } catch (NameNotFoundException e) {
            Logger.error("ApplicationInfo not found");
            return new Bundle();
        }
        return applicationInfo.metaData;
    }

    private static boolean checkContext(Context context) {
        if (context == null) {
            Logger.error("Missing context");
            return false;
        }
        return true;
    }

    private static boolean checkPermission(Context context, String permission) {
        int result = context.checkCallingOrSelfPermission(permission);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private static boolean checkActivityState(ActivityState activityState) {
        if (activityState == null) {
            Logger.error("Missing activity state.");
            return false;
        }
        return true;
    }

    private static boolean checkAppTokenNotNull(String appToken) {
        if (appToken == null) {
            Logger.error("Missing App Token.");
            return false;
        }
        return true;
    }

    private static boolean checkAppTokenLength(String appToken) {
        if (appToken.length() != 12) {
            Logger.error(String.format("Malformed App Token '%s'", appToken));
            return false;
        }
        return true;
    }

    private static boolean checkEventToken(String eventToken) {
        if (eventToken == null) {
            Logger.error("Missing Event Token");

            return false;
        }

        if (eventToken.length() != 6) {
            Logger.error(String.format("Malformed Event Token '%s'", eventToken));
            return false;
        }
        return true;
    }

    private static boolean checkAmount(double amount) {
        if (amount <= 0.0) {
            Logger.error(String.format(Locale.US, "Invalid amount %f", amount));
            return false;
        }
        return true;
    }
}
