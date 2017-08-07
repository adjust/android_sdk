package com.adjust.sdk;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import java.lang.ref.WeakReference;
import java.net.SocketTimeoutException;

/**
 * SdkClickHandler class.
 *
 * @author Pedro Silva (nonelse)
 * @since 31st March 2016
 */

public class SdkClickHandler implements ISdkClickHandler {
    /**
     * Divisor for milliseconds -> seconds conversion.
     */
    private static final double MILLISECONDS_TO_SECONDS_DIVISOR = 1000.0;

    /**
     * SdkClickHandler scheduled executor source.
     */
    private static final String SCHEDULED_EXECUTOR_SOURCE = "SdkClickHandler";

    /**
     * Indicates whether SdkClickHandler is paused or not.
     */
    private boolean paused;

    /**
     * Adjust logger.
     */
    private ILogger logger;

    /**
     * Backoff strategy.
     */
    private BackoffStrategy backoffStrategy;

    /**
     * Sending queue.
     */
    private List<ActivityPackage> packageQueue;

    /**
     * Custom actions scheduled executor.
     */
    private CustomScheduledExecutor scheduledExecutor;

    /**
     * ActivityHandler instance.
     */
    private WeakReference<IActivityHandler> activityHandlerWeakRef;

    /**
     * SdkClickHandler constructor.
     *
     * @param activityHandler ActivityHandler reference
     * @param startsSending   Is sending paused?
     */
    public SdkClickHandler(final IActivityHandler activityHandler, final boolean startsSending) {
        init(activityHandler, startsSending);

        logger = AdjustFactory.getLogger();
        backoffStrategy = AdjustFactory.getSdkClickBackoffStrategy();
        scheduledExecutor = new CustomScheduledExecutor(SCHEDULED_EXECUTOR_SOURCE, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(final IActivityHandler activityHandler, final boolean startsSending) {
        paused = !startsSending;
        packageQueue = new ArrayList<ActivityPackage>();
        activityHandlerWeakRef = new WeakReference<IActivityHandler>(activityHandler);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void pauseSending() {
        paused = true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void resumeSending() {
        paused = false;

        sendNextSdkClick();
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendSavedReferrers() {
        scheduledExecutor.submit(new Runnable() {
            @Override
            public void run() {
                SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(
                        ((ActivityHandler) activityHandlerWeakRef.get()).getAdjustConfig().context);
                // JSONArray referrerQueue = sharedPreferencesManager.getReferrers();
                ArrayList<Referrer> referrers = sharedPreferencesManager.getReferrers();

                for (int i = 0; i < referrers.size(); i += 1) {
                    Referrer referrer = referrers.get(i);

                    // Don't send the one already flagged for sending.
                    if (referrer.getIsBeingSent()) {
                        continue;
                    }

                    ActivityPackage sdkClickPackage = PackageFactory.getSdkClickPackage(
                            referrer.getContent(),
                            referrer.getClickTime(),
                            ((ActivityHandler) activityHandlerWeakRef.get()).getActivityState(),
                            ((ActivityHandler) activityHandlerWeakRef.get()).getAdjustConfig(),
                            ((ActivityHandler) activityHandlerWeakRef.get()).getDeviceInfo(),
                            ((ActivityHandler) activityHandlerWeakRef.get()).getSessionParameters());

                    if (sdkClickPackage == null) {
                        return;
                    }

                    // Mark referrer as being sent.
                    sharedPreferencesManager.markReferrerForSending(referrer);

                    // Send referrer sdk_click package.
                    sendSdkClick(sdkClickPackage);
                }
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void teardown() {
        logger.verbose("SdkClickHandler teardown");

        if (scheduledExecutor != null) {
            try {
                scheduledExecutor.shutdownNow();
            } catch (SecurityException e) {

            }
        }

        if (packageQueue != null) {
            packageQueue.clear();
        }

        if (activityHandlerWeakRef != null) {
            activityHandlerWeakRef.clear();
        }

        logger = null;
        packageQueue = null;
        backoffStrategy = null;
        scheduledExecutor = null;
    }

    /**
     * Send next sdk_click package from the queue.
     */
    private void sendNextSdkClick() {
        scheduledExecutor.submit(new Runnable() {
            @Override
            public void run() {
                sendNextSdkClickI();
            }
        });
    }

    /**
     * Send next sdk_click package from the queue (runs within scheduled executor).
     */
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
        double waitTimeSeconds = waitTimeMilliSeconds / MILLISECONDS_TO_SECONDS_DIVISOR;
        String secondsString = Util.SecondsDisplayFormat.format(waitTimeSeconds);

        logger.verbose("Waiting for %s seconds before retrying sdk_click for the %d time", secondsString, retries);

        scheduledExecutor.schedule(runnable, waitTimeMilliSeconds, TimeUnit.MILLISECONDS);
    }

    /**
     * Send sdk_click package passed as the parameter (runs within scheduled executor).
     *
     * @param sdkClickPackage sdk_click package to be sent.
     */
    private void sendSdkClickI(final ActivityPackage sdkClickPackage) {
        // Check before sending if referrer was sent and removed already.
        ActivityHandler activityHandler = (ActivityHandler) activityHandlerWeakRef.get();
        SharedPreferencesManager sharedPreferencesManager
                = new SharedPreferencesManager(activityHandler.getContext());

        if (!sharedPreferencesManager.doesReferrerExist(
                sdkClickPackage.getParameters().get("referrer"),
                sdkClickPackage.getClickTime())) {
            return;
        }

        String targetURL = Constants.BASE_URL + sdkClickPackage.getPath();

        try {
            ResponseData responseData = UtilNetworking.createPOSTHttpsURLConnection(
                    targetURL,
                    sdkClickPackage,
                    packageQueue.size() - 1);

            if (responseData.jsonResponse == null) {
                retrySendingI(sdkClickPackage);
                return;
            }

            if (activityHandler == null) {
                return;
            }

            // Remove referrer from shared preferences after sdk_click is sent.
            sharedPreferencesManager.removeReferrer(
                    sdkClickPackage.getClickTime(),
                    sdkClickPackage.getParameters().get("referrer"));

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

    /**
     * Retry sending of the sdk_click package passed as the parameter (runs within scheduled executor).
     *
     * @param sdkClickPackage sdk_click package to be retried.
     */
    private void retrySendingI(final ActivityPackage sdkClickPackage) {
        int retries = sdkClickPackage.increaseRetries();

        logger.error("Retrying sdk_click package for the %d time", retries);

        sendSdkClick(sdkClickPackage);
    }

    /**
     * Print error log messages (runs within scheduled executor).
     *
     * @param sdkClickPackage sdk_click package for which error occured.
     * @param message         Message content.
     * @param throwable       Throwable to read the reason of the error.
     */
    private void logErrorMessageI(final ActivityPackage sdkClickPackage,
                                  final String message,
                                  final Throwable throwable) {
        final String packageMessage = sdkClickPackage.getFailureMessage();
        final String reasonString = Util.getReasonString(message, throwable);
        final String finalMessage = String.format("%s. (%s)", packageMessage, reasonString);

        logger.error(finalMessage);
    }
}
