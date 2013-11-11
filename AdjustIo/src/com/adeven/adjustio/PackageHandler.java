//
//  PackageHandler.java
//  AdjustIo
//
//  Created by Christian Wellenbrock on 2013-06-25.
//  Copyright (c) 2013 adeven. All rights reserved.
//  See the file MIT-LICENSE for copying permission.
//

package com.adeven.adjustio;

import android.content.Context;
import android.os.HandlerThread;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

// persistent
public class PackageHandler extends HandlerThread {
    private static final String PACKAGE_QUEUE_FILENAME = "AdjustIoPackageQueue";

    private       RequestHandler        requestHandler;
    private       List<ActivityPackage> packageQueue;
    private       AtomicBoolean         isSending;
    private       boolean               paused;
    private final Context               context;

    protected PackageHandler(Context context) {
        super(Logger.LOGTAG, MIN_PRIORITY);
        setDaemon(true);
        start();
        this.context = context;
        initInternal();
    }

    // add a package to the queue, trigger sending
    protected void addPackage(ActivityPackage pack) {
        addInternal(pack);
    }

    // try to send the oldest package
    protected void sendFirstPackage() {
        sendFirstInternal();
    }

    // remove oldest package and try to send the next one
    // (after success or possibly permanent failure)
    protected void sendNextPackage() {
        sendNextInternal();
    }

    // close the package to retry in the future (after temporary failure)
    protected void closeFirstPackage() {
        isSending.set(false);
    }

    // interrupt the sending loop after the current request has finished
    protected void pauseSending() {
        paused = true;
    }

    // allow sending requests again
    protected void resumeSending() {
        paused = false;
    }

    // internal methods run in dedicated queue thread

    private void initInternal() {
        requestHandler = new RequestHandler(this);
        isSending = new AtomicBoolean();

        readPackageQueue();
    }

    private void addInternal(ActivityPackage newPackage) {
        packageQueue.add(newPackage);
        Logger.debug(String.format(Locale.US, "Added package %d (%s)", packageQueue.size(), newPackage));
        Logger.verbose(newPackage.getExtendedString());

        writePackageQueue();
    }

    private void sendFirstInternal() {
        if (packageQueue.isEmpty()) {
            return;
        }

        if (paused) {
            Logger.debug("Package handler is paused");
            return;
        }
        if (isSending.getAndSet(true)) {
            Logger.verbose("Package handler is already sending");
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
            FileInputStream inputStream = context.openFileInput(PACKAGE_QUEUE_FILENAME);
            BufferedInputStream bufferedStream = new BufferedInputStream(inputStream);
            ObjectInputStream objectStream = new ObjectInputStream(bufferedStream);

            try {
                Object object = objectStream.readObject();
                @SuppressWarnings("unchecked")
                List<ActivityPackage> packageQueue = (List<ActivityPackage>) object;
                Logger.debug(String.format(Locale.US, "Package handler read %d packages", packageQueue.size()));
                this.packageQueue = packageQueue;
                return;
            } catch (ClassNotFoundException e) {
                Logger.error("Failed to find package queue class");
            } catch (OptionalDataException e) {
                /* no-op */
            } catch (IOException e) {
                Logger.error("Failed to read package queue object");
            } catch (ClassCastException e) {
                Logger.error("Failed to cast package queue object");
            } finally {
                objectStream.close();
            }
        } catch (FileNotFoundException e) {
            Logger.verbose("Package queue file not found");
        } catch (IOException e) {
            Logger.error("Failed to read package queue file");
        }

        // start with a fresh package queue in case of any exception
        packageQueue = new ArrayList<ActivityPackage>();
    }

    private void writePackageQueue() {
        try {
            FileOutputStream outputStream = context.openFileOutput(PACKAGE_QUEUE_FILENAME, Context.MODE_PRIVATE);
            BufferedOutputStream bufferedStream = new BufferedOutputStream(outputStream);
            ObjectOutputStream objectStream = new ObjectOutputStream(bufferedStream);

            try {
                objectStream.writeObject(packageQueue);
                Logger.debug(String.format(Locale.US, "Package handler wrote %d packages", packageQueue.size()));
            } catch (NotSerializableException e) {
                Logger.error("Failed to serialize packages");
            } finally {
                objectStream.close();
            }
        } catch (IOException e) {
            Logger.error(String.format("Failed to write packages (%s)", e.getLocalizedMessage()));
            e.printStackTrace();
        }
    }
}
