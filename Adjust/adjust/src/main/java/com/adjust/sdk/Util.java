//
//  Util.java
//  Adjust
//
//  Created by Christian Wellenbrock on 2012-10-11.
//  Copyright (c) 2012-2014 adjust GmbH. All rights reserved.
//  See the file MIT-LICENSE for copying permission.
//

package com.adjust.sdk;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Looper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.text.*;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

import static com.adjust.sdk.Constants.ENCODING;
import static com.adjust.sdk.Constants.MD5;
import static com.adjust.sdk.Constants.SHA1;

/**
 * Collects utility functions used by Adjust.
 */
public class Util {
    public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'Z";
    private static final String fieldReadErrorMessage = "Unable to read '%s' field in migration device with message (%s)";
    public static final DecimalFormat SecondsDisplayFormat = new DecimalFormat("0.0");

    private static String userAgent;

    private static ILogger getLogger() {
        return AdjustFactory.getLogger();
    }

    protected static String createUuid() {
        return UUID.randomUUID().toString();
    }

    public static String quote(final String string) {
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

    public static String getPlayAdId(final Context context) {
        return Reflection.getPlayAdId(context);
    }

    public static void getGoogleAdId(final Context context, final OnDeviceIdsRead onDeviceIdRead) {
        ILogger logger = AdjustFactory.getLogger();
        if (Looper.myLooper() != Looper.getMainLooper()) {
            logger.debug("GoogleAdId being read in the background");
            String GoogleAdId = Util.getPlayAdId(context);

            logger.debug("GoogleAdId read " + GoogleAdId);
            onDeviceIdRead.onGoogleAdIdRead(GoogleAdId);
            return;
        }

        logger.debug("GoogleAdId being read in the foreground");
        new AsyncTask<Context, Void, String>() {
            @Override
            protected String doInBackground(final Context... params) {
                ILogger logger = AdjustFactory.getLogger();
                Context innerContext = params[0];
                String innerResult = Util.getPlayAdId(innerContext);
                logger.debug("GoogleAdId read " + innerResult);
                return innerResult;
            }

            @Override
            protected void onPostExecute(final String playAdiId) {
                onDeviceIdRead.onGoogleAdIdRead(playAdiId);
            }
        }.execute(context);
    }

    public static Boolean isPlayTrackingEnabled(final Context context) {
        return Reflection.isPlayTrackingEnabled(context);
    }

    public static String getMacAddress(final Context context) {
        return Reflection.getMacAddress(context);
    }

    public static Map<String, String> getPluginKeys(final Context context) {
        return Reflection.getPluginKeys(context);
    }

    public static String getAndroidId(final Context context) {
        return Reflection.getAndroidId(context);
    }

    public static <T> T readObject(final Context context,
                                   final String filename,
                                   final String objectName,
                                   final Class<T> type) {
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

    public static <T> void writeObject(final T object,
                                       final Context context,
                                       final String filename,
                                       final String objectName) {
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

    public static ResponseData readHttpResponse(final HttpsURLConnection connection,
                                                final ActivityPackage activityPackage) throws Exception {
        StringBuilder sb = new StringBuilder();
        ILogger logger = getLogger();
        Integer responseCode = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;

        try {
            connection.connect();

            responseCode = connection.getResponseCode();
            InputStream inputStream;

            if (responseCode >= 400) {
                inputStream = connection.getErrorStream();
            } else {
                inputStream = connection.getInputStream();
            }

            inputStreamReader = new InputStreamReader(inputStream);
            bufferedReader = new BufferedReader(inputStreamReader);

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
        } catch (Exception e) {
            logger.error("Failed to read response. (%s)", e.getMessage());
            throw e;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }

            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (inputStreamReader != null) {
                    inputStreamReader.close();
                }
            } catch (Exception e) {
                throw e;
            }
        }

        ResponseData responseData = ResponseData.buildResponseData(activityPackage);

        String stringResponse = sb.toString();
        logger.verbose("Response: %s", stringResponse);

        if (stringResponse.length() == 0) {
            return responseData;
        }

        JSONObject jsonResponse = null;
        try {
            jsonResponse = new JSONObject(stringResponse);
        } catch (JSONException e) {
            String message = String.format("Failed to parse json response. (%s)", e.getMessage());
            logger.error(message);
            responseData.message = message;
        }

        if (jsonResponse == null) {
            return responseData;
        }

        responseData.jsonResponse = jsonResponse;

        String message = jsonResponse.optString("message", null);

        responseData.message = message;
        responseData.timestamp = jsonResponse.optString("timestamp", null);
        responseData.adid = jsonResponse.optString("adid", null);

        if (message == null) {
            message = "No message found";
        }

        if (responseCode == HttpsURLConnection.HTTP_OK) {
            logger.info("%s", message);
            responseData.success = true;
        } else {
            logger.error("%s", message);
        }

        return responseData;
    }

    public static AdjustFactory.URLGetConnection createGETHttpsURLConnection(final String urlString,
                                                                             final String clientSdk)
            throws IOException {
        HttpsURLConnection connection;
        URL url = new URL(urlString);
        AdjustFactory.URLGetConnection urlGetConnection = AdjustFactory.getHttpsURLGetConnection(url);

        connection = urlGetConnection.httpsURLConnection;
        setDefaultHttpsUrlConnectionProperties(connection, clientSdk);

        connection.setRequestMethod("GET");

        return urlGetConnection;
    }

    public static HttpsURLConnection createPOSTHttpsURLConnection(final String urlString,
                                                                  final String clientSdk,
                                                                  final Map<String, String> parameters,
                                                                  final int queueSize)
            throws IOException {
        DataOutputStream dataOutputStream = null;
        HttpsURLConnection connection = null;
        try {
            URL url = new URL(urlString);
            connection = AdjustFactory.getHttpsURLConnection(url);

            setDefaultHttpsUrlConnectionProperties(connection, clientSdk);
            connection.setRequestMethod("POST");

            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            dataOutputStream = new DataOutputStream(connection.getOutputStream());
            dataOutputStream.writeBytes(getPostDataString(parameters, queueSize));

            return connection;
        } catch (IOException e) {
            throw e;
        } finally {
            try {
                if (dataOutputStream != null) {
                    dataOutputStream.flush();
                    dataOutputStream.close();
                }
            } catch (Exception e) {
                throw e;
            }
        }
    }

    private static String getPostDataString(final Map<String, String> body,
                                            final int queueSize) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();

        for (Map.Entry<String, String> entry : body.entrySet()) {
            String encodedName = URLEncoder.encode(entry.getKey(), Constants.ENCODING);
            String value = entry.getValue();
            String encodedValue = value != null ? URLEncoder.encode(value, Constants.ENCODING) : "";
            if (result.length() > 0) {
                result.append("&");
            }

            result.append(encodedName);
            result.append("=");
            result.append(encodedValue);
        }

        long now = System.currentTimeMillis();
        final DateFormat dateFormat = new SimpleDateFormat(Util.DATE_FORMAT);
        String dateString = dateFormat.format(now);

        result.append("&");
        result.append(URLEncoder.encode("sent_at", Constants.ENCODING));
        result.append("=");
        result.append(URLEncoder.encode(dateString, Constants.ENCODING));

        if (queueSize > 0) {
            result.append("&");
            result.append(URLEncoder.encode("queue_size", Constants.ENCODING));
            result.append("=");
            result.append(URLEncoder.encode("" + queueSize, Constants.ENCODING));
        }

        return result.toString();
    }

