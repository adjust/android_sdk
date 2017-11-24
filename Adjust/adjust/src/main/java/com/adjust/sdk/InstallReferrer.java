package com.adjust.sdk;

import android.content.Context;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static com.adjust.sdk.Constants.ONE_SECOND;

/**
 * Created by nonelse on 21.11.17.
 */

public class InstallReferrer implements InvocationHandler {
    public static final String packageBaseName = "com.android.installreferrer.";
    /** Play Store service is not connected now - potentially transient state */
    private static final int SERVICE_DISCONNECTED = -1;
    /** Success. */
    private static final int OK = 0;
    /** Could not initiate connection to the Install Referrer service. */
    private static final int SERVICE_UNAVAILABLE = 1;
    /** Install Referrer API not supported by the installed Play Store app. */
    private static final int FEATURE_NOT_SUPPORTED = 2;
    /** General errors caused by incorrect usage */
    private static final int DEVELOPER_ERROR = 3;

    private ILogger logger;
    private Object referrerClient;
    private int retries;
    private Context context;
    private Object flagLock;
    private boolean hasInstallReferrerBeenRead;
    private TimerOnce retryTimer;
    private WeakReference<IActivityHandler> activityHandlerWeakRef;

    private int RETRY_WAIT_TIME = ONE_SECOND * 3;

    public InstallReferrer(Context context, IActivityHandler activityHandler) {
        this.logger = AdjustFactory.getLogger();
        this.context = context;
        this.flagLock = new Object();
        this.hasInstallReferrerBeenRead = false;
        this.retries = 0;
        this.retryTimer = new TimerOnce(new Runnable() {
            @Override
            public void run() {
                startConnection();
            }
        }, "InstallReferrer");
        activityHandlerWeakRef = new WeakReference<IActivityHandler>(activityHandler);

        startConnection();
    }

    public void startConnection () {
        closeReferrerClient();

        synchronized (flagLock) {
            if (hasInstallReferrerBeenRead) {
                logger.debug("Install referrer has already been read");
                return;
            }
        }

        this.referrerClient = createInstallReferrerClient(context);

        Class listenerClass = getInstallReferrerStateListenerClass();

        Object listenerProxy = createProxyInstallReferrerStateListener(listenerClass);

        try {
            Reflection.invokeInstanceMethod(this.referrerClient, "startConnection",
                    new Class[] {listenerClass}, listenerProxy);
        } catch (Exception e) {
            logger.error("startConnection error (%s)", e.getMessage());
        }
    }

    public Object createInstallReferrerClient(Context context) {
        try {
            Object builder = Reflection.invokeStaticMethod(packageBaseName + "api.InstallReferrerClient",
                    "newBuilder",
                    new Class[]{Context.class}, context);
            return Reflection.invokeInstanceMethod(builder, "build", null);
        } catch (Exception e) {
            logger.warn("Couldn't create instance of referrer client (%s)", e.getMessage());
        }
        return null;
    }

    public Object createProxyInstallReferrerStateListener(Class installReferrerStateListenerClass) {
        if (installReferrerStateListenerClass == null) {
            return null;
        }
        return Proxy.newProxyInstance(
                installReferrerStateListenerClass.getClassLoader(),
                new Class[]{ installReferrerStateListenerClass },
                this
        );
    }

