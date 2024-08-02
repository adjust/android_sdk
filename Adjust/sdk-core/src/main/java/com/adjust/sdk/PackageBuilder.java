//
//  PackageBuilder.java
//  Adjust SDK
//
//  Created by Christian Wellenbrock (@wellle) on 25th June 2013.
//  Copyright (c) 2013-2018 Adjust GmbH. All rights reserved.
//

package com.adjust.sdk;

import java.util.Map;
import java.util.Date;
import java.util.HashMap;

import org.json.JSONObject;

import android.text.TextUtils;

public class PackageBuilder {
    private static ILogger logger = AdjustFactory.getLogger();
    private long createdAt;
    private DeviceInfo deviceInfo;
    private AdjustConfig adjustConfig;
    private ActivityStateCopy activityStateCopy;
    private GlobalParameters globalParameters;

    long clickTimeInSeconds = -1;
    long clickTimeInMilliseconds = -1;
    long installBeginTimeInSeconds = -1;
    long clickTimeServerInSeconds = -1;
    long installBeginTimeServerInSeconds = -1;
    String reftag;
    String deeplink;
    String referrer;
    String installVersion;
    String rawReferrer;
    String referrerApi;
    String preinstallPayload;
    String preinstallLocation;
    Boolean googlePlayInstant;
    AdjustAttribution attribution;
    Map<String, String> extraParameters;
    Boolean isClick;
    ActivityHandler.InternalState internalState;

    static class ActivityStateCopy {
        int eventCount = -1;
        int sessionCount = -1;
        int subsessionCount = -1;
        long timeSpent = -1;
        long sessionLength = -1;
        String uuid = null;
        String pushToken = null;

        ActivityStateCopy(ActivityState activityState) {
            if (activityState == null) {
                return;
            }
            this.eventCount = activityState.eventCount;
            this.sessionCount = activityState.sessionCount;
            this.subsessionCount = activityState.subsessionCount;
            this.timeSpent = activityState.timeSpent;
            this.sessionLength = activityState.sessionLength;
            this.uuid = activityState.uuid;
            this.pushToken = activityState.pushToken;
        }
    }

    PackageBuilder(AdjustConfig adjustConfig,
                   DeviceInfo deviceInfo,
                   ActivityState activityState,
                   GlobalParameters globalParameters,
                   long createdAt) {
        this.createdAt = createdAt;
        this.deviceInfo = deviceInfo;
        this.adjustConfig = adjustConfig;
        this.activityStateCopy = new ActivityStateCopy(activityState);
        this.globalParameters = globalParameters;
    }

    ActivityPackage buildSessionPackage() {
        Map<String, String> parameters = getSessionParameters();
        ActivityPackage sessionPackage = getDefaultActivityPackage(ActivityKind.SESSION);
        sessionPackage.setPath("/session");
        sessionPackage.setSuffix("");

        sessionPackage.setParameters(parameters);
        return sessionPackage;
    }

    ActivityPackage buildEventPackage(AdjustEvent event) {
        Map<String, String> parameters = getEventParameters(event);
        ActivityPackage eventPackage = getDefaultActivityPackage(ActivityKind.EVENT);
        eventPackage.setPath("/event");
        eventPackage.setSuffix(getEventSuffix(event));

        eventPackage.setParameters(parameters);

        eventPackage.setCallbackParameters(event.callbackParameters);
        eventPackage.setPartnerParameters(event.partnerParameters);

        return eventPackage;
    }

    ActivityPackage buildInfoPackage(String source) {
        Map<String, String> parameters = getInfoParameters(source);
        ActivityPackage infoPackage = getDefaultActivityPackage(ActivityKind.INFO);
        infoPackage.setPath("/sdk_info");
        infoPackage.setSuffix("");

        infoPackage.setParameters(parameters);
        return infoPackage;
    }

    ActivityPackage buildClickPackage(String source) {
        Map<String, String> parameters = getClickParameters(source);
        ActivityPackage clickPackage = getDefaultActivityPackage(ActivityKind.CLICK);
        clickPackage.setPath("/sdk_click");
        clickPackage.setSuffix("");
        clickPackage.setClickTimeInMilliseconds(clickTimeInMilliseconds);
        clickPackage.setClickTimeInSeconds(clickTimeInSeconds);
        clickPackage.setInstallBeginTimeInSeconds(installBeginTimeInSeconds);
        clickPackage.setClickTimeServerInSeconds(clickTimeServerInSeconds);
        clickPackage.setInstallBeginTimeServerInSeconds(installBeginTimeServerInSeconds);
        clickPackage.setInstallVersion(installVersion);
        clickPackage.setGooglePlayInstant(googlePlayInstant);
        clickPackage.setIsClick(isClick);

        clickPackage.setParameters(parameters);
        return clickPackage;
    }

    ActivityPackage buildAttributionPackage(String initiatedByDescription) {
        Map<String, String> parameters = getAttributionParameters(initiatedByDescription);
        ActivityPackage attributionPackage = getDefaultActivityPackage(ActivityKind.ATTRIBUTION);
        attributionPackage.setPath("attribution"); // does not contain '/' because of Uri.Builder.appendPath
        attributionPackage.setSuffix("");

        attributionPackage.setParameters(parameters);
        return attributionPackage;
    }

    ActivityPackage buildGdprPackage() {
        Map<String, String> parameters = getGdprParameters();
        ActivityPackage gdprPackage = getDefaultActivityPackage(ActivityKind.GDPR);
        gdprPackage.setPath("/gdpr_forget_device");
        gdprPackage.setSuffix("");

        gdprPackage.setParameters(parameters);
        return gdprPackage;
    }

    ActivityPackage buildThirdPartySharingPackage(
            final AdjustThirdPartySharing adjustThirdPartySharing)
    {
        Map<String, String> parameters = getThirdPartySharingParameters(adjustThirdPartySharing);
        ActivityPackage activityPackage = getDefaultActivityPackage(ActivityKind.THIRD_PARTY_SHARING);
        activityPackage.setPath("/third_party_sharing");
        activityPackage.setSuffix("");

        activityPackage.setParameters(parameters);
        return activityPackage;
    }

    ActivityPackage buildMeasurementConsentPackage(final boolean consentMeasurement) {
        Map<String, String> parameters = getMeasurementConsentParameters(consentMeasurement);
        ActivityPackage activityPackage =
                getDefaultActivityPackage(ActivityKind.MEASUREMENT_CONSENT);
        activityPackage.setPath("/measurement_consent");
        activityPackage.setSuffix("");

        activityPackage.setParameters(parameters);
        return activityPackage;
    }

    ActivityPackage buildAdRevenuePackage(AdjustAdRevenue adjustAdRevenue) {
        Map<String, String> parameters = getAdRevenueParameters(adjustAdRevenue);
        ActivityPackage adRevenuePackage = getDefaultActivityPackage(ActivityKind.AD_REVENUE);
        adRevenuePackage.setPath("/ad_revenue");
        adRevenuePackage.setSuffix("");

        adRevenuePackage.setParameters(parameters);

        adRevenuePackage.setCallbackParameters(adjustAdRevenue.callbackParameters);
        adRevenuePackage.setPartnerParameters(adjustAdRevenue.partnerParameters);

        return adRevenuePackage;
    }

    ActivityPackage buildSubscriptionPackage(AdjustPlayStoreSubscription subscription) {
        Map<String, String> parameters = getSubscriptionParameters(subscription);
        ActivityPackage subscriptionPackage = getDefaultActivityPackage(ActivityKind.SUBSCRIPTION);
        subscriptionPackage.setPath("/v2/purchase");
        subscriptionPackage.setSuffix("");

        subscriptionPackage.setParameters(parameters);
        return subscriptionPackage;
    }

    ActivityPackage buildVerificationPackage(AdjustPlayStorePurchase purchase, OnPurchaseVerificationFinishedListener callback) {
        Map<String, String> parameters = getVerificationParameters(purchase);
        ActivityPackage purchaseVerificationPackage = getDefaultActivityPackage(ActivityKind.PURCHASE_VERIFICATION);
        purchaseVerificationPackage.setPath("/verify");
        purchaseVerificationPackage.setSuffix("");
        purchaseVerificationPackage.setPurchaseVerificationCallback(callback);

        purchaseVerificationPackage.setParameters(parameters);
        return purchaseVerificationPackage;
    }

    ActivityPackage buildVerificationPackage(AdjustEvent event, OnPurchaseVerificationFinishedListener callback) {
        Map<String, String> parameters = getVerificationParameters(event);
        ActivityPackage purchaseVerificationPackage = getDefaultActivityPackage(ActivityKind.PURCHASE_VERIFICATION);
        purchaseVerificationPackage.setPath("/verify");
        purchaseVerificationPackage.setSuffix("");
        purchaseVerificationPackage.setPurchaseVerificationCallback(callback);

        purchaseVerificationPackage.setParameters(parameters);
        return purchaseVerificationPackage;
    }

