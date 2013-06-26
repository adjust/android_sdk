package com.adeven.adjustio;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class PackageBuilder {
    // general
    public String appToken;
    public String macSha1;
    public String macShort; // TODO: md5!
    public String androidId;
    public String attributionId;

    // sessions
    public int sessionCount;
    public int subsessionCount;
    public long createdAt;
    public long sessionLength;
    public long timeSpent;
    public long lastInterval;

    // events
    public String eventToken;
    public float amountInCents;
    public Map<String, String> callbackParameters; // TODO: remove

    // meta
    public String path;
    public String userAgent;
    public String kind;
    public String suffix;
    public String successMessage;
    public String failureMessage;

    public TrackingPackage buildSessionPackage() {
        Map<String, String> parameters = new HashMap<String, String>();

        addString(parameters, "app_token", appToken);
        addString(parameters, "mac_sha1", macSha1);
        addString(parameters, "mac", macShort);
        addString(parameters, "android_id", androidId);
        addString(parameters, "fb_id", attributionId);

        addInt(parameters, "session_id", sessionCount); // TODO: rename?
        addInt(parameters, "subsession_count", subsessionCount);
        addDate(parameters, "created_at", createdAt);
        addDuration(parameters, "session_length", sessionLength);
        addDuration(parameters, "time_spent", timeSpent);
        addDuration(parameters, "last_interval", lastInterval);

        path = "/startup";
        successMessage = "Tracked session start.";
        failureMessage = "Failed to track session start.";

        TrackingPackage sessionPackage = new TrackingPackage();
        sessionPackage.kind = "session start";
        sessionPackage.path = "/startup";
        sessionPackage.successMessage = "Tracked session start.";   // TODO: can these logs be improved?
        sessionPackage.failureMessage = "Failed to track session start.";
        sessionPackage.parameters = parameters;
        sessionPackage.userAgent = userAgent;

        Logger.info("session package: " + sessionPackage);

        return sessionPackage;
        // similar for event and revenue, then extract common parts
    }

    public TrackingPackage buildEventPackage() {
        return null;
    }

    public TrackingPackage buildRevenuePackage() {
        return null;
    }

    private void addString(Map<String, String> parameters, String key, String value) {
        if (value == null || value == "") {
            return;
        }
        parameters.put(key, value);
    }

    private void addInt(Map<String, String> parameters, String key, long value) {
        if (value == -1) {
            return;
        }
        String valueString = Long.toString(value);
        addString(parameters, key, valueString);
    }

    private void addDate(Map<String, String> parameters, String key, long value) {
        if (value == -1) {
            return;
        }
        Date date = new Date(value);
        String dateString = date.toString();    // TODO: format
        addString(parameters, key, dateString);
    }

    private void addDuration(Map<String, String> parameters, String key, long durationInMilliSeconds) {
        long durationInSeconds = durationInMilliSeconds / 1000;
        addInt(parameters, key, durationInSeconds);
    }
}
