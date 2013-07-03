package com.adeven.adjustio;

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
import java.util.Date;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

public class ActivityHandler extends HandlerThread {
    private static final String SESSION_STATE_FILENAME = "activitystate1"; // TODO: change filename

    private static final long TIMER_INTERVAL      = 1000 * 10; // 10 second, TODO: one minute
    private static final long SESSION_INTERVAL    = 1000 * 15; // 15 seconds, TODO: 30 minutes
    private static final long SUBSESSION_INTERVAL = 1000 *  1; // one second

    private static final int MESSAGE_ARG_INIT    = 72630;
    private static final int MESSAGE_ARG_START   = 72640;
    private static final int MESSAGE_ARG_END     = 72650;
    private static final int MESSAGE_ARG_EVENT   = 72660;
    private static final int MESSAGE_ARG_REVENUE = 72670;

    private InternalHandler internalHandler;
    private PackageHandler packageHandler;
    private ActivityState activityState;
    private static ScheduledExecutorService timer; // TODO: rename to timer

    private Context context;

    private String appToken;
    private String macSha1;
    private String macShortMd5; // TODO: md5!!!
    private String androidId; // everything else here could be persisted
    private String fbAttributionId;
    private String userAgent; // changes, should be updated periodically

    protected ActivityHandler(String appToken, Context context) {
        super(Logger.LOGTAG, MIN_PRIORITY);
        setDaemon(true);
        start();
        internalHandler = new InternalHandler(getLooper(), this);

        this.context = context;

        Message message = Message.obtain();
        message.arg1 = MESSAGE_ARG_INIT;
        message.obj = appToken;
        internalHandler.sendMessage(message);
    }

    protected void trackSubsessionStart() {
        Message message = Message.obtain();
        message.arg1 = MESSAGE_ARG_START;
        internalHandler.sendMessage(message);
    }

    protected void trackSubsessionEnd() {
        Message message = Message.obtain();
        message.arg1 = MESSAGE_ARG_END;
        internalHandler.sendMessage(message);
    }

    protected void trackEvent(String eventToken, Map<String, String> parameters) {
        PackageBuilder builder = new PackageBuilder();
        builder.eventToken = eventToken;
        builder.callbackParameters = parameters;

        Message message = Message.obtain();
        message.arg1 = MESSAGE_ARG_EVENT;
        message.obj = builder;
        internalHandler.sendMessage(message);
    }

    protected void trackRevenue(float amountInCents, String eventToken, Map<String, String> parameters) {
        PackageBuilder builder = new PackageBuilder();
        builder.amountInCents = amountInCents;
        builder.eventToken = eventToken;
        builder.callbackParameters = parameters;

        Message message = Message.obtain();
        message.arg1 = MESSAGE_ARG_REVENUE;
        message.obj = builder;
        internalHandler.sendMessage(message);
    }

    private static final class InternalHandler extends Handler {
        private final WeakReference<ActivityHandler> sessionHandlerReference;

        protected InternalHandler(Looper looper, ActivityHandler sessionHandler) {
            super(looper);
            this.sessionHandlerReference = new WeakReference<ActivityHandler>(sessionHandler);
        }

        public void handleMessage(Message message) {
            super.handleMessage(message);

            ActivityHandler sessionHandler = sessionHandlerReference.get();
            if (sessionHandler == null) return;

            switch (message.arg1) {
            case MESSAGE_ARG_INIT:
                String appToken = (String) message.obj;
                sessionHandler.initInternal(appToken);
                break;
            case MESSAGE_ARG_START:
                sessionHandler.startInternal();
                break;
            case MESSAGE_ARG_END:
                sessionHandler.endInternal();
                break;
            case MESSAGE_ARG_EVENT:
                PackageBuilder eventBuilder = (PackageBuilder) message.obj;
                sessionHandler.eventInternal(eventBuilder);
                break;
            case MESSAGE_ARG_REVENUE:
                PackageBuilder revenueBuilder = (PackageBuilder) message.obj;
                sessionHandler.revenueInternal(revenueBuilder);
                break;
            }
        }
    }

    // TODO: rename internal methods?
    // TODO: remove internal for all methods without external part
    // TODO: move internal methods up to the public interface?
    // like foo(), fooInternal(), bar(), barInternal(), etc.

