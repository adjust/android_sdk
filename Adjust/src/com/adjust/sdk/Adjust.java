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
    private static Adjust defaultInstance;
    private static Logger getLogger() {
        return AdjustFactory.getLogger();
    };

    private Adjust() {};

    public static synchronized Adjust getInstance() {
        if (defaultInstance == null) {
            defaultInstance = new Adjust();
        }
        return defaultInstance;
    }

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
