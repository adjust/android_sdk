package com.adeven.adjustio;

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
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

// TODO: user String.format everywhere instead of "a" + "b"

// persistent
public class PackageHandler extends HandlerThread {
    private static final String PACKAGE_QUEUE_FILENAME = "testqueue6";  // TODO: change filename

    private static final int MESSAGE_ARG_INIT = 72501; // TODO: change constants!
    private static final int MESSAGE_ARG_ADD = 72500;
    private static final int MESSAGE_ARG_SEND_NEXT = 72510;
    private static final int MESSAGE_ARG_SEND_FIRST = 72530;

    private InternalHandler internalHandler;
    private RequestHandler requestHandler;
    private List<ActivityPackage> packageQueue;
    private AtomicBoolean isSending;
    private boolean paused;
    private Context context;

    protected PackageHandler(Context context) {
        super(Logger.LOGTAG, MIN_PRIORITY);
        setDaemon(true);
        start();
        this.internalHandler = new InternalHandler(getLooper(), this);

        this.context = context;

        Message message = Message.obtain();
        message.arg1 = MESSAGE_ARG_INIT;
        internalHandler.sendMessage(message);
    }

    // add a package to the queue, trigger sending
    protected void addPackage(ActivityPackage pack) {
        Message message = Message.obtain();
        message.arg1 = MESSAGE_ARG_ADD;
        message.obj = pack;
        internalHandler.sendMessage(message);
    }

    // try to send the oldest package
    protected void sendFirstPackage() {
        Message message = Message.obtain();
        message.arg1 = MESSAGE_ARG_SEND_FIRST;
        internalHandler.sendMessage(message);
    }

    // remove oldest package and try to send the next one
    // (after success or possibly permanent failure)
    protected void sendNextPackage() {
        Message message = Message.obtain();
        message.arg1 = MESSAGE_ARG_SEND_NEXT;
        internalHandler.sendMessage(message);
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

    private static final class InternalHandler extends Handler {
        private final WeakReference<PackageHandler> packageHandlerReference;

        protected InternalHandler(Looper looper, PackageHandler packageHandler) {
            super(looper);
            this.packageHandlerReference = new WeakReference<PackageHandler>(packageHandler);
        }

        public void handleMessage(Message message) {
            super.handleMessage(message);

            PackageHandler packageHandler = packageHandlerReference.get();
            if (packageHandler == null) return;

            switch (message.arg1) {
            case MESSAGE_ARG_INIT:
                packageHandler.initInternal();
                break;
            case MESSAGE_ARG_ADD:
                ActivityPackage activityPackage = (ActivityPackage) message.obj;
                packageHandler.addInternal(activityPackage);
                break;
            case MESSAGE_ARG_SEND_FIRST:
                packageHandler.sendFirstInternal();
                break;
            case MESSAGE_ARG_SEND_NEXT:
                packageHandler.sendNextInternal();
                break;
            }
        }
    }

    // internal methods run in dedicated queue thread

    private void initInternal() {
        requestHandler = new RequestHandler(this);
        isSending = new AtomicBoolean();

        readPackageQueue();
    }

    private void addInternal(ActivityPackage newPackage) {
        packageQueue.add(newPackage);
        Logger.debug(String.format(Locale.US, "Added package %d (%s)",  packageQueue.size(), newPackage));
        Logger.verbose(newPackage.getParameterString());

        writePackageQueue();
        sendFirstInternal();
    }

    private void sendFirstInternal() {
        if (packageQueue.size() == 0) return;

        if (paused) {
            Logger.debug("Package handler is paused");
            return;
        }
        if (isSending.getAndSet(true)) {
            Logger.debug("Package handler is already sending");
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
            }
            catch (ClassNotFoundException e) {
                Logger.error("Failed to find package queue class");
            }
            catch (OptionalDataException e) {} catch (IOException e) {
                Logger.error("Failed to read package queue object");
            }
            catch (ClassCastException e) {
                Logger.error("Failed to cast package queue object");
            }
            finally {
                objectStream.close();
            }
        }
        catch (FileNotFoundException e) {
            Logger.verbose("Package queue file not found");
        }
        catch (IOException e) {
            Logger.error("Failed to read package queue file");
        }

        // start with a fresh package queue in case of any exception
        packageQueue = new ArrayList<ActivityPackage>();
    }

    private void writePackageQueue() {
        try {   // TODO: remove sleeps
            Thread.sleep(100);
        } catch (Exception e) {}

        try {
            FileOutputStream outputStream = context.openFileOutput(PACKAGE_QUEUE_FILENAME, Context.MODE_PRIVATE);
            BufferedOutputStream bufferedStream = new BufferedOutputStream(outputStream);
            ObjectOutputStream objectStream = new ObjectOutputStream(bufferedStream);

            try {
                objectStream.writeObject(packageQueue);
                Logger.verbose(String.format(Locale.US, "Package handler wrote %d packages", packageQueue.size()));
            }
            catch (NotSerializableException e) {
                Logger.error("Failed to serialize packages");
            }
            finally {
                objectStream.close();
            }
        }
        catch (IOException e) {
            Logger.error(String.format("Failed to write packages (%s)", e.getLocalizedMessage()));
            e.printStackTrace();
        }
    }
}
