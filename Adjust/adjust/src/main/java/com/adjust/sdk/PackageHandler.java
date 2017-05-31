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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.adjust.sdk.Constants.CALLBACK_PARAMETERS;
import static com.adjust.sdk.Constants.PARTNER_PARAMETERS;

// persistent
public class PackageHandler implements IPackageHandler {
    private static final String PACKAGE_QUEUE_FILENAME = "AdjustIoPackageQueue";
    private static final String PACKAGE_QUEUE_NAME = "Package queue";

    private CustomScheduledExecutor scheduledExecutor;
    private IRequestHandler requestHandler;
    private WeakReference<IActivityHandler> activityHandlerWeakRef;
    private List<ActivityPackage> packageQueue;
    private AtomicBoolean isSending;
    private boolean paused;
    private Context context;
    private ILogger logger;
    private BackoffStrategy backoffStrategy;
    private String basePath;

    @Override
    public void teardown(boolean deleteState) {
        logger.verbose("PackageHandler teardown");
        if (scheduledExecutor != null) {
            try {
                scheduledExecutor.shutdownNow();
            } catch(SecurityException se) {}
        }
        if (activityHandlerWeakRef != null) {
            activityHandlerWeakRef.clear();
        }
        if (requestHandler != null) {
            requestHandler.teardown();
        }
        if (packageQueue != null) {
            packageQueue.clear();
        }
        if (deleteState && context != null) {
            deletePackageQueue(context);
        }
        scheduledExecutor = null;
        requestHandler = null;
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
                          boolean startsSending) {
        this.scheduledExecutor = new CustomScheduledExecutor("PackageHandler", false);
        this.logger = AdjustFactory.getLogger();
        this.backoffStrategy = AdjustFactory.getPackageHandlerBackoffStrategy();

        init(activityHandler, context, startsSending);

        scheduledExecutor.submit(new Runnable() {
            @Override
            public void run() {
                initI();
            }
        });
    }

    @Override
    public void init(IActivityHandler activityHandler, Context context, boolean startsSending) {
        this.activityHandlerWeakRef = new WeakReference<IActivityHandler>(activityHandler);
        this.context = context;
        this.paused = !startsSending;
        this.basePath = activityHandler.getBasePath();
    }

    // add a package to the queue
    @Override
    public void addPackage(final ActivityPackage activityPackage) {
        scheduledExecutor.submit(new Runnable() {
            @Override
            public void run() {
                addI(activityPackage);
            }
        });
    }

    // try to send the oldest package
    @Override
    public void sendFirstPackage() {
        scheduledExecutor.submit(new Runnable() {
            @Override
            public void run() {
                sendFirstI();
            }
        });
    }

    // remove oldest package and try to send the next one
    // (after success or possibly permanent failure)
    @Override
    public void sendNextPackage(ResponseData responseData) {
        scheduledExecutor.submit(new Runnable() {
            @Override
            public void run() {
                sendNextI();
            }
        });

        IActivityHandler activityHandler = activityHandlerWeakRef.get();
        if (activityHandler != null) {
            activityHandler.finishedTrackingActivity(responseData);
        }
    }

    // close the package to retry in the future (after temporary failure)
    @Override
    public void closeFirstPackage(ResponseData responseData, ActivityPackage activityPackage) {
        responseData.willRetry = true;

        IActivityHandler activityHandler = activityHandlerWeakRef.get();
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

        if (activityPackage == null) {
            runnable.run();
            return;
        }

        int retries = activityPackage.increaseRetries();

        long waitTimeMilliSeconds = Util.getWaitingTime(retries, backoffStrategy);

        double waitTimeSeconds = waitTimeMilliSeconds / 1000.0;
        String secondsString = Util.SecondsDisplayFormat.format(waitTimeSeconds);

        logger.verbose("Waiting for %s seconds before retrying the %d time", secondsString, retries);
        scheduledExecutor.schedule(runnable, waitTimeMilliSeconds, TimeUnit.MILLISECONDS);
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
    public void updatePackages(SessionParameters sessionParameters) {
        final SessionParameters sessionParametersCopy;
        if (sessionParameters != null) {
            sessionParametersCopy = sessionParameters.deepCopy();
        } else {
            sessionParametersCopy = null;
        }
        scheduledExecutor.submit(new Runnable() {
            @Override
            public void run() {
                updatePackagesI(sessionParametersCopy);
            }
        });
    }

    @Override
    public String getBasePath() {
        return this.basePath;
    }

    // internal methods run in dedicated queue thread

    private void initI() {
        requestHandler = AdjustFactory.getRequestHandler(this);

        isSending = new AtomicBoolean();

        readPackageQueueI();
    }

    private void addI(ActivityPackage newPackage) {
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

        ActivityPackage firstPackage = packageQueue.get(0);
        requestHandler.sendPackage(firstPackage, packageQueue.size() - 1);
    }

    private void sendNextI() {
        packageQueue.remove(0);
        writePackageQueueI();
        isSending.set(false);
        logger.verbose("Package handler can send");
        sendFirstI();
    }

    public void updatePackagesI(SessionParameters sessionParameters) {
        if (sessionParameters == null) {
            return;
        }

        logger.debug("Updating package handler queue");
        logger.verbose("Session callback parameters: %s", sessionParameters.callbackParameters);
        logger.verbose("Session partner parameters: %s", sessionParameters.partnerParameters);

        for (ActivityPackage activityPackage : packageQueue) {
            Map<String, String> parameters = activityPackage.getParameters();
            // callback parameters
            Map<String, String> mergedCallbackParameters = Util.mergeParameters(sessionParameters.callbackParameters,
                    activityPackage.getCallbackParameters(),
                    "Callback");

            PackageBuilder.addMapJson(parameters, CALLBACK_PARAMETERS, mergedCallbackParameters);
            // partner parameters
            Map<String, String> mergedPartnerParameters = Util.mergeParameters(sessionParameters.partnerParameters,
                    activityPackage.getPartnerParameters(),
                    "Partner");

            PackageBuilder.addMapJson(parameters, PARTNER_PARAMETERS, mergedPartnerParameters);
        }

        writePackageQueueI();
    }

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
