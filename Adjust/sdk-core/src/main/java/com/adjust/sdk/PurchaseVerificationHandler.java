package com.adjust.sdk;

import com.adjust.sdk.network.IActivityPackageSender;
import com.adjust.sdk.scheduler.SingleThreadCachedScheduler;
import com.adjust.sdk.scheduler.ThreadScheduler;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

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
     * Flag to indicate if Purchase Verification package is being sent.
     */
    private boolean isSendingPurchaseVerificationPackage;

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

    private long lastPackageRetryInMilli= 0L;

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
        isSendingPurchaseVerificationPackage = false;
        lastPackageRetryInMilli = 0L;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void pauseSending() {
        paused = true;
        isSendingPurchaseVerificationPackage = false;
        lastPackageRetryInMilli = 0L;
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
                sendPurchaseVerificationPackageI(purchaseVerification);
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
        scheduler = null;
        isSendingPurchaseVerificationPackage = false;
        lastPackageRetryInMilli = 0L;
    }

    /**
     * Add purchase_verification package to the queue and send.
     */
    private void sendPurchaseVerificationPackageI(final ActivityPackage purchaseVerification) {
        packageQueue.add(purchaseVerification);
        logger.debug("Added purchase_verification %d", packageQueue.size());
        logger.verbose("%s", purchaseVerification.getExtendedString());
        sendNextPurchaseVerificationPackage();
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
        if (packageQueue.isEmpty()) {
            return;
        }
        if (activityHandler.getActivityState().isGdprForgotten) {
            logger.debug("purchase_verification request won't be sent for GDPR forgotten user");
            return;
        }
        if (paused) {
            logger.debug("PurchaseVerificationHandler is paused");
            return;
        }
        if (isSendingPurchaseVerificationPackage) {
            logger.debug("PurchaseVerificationHandler is is already sending a package");
            return;
        }

        long waitTimeMilliSeconds = waitTime();

        if (waitTimeMilliSeconds > 0) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    lastPackageRetryInMilli = 0L;
                    sendNextPurchaseVerificationPackage();
                }
            };
            scheduler.schedule(runnable, waitTimeMilliSeconds);
            return;
        }

        // get the package but keep it in the queue until processing is complete
        final ActivityPackage purchaseVerificationPackage = packageQueue.get(0);
        isSendingPurchaseVerificationPackage = true;
        sendPurchaseVerificationPackageSync(purchaseVerificationPackage);
    }

    /**
     * Send purchase_verification package passed as the parameter (runs within scheduled executor).
     *
     * @param purchaseVerificationPackage purchase_verification package to be sent.
     */
    private void sendPurchaseVerificationPackageSync(final ActivityPackage purchaseVerificationPackage) {
        IActivityHandler activityHandler = activityHandlerWeakRef.get();
        ResponseData responseData = activityPackageSender.sendActivityPackageSync(
                purchaseVerificationPackage,
                null);

        if (!(responseData instanceof PurchaseVerificationResponseData)) {
            return;
        }

        // reset flag to indicate we're done processing this package
        isSendingPurchaseVerificationPackage = false;

        PurchaseVerificationResponseData purchaseVerificationResponseData
                = (PurchaseVerificationResponseData)responseData;

        if (purchaseVerificationResponseData.jsonResponse == null) {
            logger.error("Could not get purchase_verification JSON response with message: %s",
                    purchaseVerificationResponseData.message);
        } else {

            if (activityHandler == null) {
                return;
            }
            // check if any package response contains information that user has opted out.
            // if yes, disable SDK and flush any potentially stored packages that happened afterwards.
            if (purchaseVerificationResponseData.trackingState == TrackingState.OPTED_OUT) {
                activityHandler.gotOptOutResponse();
                return;
            }

            // check if backend requested retry_in delay
            if (purchaseVerificationResponseData.willRetry) {
                if (responseData.retryIn != null && responseData.retryIn > 0) {
                    lastPackageRetryInMilli = responseData.retryIn;
                    logger.error("Retrying purchase_verification package with retry in %d ms",
                            lastPackageRetryInMilli);
                }

                // package stays in queue - schedule retry
                sendNextPurchaseVerificationPackage();
                return;
            }

            lastPackageRetryInMilli = 0L;
        }

        // processing is complete - remove the package from queue
        if (!packageQueue.isEmpty()) {
            packageQueue.remove(0);
        }

        // finish package tracking without retrying / backoff
        activityHandler.finishedTrackingActivity(purchaseVerificationResponseData);

        // process next package in queue if any
        sendNextPurchaseVerificationPackage();
    }

    private long waitTime() {
        // handle backend-requested retry_in delay
        if (lastPackageRetryInMilli > 0) {
            logger.verbose("Waiting for %d ms before retrying purchase_verification with retry_in",
                    lastPackageRetryInMilli);
            return  lastPackageRetryInMilli;
        }

        return 0L;
    }
}
