// TODO: add comments
package com.adeven.adjustio;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
    private Handler queueHandler;
    private Context context;
    private AtomicBoolean isTracking;
    private RequestThread requestThread;
    private List<TrackingPackage> packages;

    private static final String QUEUE_FILENAME = "testqueue";
    private static final int MESSAGE_ARG_ADD = 72500;   // TODO: change numbers?
    private static final int MESSAGE_ARG_REMOVE = 72510;
    private static final int MESSAGE_ARG_READ = 72520;
    private static final int MESSAGE_ARG_TRACK = 72530;

    // tracking loop:
    // - q.addPackage
    // - q.addInternal // or q.tryTrack
    // - q.trackInternal (lock) // exception (unlock)
    // - r.trackPackage
    // - r.requestFinished
    // - q.trackNext // or q.rejectFirst (unlock)
    // - q.removeInternal (unlock)
    // - q.trackInternal (repeat)

    // TODO: on session end: stop current tracking loop
    // add an attribute that gets read before trackInternal starts

    public QueueThread(Context context) {
        super(Logger.LOGTAG, MIN_PRIORITY);
        setDaemon(true);
        start();
        this.queueHandler = new PackageHandler(getLooper(), this);

        this.context = context;
        this.isTracking = new AtomicBoolean();
        this.requestThread = new RequestThread(this);

        readPackages();
    }

    public void addPackage(TrackingPackage pack) {
        Message message = Message.obtain();
        message.arg1 = MESSAGE_ARG_ADD;
        message.obj = pack;
        queueHandler.sendMessage(message);
    }

    public void tryTrackFirstPackage() {
        Message message = Message.obtain();
        message.arg1 = MESSAGE_ARG_TRACK;
        queueHandler.sendMessage(message);
    }

    public void trackNextPackage() {
        Message message = Message.obtain();
        message.arg1 = MESSAGE_ARG_REMOVE;
        queueHandler.sendMessage(message);
    }

    public void rejectFirstPackage() {
        isTracking.set(false);
    }

    private void readPackages() {
        Message message = Message.obtain();
        message.arg1 = MESSAGE_ARG_READ;
        queueHandler.sendMessage(message);
    }

    private static final class PackageHandler extends Handler {
        private final WeakReference<QueueThread> queueThreadReference;

        public PackageHandler(Looper looper, QueueThread queueThread) {
            super(looper);
            this.queueThreadReference = new WeakReference<QueueThread>(queueThread);
        }

        @Override
        public void handleMessage(Message message) {
            super.handleMessage(message);

            QueueThread queueThread = queueThreadReference.get();
            if (queueThread == null) {
                return;
            } else if (message.arg1 == MESSAGE_ARG_ADD) {
                queueThread.addInternal((TrackingPackage) message.obj);
            } else if (message.arg1 == MESSAGE_ARG_REMOVE) {
                queueThread.removeInternal();
            } else if (message.arg1 == MESSAGE_ARG_READ) {
                queueThread.readInternal();
            } else if (message.arg1 == MESSAGE_ARG_TRACK) {
                queueThread.trackInternal();
            }
        }
    }

    // internal methods run in own thread

    // TODO: lock needed? test with sleep
    private void addInternal(TrackingPackage pack) {
        packages.add(pack);
        writeInternal();

        trackInternal();
    }

    // TODO: check package?
    private void removeInternal() {
        packages.remove(0);
        writeInternal();
        isTracking.set(false);

        trackInternal();
    }

    // TODO: add lock
    private void trackInternal() {
        if (isTracking.getAndSet(true)) {
            Logger.error("locked");
            return;
        }
        try {
            TrackingPackage firstPackage = packages.get(0);
            requestThread.trackPackage(firstPackage);
        } catch (IndexOutOfBoundsException e) {
            isTracking.set(false);
        }
    }

    @SuppressWarnings("unchecked")
    private void readInternal() {
        try {
            FileInputStream inputStream = context.openFileInput(QUEUE_FILENAME);
            BufferedInputStream bufferedStream = new BufferedInputStream(inputStream);
            ObjectInputStream objectStream = new ObjectInputStream(bufferedStream);
            try {
                packages = (List<TrackingPackage>)objectStream.readObject();
                Logger.error("packages " + packages.size());
            } finally {
                objectStream.close();
            }
        } catch (FileNotFoundException e) {
            packages = new ArrayList<TrackingPackage>();
        } catch (ClassNotFoundException e) {
            Logger.error("class not found");
        } catch (IOException e) {
            Logger.error("failed to read object");
        }
    }

    private void writeInternal() {
        try { Thread.sleep(100); } catch (Exception e) {}

        try {
            FileOutputStream outputStream = context.openFileOutput(QUEUE_FILENAME, Context.MODE_PRIVATE);
            BufferedOutputStream bufferedStream = new BufferedOutputStream(outputStream);
            ObjectOutputStream objectStream = new ObjectOutputStream(bufferedStream);
            try {
                objectStream.writeObject(packages);
            } finally {
                objectStream.close();
            }
        } catch (IOException e) {
            Logger.error("failed to write package"); // TODO: improve log
        }
    }
}