    private Map<String, String> getSessionParameters() {
        Map<String, String> parameters = new HashMap<String, String>();

        deviceInfo.reloadOtherDeviceInfoParams(adjustConfig, logger);

        // Check if plugin is used and if yes, add read parameters.
        if (deviceInfo.imeiParameters != null) {
            parameters.putAll(deviceInfo.imeiParameters);
        }

        // Check if oaid plugin is used and if yes, add the parameter
        if (deviceInfo.oaidParameters != null) {
            parameters.putAll(deviceInfo.oaidParameters);
        }

        // Callback and partner parameters.
        PackageBuilder.addMapJson(parameters, "callback_params", this.globalParameters.callbackParameters);
        PackageBuilder.addMapJson(parameters, "partner_params", this.globalParameters.partnerParameters);

        // Device identifiers.
        deviceInfo.reloadPlayIds(adjustConfig);
        PackageBuilder.addString(parameters, "android_uuid", activityStateCopy.uuid);
        PackageBuilder.addString(parameters, "gps_adid", deviceInfo.playAdId);
        PackageBuilder.addLong(parameters, "gps_adid_attempt", deviceInfo.playAdIdAttempt);
        PackageBuilder.addString(parameters, "gps_adid_src", deviceInfo.playAdIdSource);
        PackageBuilder.addBoolean(parameters, "tracking_enabled", deviceInfo.isTrackingEnabled);
        PackageBuilder.addString(parameters, "fire_adid", deviceInfo.fireAdId);
        PackageBuilder.addBoolean(parameters, "fire_tracking_enabled", deviceInfo.fireTrackingEnabled);
        PackageBuilder.addString(parameters, "google_app_set_id", deviceInfo.appSetId);

        if (!containsPlayIds(parameters) && !containsFireIds(parameters)) {
            logger.warn("Google Advertising ID or Fire Advertising ID not detected, " +
                    "fallback to non Google Play and Fire identifiers will take place");
            deviceInfo.reloadNonPlayIds(adjustConfig);
            PackageBuilder.addString(parameters, "android_id", deviceInfo.androidId);
        }

        // Rest of the parameters.
        PackageBuilder.addString(parameters, "api_level", deviceInfo.apiLevel);
        PackageBuilder.addString(parameters, "app_token", adjustConfig.appToken);
        PackageBuilder.addString(parameters, "app_version", deviceInfo.appVersion);
        PackageBuilder.addBoolean(parameters, "attribution_deeplink", true);
        PackageBuilder.addLong(parameters, "connectivity_type", deviceInfo.connectivityType);
        PackageBuilder.addString(parameters, "country", deviceInfo.country);
        PackageBuilder.addString(parameters, "cpu_type", deviceInfo.abi);
        PackageBuilder.addDateInMilliseconds(parameters, "created_at", createdAt);
        PackageBuilder.addString(parameters, "default_tracker", adjustConfig.defaultTracker);
        PackageBuilder.addBoolean(parameters, "needs_cost", adjustConfig.isCostDataInAttributionEnabled);
        PackageBuilder.addString(parameters, "device_manufacturer", deviceInfo.deviceManufacturer);
        PackageBuilder.addString(parameters, "device_name", deviceInfo.deviceName);
        PackageBuilder.addString(parameters, "device_type", deviceInfo.deviceType);
        PackageBuilder.addLong(parameters, "ui_mode", deviceInfo.uiMode);
        PackageBuilder.addString(parameters, "display_height", deviceInfo.displayHeight);
        PackageBuilder.addString(parameters, "display_width", deviceInfo.displayWidth);
        PackageBuilder.addString(parameters, "environment", adjustConfig.environment);
        PackageBuilder.addString(parameters, "external_device_id", adjustConfig.externalDeviceId);
        PackageBuilder.addString(parameters, "fb_id", deviceInfo.fbAttributionId);
        PackageBuilder.addString(parameters, "hardware_name", deviceInfo.hardwareName);
        PackageBuilder.addString(parameters, "installed_at", deviceInfo.appInstallTime);
        PackageBuilder.addString(parameters, "language", deviceInfo.language);
        PackageBuilder.addString(parameters, "mcc", deviceInfo.mcc);
        PackageBuilder.addString(parameters, "mnc", deviceInfo.mnc);
        PackageBuilder.addBoolean(parameters, "needs_response_details", true);
        PackageBuilder.addString(parameters, "os_build", deviceInfo.buildName);
        PackageBuilder.addString(parameters, "os_name", deviceInfo.osName);
        PackageBuilder.addString(parameters, "os_version", deviceInfo.osVersion);
        PackageBuilder.addString(parameters, "package_name", deviceInfo.packageName);
        PackageBuilder.addString(parameters, "push_token", activityStateCopy.pushToken);
        PackageBuilder.addString(parameters, "screen_density", deviceInfo.screenDensity);
        PackageBuilder.addString(parameters, "screen_format", deviceInfo.screenFormat);
        PackageBuilder.addString(parameters, "screen_size", deviceInfo.screenSize);
        PackageBuilder.addLong(parameters, "session_count", activityStateCopy.sessionCount);
        PackageBuilder.addDuration(parameters, "session_length", activityStateCopy.sessionLength);
        PackageBuilder.addLong(parameters, "subsession_count", activityStateCopy.subsessionCount);
        PackageBuilder.addDuration(parameters, "time_spent", activityStateCopy.timeSpent);
        PackageBuilder.addString(parameters, "updated_at", deviceInfo.appUpdateTime);

        // google play games
        PackageBuilder.addBoolean(parameters, "gpg_pc_enabled", deviceInfo.isGooglePlayGamesForPC ? true : null);

        // control params json
        JSONObject controlParams = SharedPreferencesManager.getDefaultInstance(adjustConfig.context).getControlParamsJson();
        PackageBuilder.addJsonObject(parameters, "control_params", controlParams);

        injectFeatureFlagsWithParameters(parameters);

        checkDeviceIds(parameters);
        return parameters;
    }

    public Map<String, String> getEventParameters(AdjustEvent event) {
        Map<String, String> parameters = new HashMap<String, String>();

        deviceInfo.reloadOtherDeviceInfoParams(adjustConfig, logger);

        // Check if plugin is used and if yes, add read parameters.
        if (deviceInfo.imeiParameters != null) {
            parameters.putAll(deviceInfo.imeiParameters);
        }

        // Check if oaid plugin is used and if yes, add the parameter
        if (deviceInfo.oaidParameters != null) {
            parameters.putAll(deviceInfo.oaidParameters);
        }

        // Callback and partner parameters.
        PackageBuilder.addMapJson(parameters, "callback_params", Util.mergeParameters(this.globalParameters.callbackParameters, event.callbackParameters, "Callback"));
        PackageBuilder.addMapJson(parameters, "partner_params", Util.mergeParameters(this.globalParameters.partnerParameters, event.partnerParameters, "Partner"));

        // Device identifiers.
        deviceInfo.reloadPlayIds(adjustConfig);
        PackageBuilder.addString(parameters, "android_uuid", activityStateCopy.uuid);
        PackageBuilder.addString(parameters, "gps_adid", deviceInfo.playAdId);
        PackageBuilder.addLong(parameters, "gps_adid_attempt", deviceInfo.playAdIdAttempt);
        PackageBuilder.addString(parameters, "gps_adid_src", deviceInfo.playAdIdSource);
        PackageBuilder.addBoolean(parameters, "tracking_enabled", deviceInfo.isTrackingEnabled);
        PackageBuilder.addString(parameters, "fire_adid", deviceInfo.fireAdId);
        PackageBuilder.addBoolean(parameters, "fire_tracking_enabled", deviceInfo.fireTrackingEnabled);
        PackageBuilder.addString(parameters, "google_app_set_id", deviceInfo.appSetId);

        if (!containsPlayIds(parameters) && !containsFireIds(parameters)) {
            logger.warn("Google Advertising ID or Fire Advertising ID not detected, " +
                    "fallback to non Google Play and Fire identifiers will take place");
            deviceInfo.reloadNonPlayIds(adjustConfig);
            PackageBuilder.addString(parameters, "android_id", deviceInfo.androidId);
        }

        // Rest of the parameters.
        PackageBuilder.addString(parameters, "api_level", deviceInfo.apiLevel);
        PackageBuilder.addString(parameters, "app_token", adjustConfig.appToken);
        PackageBuilder.addString(parameters, "app_version", deviceInfo.appVersion);
        PackageBuilder.addBoolean(parameters, "attribution_deeplink", true);
        PackageBuilder.addLong(parameters, "connectivity_type", deviceInfo.connectivityType);
        PackageBuilder.addString(parameters, "country", deviceInfo.country);
        PackageBuilder.addString(parameters, "cpu_type", deviceInfo.abi);
        PackageBuilder.addDateInMilliseconds(parameters, "created_at", createdAt);
        PackageBuilder.addString(parameters, "currency", event.currency);
        PackageBuilder.addBoolean(parameters, "needs_cost", adjustConfig.isCostDataInAttributionEnabled);
        PackageBuilder.addString(parameters, "device_manufacturer", deviceInfo.deviceManufacturer);
        PackageBuilder.addString(parameters, "device_name", deviceInfo.deviceName);
        PackageBuilder.addString(parameters, "device_type", deviceInfo.deviceType);
        PackageBuilder.addLong(parameters, "ui_mode", deviceInfo.uiMode);
        PackageBuilder.addString(parameters, "display_height", deviceInfo.displayHeight);
        PackageBuilder.addString(parameters, "display_width", deviceInfo.displayWidth);
        PackageBuilder.addString(parameters, "environment", adjustConfig.environment);
        PackageBuilder.addString(parameters, "event_callback_id", event.callbackId);
        PackageBuilder.addLong(parameters, "event_count", activityStateCopy.eventCount);
        PackageBuilder.addString(parameters, "event_token", event.eventToken);
        PackageBuilder.addString(parameters, "external_device_id", adjustConfig.externalDeviceId);
        PackageBuilder.addString(parameters, "fb_id", deviceInfo.fbAttributionId);
        PackageBuilder.addString(parameters, "hardware_name", deviceInfo.hardwareName);
        PackageBuilder.addString(parameters, "language", deviceInfo.language);
        PackageBuilder.addString(parameters, "mcc", deviceInfo.mcc);
        PackageBuilder.addString(parameters, "mnc", deviceInfo.mnc);
        PackageBuilder.addBoolean(parameters, "needs_response_details", true);
        PackageBuilder.addString(parameters, "os_build", deviceInfo.buildName);
        PackageBuilder.addString(parameters, "os_name", deviceInfo.osName);
        PackageBuilder.addString(parameters, "os_version", deviceInfo.osVersion);
        PackageBuilder.addString(parameters, "package_name", deviceInfo.packageName);
        PackageBuilder.addString(parameters, "product_id", event.productId);
        PackageBuilder.addString(parameters, "purchase_token", event.purchaseToken);
        PackageBuilder.addString(parameters, "push_token", activityStateCopy.pushToken);
        PackageBuilder.addDouble(parameters, "revenue", event.revenue);
        PackageBuilder.addString(parameters, "order_id", event.orderId);
        PackageBuilder.addString(parameters, "deduplication_id", event.deduplicationId);
        PackageBuilder.addString(parameters, "screen_density", deviceInfo.screenDensity);
        PackageBuilder.addString(parameters, "screen_format", deviceInfo.screenFormat);
        PackageBuilder.addString(parameters, "screen_size", deviceInfo.screenSize);
        PackageBuilder.addLong(parameters, "session_count", activityStateCopy.sessionCount);
        PackageBuilder.addDuration(parameters, "session_length", activityStateCopy.sessionLength);
        PackageBuilder.addLong(parameters, "subsession_count", activityStateCopy.subsessionCount);
        PackageBuilder.addDuration(parameters, "time_spent", activityStateCopy.timeSpent);

        // google play games
        PackageBuilder.addBoolean(parameters, "gpg_pc_enabled", deviceInfo.isGooglePlayGamesForPC ? true : null);

        // control params json
        JSONObject controlParams = SharedPreferencesManager.getDefaultInstance(adjustConfig.context).getControlParamsJson();
        PackageBuilder.addJsonObject(parameters, "control_params", controlParams);

        injectFeatureFlagsWithParameters(parameters);

        checkDeviceIds(parameters);
        return parameters;
    }

