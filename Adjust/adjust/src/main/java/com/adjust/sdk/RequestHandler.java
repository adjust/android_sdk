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

import org.json.JSONException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Locale;

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
                case SEND:
                    ActivityPackage activityPackage = (ActivityPackage) message.obj;
                    requestHandler.sendInternal(activityPackage);
                    break;
            }
        }
    }

    private void sendInternal(ActivityPackage activityPackage) {
        URL url;
        String targetURL = Constants.BASE_URL + activityPackage.getPath();

        try {
            HttpsURLConnection connection = Util.createPOSTHttpsURLConnection(
                    targetURL,
                    activityPackage.getClientSdk(),
                    activityPackage.getParameters());

            ResponseData responseData = Util.readHttpResponse(connection);
            responseData.activityKindString = activityPackage.getActivityKind().toString();

            requestFinished(responseData);
        } catch (UnsupportedEncodingException e) {
            sendNextPackage(activityPackage, "Failed to encode parameters", e);
        } catch (SocketTimeoutException e) {
            closePackage(activityPackage, "Request timed out", e);
        } catch (IOException e) {
            closePackage(activityPackage, "Request failed", e);
        } catch (Throwable e) {
            sendNextPackage(activityPackage, "Runtime exception", e);
        }
    }

    private void requestFinished(ResponseData responseData) throws JSONException {
        if (responseData.jsonResponse == null) {
            packageHandler.closeFirstPackage(responseData);
            return;
        }

        packageHandler.sendNextPackage(responseData);
    }

    // close current package because it failed
    private void closePackage(ActivityPackage activityPackage, String message, Throwable throwable) {
        final String packageMessage = activityPackage.getFailureMessage();
        final String reasonString = getReasonString(message, throwable);
        String finalMessage = String.format("%s. (%s) Will retry later", packageMessage, reasonString);
        logger.error(finalMessage);

        ResponseData responseData = new ResponseData();
        responseData.message = finalMessage;
        responseData.activityKindString = activityPackage.getActivityKind().toString();

        packageHandler.closeFirstPackage(responseData);
    }

    // send next package because the current package failed
    private void sendNextPackage(ActivityPackage activityPackage, String message, Throwable throwable) {
        final String failureMessage = activityPackage.getFailureMessage();
        final String reasonString = getReasonString(message, throwable);
        String finalMessage = String.format("%s. (%s)", failureMessage, reasonString);
        logger.error(finalMessage);

        ResponseData responseData = new ResponseData();
        responseData.message = finalMessage;
        responseData.activityKindString = activityPackage.getActivityKind().toString();

        packageHandler.sendNextPackage(responseData);
    }

    private String getReasonString(String message, Throwable throwable) {
        if (throwable != null) {
            return String.format(Locale.US, "%s: %s", message, throwable);
        } else {
            return String.format(Locale.US, "%s", message);
        }
    }
}
