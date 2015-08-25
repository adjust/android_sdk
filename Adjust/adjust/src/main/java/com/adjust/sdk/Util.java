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
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Map;
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

    private static SimpleDateFormat dateFormat;
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'Z";
    private static final String fieldReadErrorMessage = "Unable to read '%s' field in migration device with message (%s)";

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

    public static String dateFormat(long date) {
        if (dateFormat == null) {
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

    public static <T> T readObject(Context context, String filename, String objectName) {
        Closeable closable = null;
        @SuppressWarnings("unchecked")
        T object = null;
        try {
            FileInputStream inputStream = context.openFileInput(filename);
            closable = inputStream;

            BufferedInputStream bufferedStream = new BufferedInputStream(inputStream);
            closable = bufferedStream;

            ObjectInputStream objectStream = new ObjectInputStream(bufferedStream);
            closable = objectStream;

            try {
                object = (T) objectStream.readObject();
                getLogger().debug("Read %s: %s", objectName, object);
            } catch (ClassNotFoundException e) {
                getLogger().error("Failed to find %s class (%s)", objectName, e.getMessage());
            } catch (ClassCastException e) {
                getLogger().error("Failed to cast %s object (%s)", objectName, e.getMessage());
            } catch (Exception e) {
                getLogger().error("Failed to read %s object (%s)", objectName, e.getMessage());
            }
        } catch (FileNotFoundException e) {
            getLogger().verbose("%s file not found", objectName);
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

    public static JSONObject readHttpResponse(HttpsURLConnection connection) throws Exception {
        StringBuffer sb = new StringBuffer();
        ILogger logger = getLogger();
        Integer responseCode = null;
        try {
            responseCode = connection.getResponseCode();
            InputStream inputStream;

            if (responseCode >= 400) {
                inputStream = connection.getErrorStream();
            } else {
                inputStream = connection.getInputStream();
            }

            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

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
        }
        String stringResponse = sb.toString();
        logger.verbose("Response: %s", stringResponse);

        if (stringResponse == null || stringResponse.length() == 0) {
            return null;
        }

        JSONObject jsonResponse = null;
        try {
            jsonResponse = new JSONObject(stringResponse);
        } catch (JSONException e) {
            getLogger().error("Failed to parse json response. (%s)", e.getMessage());
        }

        if (jsonResponse == null) return null;

        String message = jsonResponse.optString("message", null);

        if (message == null) {
            message = "No message found";
        }

        if (responseCode != null &&
                responseCode == HttpsURLConnection.HTTP_OK) {
            logger.info("%s", message);
        } else {
            logger.error("%s", message);
        }

        return jsonResponse;
    }

    public static HttpsURLConnection createGETHttpsURLConnection(String urlString, String clientSdk)
            throws IOException {
        HttpsURLConnection connection = createHttpsURLConnection(urlString, clientSdk);
        connection.setRequestMethod("GET");

        return connection;
    }

    public static HttpsURLConnection createPOSTHttpsURLConnection(String urlString, String clientSdk, Map<String, String> parameters)
            throws IOException {
        HttpsURLConnection connection = createHttpsURLConnection(urlString, clientSdk);
        connection.setRequestMethod("POST");

        connection.setUseCaches(false);
        connection.setDoInput(true);
        connection.setDoOutput(true);

        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
        wr.writeBytes(getPostDataString(parameters));
        wr.flush();
        wr.close();

        return connection;
    }

    private static String getPostDataString(Map<String, String> body) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();

        for(Map.Entry<String, String> entry : body.entrySet()) {
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
        String dateString = Util.dateFormat(now);

        result.append("&");
        result.append(URLEncoder.encode("sent_at", Constants.ENCODING));
        result.append("=");
        result.append(URLEncoder.encode(dateString, Constants.ENCODING));

        return result.toString();
    }

    public static HttpsURLConnection createHttpsURLConnection(String urlString, String clientSdk)
            throws IOException {
        URL url = new URL(urlString);

        HttpsURLConnection connection = AdjustFactory.getHttpsURLConnection(url);

        connection.setRequestProperty("Client-SDK", clientSdk);
        connection.setConnectTimeout(Constants.ONE_MINUTE);
        connection.setReadTimeout(Constants.ONE_MINUTE);

        return connection;
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

    public static boolean equalsMap(Map first, Map second) {
        if (first == null || second == null) {
            return first == null && second == null;
        }
        return first.entrySet().equals(second.entrySet());
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

    public static int hashMap(Map value) {
        if (value == null) {
            return 0;
        }
        return value.entrySet().hashCode();
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
}