    private Map<String, String> getInfoParameters(String source) {
        Map<String, String> parameters = new HashMap<String, String>();

        deviceInfo.reloadOtherDeviceInfoParams(adjustConfig, logger);

        // Check if plugin is used and if yes, add read parameters.
        if (deviceInfo.imeiParameters != null) {
            parameters.putAll(deviceInfo.imeiParameters);
        }

        // Check if oaid plugin is used and if yes, add the parameter
        if (deviceInfo.oaidParameters != null) {
            parameters.putAll(deviceInfo.oaidParameters);
        }

        // Device identifiers.
        deviceInfo.reloadPlayIds(adjustConfig);
        PackageBuilder.addString(parameters, "android_uuid", activityStateCopy.uuid);
        PackageBuilder.addString(parameters, "gps_adid", deviceInfo.playAdId);
        PackageBuilder.addLong(parameters, "gps_adid_attempt", deviceInfo.playAdIdAttempt);
        PackageBuilder.addString(parameters, "gps_adid_src", deviceInfo.playAdIdSource);
        PackageBuilder.addBoolean(parameters, "tracking_enabled", deviceInfo.isTrackingEnabled);
        PackageBuilder.addString(parameters, "fire_adid", deviceInfo.fireAdId);
        PackageBuilder.addBoolean(parameters, "fire_tracking_enabled", deviceInfo.fireTrackingEnabled);
        PackageBuilder.addString(parameters, "google_app_set_id", deviceInfo.appSetId);

        if (!containsPlayIds(parameters) && !containsFireIds(parameters)) {
            logger.warn("Google Advertising ID or Fire Advertising ID not detected, " +
                    "fallback to non Google Play and Fire identifiers will take place");
            deviceInfo.reloadNonPlayIds(adjustConfig);
            PackageBuilder.addString(parameters, "android_id", deviceInfo.androidId);
        }

        // Rest of the parameters.
        PackageBuilder.addString(parameters, "app_token", adjustConfig.appToken);
        PackageBuilder.addBoolean(parameters, "attribution_deeplink", true);
        PackageBuilder.addDateInMilliseconds(parameters, "created_at", createdAt);
        PackageBuilder.addBoolean(parameters, "needs_cost", adjustConfig.isCostDataInAttributionEnabled);
        PackageBuilder.addString(parameters, "environment", adjustConfig.environment);
        PackageBuilder.addString(parameters, "external_device_id", adjustConfig.externalDeviceId);
        PackageBuilder.addBoolean(parameters, "needs_response_details", true);
        PackageBuilder.addString(parameters, "push_token", activityStateCopy.pushToken);
        PackageBuilder.addString(parameters, "source", source);

        // google play games
        PackageBuilder.addBoolean(parameters, "gpg_pc_enabled", deviceInfo.isGooglePlayGamesForPC ? true : null);

        // control params json
        JSONObject controlParams = SharedPreferencesManager.getDefaultInstance(adjustConfig.context).getControlParamsJson();
        PackageBuilder.addJsonObject(parameters, "control_params", controlParams);

        injectFeatureFlagsWithParameters(parameters);

        checkDeviceIds(parameters);
        return parameters;
    }

    private Map<String, String> getClickParameters(String source) {
        Map<String, String> parameters = new HashMap<String, String>();

        deviceInfo.reloadOtherDeviceInfoParams(adjustConfig, logger);

        // Check if plugin is used and if yes, add read parameters.
        if (deviceInfo.imeiParameters != null) {
            parameters.putAll(deviceInfo.imeiParameters);
        }

        // Check if oaid plugin is used and if yes, add the parameter
        if (deviceInfo.oaidParameters != null) {
            parameters.putAll(deviceInfo.oaidParameters);
        }

        // Device identifiers.
        deviceInfo.reloadPlayIds(adjustConfig);
        PackageBuilder.addString(parameters, "android_uuid", activityStateCopy.uuid);
        PackageBuilder.addString(parameters, "gps_adid", deviceInfo.playAdId);
        PackageBuilder.addLong(parameters, "gps_adid_attempt", deviceInfo.playAdIdAttempt);
        PackageBuilder.addString(parameters, "gps_adid_src", deviceInfo.playAdIdSource);
        PackageBuilder.addBoolean(parameters, "tracking_enabled", deviceInfo.isTrackingEnabled);
        PackageBuilder.addString(parameters, "fire_adid", deviceInfo.fireAdId);
        PackageBuilder.addBoolean(parameters, "fire_tracking_enabled", deviceInfo.fireTrackingEnabled);
        PackageBuilder.addString(parameters, "google_app_set_id", deviceInfo.appSetId);

        if (!containsPlayIds(parameters) && !containsFireIds(parameters)) {
            logger.warn("Google Advertising ID or Fire Advertising ID not detected, " +
                    "fallback to non Google Play and Fire identifiers will take place");
            deviceInfo.reloadNonPlayIds(adjustConfig);
            PackageBuilder.addString(parameters, "android_id", deviceInfo.androidId);
        }

        // Attribution parameters.
        if (attribution != null) {
            PackageBuilder.addString(parameters, "tracker", attribution.trackerName);
            PackageBuilder.addString(parameters, "campaign", attribution.campaign);
            PackageBuilder.addString(parameters, "adgroup", attribution.adgroup);
            PackageBuilder.addString(parameters, "creative", attribution.creative);
        }

        // Rest of the parameters.
        PackageBuilder.addString(parameters, "api_level", deviceInfo.apiLevel);
        PackageBuilder.addString(parameters, "app_token", adjustConfig.appToken);
        PackageBuilder.addString(parameters, "app_version", deviceInfo.appVersion);
        PackageBuilder.addBoolean(parameters, "attribution_deeplink", true);
        PackageBuilder.addMapJson(parameters, "callback_params", this.globalParameters.callbackParameters);
        PackageBuilder.addDateInMilliseconds(parameters, "click_time", clickTimeInMilliseconds);
        PackageBuilder.addDateInSeconds(parameters, "click_time", clickTimeInSeconds);
        PackageBuilder.addDateInSeconds(parameters, "click_time_server", clickTimeServerInSeconds);
        PackageBuilder.addLong(parameters, "connectivity_type", deviceInfo.connectivityType);
        PackageBuilder.addString(parameters, "country", deviceInfo.country);
        PackageBuilder.addString(parameters, "cpu_type", deviceInfo.abi);
        PackageBuilder.addDateInMilliseconds(parameters, "created_at", createdAt);
        PackageBuilder.addString(parameters, "deeplink", deeplink);
        PackageBuilder.addBoolean(parameters, "needs_cost", adjustConfig.isCostDataInAttributionEnabled);
        PackageBuilder.addString(parameters, "device_manufacturer", deviceInfo.deviceManufacturer);
        PackageBuilder.addString(parameters, "device_name", deviceInfo.deviceName);
        PackageBuilder.addString(parameters, "device_type", deviceInfo.deviceType);
        PackageBuilder.addLong(parameters, "ui_mode", deviceInfo.uiMode);
        PackageBuilder.addString(parameters, "display_height", deviceInfo.displayHeight);
        PackageBuilder.addString(parameters, "display_width", deviceInfo.displayWidth);
        PackageBuilder.addString(parameters, "environment", adjustConfig.environment);
        PackageBuilder.addString(parameters, "external_device_id", adjustConfig.externalDeviceId);
        PackageBuilder.addString(parameters, "fb_id", deviceInfo.fbAttributionId);
        PackageBuilder.addBoolean(parameters, "google_play_instant", googlePlayInstant);
        PackageBuilder.addBoolean(parameters, "is_click", isClick);
        PackageBuilder.addString(parameters, "hardware_name", deviceInfo.hardwareName);
        PackageBuilder.addDateInSeconds(parameters, "install_begin_time", installBeginTimeInSeconds);
        PackageBuilder.addDateInSeconds(parameters, "install_begin_time_server", installBeginTimeServerInSeconds);
        PackageBuilder.addString(parameters, "install_version", installVersion);
        PackageBuilder.addString(parameters, "installed_at", deviceInfo.appInstallTime);
        PackageBuilder.addString(parameters, "language", deviceInfo.language);
        PackageBuilder.addString(parameters, "mcc", deviceInfo.mcc);
        PackageBuilder.addString(parameters, "mnc", deviceInfo.mnc);
        PackageBuilder.addBoolean(parameters, "needs_response_details", true);
        PackageBuilder.addString(parameters, "os_build", deviceInfo.buildName);
        PackageBuilder.addString(parameters, "os_name", deviceInfo.osName);
        PackageBuilder.addString(parameters, "os_version", deviceInfo.osVersion);
        PackageBuilder.addString(parameters, "package_name", deviceInfo.packageName);
        PackageBuilder.addMapJson(parameters, "params", extraParameters);
        PackageBuilder.addMapJson(parameters, "partner_params", this.globalParameters.partnerParameters);
        PackageBuilder.addString(parameters, "push_token", activityStateCopy.pushToken);
        PackageBuilder.addString(parameters, "raw_referrer", rawReferrer);
        PackageBuilder.addString(parameters, "referrer", referrer);
        PackageBuilder.addString(parameters, "referrer_api", referrerApi);
        PackageBuilder.addString(parameters, "reftag", reftag);
        PackageBuilder.addString(parameters, "screen_density", deviceInfo.screenDensity);
        PackageBuilder.addString(parameters, "screen_format", deviceInfo.screenFormat);
        PackageBuilder.addString(parameters, "screen_size", deviceInfo.screenSize);
        PackageBuilder.addLong(parameters, "session_count", activityStateCopy.sessionCount);
        PackageBuilder.addDuration(parameters, "session_length", activityStateCopy.sessionLength);
        PackageBuilder.addString(parameters, "source", source);
        PackageBuilder.addLong(parameters, "subsession_count", activityStateCopy.subsessionCount);
        PackageBuilder.addDuration(parameters, "time_spent", activityStateCopy.timeSpent);
        PackageBuilder.addString(parameters, "updated_at", deviceInfo.appUpdateTime);
        PackageBuilder.addString(parameters, "payload", preinstallPayload);
        PackageBuilder.addString(parameters, "found_location", preinstallLocation);

        // google play games
        PackageBuilder.addBoolean(parameters, "gpg_pc_enabled", deviceInfo.isGooglePlayGamesForPC ? true : null);

        // control params json
        JSONObject controlParams = SharedPreferencesManager.getDefaultInstance(adjustConfig.context).getControlParamsJson();
        PackageBuilder.addJsonObject(parameters, "control_params", controlParams);

        injectFeatureFlagsWithParameters(parameters);

        checkDeviceIds(parameters);
        return parameters;
    }

