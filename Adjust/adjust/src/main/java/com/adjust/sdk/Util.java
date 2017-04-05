//
//  Util.java
//  Adjust
//
//  Created by Christian Wellenbrock on 2012-10-11.
//  Copyright (c) 2012-2014 adjust GmbH. All rights reserved.
//  See the file MIT-LICENSE for copying permission.
//

package com.adjust.sdk;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Looper;
import android.provider.Settings.Secure;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.adjust.sdk.Constants.ENCODING;
import static com.adjust.sdk.Constants.MD5;
import static com.adjust.sdk.Constants.SHA1;

/**
 * Collects utility functions used by Adjust.
 */
public class Util {
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'Z";
    private static final String fieldReadErrorMessage = "Unable to read '%s' field in migration device with message (%s)";
    public static final DecimalFormat SecondsDisplayFormat = new DecimalFormat("0.0");
    public static final SimpleDateFormat dateFormatter = new SimpleDateFormat(DATE_FORMAT, Locale.US);


    private static ILogger getLogger() {
        return AdjustFactory.getLogger();
    }

    protected static String createUuid() {
        return UUID.randomUUID().toString();
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

        return String.format(Locale.US, "'%s'", string);
    }

    public static String getPlayAdId(Context context) {
        return Reflection.getPlayAdId(context);
    }

    public static void getGoogleAdId(Context context, final OnDeviceIdsRead onDeviceIdRead) {
        ILogger logger = AdjustFactory.getLogger();
        if (Looper.myLooper() != Looper.getMainLooper()) {
            logger.debug("GoogleAdId being read in the background");
            String GoogleAdId = Util.getPlayAdId(context);

            logger.debug("GoogleAdId read " + GoogleAdId);
            onDeviceIdRead.onGoogleAdIdRead(GoogleAdId);
            return;
        }

        logger.debug("GoogleAdId being read in the foreground");
        new AsyncTask<Context,Void,String>() {
            @Override
            protected String doInBackground(Context... params) {
                ILogger logger = AdjustFactory.getLogger();
                Context innerContext = params[0];
                String innerResult = Util.getPlayAdId(innerContext);
                logger.debug("GoogleAdId read " + innerResult);
                return innerResult;
            }

            @Override
            protected void onPostExecute(String playAdiId) {
                ILogger logger = AdjustFactory.getLogger();
                onDeviceIdRead.onGoogleAdIdRead(playAdiId);
            }
        }.execute(context);
    }

    public static Boolean isPlayTrackingEnabled(Context context) {
        return Reflection.isPlayTrackingEnabled(context);
    }

    public static String getMacAddress(Context context) {
        return Reflection.getMacAddress(context);
    }

    public static Map<String, String> getPluginKeys(Context context) {
        return Reflection.getPluginKeys(context);
    }
    public static String getAndroidId(Context context) {
        return Reflection.getAndroidId(context);
    }

    public static <T> T readObject(Context context, String filename, String objectName, Class<T> type) {
        Closeable closable = null;
        T object = null;
        try {
            FileInputStream inputStream = context.openFileInput(filename);
            closable = inputStream;

            BufferedInputStream bufferedStream = new BufferedInputStream(inputStream);
            closable = bufferedStream;

            ObjectInputStream objectStream = new ObjectInputStream(bufferedStream);
            closable = objectStream;

            try {
                object = type.cast(objectStream.readObject());
                getLogger().debug("Read %s: %s", objectName, object);
            } catch (ClassNotFoundException e) {
                getLogger().error("Failed to find %s class (%s)", objectName, e.getMessage());
            } catch (ClassCastException e) {
                getLogger().error("Failed to cast %s object (%s)", objectName, e.getMessage());
            } catch (Exception e) {
                getLogger().error("Failed to read %s object (%s)", objectName, e.getMessage());
            }
        } catch (FileNotFoundException e) {
            getLogger().debug("%s file not found", objectName);
        } catch (Exception e) {
            getLogger().error("Failed to open %s file for reading (%s)", objectName, e);
        }
        try {
            if (closable != null) {
                closable.close();
            }
        } catch (Exception e) {
            getLogger().error("Failed to close %s file for reading (%s)", objectName, e);
        }

        return object;
    }

