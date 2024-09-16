//
//  PackageHandler.java
//  Adjust
//
//  Created by Christian Wellenbrock on 2013-06-25.
//  Copyright (c) 2013 adjust GmbH. All rights reserved.
//  See the file MIT-LICENSE for copying permission.
//

package com.adjust.sdk;

import android.content.Context;

import com.adjust.sdk.network.IActivityPackageSender;
import com.adjust.sdk.scheduler.SingleThreadCachedScheduler;
import com.adjust.sdk.scheduler.ThreadScheduler;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;


// persistent
public class PackageHandler implements IPackageHandler,
        IActivityPackageSender.ResponseDataCallbackSubscriber
{
    private static final String PACKAGE_QUEUE_FILENAME = "AdjustIoPackageQueue";
    private static final String PACKAGE_QUEUE_NAME = "Package queue";

    private ThreadScheduler scheduler;
    private IActivityPackageSender activityPackageSender;
    private WeakReference<IActivityHandler> activityHandlerWeakRef;
    private List<ActivityPackage> packageQueue;
    private AtomicBoolean isSending;
    private boolean paused;
    private Context context;
    private ILogger logger;
    private BackoffStrategy backoffStrategy;
    private BackoffStrategy backoffStrategyForInstallSession;
    private boolean isRetrying;
    private long retryStartedAtTimeMilliSeconds;
    private double totalWaitTimeSeconds;

    @Override
    public void teardown() {
        logger.verbose("PackageHandler teardown");
        if (scheduler != null) {
            scheduler.teardown();
        }
        if (activityHandlerWeakRef != null) {
            activityHandlerWeakRef.clear();
        }
        if (packageQueue != null) {
            packageQueue.clear();
        }
        scheduler = null;
        activityHandlerWeakRef = null;
        packageQueue = null;
        isSending = null;
        context = null;
        logger = null;
        backoffStrategy = null;
    }

    static void deleteState(Context context) {
        deletePackageQueue(context);
    }

    public PackageHandler(IActivityHandler activityHandler,
                          Context context,
                          boolean startsSending,
                          IActivityPackageSender packageHandlerActivityPackageSender)
    {
        this.scheduler = new SingleThreadCachedScheduler("PackageHandler");
        this.logger = AdjustFactory.getLogger();
        this.backoffStrategy = AdjustFactory.getPackageHandlerBackoffStrategy();
        this.backoffStrategyForInstallSession = AdjustFactory.getInstallSessionBackoffStrategy();
        this.isRetrying = false;
        this.totalWaitTimeSeconds = 0.0;

        init(activityHandler, context, startsSending, packageHandlerActivityPackageSender);

        scheduler.submit(new Runnable() {
            @Override
            public void run() {
                initI();
            }
        });
    }

    @Override
    public void init(IActivityHandler activityHandler,
                     Context context,
                     boolean startsSending,
                     IActivityPackageSender packageHandlerActivityPackageSender)
    {
        this.activityHandlerWeakRef = new WeakReference<IActivityHandler>(activityHandler);
        this.context = context;
        this.paused = !startsSending;
        this.activityPackageSender = packageHandlerActivityPackageSender;
    }

    // add a package to the queue
    @Override
    public void addPackage(final ActivityPackage activityPackage) {
        scheduler.submit(new Runnable() {
            @Override
            public void run() {
                addI(activityPackage);
            }
        });
    }

    // try to send the oldest package
    @Override
    public void sendFirstPackage() {
        scheduler.submit(new Runnable() {
            @Override
            public void run() {
                sendFirstI();
            }
        });
    }

    @Override
    public void onResponseDataCallback(final ResponseData responseData) {
        logger.debug("Got response in PackageHandler");
        IActivityHandler activityHandler = activityHandlerWeakRef.get();
        if (activityHandler != null &&
                responseData.trackingState == TrackingState.OPTED_OUT) {
            activityHandler.gotOptOutResponse();
        }

        if (!responseData.willRetry) {
            scheduler.submit(new Runnable() {
                @Override
                public void run() {
                    sendNextI(responseData.continueIn);
                }
            });

            if (activityHandler != null) {
                activityHandler.finishedTrackingActivity(responseData);
            }
            return;
        }

        if (!isRetrying) {
            isRetrying = true;
            retryStartedAtTimeMilliSeconds = System.currentTimeMillis();
        }

        writePackageQueueI();

        if (activityHandler != null) {
            activityHandler.finishedTrackingActivity(responseData);
        }

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                logger.verbose("Package handler can send");
                isSending.set(false);

                // Try to send the same package after sleeping
                sendFirstPackage();
            }
        };

        if (responseData.retryIn != null) {
            long retryIn = responseData.retryIn;
            scheduler.schedule(runnable, retryIn);
            return;
        }

        int retries = responseData.activityPackage.increaseRetries();
        long waitTimeMilliSeconds;

        SharedPreferencesManager sharedPreferencesManager = SharedPreferencesManager.getDefaultInstance(context);

        if (responseData.activityPackage.getActivityKind() ==
                ActivityKind.SESSION && !sharedPreferencesManager.getInstallTracked()) {
            waitTimeMilliSeconds = Util.getWaitingTime(retries, backoffStrategyForInstallSession);
        } else {
            waitTimeMilliSeconds = Util.getWaitingTime(retries, backoffStrategy);
        }

        double waitTimeSeconds = waitTimeMilliSeconds / 1000.0;
        String secondsString = Util.SecondsDisplayFormat.format(waitTimeSeconds);

        totalWaitTimeSeconds += waitTimeSeconds;

        logger.verbose("Waiting for %s seconds before retrying %s for the %d time",
                secondsString,
                responseData.activityPackage.getActivityKind().toString(),
                retries);
        scheduler.schedule(runnable, waitTimeMilliSeconds);
        responseData.activityPackage.setWaitBeforeSendTimeSeconds(responseData.activityPackage.getWaitBeforeSendTimeSeconds() + waitTimeSeconds);
    }

    // interrupt the sending loop after the current request has finished
    @Override
    public void pauseSending() {
        paused = true;
    }

    // allow sending requests again
    @Override
    public void resumeSending() {
        paused = false;
    }

    @Override
    public void flush() {
        scheduler.submit(new Runnable() {
            @Override
            public void run() {
                flushI();
            }
        });
    }

    // internal methods run in dedicated queue thread
    private void initI() {
        isSending = new AtomicBoolean();

        readPackageQueueI();
    }

    private void addI(ActivityPackage newPackage) {
        if (isRetrying) {
            long now = System.currentTimeMillis();
            double waitSeconds = totalWaitTimeSeconds - (now - retryStartedAtTimeMilliSeconds) / 1000.0;;
            newPackage.setWaitBeforeSendTimeSeconds(waitSeconds);
        }
        PackageBuilder.addLong(newPackage.getParameters(), "enqueue_size", packageQueue.size());

        packageQueue.add(newPackage);
        logger.debug("Added package %d (%s)", packageQueue.size(), newPackage);
        logger.verbose("%s", newPackage.getExtendedString());

        writePackageQueueI();
    }

    private void sendFirstI() {
        if (packageQueue.isEmpty()) {
            return;
        }

        if (paused) {
            logger.debug("Package handler is paused");
            return;
        }
        if (isSending.getAndSet(true)) {
            logger.verbose("Package handler is already sending");
            return;
        }

        Map<String, String> sendingParameters = generateSendingParametersI();

        ActivityPackage firstPackage = packageQueue.get(0);

        PackageBuilder.addLong(sendingParameters, "retry_count", firstPackage.getRetryCount());
        PackageBuilder.addLong(sendingParameters, "first_error", firstPackage.getFirstErrorCode());
        PackageBuilder.addLong(sendingParameters, "last_error", firstPackage.getLastErrorCode());
        PackageBuilder.addDouble(sendingParameters, "wait_total", totalWaitTimeSeconds);
        PackageBuilder.addDouble(sendingParameters, "wait_time", firstPackage.getWaitBeforeSendTimeSeconds());

        activityPackageSender.sendActivityPackage(firstPackage,
                sendingParameters,
                this);
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

    private void sendNextI(Long previousResponseContinueIn) {
        isRetrying = false;
        retryStartedAtTimeMilliSeconds = 0;

        if (packageQueue.isEmpty()) {
            // at this point, the queue has been emptied
            // reset total_wait in this moment to allow all requests to populate total_wait
            totalWaitTimeSeconds = 0.0;
            return;
        }

        packageQueue.remove(0);
        writePackageQueueI();

        if (previousResponseContinueIn != null && previousResponseContinueIn > 0) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    logger.verbose("Package handler finished waiting to continue");
                    isSending.set(false);
                    sendFirstPackage();
                }
            };

            logger.verbose("Waiting for %d seconds before continuing for next package in continue_in", previousResponseContinueIn / 1000.0);
            scheduler.schedule(runnable, previousResponseContinueIn);

        } else {
            logger.verbose("Package handler can send");
            isSending.set(false);
            sendFirstI();
        }
    }

    private void flushI() {
        packageQueue.clear();
        writePackageQueueI();
    }

    @SuppressWarnings("unchecked")
    private void readPackageQueueI() {
        try {
            packageQueue = Util.readObject(context,
                    PACKAGE_QUEUE_FILENAME,
                    PACKAGE_QUEUE_NAME,
                    (Class<List<ActivityPackage>>)(Class)List.class);
        } catch (Exception e) {
            logger.error("Failed to read %s file (%s)", PACKAGE_QUEUE_NAME, e.getMessage());
            packageQueue = null;
        }

        if (packageQueue != null) {
            logger.debug("Package handler read %d packages", packageQueue.size());
        } else {
            packageQueue = new ArrayList<ActivityPackage>();
        }
    }

    private void writePackageQueueI() {
        Util.writeObject(packageQueue, context, PACKAGE_QUEUE_FILENAME, PACKAGE_QUEUE_NAME);
        logger.debug("Package handler wrote %d packages", packageQueue.size());
    }

    public static Boolean deletePackageQueue(Context context) {
        return context.deleteFile(PACKAGE_QUEUE_FILENAME);
    }
}
