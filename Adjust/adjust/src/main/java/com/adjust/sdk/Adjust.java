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
public class Adjust {

    static AdjustInstance defaultInstance;

    private Adjust() {
    }

    public static synchronized AdjustInstance getDefaultInstance() {
        if (defaultInstance == null) {
            defaultInstance = new AdjustInstance();
        }
        return defaultInstance;
    }

    public static void onCreate(AdjustConfig adjustConfig) {
        AdjustInstance adjustInstance = Adjust.getDefaultInstance();
        adjustInstance.onCreate(adjustConfig);
    }

    public static void trackEvent(AdjustEvent event) {
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

    public static void setEnabled(boolean enabled) {
        AdjustInstance adjustInstance = Adjust.getDefaultInstance();
        adjustInstance.setEnabled(enabled);
    }

    public static boolean isEnabled() {
        AdjustInstance adjustInstance = Adjust.getDefaultInstance();
        return adjustInstance.isEnabled();
    }

    public static void appWillOpenUrl(Uri url) {
        AdjustInstance adjustInstance = Adjust.getDefaultInstance();
        adjustInstance.appWillOpenUrl(url);
    }

    public static void setReferrer(String referrer) {
        AdjustInstance adjustInstance = Adjust.getDefaultInstance();
        adjustInstance.sendReferrer(referrer);
    }

    public static void setOfflineMode(boolean enabled) {
        AdjustInstance adjustInstance = Adjust.getDefaultInstance();
        adjustInstance.setOfflineMode(enabled);
    }

    public static void sendFirstPackages() {
        AdjustInstance adjustInstance = Adjust.getDefaultInstance();
        adjustInstance.sendFirstPackages();
    }

    public static void addSessionCallbackParameter(String key, String value) {
        AdjustInstance adjustInstance = Adjust.getDefaultInstance();
        adjustInstance.addSessionCallbackParameter(key, value);
    }

    public static void addSessionPartnerParameter(String key, String value) {
        AdjustInstance adjustInstance = Adjust.getDefaultInstance();
        adjustInstance.addSessionPartnerParameter(key, value);
    }

    public static void removeSessionCallbackParameter(String key) {
        AdjustInstance adjustInstance = Adjust.getDefaultInstance();
        adjustInstance.removeSessionCallbackParameter(key);
    }

    public static void removeSessionPartnerParameter(String key) {
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

    public static void setPushToken(String token) {
        AdjustInstance adjustInstance = Adjust.getDefaultInstance();
        adjustInstance.setPushToken(token);
    }

    public static void getGoogleAdId(Context context, OnDeviceIdsRead onDeviceIdRead) {
        Util.getGoogleAdId(context, onDeviceIdRead);
    }

    public static String getAdid() {
        AdjustInstance adjustInstance = Adjust.getDefaultInstance();
        return adjustInstance.getAdid();
    }

    public static AdjustAttribution getAttribution() {
        AdjustInstance adjustInstance = Adjust.getDefaultInstance();
        return adjustInstance.getAttribution();
    }
}
