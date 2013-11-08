//
//  ActivityState.java
//  AdjustIo
//
//  Created by Christian Wellenbrock on 2013-06-25.
//  Copyright (c) 2013 adeven. All rights reserved.
//  See the file MIT-LICENSE for copying permission.
//

package com.adeven.adjustio;

import java.io.Serializable;
import java.util.Date;
import java.util.Locale;

public class ActivityState implements Serializable {
    private static final long serialVersionUID = 9039439291143138148L;

    // global counters
    protected int eventCount;
    protected int sessionCount;

    // session attributes
    protected int  subsessionCount;
    protected long sessionLength;   // all durations in milliseconds
    protected long timeSpent;
    protected long lastActivity;    // all times in milliseconds since 1970

    protected long createdAt;
    protected long lastInterval;

    protected ActivityState() {
        eventCount = 0; // no events yet
        sessionCount = 0; // the first session just started
        subsessionCount = -1; // we don't know how many subsessions this first  session will have
        sessionLength = -1; // same for session length and time spent
        timeSpent = -1; // this information will be collected and attached to the next session
        lastActivity = -1;
        createdAt = -1;
        lastInterval = -1;
    }

    protected void resetSessionAttributes(long now) {
        subsessionCount = 1; // first subsession
        sessionLength = 0; // no session length yet
        timeSpent = 0; // no time spent yet
        lastActivity = now;
        createdAt = -1;
        lastInterval = -1;
    }

    protected void injectSessionAttributes(PackageBuilder builder) {
        injectGeneralAttributes(builder);
        builder.lastInterval = lastInterval;
    }

    protected void injectEventAttributes(PackageBuilder builder) {
        injectGeneralAttributes(builder);
        builder.eventCount = eventCount;
    }

    public String toString() {
        return String.format(Locale.US,
                             "ec:%d sc:%d ssc:%d sl:%.1f ts:%.1f la:%s",
                             eventCount, sessionCount, subsessionCount,
                             sessionLength / 1000.0, timeSpent / 1000.0,
                             stamp(lastActivity));
    }

    private static String stamp(long dateMillis) {
        Date date = new Date(dateMillis);
        return String.format(Locale.US,
                             "%02d:%02d:%02d",
                             date.getHours(),
                             date.getMinutes(),
                             date.getSeconds());
    }

    private void injectGeneralAttributes(PackageBuilder builder) {
        builder.sessionCount = sessionCount;
        builder.subsessionCount = subsessionCount;
        builder.sessionLength = sessionLength;
        builder.timeSpent = timeSpent;
        builder.createdAt = createdAt;
    }
}