    // called from outside

    private void initInternal(String token) {
        if (!checkAppTokenNotNull(token)) return;
        if (!checkAppTokenLength(token)) return;
        if (!checkContext(context)) return;
        if (!checkPermissions(context)) return;

        String macAddress = Util.getMacAddress(context);

        appToken = token;
        macSha1 = Util.sha1(macAddress);
        macShortMd5 = macAddress.replaceAll(":", ""); // TODO: macMd5!!!
        userAgent = Util.getUserAgent(context);
        androidId = Util.getAndroidId(context);
        fbAttributionId = Util.getAttributionId(context);

        packageHandler = new PackageHandler(context);
        readActivityState();
    }

    private void startInternal() {
        if (!checkAppTokenNotNull(appToken)) return;

        packageHandler.resumeSending();
        startTimer();

        long now = new Date().getTime();

        // very first session
        if (activityState == null) {
            Logger.info("First session");
            activityState = new ActivityState();
            activityState.sessionCount = 1; // this is the first session
            activityState.createdAt = now;  // starting now

            transferSessionPackage();
            writeActivityState();
            return;
        }

        long lastInterval = now - activityState.lastActivity;
        if (lastInterval < 0) {
            Logger.error("Time travel!");
            activityState.lastActivity = now;
            writeActivityState();
            return;
        }

        // new session
        if (lastInterval > SESSION_INTERVAL) {
            activityState.lastInterval = lastInterval;
            transferSessionPackage();
            activityState.startNextSession(now);
            writeActivityState();
            return;
        }

        // new subsession
        if (lastInterval > SUBSESSION_INTERVAL) {
            activityState.subsessionCount++;
        }
        activityState.sessionLength += lastInterval;
        activityState.lastActivity = now;
        writeActivityState();
    }

    private void endInternal() {
        if (!checkAppTokenNotNull(appToken)) return;

        packageHandler.pauseSending();
        stopTimer();
        updateActivityState();
        writeActivityState();
    }

    // TODO: set session attributes to -1 for events after session end?
    private void eventInternal(PackageBuilder eventBuilder) {
        if (!checkAppTokenNotNull(appToken)) return;
        if (!checkActivityState(activityState)) return;
        if (!checkEventTokenNotNull(eventBuilder.eventToken)) return;
        if (!checkEventTokenLength(eventBuilder.eventToken)) return;

        activityState.eventCount++;
        updateActivityState();
        injectGeneralAttributes(eventBuilder);
        activityState.injectEventAttributes(eventBuilder);

        ActivityPackage eventPackage = eventBuilder.buildEventPackage();
        packageHandler.addPackage(eventPackage);

        writeActivityState();
        try { Thread.sleep(500); } catch(Exception e) {}
    }

    private void revenueInternal(PackageBuilder revenueBuilder) {
        if (!checkAppTokenNotNull(appToken)) return;
        if (!checkActivityState(activityState)) return;
        if (!checkAmount(revenueBuilder.amountInCents)) return;
        if (!checkEventTokenLength(revenueBuilder.eventToken)) return;

        activityState.eventCount++;
        updateActivityState();
        injectGeneralAttributes(revenueBuilder);
        activityState.injectEventAttributes(revenueBuilder);

        ActivityPackage revenuePackage = revenueBuilder.buildRevenuePackage();
        packageHandler.addPackage(revenuePackage);

        writeActivityState();
        try { Thread.sleep(500); } catch(Exception e) {}
    }

    // called from inside

    private void readActivityState() {
        // if any exception gets raised we start with a fresh activity state
        activityState = null;

        try {
            FileInputStream inputStream = context.openFileInput(SESSION_STATE_FILENAME);
            BufferedInputStream bufferedStream = new BufferedInputStream(inputStream);
            ObjectInputStream objectStream = new ObjectInputStream(bufferedStream);

            try {
                activityState = (ActivityState) objectStream.readObject();
                Logger.debug("Read activity state: " + activityState);
            }
            catch (ClassNotFoundException e) {
                Logger.error("Failed to find activity state class");
            }
            catch (OptionalDataException e) {}
            catch (IOException e) {
                Logger.error("Failed to read activity states object");
            }
            catch (ClassCastException e) {
                Logger.error("Failed to cast activity state object");
            }
            finally {
                objectStream.close();
            }

        }
        catch (FileNotFoundException e) {
            Logger.verbose("Activity state file not found");
        }
        catch (IOException e) {
            Logger.error("Failed to read activity state file");
        }
    }

