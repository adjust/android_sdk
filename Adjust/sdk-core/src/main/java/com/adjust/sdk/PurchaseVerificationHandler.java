package com.adjust.sdk;

import com.adjust.sdk.network.IActivityPackageSender;
import com.adjust.sdk.scheduler.SingleThreadCachedScheduler;
import com.adjust.sdk.scheduler.ThreadScheduler;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PurchaseVerificationHandler class.
 *
 * @author Ugljesa Erceg (uerceg)
 * @since 30th May 2023
 */

public class PurchaseVerificationHandler implements IPurchaseVerificationHandler {
    /**
     * Divisor for milliseconds -> seconds conversion.
     */
    private static final double MILLISECONDS_TO_SECONDS_DIVISOR = 1000.0;

    /**
     * PurchaseVerificationHandler scheduled executor source.
     */
    private static final String SCHEDULED_EXECUTOR_SOURCE = "PurchaseVerificationHandler";

    /**
     * Indicates whether PurchaseVerificationHandler is paused or not.
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
    private ThreadScheduler scheduler;

    /**
     * ActivityHandler instance.
     */
    private WeakReference<IActivityHandler> activityHandlerWeakRef;

    private IActivityPackageSender activityPackageSender;

    /**
     * PurchaseVerificationHandler constructor.
     *
     * @param activityHandler ActivityHandler reference
     * @param startsSending   Is sending paused?
     */
    public PurchaseVerificationHandler(final IActivityHandler activityHandler,
                                       final boolean startsSending,
                                       final IActivityPackageSender purchaseVerificationHandlerActivityPackageSender)
    {
        init(activityHandler, startsSending, purchaseVerificationHandlerActivityPackageSender);
        this.logger = AdjustFactory.getLogger();
        backoffStrategy = AdjustFactory.getSdkClickBackoffStrategy();
        scheduler = new SingleThreadCachedScheduler(SCHEDULED_EXECUTOR_SOURCE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(final IActivityHandler activityHandler,
                     final boolean startsSending,
                     final IActivityPackageSender purchaseVerificationHandlerActivityPackageSender) {
        paused = !startsSending;
        packageQueue = new ArrayList<ActivityPackage>();
        activityHandlerWeakRef = new WeakReference<IActivityHandler>(activityHandler);
        activityPackageSender = purchaseVerificationHandlerActivityPackageSender;
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

        sendNextPurchaseVerificationPackage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendPurchaseVerificationPackage(final ActivityPackage purchaseVerification) {
        scheduler.submit(new Runnable() {
            @Override
            public void run() {
                packageQueue.add(purchaseVerification);
                logger.debug("Added purchase_verification %d", packageQueue.size());
                logger.verbose("%s", purchaseVerification.getExtendedString());
                sendNextPurchaseVerificationPackage();
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void teardown() {
        logger.verbose("PurchaseVerificationHandler teardown");

        if (scheduler != null) {
            scheduler.teardown();
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
        scheduler = null;
    }

    /**
     * Send next purchase_verification package from the queue.
     */
    private void sendNextPurchaseVerificationPackage() {
        scheduler.submit(new Runnable() {
            @Override
            public void run() {
                sendNextPurchaseVerificationPackageI();
            }
        });
    }

    /**
     * Send next purchase_verification from the queue (runs within scheduled executor).
     */
    private void sendNextPurchaseVerificationPackageI() {
        IActivityHandler activityHandler = activityHandlerWeakRef.get();
        if (activityHandler.getActivityState() == null) {
            return;
        }
        if (activityHandler.getActivityState().isGdprForgotten) {
            return;
        }
        if (paused) {
            return;
        }
        if (packageQueue.isEmpty()) {
            return;
        }

        final ActivityPackage purchaseVerificationPackage = packageQueue.remove(0);
        int retries = purchaseVerificationPackage.getRetries();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                sendPurchaseVerificationPackageI(purchaseVerificationPackage);
                sendNextPurchaseVerificationPackage();
            }
        };

        if (retries <= 0) {
            runnable.run();
            return;
        }

        long waitTimeMilliSeconds = Util.getWaitingTime(retries, backoffStrategy);
        double waitTimeSeconds = waitTimeMilliSeconds / MILLISECONDS_TO_SECONDS_DIVISOR;
        String secondsString = Util.SecondsDisplayFormat.format(waitTimeSeconds);

        logger.verbose("Waiting for %s seconds before retrying purchase_verification for the %d time", secondsString, retries);
        scheduler.schedule(runnable, waitTimeMilliSeconds);
    }

    /**
     * Send purchase_verification package passed as the parameter (runs within scheduled executor).
     *
     * @param purchaseVerificationPackage purchase_verification package to be sent.
     */
    private void sendPurchaseVerificationPackageI(final ActivityPackage purchaseVerificationPackage) {
        IActivityHandler activityHandler = activityHandlerWeakRef.get();
        Map<String, String> sendingParameters = generateSendingParametersI();
        ResponseData responseData = activityPackageSender.sendActivityPackageSync(
                purchaseVerificationPackage,
                sendingParameters);

        if (!(responseData instanceof PurchaseVerificationResponseData)) {
            return;
        }

        PurchaseVerificationResponseData purchaseVerificationResponseData
                = (PurchaseVerificationResponseData)responseData;

        if (purchaseVerificationResponseData.willRetry) {
            retrySendingI(purchaseVerificationPackage);
            return;
        }
        if (activityHandler == null) {
            return;
        }
        if (purchaseVerificationResponseData.trackingState == TrackingState.OPTED_OUT) {
            activityHandler.gotOptOutResponse();
            return;
        }

        activityHandler.finishedTrackingActivity(purchaseVerificationResponseData);
    }
    private Map<String, String> generateSendingParametersI() {
        HashMap<String, String> sendingParameters = new HashMap<>();
        long now = System.currentTimeMillis();
        String dateString = Util.dateFormatter.format(now);
        PackageBuilder.addString(sendingParameters, "sent_at", dateString);
        int queueSize = packageQueue.size() - 1;
        if (queueSize > 0) {
            PackageBuilder.addLong(sendingParameters, "queue_size", queueSize);
        }
        return sendingParameters;
    }

    /**
     * Retry sending of the purchase_verification package passed as the parameter (runs within scheduled executor).
     *
     * @param purchaseVerificationPackage purchase_verification package to be retried.
     */
    private void retrySendingI(final ActivityPackage purchaseVerificationPackage) {
        int retries = purchaseVerificationPackage.increaseRetries();
        logger.error("Retrying purchase_verification package for the %d time", retries);
        sendPurchaseVerificationPackage(purchaseVerificationPackage);
    }
}