    private Map<String, String> getAttributionParameters(String initiatedBy) {
        Map<String, String> parameters = new HashMap<String, String>();

        deviceInfo.reloadOtherDeviceInfoParams(adjustConfig, logger);

        // Check if plugin is used and if yes, add read parameters.
        if (deviceInfo.imeiParameters != null) {
            parameters.putAll(deviceInfo.imeiParameters);
        }

        // Check if oaid plugin is used and if yes, add the parameter
        if (deviceInfo.oaidParameters != null) {
            parameters.putAll(deviceInfo.oaidParameters);
        }

        // Device identifiers.
        deviceInfo.reloadPlayIds(adjustConfig);
        PackageBuilder.addString(parameters, "android_uuid", activityStateCopy.uuid);
        PackageBuilder.addString(parameters, "gps_adid", deviceInfo.playAdId);
        PackageBuilder.addLong(parameters, "gps_adid_attempt", deviceInfo.playAdIdAttempt);
        PackageBuilder.addString(parameters, "gps_adid_src", deviceInfo.playAdIdSource);
        PackageBuilder.addBoolean(parameters, "tracking_enabled", deviceInfo.isTrackingEnabled);
        PackageBuilder.addString(parameters, "fire_adid", deviceInfo.fireAdId);
        PackageBuilder.addBoolean(parameters, "fire_tracking_enabled", deviceInfo.fireTrackingEnabled);
        PackageBuilder.addString(parameters, "google_app_set_id", deviceInfo.appSetId);

        if (!containsPlayIds(parameters) && !containsFireIds(parameters)) {
            logger.warn("Google Advertising ID or Fire Advertising ID not detected, " +
                    "fallback to non Google Play and Fire identifiers will take place");
            deviceInfo.reloadNonPlayIds(adjustConfig);
            PackageBuilder.addString(parameters, "android_id", deviceInfo.androidId);
        }

        // Rest of the parameters.
        PackageBuilder.addString(parameters, "api_level", deviceInfo.apiLevel);
        PackageBuilder.addString(parameters, "app_token", adjustConfig.appToken);
        PackageBuilder.addString(parameters, "app_version", deviceInfo.appVersion);
        PackageBuilder.addBoolean(parameters, "attribution_deeplink", true);
        PackageBuilder.addDateInMilliseconds(parameters, "created_at", createdAt);
        PackageBuilder.addBoolean(parameters, "needs_cost", adjustConfig.isCostDataInAttributionEnabled);
        PackageBuilder.addString(parameters, "device_name", deviceInfo.deviceName);
        PackageBuilder.addString(parameters, "device_type", deviceInfo.deviceType);
        PackageBuilder.addLong(parameters, "ui_mode", deviceInfo.uiMode);
        PackageBuilder.addString(parameters, "environment", adjustConfig.environment);
        PackageBuilder.addString(parameters, "external_device_id", adjustConfig.externalDeviceId);
        PackageBuilder.addString(parameters, "initiated_by", initiatedBy);
        PackageBuilder.addBoolean(parameters, "needs_response_details", true);
        PackageBuilder.addString(parameters, "os_name", deviceInfo.osName);
        PackageBuilder.addString(parameters, "os_version", deviceInfo.osVersion);
        PackageBuilder.addString(parameters, "package_name", deviceInfo.packageName);
        PackageBuilder.addString(parameters, "push_token", activityStateCopy.pushToken);

        // google play games
        PackageBuilder.addBoolean(parameters, "gpg_pc_enabled", deviceInfo.isGooglePlayGamesForPC ? true : null);

        // control params json
        JSONObject controlParams = SharedPreferencesManager.getDefaultInstance(adjustConfig.context).getControlParamsJson();
        PackageBuilder.addJsonObject(parameters, "control_params", controlParams);

        injectFeatureFlagsWithParameters(parameters);

        checkDeviceIds(parameters);
        return parameters;
    }

    private Map<String, String> getGdprParameters() {
        Map<String, String> parameters = new HashMap<String, String>();

        deviceInfo.reloadOtherDeviceInfoParams(adjustConfig, logger);

        // Check if plugin is used and if yes, add read parameters.
        if (deviceInfo.imeiParameters != null) {
            parameters.putAll(deviceInfo.imeiParameters);
        }

        // Check if oaid plugin is used and if yes, add the parameter
        if (deviceInfo.oaidParameters != null) {
            parameters.putAll(deviceInfo.oaidParameters);
        }

        // Device identifiers.
        deviceInfo.reloadPlayIds(adjustConfig);
        PackageBuilder.addString(parameters, "android_uuid", activityStateCopy.uuid);
        PackageBuilder.addString(parameters, "gps_adid", deviceInfo.playAdId);
        PackageBuilder.addLong(parameters, "gps_adid_attempt", deviceInfo.playAdIdAttempt);
        PackageBuilder.addString(parameters, "gps_adid_src", deviceInfo.playAdIdSource);
        PackageBuilder.addBoolean(parameters, "tracking_enabled", deviceInfo.isTrackingEnabled);
        PackageBuilder.addString(parameters, "fire_adid", deviceInfo.fireAdId);
        PackageBuilder.addBoolean(parameters, "fire_tracking_enabled", deviceInfo.fireTrackingEnabled);
        PackageBuilder.addString(parameters, "google_app_set_id", deviceInfo.appSetId);

        if (!containsPlayIds(parameters) && !containsFireIds(parameters)) {
            logger.warn("Google Advertising ID or Fire Advertising ID not detected, " +
                    "fallback to non Google Play and Fire identifiers will take place");
            deviceInfo.reloadNonPlayIds(adjustConfig);
            PackageBuilder.addString(parameters, "android_id", deviceInfo.androidId);
        }

        // Rest of the parameters.
        PackageBuilder.addString(parameters, "api_level", deviceInfo.apiLevel);
        PackageBuilder.addString(parameters, "app_token", adjustConfig.appToken);
        PackageBuilder.addString(parameters, "app_version", deviceInfo.appVersion);
        PackageBuilder.addBoolean(parameters, "attribution_deeplink", true);
        PackageBuilder.addDateInMilliseconds(parameters, "created_at", createdAt);
        PackageBuilder.addBoolean(parameters, "needs_cost", adjustConfig.isCostDataInAttributionEnabled);
        PackageBuilder.addString(parameters, "device_name", deviceInfo.deviceName);
        PackageBuilder.addString(parameters, "device_type", deviceInfo.deviceType);
        PackageBuilder.addLong(parameters, "ui_mode", deviceInfo.uiMode);
        PackageBuilder.addString(parameters, "environment", adjustConfig.environment);
        PackageBuilder.addString(parameters, "external_device_id", adjustConfig.externalDeviceId);
        PackageBuilder.addBoolean(parameters, "needs_response_details", true);
        PackageBuilder.addString(parameters, "os_name", deviceInfo.osName);
        PackageBuilder.addString(parameters, "os_version", deviceInfo.osVersion);
        PackageBuilder.addString(parameters, "package_name", deviceInfo.packageName);
        PackageBuilder.addString(parameters, "push_token", activityStateCopy.pushToken);

        // google play games
        PackageBuilder.addBoolean(parameters, "gpg_pc_enabled", deviceInfo.isGooglePlayGamesForPC ? true : null);

        // control params json
        JSONObject controlParams = SharedPreferencesManager.getDefaultInstance(adjustConfig.context).getControlParamsJson();
        PackageBuilder.addJsonObject(parameters, "control_params", controlParams);

        injectFeatureFlagsWithParameters(parameters);

        checkDeviceIds(parameters);
        return parameters;
    }

