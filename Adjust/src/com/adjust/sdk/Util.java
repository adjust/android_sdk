//
//  Util.java
//  Adjust
//
//  Created by Christian Wellenbrock on 2012-10-11.
//  Copyright (c) 2012-2013 adeven. All rights reserved.
//  See the file MIT-LICENSE for copying permission.
//

package com.adjust.sdk;

import static com.adjust.sdk.Constants.ENCODING;
import static com.adjust.sdk.Constants.HIGH;
import static com.adjust.sdk.Constants.LARGE;
import static com.adjust.sdk.Constants.LONG;
import static com.adjust.sdk.Constants.LOW;
import static com.adjust.sdk.Constants.MD5;
import static com.adjust.sdk.Constants.MEDIUM;
import static com.adjust.sdk.Constants.NORMAL;
import static com.adjust.sdk.Constants.SHA1;
import static com.adjust.sdk.Constants.SMALL;
import static com.adjust.sdk.Constants.UNKNOWN;
import static com.adjust.sdk.Constants.XLARGE;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import android.util.DisplayMetrics;


/**
 * Collects utility functions used by Adjust.
 */
public class Util {

    protected static String getUserAgent(final Context context) {
        final Resources resources = context.getResources();
        final DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        final Configuration configuration = resources.getConfiguration();
        final Locale locale = configuration.locale;
        final int screenLayout = configuration.screenLayout;

        final String[] parts = {
          getPackageName(context),
          getAppVersion(context),
          getDeviceType(screenLayout),
          getDeviceName(),
          getOsName(),
          getOsVersion(),
          getLanguage(locale),
          getCountry(locale),
          getScreenSize(screenLayout),
          getScreenFormat(screenLayout),
          getScreenDensity(displayMetrics),
          getDisplayWidth(displayMetrics),
          getDisplayHeight(displayMetrics)
        };
        return TextUtils.join(" ", parts);
    }

    private static String getPackageName(final Context context) {
        final String packageName = context.getPackageName();
        return sanitizeString(packageName);
    }

    private static String getAppVersion(final Context context) {
        try {
            final PackageManager packageManager = context.getPackageManager();
            final String name = context.getPackageName();
            final PackageInfo info = packageManager.getPackageInfo(name, 0);
            final String versionName = info.versionName;
            return sanitizeString(versionName);
        } catch (NameNotFoundException e) {
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
        return sanitizeString(deviceName);
    }

    private static String getOsName() {
        return "android";
    }

    private static String getOsVersion() {
        final String osVersion = "" + Build.VERSION.SDK_INT;
        return sanitizeString(osVersion);
    }

    private static String getLanguage(final Locale locale) {
        final String language = locale.getLanguage();
        return sanitizeStringShort(language);
    }

    private static String getCountry(final Locale locale) {
        final String country = locale.getCountry();
        return sanitizeStringShort(country);
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
        return sanitizeString(displayWidth);
    }

    private static String getDisplayHeight(DisplayMetrics displayMetrics) {
        final String displayHeight = String.valueOf(displayMetrics.heightPixels);
        return sanitizeString(displayHeight);
    }

    protected static String createUuid() {
        return UUID.randomUUID().toString();
    }

    protected static String getMacAddress(Context context) {
        final String rawAddress = getRawMacAddress(context);
        final String upperAddress = rawAddress.toUpperCase(Locale.US);
        return sanitizeString(upperAddress);
    }

    private static String getRawMacAddress(Context context) {
        // android devices should have a wlan address
        final String wlanAddress = loadAddress("wlan0");
        if (wlanAddress != null) {
            return wlanAddress;
        }

        // emulators should have an ethernet address
        final String ethAddress = loadAddress("eth0");
        if (ethAddress != null) {
            return ethAddress;
        }

        // query the wifi manager (requires the ACCESS_WIFI_STATE permission)
        try {
            final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            final String wifiAddress = wifiManager.getConnectionInfo().getMacAddress();
            if (wifiAddress != null) {
                return wifiAddress;
            }
        } catch (Exception e) {
            /* no-op */
        }

        return "";
    }

    // removes spaces and replaces empty string with "unknown"
    private static String sanitizeString(final String string) {
        return sanitizeString(string, UNKNOWN);
    }

    private static String sanitizeStringShort(final String string) {
        return sanitizeString(string, "zz");
    }

    private static String sanitizeString(final String string, final String defaultString) {
        String result = string;
        if (TextUtils.isEmpty(result)) {
            result = defaultString;
        }

        result = result.replaceAll("\\s", "");
        if (TextUtils.isEmpty(result)) {
            result = defaultString;
        }

        return result;
    }

    protected static String loadAddress(final String interfaceName) {
        try {
            final String filePath = "/sys/class/net/" + interfaceName + "/address";
            final StringBuilder fileData = new StringBuilder(1000);
            final BufferedReader reader = new BufferedReader(new FileReader(filePath), 1024);
            final char[] buf = new char[1024];
            int numRead;

            String readData;
            while ((numRead = reader.read(buf)) != -1) {
                readData = String.valueOf(buf, 0, numRead);
                fileData.append(readData);
            }

            reader.close();
            return fileData.toString();
        } catch (IOException e) {
            return null;
        }
    }

    protected static String getAndroidId(final Context context) {
        return Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
    }

    protected static String getAttributionId(final Context context) {
        try {
            final ContentResolver contentResolver = context.getContentResolver();
            final Uri uri = Uri.parse("content://com.facebook.katana.provider.AttributionIdProvider");
            final String columnName = "aid";
            final String[] projection = {columnName};
            final Cursor cursor = contentResolver.query(uri, projection, null, null, null);

            if (null == cursor) {
                return null;
            }
            if (!cursor.moveToFirst()) {
                cursor.close();
                return null;
            }

            final String attributionId = cursor.getString(cursor.getColumnIndex(columnName));
            cursor.close();
            return attributionId;
        } catch (Exception e) {
            return null;
        }
    }

    protected static String sha1(final String text) {
        return hash(text, SHA1);
    }

    protected static String md5(final String text) {
        return hash(text, MD5);
    }

    private static String hash(final String text, final String method) {
        try {
            final byte[] bytes = text.getBytes(ENCODING);
            final MessageDigest mesd = MessageDigest.getInstance(method);
            mesd.update(bytes, 0, bytes.length);
            final byte[] hash = mesd.digest();
            return convertToHex(hash);
        } catch (Exception e) {
            return "";
        }
    }

    private static String convertToHex(final byte[] bytes) {
        final BigInteger bigInt = new BigInteger(1, bytes);
        final String formatString = "%0" + (bytes.length << 1) + "x";
        return String.format(formatString, bigInt);
    }

    public static String quote(String string) {
        if (string == null) {
            return null;
        }

        Pattern pattern = Pattern.compile("\\s");
        Matcher matcher = pattern.matcher(string);
        if (!matcher.find()) {
            return string;
        }

        return String.format("'%s'", string);
    }
}
