//
//  AdjustIo.java
//  AdjustIo
//
//  Created by Christian Wellenbrock on 11.10.12.
//  Copyright (c) 2012 adeven. All rights reserved.
//  See the file MIT-LICENSE for copying permission.
//

package com.adeven.adjustio;

import java.util.Map;

import android.content.Context;

/**
 * The main interface to AdjustIo.
 *
 * Use the methods of this class to tell AdjustIo about the usage of your app.
 * See the README for details.
 *
 * @author wellle
 * @since 11.10.12
 */
public class AdjustIo {

    /**
     * Tell AdjustIo that the application did launch.
     *
     * This is required to initialize AdjustIo.
     * Call this in the onCreate method of your launch activity.
     *
     * @param context Your application context
     *     Generally obtained by calling getApplication()
     */

    public static void appDidLaunch(Context context) {
        if (!Util.checkPermissions(context)) {
            return;
        }

        String macAddress = Util.getMacAddress(context);

        packageName = context.getPackageName();
        macSha1 = Util.sha1(macAddress);
        macShort = macAddress.replaceAll(":", "");
        userAgent = Util.getUserAgent(context);
        androidId = Util.getAndroidId(context);
        attributionId = Util.getAttributionId(context);

        trackSessionStart();
    }


    /**
     * Track any kind of event.
     *
     * You can assign a callback url to the event which
     * will get called every time the event is reported. You can also provide
     * parameters that will be forwarded to these callbacks.
     *
     * @param eventToken The token for this kind of event
     *     It must be exactly six characters long
     *     You create them in your dashboard at http://www.adjust.io
     * @param parameters An optional dictionary containing callback parameters
     *     Provide key-value-pairs to be forwarded to your callbacks
     */

    public static void trackEvent(String eventToken) {
        trackEvent(eventToken, null);
    }

    public static void trackEvent(String eventToken, Map<String, String> parameters) {
        if (eventToken.length() != 6) {
            Logger.error(
                "Event tracking only works with proper event tokens. " +
                "Find them in your dashboard at http://www.adjust.io " +
                "or contact support@adjust.io"
            );
            return;
        }

        String paramString = Util.getBase64EncodedParameters(parameters);
        String successMessage = "Tracked event: '" + eventToken + "'";
        String failureMessage = "Failed to track event: '" + eventToken + "'";

        TrackingPackage event = new TrackingPackage.Builder()
            .setPath("/event")
            .setSuccessMessage(successMessage)
            .setFailureMessage(failureMessage)
            .setUserAgent(userAgent)
            .addTrackingParameter(EVENT_TOKEN, eventToken)
            .addTrackingParameter(PACKAGE_NAME, packageName)
            .addTrackingParameter(MAC_SHORT, macShort)
            .addTrackingParameter(ANDROID_ID, androidId)
            .addTrackingParameter(PARAMETERS, paramString)
            .build();
        getRequestThread().track(event);
    }


    /**
     * Tell AdjustIo that the current user generated some revenue.
     *
     * The amount is measured in cents and rounded to on digit after the decimal
     * point. If you want to differentiate between various types of specific revenues
     * you can do so by using different event tokens. If your revenue events have
     * callbacks, you can also pass in parameters that will be forwarded to your
     * server.
     *
     * @param amountInCents The amount in cents (example: 1.5f means one and a half cents)
     * @param eventToken The token for this revenue event (see above)
     * @param parameters Parameters for this revenue event (see above)
     */

    public static void trackRevenue(float amountInCents) {
        AdjustIo.trackRevenue(amountInCents, null);
    }

    public static void trackRevenue(float amountInCents, String eventToken) {
        AdjustIo.trackRevenue(amountInCents, eventToken, null);
    }

    public static void trackRevenue(float amountInCents, String eventToken, Map<String, String> parameters) {
        if (eventToken != null && eventToken.length() != 6) {
            Logger.error(
                "Specific revenue tracking only works with proper event tokens. " +
                "Find them in your dashboard at http://www.adjust.io " +
                "or contact support@adjust.io"
            );
            return;
        }

        int amountInMillis = Math.round(10 * amountInCents);
        amountInCents = amountInMillis/10.0f; // now rounded to one decimal point
        String amount = Integer.toString(amountInMillis);
        String paramString = Util.getBase64EncodedParameters(parameters);
        String successMessage = "Tracked revenue: " + amountInCents + " Cent";
        String failureMessage = "Failed to track revenue: " + amountInCents + " Cent";

        if (eventToken != null) {
            String eventString = " (event token: '" + eventToken + "')";
            successMessage += eventString;
            failureMessage += eventString;
        }

        TrackingPackage revenue = new TrackingPackage.Builder()
            .setPath("/revenue")
            .setSuccessMessage(successMessage)
            .setFailureMessage(failureMessage)
            .setUserAgent(userAgent)
            .addTrackingParameter(PACKAGE_NAME, packageName)
            .addTrackingParameter(MAC_SHORT, macShort)
            .addTrackingParameter(ANDROID_ID, androidId)
            .addTrackingParameter(AMOUNT, amount)
            .addTrackingParameter(EVENT_TOKEN, eventToken)
            .addTrackingParameter(PARAMETERS, paramString)
            .build();
        getRequestThread().track(revenue);
    }


    /**
     * Change the verbosity of AdjustIo's logs.
     *
     * @param logLevel The desired minimum log level (default: info)
     *     Must be one of the following:
     *      - Log.VERBOSE (enable all logging)
     *      - Log.DEBUG
     *      - Log.INFO    (the default)
     *      - Log.WARN    (disable info logging)
     *      - Log.ERROR   (disable warnings as well)
     *      - Log.ASSERT  (disable errors as well)
     */

    public static void setLogLevel(int logLevel) {
        Logger.setLogLevel(logLevel);
    }


    // This line marks the end of the public interface.

    private static final String PACKAGE_NAME = "app_id";
    private static final String MAC_SHA1 = "mac_sha1";
    private static final String MAC_SHORT = "mac";
    private static final String ANDROID_ID = "android_id";
    private static final String ATTRIBUTION_ID = "fb_id";
    private static final String EVENT_TOKEN = "event_id";
    private static final String PARAMETERS = "params";
    private static final String AMOUNT = "amount";

    private static String packageName;
    private static String macSha1;
    private static String macShort;
    private static String userAgent;
    private static String androidId;
    private static String attributionId;

    private static void trackSessionStart() {
        TrackingPackage sessionStart = new TrackingPackage.Builder()
            .setPath("/startup")
            .setSuccessMessage("Tracked session start.")
            .setFailureMessage("Failed to track session start.")
            .setUserAgent(userAgent)
            .addTrackingParameter(PACKAGE_NAME, packageName)
            .addTrackingParameter(MAC_SHORT, macShort)
            .addTrackingParameter(MAC_SHA1, macSha1)
            .addTrackingParameter(ANDROID_ID, androidId)
            .addTrackingParameter(ATTRIBUTION_ID, attributionId)
            .build();
        getRequestThread().track(sessionStart);
    }

    private static RequestThread requestThread;
    private static RequestThread getRequestThread() {
        if (requestThread == null) {
            requestThread = new RequestThread();
        }
        return requestThread;
    }
}
