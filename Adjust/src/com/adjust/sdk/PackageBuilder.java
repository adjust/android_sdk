//
//  PackageBuilder.java
//  Adjust
//
//  Created by Christian Wellenbrock on 2013-06-25.
//  Copyright (c) 2013 adjust GmbH. All rights reserved.
//  See the file MIT-LICENSE for copying permission.
//

package com.adjust.sdk;

import android.text.TextUtils;
import android.util.Base64;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

class PackageBuilder {

    private String defaultTracker;
    private String referrer;

    Event event;
    private AdjustConfig adjustConfig;
    private DeviceInfo deviceInfo;
    private ActivityState activityState;

    // reattributions
    private Map<String, String> deepLinkParameters;

    public PackageBuilder(AdjustConfig adjustConfig, DeviceInfo deviceInfo, ActivityState activityState) {
        this.adjustConfig = adjustConfig;
        this.deviceInfo = deviceInfo;
        this.activityState = activityState.clone();
    }

    public void setDefaultTracker(String defaultTracker) {
        this.defaultTracker = defaultTracker;
    }

    public void setReferrer(String referrer) {
        this.referrer = referrer;
    }

    public void setDeepLinkParameters(Map<String, String> deepLinkParameters) {
        this.deepLinkParameters = deepLinkParameters;
    }

    public ActivityPackage buildSessionPackage() {
        Map<String, String> parameters = getDefaultParameters();
        addDuration(parameters, "last_interval", activityState.lastInterval);
        addString(parameters, "default_tracker", defaultTracker);
        addString(parameters, Constants.REFERRER, referrer);

        ActivityPackage sessionPackage = getDefaultActivityPackage();
        sessionPackage.setPath("/startup");
        sessionPackage.setActivityKind(ActivityKind.SESSION);
        sessionPackage.setSuffix("");
        sessionPackage.setParameters(parameters);

        return sessionPackage;
    }

    public ActivityPackage buildEventPackage() {
        Map<String, String> parameters = getDefaultParameters();
        injectEventParameters(parameters);

        ActivityPackage eventPackage = getDefaultActivityPackage();
        eventPackage.setPath("/event");
        eventPackage.setActivityKind(ActivityKind.EVENT);
        eventPackage.setSuffix(getEventSuffix());
        eventPackage.setParameters(parameters);

        return eventPackage;
    }

    public ActivityPackage buildReattributionPackage() {
        Map<String, String> parameters = getDefaultParameters();
        addMapJson(parameters, "deeplink_parameters", deepLinkParameters);

        ActivityPackage reattributionPackage = getDefaultActivityPackage();
        reattributionPackage.setPath("/reattribute");
        reattributionPackage.setActivityKind(ActivityKind.REATTRIBUTION);
        reattributionPackage.setSuffix("");
        reattributionPackage.setParameters(parameters);

        return reattributionPackage;
    }

    private ActivityPackage getDefaultActivityPackage() {
        ActivityPackage activityPackage = new ActivityPackage();
        activityPackage.setUserAgent(deviceInfo.userAgent);
        activityPackage.setClientSdk(deviceInfo.clientSdk);
        return activityPackage;
    }

    private Map<String, String> getDefaultParameters() {
        Map<String, String> parameters = new HashMap<String, String>();

        // general
        addDate(parameters, "created_at", activityState.createdAt);
        addString(parameters, "app_token", adjustConfig.appToken);
        addString(parameters, "mac_sha1", deviceInfo.macSha1);
        addString(parameters, "mac_md5", deviceInfo.macShortMd5);
        addString(parameters, "android_id", deviceInfo.androidId);
        addString(parameters, "android_uuid", activityState.uuid);
        addString(parameters, "fb_id", deviceInfo.fbAttributionId);
        addString(parameters, "environment", adjustConfig.environment);
        String playAdId = Util.getPlayAdId(adjustConfig.context);
        addString(parameters, "gps_adid", playAdId);
        Boolean isTrackingEnabled = Util.isPlayTrackingEnabled(adjustConfig.context);
        addBoolean(parameters, "tracking_enabled", isTrackingEnabled);
        fillPluginKeys(parameters);
        checkDeviceIds(parameters);

        // session related (used for events as well)
        addInt(parameters, "session_count", activityState.sessionCount);
        addInt(parameters, "subsession_count", activityState.subsessionCount);
        addDuration(parameters, "session_length", activityState.sessionLength);
        addDuration(parameters, "time_spent", activityState.timeSpent);

        return parameters;
    }

    private void checkDeviceIds(Map<String, String> parameters) {
        if (!parameters.containsKey("mac_sha1")
            && !parameters.containsKey("mac_md5")
            && !parameters.containsKey("android_id")
            && !parameters.containsKey("gps_adid"))
        {
            Logger logger = AdjustFactory.getLogger();
            logger.error("Missing device id's. Please check if Proguard is correctly set with Adjust SDK");
        }
    }

    private void fillPluginKeys(Map<String, String> parameters) {
        if (deviceInfo.pluginKeys== null) {
            return;
        }

        for (Map.Entry<String, String> pluginEntry : deviceInfo.pluginKeys.entrySet()) {
            addString(parameters, pluginEntry.getKey(), pluginEntry.getValue());
        }
    }

    private void injectEventParameters(Map<String, String> parameters) {
        addInt(parameters, "event_count", activityState.eventCount);
        addString(parameters, "event_token", event.eventToken);
        addMapBase64(parameters, "params", event.callbackParameters);
    }

    private String getAmountString() {
        long amountInMillis = Math.round(1000 * event.revenue);
        event.revenue = amountInMillis / 1000.0; // now rounded to one decimal point
        return Long.toString(amountInMillis);
    }

    private String getEventSuffix() {
        return String.format(" '%s'", event.eventToken);
    }

    private String getRevenueSuffix() {
        if (event.revenue == null) {
            return String.format(" '%s'", event.eventToken);
        } else {
            return String.format(Locale.US, " (%.1f cent, '%s')", event.revenue, event.eventToken);
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

        String dateString = Util.dateFormat(value);
        addString(parameters, key, dateString);
    }

    private void addDuration(Map<String, String> parameters, String key, long durationInMilliSeconds) {
        if (durationInMilliSeconds < 0) {
            return;
        }

        long durationInSeconds = (durationInMilliSeconds + 500) / 1000;
        addInt(parameters, key, durationInSeconds);
    }

    private void addMapBase64(Map<String, String> parameters, String key, Map<String, String> map) {
        if (null == map) {
            return;
        }

        JSONObject jsonObject = new JSONObject(map);
        byte[] jsonBytes = jsonObject.toString().getBytes();
        String encodedMap = Base64.encodeToString(jsonBytes, Base64.NO_WRAP);

        addString(parameters, key, encodedMap);
    }

    private void addMapJson(Map<String, String> parameters, String key, Map<String, String> map) {
        if (null == map) {
            return;
        }

        JSONObject jsonObject = new JSONObject(map);
        String jsonString = jsonObject.toString();

        addString(parameters, key, jsonString);
    }

    private void addBoolean(Map<String, String> parameters, String key, Boolean value) {
        if (value == null) {
            return;
        }

        int intValue = value? 1 : 0;

        addInt(parameters, key, intValue);
    }
}
