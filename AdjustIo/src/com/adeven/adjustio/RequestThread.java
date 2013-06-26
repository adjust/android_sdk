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

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

/**
 * Used to send tracking packages.
 *
 * @author keyboardsurfer
 * @since 17.4.13
 */
public class RequestThread extends HandlerThread {

    private static final int MESSAGE_ARG_TRACK = 72400;
    private Handler trackingHandler;
    private QueueThread queueThread;

    public RequestThread(QueueThread queueThread) {
        super(Logger.LOGTAG, MIN_PRIORITY);
        setDaemon(true);
        start();

        this.trackingHandler = new RequestHandler(getLooper(), this);
        this.queueThread = queueThread;
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
            if (requestThread == null) {
                return;
            } else if (message.arg1 == MESSAGE_ARG_TRACK) {
                requestThread.trackInternal((TrackingPackage) message.obj);
            }
        }
    }

    private void trackInternal(TrackingPackage trackingPackage) {

        HttpClient httpClient = Util.getHttpClient(trackingPackage.userAgent);
        HttpPost request = Util.getPostRequest(trackingPackage.path);

        // TODO: test all paths!
        // TODO: track next if unsure (because rejected packages will always be retried)
        try {
            trackingPackage.injectEntity(request);
            HttpResponse response = httpClient.execute(request);
            requestFinished(response, trackingPackage);
        } catch (UnsupportedEncodingException e) {
            Logger.error("failed to encode parameters");
            queueThread.trackNextPackage();
        } catch (ClientProtocolException e) {
            Logger.error("client protocol error");
            queueThread.rejectFirstPackage(); // TODO: track next, right?
        } catch (IOException e) {
            Logger.error("connection failed (" + e.getLocalizedMessage() + ")"); // TODO: "will try again later"
            queueThread.rejectFirstPackage();
        } catch (IllegalArgumentException e) {
            Logger.error("Failed to track package (" + e.getLocalizedMessage() + ")");
            queueThread.trackNextPackage();
        }
    }

    private void requestFinished(HttpResponse response, TrackingPackage trackingPackage) {
        if (response == null) { // TODO: test
            Logger.debug(trackingPackage.failureMessage + " (Request failed)"); // TODO: "will retry later" like on ios
            queueThread.rejectFirstPackage();
            return;
        }

        int statusCode = response.getStatusLine().getStatusCode();
        String responseString = parseResponse(response);

        if (statusCode == HttpStatus.SC_OK) {
            Logger.info(trackingPackage.successMessage);
        } else {
            Logger.warn(trackingPackage.failureMessage + " (" + responseString + ")");
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
        } catch (Exception e) {
            Logger.error("error parsing response", e);
            return "Failed parsing response";
        }
    }
}