    public Class getInstallReferrerStateListenerClass() {
        try{
            return Class.forName(packageBaseName + "api.InstallReferrerStateListener");
        } catch (Exception e) {
            logger.error("getInstallReferrerStateListenerClass error (%s)", e.getMessage());
        }
        return null;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            String methodName = method.getName();
            // Prints the method being invoked
            logger.debug("InstallReferrer invoke method name: %s", methodName);
            for (Object arg : args) {
                logger.debug("InstallReferrer invoke arg: %s", arg);
            }

            // if the method name equals some method's name then call your method
            if (methodName.equals("onInstallReferrerSetupFinished")) {
                onInstallReferrerSetupFinishedObject(args[0]);
            } else if (methodName.equals("onInstallReferrerServiceDisconnected")) {
                onInstallReferrerServiceDisconnected();
            }
        } catch (Exception e) {
            logger.error("InstallReferrer invoke error (%s)", e.getMessage());
        }
       return null;
    }

    public void onInstallReferrerSetupFinishedObject(Object responseCodeObj) {
        logger.debug("InstallReferrer onInstallReferrerSetupFinished");
        try {
            logger.debug("onInstallReferrerSetupFinishedObject arg class name: %s", responseCodeObj.getClass().getName());
            logger.debug("onInstallReferrerSetupFinishedObject arg toString: %s", responseCodeObj.toString());
            onInstallReferrerSetupFinishedInt((Integer)responseCodeObj);
        } catch (Exception e) {
            logger.error("onInstallReferrerSetupFinished  error (%s)", e.getMessage());
        }
    }

    public void onInstallReferrerSetupFinishedInt(final int responseCode) {
        switch (responseCode) {
            case OK:
                // Connection established
                try {
                    // extract referrer
                    Object referrerDetails = getInstallReferrer();
                    String stringInstallReferrer = getStringInstallReferrer(referrerDetails);
                    long clickTime = getReferrerClickTimestampSeconds(referrerDetails);
                    long installBeginTime = getInstallBeginTimestampSeconds(referrerDetails);
                    logger.debug("installReferrer: %s, clickTime: %d, installBeginTime: %d",
                            stringInstallReferrer, clickTime, installBeginTime);
                    // save referrer
                    SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(context);
                    sharedPreferencesManager.saveInstallReferrer(stringInstallReferrer, clickTime, installBeginTime);
                    // send referrer
                    IActivityHandler activityHandler = activityHandlerWeakRef.get();
                    if (activityHandler != null) {
                        activityHandler.sendReferrer();
                    }
                } catch (Exception e) {
                    logger.debug("Couldn't get install referrer from client (%s). Retrying", e.getMessage());
                    retry();
                }
                break;
            case FEATURE_NOT_SUPPORTED:
                // API not available on the current Play Store app
                logger.debug("Install referrer not available on the current Play Store app");
                break;
            case SERVICE_UNAVAILABLE:
                // Connection could not be established
                logger.debug("Could not initiate connection to the Install Referrer service. Retrying");
                retry();
                break;
            case DEVELOPER_ERROR:
                logger.debug("Install referrer general errors caused by incorrect usage. Retrying");
                retry();
                break;
            case SERVICE_DISCONNECTED:
                // Play Store service is not connected now - potentially transient state
                logger.debug("Play Store service is not connected now. Retrying");
                retry();
                break;
            default:
                logger.debug("Unexpected response code of install referrer response: %d", responseCode);
                break;
        }
        closeReferrerClient();
    }

    private Object getInstallReferrer() {
        if (this.referrerClient == null) {
            return null;
        }
        try {
            return Reflection.invokeInstanceMethod(
                    this.referrerClient, "getInstallReferrer", null);
        } catch (Exception e) {
            logger.error("getInstallReferrer error (%s)", e.getMessage());
        }
        return null;
    }

    private String getStringInstallReferrer(Object referrerDetails) {
        if (referrerDetails == null) {
            return null;
        }
        try {
            String stringInstallReferrer = (String)Reflection.invokeInstanceMethod(
                    referrerDetails, "getInstallReferrer", null);
            return stringInstallReferrer;
        } catch (Exception e) {
            logger.error("getStringInstallReferrer error (%s)", e.getMessage());
        }
        return null;
    }

    private long getReferrerClickTimestampSeconds(Object referrerDetails) {
        if (referrerDetails == null) {
            return -1;
        }
        try {
            Long clickTime = (Long)Reflection.invokeInstanceMethod(
                    referrerDetails, "getReferrerClickTimestampSeconds", null);
            return clickTime;
        } catch (Exception e) {
            logger.error("getReferrerClickTimestampSeconds error (%s)", e.getMessage());
        }
        return -1;
    }

    private long getInstallBeginTimestampSeconds(Object referrerDetails) {
        if (referrerDetails == null) {
            return -1;
        }
        try {
            Long installBeginTime = (Long)Reflection.invokeInstanceMethod(
                    referrerDetails, "getInstallBeginTimestampSeconds", null);
            return installBeginTime;
        } catch (Exception e) {
            logger.error("getInstallBeginTimestampSeconds error (%s)", e.getMessage());
        }
        return -1;
    }

    private void saveInstallReferrer(String installReferrer, long clickTime, long installBeginTime, Context context) {
        SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(context);
        sharedPreferencesManager.saveInstallReferrer(installReferrer, clickTime, installBeginTime);
    }

    public void onInstallReferrerServiceDisconnected() {
        logger.debug("InstallReferrer onInstallReferrerServiceDisconnected");
    }

    private void retry() {
        synchronized (flagLock) {
            if (hasInstallReferrerBeenRead) {
                logger.debug("Install referrer has already been read");
                return;
            }
        }
        // increase retry counter
        retries++;
        if (retries > Constants.MAX_INSTALL_REFERRER_RETRIES) {
            logger.debug("Limit number of retry for install referrer surpassed");
            return;
        }

        long firingIn = retryTimer.getFireIn();
        if (firingIn > 0) {
            logger.debug("Already waiting to retry to read install referrer in %d milliseconds", firingIn);
            return;
        }
        retryTimer.startIn(RETRY_WAIT_TIME);
    }

    private void closeReferrerClient() {
        if (referrerClient == null) {
            return;
        }

        try {
            Reflection.invokeInstanceMethod(referrerClient, "endConnection", null);
        } catch (Exception e) {
            logger.error("closeReferrerClient error (%s)", e.getMessage());
        }
        referrerClient = null;
    }
}
