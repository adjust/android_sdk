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

import android.app.Activity;

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

    // every call gets forwarded to activityHandler
    private static ActivityHandler activityHandler;

    // TODO: update all comments
    /**
     * Tell AdjustIo that the application did launch.
     *
     * This is required to initialize AdjustIo.
     * Call this in the onCreate method of your launch activity.
     *
     * @param context Your application context
     *     Generally obtained by calling getApplication()
     */

    public static void onResume(String appToken, Activity activity) {
        if (activityHandler == null) {
            activityHandler = new ActivityHandler(appToken, activity);
        }
        activityHandler.trackSubsessionStart();
    }

    public static void onPause() {
        try {
            activityHandler.trackSubsessionEnd();
        } catch (NullPointerException e) {
            Logger.error("No activity handler found");
        }
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
        try {
            activityHandler.trackEvent(eventToken, parameters);
        } catch (NullPointerException e) {
            Logger.error("No activity handler found");
        }
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
        try {
            activityHandler.trackRevenue(amountInCents, eventToken, parameters);
        } catch (NullPointerException e) {
            Logger.error("No activity handler found");
        }
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
}
