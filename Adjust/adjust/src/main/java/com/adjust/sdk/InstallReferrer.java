package com.adjust.sdk;

import android.content.Context;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static com.adjust.sdk.Constants.ONE_SECOND;

/**
 * Class used for Android install referrer logic.
 *
 * @author Pedro Silva (nonelse)
 * @since 21st November 2017
 */

public class InstallReferrer implements InvocationHandler {
    /**
     * Android install referrer library package name.
     */
    public static final String PACKAGE_BASE_NAME = "com.android.installreferrer.";

    /**
     * Play Store service is not connected now - potentially transient state.
     */
    private static final int STATUS_SERVICE_DISCONNECTED = -1;
    /**
     * Play Store service connection success.
     */
    private static final int STATUS_OK = 0;

    /**
     * Could not initiate connection to the install referrer service.
     */
    private static final int STATUS_SERVICE_UNAVAILABLE = 1;

    /**
     * Install Referrer API not supported by the installed Play Store app.
     */
    private static final int STATUS_FEATURE_NOT_SUPPORTED = 2;

    /**
     * General errors caused by incorrect usage.
     */
    private static final int STATUS_DEVELOPER_ERROR = 3;

    /**
     * Retry time interval.
     */
    private int retryWaitTime = ONE_SECOND * 3;

    /**
     * Number of retries attempted to connect to service.
     */
    private int retries;

    /**
     * Boolean indicating whether service responded with install referrer information.
     */
    private boolean hasInstallReferrerBeenRead;

    /**
     * Adjust logger instance.
     */
    private ILogger logger;

    /**
     * InstallReferrer class instance.
     */
    private Object referrerClient;

    /**
     * Application context.
     */
    private Context context;

    /**
     * Lock.
     */
    private Object flagLock;

    /**
     * Timer which fires retry attempts.
     */
    private TimerOnce retryTimer;

    /**
     * Weak reference to ActivityHandler instance.
     */
    private WeakReference<IActivityHandler> activityHandlerWeakRef;

