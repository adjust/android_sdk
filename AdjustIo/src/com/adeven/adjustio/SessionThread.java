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

public class SessionThread extends HandlerThread {
    private static final String SESSION_FILENAME = "sessionstate1"; // TODO: change filename

    private static final long UPDATE_INTERVAL  = 1000 * 10; // 10 second, TODO: one minute
    private static final long SESSION_INTERVAL = 1000 * 15; // 30 seconds, TODO: 30 minutes
    private static final long SUBSESSION_INTERVAL = 1000 * 1; // one second

    private static final int MESSAGE_ARG_INIT = 72630;
    private static final int MESSAGE_ARG_START = 72640;
    private static final int MESSAGE_ARG_END = 72650;
    private static final int MESSAGE_ARG_EVENT = 72660;
    private static final int MESSAGE_ARG_REVENUE = 72670;

    private Handler sessionHandler;
    private SessionState sessionState;
    private QueueThread queueThread;
    private static ScheduledExecutorService executor;

    private Context context;

    private String appToken;
    private String macSha1;
    private String macShort;
    private String userAgent;  // changes, should be updated periodically
    private String androidId;  // everything else here could be persisted
    private String attributionId;

    protected SessionThread(String appToken, Context context) {
        super(Logger.LOGTAG, MIN_PRIORITY);
        setDaemon(true);
        start();
        sessionHandler = new SessionHandler(getLooper(), this);

        this.appToken = appToken;
        this.context = context;

        Message message = Message.obtain();
        message.arg1 = MESSAGE_ARG_INIT;
        sessionHandler.sendMessage(message);
    }

    protected void trackSubsessionStart() {
        Message message = Message.obtain();
        message.arg1 = MESSAGE_ARG_START;
        sessionHandler.sendMessage(message);
    }

    protected void trackSubsessionEnd() {
        Message message = Message.obtain();
        message.arg1 = MESSAGE_ARG_END;
        sessionHandler.sendMessage(message);
    }

    protected void trackEvent(String eventToken, Map<String, String> parameters) {
        PackageBuilder builder = new PackageBuilder();
        builder.eventToken = eventToken;
        builder.callbackParameters = parameters;

        Message message = Message.obtain();
        message.arg1 = MESSAGE_ARG_EVENT;
        message.obj = builder;
        sessionHandler.sendMessage(message);
    }

    protected void trackRevenue(float amountInCents, String eventToken, Map<String, String> parameters) {
        PackageBuilder builder = new PackageBuilder();
        builder.amountInCents = amountInCents;
        builder.eventToken = eventToken;
        builder.callbackParameters = parameters;

        Message message = Message.obtain();
        message.arg1 = MESSAGE_ARG_REVENUE;
        message.obj = builder;
        sessionHandler.sendMessage(message);
    }

    private static final class SessionHandler extends Handler {
        private final WeakReference<SessionThread> sessionThreadReference;

        protected SessionHandler(Looper looper, SessionThread sessionThread) {
            super(looper);
            this.sessionThreadReference = new WeakReference<SessionThread>(sessionThread);
        }

        public void handleMessage(Message message) {
            super.handleMessage(message);

            SessionThread sessionThread = sessionThreadReference.get();
            if (sessionThread == null) return;

            switch (message.arg1) {
            case MESSAGE_ARG_INIT:
                sessionThread.initInternal();
                break;
            case MESSAGE_ARG_START:
                sessionThread.startInternal();
                break;
            case MESSAGE_ARG_END:
                sessionThread.endInternal();
                break;
            case MESSAGE_ARG_EVENT:
                PackageBuilder eventBuilder = (PackageBuilder) message.obj;
                sessionThread.eventInternal(eventBuilder);
                break;
            case MESSAGE_ARG_REVENUE:
                PackageBuilder revenueBuilder = (PackageBuilder) message.obj;
                sessionThread.revenueInternal(revenueBuilder);
                break;
            }
        }
    }

    // TODO: rename internal methods?

    // called from outside

    private void initInternal() {
        if (!checkState()) return;

        String macAddress = Util.getMacAddress(context);
        macSha1 = Util.sha1(macAddress);
        macShort = macAddress.replaceAll(":", "");  // TODO: macMd5!!!
        userAgent = Util.getUserAgent(context);
        androidId = Util.getAndroidId(context);
        attributionId = Util.getAttributionId(context);

        queueThread = new QueueThread(context);
        readSessionStateInternal();
    }

    private void startInternal() {
        if (!checkState()) return;
        queueThread.resumeTracking();
        startExecutor();

        long now = new Date().getTime();

        // very first session
        if (sessionState == null) {
            Logger.info("first session");
            sessionState = new SessionState();
            sessionState.sessionCount = 1; // this is the first session
            sessionState.createdAt = now;  // starting now (that's all we know)

            enqueueSessionInternal();
            writeSessionStateInternal();
            return;
        }

        long lastInterval = now - sessionState.lastActivity;
        if (lastInterval < 0) {
            Logger.error("time travel");
            sessionState.lastActivity = now;
            return;
        }

        // new session
        if (lastInterval > SESSION_INTERVAL) {
            sessionState.lastInterval = lastInterval;
            enqueueSessionInternal();
            sessionState.startNextSession(now);
            writeSessionStateInternal();
            return;
        }

        // new subsession start
        if (lastInterval > SUBSESSION_INTERVAL) {
            sessionState.subsessionCount++;
        }
        sessionState.sessionLength += lastInterval;
        sessionState.lastActivity = now;

        writeSessionStateInternal();
    }

