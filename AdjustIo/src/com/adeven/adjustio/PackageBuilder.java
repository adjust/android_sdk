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
    private   String appToken;
    private   String macSha1;
    private   String macShortMd5;
    private String androidId;
    private String fbAttributionId;
    private String userAgent;
    private String clientSdk;
    private String environment;

    // sessions
    private int    sessionCount;
    private int    subsessionCount;
    private long   createdAt;
    private long   sessionLength;
    private long   timeSpent;
    private long   lastInterval;
    private String defaultTracker;
    private String referrer;

    // events
    private int                 eventCount;
    private String              eventToken;
    private double              amountInCents;
    private Map<String, String> callbackParameters;

    private static SimpleDateFormat dateFormat;

    public String getAppToken() {
        return appToken;
    }

    public void setAppToken(String appToken) {
        this.appToken = appToken;
    }

    public String getMacSha1() {
        return macSha1;
    }

    public void setMacSha1(String macSha1) {
        this.macSha1 = macSha1;
    }

    public String getMacShortMd5() {
        return macShortMd5;
    }

    public void setMacShortMd5(String macShortMd5) {
        this.macShortMd5 = macShortMd5;
    }

    public String getAndroidId() {
        return androidId;
    }

    public void setAndroidId(String androidId) {
        this.androidId = androidId;
    }

    public String getFbAttributionId() {
        return fbAttributionId;
    }

    public void setFbAttributionId(String fbAttributionId) {
        this.fbAttributionId = fbAttributionId;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getClientSdk() {
        return clientSdk;
    }

    public void setClientSdk(String clientSdk) {
        this.clientSdk = clientSdk;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public int getSessionCount() {
        return sessionCount;
    }

    public void setSessionCount(int sessionCount) {
        this.sessionCount = sessionCount;
    }

    public int getSubsessionCount() {
        return subsessionCount;
    }

    public void setSubsessionCount(int subsessionCount) {
        this.subsessionCount = subsessionCount;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getSessionLength() {
        return sessionLength;
    }

    public void setSessionLength(long sessionLength) {
        this.sessionLength = sessionLength;
    }

    public long getTimeSpent() {
        return timeSpent;
    }

    public void setTimeSpent(long timeSpent) {
        this.timeSpent = timeSpent;
    }

    public long getLastInterval() {
        return lastInterval;
    }

    public void setLastInterval(long lastInterval) {
        this.lastInterval = lastInterval;
    }

    public String getDefaultTracker() {
        return defaultTracker;
    }

    public void setDefaultTracker(String defaultTracker) {
        this.defaultTracker = defaultTracker;
    }

    public String getReferrer() {
        return referrer;
    }

    public void setReferrer(String referrer) {
        this.referrer = referrer;
    }

    public int getEventCount() {
        return eventCount;
    }

    public void setEventCount(int eventCount) {
        this.eventCount = eventCount;
    }

    public String getEventToken() {
        return eventToken;
    }

    public void setEventToken(String eventToken) {
        this.eventToken = eventToken;
    }

    public double getAmountInCents() {
        return amountInCents;
    }

    public void setAmountInCents(double amountInCents) {
        this.amountInCents = amountInCents;
    }

    public Map<String, String> getCallbackParameters() {
        return callbackParameters;
    }

    public void setCallbackParameters(Map<String, String> callbackParameters) {
        this.callbackParameters = callbackParameters;
    }

    protected ActivityPackage buildSessionPackage() {
        Map<String, String> parameters = getDefaultParameters();
        addDuration(parameters, "last_interval", lastInterval);
        addString(parameters, "default_tracker", defaultTracker);
        addString(parameters, Constants.REFERRER, referrer);

        ActivityPackage sessionPackage = getDefaultActivityPackage();
        sessionPackage.setPath("/startup");
        sessionPackage.setKind("session start");
        sessionPackage.setSuffix("");
        sessionPackage.setParameters(parameters);

        return sessionPackage;
    }

    protected ActivityPackage buildEventPackage() {
        Map<String, String> parameters = getDefaultParameters();
        injectEventParameters(parameters);

        ActivityPackage eventPackage = getDefaultActivityPackage();
        eventPackage.setPath("/event");
        eventPackage.setKind("event");
        eventPackage.setSuffix(getEventSuffix());
        eventPackage.setParameters(parameters);

        return eventPackage;
    }

    protected ActivityPackage buildRevenuePackage() {
        Map<String, String> parameters = getDefaultParameters();
        injectEventParameters(parameters);
        addString(parameters, "amount", getAmountString());

        ActivityPackage revenuePackage = getDefaultActivityPackage();
        revenuePackage.setPath("/revenue");
        revenuePackage.setKind("revenue");
        revenuePackage.setSuffix(getRevenueSuffix());
        revenuePackage.setParameters(parameters);

        return revenuePackage;
    }

    private ActivityPackage getDefaultActivityPackage() {
        ActivityPackage activityPackage = new ActivityPackage();
        activityPackage.setUserAgent(userAgent);
        activityPackage.setClientSdk(clientSdk);
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