    /**
     * Default constructor.
     *
     * @param context         Application context
     * @param activityHandler ActivityHandler reference
     */
    public InstallReferrer(final Context context, final IActivityHandler activityHandler) {
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

    /**
     * Start connection with install referrer service.
     */
    public void startConnection() {
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

        startConnection(listenerClass, listenerProxy);
    }

    /**
     * Create InstallReferrerClient object instance.
     *
     * @param context App context
     * @return Instance of InstallReferrerClient. Defaults to null if failed to create one.
     */
    public Object createInstallReferrerClient(final Context context) {
        if (context == null) {
            return null;
        }
        try {
            Object builder = Reflection.invokeStaticMethod(PACKAGE_BASE_NAME + "api.InstallReferrerClient",
                    "newBuilder",
                    new Class[]{Context.class}, context);
            return Reflection.invokeInstanceMethod(builder, "build", null);
        } catch (Exception e) {
            logger.warn("Couldn't create instance of referrer client (%s)", e.getMessage());
        }
        return null;
    }

    /**
     * Get InstallReferrerStateListener class object.
     *
     * @return Class object for InstallReferrerStateListener class.
     */
    public Class getInstallReferrerStateListenerClass() {
        try {
            return Class.forName(PACKAGE_BASE_NAME + "api.InstallReferrerStateListener");
        } catch (Exception e) {
            logger.error("getInstallReferrerStateListenerClass error (%s)", e.getMessage());
        }
        return null;
    }

    /**
     * Get object instance for given class (InstallReferrerStateListener in this case).
     *
     * @param installReferrerStateListenerClass Class object
     * @return Instance of Class type object.
     */
    public Object createProxyInstallReferrerStateListener(final Class installReferrerStateListenerClass) {
        if (installReferrerStateListenerClass == null) {
            return null;
        }
        return Proxy.newProxyInstance(
                installReferrerStateListenerClass.getClassLoader(),
                new Class[]{installReferrerStateListenerClass},
                this
        );
    }

    /**
     * Initialise connection with install referrer service.
     *
     * @param listenerClass Callback listener class type
     * @param listenerProxy Callback listener object instance
     */
    public void startConnection(final Class listenerClass, final Object listenerProxy) {
        if (referrerClient == null) {
            return;
        }
        if (listenerClass == null || listenerProxy == null) {
            return;
        }
        try {
            Reflection.invokeInstanceMethod(this.referrerClient, "startConnection",
                    new Class[]{listenerClass}, listenerProxy);
        } catch (Exception e) {
            logger.error("startConnection error (%s)", e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        try {
            String methodName = method.getName();
            // Prints the method being invoked
            logger.debug("InstallReferrer invoke method name: %s", methodName);
            for (Object arg : args) {
                logger.debug("InstallReferrer invoke arg: %s", arg);
            }

            // if the method name equals some method's name then call your method
            if (methodName.equals("onInstallReferrerSetupFinished")) {
                onInstallReferrerSetupFinishedInt((Integer) args[0]);
            } else if (methodName.equals("onInstallReferrerServiceDisconnected")) {
                logger.debug("InstallReferrer onInstallReferrerServiceDisconnected");
            }
        } catch (Exception e) {
            logger.error("InstallReferrer invoke error (%s)", e.getMessage());
        }
        return null;
    }

    /**
     * Check and process response from install referrer service.
     *
     * @param responseCode Response code from install referrer service
     */
    public void onInstallReferrerSetupFinishedInt(final int responseCode) {
        switch (responseCode) {
            case STATUS_OK:
                // Connection established
                try {
                    // Extract referrer
                    Object referrerDetails = getInstallReferrer();
                    String installReferrer = getStringInstallReferrer(referrerDetails);
                    long clickTime = getReferrerClickTimestampSeconds(referrerDetails);
                    long installBegin = getInstallBeginTimestampSeconds(referrerDetails);
                    logger.debug("installReferrer: %s, clickTime: %d, installBeginTime: %d",
                            installReferrer, clickTime, installBegin);

                    // Stuff successfully read, try to send it.
                    IActivityHandler activityHandler = activityHandlerWeakRef.get();
                    if (activityHandler != null) {
                        activityHandler.sendInstallReferrer(clickTime, installBegin, installReferrer);
                    }
                    synchronized (flagLock) {
                        hasInstallReferrerBeenRead = true;
                    }
                } catch (Exception e) {
                    logger.debug("Couldn't get install referrer from client (%s). Retrying ...", e.getMessage());
                    retry();
                }
                break;
            case STATUS_FEATURE_NOT_SUPPORTED:
                // API not available on the current Play Store app
                logger.debug("Install referrer not available on the current Play Store app.");
                break;
            case STATUS_SERVICE_UNAVAILABLE:
                // Connection could not be established
                logger.debug("Could not initiate connection to the Install Referrer service. Retrying ...");
                retry();
                break;
            case STATUS_DEVELOPER_ERROR:
                logger.debug("Install referrer general errors caused by incorrect usage. Retrying ...");
                retry();
                break;
            case STATUS_SERVICE_DISCONNECTED:
                // Play Store service is not connected now - potentially transient state
                logger.debug("Play Store service is not connected now. Retrying ...");
                retry();
                break;
            default:
                logger.debug("Unexpected response code of install referrer response: %d", responseCode);
                break;
        }
        closeReferrerClient();
    }

    /**
     * Get ReferrerDetails object (response).
     *
     * @return ReferrerDetails object
     */
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

    /**
     * Get install referrer string value.
     *
     * @param referrerDetails ReferrerDetails object
     * @return Install referrer string value.
     */
    private String getStringInstallReferrer(final Object referrerDetails) {
        if (referrerDetails == null) {
            return null;
        }
        try {
            String stringInstallReferrer = (String) Reflection.invokeInstanceMethod(
                    referrerDetails, "getInstallReferrer", null);
            return stringInstallReferrer;
        } catch (Exception e) {
            logger.error("getStringInstallReferrer error (%s)", e.getMessage());
        }
        return null;
    }

    /**
     * Get redirect URL click timestamp.
     *
     * @param referrerDetails ReferrerDetails object
     * @return Redirect URL click timestamp.
     */
    private long getReferrerClickTimestampSeconds(final Object referrerDetails) {
        if (referrerDetails == null) {
            return -1;
        }
        try {
            Long clickTime = (Long) Reflection.invokeInstanceMethod(
                    referrerDetails, "getReferrerClickTimestampSeconds", null);
            return clickTime;
        } catch (Exception e) {
            logger.error("getReferrerClickTimestampSeconds error (%s)", e.getMessage());
        }
        return -1;
    }

    /**
     * Get Play Store app INSTALL button click timestamp.
     *
     * @param referrerDetails ReferrerDetails object
     * @return Play Store app INSTALL button click timestamp.
     */
    private long getInstallBeginTimestampSeconds(final Object referrerDetails) {
        if (referrerDetails == null) {
            return -1;
        }
        try {
            Long installBeginTime = (Long) Reflection.invokeInstanceMethod(
                    referrerDetails, "getInstallBeginTimestampSeconds", null);
            return installBeginTime;
        } catch (Exception e) {
            logger.error("getInstallBeginTimestampSeconds error (%s)", e.getMessage());
        }
        return -1;
    }

    /**
     * Retry connection to install referrer service.
     */
    private void retry() {
        synchronized (flagLock) {
            if (hasInstallReferrerBeenRead) {
                logger.debug("Install referrer has already been read");
                return;
            }
        }
        // Increase retry counter
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
        retryTimer.startIn(retryWaitTime);
    }

    /**
     * Terminate connection to install referrer service.
     */
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
