//
//  Util.java
//  Adjust
//
//  Created by Christian Wellenbrock on 2012-10-11.
//  Copyright (c) 2012-2014 adjust GmbH. All rights reserved.
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
import static com.adjust.sdk.Constants.PLUGINS;
import static com.adjust.sdk.Constants.SHA1;
import static com.adjust.sdk.Constants.SMALL;
import static com.adjust.sdk.Constants.UNKNOWN;
import static com.adjust.sdk.Constants.XLARGE;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;

import com.adjust.sdk.plugin.Plugin;

/**
 * Collects utility functions used by Adjust.
 */
public class Util {

    private static SimpleDateFormat dateFormat;
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss:SSS'Z'Z";

    protected static String createUuid() {
        return UUID.randomUUID().toString();
    }

    // removes spaces and replaces empty string with "unknown"
    static String sanitizeString(final String string) {
        return sanitizeString(string, UNKNOWN);
    }

    static String sanitizeStringShort(final String string) {
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

    public static String dateFormat(long date) {
        if (null == dateFormat) {
            dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.US);
        }
        return dateFormat.format(date);
    }

    public static String getPlayAdId(Context context) {
        return Reflection.getPlayAdId(context);
    }

    public static Boolean isPlayTrackingEnabled(Context context) {
        return Reflection.isPlayTrackingEnabled(context);
    }

    public static boolean isGooglePlayServicesAvailable(Context context) {
        return Reflection.isGooglePlayServicesAvailable(context);
    }

    public static String getMacAddress(Context context) {
        return Reflection.getMacAddress(context);
    }

    public static String getMacSha1(String macAddress) {
        if (macAddress == null) {
            return null;
        }
        String macSha1 = sha1(macAddress);

        return macSha1;
    }

    public static String getMacShortMd5(String macAddress) {
        if (macAddress == null) {
            return null;
        }
        String macShort = macAddress.replaceAll(":", "");
        String macShortMd5 = md5(macShort);

        return macShortMd5;
    }

    public static String getAndroidId(Context context) {
        return Reflection.getAndroidId(context);
    }

    private static String sha1(final String text) {
        return hash(text, SHA1);
    }

    private static String md5(final String text) {
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
            return null;
        }
    }

    private static String convertToHex(final byte[] bytes) {
        final BigInteger bigInt = new BigInteger(1, bytes);
        final String formatString = "%0" + (bytes.length << 1) + "x";
        return String.format(formatString, bigInt);
    }

    public static Map<String, String> getPluginKeys(Context context) {
        Map<String, String> pluginKeys = new HashMap<String, String>();

        for (Plugin plugin : getPlugins()) {
            Map.Entry<String, String> pluginEntry = plugin.getParameter(context);
            if (pluginEntry != null) {
                pluginKeys.put(pluginEntry.getKey(), pluginEntry.getValue());
            }
        }

        if (pluginKeys.size() == 0) {
            return null;
        } else {
            return pluginKeys;
        }
    }

    private static List<Plugin> getPlugins() {
        List<Plugin> plugins = new ArrayList<Plugin>(PLUGINS.size());

        for (String pluginName : PLUGINS) {
            Object pluginObject = Reflection.createDefaultInstance(pluginName);
            if (pluginObject != null && pluginObject instanceof Plugin) {
                plugins.add((Plugin) pluginObject);
            }
        }

        return plugins;
    }
}
