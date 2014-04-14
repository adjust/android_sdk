//
//  Adjust.java
//  Adjust
//
//  Created by Christian Wellenbrock on 2012-10-11.
//  Copyright (c) 2012-2013 adeven. All rights reserved.
//  See the file MIT-LICENSE for copying permission.
//

package com.adjust.sdk;

import static com.adjust.sdk.Constants.NO_ACTIVITY_HANDLER_FOUND;

import java.util.Map;

import android.app.Activity;
import android.net.Uri;

/**
 * The main interface to Adjust.
 * <p/>
 * Use the methods of this class to tell Adjust about the usage of your app.
 * See the README for details.
 */
public class Adjust {

    /**
     * Tell Adjust that an activity did resume.
     * <p/>
     * This is used to initialize Adjust and keep track of the current session state.
     * Call this in the onResume method of every activity of your app.
     *
     * @param activity The activity that has just resumed.
     */
    public static void onResume(Activity activity) {
        if (null == activityHandler) {
            activityHandler = new ActivityHandler(activity);
        }
        logger = AdjustFactory.getLogger();
        activityHandler.trackSubsessionStart();
    }

    /**
     * Tell Adjust that an activity will pause.
     * <p/>
     * This is used to calculate session attributes like session length and subsession count.
     * Call this in the onPause method of every activity of your app.
     */
    public static void onPause() {
        try {
            logger.debug("onPause");
            activityHandler.trackSubsessionEnd();
        } catch (NullPointerException e) {
            if(logger != null)
                logger.error(NO_ACTIVITY_HANDLER_FOUND);
        }
    }

    public static void setOnFinishedListener(OnFinishedListener listener) {
        try {
            activityHandler.setOnFinishedListener(listener);
        } catch (NullPointerException e) {
            if(logger != null)
                logger.error(NO_ACTIVITY_HANDLER_FOUND);
        }
    }

    /**
     * Tell Adjust that a particular event has happened.
     * <p/>
     * In your dashboard at http://adjust.io you can assign a callback URL to each
     * event type. That URL will get called every time the event is triggered. On
     * top of that you can pass a set of parameters to the following method that
     * will be forwarded to these callbacks.
     *
     * @param eventToken The Event Token for this kind of event. They are created
     *                   in the dashboard at http://adjust.io and should be six characters long.
     * @param parameters An optional dictionary containing the callback parameters.
     *                   Provide key-value-pairs to be forwarded to your callbacks.
     */
    public static void trackEvent(String eventToken) {
        trackEvent(eventToken, null);
    }

    public static void trackEvent(String eventToken, Map<String, String> parameters) {
        try {
            activityHandler.trackEvent(eventToken, parameters);
        } catch (NullPointerException e) {
            if(logger != null)
                logger.error(NO_ACTIVITY_HANDLER_FOUND);
        }
    }


    /**
     * Tell Adjust that a user generated some revenue.
     * <p/>
     * The amount is measured in cents and rounded to on digit after the
     * decimal point. If you want to differentiate between several revenue
     * types, you can do so by using different event tokens. If your revenue
     * events have callbacks, you can also pass in parameters that will be
     * forwarded to your end point.
     *
     * @param amountInCents The amount in cents (example: 1.5 means one and a half cents)
     * @param eventToken The token for this revenue event (optional, see above)
     * @param parameters Parameters for this revenue event (optional, see above)
     */
    public static void trackRevenue(double amountInCents) {
        Adjust.trackRevenue(amountInCents, null);
    }

    public static void trackRevenue(double amountInCents, String eventToken) {
        Adjust.trackRevenue(amountInCents, eventToken, null);
    }

    public static void trackRevenue(double amountInCents, String eventToken, Map<String, String> parameters) {
        try {
            activityHandler.trackRevenue(amountInCents, eventToken, parameters);
        } catch (NullPointerException e) {
            if(logger != null)
                logger.error(NO_ACTIVITY_HANDLER_FOUND);
        }
    }

    /**
     * Enable or disable the adjust SDK
     *
     * @param enabled The flag to enable or disable the adjust SDK
     */
    public static void setEnabled(Boolean enabled) {
        try {
            activityHandler.setEnabled(enabled);
        } catch (NullPointerException e) {
            if (logger != null)
                logger.error(NO_ACTIVITY_HANDLER_FOUND);
        }
    }

    /**
     * Check if the SDK is enabled or disabled
     */
    public static Boolean isEnabled() {
        try {
            return activityHandler.isEnabled();
        } catch (NullPointerException e) {
            if (logger != null)
                logger.error(NO_ACTIVITY_HANDLER_FOUND);
        }
        return false;
    }

    public static void appWillOpenUrl(Uri url) {
        try {
            activityHandler.readOpenUrl(url);
        } catch (NullPointerException e) {
            if (logger != null)
                logger.error(NO_ACTIVITY_HANDLER_FOUND);
        }

    }


    // Special appDidLaunch method used by SDK wrappers such as our Adobe Air SDK.
    protected static void appDidLaunch(Activity activity, String appToken, String environment, String logLevel, boolean eventBuffering) {
        activityHandler = new ActivityHandler(activity, appToken, environment, logLevel, eventBuffering);
    }

    // Special method used by SDK wrappers such as our Adobe Air SDK.
    protected static void setSdkPrefix(String sdkPrefix) {
        activityHandler.setSdkPrefix(sdkPrefix);
    }

    /**
     * Every activity will get forwarded to this handler to be processed in the background.
     */
    private static ActivityHandler activityHandler;
    private static Logger logger;

}
