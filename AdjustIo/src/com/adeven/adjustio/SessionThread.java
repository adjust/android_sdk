package com.adeven.adjustio;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

public class SessionThread extends HandlerThread {
    private String appToken;
    private Context context;

    private String macSha1;
    private String macShort;
    private String userAgent;
    private String androidId;
    private String attributionId;

    private Handler sessionHandler;
    private SessionState sessionState;
    private QueueThread queueThread;
    private static ScheduledExecutorService executor;

    private static final String SESSION_FILENAME = "sessionstate";
    private static final int MESSAGE_ARG_UPDATE = 72610;
    private static final int MESSAGE_ARG_INIT = 72630;
    private static final int MESSAGE_ARG_START = 72640;
    private static final int MESSAGE_ARG_END = 72650;
    private static final int MESSAGE_ARG_EVENT = 72660;
    private static final int MESSAGE_ARG_REVENUE = 72670;

    private static final long SESSION_INTERVAL = 1000 * 1; // 1 second, TODO: 30
                                                            // minutes

    public SessionThread(String appToken, Context context) {
        super(Logger.LOGTAG, MIN_PRIORITY);
        setDaemon(true);
        start();
        this.sessionHandler = new SessionHandler(getLooper(), this);

        this.appToken = appToken;
        this.context = context;

        Message message = Message.obtain();
        message.arg1 = MESSAGE_ARG_INIT;
        sessionHandler.sendMessage(message);
    }

    public void trackSubsessionStart() {
        Message message = Message.obtain();
        message.arg1 = MESSAGE_ARG_START;
        sessionHandler.sendMessage(message);
    }

    public void trackSubsessionEnd() {
        Message message = Message.obtain();
        message.arg1 = MESSAGE_ARG_END;
        sessionHandler.sendMessage(message);
    }

    public void trackEvent(String eventToken, Map<String, String> parameters) {
        PackageBuilder builder = new PackageBuilder();
        builder.eventToken = eventToken;
        builder.callbackParameters = parameters;

        Message message = Message.obtain();
        message.arg1 = MESSAGE_ARG_EVENT;
        message.obj = builder;
        sessionHandler.sendMessage(message);
    }

    public void trackRevenue(float amountInCents, String eventToken, Map<String, String> parameters) {
        PackageBuilder builder = new PackageBuilder();
        builder.amountInCents = amountInCents;
        builder.eventToken = eventToken;
        builder.callbackParameters = parameters;

        Message message = Message.obtain();
        message.arg1 = MESSAGE_ARG_REVENUE;
        message.obj = builder;
        sessionHandler.sendMessage(message);
    }

    public void updateLastActivity() {
        Message message = Message.obtain();
        message.arg1 = MESSAGE_ARG_UPDATE;
        sessionHandler.sendMessage(message);
    }

    private static final class SessionHandler extends Handler {
        private final WeakReference<SessionThread> sessionThreadReference;

        public SessionHandler(Looper looper, SessionThread sessionThread) {
            super(looper);
            this.sessionThreadReference = new WeakReference<SessionThread>(sessionThread);
        }

        public void handleMessage(Message message) {
            super.handleMessage(message);

            SessionThread sessionThread = sessionThreadReference.get();
            if (sessionThread == null) {
                return;
            }

            switch (message.arg1) {
            case MESSAGE_ARG_UPDATE:
                sessionThread.updateInternal();
                break;
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
                sessionThread.eventInternal((PackageBuilder) message.obj);
                break;
            case MESSAGE_ARG_REVENUE:
                sessionThread.revenueInternal((PackageBuilder) message.obj);
                break;
            }
        }
    }

    // TODO: rename internal methods

    private void initInternal() {
        if (!Util.checkPermissions(context))
            return;
        if (!checkAppToken(appToken))
            return;
        if (!checkContext(context))
            return;

        String macAddress = Util.getMacAddress(context);

        macSha1 = Util.sha1(macAddress);
        macShort = macAddress.replaceAll(":", "");
        userAgent = Util.getUserAgent(context);
        androidId = Util.getAndroidId(context);
        attributionId = Util.getAttributionId(context);

        queueThread = new QueueThread(context);
        readSessionStateInternal();
    }

    private void updateInternal() {
        Logger.info("update");
    }

    private void startInternal() {
        // startExecutor(); // TODO: enable

        long now = new Date().getTime();

        // very first session on this device
        if (sessionState == null) {
            Logger.info("first session");
            sessionState = new SessionState();
            sessionState.sessionCount = 1; // this is the first session
            sessionState.createdAt = now; // starting now (that's all we know)

            enqueueSessionInternal(-1);
            writeSessionStateInternal();
            return;
        }

        long lastInterval = now - sessionState.lastActivity;
        if (lastInterval < 0) {
            Logger.info("time travel");
            // TODO: extract this check?
            // should not happen, skip last interval and continue from here
            sessionState.lastActivity = now;
            return;
        }

        // new session
        if (lastInterval > SESSION_INTERVAL) {
            Logger.info("new session");
            enqueueSessionInternal(lastInterval);
            sessionState.startNextSession(now);
            writeSessionStateInternal();
            return;
        }

        // new subsession start
        sessionState.subsessionCount++;
        sessionState.sessionLength += lastInterval;
        sessionState.lastActivity = now;
        Logger.info("new subsession " + sessionState.subsessionCount);
    }

