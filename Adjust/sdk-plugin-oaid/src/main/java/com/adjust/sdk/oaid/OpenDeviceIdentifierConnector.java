package com.adjust.sdk.oaid;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class OpenDeviceIdentifierConnector implements ServiceConnection, IBinder.DeathRecipient {

    private static volatile OpenDeviceIdentifierConnector instance = null;
    private static final Object lockObject = new Object();
    private BlockingQueue<IBinder> binders = null;
    private Context context;

    private OpenDeviceIdentifierConnector(Context context) {
        binders = new LinkedBlockingDeque();
        this.context = context;
    }

    public static OpenDeviceIdentifierConnector getInstance(Context context) {
        if (instance == null) {
            synchronized (OpenDeviceIdentifierConnector.class) {
                if (instance == null) {
                    instance = new OpenDeviceIdentifierConnector(context);
                }
            }
        }
        return instance;
    }

    public boolean isServiceConnected() {
        return !binders.isEmpty();
    }

    public OpenDeviceIdentifierService getOpenDeviceIdentifierService(long timeOut, TimeUnit timeUnit)
            throws InterruptedException, TimeoutException {

        // poll in order to wait & retrieve the service
        IBinder service = binders.poll(timeOut, timeUnit);
        if (service == null) {
            throw new TimeoutException("Timed out waiting for the service connection");
        }

        // set back for next poll
        set(service);

        return OpenDeviceIdentifierService.Stub.asInterface(service);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        //lets use the latest instance
        set(service);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        reset();
    }

    @Override
    public void onBindingDied(ComponentName name) {
        unbindAndReset();
    }

    @Override
    public void onNullBinding(ComponentName name) {
        unbindAndReset();
    }

    @Override
    public void binderDied() {
        unbindAndReset();
    }

    public void unbindAndReset() {
        context.unbindService(this);
        reset();
    }

    private void reset() {
        synchronized (lockObject) {
            binders.clear();
        }
    }

    private void set(IBinder service) {
        synchronized (lockObject) {
            binders.clear();
            binders.add(service);
        }
    }
}

