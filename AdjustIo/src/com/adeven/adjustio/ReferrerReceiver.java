package com.adeven.adjustio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

// support multiple BroadcastReceivers for the INSTALL_REFERRER:
// http://blog.appington.com/2012/08/01/giving-credit-for-android-app-installs

public class ReferrerReceiver extends BroadcastReceiver {
    protected static final String REFERRER_KEY = "AdjustIoInstallReferrer";

    @Override
    public void onReceive(Context context, Intent intent) {
        String rawReferrer = intent.getStringExtra("referrer");
        if (rawReferrer == null) {
            return;
        }

        String referrer = "";
        try {
            referrer = URLDecoder.decode(rawReferrer, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            referrer = "malformed";
        }

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putString(REFERRER_KEY, referrer).commit();
    }
}
