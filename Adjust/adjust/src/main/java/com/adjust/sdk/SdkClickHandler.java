package com.adjust.sdk;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by pfms on 31/03/16.
 */
public class SdkClickHandler implements ISdkClickHandler {
    private CustomScheduledExecutor scheduledExecutor;
    private ILogger logger;
    private boolean paused;
    private List<ActivityPackage> packageQueue;
    private BackoffStrategy backoffStrategy;
    private WeakReference<IActivityHandler> activityHandlerWeakRef;
    private String basePath;

    @Override
    public void teardown() {
        logger.verbose("SdkClickHandler teardown");
        if (scheduledExecutor != null) {
            try {
                scheduledExecutor.shutdownNow();
            } catch(SecurityException se) {}
        }
        if (packageQueue != null) {
            packageQueue.clear();
        }
        if (activityHandlerWeakRef != null) {
            activityHandlerWeakRef.clear();
        }
        scheduledExecutor = null;
        logger = null;
        packageQueue = null;
        backoffStrategy = null;
    }

    public SdkClickHandler(IActivityHandler activityHandler, boolean startsSending) {
        init(activityHandler, startsSending);
        this.logger = AdjustFactory.getLogger();
        this.scheduledExecutor = new CustomScheduledExecutor("SdkClickHandler", false);
        this.backoffStrategy = AdjustFactory.getSdkClickBackoffStrategy();
    }

    @Override
    public void init(IActivityHandler activityHandler, boolean startsSending) {
        this.paused = !startsSending;
        this.packageQueue = new ArrayList<ActivityPackage>();
        this.activityHandlerWeakRef = new WeakReference<IActivityHandler>(activityHandler);
        this.basePath = activityHandler.getBasePath();
    }

    @Override
    public void pauseSending() {
        paused = true;
    }

    @Override
    public void resumeSending() {
        paused = false;

        sendNextSdkClick();
    }

    @Override
    public void sendSdkClick(final ActivityPackage sdkClick) {
        scheduledExecutor.submit(new Runnable() {
            @Override
            public void run() {
                packageQueue.add(sdkClick);
                logger.debug("Added sdk_click %d", packageQueue.size());
                logger.verbose("%s", sdkClick.getExtendedString());
                sendNextSdkClick();
            }
        });
    }

    private void sendNextSdkClick() {
        scheduledExecutor.submit(new Runnable() {
            @Override
            public void run() {
                sendNextSdkClickI();
            }
        });
    }

    private void sendNextSdkClickI() {
        if (paused) {
            return;
        }

        if (packageQueue.isEmpty()) {
            return;
        }

        final ActivityPackage sdkClickPackage = packageQueue.remove(0);
        int retries = sdkClickPackage.getRetries();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                sendSdkClickI(sdkClickPackage);
                sendNextSdkClick();
            }
        };

        if (retries <= 0) {
            runnable.run();
            return;
        }

        long waitTimeMilliSeconds = Util.getWaitingTime(retries, backoffStrategy);

        double waitTimeSeconds = waitTimeMilliSeconds / 1000.0;
        String secondsString = Util.SecondsDisplayFormat.format(waitTimeSeconds);

        logger.verbose("Waiting for %s seconds before retrying sdk_click for the %d time", secondsString, retries);
        scheduledExecutor.schedule(runnable, waitTimeMilliSeconds, TimeUnit.MILLISECONDS);
    }

    private void sendSdkClickI(ActivityPackage sdkClickPackage) {
        String url = AdjustFactory.getBaseUrl();
        if (basePath != null) {
            url += basePath;
        }

        String targetURL = url + sdkClickPackage.getPath();

        try {
            ResponseData responseData = UtilNetworking.createPOSTHttpsURLConnection(targetURL, sdkClickPackage, packageQueue.size() - 1);

            if (responseData.jsonResponse == null) {
                retrySendingI(sdkClickPackage);
                return;
            }

            IActivityHandler activityHandler = activityHandlerWeakRef.get();
            if (activityHandler == null) {
                return;
            }

            activityHandler.finishedTrackingActivity(responseData);

        } catch (UnsupportedEncodingException e) {
            logErrorMessageI(sdkClickPackage, "Sdk_click failed to encode parameters", e);
        } catch (SocketTimeoutException e) {
            logErrorMessageI(sdkClickPackage, "Sdk_click request timed out. Will retry later", e);
            retrySendingI(sdkClickPackage);
        } catch (IOException e) {
            logErrorMessageI(sdkClickPackage, "Sdk_click request failed. Will retry later", e);
            retrySendingI(sdkClickPackage);
        } catch (Throwable e) {
            logErrorMessageI(sdkClickPackage, "Sdk_click runtime exception", e);
        }
    }

    private void retrySendingI(ActivityPackage sdkClickPackage) {
        int retries = sdkClickPackage.increaseRetries();

        logger.error("Retrying sdk_click package for the %d time", retries);
        sendSdkClick(sdkClickPackage);
    }

    private void logErrorMessageI(ActivityPackage sdkClickPackage, String message, Throwable throwable) {
        final String packageMessage = sdkClickPackage.getFailureMessage();
        final String reasonString = Util.getReasonString(message, throwable);
        String finalMessage = String.format("%s. (%s)", packageMessage, reasonString);
        logger.error(finalMessage);
    }
}
