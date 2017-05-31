package com.adjust.sdk;

import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by uerceg on 03/04/2017.
 */

public class UtilNetworking {
    private static String userAgent;

    private static ILogger getLogger() {
        return AdjustFactory.getLogger();
    }

    public static void setUserAgent(String userAgent) {
        UtilNetworking.userAgent = userAgent;
    }

    public static ResponseData createPOSTHttpsURLConnection(String urlString, ActivityPackage activityPackage, int queueSize) throws Exception {
        DataOutputStream wr = null;

        try {
            URL url = new URL(urlString);
            HttpsURLConnection connection = AdjustFactory.getHttpsURLConnection(url);
            Map<String, String> parameters = new HashMap<String, String>(activityPackage.getParameters());
            IConnectionOptions connectionOptions = AdjustFactory.getConnectionOptions();

            connectionOptions.applyConnectionOptions(connection, activityPackage.getClientSdk());

            connection.setRequestMethod("POST");
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            wr = new DataOutputStream(connection.getOutputStream());
            wr.writeBytes(getPostDataString(parameters, queueSize));

            ResponseData responseData = readHttpResponse(connection, activityPackage);

            return responseData;
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                if (wr != null) {
                    wr.flush();
                    wr.close();
                }
            } catch (Exception e) {}
        }
    }

    public static ResponseData createGETHttpsURLConnection(ActivityPackage activityPackage, String basePath) throws Exception {
        try {
            Map<String, String> parameters = new HashMap<String, String>(activityPackage.getParameters());
            Uri uri = buildUri(activityPackage.getPath(), parameters, basePath);
            URL url = new URL(uri.toString());
            HttpsURLConnection connection = AdjustFactory.getHttpsURLConnection(url);
            IConnectionOptions connectionOptions = AdjustFactory.getConnectionOptions();

            connectionOptions.applyConnectionOptions(connection, activityPackage.getClientSdk());

            connection.setRequestMethod("GET");

            ResponseData responseData = readHttpResponse(connection, activityPackage);

            return responseData;
        } catch (Exception e) {
            throw e;
        }
    }

    private static ResponseData readHttpResponse(HttpsURLConnection connection, ActivityPackage activityPackage) throws Exception {
        StringBuffer sb = new StringBuffer();
        ILogger logger = getLogger();
        Integer responseCode = null;

        ResponseData responseData = ResponseData.buildResponseData(activityPackage);

        try {
            connection.connect();

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
        }
        catch (Exception e) {
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

        if (responseCode != null && responseCode == HttpsURLConnection.HTTP_OK) {
            logger.info("%s", message);
            responseData.success = true;
        } else {
            logger.error("%s", message);
        }

        return responseData;
    }

    private static String getPostDataString(Map<String, String> body, int queueSize) throws UnsupportedEncodingException {
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
        String dateString = Util.dateFormatter.format(now);

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

    private static Uri buildUri(String path, Map<String, String> parameters, String basePath) {
        Uri.Builder uriBuilder = new Uri.Builder();

        String scheme = Constants.SCHEME;
        String authority = Constants.AUTHORITY;
        String initialPath = "";

        try {
            String url = AdjustFactory.getBaseUrl();
            if (basePath != null) {
                url += basePath;
            }
            URL baseUrl = new URL(url);
            scheme = baseUrl.getProtocol();
            authority = baseUrl.getAuthority();
            initialPath = baseUrl.getPath();
        } catch (MalformedURLException e) {
            getLogger().error("Unable to parse endpoint (%s)", e.getMessage());
        }

        uriBuilder.scheme(scheme);
        uriBuilder.encodedAuthority(authority);
        uriBuilder.path(initialPath);
        uriBuilder.appendPath(path);

        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            uriBuilder.appendQueryParameter(entry.getKey(), entry.getValue());
        }

        long now = System.currentTimeMillis();
        String dateString = Util.dateFormatter.format(now);

        uriBuilder.appendQueryParameter("sent_at", dateString);

        return uriBuilder.build();
    }

    public interface IConnectionOptions {
        void applyConnectionOptions(HttpsURLConnection connection, String clientSdk);
    }

    static class ConnectionOptions implements IConnectionOptions {
        @Override
        public void applyConnectionOptions(HttpsURLConnection connection, String clientSdk) {
            connection.setRequestProperty("Client-SDK", clientSdk);
            connection.setConnectTimeout(Constants.ONE_MINUTE);
            connection.setReadTimeout(Constants.ONE_MINUTE);

            if (userAgent != null) {
                connection.setRequestProperty("User-Agent", userAgent);
            }
        }
    }
}
