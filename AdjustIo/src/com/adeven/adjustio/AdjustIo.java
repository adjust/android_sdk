//
//  AdjustIo.java
//  AdjustIo
//
//  Created by Christian Wellenbrock on 2012-10-11.
//  Copyright (c) 2012-2013 adeven. All rights reserved.
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
 */
public class AdjustIo {

    /**
     * Tell AdjustIo that an activity did resume.
     *
     * This is used to initialize AdjustIo and keep track of the current session state.
     * Call this in the onResume method of every activity of your app.
     *
     * @param activity The activity that has just resumed.
     */
    public static void onResume(Activity activity) {
        if (activityHandler == null) {
            activityHandler = new ActivityHandler(activity);
        }
        activityHandler.trackSubsessionStart();
    }

    /**
     * Tell AdjustIo that an activity will pause.
     *
     * This is used to calculate session attributes like session length and subsession count.
     * Call this in the onPause method of every activity of your app.
     */
    public static void onPause() {
        try {
            activityHandler.trackSubsessionEnd();
        } catch (NullPointerException e) {
            Logger.error("No activity handler found");
        }
    }

    /**
     * Tell AdjustIo that a particular event has happened.
     *
     * In your dashboard at http://adjust.io you can assign a callback URL to each
     * event type. That URL will get called every time the event is triggered. On
     * top of that you can pass a set of parameters to the following method that
     * will be forwarded to these callbacks.
     *
     * @param eventToken The Event Token for this kind of event. They are created
     *     in the dashboard at http://adjust.io and should be six characters long.
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
     * Tell AdjustIo that a user generated some revenue.
     *
     * The amount is measured in cents and rounded to on digit after the
     * decimal point. If you want to differentiate between several revenue
     * types, you can do so by using different event tokens. If your revenue
     * events have callbacks, you can also pass in parameters that will be
     * forwarded to your end point.
     *
     * @param amountInCents The amount in cents (example: 1.5 means one and a half cents)
     */
    public static void trackRevenue(double amountInCents) {
        AdjustIo.trackRevenue(amountInCents, null);
    }

    public static void trackRevenue(double amountInCents, String eventToken) {
        AdjustIo.trackRevenue(amountInCents, eventToken, null);
    }

    public static void trackRevenue(double amountInCents, String eventToken, Map<String, String> parameters) {
        try {
            activityHandler.trackRevenue(amountInCents, eventToken, parameters);
        } catch (NullPointerException e) {
            Logger.error("No activity handler found");
        }
    }

    /**
     * Every activity will get forwarded to this handler to be processed in the background.
     */
    private static ActivityHandler activityHandler;

}
