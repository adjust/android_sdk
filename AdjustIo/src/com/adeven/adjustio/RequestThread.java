package com.adeven.adjustio;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.SocketTimeoutException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

public class RequestThread extends HandlerThread {
    private static final int CONNECTION_TIMEOUT = 1000 * 5; // 5 seconds TODO: 1 minute?
    private static final int SOCKET_TIMEOUT = 1000 * 5; // 5 seconds TODO: 1 minute?

    private static final int MESSAGE_ARG_INIT = 72401;
    private static final int MESSAGE_ARG_TRACK = 72400;

    private Handler trackingHandler;
    private QueueThread queueThread;
    private HttpClient httpClient;

    public RequestThread(QueueThread queueThread) {
        super(Logger.LOGTAG, MIN_PRIORITY);
        setDaemon(true);
        start();

        this.trackingHandler = new RequestHandler(getLooper(), this);
        this.queueThread = queueThread;

        Message message = Message.obtain();
        message.arg1 = MESSAGE_ARG_INIT;
        trackingHandler.sendMessage(message);
    }

    public void trackPackage(TrackingPackage pack) {
        Message message = Message.obtain();
        message.arg1 = MESSAGE_ARG_TRACK;
        message.obj = pack;
        trackingHandler.sendMessage(message);
    }

    private static final class RequestHandler extends Handler {
        private final WeakReference<RequestThread> requestThreadReference;

        public RequestHandler(Looper looper, RequestThread requestThread) {
            super(looper);
            this.requestThreadReference = new WeakReference<RequestThread>(requestThread);
        }

        public void handleMessage(Message message) {
            super.handleMessage(message);

            RequestThread requestThread = requestThreadReference.get();
            if (requestThread == null) return;

            switch (message.arg1) {
            case MESSAGE_ARG_INIT:
                requestThread.initInternal();
                break;
            case MESSAGE_ARG_TRACK:
                TrackingPackage trackingPackage = (TrackingPackage) message.obj;
                requestThread.trackInternal(trackingPackage);
                break;
            }
        }
    }

    private void initInternal() {
        HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, CONNECTION_TIMEOUT);
        HttpConnectionParams.setSoTimeout(httpParams, SOCKET_TIMEOUT);
        httpClient = new DefaultHttpClient(httpParams);
        Logger.error("init httpclient " + httpClient);
    }

    private void trackInternal(TrackingPackage trackingPackage) {
        // TODO: test all paths!
        try {
            setUserAgent(trackingPackage.userAgent);
            HttpUriRequest request = trackingPackage.getRequest();
            HttpResponse response = httpClient.execute(request);
            requestFinished(response, trackingPackage);
        }
        catch (UnsupportedEncodingException e) {
            Logger.error("failed to encode parameters");
            queueThread.trackNextPackage();
        }
        catch (ClientProtocolException e) {
            closePackage(trackingPackage, "Client protocol error");
        }
        catch (SocketTimeoutException e) {
            closePackage(trackingPackage, "Request timed out");
        }
        catch (IOException e) {
            closePackage(trackingPackage, "Request failed: " + e.getLocalizedMessage());
        }
        catch (RuntimeException e) {
            trackNextPackage(trackingPackage, "Runtime exception: " + e.getClass() + ": " + e.getLocalizedMessage());
        }
    }

    private void closePackage(TrackingPackage trackingPackage, String message) {
        String failureMessage = trackingPackage.getFailureMessage();
        Logger.error(failureMessage + " Will retry later. (" + message + ")");
        queueThread.closeFirstPackage();
    }

    private void trackNextPackage(TrackingPackage trackingPackage, String message) {
        String failureMessage = trackingPackage.getFailureMessage();
        Logger.error(failureMessage + " (No parameters found)");
        queueThread.trackNextPackage();
    }

    private void requestFinished(HttpResponse response, TrackingPackage trackingPackage) {
        if (response == null) { // TODO: test
            Logger.debug(trackingPackage.getFailureMessage() + " (Missing response)"); // TODO: "will retry later" like on ios
            queueThread.closeFirstPackage();
            return;
        }

        int statusCode = response.getStatusLine().getStatusCode();
        String responseString = parseResponse(response);

        if (statusCode == HttpStatus.SC_OK) {
            Logger.info(trackingPackage.getSuccessMessage());
        } else {
            Logger.warn(trackingPackage.getFailureMessage() + " (" + responseString + ")");
        }

        queueThread.trackNextPackage();
    }

    private String parseResponse(HttpResponse response) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            response.getEntity().writeTo(out);
            out.close();
            String responseString = out.toString().trim();
            return responseString;
        }
        catch (Exception e) {
            Logger.error("error parsing response", e);
            return "Failed parsing response";
        }
    }

    private void setUserAgent(String userAgent) {
        HttpParams httpParams = httpClient.getParams();
        httpParams.setParameter(CoreProtocolPNames.USER_AGENT, userAgent);
    }
}
