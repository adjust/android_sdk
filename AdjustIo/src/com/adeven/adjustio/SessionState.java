package com.adeven.adjustio;

import java.io.Serializable;
import java.util.Date;

public class SessionState implements Serializable {
    private static final long serialVersionUID = 9039439291143138148L;

    // TODO: make attributes private?

    // global counters
    protected int eventCount;
    protected int sessionCount;

    // session attributes
    protected int subsessionCount;
    protected long sessionLength;      // all durations in milliseconds
    protected long timeSpent;
    protected long createdAt;          // all times in milliseconds since 1970
    protected long lastActivity;
    protected long lastInterval;

    protected SessionState() {
        eventCount = 0;        // no events yet
        sessionCount = 0;      // the first session just started
        subsessionCount = -1;  // we don't know how many subssessions this first  session will have
        sessionLength = -1;    // same for session length and time spent
        timeSpent = -1;        // this information will be collected and attached to the next session
        createdAt = -1;
        lastActivity = -1;
        lastInterval = -1;
    }

    protected void startNextSession(long now) {
        sessionCount++;        // the next session just started
        subsessionCount = 1;   // first subsession
        sessionLength = 0;     // no session length yet
        timeSpent = 0;         // no time spent yet
        createdAt = now;
        lastActivity = now;
        lastInterval = 0;
    }

    protected void injectSessionAttributes(PackageBuilder builder) {
        builder.sessionCount = sessionCount;
        builder.subsessionCount = subsessionCount;
        builder.sessionLength = sessionLength;
        builder.timeSpent = timeSpent;
        builder.createdAt = createdAt;

        builder.lastInterval = lastInterval;
    }

    protected void injectEventAttributes(PackageBuilder builder) {
        builder.sessionCount = sessionCount;
        builder.subsessionCount = subsessionCount;
        builder.sessionLength = sessionLength;
        builder.timeSpent = timeSpent;
        builder.createdAt = createdAt;

        builder.eventCount = eventCount;
    }

    public String toString() {
        return "ec:" + eventCount +
                " sc:" + sessionCount +
                " ssc:" + subsessionCount +
                " sl:" + sessionLength +
                " ts:" + timeSpent +
                " ca:" + stamp(createdAt) +
                " la:" + stamp(lastActivity);
    }

    private static String stamp(long date) {
        Date d = new Date(date);
        return "" + d.getHours() +
                ":" + d.getMinutes() +
                ":" + d.getSeconds();
    }
}