    public static void setDefaultHttpsUrlConnectionProperties(final HttpsURLConnection connection,
                                                              final String clientSdk) {
        connection.setRequestProperty("Client-SDK", clientSdk);
        connection.setConnectTimeout(Constants.ONE_MINUTE);
        connection.setReadTimeout(Constants.ONE_MINUTE);
        if (userAgent != null) {
            connection.setRequestProperty("User-Agent", userAgent);
        }
    }

    public static boolean checkPermission(final Context context,
                                          final String permission) {
        int result = context.checkCallingOrSelfPermission(permission);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    public static String readStringField(final ObjectInputStream.GetField fields,
                                         final String name,
                                         final String defaultValue) {
        return readObjectField(fields, name, defaultValue);
    }

    public static <T> T readObjectField(final ObjectInputStream.GetField fields,
                                        final String name,
                                        final T defaultValue) {
        try {
            return (T) fields.get(name, defaultValue);
        } catch (Exception e) {
            getLogger().debug(fieldReadErrorMessage, name, e.getMessage());
            return defaultValue;
        }
    }

    public static boolean readBooleanField(final ObjectInputStream.GetField fields,
                                           final String name,
                                           final boolean defaultValue) {
        try {
            return fields.get(name, defaultValue);
        } catch (Exception e) {
            getLogger().debug(fieldReadErrorMessage, name, e.getMessage());
            return defaultValue;
        }
    }

    public static int readIntField(final ObjectInputStream.GetField fields,
                                   final String name,
                                   final int defaultValue) {
        try {
            return fields.get(name, defaultValue);
        } catch (Exception e) {
            getLogger().debug(fieldReadErrorMessage, name, e.getMessage());
            return defaultValue;
        }
    }

    public static long readLongField(final ObjectInputStream.GetField fields,
                                     final String name,
                                     final long defaultValue) {
        try {
            return fields.get(name, defaultValue);
        } catch (Exception e) {
            getLogger().debug(fieldReadErrorMessage, name, e.getMessage());
            return defaultValue;
        }
    }

    public static boolean equalObject(final Object first,
                                      final Object second) {
        if (first == null || second == null) {
            return first == null && second == null;
        }
        return first.equals(second);
    }

    public static boolean equalsDouble(final Double first,
                                       final Double second) {
        if (first == null || second == null) {
            return first == null && second == null;
        }
        return Double.doubleToLongBits(first) == Double.doubleToLongBits(second);
    }

    public static boolean equalString(final String first,
                                      final String second) {
        return equalObject(first, second);
    }

    public static boolean equalEnum(final Enum first, final Enum second) {
        return equalObject(first, second);
    }

    public static boolean equalLong(final Long first, final Long second) {
        return equalObject(first, second);
    }

    public static boolean equalInt(final Integer first, final Integer second) {
        return equalObject(first, second);
    }

    public static boolean equalBoolean(final Boolean first, final Boolean second) {
        return equalObject(first, second);
    }

    public static int hashBoolean(final Boolean value) {
        if (value == null) {
            return 0;
        }
        return value.hashCode();
    }

    public static int hashLong(final Long value) {
        if (value == null) {
            return 0;
        }
        return value.hashCode();
    }

    public static int hashString(final String value) {
        if (value == null) {
            return 0;
        }
        return value.hashCode();
    }

    public static int hashEnum(final Enum value) {
        if (value == null) {
            return 0;
        }
        return value.hashCode();
    }

    public static int hashObject(final Object value) {
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

    public static String getReasonString(final String message,
                                         final Throwable throwable) {
        if (throwable != null) {
            return String.format(Locale.US, "%s: %s", message, throwable);
        } else {
            return String.format(Locale.US, "%s", message);
        }
    }

    public static long getWaitingTime(final int retries,
                                      final BackoffStrategy backoffStrategy) {
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
        double waitingTime = ceilingTime * randomDouble;
        return (long) waitingTime;
    }

    private static double randomInRange(final double minRange,
                                        final double maxRange) {
        Random random = new Random();
        double range = maxRange - minRange;
        double scaled = random.nextDouble() * range;
        return scaled + minRange;
    }

    public static boolean isValidParameter(final String attribute,
                                           final String attributeType,
                                           final String parameterName) {
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

    public static Map<String, String> mergeParameters(final Map<String, String> target,
                                                      final Map<String, String> source,
                                                      final String parameterName) {
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

    public static void setUserAgent(final String userAgent) {
        Util.userAgent = userAgent;
    }
}
