package com.adjust.sdk;

import android.content.Context;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
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
    private static final String PACKAGE_BASE_NAME = "com.android.installreferrer.";

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
    private final Object flagLock;

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
    }

    /**
     * Start connection with install referrer service.
     */
    public void startConnection() {
        if (!AdjustFactory.getTryInstallReferrer()) {
            return;
        }
        closeReferrerClient();

        synchronized (flagLock) {
            if (hasInstallReferrerBeenRead) {
                logger.debug("Install referrer has already been read");
                return;
            }
        }

        if (context == null) {
            return;
        }

        this.referrerClient = createInstallReferrerClient(context);
        if (this.referrerClient == null) {
            return;
        }

        Class listenerClass = getInstallReferrerStateListenerClass();
        if (listenerClass == null) {
            return;
        }

        Object listenerProxy = createProxyInstallReferrerStateListener(listenerClass);
        if (listenerProxy == null) {
            return;
        }

        startConnection(listenerClass, listenerProxy);
    }

    /**
     * Create InstallReferrerClient object instance.
     *
     * @param context App context
     * @return Instance of InstallReferrerClient. Defaults to null if failed to create one.
     */
    private Object createInstallReferrerClient(final Context context) {
        try {
            Object builder = Reflection.invokeStaticMethod(PACKAGE_BASE_NAME + "api.InstallReferrerClient",
                    "newBuilder",
                    new Class[]{Context.class}, context);
            return Reflection.invokeInstanceMethod(builder, "build", null);
        } catch (ClassNotFoundException ex) {
            logger.warn("InstallReferrer not integrated in project (%s) thrown by (%s)",
                    ex.getMessage(),
                    ex.getClass().getCanonicalName());
        } catch (Exception ex) {
            logger.error("createInstallReferrerClient error (%s) from (%s)",
                    ex.getMessage(),
                    ex.getClass().getCanonicalName());
        }

        return null;
    }

    /**
     * Get InstallReferrerStateListener class object.
     *
     * @return Class object for InstallReferrerStateListener class.
     */
    private Class getInstallReferrerStateListenerClass() {
        try {
            return Class.forName(PACKAGE_BASE_NAME + "api.InstallReferrerStateListener");
        } catch (Exception ex) {
            logger.error("getInstallReferrerStateListenerClass error (%s) from (%s)",
                    ex.getMessage(),
                    ex.getClass().getCanonicalName());
        }
        return null;
    }

    /**
     * Get object instance for given class (InstallReferrerStateListener in this case).
     *
     * @param installReferrerStateListenerClass Class object
     * @return Instance of Class type object.
     */
    private Object createProxyInstallReferrerStateListener(final Class installReferrerStateListenerClass) {
        Object proxyInstance = null;

        try {
            proxyInstance = Proxy.newProxyInstance(
                    installReferrerStateListenerClass.getClassLoader(),
                    new Class[]{installReferrerStateListenerClass},
                    this
            );
        } catch (IllegalArgumentException ex) {
            logger.error("InstallReferrer proxy violating parameter restrictions");
        } catch (NullPointerException ex) {
            logger.error("Null argument passed to InstallReferrer proxy");
        }

        return proxyInstance;
    }

    /**
     * Initialise connection with install referrer service.
     *
     * @param listenerClass Callback listener class type
     * @param listenerProxy Callback listener object instance
     */
    private void startConnection(final Class listenerClass, final Object listenerProxy) {
        try {
            Reflection.invokeInstanceMethod(this.referrerClient, "startConnection",
                    new Class[]{listenerClass}, listenerProxy);
        } catch (InvocationTargetException ex) {
            // Check for an underlying root cause in the stack trace
            if (Util.hasRootCause(ex)) {
                logger.error("InstallReferrer encountered an InvocationTargetException %s",
                        Util.getRootCause(ex));
            }
        } catch (Exception ex) {
            logger.error("startConnection error (%s) thrown by (%s)",
                    ex.getMessage(),
                    ex.getClass().getCanonicalName());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object invoke(final Object proxy, final Method method, Object[] args)
            throws Throwable {
        if (method == null) {
            logger.error("InstallReferrer invoke method null");
            return null;
        }
        String methodName = method.getName();
        if (methodName == null) {
            logger.error("InstallReferrer invoke method name null");
            return null;
        }
        // Prints the method being invoked
        logger.debug("InstallReferrer invoke method name: %s", methodName);
        if (args == null) {
            logger.warn("InstallReferrer invoke args null");
            args = new Object[0];
        }
        for (Object arg : args) {
            logger.debug("InstallReferrer invoke arg: %s", arg);
        }

        // if the method name equals some method's name then call your method
        if (methodName.equals("onInstallReferrerSetupFinished")) {
            if (args.length != 1) {
                logger.error("InstallReferrer invoke onInstallReferrerSetupFinished args lenght not 1: %d", args.length);
                return null;
            }

            Object arg = args[0];
            if (!(arg instanceof Integer)) {
                logger.error("InstallReferrer invoke onInstallReferrerSetupFinished arg not int");
                return null;
            }

            Integer responseCode = (Integer) arg;
            if (responseCode == null) {
                logger.error("InstallReferrer invoke onInstallReferrerSetupFinished responseCode arg is null");
                return null;
            }

            onInstallReferrerSetupFinishedInt(responseCode);
        } else if (methodName.equals("onInstallReferrerServiceDisconnected")) {
            logger.debug("InstallReferrer onInstallReferrerServiceDisconnected");
        }
        return null;
    }

    /**
     * Check and process response from install referrer service.
     *
     * @param responseCode Response code from install referrer service
     */
    private void onInstallReferrerSetupFinishedInt(final int responseCode) {
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
                    closeReferrerClient();
                } catch (Exception e) {
                    logger.warn("Couldn't get install referrer from client (%s). Retrying ...", e.getMessage());
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
                closeReferrerClient();
                break;
        }
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
            logger.error("getInstallReferrer error (%s) thrown by (%s)",
                    e.getMessage(),
                    e.getClass().getCanonicalName());
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
            logger.error("getStringInstallReferrer error (%s) thrown by (%s)",
                    e.getMessage(),
                    e.getClass().getCanonicalName());
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
            logger.error("getReferrerClickTimestampSeconds error (%s) thrown by (%s)",
                    e.getMessage(),
                    e.getClass().getCanonicalName());
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
            logger.error("getInstallBeginTimestampSeconds error (%s) thrown by (%s)",
                    e.getMessage(),
                    e.getClass().getCanonicalName());
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
            logger.error("closeReferrerClient error (%s) thrown by (%s)",
                    e.getMessage(),
                    e.getClass().getCanonicalName());
        }
        referrerClient = null;
    }
}
