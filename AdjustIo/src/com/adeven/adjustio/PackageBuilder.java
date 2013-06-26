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
    public Map<String, String> callbackParameters;

    // meta
    public String path;
    public String userAgent;
    public String kind;
    public String suffix;
    public String successMessage;
    public String failureMessage;

    public TrackingPackage buildSessionPackage() {
        Map<String, String> pairs = new HashMap<String, String>();

        addString(pairs, "app_token", appToken);
        addString(pairs, "mac_sha1", macSha1);
        addString(pairs, "mac", macShort);
        addString(pairs, "android_id", androidId);
        addString(pairs, "fb_id", attributionId);

        addInt(pairs, "session_id", sessionCount); // TODO: rename?
        addInt(pairs, "subsession_count", subsessionCount);
        addDate(pairs, "created_at", createdAt);
        addDuration(pairs, "session_length", sessionLength);
        addDuration(pairs, "time_spent", timeSpent);
        addDuration(pairs, "last_interval", lastInterval);

        String parameterString = buildParameterString(pairs);

        path = "/startup";
        successMessage = "Tracked session start.";
        failureMessage = "Failed to track session start.";

        TrackingPackage sessionPackage = new TrackingPackage();
        sessionPackage.kind = "session start";
        sessionPackage.path = "/startup";
        sessionPackage.successMessage = "Tracked session start.";   // TODO: can these logs be improved?
        sessionPackage.failureMessage = "Failed to track session start.";
        sessionPackage.parameters = parameterString;
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

    private String buildParameterString(Map<String, String> pairs) {
        String parameterString = null;
        for (Map.Entry<String, String> entry : pairs.entrySet()) {
            String pair = entry.getKey() + "=" + entry.getValue();
            if (parameterString == null) {
                parameterString = pair;
            } else {
                parameterString += "&" + pair;
            }
        }
        return parameterString;
    }

    private void getParameterString() {
        String callbackParametersString = Util.getBase64EncodedParameters(callbackParameters);
        int amountInMillis = Math.round(10 * amountInCents);
        float amountInCents = amountInMillis / 10.0f; // now rounded to one decimal point
        String amountString = Integer.toString(amountInMillis);

        Map<String, String> pairs = new HashMap<String, String>();

        addString(pairs, "app_token", appToken);
        addString(pairs, "mac_sha1", macSha1);
        addString(pairs, "mac", macShort);
        addString(pairs, "android_id", androidId);
        addString(pairs, "fb_id", attributionId);

        addInt(pairs, "session_id", sessionCount); // TODO: rename?
        addInt(pairs, "subsession_count", subsessionCount);
        addDate(pairs, "session_length", sessionLength);
        addDate(pairs, "time_spent", timeSpent);
        addDate(pairs, "last_interval", lastInterval);

        addString(pairs, "event_id", eventToken);
        addString(pairs, "params", callbackParametersString);
        addString(pairs, "amount", amountString);
    }

    private void addString(Map<String, String> pairs, String key, String value) {
        if (value == null || value == "") {
            return;
        }
        pairs.put(key, value);
    }

    private void addInt(Map<String, String> pairs, String key, long value) {
        if (value == -1) {
            return;
        }
        String valueString = Long.toString(value);
        pairs.put(key, valueString);
    }

    private void addDate(Map<String, String> pairs, String key, long value) {
        if (value == -1) {
            return;
        }
        Date date = new Date(value);
        pairs.put(key, date.toString());    // TODO: format
    }

    private void addDuration(Map<String, String> pairs, String key, long durationInMilliSeconds) {
        long durationInSeconds = durationInMilliSeconds / 1000;
        addInt(pairs, key, durationInSeconds);
    }
}