    public static <T> void writeObject(T object, Context context, String filename, String objectName) {
        Closeable closable = null;
        try {
            FileOutputStream outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
            closable = outputStream;

            BufferedOutputStream bufferedStream = new BufferedOutputStream(outputStream);
            closable = bufferedStream;

            ObjectOutputStream objectStream = new ObjectOutputStream(bufferedStream);
            closable = objectStream;

            try {
                objectStream.writeObject(object);

                getLogger().debug("Wrote %s: %s", objectName, object);
            } catch (NotSerializableException e) {
                getLogger().error("Failed to serialize %s", objectName);
            }
        } catch (Exception e) {
            getLogger().error("Failed to open %s for writing (%s)", objectName, e);
        }
        try {
            if (closable != null) {
                closable.close();
            }
        } catch (Exception e) {
            getLogger().error("Failed to close %s file for writing (%s)", objectName, e);
        }
    }

    public static boolean checkPermission(Context context, String permission) {
        int result = context.checkCallingOrSelfPermission(permission);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    public static String readStringField(ObjectInputStream.GetField fields, String name, String defaultValue) {
        return readObjectField(fields, name, defaultValue);
    }

    public static <T> T readObjectField(ObjectInputStream.GetField fields, String name, T defaultValue) {
        try {
            return (T) fields.get(name, defaultValue);
        } catch (Exception e) {
            getLogger().debug(fieldReadErrorMessage, name, e.getMessage());
            return defaultValue;
        }
    }

    public static boolean readBooleanField(ObjectInputStream.GetField fields, String name, boolean defaultValue) {
        try {
            return fields.get(name, defaultValue);
        } catch (Exception e) {
            getLogger().debug(fieldReadErrorMessage, name, e.getMessage());
            return defaultValue;
        }
    }

    public static int readIntField(ObjectInputStream.GetField fields, String name, int defaultValue) {
        try {
            return fields.get(name, defaultValue);
        } catch (Exception e) {
            getLogger().debug(fieldReadErrorMessage, name, e.getMessage());
            return defaultValue;
        }
    }

    public static long readLongField(ObjectInputStream.GetField fields, String name, long defaultValue) {
        try {
            return fields.get(name, defaultValue);
        } catch (Exception e) {
            getLogger().debug(fieldReadErrorMessage, name, e.getMessage());
            return defaultValue;
        }
    }

    public static boolean equalObject(Object first, Object second) {
        if (first == null || second == null) {
            return first == null && second == null;
        }
        return first.equals(second);
    }

    public static boolean equalsDouble(Double first, Double second) {
        if (first == null || second == null) {
            return first == null && second == null;
        }
        return Double.doubleToLongBits(first) == Double.doubleToLongBits(second);
    }

    public static boolean equalString(String first, String second) {
        return equalObject(first, second);
    }

    public static boolean equalEnum(Enum first, Enum second) {
        return equalObject(first, second);
    }

    public static boolean equalLong(Long first, Long second) {
        return equalObject(first, second);
    }

    public static boolean equalInt(Integer first, Integer second) {
        return equalObject(first, second);
    }

    public static boolean equalBoolean(Boolean first, Boolean second) {
        return equalObject(first, second);
    }

    public static int hashBoolean(Boolean value) {
        if (value == null) {
            return 0;
        }
        return value.hashCode();
    }

    public static int hashLong(Long value) {
        if (value == null) {
            return 0;
        }
        return value.hashCode();
    }

    public static int hashString(String value) {
        if (value == null) {
            return 0;
        }
        return value.hashCode();
    }

    public static int hashEnum(Enum value) {
        if (value == null) {
            return 0;
        }
        return value.hashCode();
    }

    public static int hashObject(Object value) {
        if (value == null) {
            return 0;
        }
        return value.hashCode();
    }

    public static String sha1(final String text) {
        return hash(text, SHA1);
    }

    public static String md5(final String text) {
        return hash(text, MD5);
    }

    public static String hash(final String text, final String method) {
        String hashString = null;
        try {
            final byte[] bytes = text.getBytes(ENCODING);
            final MessageDigest mesd = MessageDigest.getInstance(method);
            mesd.update(bytes, 0, bytes.length);
            final byte[] hash = mesd.digest();
            hashString = convertToHex(hash);
        } catch (Exception e) {
        }
        return hashString;
    }

    public static String convertToHex(final byte[] bytes) {
        final BigInteger bigInt = new BigInteger(1, bytes);
        final String formatString = "%0" + (bytes.length << 1) + "x";
        return String.format(Locale.US, formatString, bigInt);
    }

    public static String[] getSupportedAbis() {
        return Reflection.getSupportedAbis();
    }

    public static String getCpuAbi() {
        return Reflection.getCpuAbi();
    }

    public static String getReasonString(String message, Throwable throwable) {
        if (throwable != null) {
            return String.format(Locale.US, "%s: %s", message, throwable);
        } else {
            return String.format(Locale.US, "%s", message);
        }
    }

    public static long getWaitingTime(int retries, BackoffStrategy backoffStrategy) {
        if (retries < backoffStrategy.minRetries) {
            return 0;
        }
        // start with expon 0
        int expon = retries - backoffStrategy.minRetries;
        // get the exponential Time from the power of 2: 1, 2, 4, 8, 16, ... * times the multiplier
        long exponentialTime = ((long) Math.pow(2, expon)) * backoffStrategy.milliSecondMultiplier;
        // limit the maximum allowed time to wait
        long ceilingTime = Math.min(exponentialTime, backoffStrategy.maxWait);
        // get the random range
        double randomDouble = randomInRange(backoffStrategy.minRange, backoffStrategy.maxRange);
        // apply jitter factor
        double waitingTime =  ceilingTime * randomDouble;
        return (long)waitingTime;
    }

    private static double randomInRange(double minRange, double maxRange) {
        Random random = new Random();
        double range = maxRange - minRange;
        double scaled = random.nextDouble() * range;
        double shifted = scaled + minRange;
        return shifted;
    }

    public static boolean isValidParameter(String attribute, String attributeType, String parameterName) {
        if (attribute == null) {
            getLogger().error("%s parameter %s is missing", parameterName, attributeType);
            return false;
        }
        if (attribute.equals("")) {
            getLogger().error("%s parameter %s is empty", parameterName, attributeType);
            return false;
        }

        return true;
    }

    public static Map<String, String> mergeParameters(Map<String, String> target,
                                                      Map<String, String> source,
                                                      String parameterName) {
        if (target == null) {
            return source;
        }
        if (source == null) {
            return target;
        }
        Map<String, String> mergedParameters = new HashMap<String, String>(target);
        ILogger logger = getLogger();
        for (Map.Entry<String, String> parameterSourceEntry : source.entrySet()) {
            String oldValue = mergedParameters.put(parameterSourceEntry.getKey(), parameterSourceEntry.getValue());
            if (oldValue != null) {
                logger.warn("Key %s with value %s from %s parameter was replaced by value %s",
                        parameterSourceEntry.getKey(),
                        oldValue,
                        parameterName,
                        parameterSourceEntry.getValue());
            }
        }
        return mergedParameters;
    }

    public static String getVmInstructionSet() {
        return Reflection.getVmInstructionSet();
    }

    public static Locale getLocale(Configuration configuration) {
        Locale locale = Reflection.getLocaleFromLocaleList(configuration);
        if (locale != null) {
            return locale;
        }
        return Reflection.getLocaleFromField(configuration);
    }

    public static String getFireAdvertisingId(ContentResolver contentResolver) {
        if (contentResolver == null) {
            return null;
        }
        try {
            // get advertising
            return Secure.getString(contentResolver, "advertising_id");
        } catch (Exception ex) {
            // not supported
        }
        return null;
    }

    public static Boolean getFireTrackingEnabled(ContentResolver contentResolver) {
        try {
            // get user's tracking preference
            return Secure.getInt(contentResolver, "limit_ad_tracking") == 0;
        } catch (Exception ex) {
            // not supported
        }
        return null;
    }
}