    private Map<String, String> getThirdPartySharingParameters
            (final AdjustThirdPartySharing adjustThirdPartySharing)
    {
        Map<String, String> parameters = new HashMap<String, String>();

        deviceInfo.reloadOtherDeviceInfoParams(adjustConfig, logger);

        // Check if plugin is used and if yes, add read parameters.
        if (deviceInfo.imeiParameters != null) {
            parameters.putAll(deviceInfo.imeiParameters);
        }

        // Check if oaid plugin is used and if yes, add the parameter
        if (deviceInfo.oaidParameters != null) {
            parameters.putAll(deviceInfo.oaidParameters);
        }

        // Third Party Sharing
        if (adjustThirdPartySharing.isEnabled != null) {
            PackageBuilder.addString(parameters, "sharing",
                    adjustThirdPartySharing.isEnabled.booleanValue() ?
                            "enable" : "disable");
        }
        PackageBuilder.addMapJson(parameters, "granular_third_party_sharing_options",
                adjustThirdPartySharing.granularOptions);

        PackageBuilder.addMapJson(parameters, "partner_sharing_settings",
                adjustThirdPartySharing.partnerSharingSettings);

        // Device identifiers.
        deviceInfo.reloadPlayIds(adjustConfig);
        PackageBuilder.addString(parameters, "android_uuid", activityStateCopy.uuid);
        PackageBuilder.addString(parameters, "gps_adid", deviceInfo.playAdId);
        PackageBuilder.addLong(parameters, "gps_adid_attempt", deviceInfo.playAdIdAttempt);
        PackageBuilder.addString(parameters, "gps_adid_src", deviceInfo.playAdIdSource);
        PackageBuilder.addBoolean(parameters, "tracking_enabled", deviceInfo.isTrackingEnabled);
        PackageBuilder.addString(parameters, "fire_adid", deviceInfo.fireAdId);
        PackageBuilder.addBoolean(parameters, "fire_tracking_enabled", deviceInfo.fireTrackingEnabled);
        PackageBuilder.addString(parameters, "google_app_set_id", deviceInfo.appSetId);

        if (!containsPlayIds(parameters) && !containsFireIds(parameters)) {
            logger.warn("Google Advertising ID or Fire Advertising ID not detected, " +
                    "fallback to non Google Play and Fire identifiers will take place");
            deviceInfo.reloadNonPlayIds(adjustConfig);
            PackageBuilder.addString(parameters, "android_id", deviceInfo.androidId);
        }

        // Rest of the parameters.
        PackageBuilder.addString(parameters, "api_level", deviceInfo.apiLevel);
        PackageBuilder.addString(parameters, "app_token", adjustConfig.appToken);
        PackageBuilder.addString(parameters, "app_version", deviceInfo.appVersion);
        PackageBuilder.addBoolean(parameters, "attribution_deeplink", true);
        PackageBuilder.addDateInMilliseconds(parameters, "created_at", createdAt);
        PackageBuilder.addString(parameters, "device_name", deviceInfo.deviceName);
        PackageBuilder.addString(parameters, "device_type", deviceInfo.deviceType);
        PackageBuilder.addLong(parameters, "ui_mode", deviceInfo.uiMode);
        PackageBuilder.addString(parameters, "environment", adjustConfig.environment);
        PackageBuilder.addString(parameters, "external_device_id", adjustConfig.externalDeviceId);
        PackageBuilder.addBoolean(parameters, "needs_response_details", true);
        PackageBuilder.addString(parameters, "os_name", deviceInfo.osName);
        PackageBuilder.addString(parameters, "os_version", deviceInfo.osVersion);
        PackageBuilder.addString(parameters, "package_name", deviceInfo.packageName);
        PackageBuilder.addString(parameters, "push_token", activityStateCopy.pushToken);

        // google play games
        PackageBuilder.addBoolean(parameters, "gpg_pc_enabled", deviceInfo.isGooglePlayGamesForPC ? true : null);

        // control params json
        JSONObject controlParams = SharedPreferencesManager.getDefaultInstance(adjustConfig.context).getControlParamsJson();
        PackageBuilder.addJsonObject(parameters, "control_params", controlParams);

        injectFeatureFlagsWithParameters(parameters);

        checkDeviceIds(parameters);
        return parameters;
    }

    private Map<String, String> getMeasurementConsentParameters(
            final boolean consentMeasurement)
    {
        Map<String, String> parameters = new HashMap<String, String>();

        deviceInfo.reloadOtherDeviceInfoParams(adjustConfig, logger);

        // Check if plugin is used and if yes, add read parameters.
        if (deviceInfo.imeiParameters != null) {
            parameters.putAll(deviceInfo.imeiParameters);
        }

        // Check if oaid plugin is used and if yes, add the parameter
        if (deviceInfo.oaidParameters != null) {
            parameters.putAll(deviceInfo.oaidParameters);
        }

        // Measurement Consent
        PackageBuilder.addString(parameters, "measurement",
                consentMeasurement ? "enable" : "disable");

        // Device identifiers.
        deviceInfo.reloadPlayIds(adjustConfig);
        PackageBuilder.addString(parameters, "android_uuid", activityStateCopy.uuid);
        PackageBuilder.addString(parameters, "gps_adid", deviceInfo.playAdId);
        PackageBuilder.addLong(parameters, "gps_adid_attempt", deviceInfo.playAdIdAttempt);
        PackageBuilder.addString(parameters, "gps_adid_src", deviceInfo.playAdIdSource);
        PackageBuilder.addBoolean(parameters, "tracking_enabled", deviceInfo.isTrackingEnabled);
        PackageBuilder.addString(parameters, "fire_adid", deviceInfo.fireAdId);
        PackageBuilder.addBoolean(parameters, "fire_tracking_enabled", deviceInfo.fireTrackingEnabled);
        PackageBuilder.addString(parameters, "google_app_set_id", deviceInfo.appSetId);

        if (!containsPlayIds(parameters) && !containsFireIds(parameters)) {
            logger.warn("Google Advertising ID or Fire Advertising ID not detected, " +
                    "fallback to non Google Play and Fire identifiers will take place");
            deviceInfo.reloadNonPlayIds(adjustConfig);
            PackageBuilder.addString(parameters, "android_id", deviceInfo.androidId);
        }

        // Rest of the parameters.
        PackageBuilder.addString(parameters, "api_level", deviceInfo.apiLevel);
        PackageBuilder.addString(parameters, "app_token", adjustConfig.appToken);
        PackageBuilder.addString(parameters, "app_version", deviceInfo.appVersion);
        PackageBuilder.addBoolean(parameters, "attribution_deeplink", true);
        PackageBuilder.addDateInMilliseconds(parameters, "created_at", createdAt);
        PackageBuilder.addString(parameters, "device_name", deviceInfo.deviceName);
        PackageBuilder.addString(parameters, "device_type", deviceInfo.deviceType);
        PackageBuilder.addLong(parameters, "ui_mode", deviceInfo.uiMode);
        PackageBuilder.addString(parameters, "environment", adjustConfig.environment);
        PackageBuilder.addString(parameters, "external_device_id", adjustConfig.externalDeviceId);
        PackageBuilder.addBoolean(parameters, "needs_response_details", true);
        PackageBuilder.addString(parameters, "os_name", deviceInfo.osName);
        PackageBuilder.addString(parameters, "os_version", deviceInfo.osVersion);
        PackageBuilder.addString(parameters, "package_name", deviceInfo.packageName);
        PackageBuilder.addString(parameters, "push_token", activityStateCopy.pushToken);

        // google play games
        PackageBuilder.addBoolean(parameters, "gpg_pc_enabled", deviceInfo.isGooglePlayGamesForPC ? true : null);

        // control params json
        JSONObject controlParams = SharedPreferencesManager.getDefaultInstance(adjustConfig.context).getControlParamsJson();
        PackageBuilder.addJsonObject(parameters, "control_params", controlParams);

        injectFeatureFlagsWithParameters(parameters);

        checkDeviceIds(parameters);
        return parameters;
    }

