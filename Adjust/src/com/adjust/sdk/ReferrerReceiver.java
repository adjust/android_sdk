package com.adjust.sdk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import static com.adjust.sdk.Constants.ENCODING;
import static com.adjust.sdk.Constants.MALFORMED;
import static com.adjust.sdk.Constants.REFERRER;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

// support multiple BroadcastReceivers for the INSTALL_REFERRER:
// http://blog.appington.com/2012/08/01/giving-credit-for-android-app-installs

public class ReferrerReceiver extends BroadcastReceiver {

    protected static final String REFERRER_KEY = "AdjustInstallReferrer";

    @Override
    public void onReceive(Context context, Intent intent) {
        String rawReferrer = intent.getStringExtra(REFERRER);
        if (null == rawReferrer) {
            return;
        }

        String referrer;
        try {
            referrer = URLDecoder.decode(rawReferrer, ENCODING);
        } catch (UnsupportedEncodingException e) {
            referrer = MALFORMED;
        }

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putString(REFERRER_KEY, referrer).commit();
    }
}
