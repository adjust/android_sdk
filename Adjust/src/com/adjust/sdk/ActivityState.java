//
//  ActivityState.java
//  Adjust
//
//  Created by Christian Wellenbrock on 2013-06-25.
//  Copyright (c) 2013 adeven. All rights reserved.
//  See the file MIT-LICENSE for copying permission.
//

package com.adjust.sdk;

import java.io.IOException;
import java.io.NotActiveException;
import java.io.ObjectInputStream;
import java.io.ObjectInputStream.GetField;
import java.io.Serializable;
import java.util.Date;
import java.util.Locale;

import android.util.Log;

public class ActivityState implements Serializable {
    private static final long serialVersionUID = 9039439291143138148L;

    // persistent data
    protected String uuid;
    protected Boolean enabled;

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
        // create UUID for new devices
        uuid = Util.createUuid();
        enabled = true;

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
        builder.setLastInterval(lastInterval);
    }

    protected void injectEventAttributes(PackageBuilder builder) {
        injectGeneralAttributes(builder);
        builder.setEventCount(eventCount);
    }

    @Override
    public String toString() {
        return String.format(Locale.US,
                             "ec:%d sc:%d ssc:%d sl:%.1f ts:%.1f la:%s",
                             eventCount, sessionCount, subsessionCount,
                             sessionLength / 1000.0, timeSpent / 1000.0,
                             stamp(lastActivity));
    }

    private void readObject(ObjectInputStream stream) throws NotActiveException, IOException, ClassNotFoundException {
        GetField fields = stream.readFields();

        eventCount = fields.get("eventCount", 0);
        sessionCount = fields.get("sessionCount", 0);
        subsessionCount = fields.get("subsessionCount", -1);
        sessionLength = fields.get("sessionLength", -1l);
        timeSpent = fields.get("timeSpent", -1l);
        lastActivity = fields.get("lastActivity", -1l);
        createdAt = fields.get("createdAt", -1l);
        lastInterval = fields.get("lastInterval", -1l);

        // default values for migrating devices
        uuid = null;
        enabled = true;
        // try to read in order of less recent new fields
        try {
            uuid = (String)fields.get("uuid", null);
            enabled = fields.get("enabled", true);
            // add new fields here
        } catch (Exception e) {
            Logger logger = AdjustFactory.getLogger();
            logger.debug(String.format("Unable to read new field in migration device with error (%s)",
                    e.getMessage()));
        }

        // create UUID for migrating devices
        if (uuid == null) {
            uuid = Util.createUuid();
            Log.d("XXX", "migrate " + uuid);
        }
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
        builder.setSessionCount(sessionCount);
        builder.setSubsessionCount(subsessionCount);
        builder.setSessionLength(sessionLength);
        builder.setTimeSpent(timeSpent);
        builder.setCreatedAt(createdAt);
        builder.setUuid(uuid);
    }
}
