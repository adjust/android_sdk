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
public class QueueThread extends HandlerThread {
    private static final String QUEUE_FILENAME = "testqueue3";  // TODO: change filename

    private static final int MESSAGE_ARG_ADD = 72500; // TODO: change constants!
    private static final int MESSAGE_ARG_TRACK_NEXT = 72510;
    private static final int MESSAGE_ARG_TRACK_FIRST = 72530;
    private static final int MESSAGE_ARG_READ = 72520;

    private Handler queueHandler;
    private RequestThread requestThread;
    private Context context;
    private AtomicBoolean isTracking;
    private List<TrackingPackage> packages;
    private boolean paused;

    protected QueueThread(Context context) {
        super(Logger.LOGTAG, MIN_PRIORITY);
        setDaemon(true);
        start();
        this.queueHandler = new PackageHandler(getLooper(), this);

        this.context = context;
        this.isTracking = new AtomicBoolean();
        this.requestThread = new RequestThread(this);

        Message message = Message.obtain();
        message.arg1 = MESSAGE_ARG_READ;
        queueHandler.sendMessage(message);
    }

    // add a package to the queue, trigger tracking
    protected void addPackage(TrackingPackage pack) {
        Message message = Message.obtain();
        message.arg1 = MESSAGE_ARG_ADD;
        message.obj = pack;
        queueHandler.sendMessage(message);
    }

    // try to track the oldest package
    protected void trackFirstPackage() {
        Message message = Message.obtain();
        message.arg1 = MESSAGE_ARG_TRACK_FIRST;
        queueHandler.sendMessage(message);
    }

    // remove oldest package and try to track the next one
    // (after success or possibly permanent failure)
    protected void trackNextPackage() {
        Message message = Message.obtain();
        message.arg1 = MESSAGE_ARG_TRACK_NEXT;
        queueHandler.sendMessage(message);
    }

    // close the package to retry in the future (after temporary failure)
    protected void closeFirstPackage() {
        isTracking.set(false);
    }

    // interrupt the tracking loop after the current request has finished
    protected void pauseTracking() {
        paused = true;
    }

    // allow tracking requests again
    protected void resumeTracking() {
        paused = false;
    }

    private static final class PackageHandler extends Handler {
        private final WeakReference<QueueThread> queueThreadReference;

        protected PackageHandler(Looper looper, QueueThread queueThread) {
            super(looper);
            this.queueThreadReference = new WeakReference<QueueThread>(queueThread);
        }

        public void handleMessage(Message message) {
            super.handleMessage(message);

            QueueThread queueThread = queueThreadReference.get();
            if (queueThread == null) return;

            switch (message.arg1) {
            case MESSAGE_ARG_ADD:
                TrackingPackage trackingPackage = (TrackingPackage) message.obj;
                queueThread.addInternal(trackingPackage);
                break;
            case MESSAGE_ARG_TRACK_FIRST:
                queueThread.trackFirstInternal();
                break;
            case MESSAGE_ARG_TRACK_NEXT:
                queueThread.trackNextInternal();
                break;
            case MESSAGE_ARG_READ:
                queueThread.readPackagesInternal();
                break;
            }
        }
    }

    // internal methods run in dedicated queue thread

    private void addInternal(TrackingPackage newPackage) {
        packages.add(newPackage);
        Logger.debug("added package " + packages.size() + " (" + newPackage + ")");
        Logger.verbose(newPackage.parameterString());

        writePackagesInternal();
        trackFirstInternal();
    }

    private void trackFirstInternal() {
        if (paused) {
            Logger.debug("paused");
            return;
        }
        if (isTracking.getAndSet(true)) {
            Logger.debug("locked");
            return;
        }

        try {
            TrackingPackage firstPackage = packages.get(0);
            requestThread.trackPackage(firstPackage);
        }
        catch (IndexOutOfBoundsException e) {
            isTracking.set(false);
        }
    }

    private void trackNextInternal() {
        packages.remove(0);
        writePackagesInternal();
        isTracking.set(false);
        trackFirstInternal();
    }

    private void readPackagesInternal() {
        // initialize with empty list; if any exception gets raised
        // while reading the queue file this list will be used
        packages = new ArrayList<TrackingPackage>();

        try {
            FileInputStream inputStream = context.openFileInput(QUEUE_FILENAME);
            BufferedInputStream bufferedStream = new BufferedInputStream(inputStream);
            ObjectInputStream objectStream = new ObjectInputStream(bufferedStream);

            try {
                Object object = objectStream.readObject();
                @SuppressWarnings("unchecked")
                List<TrackingPackage> packages = (List<TrackingPackage>) object;
                Logger.debug("queue thread read " + packages.size() + " packages");
                this.packages = packages;
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

    private void writePackagesInternal() {
        try {   // TODO: remove sleeps
            Thread.sleep(100);
        } catch (Exception e) {}

        try {
            FileOutputStream outputStream = context.openFileOutput(QUEUE_FILENAME, Context.MODE_PRIVATE);
            BufferedOutputStream bufferedStream = new BufferedOutputStream(outputStream);
            ObjectOutputStream objectStream = new ObjectOutputStream(bufferedStream);

            try {
                objectStream.writeObject(packages);
                Logger.verbose("queue thread wrote " + packages.size() + " packages");
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
