package com.adjust.sdk;

import android.net.Uri;
import android.net.UrlQuerySanitizer;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by uerceg on 04.08.17.
 */

public class PackageFactory {
    private static final String ADJUST_PREFIX = "adjust_";

    public static ActivityPackage getSdkClickPackage(final String referrer,
                                                     final long clickTime,
                                                     final ActivityState activityState,
                                                     final AdjustConfig adjustConfig,
                                                     final DeviceInfo deviceInfo,
                                                     final SessionParameters sessionParameters) {
        if (referrer == null || referrer.length() == 0) {
            return null;
        }

        AdjustFactory.getLogger().verbose("Referrer to parse (%s)", referrer);

        UrlQuerySanitizer querySanitizer = new UrlQuerySanitizer();
        querySanitizer.setUnregisteredParameterValueSanitizer(UrlQuerySanitizer.getAllButNulLegal());
        querySanitizer.setAllowUnregisteredParamaters(true);
        querySanitizer.parseQuery(referrer);

        PackageBuilder clickPackageBuilder = queryStringClickPackageBuilder(
                querySanitizer.getParameterList(),
                activityState,
                adjustConfig,
                deviceInfo,
                sessionParameters);

        if (clickPackageBuilder == null) {
            return null;
        }

        clickPackageBuilder.referrer = referrer;
        clickPackageBuilder.clickTime = clickTime;

        ActivityPackage clickPackage = clickPackageBuilder.buildClickPackage(Constants.REFTAG);

        return clickPackage;
    }

    public static ActivityPackage getSdkClickPackage(final Uri url,
                                                     final long clickTime,
                                                     final ActivityState activityState,
                                                     final AdjustConfig adjustConfig,
                                                     final DeviceInfo deviceInfo,
                                                     final SessionParameters sessionParameters) {
        if (url == null) {
            return null;
        }

        String urlString = url.toString();

        if (urlString == null || urlString.length() == 0) {
            return null;
        }

        AdjustFactory.getLogger().verbose("Url to parse (%s)", url);

        UrlQuerySanitizer querySanitizer = new UrlQuerySanitizer();
        querySanitizer.setUnregisteredParameterValueSanitizer(UrlQuerySanitizer.getAllButNulLegal());
        querySanitizer.setAllowUnregisteredParamaters(true);
        querySanitizer.parseUrl(urlString);

        PackageBuilder clickPackageBuilder = queryStringClickPackageBuilder(
                querySanitizer.getParameterList(),
                activityState,
                adjustConfig,
                deviceInfo,
                sessionParameters);

        if (clickPackageBuilder == null) {
            return null;
        }

        clickPackageBuilder.deeplink = url.toString();
        clickPackageBuilder.clickTime = clickTime;

        ActivityPackage clickPackage = clickPackageBuilder.buildClickPackage(Constants.DEEPLINK);

        return clickPackage;
    }

    private static PackageBuilder queryStringClickPackageBuilder(
            final List<UrlQuerySanitizer.ParameterValuePair> queryList,
            final ActivityState activityState,
            final AdjustConfig adjustConfig,
            final DeviceInfo deviceInfo,
            final SessionParameters sessionParameters) {
        if (queryList == null) {
            return null;
        }

        Map<String, String> queryStringParameters = new LinkedHashMap<String, String>();
        AdjustAttribution queryStringAttribution = new AdjustAttribution();

        for (UrlQuerySanitizer.ParameterValuePair parameterValuePair : queryList) {
            readQueryString(
                    parameterValuePair.mParameter,
                    parameterValuePair.mValue,
                    queryStringParameters,
                    queryStringAttribution);
        }

        long now = System.currentTimeMillis();
        String reftag = queryStringParameters.remove(Constants.REFTAG);

        // Check if activity state != null
        // (referrer can be called before onResume)
        if (activityState != null) {
            long lastInterval = now - activityState.lastActivity;
            activityState.lastInterval = lastInterval;
        }

        PackageBuilder builder = new PackageBuilder(
                adjustConfig,
                deviceInfo,
                activityState,
                sessionParameters,
                now);

        builder.extraParameters = queryStringParameters;
        builder.attribution = queryStringAttribution;
        builder.reftag = reftag;

        return builder;
    }

    private static boolean readQueryString(final String key,
                                           final String value,
                                           final Map<String, String> extraParameters,
                                           AdjustAttribution queryStringAttribution) {
        if (key == null || value == null) {
            return false;
        }

        // Parameter key does not start with "adjust_" prefix.
        if (!key.startsWith(ADJUST_PREFIX)) {
            return false;
        }

        String keyWOutPrefix = key.substring(ADJUST_PREFIX.length());

        if (keyWOutPrefix.length() == 0) {
            return false;
        }

        if (value.length() == 0) {
            return false;
        }

        if (!tryToSetAttribution(queryStringAttribution, keyWOutPrefix, value)) {
            extraParameters.put(keyWOutPrefix, value);
        }

        return true;
    }

    private static boolean tryToSetAttribution(AdjustAttribution queryStringAttribution,
                                               final String key,
                                               final String value) {
        if (key.equals("tracker")) {
            queryStringAttribution.trackerName = value;
            return true;
        }

        if (key.equals("campaign")) {
            queryStringAttribution.campaign = value;
            return true;
        }

        if (key.equals("adgroup")) {
            queryStringAttribution.adgroup = value;
            return true;
        }

        if (key.equals("creative")) {
            queryStringAttribution.creative = value;
            return true;
        }

        return false;
    }
}