    private void endInternal() {
        stopExecutor();

        if (sessionState == null) {
            // ignore session ends before first session start
            return;
        }

        long now = new Date().getTime();
        long lastInterval = now - sessionState.lastActivity;
        if (lastInterval < 0) {
            // should not happen, skip last interval and continue from here
            sessionState.lastActivity = now;
            return;
        }

        if (lastInterval > SESSION_INTERVAL) {
            // ignore late ends (should not happen because of activity timer)
            return;
        }

        // subsession end
        sessionState.sessionLength += lastInterval;
        sessionState.timeSpent += lastInterval;
        sessionState.lastActivity = now;
    }

    private void enqueueSessionInternal(long lastInterval) {
        PackageBuilder builder = sessionState.getPackageBuilder();
        builder.lastInterval = lastInterval;

        // TODO: extract?
        builder.userAgent = userAgent;
        builder.appToken = appToken;
        builder.macShort = macShort;
        builder.macSha1 = macSha1;
        builder.androidId = androidId;
        builder.attributionId = attributionId;

        TrackingPackage sessionStart = builder.buildSessionPackage();
        queueThread.addPackage(sessionStart);
    }

    private void eventInternal(PackageBuilder builder) {
        // TODO: clean up the builder stuff: reuse builder or even use its
        // eventToken and parameters
        if (!checkEventTokenNotNull(builder.eventToken))
            return;
        if (!checkEventTokenLength(builder.eventToken))
            return;

        String successMessage = "Tracked event: '" + builder.eventToken + "'";
        String failureMessage = "Failed to track event: '" + builder.eventToken + "'";

        builder.path = "/event";
        builder.successMessage = successMessage;
        builder.failureMessage = failureMessage;
        builder.userAgent = userAgent;
        builder.appToken = appToken;
        builder.macShort = macShort;
        builder.androidId = androidId;
        builder.eventToken = builder.eventToken;
//        TrackingPackage eventPackage = builder.build();
//
//      queueThread.addPackage(eventPackage);
    }

    private void revenueInternal(PackageBuilder builder) {
        if (!checkEventTokenLength(builder.eventToken))
            return;

        Logger.info("revenueInternal");
        // TODO: clean up and extract general event stuff?

//      String successMessage = "Tracked revenue: " + amountInCents + " Cent";
//      String failureMessage = "Failed to track revenue: " + amountInCents + " Cent";
//
//      if (builder.eventToken != null) {
//          String eventString = " (event token: '" + builder.eventToken + "')";
//          successMessage += eventString;
//          failureMessage += eventString;
//      }
//
//        builder.path = "/revenue";
//        builder.successMessage = successMessage;
//        builder.failureMessage = failureMessage;
//        builder.userAgent = userAgent;
//        builder.appToken = appToken;
//        builder.macShort = macShort;
//        builder.androidId = androidId;
//        builder.eventToken = builder.eventToken;
//        TrackingPackage revenuePackage = builder.build();
//        // getRequestThread().track(revenue);
//        queueThread.addPackage(revenuePackage);
    }

    private void readSessionStateInternal() {
        try {
            FileInputStream inputStream = context.openFileInput(SESSION_FILENAME);
            BufferedInputStream bufferedStream = new BufferedInputStream(inputStream);
            ObjectInputStream objectStream = new ObjectInputStream(bufferedStream);
            try {
                sessionState = (SessionState) objectStream.readObject();
                Logger.info("readSessionState " + sessionState);
            } finally {
                objectStream.close();
            }
        } catch (FileNotFoundException e) {
            // first ever session start
        } catch (ClassNotFoundException e) {
            Logger.error("class not found");
        } catch (IOException e) {
            Logger.error("failed to read object");
        } catch (IllegalArgumentException e) {
            Logger.error("illegal argument");
        }
    }

    private void writeSessionStateInternal() {
        try {
            Thread.sleep(100);
        } catch (Exception e) {
        }

        try {
            FileOutputStream outputStream = context.openFileOutput(SESSION_FILENAME, Context.MODE_PRIVATE);
            BufferedOutputStream bufferedStream = new BufferedOutputStream(outputStream);
            ObjectOutputStream objectStream = new ObjectOutputStream(bufferedStream);
            try {
                Logger.info("writeSessionState " + sessionState);
                objectStream.writeObject(sessionState);
            } finally {
                objectStream.close();
            }
        } catch (IOException e) {
            Logger.error("failed to write package"); // TODO: improve log
        }
    }

    private void startExecutor() {
        stopExecutor();
        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleWithFixedDelay(new Runnable() {
            public void run() {
                Logger.info("timer");
                updateInternal();
                queueThread.tryTrackFirstPackage();
            }
        }, 5, 5, TimeUnit.SECONDS); // TODO: one minute, extract constants
    }

    private void stopExecutor() {
        try {
            executor.shutdown();
        } catch (NullPointerException e) {
            // TODO: log?
        }
    }

    private static boolean checkAppToken(String appToken) {
        if (appToken == null) {
            Logger.error("Missing App Token.");
            return false;
        } else if (appToken.length() != 12) {
            Logger.error("Malformed App Token " + appToken);
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
