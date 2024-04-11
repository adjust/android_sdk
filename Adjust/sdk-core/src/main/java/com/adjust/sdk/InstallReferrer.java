package com.adjust.sdk;

import android.content.Context;

import com.adjust.sdk.scheduler.SingleThreadCachedScheduler;
import com.adjust.sdk.scheduler.ThreadExecutor;
import com.adjust.sdk.scheduler.TimerOnce;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicBoolean;

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
     * Boolean indicating whether service should be tried to read.
     * Either because it has not yet tried,
     * or it did and it was successful
     * or it did, was not successful, but it should not retry
     */
    private final AtomicBoolean shouldTryToRead;

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
     * Timer which fires retry attempts.
     */
    private TimerOnce retryTimer;

    /**
     * Referrer callback.
     */
    private final InstallReferrerReadListener referrerCallback;

    private ThreadExecutor executor;

    /**
     * Default constructor.
     *
     * @param context          Application context
     * @param referrerCallback Callback for referrer information
     */
    public InstallReferrer(final Context context, final InstallReferrerReadListener referrerCallback) {
        this.logger = AdjustFactory.getLogger();
        this.context = context;
        this.shouldTryToRead = new AtomicBoolean(true);
        this.retries = 0;
        this.retryTimer = new TimerOnce(new Runnable() {
            @Override
            public void run() {
                startConnection();
            }
        }, "InstallReferrer");
        this.referrerCallback = referrerCallback;
        this.executor = new SingleThreadCachedScheduler("InstallReferrer");
    }

    /**
     * Start connection with install referrer service.
     */
    public void startConnection() {
        if (! AdjustFactory.getTryInstallReferrer()) {
            this.referrerCallback.onFail("Sdk has been configured to not try install referrer");
            return;
        }
        closeReferrerClient();

        if (! shouldTryToRead.get()) {
            this.referrerCallback.onFail("Should not try to read the install referrer any longer");
            return;
        }

        if (context == null) {
            this.referrerCallback.onFail("Requires context to read the install referrer");
            return;
        }

        this.referrerClient = createInstallReferrerClient(context);
        if (this.referrerClient == null) {
            // this.referrerCallback.onFail called inside createInstallReferrerClient(context)
            return;
        }

        final Class listenerClass = getInstallReferrerStateListenerClass();
        if (listenerClass == null) {
            // this.referrerCallback.onFail called inside getInstallReferrerStateListenerClass()
            return;
        }

        final Object listenerProxy = createProxyInstallReferrerStateListener(listenerClass);
        if (listenerProxy == null) {
            // this.referrerCallback.onFail called inside createProxyInstallReferrerStateListener(listenerClass)
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
        } catch (final ClassNotFoundException ex) {
            this.referrerCallback.onFail(
              Util.formatString("InstallReferrer not integrated in project (%s) thrown by (%s)",
                ex.getMessage(), ex.getClass().getCanonicalName()));
        } catch (final Exception ex) {
            this.referrerCallback.onFail(
              Util.formatString("createInstallReferrerClient error (%s) from (%s)",
                ex.getMessage(), ex.getClass().getCanonicalName()));
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
            this.referrerCallback.onFail(
              Util.formatString("getInstallReferrerStateListenerClass error (%s) from (%s)",
                ex.getMessage(), ex.getClass().getCanonicalName()));
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
        } catch (final IllegalArgumentException ex) {
            this.referrerCallback.onFail("InstallReferrer proxy violating parameter restrictions");
        } catch (final NullPointerException ex) {
            this.referrerCallback.onFail("Null argument passed to InstallReferrer proxy");
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
        } catch (final InvocationTargetException ex) {
            if (Util.hasRootCause(ex)) {
                // Check for an underlying root cause in the stack trace
                referrerCallback.onFail(
                  Util.formatString("InstallReferrer encountered an InvocationTargetException %s",
                    Util.getRootCause(ex)));
            } else {
                referrerCallback.onFail("InstallReferrer encountered an InvocationTargetException");
            }
        } catch (final Exception ex) {
            referrerCallback.onFail(Util.formatString("startConnection error (%s) thrown by (%s)",
              ex.getMessage(), ex.getClass().getCanonicalName()));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args)
            throws Throwable {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    invokeI(proxy, method, args);
                } catch (final Throwable throwable) {
                    referrerCallback.onFail(Util.formatString("invoke error (%s) thrown by (%s)",
                      throwable.getMessage(), throwable.getClass().getCanonicalName()));
                }
            }
        });

        return null;
    }

    private Object invokeI(final Object proxy, final Method method, Object[] args)
      throws Throwable
    {
        if (method == null) {
            this.referrerCallback.onFail("InstallReferrer invoke method null");
            return null;
        }
        String methodName = method.getName();
        if (methodName == null) {
            this.referrerCallback.onFail("InstallReferrer invoke method name null");
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
                this.referrerCallback.onFail(Util.formatString(
                  "InstallReferrer invoke onInstallReferrerSetupFinished args lenght not 1: %d",
                  args.length));
                return null;
            }

            Object arg = args[0];
            if (!(arg instanceof Integer)) {
                this.referrerCallback.onFail("InstallReferrer invoke onInstallReferrerSetupFinished arg not int");
                return null;
            }

            Integer responseCode = (Integer) arg;
            if (responseCode == null) {
                this.referrerCallback.onFail("InstallReferrer invoke onInstallReferrerSetupFinished responseCode arg is null");
                return null;
            }

            onInstallReferrerSetupFinishedIntI(responseCode);
        } else if (methodName.equals("onInstallReferrerServiceDisconnected")) {
            logger.debug("Connection to install referrer service was lost. Retrying ...");
            retryI("onInstallReferrerServiceDisconnected");
        } else {
            this.referrerCallback.onFail(
              Util.formatString("Reflection call method name not expected: %s", methodName));
        }
        return null;
    }

    /**
     * Check and process response from install referrer service.
     *
     * @param responseCode Response code from install referrer service
     */
    private void onInstallReferrerSetupFinishedIntI(final int responseCode) {
        boolean retryAtEnd = false;
        switch (responseCode) {
            /** Success. */
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

                    String installVersion = getStringInstallVersion(referrerDetails);
                    long clickTimeServer = getReferrerClickTimestampServerSeconds(referrerDetails);
                    long installBeginServer = getInstallBeginTimestampServerSeconds(referrerDetails);
                    Boolean googlePlayInstant = getBooleanGooglePlayInstantParam(referrerDetails);
                    logger.debug("installVersion: %s, clickTimeServer: %d, " +
                                    "installBeginServer: %d, googlePlayInstant: %b",
                            installVersion, clickTimeServer, installBeginServer, googlePlayInstant);

                    logger.debug("Install Referrer read successfully. Closing connection");

                    ReferrerDetails installReferrerDetails = new ReferrerDetails(installReferrer,
                            clickTime, installBegin, clickTimeServer, installBeginServer,
                            installVersion, googlePlayInstant, null);

                    // Stuff successfully read, try to send it.
                    referrerCallback.onInstallReferrerRead(installReferrerDetails, Constants.REFERRER_API_GOOGLE);
                } catch (Exception e) {
                    logger.warn(
                      "Couldn't get install referrer from client (%s). Retrying...",
                      e.getMessage());
                    // retrying, so no need to referrerCallback.onFail
                    retryAtEnd = true;
                }
                break;
            /** Install Referrer API not supported by the installed Play Store app. */
            case STATUS_FEATURE_NOT_SUPPORTED:
                // API not available on the current Play Store app
                this.referrerCallback.onFail(
                  "Install Referrer API not supported by the installed Play Store app. Closing connection");
                break;
            /** Could not initiate connection to the Install Referrer service. */
            case STATUS_SERVICE_UNAVAILABLE:
                // Connection could not be established
                logger.debug("Could not initiate connection to the Install Referrer service. Retrying...");
                // retrying, so no need to referrerCallback.onFail
                retryAtEnd = true;
                break;
            /**
             * Play Store service is not connected now - potentially transient state.
             *
             * <p>E.g. Play Store could have been updated in the background while your app was still
             * running. So feel free to introduce your retry policy for such use case. It should lead to a
             * call to {@link #startConnection(InstallReferrerStateListener)} right after or in some time
             * after you received this code.
             */
            case STATUS_SERVICE_DISCONNECTED:
                // Play Store service is not connected now - potentially transient state
                logger.debug("Play Store service is not connected now. Retrying...");
                // retrying, so no need to referrerCallback.onFail
                retryAtEnd = true;
                break;
            /** General errors caused by incorrect usage */
            case STATUS_DEVELOPER_ERROR:
                logger.debug("Install Referrer API general errors caused by incorrect usage. Retrying...");
                // retrying, so no need to referrerCallback.onFail
                retryAtEnd = true;
                break;
            default:
                this.referrerCallback.onFail(Util.formatString(
                  "Unexpected response code of install referrer response: %d. Closing connection",
                  responseCode));
                break;
        }
        if (retryAtEnd) {
            retryI("end of onInstallReferrerSetupFinishedIntI");
        } else {
            shouldTryToRead.set(false);
            closeReferrerClient();
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
     * Get install version string value.
     *
     * @param referrerDetails ReferrerDetails object
     * @return Install version string value.
     */
    private String getStringInstallVersion(final Object referrerDetails) {
        if (referrerDetails == null) {
            return null;
        }
        try {
            String stringInstallVersion = (String) Reflection.invokeInstanceMethod(
                    referrerDetails, "getInstallVersion", null);
            return stringInstallVersion;
        } catch (Exception e) {
            // not logging the error as this is expected to happen below v2.0
        }
        return null;
    }

    /**
     * Get redirect URL click timestamp server.
     *
     * @param referrerDetails ReferrerDetails object
     * @return Redirect URL click timestamp server.
     */
    private long getReferrerClickTimestampServerSeconds(final Object referrerDetails) {
        if (referrerDetails == null) {
            return -1;
        }
        try {
            Long clickTimeServer = (Long) Reflection.invokeInstanceMethod(
                    referrerDetails, "getReferrerClickTimestampServerSeconds", null);
            return clickTimeServer;
        } catch (Exception e) {
            // not logging the error as this is expected to happen below v2.0
        }
        return -1;
    }

    /**
     * Get Play Store app INSTALL button click timestamp server.
     *
     * @param referrerDetails ReferrerDetails object
     * @return Play Store app INSTALL button click timestamp server.
     */
    private long getInstallBeginTimestampServerSeconds(final Object referrerDetails) {
        if (referrerDetails == null) {
            return -1;
        }
        try {
            Long installBeginTime = (Long) Reflection.invokeInstanceMethod(
                    referrerDetails, "getInstallBeginTimestampServerSeconds", null);
            return installBeginTime;
        } catch (Exception e) {
            // not logging the error as this is expected to happen below v2.0
        }
        return -1;
    }

    /**
     * Get google play instant boolean value.
     *
     * @param referrerDetails ReferrerDetails object
     * @return Google play instant boolean value.
     */
    private Boolean getBooleanGooglePlayInstantParam(final Object referrerDetails) {
        if (referrerDetails == null) {
            return null;
        }
        try {
            boolean booleanGooglePlayInstantParam = (boolean) Reflection.invokeInstanceMethod(
                    referrerDetails, "getGooglePlayInstantParam", null);
            return booleanGooglePlayInstantParam;
        } catch (Exception e) {
            // not logging the error as this is expected to happen below v2.0
        }
        return null;
    }

    /**
     * Retry connection to install referrer service.
     */
    private void retryI(final String source) {
        if (! shouldTryToRead.get()) {
            this.referrerCallback.onFail(
              Util.formatString("Should not try to read Install referrer from %s", source));
            closeReferrerClient();
            return;
        }
        // Check increase retry counter
        if (retries + 1 > Constants.MAX_INSTALL_REFERRER_RETRIES) {
            this.referrerCallback.onFail(
              Util.formatString("Limit number of retry of %d for install referrer surpassed from %s",
                Constants.MAX_INSTALL_REFERRER_RETRIES, source));
            return;
        }

        long firingIn = retryTimer.getFireIn();
        if (firingIn > 0) {
            logger.debug("Already waiting to retry to read install referrer in %d milliseconds", firingIn);
            return;
        }

        retries++;
        logger.debug("Retry number %d to connect to install referrer API", retries);

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
            logger.debug("Install Referrer API connection closed");
        } catch (Exception e) {
            logger.error("closeReferrerClient error (%s) thrown by (%s)",
                    e.getMessage(),
                    e.getClass().getCanonicalName());
        }
        referrerClient = null;
    }
}
