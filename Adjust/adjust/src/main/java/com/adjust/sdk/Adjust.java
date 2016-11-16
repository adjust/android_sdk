//
//  Adjust.java
//  Adjust
//
//  Created by Christian Wellenbrock on 2012-10-11.
//  Copyright (c) 2012-2014 adjust GmbH. All rights reserved.
//  See the file MIT-LICENSE for copying permission.
//

package com.adjust.sdk;

import android.content.Context;
import android.net.Uri;

/**
 * The main interface to Adjust.
 * Use the methods of this class to tell Adjust about the usage of your app.
 * See the README for details.
 */
public final class Adjust {

    private static AdjustInstance defaultInstance;

    private Adjust() {
    }

    public static synchronized AdjustInstance getDefaultInstance() {
        if (defaultInstance == null) {
            defaultInstance = new AdjustInstance();
        }
        return defaultInstance;
    }

    public static void onCreate(final AdjustConfig adjustConfig) {
        AdjustInstance adjustInstance = Adjust.getDefaultInstance();
        adjustInstance.onCreate(adjustConfig);
    }

    public static void trackEvent(final AdjustEvent event) {
        AdjustInstance adjustInstance = Adjust.getDefaultInstance();
        adjustInstance.trackEvent(event);
    }

    public static void onResume() {
        AdjustInstance adjustInstance = Adjust.getDefaultInstance();
        adjustInstance.onResume();
    }

    public static void onPause() {
        AdjustInstance adjustInstance = Adjust.getDefaultInstance();
        adjustInstance.onPause();
    }

    public static void setEnabled(final boolean enabled) {
        AdjustInstance adjustInstance = Adjust.getDefaultInstance();
        adjustInstance.setEnabled(enabled);
    }

    public static boolean isEnabled() {
        AdjustInstance adjustInstance = Adjust.getDefaultInstance();
        return adjustInstance.isEnabled();
    }

    public static void appWillOpenUrl(final Uri url) {
        AdjustInstance adjustInstance = Adjust.getDefaultInstance();
        adjustInstance.appWillOpenUrl(url);
    }

    public static void setReferrer(final String referrer) {
        AdjustInstance adjustInstance = Adjust.getDefaultInstance();
        adjustInstance.sendReferrer(referrer);
    }

    public static void setOfflineMode(final boolean enabled) {
        AdjustInstance adjustInstance = Adjust.getDefaultInstance();
        adjustInstance.setOfflineMode(enabled);
    }

    public static void sendFirstPackages() {
        AdjustInstance adjustInstance = Adjust.getDefaultInstance();
        adjustInstance.sendFirstPackages();
    }

    public static void addSessionCallbackParameter(final String key, final String value) {
        AdjustInstance adjustInstance = Adjust.getDefaultInstance();
        adjustInstance.addSessionCallbackParameter(key, value);
    }

    public static void addSessionPartnerParameter(final String key, final String value) {
        AdjustInstance adjustInstance = Adjust.getDefaultInstance();
        adjustInstance.addSessionPartnerParameter(key, value);
    }

    public static void removeSessionCallbackParameter(final String key) {
        AdjustInstance adjustInstance = Adjust.getDefaultInstance();
        adjustInstance.removeSessionCallbackParameter(key);
    }

    public static void removeSessionPartnerParameter(final String key) {
        AdjustInstance adjustInstance = Adjust.getDefaultInstance();
        adjustInstance.removeSessionPartnerParameter(key);
    }

    public static void resetSessionCallbackParameters() {
        AdjustInstance adjustInstance = Adjust.getDefaultInstance();
        adjustInstance.resetSessionCallbackParameters();
    }

    public static void resetSessionPartnerParameters() {
        AdjustInstance adjustInstance = Adjust.getDefaultInstance();
        adjustInstance.resetSessionPartnerParameters();
    }

    public static void setPushToken(final String token) {
        AdjustInstance adjustInstance = Adjust.getDefaultInstance();
        adjustInstance.setPushToken(token);
    }

    public static void getGoogleAdId(final Context context,
                                     final OnDeviceIdsRead onDeviceIdRead) {
        Util.getGoogleAdId(context, onDeviceIdRead);
    }
}
