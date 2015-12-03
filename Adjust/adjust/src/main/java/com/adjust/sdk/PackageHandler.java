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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

// persistent
public class PackageHandler extends HandlerThread implements IPackageHandler {
    private static final String PACKAGE_QUEUE_FILENAME = "AdjustIoPackageQueue";
    private static final String PACKAGE_QUEUE_NAME = "Package queue";

    private final InternalHandler internalHandler;
    private IRequestHandler requestHandler;
    private IActivityHandler activityHandler;
    private List<ActivityPackage> packageQueue;
    private AtomicBoolean isSending;
    private boolean paused;
    private Context context;
    private ILogger logger;

    public PackageHandler(IActivityHandler activityHandler,
                          Context context,
                          boolean startPaused) {
        super(Constants.LOGTAG, MIN_PRIORITY);
        setDaemon(true);
        start();
        this.internalHandler = new InternalHandler(getLooper(), this);
        this.logger = AdjustFactory.getLogger();

        init(activityHandler, context, startPaused);

        Message message = Message.obtain();
        message.arg1 = InternalHandler.INIT;
        internalHandler.sendMessage(message);
    }

    @Override
    public void init(IActivityHandler activityHandler, Context context, boolean startPaused) {
        this.activityHandler = activityHandler;
        this.context = context;
        this.paused = startPaused;
    }

    // add a package to the queue
    @Override
    public void addPackage(ActivityPackage pack) {
        Message message = Message.obtain();
        message.arg1 = InternalHandler.ADD;
        message.obj = pack;
        internalHandler.sendMessage(message);
    }

    // try to send the oldest package
    @Override
    public void sendFirstPackage() {
        Message message = Message.obtain();
        message.arg1 = InternalHandler.SEND_FIRST;
        internalHandler.sendMessage(message);
    }

    // remove oldest package and try to send the next one
    // (after success or possibly permanent failure)
    @Override
    public void sendNextPackage(ResponseData responseData) {
        Message message = Message.obtain();
        message.arg1 = InternalHandler.SEND_NEXT;
        internalHandler.sendMessage(message);

        responseData.willRetry = false;
        activityHandler.finishedTrackingActivity(responseData);
    }

    // close the package to retry in the future (after temporary failure)
    @Override
    public void closeFirstPackage(ResponseData responseData) {
        isSending.set(false);

        responseData.willRetry = true;
        activityHandler.finishedTrackingActivity(responseData);
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

    private static final class InternalHandler extends Handler {
        private static final int INIT = 1;
        private static final int ADD = 2;
        private static final int SEND_NEXT = 3;
        private static final int SEND_FIRST = 4;

        private final WeakReference<PackageHandler> packageHandlerReference;

        protected InternalHandler(Looper looper, PackageHandler packageHandler) {
            super(looper);
            this.packageHandlerReference = new WeakReference<PackageHandler>(packageHandler);
        }

        @Override
        public void handleMessage(Message message) {
            super.handleMessage(message);

            PackageHandler packageHandler = packageHandlerReference.get();
            if (packageHandler == null) {
                return;
            }

            switch (message.arg1) {
                case INIT:
                    packageHandler.initInternal();
                    break;
                case ADD:
                    ActivityPackage activityPackage = (ActivityPackage) message.obj;
                    packageHandler.addInternal(activityPackage);
                    break;
                case SEND_FIRST:
                    packageHandler.sendFirstInternal();
                    break;
                case SEND_NEXT:
                    packageHandler.sendNextInternal();
                    break;
            }
        }
    }

    // internal methods run in dedicated queue thread

    private void initInternal() {
        requestHandler = AdjustFactory.getRequestHandler(this);

        isSending = new AtomicBoolean();

        readPackageQueue();
    }

    private void addInternal(ActivityPackage newPackage) {
        if (newPackage.getActivityKind().equals(ActivityKind.CLICK) && !packageQueue.isEmpty()) {
            packageQueue.add(1, newPackage);
        } else {
            packageQueue.add(newPackage);
        }
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
        requestHandler.sendPackage(firstPackage);
    }

    private void sendNextInternal() {
        packageQueue.remove(0);
        writePackageQueue();
        isSending.set(false);
        sendFirstInternal();
    }

    private void readPackageQueue() {
        try {
            packageQueue = Util.readObject(context, PACKAGE_QUEUE_FILENAME, PACKAGE_QUEUE_NAME, List.class);
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
