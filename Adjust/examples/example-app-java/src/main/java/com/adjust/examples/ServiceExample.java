package com.adjust.examples;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import com.adjust.sdk.Adjust;
import com.adjust.sdk.AdjustEvent;

/**
 * Created by pfms on 16/03/16.
 */
public class ServiceExample extends Service {
    private static final String EVENT_TOKEN_BACKGROUND = "pkd28h";

    private static boolean flip = true;

    public ServiceExample() {
        super();
        Log.d("example", "ServiceExample constructor");
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("example", "ServiceExample onBind");

        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("example", "ServiceExample onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int startDefaultOption = super.onStartCommand(intent, flags, startId);
        Log.d("example", "ServiceExample onStartCommand");

        if (flip) {
            Adjust.disable();
            flip = false;
        } else {
            Adjust.enable();
            flip = true;
        }

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                Log.d("example", "ServiceExample background sleeping");
                SystemClock.sleep(3000);
                Log.d("example", "ServiceExample background awake");

                AdjustEvent event = new AdjustEvent(EVENT_TOKEN_BACKGROUND);
                Adjust.trackEvent(event);

                Log.d("example", "ServiceExample background event tracked");

                return null;
            }
        }.execute();

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("example", "ServiceExample onDestroy");
    }
}
