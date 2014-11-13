package com.adjust.sdk;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;

import java.util.Locale;

import static com.adjust.sdk.Constants.HIGH;
import static com.adjust.sdk.Constants.LARGE;
import static com.adjust.sdk.Constants.LONG;
import static com.adjust.sdk.Constants.LOW;
import static com.adjust.sdk.Constants.MEDIUM;
import static com.adjust.sdk.Constants.NORMAL;
import static com.adjust.sdk.Constants.SMALL;
import static com.adjust.sdk.Constants.UNKNOWN;
import static com.adjust.sdk.Constants.XLARGE;

/**
 * Created by pfms on 07/11/14.
 */
class UserAgent {
    String packageName;
    String appVersion;
    String deviceType;
    String deviceName;
    String deviceManufacturer;
    String osName;
    String osVersion;
    String language;
    String country;
    String screenSize;
    String screenFormat;
    String screenDensity;
    String displayWidth;
    String displayHeight;
    String networkType;
    String networkSubtype;
    String simOperator;

    static UserAgent getUserAgent(final Context context) {
        final Resources resources = context.getResources();
        final DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        final Configuration configuration = resources.getConfiguration();
        final Locale locale = configuration.locale;
        final int screenLayout = configuration.screenLayout;
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        UserAgent userAgent = new UserAgent();
        userAgent.packageName = getPackageName(context);
        userAgent.appVersion = getAppVersion(context);
        userAgent.deviceType = getDeviceType(screenLayout);
        userAgent.deviceName = getDeviceName();
        userAgent.deviceManufacturer = getDeviceManufacturer();
        userAgent.osName = getOsName();
        userAgent.osVersion = getOsVersion();
        userAgent.language = getLanguage(locale);
        userAgent.country = getCountry(locale);
        userAgent.screenSize = getScreenSize(screenLayout);
        userAgent.screenFormat = getScreenFormat(screenLayout);
        userAgent.screenDensity = getScreenDensity(displayMetrics);
        userAgent.displayWidth = getDisplayWidth(displayMetrics);
        userAgent.displayHeight = getDisplayHeight(displayMetrics);
        userAgent.networkType = getNetworkType(networkInfo);
        userAgent.networkSubtype = getNetworkSubtype(networkInfo);
        userAgent.simOperator = getSimOperator(context);

        return userAgent;
    }

    private static String getPackageName(final Context context) {
        final String packageName = context.getPackageName();
        return Util.sanitizeString(packageName);
    }

    private static String getAppVersion(final Context context) {
        try {
            final PackageManager packageManager = context.getPackageManager();
            final String name = context.getPackageName();
            final PackageInfo info = packageManager.getPackageInfo(name, 0);
            final String versionName = info.versionName;
            return Util.sanitizeString(versionName);
        } catch (PackageManager.NameNotFoundException e) {
            return UNKNOWN;
        }
    }

    private static String getDeviceType(final int screenLayout) {
        int screenSize = screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;

        switch (screenSize) {
            case Configuration.SCREENLAYOUT_SIZE_SMALL:
            case Configuration.SCREENLAYOUT_SIZE_NORMAL:
                return "phone";
            case Configuration.SCREENLAYOUT_SIZE_LARGE:
            case 4:
                return "tablet";
            default:
                return UNKNOWN;
        }
    }

    private static String getDeviceName() {
        final String deviceName = Build.MODEL;
        return Util.sanitizeString(deviceName);
    }

    private static String getDeviceManufacturer() {
        final String deviceManufacturer = Build.MANUFACTURER;
        return Util.sanitizeString(deviceManufacturer);
    }

    private static String getOsName() {
        return "android";
    }

    private static String getOsVersion() {
        final String osVersion = "" + Build.VERSION.SDK_INT;
        return Util.sanitizeString(osVersion);
    }

    private static String getLanguage(final Locale locale) {
        final String language = locale.getLanguage();
        return Util.sanitizeStringShort(language);
    }

    private static String getCountry(final Locale locale) {
        final String country = locale.getCountry();
        return Util.sanitizeStringShort(country);
    }

    private static String getScreenSize(final int screenLayout) {
        final int screenSize = screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;

        switch (screenSize) {
            case Configuration.SCREENLAYOUT_SIZE_SMALL:
                return SMALL;
            case Configuration.SCREENLAYOUT_SIZE_NORMAL:
                return NORMAL;
            case Configuration.SCREENLAYOUT_SIZE_LARGE:
                return LARGE;
            case 4:
                return XLARGE;
            default:
                return UNKNOWN;
        }
    }

    private static String getScreenFormat(final int screenLayout) {
        final int screenFormat = screenLayout & Configuration.SCREENLAYOUT_LONG_MASK;

        switch (screenFormat) {
            case Configuration.SCREENLAYOUT_LONG_YES:
                return LONG;
            case Configuration.SCREENLAYOUT_LONG_NO:
                return NORMAL;
            default:
                return UNKNOWN;
        }
    }

    private static String getScreenDensity(final DisplayMetrics displayMetrics) {
        final int density = displayMetrics.densityDpi;
        final int low = (DisplayMetrics.DENSITY_MEDIUM + DisplayMetrics.DENSITY_LOW) / 2;
        final int high = (DisplayMetrics.DENSITY_MEDIUM + DisplayMetrics.DENSITY_HIGH) / 2;

        if (0 == density) {
            return UNKNOWN;
        } else if (density < low) {
            return LOW;
        } else if (density > high) {
            return HIGH;
        }
        return MEDIUM;
    }

    private static String getDisplayWidth(DisplayMetrics displayMetrics) {
        final String displayWidth = String.valueOf(displayMetrics.widthPixels);
        return Util.sanitizeString(displayWidth);
    }

    private static String getDisplayHeight(DisplayMetrics displayMetrics) {
        final String displayHeight = String.valueOf(displayMetrics.heightPixels);
        return Util.sanitizeString(displayHeight);
    }

    private static String getNetworkType(NetworkInfo networkInfo) {
        final String networkType = networkInfo.getTypeName();
        return Util.sanitizeString(networkType);
    }

    private static String getNetworkSubtype(NetworkInfo networkInfo) {
        final String networkSubtype = networkInfo.getSubtypeName();
        return Util.sanitizeString(networkSubtype);
    }

    private static String getSimOperator(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        final String simOperator = telephonyManager.getSimOperator();
        return Util.sanitizeString(simOperator);
    }
}