    private Map<String, String> getAdRevenueParameters(AdjustAdRevenue adjustAdRevenue) {
        Map<String, String> parameters = new HashMap<String, String>();

        deviceInfo.reloadOtherDeviceInfoParams(adjustConfig, logger);

        // Check if plugin is used and if yes, add read parameters.
        if (deviceInfo.imeiParameters != null) {
            parameters.putAll(deviceInfo.imeiParameters);
        }

        // Check if oaid plugin is used and if yes, add the parameter
        if (deviceInfo.oaidParameters != null) {
            parameters.putAll(deviceInfo.oaidParameters);
        }

        // Callback and partner parameters.
        PackageBuilder.addMapJson(parameters, "callback_params", Util.mergeParameters(this.globalParameters.callbackParameters, adjustAdRevenue.callbackParameters, "Callback"));
        PackageBuilder.addMapJson(parameters, "partner_params", Util.mergeParameters(this.globalParameters.partnerParameters, adjustAdRevenue.partnerParameters, "Partner"));

        // Device identifiers.
        deviceInfo.reloadPlayIds(adjustConfig);
        PackageBuilder.addString(parameters, "android_uuid", activityStateCopy.uuid);
        PackageBuilder.addString(parameters, "gps_adid", deviceInfo.playAdId);
        PackageBuilder.addLong(parameters, "gps_adid_attempt", deviceInfo.playAdIdAttempt);
        PackageBuilder.addString(parameters, "gps_adid_src", deviceInfo.playAdIdSource);
        PackageBuilder.addBoolean(parameters, "tracking_enabled", deviceInfo.isTrackingEnabled);
        PackageBuilder.addString(parameters, "fire_adid", deviceInfo.fireAdId);
        PackageBuilder.addBoolean(parameters, "fire_tracking_enabled", deviceInfo.fireTrackingEnabled);
        PackageBuilder.addString(parameters, "google_app_set_id", deviceInfo.appSetId);

        if (!containsPlayIds(parameters) && !containsFireIds(parameters)) {
            logger.warn("Google Advertising ID or Fire Advertising ID not detected, " +
                        "fallback to non Google Play and Fire identifiers will take place");
            deviceInfo.reloadNonPlayIds(adjustConfig);
            PackageBuilder.addString(parameters, "android_id", deviceInfo.androidId);
        }

        // Rest of the parameters.
        PackageBuilder.addString(parameters, "api_level", deviceInfo.apiLevel);
        PackageBuilder.addString(parameters, "app_token", adjustConfig.appToken);
        PackageBuilder.addString(parameters, "app_version", deviceInfo.appVersion);
        PackageBuilder.addBoolean(parameters, "attribution_deeplink", true);
        PackageBuilder.addLong(parameters, "connectivity_type", deviceInfo.connectivityType);
        PackageBuilder.addString(parameters, "country", deviceInfo.country);
        PackageBuilder.addString(parameters, "cpu_type", deviceInfo.abi);
        PackageBuilder.addDateInMilliseconds(parameters, "created_at", createdAt);
        PackageBuilder.addString(parameters, "default_tracker", adjustConfig.defaultTracker);
        PackageBuilder.addBoolean(parameters, "needs_cost", adjustConfig.isCostDataInAttributionEnabled);
        PackageBuilder.addString(parameters, "device_manufacturer", deviceInfo.deviceManufacturer);
        PackageBuilder.addString(parameters, "device_name", deviceInfo.deviceName);
        PackageBuilder.addString(parameters, "device_type", deviceInfo.deviceType);
        PackageBuilder.addLong(parameters, "ui_mode", deviceInfo.uiMode);
        PackageBuilder.addString(parameters, "display_height", deviceInfo.displayHeight);
        PackageBuilder.addString(parameters, "display_width", deviceInfo.displayWidth);
        PackageBuilder.addString(parameters, "environment", adjustConfig.environment);
        PackageBuilder.addString(parameters, "external_device_id", adjustConfig.externalDeviceId);
        PackageBuilder.addString(parameters, "fb_id", deviceInfo.fbAttributionId);
        PackageBuilder.addString(parameters, "hardware_name", deviceInfo.hardwareName);
        PackageBuilder.addString(parameters, "installed_at", deviceInfo.appInstallTime);
        PackageBuilder.addString(parameters, "language", deviceInfo.language);
        PackageBuilder.addString(parameters, "mcc", deviceInfo.mcc);
        PackageBuilder.addString(parameters, "mnc", deviceInfo.mnc);
        PackageBuilder.addBoolean(parameters, "needs_response_details", true);
        PackageBuilder.addString(parameters, "os_build", deviceInfo.buildName);
        PackageBuilder.addString(parameters, "os_name", deviceInfo.osName);
        PackageBuilder.addString(parameters, "os_version", deviceInfo.osVersion);
        PackageBuilder.addString(parameters, "package_name", deviceInfo.packageName);
        PackageBuilder.addString(parameters, "push_token", activityStateCopy.pushToken);
        PackageBuilder.addString(parameters, "screen_density", deviceInfo.screenDensity);
        PackageBuilder.addString(parameters, "screen_format", deviceInfo.screenFormat);
        PackageBuilder.addString(parameters, "screen_size", deviceInfo.screenSize);
        PackageBuilder.addString(parameters, "source", adjustAdRevenue.source);
        PackageBuilder.addDoubleWithoutRounding(parameters, "revenue", adjustAdRevenue.revenue);
        PackageBuilder.addString(parameters, "currency", adjustAdRevenue.currency);
        PackageBuilder.addInteger(parameters, "ad_impressions_count", adjustAdRevenue.adImpressionsCount);
        PackageBuilder.addString(parameters, "ad_revenue_network", adjustAdRevenue.adRevenueNetwork);
        PackageBuilder.addString(parameters, "ad_revenue_unit", adjustAdRevenue.adRevenueUnit);
        PackageBuilder.addString(parameters, "ad_revenue_placement", adjustAdRevenue.adRevenuePlacement);
        PackageBuilder.addLong(parameters, "session_count", activityStateCopy.sessionCount);
        PackageBuilder.addDuration(parameters, "session_length", activityStateCopy.sessionLength);
        PackageBuilder.addLong(parameters, "subsession_count", activityStateCopy.subsessionCount);
        PackageBuilder.addDuration(parameters, "time_spent", activityStateCopy.timeSpent);
        PackageBuilder.addString(parameters, "updated_at", deviceInfo.appUpdateTime);

        // google play games
        PackageBuilder.addBoolean(parameters, "gpg_pc_enabled", deviceInfo.isGooglePlayGamesForPC ? true : null);

        // control params json
        JSONObject controlParams = SharedPreferencesManager.getDefaultInstance(adjustConfig.context).getControlParamsJson();
        PackageBuilder.addJsonObject(parameters, "control_params", controlParams);

        injectFeatureFlagsWithParameters(parameters);

        checkDeviceIds(parameters);
        return parameters;
    }

    private Map<String, String> getSubscriptionParameters(AdjustPlayStoreSubscription subscription) {
        Map<String, String> parameters = new HashMap<String, String>();

        deviceInfo.reloadOtherDeviceInfoParams(adjustConfig, logger);

        // Check if plugin is used and if yes, add read parameters.
        if (deviceInfo.imeiParameters != null) {
            parameters.putAll(deviceInfo.imeiParameters);
        }

        // Check if oaid plugin is used and if yes, add the parameter
        if (deviceInfo.oaidParameters != null) {
            parameters.putAll(deviceInfo.oaidParameters);
        }

        // Device identifiers.
        deviceInfo.reloadPlayIds(adjustConfig);
        PackageBuilder.addString(parameters, "android_uuid", activityStateCopy.uuid);
        PackageBuilder.addString(parameters, "gps_adid", deviceInfo.playAdId);
        PackageBuilder.addLong(parameters, "gps_adid_attempt", deviceInfo.playAdIdAttempt);
        PackageBuilder.addString(parameters, "gps_adid_src", deviceInfo.playAdIdSource);
        PackageBuilder.addBoolean(parameters, "tracking_enabled", deviceInfo.isTrackingEnabled);
        PackageBuilder.addString(parameters, "fire_adid", deviceInfo.fireAdId);
        PackageBuilder.addBoolean(parameters, "fire_tracking_enabled", deviceInfo.fireTrackingEnabled);
        PackageBuilder.addString(parameters, "google_app_set_id", deviceInfo.appSetId);

        if (!containsPlayIds(parameters) && !containsFireIds(parameters)) {
            logger.warn("Google Advertising ID or Fire Advertising ID not detected, " +
                    "fallback to non Google Play and Fire identifiers will take place");
            deviceInfo.reloadNonPlayIds(adjustConfig);
            PackageBuilder.addString(parameters, "android_id", deviceInfo.androidId);
        }

        // Callback and partner parameters.
        PackageBuilder.addMapJson(parameters, "callback_params", Util.mergeParameters(this.globalParameters.callbackParameters, subscription.getCallbackParameters(), "Callback"));
        PackageBuilder.addMapJson(parameters, "partner_params", Util.mergeParameters(this.globalParameters.partnerParameters, subscription.getPartnerParameters(), "Partner"));

        // Rest of the parameters.
        PackageBuilder.addString(parameters, "api_level", deviceInfo.apiLevel);
        PackageBuilder.addString(parameters, "app_token", adjustConfig.appToken);
        PackageBuilder.addString(parameters, "app_version", deviceInfo.appVersion);
        PackageBuilder.addBoolean(parameters, "attribution_deeplink", true);
        PackageBuilder.addLong(parameters, "connectivity_type", deviceInfo.connectivityType);
        PackageBuilder.addString(parameters, "country", deviceInfo.country);
        PackageBuilder.addString(parameters, "cpu_type", deviceInfo.abi);
        PackageBuilder.addDateInMilliseconds(parameters, "created_at", createdAt);
        PackageBuilder.addString(parameters, "default_tracker", adjustConfig.defaultTracker);
        PackageBuilder.addBoolean(parameters, "needs_cost", adjustConfig.isCostDataInAttributionEnabled);
        PackageBuilder.addString(parameters, "device_manufacturer", deviceInfo.deviceManufacturer);
        PackageBuilder.addString(parameters, "device_name", deviceInfo.deviceName);
        PackageBuilder.addString(parameters, "device_type", deviceInfo.deviceType);
        PackageBuilder.addLong(parameters, "ui_mode", deviceInfo.uiMode);
        PackageBuilder.addString(parameters, "display_height", deviceInfo.displayHeight);
        PackageBuilder.addString(parameters, "display_width", deviceInfo.displayWidth);
        PackageBuilder.addString(parameters, "environment", adjustConfig.environment);
        PackageBuilder.addString(parameters, "external_device_id", adjustConfig.externalDeviceId);
        PackageBuilder.addString(parameters, "fb_id", deviceInfo.fbAttributionId);
        PackageBuilder.addString(parameters, "hardware_name", deviceInfo.hardwareName);
        PackageBuilder.addString(parameters, "installed_at", deviceInfo.appInstallTime);
        PackageBuilder.addString(parameters, "language", deviceInfo.language);
        PackageBuilder.addString(parameters, "mcc", deviceInfo.mcc);
        PackageBuilder.addString(parameters, "mnc", deviceInfo.mnc);
        PackageBuilder.addBoolean(parameters, "needs_response_details", true);
        PackageBuilder.addString(parameters, "os_build", deviceInfo.buildName);
        PackageBuilder.addString(parameters, "os_name", deviceInfo.osName);
        PackageBuilder.addString(parameters, "os_version", deviceInfo.osVersion);
        PackageBuilder.addString(parameters, "package_name", deviceInfo.packageName);
        PackageBuilder.addString(parameters, "push_token", activityStateCopy.pushToken);
        PackageBuilder.addString(parameters, "screen_density", deviceInfo.screenDensity);
        PackageBuilder.addString(parameters, "screen_format", deviceInfo.screenFormat);
        PackageBuilder.addString(parameters, "screen_size", deviceInfo.screenSize);
        PackageBuilder.addLong(parameters, "session_count", activityStateCopy.sessionCount);
        PackageBuilder.addDuration(parameters, "session_length", activityStateCopy.sessionLength);
        PackageBuilder.addLong(parameters, "subsession_count", activityStateCopy.subsessionCount);
        PackageBuilder.addDuration(parameters, "time_spent", activityStateCopy.timeSpent);
        PackageBuilder.addString(parameters, "updated_at", deviceInfo.appUpdateTime);

        // subscription specific parameters
        PackageBuilder.addString(parameters, "currency", subscription.getCurrency());
        PackageBuilder.addString(parameters, "product_id", subscription.getSku());
        PackageBuilder.addString(parameters, "purchase_token", subscription.getPurchaseToken());
        PackageBuilder.addString(parameters, "receipt", subscription.getSignature());
        PackageBuilder.addLong(parameters, "revenue", subscription.getPrice());
        PackageBuilder.addDateInMilliseconds(parameters, "transaction_date", subscription.getPurchaseTime());
        PackageBuilder.addString(parameters, "transaction_id", subscription.getOrderId());

        // google play games
        PackageBuilder.addBoolean(parameters, "gpg_pc_enabled", deviceInfo.isGooglePlayGamesForPC ? true : null);

        // control params json
        JSONObject controlParams = SharedPreferencesManager.getDefaultInstance(adjustConfig.context).getControlParamsJson();
        PackageBuilder.addJsonObject(parameters, "control_params", controlParams);

        injectFeatureFlagsWithParameters(parameters);

        checkDeviceIds(parameters);
        return parameters;
    }

