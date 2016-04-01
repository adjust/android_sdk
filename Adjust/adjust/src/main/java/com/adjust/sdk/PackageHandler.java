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
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

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
                initInternal();
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
                addInternal(activityPackage);
            }
        });
    }

    // try to send the oldest package
    @Override
    public void sendFirstPackage() {
        internalHandler.post(new Runnable() {
            @Override
            public void run() {
                sendFirstInternal();
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
                sendNextInternal();
            }
        });

        activityHandler.finishedTrackingActivity(responseData);
    }

    // close the package to retry in the future (after temporary failure)
    @Override
    public void closeFirstPackage(ResponseData responseData, ActivityPackage activityPackage) {
        logger.verbose("Package handler can send");

        responseData.willRetry = true;
        activityHandler.finishedTrackingActivity(responseData);

        if (activityPackage != null) {
            int retries = activityPackage.increaseRetries();

            long waitTime = Util.getWaitingTime(retries, backoffStrategy);

            DecimalFormat df = new DecimalFormat("#.#");
            double waitTimeSeconds = waitTime / 1000.0;

            logger.verbose("Sleeping for %s seconds before retrying the %d time", df.format(waitTimeSeconds), retries);
            SystemClock.sleep(waitTime);
        }

        isSending.set(false);
        sendFirstPackage();
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

    // internal methods run in dedicated queue thread

    private void initInternal() {
        requestHandler = AdjustFactory.getRequestHandler(this);

        isSending = new AtomicBoolean();

        readPackageQueue();
    }

    private void addInternal(ActivityPackage newPackage) {
        packageQueue.add(newPackage);
        logger.debug("Added package %d (%s)", packageQueue.size(), newPackage);
        logger.verbose("%s", newPackage.getExtendedString());

        writePackageQueue();
    }

    private void sendFirstInternal() {
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

    private void sendNextInternal() {
        packageQueue.remove(0);
        writePackageQueue();
        isSending.set(false);
        logger.verbose("Package handler can send");
        sendFirstInternal();
    }

    private void readPackageQueue() {
        try {
            packageQueue = Util.readObject(context, PACKAGE_QUEUE_FILENAME, PACKAGE_QUEUE_NAME, (Class<List<ActivityPackage>>)((Class)List.class));
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

    private void writePackageQueue() {
        Util.writeObject(packageQueue, context, PACKAGE_QUEUE_FILENAME, PACKAGE_QUEUE_NAME);
        logger.debug("Package handler wrote %d packages", packageQueue.size());
    }

    public static Boolean deletePackageQueue(Context context) {
        return context.deleteFile(PACKAGE_QUEUE_FILENAME);
    }
}