    // TODO: move to activityState?
    private void writeActivityState() {
        try { // TODO: remove sleeps!
            Thread.sleep(100);
        }
        catch (Exception e) {}

        try {
            FileOutputStream outputStream = context.openFileOutput(SESSION_STATE_FILENAME, Context.MODE_PRIVATE);
            BufferedOutputStream bufferedStream = new BufferedOutputStream(outputStream);
            ObjectOutputStream objectStream = new ObjectOutputStream(bufferedStream);

            try {
                objectStream.writeObject(activityState);
                Logger.debug("Wrote activity state: " + activityState);
            }
            catch (NotSerializableException e) {
                Logger.error("Failed to serialize activity state");
            }
            finally {
                objectStream.close();
            }

        }
        catch (IOException e) {
            Logger.error("Failed to write activity state (" + e + ")");
        }
    }

    private void transferSessionPackage() {
        PackageBuilder builder = new PackageBuilder();
        injectGeneralAttributes(builder);
        activityState.injectSessionAttributes(builder);
        ActivityPackage sessionPackage = builder.buildSessionPackage();
        packageHandler.addPackage(sessionPackage);
    }

    // called from inside

    private void updateActivityState() {
        if (!checkActivityState(activityState)) return;

        long now = new Date().getTime();
        long lastInterval = now - activityState.lastActivity;
        if (lastInterval < 0) {
            Logger.error("Time travel");
            activityState.lastActivity = now;
            return;
        }

        // ignore late updates
        if (lastInterval > SESSION_INTERVAL) return;

        activityState.sessionLength += lastInterval;
        activityState.timeSpent += lastInterval;
        activityState.lastActivity = now;
    }

    private void injectGeneralAttributes(PackageBuilder builder) {
        builder.userAgent = userAgent;
        builder.appToken = appToken;
        builder.macShortMd5 = macShortMd5;
        builder.macSha1 = macSha1;
        builder.androidId = androidId;
        builder.fbAttributionId = fbAttributionId;
    }

    private void startTimer() {
        if (timer != null) {
            stopTimer();
        }
        timer = Executors.newSingleThreadScheduledExecutor();
        timer.scheduleWithFixedDelay(new Runnable() {
            public void run() {
                timerFired();
            }
        }, 1000, TIMER_INTERVAL, TimeUnit.MILLISECONDS);
    }

    private void stopTimer() {
        try {
            timer.shutdown();
        }
        catch (NullPointerException e) {
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
            Logger.error(
                    "This SDK requires the INTERNET permission. " +
                            "See the README for details."
                    );
            result = false;
        }
        if (!checkPermission(context, android.Manifest.permission.ACCESS_WIFI_STATE)) {
            Logger.warn(
                    "You can improve your tracking results by adding the " +
                            "ACCESS_WIFI_STATE permission. See the README for details."
                    );
        }

        return result;
    }

    private static boolean checkContext(Context context) {
        if (context == null) {
            Logger.error("Missing context.");
            return false;
        }
        return true;
    }

    private static boolean checkPermission(Context context, String permission) {
        int result = context.checkCallingOrSelfPermission(permission);
        boolean granted = (result == PackageManager.PERMISSION_GRANTED);
        return granted;
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
            Logger.error("Malformed App Token '" + appToken + "'");
            return false;
        }
        return true;
    }

    private static boolean checkEventTokenNotNull(String eventToken) {
        if (eventToken == null) {
            Logger.error("Missing Event Token");
            return false;
        }
        return true;
    }

    private static boolean checkEventTokenLength(String eventToken) {
        if (eventToken == null)
            return true;

        if (eventToken.length() != 6) {
            Logger.error("Malformed Event Token '" + eventToken + "'");
            return false;
        }
        return true;
    }

    private static boolean checkAmount(float amount) {
        if (amount <= 0.0f) {
            Logger.error("Invalid amount " + amount);
            return false;
        }
        return true;
    }
}




// TODO: remove trailing lines
