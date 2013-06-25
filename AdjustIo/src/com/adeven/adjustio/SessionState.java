package com.adeven.adjustio;

import java.io.Serializable;

public class SessionState implements Serializable {
    private static final long serialVersionUID = 9039439291143138148L;

    // TODO: make attributes private?

    // global counters
    public int eventCount;
    public int sessionCount;

    // session attributes
    public int subsessionCount;
    public double sessionLength;
    public double timeSpent;
    public long createdAt;          // all times in milliseconds since 1970
    public long lastSubsessionStart;
    public long lastActivity;


    public PackageBuilder getPackageBuilder() {
        PackageBuilder builder = new PackageBuilder();

        builder.setPath("/startup");
        builder.setSuccessMessage("Tracked session start.");
        builder.setFailureMessage("Failed to track session start.");
        builder.setSessionCount(sessionCount);
        builder.setSubsessionCount(subsessionCount);
        builder.setSessionLength(sessionLength);
        builder.setTimeSpent(timeSpent);
        builder.setCreatedAt(createdAt);

        return builder;
    }
}
