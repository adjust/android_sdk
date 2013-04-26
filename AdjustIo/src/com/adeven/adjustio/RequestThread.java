//
//  RequestThread.java
//  AdjustIo
//
//  Created by Benjamin Weiss on 17.4.13
//  Copyright (c) 2012 adeven. All rights reserved.
//  See the file MIT-LICENSE for copying permission.
//

package com.adeven.adjustio;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.SocketException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

/**
 * Used to send tracking information.
 *
 * @author keyboardsurfer
 * @since 17.4.13
 */
public class RequestThread extends HandlerThread {

    private static final int MESSAGE_ARG_TRACK = 72400;
    private Handler trackingHandler;

    public RequestThread() {
        super(Logger.LOGTAG, MIN_PRIORITY);
        setDaemon(true);
        start();
        trackingHandler = new RequestHandler(getLooper(), this);
    }

    void track(TrackingPackage information) {
        Message message = Message.obtain();
        message.arg1 = MESSAGE_ARG_TRACK;
        message.obj = information;
        trackingHandler.sendMessage(message);
    }

    private void trackInternal(TrackingPackage trackingInformation) {
        HttpClient httpClient = Util.getHttpClient(trackingInformation.userAgent);
        HttpPost request = Util.getPostRequest(trackingInformation.path);

        try {
            request.setEntity(Util.getEntityEncodedParameters(trackingInformation.parameters));
            HttpResponse response = httpClient.execute(request);
            Logger.info(getLogString(response, trackingInformation));
        } catch (SocketException e) {
            Logger.error("This SDK requires the INTERNET permission. You might need to adjust your manifest. See the README for details.");
        } catch (UnsupportedEncodingException e) {
            Logger.error("Failed to encode parameters.");
        } catch (IOException e) {
            Logger.error("Unexpected IOException", e);
        }
    }

    private String getLogString(HttpResponse response, TrackingPackage trackingInformation) {
        if (response == null) {
            return trackingInformation.failureMessage + " (Request failed)";
        } else {
            int statusCode = response.getStatusLine().getStatusCode();
            String responseString = parseResponse(response);

            if (statusCode == HttpStatus.SC_OK) {
                return trackingInformation.successMessage;
            } else {
                return trackingInformation.failureMessage + " (" + responseString + ")";
            }
        }
    }

    private String parseResponse(HttpResponse response) {
        String responseString = null;

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            response.getEntity().writeTo(out);
            out.close();
            responseString = out.toString().trim();
        } catch (Exception e) {
            Logger.error("error parsing response", e);
            return "Failed parsing response";
        }

        return responseString;
    }

    private static final class RequestHandler extends Handler {
        private final WeakReference<RequestThread> requestThreadReference;

        public RequestHandler(Looper looper, RequestThread requestThread) {
            super(looper);
            this.requestThreadReference = new WeakReference<RequestThread>(requestThread);
        }

        @Override
        public void handleMessage(Message message) {
            super.handleMessage(message);
            RequestThread requestThread = requestThreadReference.get();
            if (requestThread == null) {
                return;
            }
            if (message.arg1 == MESSAGE_ARG_TRACK) {
                requestThread.trackInternal((TrackingPackage) message.obj);
            }
        }
    }
}