    private void endInternal() {
        if (!checkState()) return;
        queueThread.pauseTracking();
        stopExecutor();
        updateInternal();
    }

    private void eventInternal(PackageBuilder eventBuilder) {
        if (!checkState()) return;
        if (!checkEventTokenNotNull(eventBuilder.eventToken)) return;
        if (!checkEventTokenLength(eventBuilder.eventToken)) return;

        sessionState.eventCount++;
        updateInternal();
        injectGeneralAttributes(eventBuilder);
        sessionState.injectEventAttributes(eventBuilder);

        TrackingPackage eventPackage = eventBuilder.buildEventPackage();
        queueThread.addPackage(eventPackage);
    }

    private void revenueInternal(PackageBuilder revenueBuilder) {
        if (!checkState()) return;
        if (!checkEventTokenLength(revenueBuilder.eventToken)) return;

        sessionState.eventCount++;
        updateInternal();
        injectGeneralAttributes(revenueBuilder);
        sessionState.injectEventAttributes(revenueBuilder);

        TrackingPackage revenuePackage = revenueBuilder.buildRevenuePackage();
         queueThread.addPackage(revenuePackage);
    }

    // called from inside

    private void updateInternal() {
        if (sessionState == null) return;

        long now = new Date().getTime();
        long lastInterval = now - sessionState.lastActivity;
        if (lastInterval < 0) {
            Logger.error("time travel");
            sessionState.lastActivity = now;
            return;
        }

        if (lastInterval > SESSION_INTERVAL) return;

        sessionState.sessionLength += lastInterval;
        sessionState.timeSpent += lastInterval;
        sessionState.lastActivity = now;

        writeSessionStateInternal();
    }

    private void enqueueSessionInternal() {
        PackageBuilder builder = new PackageBuilder();
        injectGeneralAttributes(builder);
        sessionState.injectSessionAttributes(builder);
        TrackingPackage sessionPackage = builder.buildSessionPackage();
        queueThread.addPackage(sessionPackage);
    }

    private void injectGeneralAttributes(PackageBuilder builder) {
        builder.userAgent = userAgent;
        builder.appToken = appToken;
        builder.macShort = macShort;
        builder.macSha1 = macSha1;
        builder.androidId = androidId;
        builder.attributionId = attributionId;
    }

    private void readSessionStateInternal() {
        // if any exception gets raised we start with a fresh sessionState
        sessionState = null;

        try {
            FileInputStream inputStream = context.openFileInput(SESSION_FILENAME);
            BufferedInputStream bufferedStream = new BufferedInputStream(inputStream);
            ObjectInputStream objectStream = new ObjectInputStream(bufferedStream);

            try {
                sessionState = (SessionState) objectStream.readObject();
                Logger.debug("Read session state: " + sessionState);
            } catch (ClassNotFoundException e) {
                Logger.error("Failed to find session state class");
            } catch (OptionalDataException e) {} catch (IOException e) {
                Logger.error("Failed to read session states object");
            } catch (ClassCastException e) {
                Logger.error("Failed to cast session state object");
            } finally {
                objectStream.close();
            }

        } catch (FileNotFoundException e) {
            Logger.verbose("Session state file not found");
        } catch (IOException e) {
            Logger.error("Failed to read session state file");
        }
    }

    private void writeSessionStateInternal() {
        try { // TODO: remove sleeps!
            Thread.sleep(100);
        } catch (Exception e) {
        }

        try {
            FileOutputStream outputStream = context.openFileOutput(SESSION_FILENAME, Context.MODE_PRIVATE);
            BufferedOutputStream bufferedStream = new BufferedOutputStream(outputStream);
            ObjectOutputStream objectStream = new ObjectOutputStream(bufferedStream);

            try {
                objectStream.writeObject(sessionState);
                Logger.debug("Wrote session state: " + sessionState);
            } catch (NotSerializableException e) {
                Logger.error("Failed to serialize session state");
            } finally {
                objectStream.close();
            }

        } catch (IOException e) {
            Logger.error("Failed to write session state (" + e + ")");
        }
    }

    private void startExecutor() {
        stopExecutor();
        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleWithFixedDelay(new Runnable() {
            public void run() {
                updateInternal();
                queueThread.trackFirstPackage();
            }
        }, 1000, UPDATE_INTERVAL, TimeUnit.MILLISECONDS);
    }

    private void stopExecutor() {
        try {
            executor.shutdown();
        } catch (NullPointerException e) {
            Logger.error("No executor found");
        }
    }

    private boolean checkState() {
        if (!checkContext(context)) return false;
        if (!checkAppToken(appToken)) return false;
        if (!checkPermissions(context)) return false;
        return true;
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

    private static boolean checkPermission(Context context, String permission) {
        int result = context.checkCallingOrSelfPermission(permission);
        boolean granted = (result == PackageManager.PERMISSION_GRANTED);
        return granted;
    }

    private static boolean checkAppToken(String appToken) {
        if (appToken == null) {
            Logger.error("Missing App Token.");
            return false;
        } else if (appToken.length() != 12) {
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
            Logger.error("Malformed Event Token");
            return false;
        }
        return true;
    }

    private static boolean checkContext(Context context) {
        if (context == null) {
            Logger.error("Missing context.");
            return false;
        }
        return true;
    }
}
