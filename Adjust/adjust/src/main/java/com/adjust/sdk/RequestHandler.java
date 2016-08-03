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
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

public class RequestHandler extends HandlerThread implements IRequestHandler {
    private Handler internalHandler;
    private IPackageHandler packageHandler;
    private ILogger logger;

    public RequestHandler(IPackageHandler packageHandler) {
        super(Constants.LOGTAG, MIN_PRIORITY);
        setDaemon(true);
        start();

        this.logger = AdjustFactory.getLogger();
        this.internalHandler = new Handler(getLooper());
        init(packageHandler);
    }

    @Override
    public void init(IPackageHandler packageHandler) {
        this.packageHandler = packageHandler;
    }

    @Override
    public void sendPackage(final ActivityPackage activityPackage, final int queueSize) {
        internalHandler.post(new Runnable() {
            @Override
            public void run() {
                sendI(activityPackage, queueSize);
            }
        });
    }

    @Override
    public void teardown() {
        logger.verbose("RequestHandler teardown");
        if (internalHandler != null) {
            internalHandler.removeCallbacksAndMessages(null);
        }
        internalHandler = null;
        packageHandler = null;
        logger = null;

        try {
            interrupt();
        } catch (SecurityException se) {}
        quit();
    }

    private void sendI(ActivityPackage activityPackage, int queueSize) {
        String targetURL = Constants.BASE_URL + activityPackage.getPath();

        try {
            HttpsURLConnection connection = Util.createPOSTHttpsURLConnection(
                    targetURL,
                    activityPackage.getClientSdk(),
                    activityPackage.getParameters(),
                    queueSize);

            ResponseData responseData = Util.readHttpResponse(connection, activityPackage);

            if (responseData.jsonResponse == null) {
                packageHandler.closeFirstPackage(responseData, activityPackage);
                return;
            }

            packageHandler.sendNextPackage(responseData);

        } catch (UnsupportedEncodingException e) {
            sendNextPackageI(activityPackage, "Failed to encode parameters", e);
        } catch (SocketTimeoutException e) {
            closePackageI(activityPackage, "Request timed out", e);
        } catch (IOException e) {
            closePackageI(activityPackage, "Request failed", e);
        } catch (Throwable e) {
            sendNextPackageI(activityPackage, "Runtime exception", e);
        }
    }

    // close current package because it failed
    private void closePackageI(ActivityPackage activityPackage, String message, Throwable throwable) {
        final String packageMessage = activityPackage.getFailureMessage();
        final String reasonString = Util.getReasonString(message, throwable);
        String finalMessage = String.format("%s. (%s) Will retry later", packageMessage, reasonString);
        logger.error(finalMessage);

        ResponseData responseData = ResponseData.buildResponseData(activityPackage);
        responseData.message = finalMessage;

        packageHandler.closeFirstPackage(responseData, activityPackage);
    }

    // send next package because the current package failed
    private void sendNextPackageI(ActivityPackage activityPackage, String message, Throwable throwable) {
        final String failureMessage = activityPackage.getFailureMessage();
        final String reasonString = Util.getReasonString(message, throwable);
        String finalMessage = String.format("%s. (%s)", failureMessage, reasonString);
        logger.error(finalMessage);

        ResponseData responseData = ResponseData.buildResponseData(activityPackage);
        responseData.message = finalMessage;

        packageHandler.sendNextPackage(responseData);
    }
}
