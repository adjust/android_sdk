//
//  RequestHandler.java
//  Adjust
//
//  Created by Christian Wellenbrock on 2013-06-25.
//  Copyright (c) 2013 adjust GmbH. All rights reserved.
//  See the file MIT-LICENSE for copying permission.
//

package com.adjust.sdk;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class RequestHandler extends HandlerThread implements IRequestHandler {
    private InternalHandler internalHandler;
    private IPackageHandler packageHandler;
    private ILogger logger;

    public RequestHandler(IPackageHandler packageHandler) {
        super(Constants.LOGTAG, MIN_PRIORITY);
        setDaemon(true);
        start();

        this.logger = AdjustFactory.getLogger();
        this.internalHandler = new InternalHandler(getLooper(), this);
        init(packageHandler);

        Message message = Message.obtain();
        message.arg1 = InternalHandler.INIT;
        internalHandler.sendMessage(message);
    }

    @Override
    public void init(IPackageHandler packageHandler) {
        this.packageHandler = packageHandler;
    }

    @Override
    public void sendPackage(ActivityPackage pack) {
        Message message = Message.obtain();
        message.arg1 = InternalHandler.SEND;
        message.obj = pack;
        internalHandler.sendMessage(message);
    }

    private static final class InternalHandler extends Handler {
        private static final int INIT = 72401;
        private static final int SEND = 72400;

        private final WeakReference<RequestHandler> requestHandlerReference;

        protected InternalHandler(Looper looper, RequestHandler requestHandler) {
            super(looper);
            this.requestHandlerReference = new WeakReference<RequestHandler>(requestHandler);
        }

        @Override
        public void handleMessage(Message message) {
            super.handleMessage(message);

            RequestHandler requestHandler = requestHandlerReference.get();
            if (requestHandler == null) {
                return;
            }

            switch (message.arg1) {
                case INIT:
                    requestHandler.initInternal();
                    break;
                case SEND:
                    ActivityPackage activityPackage = (ActivityPackage) message.obj;
                    requestHandler.sendInternal(activityPackage);
                    break;
            }
        }
    }

    private void initInternal() {
        // TODO: HttpClient was initialized in here, maybe remove this method?
    }

    private void sendInternal(ActivityPackage activityPackage) {
        URL url;
        HttpURLConnection connection = null;
        String targetURL = Constants.BASE_URL + activityPackage.getPath();

        try {
            //Create connection
            url = new URL(targetURL);
            connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("POST");

            connection.setRequestProperty("Content-Type", URLEncodedUtils.CONTENT_TYPE);
            connection.setRequestProperty("Client-SDK", activityPackage.getClientSdk());
            connection.setRequestProperty("Accept-Language", Locale.getDefault().getLanguage());

            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            // Send request
            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.writeBytes(getPostDataString(activityPackage.getParameters()));
            wr.flush();
            wr.close();

            String response = "";

            if (connection.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                while ((line = br.readLine()) != null) {
                    response += line;
                }
            }

            logger.debug("Response = " + response);

            requestFinished(response);
        } catch (UnsupportedEncodingException e) {
            sendNextPackage(activityPackage, "Failed to encode parameters", e);
        } catch (SocketTimeoutException e) {
            closePackage(activityPackage, "Request timed out", e);
        } catch (IOException e) {
            closePackage(activityPackage, "Request failed", e);
        } catch (Throwable e) {
            sendNextPackage(activityPackage, "Runtime exception", e);
        } finally {
            if(connection != null) {
                connection.disconnect();
            }
        }

        // TODO: Check exception types and possible scenarios.
    }

    private void requestFinished(String response) throws JSONException {
        JSONObject jsonResponse = new JSONObject(response);

        if (jsonResponse == null) {
            packageHandler.closeFirstPackage();
            return;
        }

        packageHandler.finishedTrackingActivity(jsonResponse);
        packageHandler.sendNextPackage();
    }

    // close current package because it failed
    private void closePackage(ActivityPackage activityPackage, String message, Throwable throwable) {
        final String packageMessage = activityPackage.getFailureMessage();
        final String reasonString = getReasonString(message, throwable);
        logger.error("%s. (%s) Will retry later", packageMessage, reasonString);

        packageHandler.closeFirstPackage();
    }

    // send next package because the current package failed
    private void sendNextPackage(ActivityPackage activityPackage, String message, Throwable throwable) {
        final String failureMessage = activityPackage.getFailureMessage();
        final String reasonString = getReasonString(message, throwable);
        logger.error("%s. (%s)", failureMessage, reasonString);

        packageHandler.sendNextPackage();
    }

    private String getReasonString(String message, Throwable throwable) {
        if (throwable != null) {
            return String.format(Locale.US, "%s: %s", message, throwable);
        } else {
            return String.format(Locale.US, "%s", message);
        }
    }

    private String getPostDataString(Map<String, String> params) throws UnsupportedEncodingException{
        boolean first = true;
        StringBuilder result = new StringBuilder();

        for(Map.Entry<String, String> entry : params.entrySet()) {
            if (first) {
                first = false;
            } else {
                result.append("&");
            }

            result.append(URLEncoder.encode(entry.getKey(), Constants.ENCODING));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), Constants.ENCODING));
        }

        long now = System.currentTimeMillis();
        String dateString = Util.dateFormat(now);

        result.append("&");
        result.append(URLEncoder.encode("sent_at", Constants.ENCODING));
        result.append("=");
        result.append(URLEncoder.encode(dateString, Constants.ENCODING));

        return result.toString();
    }
}
