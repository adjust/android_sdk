package com.adeven.adjustio;

import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
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
    protected String userAgent;

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
    protected Map<String, String> callbackParameters;

    protected ActivityPackage buildSessionPackage() {
        Map<String, String> parameters = getDefaultParameters();

        ActivityPackage sessionPackage = new ActivityPackage();
        sessionPackage.path = "/startup";
        sessionPackage.kind = "session start";
        sessionPackage.suffix = ".";
        sessionPackage.parameters = parameters;
        sessionPackage.userAgent = userAgent;

        return sessionPackage;
    }

    protected ActivityPackage buildEventPackage() {
        Map<String, String> parameters = getDefaultParameters();
        injectEventParameters(parameters);

        ActivityPackage eventPackage = new ActivityPackage();
        eventPackage.path = "/event";
        eventPackage.kind = "event";
        eventPackage.suffix = getEventSuffix();
        eventPackage.parameters = parameters;
        eventPackage.userAgent = userAgent;

        return eventPackage;
    }

    protected ActivityPackage buildRevenuePackage() {
        Map<String, String> parameters = getDefaultParameters();
        injectEventParameters(parameters);
        addString(parameters, "amount", getAmountString());

        ActivityPackage revenuePackage = new ActivityPackage();
        revenuePackage.path = "/revenue";
        revenuePackage.kind = "revenue";
        revenuePackage.suffix = getRevenueSuffix();
        revenuePackage.parameters = parameters;
        revenuePackage.userAgent = userAgent;

        return revenuePackage;
    }

    private Map<String, String> getDefaultParameters() {
        Map<String, String> parameters = new HashMap<String, String>();

        // general
        addDate(parameters, "created_at", createdAt);
        addString(parameters, "app_token", appToken);
        addString(parameters, "mac_sha1", macSha1);
        addString(parameters, "mac", macShort);
        addString(parameters, "android_id", androidId);
        addString(parameters, "fb_id", attributionId);

        // session related (used for events as well)
        addInt(parameters, "session_id", sessionCount); // TODO: rename parameters
        addInt(parameters, "subsession_count", subsessionCount);
        addDuration(parameters, "session_length", sessionLength);
        addDuration(parameters, "time_spent", timeSpent);
        addDuration(parameters, "last_interval", lastInterval);

        return parameters;
    }

    private void injectEventParameters(Map<String, String> parameters) {
        addInt(parameters, "event_count", eventCount);
        addString(parameters, "event_id", eventToken); // TODO: rename parameters
        addMap(parameters, "params", callbackParameters);
    }

    private String getAmountString() {
        int amountInMillis = Math.round(10 * amountInCents);
        amountInCents = amountInMillis / 10.0f; // now rounded to one decimal point
        String amountString = Integer.toString(amountInMillis);
        return amountString;
    }

    private String getEventSuffix() {
        return String.format(" '%s'", eventToken);
    }

    private String getRevenueSuffix() {
        if (eventToken != null) {
            return String.format(Locale.US, " (%.1f cent, '%s')", amountInCents, eventToken);
        } else {
            return String.format(Locale.US, " (%.1f cent)", amountInCents);
        }
    }

    private void addString(Map<String, String> parameters, String key, String value) {
        if (value == null || value == "") return;

        parameters.put(key, value);
    }

    private void addInt(Map<String, String> parameters, String key, long value) {
        if (value < 0) return;

        String valueString = Long.toString(value);
        addString(parameters, key, valueString);
    }

    private void addDate(Map<String, String> parameters, String key, long value) {
        if (value < 0) return;

        Date date = new Date(value);
        String dateString = date.toString(); // TODO: format with DateFormat
        addString(parameters, key, dateString);
    }

    private void addDuration(Map<String, String> parameters, String key, long durationInMilliSeconds) {
        if (durationInMilliSeconds < 0) return;

        // TODO: test rounding
        long durationInSeconds = (durationInMilliSeconds + 500) / 1000;
        addInt(parameters, key, durationInSeconds);
    }

    private void addMap(Map<String, String> parameters, String key, Map<String, String> map) {
        if (map == null) return;

        JSONObject jsonObject = new JSONObject(map);
        byte[] jsonBytes = jsonObject.toString().getBytes();
        String encodedMap = Base64.encodeToString(jsonBytes, Base64.NO_WRAP);

        addString(parameters, key, encodedMap);
    }
}
