//
//  AdjustIo.java
//  AdjustIo
//
//  Created by Christian Wellenbrock on 11.10.12.
//  Copyright (c) 2012 adeven. All rights reserved.
//  See the file MIT-LICENSE for copying permission.
//

package com.adeven.adjustio;

import android.content.Context;
import java.util.Map;

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

        appId = context.getPackageName();
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
        // TODO: log if eventToken has wrong length
        trackEvent(eventToken, null);
    }

    public static void trackEvent(String eventToken, Map<String, String> parameters) {
        String paramString = Util.getBase64EncodedParameters(parameters);

        TrackingInformation trackingInformation = new TrackingInformation.Builder()
            .setPath("/event")
            .setSuccessMessage("Tracked event " + eventToken + ".")
            .setFailureMessage("Failed to track event " + eventToken + ".")
            .setUserAgent(userAgent)
            // TODO: add method addTrackingParameter(key, value)
            .setTrackingParameters(EVENT_TOKEN, eventToken, APP_ID, appId, MAC_SHORT, macShort, ANDROID_ID, androidId, PARAMETERS, paramString)
            .build();
        getRequestThread().track(trackingInformation);
    }

    /**
     * Tell AdjustIo that the current user generated some revenue.
     *
     * The amount is measured in cents and rounded to on digit after the decimal
     * point. If you want to differentiate between various types of revenues you
     * can do so by using different event tokens. If your revenue events have
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
        int amountInMillis = Math.round(10 * amountInCents);
        String amount = Integer.toString(amountInMillis);
        String paramString = Util.getBase64EncodedParameters(parameters);

        TrackingInformation trackingInformation = new TrackingInformation.Builder()
            .setPath("/revenue")
            .setSuccessMessage("Tracked revenue.")
            .setFailureMessage("Failed to track revenue.")
            .setUserAgent(userAgent)
            .setTrackingParameters(APP_ID, appId, MAC_SHORT, macShort, ANDROID_ID, androidId, AMOUNT, amount, EVENT_TOKEN, eventToken,
                    PARAMETERS, paramString)
            .build();
        getRequestThread().track(trackingInformation);
    }

    // TODO: add log levels INFO (default), WARN, ERROR
    /**
     * Enables toggling of the logs.
     *
     * Use this to disable the AdjustIo logs.
     *
     * @param enabled Whether or not the logging should be enabled (default: true)
     */
    public static void setLoggingEnabled(boolean enabled) {
        Logger.setLoggingEnabled(enabled);
    }

    // This line marks the end of the public interface.

    private static String appId;
    private static String macSha1;
    private static String macShort;
    private static String userAgent;
    private static String androidId;
    private static String attributionId;

    private static final String APP_ID = "app_id";
    private static final String MAC_SHA1 = "mac_sha1";
    private static final String MAC_SHORT = "mac";
    private static final String ANDROID_ID = "android_id";
    private static final String ATTRIBUTION_ID = "fb_id";
    private static final String EVENT_TOKEN = "event_id";
    private static final String PARAMETERS = "params";
    private static final String AMOUNT = "amount";

    private static RequestThread requestThread;

    private static void trackSessionStart() {
        TrackingInformation trackingInformation = new TrackingInformation.Builder()
            .setPath("/startup")
            .setSuccessMessage("Tracked session start.")
            .setFailureMessage("Failed to track session start.")
            .setUserAgent(userAgent)
            .setTrackingParameters(APP_ID, appId, MAC_SHORT, macShort, MAC_SHA1, macSha1, ANDROID_ID, androidId,
                    ATTRIBUTION_ID, attributionId)
            .build();
        getRequestThread().track(trackingInformation);
    }

    private static void trackSessionEnd() {
        TrackingInformation trackingInformation = new TrackingInformation.Builder()
            .setPath("/shutdown")
            .setSuccessMessage("Tracked session end.")
            .setFailureMessage("Failed to track session end.")
            .setUserAgent(userAgent)
            .setTrackingParameters(APP_ID, appId, MAC_SHORT, macShort, ANDROID_ID, androidId)
            .build();
        getRequestThread().track(trackingInformation);
    }

    private static RequestThread getRequestThread() {
        if (requestThread == null) {
            requestThread = new RequestThread();
        }
        return requestThread;
    }
}
