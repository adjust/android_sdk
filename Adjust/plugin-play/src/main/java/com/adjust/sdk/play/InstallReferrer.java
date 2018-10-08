package com.adjust.sdk.play;

import android.content.Context;
import android.os.RemoteException;

import com.adjust.sdk.AdjustFactory;
import com.adjust.sdk.ILogger;

import com.adjust.sdk.InstallReferrerReadListener;
import com.adjust.sdk.TimerOnce;
import com.android.installreferrer.api.InstallReferrerClient;
import com.android.installreferrer.api.InstallReferrerStateListener;
import com.android.installreferrer.api.ReferrerDetails;

import java.util.concurrent.atomic.AtomicBoolean;

import static com.adjust.sdk.Constants.ONE_SECOND;


public class InstallReferrer implements InstallReferrerStateListener {
    private static final int MAX_INSTALL_REFERRER_RETRIES = 2;
    private static final int RETRY_WAIT_TIME = ONE_SECOND * 3;

    private InstallReferrerClient client;
    private int retryNumber;

    // injected at constructor time and final
    private final ILogger logger;
    private final Context context;
    private final InstallReferrerReadListener referrerCallback;
    private final AtomicBoolean hasInstallReferrerBeenRead;
    private final TimerOnce retryTimer;


    public InstallReferrer(Context context, InstallReferrerReadListener referrerCallback, ILogger logger) {
        this.context = context;
        this.logger = logger;
        this.referrerCallback = referrerCallback;
        this.hasInstallReferrerBeenRead = new AtomicBoolean(false);
        this.retryTimer = new TimerOnce(new Runnable() {
            @Override
            public void run() {
                startConnection();
            }
        }, "InstallReferrer");

        this.retryNumber = 0;
    }

    public void startConnection() {
        if (!AdjustFactory.getTryInstallReferrer()) {
            return;
        }
        closeReferrerClient();

        if (hasInstallReferrerBeenRead.get()) {
            logger.debug("Install referrer has already been read");
            return;
        }

        client = InstallReferrerClient.newBuilder(context).build();

        client.startConnection(this);
    }

    private void closeReferrerClient() {
        if (client == null) {
            return;
        }

        client.endConnection();
        logger.debug("Install Referrer API connection closed");

        client = null;
    }

    /**
     * Called to notify that setup is complete.
     *
     * @param responseCode The response code from {@link InstallReferrerClient.InstallReferrerResponse} which returns the
     *     status of the setup process.
     */
    @Override
    public void onInstallReferrerSetupFinished(int responseCode) {
        boolean retryAtEnd = false;
        switch (responseCode) {
            /** Success. */
            case InstallReferrerClient.InstallReferrerResponse.OK:
                // Connection established
                try {
                    ReferrerDetails installReferrerDetails = client.getInstallReferrer();
                    String installReferrer = installReferrerDetails.getInstallReferrer();
                    long referrerClickTimestampSeconds = installReferrerDetails.getReferrerClickTimestampSeconds();
                    long installBeginTimestampSeconds = installReferrerDetails.getInstallBeginTimestampSeconds();
                    referrerCallback.onInstallReferrerRead(installReferrer, referrerClickTimestampSeconds, installBeginTimestampSeconds);

                    hasInstallReferrerBeenRead.set(true);
                    logger.debug("Install Referrer read successfully. Closing connection");
                } catch (RemoteException e) {
                    logger.warn("Couldn't get install referrer from client (%s). Retrying...", e.getMessage());
                    retryAtEnd = true;
                }
                break;
            /** Install Referrer API not supported by the installed Play Store app. */
            case InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED:
                // API not available on the current Play Store app
                logger.debug("Install Referrer API not supported by the installed Play Store app. Closing connection");
                break;
            /** Could not initiate connection to the Install Referrer service. */
            case InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE:
                // Connection could not be established
                logger.debug("Could not initiate connection to the Install Referrer service. Retrying...");
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
            case InstallReferrerClient.InstallReferrerResponse.SERVICE_DISCONNECTED:
                // Connection could not be established
                logger.debug("Play Store service is not connected now. Retrying ...");
                retryAtEnd = true;
                break;
            /** General errors caused by incorrect usage */
            case InstallReferrerClient.InstallReferrerResponse.DEVELOPER_ERROR:
                logger.debug("Install Referrer API general errors caused by incorrect usage. Retrying...");
                retryAtEnd = true;
                break;
            default:
                logger.debug("Unexpected response code of install referrer response: %d. Closing connection", responseCode);
                break;
        }

        if (retryAtEnd) {
            retry();
        } else {
            closeReferrerClient();
        }
    }

    /**
     * Called to notify that connection to install referrer service was lost.
     *
     * <p>Note: This does not remove install referrer service connection itself - this binding to the
     * service will remain active, and you will receive a call to {@link
     * #onInstallReferrerSetupFinished(int)} when install referrer service is next running and setup
     * is complete.
     */
    @Override
    public void onInstallReferrerServiceDisconnected() {
        // Try to restart the connection on the next request to
        // Google Play by calling the startConnection() method.
        logger.debug("Connection to install referrer service was lost. Retrying ...");
        retry();
    }

    private void retry() {
        if (hasInstallReferrerBeenRead.get()) {
            logger.debug("Install referrer has already been read");
            closeReferrerClient();
            return;
        }

        // Check increase retry counter
        if (retryNumber + 1 > MAX_INSTALL_REFERRER_RETRIES) {
            logger.debug("Limit number of retry of %d for install referrer surpassed", MAX_INSTALL_REFERRER_RETRIES);
            return;
        }

        long firingIn = retryTimer.getFireIn();
        if (firingIn > 0) {
            logger.debug("Already waiting to retry to read install referrer in %d milliseconds", firingIn);
            return;
        }

        retryNumber++;
        logger.debug("Retry number %d to connect to install referrer API", retryNumber);

        retryTimer.startIn(RETRY_WAIT_TIME);
    }
}