    private Map<String, String> getVerificationParameters(AdjustPlayStorePurchase purchase) {
        Map<String, String> parameters = new HashMap<String, String>();

        deviceInfo.reloadOtherDeviceInfoParams(adjustConfig, logger);

        // Check if plugin is used and if yes, add read parameters.
        if (deviceInfo.imeiParameters != null) {
            parameters.putAll(deviceInfo.imeiParameters);
        }

        // Check if oaid plugin is used and if yes, add the parameter
        if (deviceInfo.oaidParameters != null) {
            parameters.putAll(deviceInfo.oaidParameters);
        }

        // Device identifiers.
        deviceInfo.reloadPlayIds(adjustConfig);
        PackageBuilder.addString(parameters, "android_uuid", activityStateCopy.uuid);
        PackageBuilder.addString(parameters, "gps_adid", deviceInfo.playAdId);
        PackageBuilder.addLong(parameters, "gps_adid_attempt", deviceInfo.playAdIdAttempt);
        PackageBuilder.addString(parameters, "gps_adid_src", deviceInfo.playAdIdSource);
        PackageBuilder.addBoolean(parameters, "tracking_enabled", deviceInfo.isTrackingEnabled);
        PackageBuilder.addString(parameters, "fire_adid", deviceInfo.fireAdId);
        PackageBuilder.addBoolean(parameters, "fire_tracking_enabled", deviceInfo.fireTrackingEnabled);

        if (!containsPlayIds(parameters) && !containsFireIds(parameters)) {
            logger.warn("Google Advertising ID or Fire Advertising ID not detected, " +
                    "fallback to non Google Play and Fire identifiers will take place");
            deviceInfo.reloadNonPlayIds(adjustConfig);
            PackageBuilder.addString(parameters, "android_id", deviceInfo.androidId);
        }

        // Rest of the parameters.
        PackageBuilder.addString(parameters, "api_level", deviceInfo.apiLevel);
        PackageBuilder.addString(parameters, "app_token", adjustConfig.appToken);
        PackageBuilder.addString(parameters, "app_version", deviceInfo.appVersion);
        PackageBuilder.addBoolean(parameters, "attribution_deeplink", true);
        PackageBuilder.addLong(parameters, "connectivity_type", deviceInfo.connectivityType);
        PackageBuilder.addString(parameters, "country", deviceInfo.country);
        PackageBuilder.addString(parameters, "cpu_type", deviceInfo.abi);
        PackageBuilder.addDateInMilliseconds(parameters, "created_at", createdAt);
        PackageBuilder.addString(parameters, "default_tracker", adjustConfig.defaultTracker);
        PackageBuilder.addBoolean(parameters, "needs_cost", adjustConfig.isCostDataInAttributionEnabled);
        PackageBuilder.addString(parameters, "device_manufacturer", deviceInfo.deviceManufacturer);
        PackageBuilder.addString(parameters, "device_name", deviceInfo.deviceName);
        PackageBuilder.addString(parameters, "device_type", deviceInfo.deviceType);
        PackageBuilder.addLong(parameters, "ui_mode", deviceInfo.uiMode);
        PackageBuilder.addString(parameters, "display_height", deviceInfo.displayHeight);
        PackageBuilder.addString(parameters, "display_width", deviceInfo.displayWidth);
        PackageBuilder.addString(parameters, "environment", adjustConfig.environment);
        PackageBuilder.addString(parameters, "external_device_id", adjustConfig.externalDeviceId);
        PackageBuilder.addString(parameters, "fb_id", deviceInfo.fbAttributionId);
        PackageBuilder.addString(parameters, "hardware_name", deviceInfo.hardwareName);
        PackageBuilder.addString(parameters, "installed_at", deviceInfo.appInstallTime);
        PackageBuilder.addString(parameters, "language", deviceInfo.language);
        PackageBuilder.addString(parameters, "mcc", deviceInfo.mcc);
        PackageBuilder.addString(parameters, "mnc", deviceInfo.mnc);
        PackageBuilder.addBoolean(parameters, "needs_response_details", true);
        PackageBuilder.addString(parameters, "os_build", deviceInfo.buildName);
        PackageBuilder.addString(parameters, "os_name", deviceInfo.osName);
        PackageBuilder.addString(parameters, "os_version", deviceInfo.osVersion);
        PackageBuilder.addString(parameters, "package_name", deviceInfo.packageName);
        PackageBuilder.addString(parameters, "push_token", activityStateCopy.pushToken);
        PackageBuilder.addString(parameters, "screen_density", deviceInfo.screenDensity);
        PackageBuilder.addString(parameters, "screen_format", deviceInfo.screenFormat);
        PackageBuilder.addString(parameters, "screen_size", deviceInfo.screenSize);
        PackageBuilder.addLong(parameters, "session_count", activityStateCopy.sessionCount);
        PackageBuilder.addDuration(parameters, "session_length", activityStateCopy.sessionLength);
        PackageBuilder.addLong(parameters, "subsession_count", activityStateCopy.subsessionCount);
        PackageBuilder.addDuration(parameters, "time_spent", activityStateCopy.timeSpent);
        PackageBuilder.addString(parameters, "updated_at", deviceInfo.appUpdateTime);

        // purchase verification specific parameters
        PackageBuilder.addString(parameters, "product_id", purchase.getProductId());
        PackageBuilder.addString(parameters, "purchase_token", purchase.getPurchaseToken());

        // google play games
        PackageBuilder.addBoolean(parameters, "gpg_pc_enabled", deviceInfo.isGooglePlayGamesForPC ? true : null);

        // control params json
        JSONObject controlParams = SharedPreferencesManager.getDefaultInstance(adjustConfig.context).getControlParamsJson();
        PackageBuilder.addJsonObject(parameters, "control_params", controlParams);

        injectFeatureFlagsWithParameters(parameters);

        checkDeviceIds(parameters);
        return parameters;
    }

