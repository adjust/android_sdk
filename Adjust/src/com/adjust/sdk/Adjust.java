//
//  Adjust.java
//  Adjust
//
//  Created by Christian Wellenbrock on 2012-10-11.
//  Copyright (c) 2012-2014 adjust GmbH. All rights reserved.
//  See the file MIT-LICENSE for copying permission.
//

package com.adjust.sdk;

import android.net.Uri;

/**
 * The main interface to Adjust.
 * <p/>
 * Use the methods of this class to tell Adjust about the usage of your app.
 * See the README for details.
 */
public class Adjust {

    private ActivityHandler activityHandler;

    /**
     * Tell Adjust that an activity did resume.
     * <p/>
     * This is used to initialize Adjust and keep track of the current session state.
     * Call this in the onResume method of every activity of your app.
     *
     * @param context The context of the activity that has just resumed.
     */

    /**
     * Tell Adjust that an activity will pause.
     * <p/>
     * This is used to calculate session attributes like session length and subsession count.
     * Call this in the onPause method of every activity of your app.
     */

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

    /**
     * Enable or disable the adjust SDK
     *
     * @param enabled The flag to enable or disable the adjust SDK
     */

    /**
     * Every activity will get forwarded to this handler to be processed in the background.
     */
    private static Logger getLogger() {
        return AdjustFactory.getLogger();
    };

    public void onCreate(AdjustConfig adjustConfig) {
        if (activityHandler != null) {
            getLogger().error("Adjust already initialized");
            return;
        }

        activityHandler = new ActivityHandler(adjustConfig);
    }

    public void trackEvent(Event event) {
        if (!checkActivityHandler()) return;
        activityHandler.trackEvent(event);
    }

    public void onResume() {
        if (!checkActivityHandler()) return;
        activityHandler.trackSubsessionStart();
    }

    public void onPause() {
        if (!checkActivityHandler()) return;
        activityHandler.trackSubsessionEnd();
    }

    public void setOnFinishedListener(OnFinishedListener listener) {
        if (!checkActivityHandler()) return;
        activityHandler.setOnFinishedListener(listener);
    }

    public void setEnabled(Boolean enabled) {
        if (!checkActivityHandler()) return;
        activityHandler.setEnabled(enabled);
    }

    public boolean isEnabled() {
        if (!checkActivityHandler()) return false;
        return activityHandler.isEnabled();
    }

    public void appWillOpenUrl(Uri url) {
        if (!checkActivityHandler()) return;
        activityHandler.readOpenUrl(url);
    }


    private boolean checkActivityHandler() {
        if (activityHandler == null) {
            getLogger().error("Please initialize Adjust by calling 'onCreate' before");
            return false;
        } else {
            return true;
        }
    }
}
