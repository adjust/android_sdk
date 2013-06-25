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

    // TODO: move to TrackingPackage?
    private static final String APP_TOKEN = "app_token";
    private static final String MAC_SHA1 = "mac_sha1";
    private static final String MAC_SHORT = "mac";
    private static final String ANDROID_ID = "android_id";
    private static final String ATTRIBUTION_ID = "fb_id";
    private static final String EVENT_TOKEN = "event_id";
    private static final String PARAMETERS = "params";
    private static final String AMOUNT = "amount";

    private static final String SESSION_FILENAME = "sessionstate";
    private static final int MESSAGE_ARG_UPDATE = 72610;
    private static final int MESSAGE_ARG_READ = 72620;
    private static final int MESSAGE_ARG_INIT = 72630;
    private static final int MESSAGE_ARG_START = 72640;
    private static final int MESSAGE_ARG_END = 72650;
    private static final int MESSAGE_ARG_EVENT= 72660;
    private static final int MESSAGE_ARG_REVENUE= 72670;

    public SessionThread(String appToken, Context context) {
        super(Logger.LOGTAG, MIN_PRIORITY);
        setDaemon(true);
        start();
        this.sessionHandler = new SessionHandler(getLooper(), this);

        this.appToken = appToken;
        this.context = context;

        init();
        readSessionState();
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
        TrackingPackage.Builder builder = new TrackingPackage.Builder();
        builder.eventToken = eventToken;
        builder.parameters = parameters;

        Message message = Message.obtain();
        message.arg1 = MESSAGE_ARG_EVENT;
        message.obj = builder;
        sessionHandler.sendMessage(message);
    }

    public void trackRevenue(float amountInCents, String eventToken, Map<String, String> parameters) {
        TrackingPackage.Builder builder = new TrackingPackage.Builder();
        builder.amountInCents = amountInCents;
        builder.eventToken = eventToken;
        builder.parameters = parameters;

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

    private void init() {
        Message message = Message.obtain();
        message.arg1 = MESSAGE_ARG_INIT;
        sessionHandler.sendMessage(message);
    }

    private void readSessionState() {
        Message message = Message.obtain();
        message.arg1 = MESSAGE_ARG_READ;
        sessionHandler.sendMessage(message);
    }

    private static final class SessionHandler extends Handler {
        private final WeakReference<SessionThread> sessionThreadReference;

        public SessionHandler(Looper looper, SessionThread sessionThread) {
            super(looper);
            this.sessionThreadReference = new WeakReference<SessionThread>(sessionThread);
        }

        @Override
        public void handleMessage(Message message) {
            super.handleMessage(message);

            SessionThread sessionThread = sessionThreadReference.get();
            if (sessionThread == null) {
                return;
            } else if (message.arg1 == MESSAGE_ARG_UPDATE) {
                sessionThread.updateInternal();
            } else if (message.arg1 == MESSAGE_ARG_READ) {
                sessionThread.readInternal();
            } else if (message.arg1 == MESSAGE_ARG_INIT) {
                sessionThread.initInternal();
            } else if (message.arg1 == MESSAGE_ARG_START) {
                sessionThread.startInternal();
            } else if (message.arg1 == MESSAGE_ARG_END) {
                sessionThread.endInternal();
            } else if (message.arg1 == MESSAGE_ARG_EVENT) {
                TrackingPackage.Builder builder = (TrackingPackage.Builder)message.obj;
                sessionThread.eventInternal(builder);
            } else if (message.arg1 == MESSAGE_ARG_REVENUE) {
                TrackingPackage.Builder builder = (TrackingPackage.Builder)message.obj;
                sessionThread.revenueInternal(builder);
            }
        }
    }

    private void updateInternal() {
        Logger.info("update");
    }

    private void initInternal() {
        if (!Util.checkPermissions(context)) return;
        if (!checkAppToken(appToken)) return;
        if (!checkContext(context)) return;

        String macAddress = Util.getMacAddress(context);

        macSha1 = Util.sha1(macAddress);
        macShort = macAddress.replaceAll(":", "");
        userAgent = Util.getUserAgent(context);
        androidId = Util.getAndroidId(context);
        attributionId = Util.getAttributionId(context);

        queueThread = new QueueThread(context);
    }

    private void startInternal() {
        startExecutor();

        TrackingPackage sessionStart = new TrackingPackage.Builder()
            .setPath("/startup")
            .setSuccessMessage("Tracked session start.")
            .setFailureMessage("Failed to track session start.")
            .setUserAgent(userAgent)
            .addTrackingParameter(APP_TOKEN, appToken)
            .addTrackingParameter(MAC_SHORT, macShort)
            .addTrackingParameter(MAC_SHA1, macSha1)
            .addTrackingParameter(ANDROID_ID, androidId)
            .addTrackingParameter(ATTRIBUTION_ID, attributionId)
            .build();

        queueThread.addPackage(sessionStart);
    }

    private void endInternal() {
        stopExecutor();
        // TODO: update last activity
    }

    private void eventInternal(TrackingPackage.Builder builder) {
        // TODO: clean up the builder stuff: reuse builder or even use its eventToken and parameters
        if (!checkEventTokenNotNull(builder.eventToken)) return;
        if (!checkEventTokenLength(builder.eventToken)) return;

        String paramString = Util.getBase64EncodedParameters(builder.parameters);
        String successMessage = "Tracked event: '" + builder.eventToken + "'";
        String failureMessage = "Failed to track event: '" + builder.eventToken + "'";

        TrackingPackage eventPackage = new TrackingPackage.Builder()
            .setPath("/event")
            .setSuccessMessage(successMessage)
            .setFailureMessage(failureMessage)
            .setUserAgent(userAgent)
            .addTrackingParameter(APP_TOKEN, appToken)
            .addTrackingParameter(MAC_SHORT, macShort)
            .addTrackingParameter(ANDROID_ID, androidId)
            .addTrackingParameter(EVENT_TOKEN, builder.eventToken)
            .addTrackingParameter(PARAMETERS, paramString)
            .build();

        queueThread.addPackage(eventPackage);
    }

    private void revenueInternal(TrackingPackage.Builder builder) {
    	if (!checkEventTokenLength(builder.eventToken)) return;

        Logger.info("revenueInternal");
        // TODO: clean up and extract general event stuff?


        int amountInMillis = Math.round(10 * builder.amountInCents);
        float amountInCents = amountInMillis/10.0f; // now rounded to one decimal point
        String amount = Integer.toString(amountInMillis);
        String paramString = Util.getBase64EncodedParameters(builder.parameters);
        String successMessage = "Tracked revenue: " + amountInCents + " Cent";
        String failureMessage = "Failed to track revenue: " + amountInCents + " Cent";

        if (builder.eventToken != null) {
            String eventString = " (event token: '" + builder.eventToken + "')";
            successMessage += eventString;
            failureMessage += eventString;
        }

        TrackingPackage revenuePackage = new TrackingPackage.Builder()
            .setPath("/revenue")
            .setSuccessMessage(successMessage)
            .setFailureMessage(failureMessage)
            .setUserAgent(userAgent)
            .addTrackingParameter(APP_TOKEN, appToken)
            .addTrackingParameter(MAC_SHORT, macShort)
            .addTrackingParameter(ANDROID_ID, androidId)
            .addTrackingParameter(AMOUNT, amount)
            .addTrackingParameter(EVENT_TOKEN, builder.eventToken)
            .addTrackingParameter(PARAMETERS, paramString)
            .build();
        //getRequestThread().track(revenue);
        queueThread.addPackage(revenuePackage);
    }

    private void readInternal() {
        try {
            FileInputStream inputStream = context.openFileInput(SESSION_FILENAME);
            BufferedInputStream bufferedStream = new BufferedInputStream(inputStream);
            ObjectInputStream objectStream = new ObjectInputStream(bufferedStream);
            try {
                sessionState = (SessionState)objectStream.readObject();
                Logger.error("state");
            } finally {
                objectStream.close();
            }
        } catch (FileNotFoundException e) {
            sessionState = new SessionState();
        } catch (ClassNotFoundException e) {
            Logger.error("class not found");
        } catch (IOException e) {
            Logger.error("failed to read object");
        }
    }

    private void writeInternal() {
        try { Thread.sleep(100); } catch (Exception e) {}

        try {
            FileOutputStream outputStream = context.openFileOutput(SESSION_FILENAME, Context.MODE_PRIVATE);
            BufferedOutputStream bufferedStream = new BufferedOutputStream(outputStream);
            ObjectOutputStream objectStream = new ObjectOutputStream(bufferedStream);
            try {
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
    	if (eventToken == null) return true;

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
