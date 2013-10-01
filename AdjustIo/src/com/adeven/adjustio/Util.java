//
//  Util.java
//  AdjustIo
//
//  Created by Christian Wellenbrock on 2012-10-11.
//  Copyright (c) 2012-2013 adeven. All rights reserved.
//  See the file MIT-LICENSE for copying permission.
//

package com.adeven.adjustio;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Locale;

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
 * Collects utility functions used by AdjustIo.
 */
public class Util {
    protected static final String BASE_URL   = "https://app.adjust.io";
    protected static final String CLIENT_SDK = "android2.1.1";

    private static final String UNKNOWN = "unknown";

    protected static String getUserAgent(Context context) {
        Resources resources = context.getResources();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        Configuration configuration = resources.getConfiguration();
        Locale locale = configuration.locale;
        int screenLayout = configuration.screenLayout;

        String[] parts = {
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
        String userAgent = TextUtils.join(" ", parts);
        return userAgent;
    }

    private static String getPackageName(Context context) {
        String packageName = context.getPackageName();
        String sanitized = sanitizeString(packageName);
        return sanitized;
    }

    private static String getAppVersion(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            String name = context.getPackageName();
            PackageInfo info = packageManager.getPackageInfo(name, 0);
            String versionName = info.versionName;
            String result = sanitizeString(versionName);
            return result;
        } catch (NameNotFoundException e) {
            return UNKNOWN;
        }
    }

    private static String getDeviceType(int screenLayout) {
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
        String deviceName = Build.MODEL;
        String sanitized = sanitizeString(deviceName);
        return sanitized;
    }

    private static String getOsName() {
        return "android";
    }

    private static String getOsVersion() {
        String osVersion = "" + Build.VERSION.SDK_INT;
        String sanitized = sanitizeString(osVersion);
        return sanitized;
    }

    private static String getLanguage(Locale locale) {
        String language = locale.getLanguage();
        String sanitized = sanitizeStringShort(language);
        return sanitized;
    }

    private static String getCountry(Locale locale) {
        String country = locale.getCountry();
        String sanitized = sanitizeStringShort(country);
        return sanitized;
    }

    private static String getScreenSize(int screenLayout) {
        int screenSize = screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;

        switch (screenSize) {
            case Configuration.SCREENLAYOUT_SIZE_SMALL:
                return "small";
            case Configuration.SCREENLAYOUT_SIZE_NORMAL:
                return "normal";
            case Configuration.SCREENLAYOUT_SIZE_LARGE:
                return "large";
            case 4:
                return "xlarge";
            default:
                return UNKNOWN;
        }
    }

    private static String getScreenFormat(int screenLayout) {
        int screenFormat = screenLayout & Configuration.SCREENLAYOUT_LONG_MASK;

        switch (screenFormat) {
            case Configuration.SCREENLAYOUT_LONG_YES:
                return "long";
            case Configuration.SCREENLAYOUT_LONG_NO:
                return "normal";
            default:
                return UNKNOWN;
        }
    }

    private static String getScreenDensity(DisplayMetrics displayMetrics) {
        int density = displayMetrics.densityDpi;
        int low = (DisplayMetrics.DENSITY_MEDIUM + DisplayMetrics.DENSITY_LOW) / 2;
        int high = (DisplayMetrics.DENSITY_MEDIUM + DisplayMetrics.DENSITY_HIGH) / 2;

        if (density == 0) {
            return UNKNOWN;
        } else if (density < low) {
            return "low";
        } else if (density > high) {
            return "high";
        } else {
            return "medium";
        }
    }

    private static String getDisplayWidth(DisplayMetrics displayMetrics) {
        String displayWidth = String.valueOf(displayMetrics.widthPixels);
        String sanitized = sanitizeString(displayWidth);
        return sanitized;
    }

    private static String getDisplayHeight(DisplayMetrics displayMetrics) {
        String displayHeight = String.valueOf(displayMetrics.heightPixels);
        String sanitized = sanitizeString(displayHeight);
        return sanitized;
    }

    protected static String getMacAddress(Context context) {
        String rawAddress = getRawMacAddress(context);
        String upperAddress = rawAddress.toUpperCase(Locale.US);
        String sanitized = sanitizeString(upperAddress);
        return sanitized;
    }

    private static String getRawMacAddress(Context context) {
        // android devices should have a wlan address
        String wlanAddress = loadAddress("wlan0");
        if (wlanAddress != null) {
            return wlanAddress;
        }

        // emulators should have an ethernet address
        String ethAddress = loadAddress("eth0");
        if (ethAddress != null) {
            return ethAddress;
        }

        // query the wifi manager (requires the ACCESS_WIFI_STATE permission)
        try {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            String wifiAddress = wifiManager.getConnectionInfo().getMacAddress();
            if (wifiAddress != null) {
                return wifiAddress;
            }
        } catch (Exception e) {
        }

        return "";
    }

    // removes spaces and replaces empty string with "unknown"
    private static String sanitizeString(String string) {
        return sanitizeString(string, UNKNOWN);
    }

    private static String sanitizeStringShort(String string) {
        return sanitizeString(string, "zz");
    }

    private static String sanitizeString(String string, String defaultString) {
        if (string == null) {
            string = defaultString;
        }

        String result = string.replaceAll("\\s", "");
        if (result.length() == 0) {
            result = defaultString;
        }

        return result;
    }

    protected static String loadAddress(String interfaceName) {
        try {
            String filePath = "/sys/class/net/" + interfaceName + "/address";
            StringBuffer fileData = new StringBuffer(1000);
            BufferedReader reader;
            reader = new BufferedReader(new FileReader(filePath), 1024);
            char[] buf = new char[1024];
            int numRead = 0;

            while ((numRead = reader.read(buf)) != -1) {
                String readData = String.valueOf(buf, 0, numRead);
                fileData.append(readData);
            }

            reader.close();
            String address = fileData.toString();
            return address;
        } catch (IOException e) {
            return null;
        }
    }

    protected static String getAndroidId(Context context) {
        return Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
    }

    protected static String getAttributionId(Context context) {
        try {
            ContentResolver contentResolver = context.getContentResolver();
            Uri uri = Uri.parse("content://com.facebook.katana.provider.AttributionIdProvider");
            String columnName = "aid";
            String[] projection = {columnName};
            Cursor cursor = contentResolver.query(uri, projection, null, null, null);

            if (cursor == null) {
                return null;
            }
            if (!cursor.moveToFirst()) {
                cursor.close();
                return null;
            }

            String attributionId = cursor.getString(cursor.getColumnIndex(columnName));
            cursor.close();
            return attributionId;
        } catch (Exception e) {
            return null;
        }
    }

    protected static String sha1(String text) {
        return hash(text, "SHA-1");
    }

    protected static String md5(String text) {
        return hash(text, "MD5");
    }

    private static String hash(String text, String method) {
        try {
            byte[] bytes = text.getBytes("UTF-8");
            MessageDigest mesd = MessageDigest.getInstance(method);
            mesd.update(bytes, 0, bytes.length);
            byte[] hash = mesd.digest();
            return convertToHex(hash);
        } catch (Exception e) {
            return "";
        }
    }

    private static String convertToHex(byte[] bytes) {
        BigInteger bigInt = new BigInteger(1, bytes);
        String formatString = "%0" + (bytes.length << 1) + "x";
        return String.format(formatString, bigInt);
    }
}
