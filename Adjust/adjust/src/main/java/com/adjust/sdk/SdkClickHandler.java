package com.adjust.sdk;

import org.json.JSONArray;
import org.json.JSONException;

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
     * Intent based referrer source name inside of sdk_click package.
     */
    private static final String SOURCE_REFTAG = "reftag";

    /**
     * Install referrer service referrer source name inside of sdk_click package.
     */
    private static final String SOURCE_INSTALL_REFERRER = "install_referrer";

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
    public void sendReftagReferrers() {
        scheduledExecutor.submit(new Runnable() {
            @Override
            public void run() {
                IActivityHandler activityHandler = activityHandlerWeakRef.get();
                SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(
                        activityHandler.getContext());
                try {
                    JSONArray rawReferrerArray = sharedPreferencesManager.getRawReferrerArray();
                    boolean hasRawReferrersBeenChanged = false;

                    for (int i = 0; i < rawReferrerArray.length(); i++) {
                        JSONArray savedRawReferrer = rawReferrerArray.getJSONArray(i);

                        int savedRawReferrerState = savedRawReferrer.optInt(2, -1);

                        // Don't send the one already sending or sent.
                        if (savedRawReferrerState != 0) {
                            continue;
                        }

                        String savedRawReferrerString = savedRawReferrer.optString(0, null);
                        long savedClickTime = savedRawReferrer.optLong(1, -1);
                        // Mark install referrer as being sent.
                        savedRawReferrer.put(2, 1);
                        hasRawReferrersBeenChanged = true;

                        // Create sdk click
                        ActivityPackage sdkClickPackage = PackageFactory.buildReftagSdkClickPackage(
                                savedRawReferrerString,
                                savedClickTime,
                                activityHandler.getActivityState(),
                                activityHandler.getAdjustConfig(),
                                activityHandler.getDeviceInfo(),
                                activityHandler.getSessionParameters());

                        // Send referrer sdk_click package.
                        sendSdkClick(sdkClickPackage);
                    }

                    if (hasRawReferrersBeenChanged) {
                        sharedPreferencesManager.saveRawReferrerArray(rawReferrerArray);
                    }
                } catch (JSONException e) {
                    logger.error("Send saved raw referrers error (%s)", e.getMessage());
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
        IActivityHandler activityHandler = activityHandlerWeakRef.get();
        String source = sdkClickPackage.getParameters().get("source");

        boolean isReftag = source != null && source.equals(SOURCE_REFTAG);
        String rawReferrerString = sdkClickPackage.getParameters().get("raw_referrer");

        if (isReftag) {
            // Check before sending if referrer was removed already.
            SharedPreferencesManager sharedPreferencesManager
                    = new SharedPreferencesManager(activityHandler.getContext());

            JSONArray rawReferrer = sharedPreferencesManager.getRawReferrer(
                    rawReferrerString,
                    sdkClickPackage.getClickTimeInMilliseconds());

            if (rawReferrer == null) {
                return;
            }
        }

        boolean isInstallReferrer = source != null && source.equals(SOURCE_INSTALL_REFERRER);
        long clickTime = -1;
        long installBegin = -1;
        String installReferrer = null;

        if (isInstallReferrer) {
            // Check if install referrer information is saved to activity state.
            // If yes, we have successfully sent it at earlier point and no need to do it again.
            // If not, proceed with sending of sdk_click package for install referrer.
            clickTime = sdkClickPackage.getClickTimeInSeconds();
            installBegin = sdkClickPackage.getInstallBeginTimeInSeconds();
            installReferrer = sdkClickPackage.getParameters().get("referrer");
        }

        String targetURL = Constants.BASE_URL + sdkClickPackage.getPath();

        try {
            SdkClickResponseData responseData = (SdkClickResponseData) UtilNetworking.createPOSTHttpsURLConnection(
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

            if (isReftag) {
                // Remove referrer from shared preferences after sdk_click is sent.
                SharedPreferencesManager sharedPreferencesManager
                        = new SharedPreferencesManager(activityHandler.getContext());

                sharedPreferencesManager.removeRawReferrer(
                        rawReferrerString,
                        sdkClickPackage.getClickTimeInMilliseconds());
            }

            if (isInstallReferrer) {
                // After successfully sending install referrer, store sent values in activity state.
                responseData.clickTime = clickTime;
                responseData.installBegin = installBegin;
                responseData.installReferrer = installReferrer;
                responseData.isInstallReferrer = true;
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
        final String finalMessage = Util.formatString("%s. (%s)", packageMessage, reasonString);

        logger.error(finalMessage);
    }
}
