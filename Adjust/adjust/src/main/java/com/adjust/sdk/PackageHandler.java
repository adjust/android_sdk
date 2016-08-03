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
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;

import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.adjust.sdk.Constants.CALLBACK_PARAMETERS;
import static com.adjust.sdk.Constants.EXTERNAL_DEVICE_ID_PARAMETER;
import static com.adjust.sdk.Constants.PARTNER_PARAMETERS;

// persistent
public class PackageHandler extends HandlerThread implements IPackageHandler {
    private static final String PACKAGE_QUEUE_FILENAME = "AdjustIoPackageQueue";
    private static final String PACKAGE_QUEUE_NAME = "Package queue";

    private Handler internalHandler;
    private IRequestHandler requestHandler;
    private IActivityHandler activityHandler;
    private List<ActivityPackage> packageQueue;
    private AtomicBoolean isSending;
    private boolean paused;
    private Context context;
    private ILogger logger;
    private BackoffStrategy backoffStrategy;

    public PackageHandler(IActivityHandler activityHandler,
                          Context context,
                          boolean startsSending) {
        super(Constants.LOGTAG, MIN_PRIORITY);
        setDaemon(true);
        start();
        this.internalHandler = new Handler(getLooper());
        this.logger = AdjustFactory.getLogger();
        this.backoffStrategy = AdjustFactory.getPackageHandlerBackoffStrategy();

        init(activityHandler, context, startsSending);

        internalHandler.post(new Runnable() {
            @Override
            public void run() {
                initI();
            }
        });
    }

    @Override
    public void init(IActivityHandler activityHandler, Context context, boolean startsSending) {
        this.activityHandler = activityHandler;
        this.context = context;
        this.paused = !startsSending;
    }

    // add a package to the queue
    @Override
    public void addPackage(final ActivityPackage activityPackage) {
        internalHandler.post(new Runnable() {
            @Override
            public void run() {
                addI(activityPackage);
            }
        });
    }

    // try to send the oldest package
    @Override
    public void sendFirstPackage() {
        internalHandler.post(new Runnable() {
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
        internalHandler.post(new Runnable() {
            @Override
            public void run() {
                sendNextI();
            }
        });

        activityHandler.finishedTrackingActivity(responseData);
    }

    // close the package to retry in the future (after temporary failure)
    @Override
    public void closeFirstPackage(ResponseData responseData, ActivityPackage activityPackage) {
        responseData.willRetry = true;
        activityHandler.finishedTrackingActivity(responseData);

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
        internalHandler.postDelayed(runnable, waitTimeMilliSeconds);
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
        internalHandler.post(new Runnable() {
            @Override
            public void run() {
                updatePackagesI(sessionParametersCopy);
            }
        });
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
        logger.verbose("Session external device id: %s", sessionParameters.externalDeviceId);
        logger.verbose("Session callback parameters: %s", sessionParameters.callbackParameters);
        logger.verbose("Session partner parameters: %s", sessionParameters.partnerParameters);

        for (ActivityPackage activityPackage : packageQueue) {
            Map<String, String> parameters = activityPackage.getParameters();
            // external device id
            PackageBuilder.addString(parameters,
                    EXTERNAL_DEVICE_ID_PARAMETER,
                    sessionParameters.externalDeviceId);

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
