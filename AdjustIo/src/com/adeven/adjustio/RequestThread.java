//
//  RequestThread.java
//  AdjustIo
//
//  Created by Benjamin Weiss on 17.4.13
//  Copyright (c) 2012 adeven. All rights reserved.
//  See the file MIT-LICENSE for copying permission.
//

package com.adeven.adjustio;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;

/**
 * Used to send tracking information.
 *
 * @author keyboardsurfer
 * @since 17.4.13
 */
class RequestThread extends HandlerThread {

    private static final int MESSAGE_ARG_TRACK = 72400;
    private Handler trackingHandler;

    RequestThread() {
        super(Util.LOGTAG, MIN_PRIORITY);
        setDaemon(true);
        start();
        trackingHandler = new Handler(getLooper()) {
            @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    if (MESSAGE_ARG_TRACK == msg.arg1) {
                        trackInternal((TrackingInformation) msg.obj);
                    }
                }
        };
    }

    void track(TrackingInformation information) {
        Message message = Message.obtain();
        message.arg1 = MESSAGE_ARG_TRACK;
        message.obj = information;
        trackingHandler.sendMessage(message);
    }

    private void trackInternal(TrackingInformation trackingInformation) {
        HttpClient httpClient = Util.getHttpClient(trackingInformation.userAgent);
        HttpPost request = Util.getPostRequest(trackingInformation.path);

        try {
            request.setEntity(Util.getEntityEncodedParameters(trackingInformation.trackingParameters));
            HttpResponse response = httpClient.execute(request);
            Logger.d(Util.LOGTAG, getLogString(response, trackingInformation));
        } catch (SocketException e) {
            Logger.e(Util.LOGTAG,
                    "This SDK requires the INTERNET permission. You might need to adjust your manifest. See the README for details.");
        } catch (UnsupportedEncodingException e) {
            Logger.d(Util.LOGTAG, "Failed to encode parameters.");
        } catch (IOException e) {
            Logger.d(Util.LOGTAG, "Unexpected IOException", e);
        }
    }

    private String getLogString(HttpResponse response, TrackingInformation trackingInformation) {
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
            Logger.d(Util.LOGTAG, "error parsing response", e);
            return "Failed parsing response";
        }

        return responseString;
    }
}