    private Map<String, String> getVerificationParameters(AdjustEvent event) {
        Map<String, String> parameters = new HashMap<String, String>();

        deviceInfo.reloadOtherDeviceInfoParams(adjustConfig, logger);

        // Check if plugin is used and if yes, add read parameters.
        if (deviceInfo.imeiParameters != null) {
            parameters.putAll(deviceInfo.imeiParameters);
        }

        // Check if oaid plugin is used and if yes, add the parameter
        if (deviceInfo.oaidParameters != null) {
            parameters.putAll(deviceInfo.oaidParameters);
        }

        // Device identifiers.
        deviceInfo.reloadPlayIds(adjustConfig);
        PackageBuilder.addString(parameters, "android_uuid", activityStateCopy.uuid);
        PackageBuilder.addString(parameters, "gps_adid", deviceInfo.playAdId);
        PackageBuilder.addLong(parameters, "gps_adid_attempt", deviceInfo.playAdIdAttempt);
        PackageBuilder.addString(parameters, "gps_adid_src", deviceInfo.playAdIdSource);
        PackageBuilder.addBoolean(parameters, "tracking_enabled", deviceInfo.isTrackingEnabled);
        PackageBuilder.addString(parameters, "fire_adid", deviceInfo.fireAdId);
        PackageBuilder.addBoolean(parameters, "fire_tracking_enabled", deviceInfo.fireTrackingEnabled);

        if (!containsPlayIds(parameters) && !containsFireIds(parameters)) {
            logger.warn("Google Advertising ID or Fire Advertising ID not detected, " +
                    "fallback to non Google Play and Fire identifiers will take place");
            deviceInfo.reloadNonPlayIds(adjustConfig);
            PackageBuilder.addString(parameters, "android_id", deviceInfo.androidId);
        }

        // Rest of the parameters.
        PackageBuilder.addString(parameters, "api_level", deviceInfo.apiLevel);
        PackageBuilder.addString(parameters, "app_token", adjustConfig.appToken);
        PackageBuilder.addString(parameters, "app_version", deviceInfo.appVersion);
        PackageBuilder.addBoolean(parameters, "attribution_deeplink", true);
        PackageBuilder.addLong(parameters, "connectivity_type", deviceInfo.connectivityType);
        PackageBuilder.addString(parameters, "country", deviceInfo.country);
        PackageBuilder.addString(parameters, "cpu_type", deviceInfo.abi);
        PackageBuilder.addDateInMilliseconds(parameters, "created_at", createdAt);
        PackageBuilder.addString(parameters, "default_tracker", adjustConfig.defaultTracker);
        PackageBuilder.addBoolean(parameters, "needs_cost", adjustConfig.isCostDataInAttributionEnabled);
        PackageBuilder.addString(parameters, "device_manufacturer", deviceInfo.deviceManufacturer);
        PackageBuilder.addString(parameters, "device_name", deviceInfo.deviceName);
        PackageBuilder.addString(parameters, "device_type", deviceInfo.deviceType);
        PackageBuilder.addLong(parameters, "ui_mode", deviceInfo.uiMode);
        PackageBuilder.addString(parameters, "display_height", deviceInfo.displayHeight);
        PackageBuilder.addString(parameters, "display_width", deviceInfo.displayWidth);
        PackageBuilder.addString(parameters, "environment", adjustConfig.environment);
        PackageBuilder.addString(parameters, "external_device_id", adjustConfig.externalDeviceId);
        PackageBuilder.addString(parameters, "fb_id", deviceInfo.fbAttributionId);
        PackageBuilder.addString(parameters, "hardware_name", deviceInfo.hardwareName);
        PackageBuilder.addString(parameters, "installed_at", deviceInfo.appInstallTime);
        PackageBuilder.addString(parameters, "language", deviceInfo.language);
        PackageBuilder.addString(parameters, "mcc", deviceInfo.mcc);
        PackageBuilder.addString(parameters, "mnc", deviceInfo.mnc);
        PackageBuilder.addBoolean(parameters, "needs_response_details", true);
        PackageBuilder.addString(parameters, "os_build", deviceInfo.buildName);
        PackageBuilder.addString(parameters, "os_name", deviceInfo.osName);
        PackageBuilder.addString(parameters, "os_version", deviceInfo.osVersion);
        PackageBuilder.addString(parameters, "package_name", deviceInfo.packageName);
        PackageBuilder.addString(parameters, "push_token", activityStateCopy.pushToken);
        PackageBuilder.addString(parameters, "screen_density", deviceInfo.screenDensity);
        PackageBuilder.addString(parameters, "screen_format", deviceInfo.screenFormat);
        PackageBuilder.addString(parameters, "screen_size", deviceInfo.screenSize);
        PackageBuilder.addLong(parameters, "session_count", activityStateCopy.sessionCount);
        PackageBuilder.addDuration(parameters, "session_length", activityStateCopy.sessionLength);
        PackageBuilder.addLong(parameters, "subsession_count", activityStateCopy.subsessionCount);
        PackageBuilder.addDuration(parameters, "time_spent", activityStateCopy.timeSpent);
        PackageBuilder.addString(parameters, "updated_at", deviceInfo.appUpdateTime);

        // purchase verification specific parameters
        PackageBuilder.addString(parameters, "product_id", event.getProductId());
        PackageBuilder.addString(parameters, "purchase_token", event.getPurchaseToken());
        PackageBuilder.addString(parameters, "event_token", event.getEventToken());
        PackageBuilder.addString(parameters, "currency", event.getCurrency());
        PackageBuilder.addDouble(parameters, "revenue", event.getRevenue());

        // google play games
        PackageBuilder.addBoolean(parameters, "gpg_pc_enabled", deviceInfo.isGooglePlayGamesForPC ? true : null);

        injectFeatureFlagsWithParameters(parameters);

        checkDeviceIds(parameters);
        return parameters;
    }

    private ActivityPackage getDefaultActivityPackage(ActivityKind activityKind) {
        ActivityPackage activityPackage = new ActivityPackage(activityKind);
        activityPackage.setClientSdk(deviceInfo.clientSdk);
        return activityPackage;
    }

    private void injectFeatureFlagsWithParameters(Map<String, String> parameters) {
        if (adjustConfig.coppaComplianceEnabled) {
            PackageBuilder.addLong(parameters, "ff_coppa", 1);
        }

        if (internalState != null) {
            PackageBuilder.addBoolean(parameters, "offline_mode_enabled", internalState.offline);
            if (internalState.isInForeground()) {
                PackageBuilder.addBoolean(parameters, "foreground", true);
            } else {
                PackageBuilder.addBoolean(parameters, "background", true);
            }
        }
        if (adjustConfig.playStoreKidsComplianceEnabled) {
            PackageBuilder.addLong(parameters, "ff_play_store_kids_app", 1);
        }
    }

    public static void addString(Map<String, String> parameters, String key, String value) {
        if (TextUtils.isEmpty(value)) {
            return;
        }
        parameters.put(key, value);
    }

    public static void addBoolean(Map<String, String> parameters, String key, Boolean value) {
        if (value == null) {
            return;
        }
        int intValue = value ? 1 : 0;
        PackageBuilder.addLong(parameters, key, intValue);
    }

    static void addJsonObject(Map<String, String> parameters, String key, JSONObject jsonObject) {
        if (jsonObject == null) {
            return;
        }

        PackageBuilder.addString(parameters, key, jsonObject.toString());
    }

    static void addMapJson(Map<String, String> parameters, String key, Map map) {
        if (map == null) {
            return;
        }
        if (map.size() == 0) {
            return;
        }

        JSONObject jsonObject = new JSONObject(map);
        String jsonString = jsonObject.toString();
        PackageBuilder.addString(parameters, key, jsonString);
    }

    public static void addLong(Map<String, String> parameters, String key, long value) {
        if (value < 0) {
            return;
        }
        String valueString = Long.toString(value);
        PackageBuilder.addString(parameters, key, valueString);
    }

    public static void addDouble(Map<String, String> parameters, String key, double value) {
        if (value < 0.0) {
            return;
        }
        String valueString = Double.toString(value);
        PackageBuilder.addString(parameters, key, valueString);
    }

    private static void addDateInMilliseconds(Map<String, String> parameters, String key, long value) {
        if (value <= 0) {
            return;
        }
        Date date = new Date(value);
        PackageBuilder.addDate(parameters, key, date);
    }

    private static void addDateInSeconds(Map<String, String> parameters, String key, long value) {
        if (value <= 0) {
            return;
        }
        Date date = new Date(value * 1000);
        PackageBuilder.addDate(parameters, key, date);
    }

    private static void addDate(Map<String, String> parameters, String key, Date value) {
        if (value == null) {
            return;
        }
        String dateString = Util.dateFormatter.format(value);
        PackageBuilder.addString(parameters, key, dateString);
    }

    private static void addDuration(Map<String, String> parameters, String key, long durationInMilliSeconds) {
        if (durationInMilliSeconds < 0) {
            return;
        }
        long durationInSeconds = (durationInMilliSeconds + 500) / 1000;
        PackageBuilder.addLong(parameters, key, durationInSeconds);
    }

    private static void addDouble(Map<String, String> parameters, String key, Double value) {
        if (value == null) {
            return;
        }
        String doubleString = Util.formatString("%.5f", value);
        PackageBuilder.addString(parameters, key, doubleString);
    }

    private static void addDoubleWithoutRounding(Map<String, String> parameters, String key, Double value) {
        if (value == null) {
            return;
        }
        String doubleString = Double.toString(value);
        PackageBuilder.addString(parameters, key, doubleString);
    }

    private static void addInteger(Map<String, String> parameters, String key, Integer value) {
        if (value == null) {
            return;
        }
        String intString = Integer.toString(value);
        PackageBuilder.addString(parameters, key, intString);
    }

    private boolean containsPlayIds(Map<String, String> parameters) {
        if (parameters == null) {
            return false;
        }
        return parameters.containsKey("gps_adid");
    }

    private boolean containsFireIds(Map<String, String> parameters) {
        if (parameters == null) {
            return false;
        }
        return parameters.containsKey("fire_adid");
    }

    private void checkDeviceIds(Map<String, String> parameters) {
        if (parameters != null && !parameters.containsKey("android_id")
                && !parameters.containsKey("gps_adid")
                && !parameters.containsKey("fire_adid")
                && !parameters.containsKey("oaid")
                && !parameters.containsKey("imei")
                && !parameters.containsKey("meid")
                && !parameters.containsKey("device_id")
                && !parameters.containsKey("imeis")
                && !parameters.containsKey("meids")
                && !parameters.containsKey("device_ids")) {
            if (adjustConfig.coppaComplianceEnabled) {
                logger.info("Missing Device IDs. COPPA enabled.");
            } else if (adjustConfig.playStoreKidsComplianceEnabled) {
                logger.info("Missing Device IDs. Play store kids compliance enabled.");
            } else {
                logger.error("Missing Device IDs. Please check if Proguard is correctly set with Adjust SDK");
            }
        }
    }

    private String getEventSuffix(AdjustEvent event) {
        if (event.revenue == null) {
            return Util.formatString("'%s'", event.eventToken);
        } else {
            return Util.formatString("(%.5f %s, '%s')", event.revenue, event.currency, event.eventToken);
        }
    }
}
