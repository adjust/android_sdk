package com.adeven.adjustio;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import android.util.Base64;

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
    public int eventCount;
    public String eventToken;
    public float amountInCents;
    public Map<String, String> callbackParameters;  // TODO: test!

    // meta TODO: remove
    public String path;
    public String userAgent;
    public String kind;
    public String suffix;

    public TrackingPackage buildSessionPackage() {
        Map<String, String> parameters = new HashMap<String, String>();

        // general
        addDate(parameters, "created_at", createdAt);
        addString(parameters, "app_token", appToken);
        addString(parameters, "mac_sha1", macSha1);
        addString(parameters, "mac", macShort);
        addString(parameters, "android_id", androidId);
        addString(parameters, "fb_id", attributionId);

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

    public TrackingPackage buildEventPackage() {
        Map<String, String> parameters = new HashMap<String, String>();

        // general
        addDate(parameters, "created_at", createdAt);
        addString(parameters, "app_token", appToken);
        addString(parameters, "mac_sha1", macSha1);
        addString(parameters, "mac", macShort);
        addString(parameters, "android_id", androidId);
        addString(parameters, "fb_id", attributionId);

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

    public TrackingPackage buildRevenuePackage() {

        Map<String, String> parameters = new HashMap<String, String>();

        // general
        addDate(parameters, "created_at", createdAt);
        addString(parameters, "app_token", appToken);
        addString(parameters, "mac_sha1", macSha1);
        addString(parameters, "mac", macShort);
        addString(parameters, "android_id", androidId);
        addString(parameters, "fb_id", attributionId);

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
