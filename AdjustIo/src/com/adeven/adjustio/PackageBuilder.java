//
//  PackageBuilder.java
//  AdjustIo
//
//  Created by Christian Wellenbrock on 2013-06-25.
//  Copyright (c) 2013 adeven. All rights reserved.
//  See the file MIT-LICENSE for copying permission.
//

package com.adeven.adjustio;

import android.text.TextUtils;
import android.util.Base64;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.json.JSONObject;

public class PackageBuilder {

    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'Z";

    // general
    protected String appToken;
    protected String macSha1;
    protected String macShortMd5;
    protected String androidId;
    protected String fbAttributionId;
    protected String userAgent;
    protected String clientSdk;
    protected String environment;

    // sessions
    protected int    sessionCount;
    protected int    subsessionCount;
    protected long   createdAt;
    protected long   sessionLength;
    protected long   timeSpent;
    protected long   lastInterval;
    protected String defaultTracker;
    protected String referrer;

    // events
    protected int                 eventCount;
    protected String              eventToken;
    protected double              amountInCents;
    protected Map<String, String> callbackParameters;

    private static SimpleDateFormat dateFormat;

    protected ActivityPackage buildSessionPackage() {
        Map<String, String> parameters = getDefaultParameters();
        addDuration(parameters, "last_interval", lastInterval);
        addString(parameters, "default_tracker", defaultTracker);
        addString(parameters, Constants.REFERRER, referrer);

        ActivityPackage sessionPackage = getDefaultActivityPackage();
        sessionPackage.path = "/startup";
        sessionPackage.kind = "session start";
        sessionPackage.suffix = "";
        sessionPackage.parameters = parameters;

        return sessionPackage;
    }

    protected ActivityPackage buildEventPackage() {
        Map<String, String> parameters = getDefaultParameters();
        injectEventParameters(parameters);

        ActivityPackage eventPackage = getDefaultActivityPackage();
        eventPackage.path = "/event";
        eventPackage.kind = "event";
        eventPackage.suffix = getEventSuffix();
        eventPackage.parameters = parameters;

        return eventPackage;
    }

    protected ActivityPackage buildRevenuePackage() {
        Map<String, String> parameters = getDefaultParameters();
        injectEventParameters(parameters);
        addString(parameters, "amount", getAmountString());

        ActivityPackage revenuePackage = getDefaultActivityPackage();
        revenuePackage.path = "/revenue";
        revenuePackage.kind = "revenue";
        revenuePackage.suffix = getRevenueSuffix();
        revenuePackage.parameters = parameters;

        return revenuePackage;
    }

    private ActivityPackage getDefaultActivityPackage() {
        ActivityPackage activityPackage = new ActivityPackage();
        activityPackage.userAgent = userAgent;
        activityPackage.clientSdk = clientSdk;
        return activityPackage;
    }

    private Map<String, String> getDefaultParameters() {
        Map<String, String> parameters = new HashMap<String, String>();

        // general
        addDate(parameters, "created_at", createdAt);
        addString(parameters, "app_token", appToken);
        addString(parameters, "mac_sha1", macSha1);
        addString(parameters, "mac_md5", macShortMd5);
        addString(parameters, "android_id", androidId);
        addString(parameters, "fb_id", fbAttributionId);
        addString(parameters, "environment", environment);

        // session related (used for events as well)
        addInt(parameters, "session_count", sessionCount);
        addInt(parameters, "subsession_count", subsessionCount);
        addDuration(parameters, "session_length", sessionLength);
        addDuration(parameters, "time_spent", timeSpent);

        return parameters;
    }

    private void injectEventParameters(Map<String, String> parameters) {
        addInt(parameters, "event_count", eventCount);
        addString(parameters, "event_token", eventToken);
        addMap(parameters, "params", callbackParameters);
    }

    private String getAmountString() {
        long amountInMillis = Math.round(10 * amountInCents);
        amountInCents = amountInMillis / 10.0; // now rounded to one decimal point
        return Long.toString(amountInMillis);
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
        if (TextUtils.isEmpty(value)) {
            return;
        }

        parameters.put(key, value);
    }

    private void addInt(Map<String, String> parameters, String key, long value) {
        if (value < 0) {
            return;
        }

        String valueString = Long.toString(value);
        addString(parameters, key, valueString);
    }

    private void addDate(Map<String, String> parameters, String key, long value) {
        if (value < 0) {
            return;
        }

        Date date = new Date(value);
        String dateString = getDateFormat().format(date);
        addString(parameters, key, dateString);
    }

    private void addDuration(Map<String, String> parameters, String key, long durationInMilliSeconds) {
        if (durationInMilliSeconds < 0) {
            return;
        }

        long durationInSeconds = (durationInMilliSeconds + 500) / 1000;
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

    private SimpleDateFormat getDateFormat() {
        if (dateFormat == null) {
            dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.US);
        }
        return dateFormat;
    }
}
