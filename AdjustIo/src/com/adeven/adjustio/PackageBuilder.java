package com.adeven.adjustio;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import android.util.Base64;

public class PackageBuilder {
    // general
    protected String appToken;
    protected String macSha1;
    protected String macShort; // TODO: md5!
    protected String androidId;
    protected String attributionId;

    // sessions
    protected int sessionCount;
    protected int subsessionCount;
    protected long createdAt;
    protected long sessionLength;
    protected long timeSpent;
    protected long lastInterval;

    // events
    protected int eventCount;
    protected String eventToken;
    protected float amountInCents;
    protected Map<String, String> callbackParameters;  // TODO: test!

    // meta TODO: remove
    protected String path;
    protected String userAgent;
    protected String kind;
    protected String suffix;

    protected TrackingPackage buildSessionPackage() {
        Map<String, String> parameters = getDefaultParameters();

        // session specific
        addInt(parameters, "session_id", sessionCount); // TODO: rename?
        addInt(parameters, "subsession_count", subsessionCount);
        addDuration(parameters, "session_length", sessionLength);
        addDuration(parameters, "time_spent", timeSpent);
        addDuration(parameters, "last_interval", lastInterval);

        TrackingPackage sessionPackage = new TrackingPackage();
        sessionPackage.path = "/startup";
        sessionPackage.kind = "session start";
        sessionPackage.suffix = ".";
        sessionPackage.parameters = parameters;
        sessionPackage.userAgent = userAgent;

        return sessionPackage;
    }

    protected TrackingPackage buildEventPackage() {
        Map<String, String> parameters = getDefaultParameters();

        // event specific
        addInt(parameters, "event_count", eventCount);
        addString(parameters, "event_id", eventToken); // TODO: rename
        addMap(parameters, "params", callbackParameters);

        // session specific (current values at time of event)
        addInt(parameters, "session_count", sessionCount);
        addInt(parameters, "subsession_count", subsessionCount);
        addDuration(parameters, "session_length", sessionLength);
        addDuration(parameters, "time_spent", timeSpent);
        addDuration(parameters, "last_interval", lastInterval);

        TrackingPackage eventPackage = new TrackingPackage();
        eventPackage.path = "/event";
        eventPackage.kind = "event";
        eventPackage.suffix = " '" + eventToken + "'.";
        eventPackage.parameters = parameters;
        eventPackage.userAgent = userAgent;

        return eventPackage;
    }

    protected TrackingPackage buildRevenuePackage() {
        Map<String, String> parameters = getDefaultParameters();

        // event specific
        addInt(parameters, "event_count", eventCount);
        addString(parameters, "event_token", eventToken);
        addString(parameters, "amount", getAmountString());
        addMap(parameters, "params", callbackParameters);

        // session specific (current values at time of event)
        addInt(parameters, "event_count", eventCount);
        addInt(parameters, "session_count", sessionCount);
        addInt(parameters, "subsession_count", subsessionCount);
        addDuration(parameters, "session_length", sessionLength);
        addDuration(parameters, "time_spent", timeSpent);
        addDuration(parameters, "last_interval", lastInterval);

        TrackingPackage revenuePackage = new TrackingPackage();
        revenuePackage.path = "/revenue";
        revenuePackage.kind = "revenue";
        revenuePackage.suffix = getRevenueSuffix();
        revenuePackage.parameters = parameters;
        revenuePackage.userAgent = userAgent;

        return revenuePackage;
    }

    private Map<String, String> getDefaultParameters() {
        Map<String, String> parameters = new HashMap<String, String>();

        addDate(parameters, "created_at", createdAt);
        addString(parameters, "app_token", appToken);
        addString(parameters, "mac_sha1", macSha1);
        addString(parameters, "mac", macShort);
        addString(parameters, "android_id", androidId);
        addString(parameters, "fb_id", attributionId);

        return parameters;
    }

    private String getAmountString() {
        int amountInMillis = Math.round(10 * amountInCents);
        amountInCents = amountInMillis / 10.0f; // now rounded to one decimal point
        String amountString = Integer.toString(amountInMillis);
        return amountString;
    }

    // examples: " (12.5 cent)."
    //           " (12.5 cent, 'abc123')."
    private String getRevenueSuffix() {
        String suffix = " (" + amountInCents + " cent";
        if (eventToken != null) {
            suffix += ", '" + eventToken + "'";
        }
        suffix += ").";
        return suffix;
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
        String dateString = date.toString(); // TODO: format
        addString(parameters, key, dateString);
    }

    private void addDuration(Map<String, String> parameters, String key, long durationInMilliSeconds) {
        long durationInSeconds = durationInMilliSeconds / 1000;
        addInt(parameters, key, durationInSeconds);
    }

    private void addMap(Map<String, String> parameters, String key, Map<String, String> map) {
        if (map == null) {
            return;
        }

        JSONObject jsonObject = new JSONObject(map);
        byte[] jsonBytes = jsonObject.toString().getBytes();
        String encodedMap = Base64.encodeToString(jsonBytes, Base64.NO_WRAP);

        addString(parameters, key, encodedMap);
    }
}
