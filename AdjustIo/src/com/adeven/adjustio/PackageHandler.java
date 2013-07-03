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
import java.util.concurrent.atomic.AtomicBoolean;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

// persistent
public class PackageHandler extends HandlerThread {
    private static final String PACKAGE_QUEUE_FILENAME = "testqueue3";  // TODO: change filename

    private static final int MESSAGE_ARG_ADD = 72500; // TODO: change constants!
    private static final int MESSAGE_ARG_SEND_NEXT = 72510;
    private static final int MESSAGE_ARG_SEND_FIRST = 72530;
    private static final int MESSAGE_ARG_READ = 72520;

    private InternalHandler internalHandler;
    private RequestHandler requestHandler;
    private Context context;
    private AtomicBoolean isSending;
    private List<ActivityPackage> packageQueue;
    private boolean paused;

    protected PackageHandler(Context context) {
        super(Logger.LOGTAG, MIN_PRIORITY);
        setDaemon(true);
        start();
        this.internalHandler = new InternalHandler(getLooper(), this);

        this.context = context;
        this.isSending = new AtomicBoolean();
        this.requestHandler = new RequestHandler(this);

        Message message = Message.obtain();
        message.arg1 = MESSAGE_ARG_READ;
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
            case MESSAGE_ARG_READ:
                packageHandler.readPackageQueue();
                break;
            }
        }
    }

    // internal methods run in dedicated queue thread

    private void addInternal(ActivityPackage newPackage) {
        packageQueue.add(newPackage);
        Logger.debug("added package " + packageQueue.size() + " (" + newPackage + ")");
        Logger.verbose(newPackage.getParameterString());

        writePackageQueue();
        sendFirstInternal();
    }

    private void sendFirstInternal() {
        if (paused) {
            Logger.debug("paused");
            return;
        }
        if (isSending.getAndSet(true)) {
            Logger.debug("locked");
            return;
        }

        try {
            ActivityPackage firstPackage = packageQueue.get(0);
            requestHandler.sendPackage(firstPackage);
        }
        catch (IndexOutOfBoundsException e) {
            isSending.set(false);
        }
    }

    private void sendNextInternal() {
        packageQueue.remove(0);
        writePackageQueue();
        isSending.set(false);
        sendFirstInternal();
    }

    private void readPackageQueue() {
        // initialize with empty list; if any exception gets raised
        // while reading the queue file this list will be used
        packageQueue = new ArrayList<ActivityPackage>();

        try {
            FileInputStream inputStream = context.openFileInput(PACKAGE_QUEUE_FILENAME);
            BufferedInputStream bufferedStream = new BufferedInputStream(inputStream);
            ObjectInputStream objectStream = new ObjectInputStream(bufferedStream);

            try {
                Object object = objectStream.readObject();
                @SuppressWarnings("unchecked")
                List<ActivityPackage> packageQueue = (List<ActivityPackage>) object;
                Logger.debug("queue handler read " + packageQueue.size() + " packages");
                this.packageQueue = packageQueue;
            }
            catch (ClassNotFoundException e) {
                Logger.error("failed to find queue class");
            }
            catch (OptionalDataException e) {} catch (IOException e) {
                Logger.error("failed to read queue object");
            }
            catch (ClassCastException e) {
                Logger.error("failed to cast queue object");
            }
            finally {
                objectStream.close();
            }
        }
        catch (FileNotFoundException e) {
            Logger.verbose("queue file not found");
        }
        catch (IOException e) {
            Logger.error("failed to read queue file");
        }
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
                Logger.verbose("queue handler wrote " + packageQueue.size() + " packages");
            }
            catch (NotSerializableException e) {
                Logger.error("failed to serialize packages");
            }
            finally {
                objectStream.close();
            }
        }
        catch (IOException e) {
            Logger.error("failed to write packages (" + e.getLocalizedMessage() + ")");
            e.printStackTrace();
        }
    }
}
